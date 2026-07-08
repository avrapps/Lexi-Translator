/*
 * Lexi Translator — Offline AI Translation Platform
 * Copyright (C) 2024-2026 ANRMS PRIVATE LIMITED
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * COPYLEFT: Using any part of this code requires you to publish your
 * ENTIRE source code under AGPL-3.0. No exceptions. No closed-source use.
 */

package com.falconlabs.aitranslator.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Android implementation of [BillingRepository] using Google Play Billing Library.
 *
 * Handles the "Buy Me a Coffee" one-time purchase that permanently removes all ads.
 *
 * Product details:
 * - Product ID: [PRODUCT_ID] ("remove_ads_coffee")
 * - Type: One-time purchase (INAPP)
 * - Price: $20.00 USD (configured in Play Console)
 * - Persistence: Linked to Play Store account — survives reinstalls.
 *
 * Purchase flow:
 * 1. [PlayBillingRepository] connects to Google Play Billing on initialization.
 * 2. On connection, it queries existing purchases to restore purchase state.
 * 3. [purchaseRemoveAds] launches the Play billing dialog from the foreground Activity.
 * 4. On successful purchase, [hasRemoveAdsPurchase] returns true immediately (cached locally).
 *
 * @param context Application context for BillingClient initialization.
 */
class PlayBillingRepository(private val context: Context) : BillingRepository {

    private var purchasedLocally: Boolean = false

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        }
    }

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases()
        .build()

    init {
        connectAndQueryPurchases()
    }

    override fun hasRemoveAdsPurchase(): Boolean = purchasedLocally

    override suspend fun purchaseRemoveAds(): PurchaseResult {
        if (!billingClient.isReady) {
            return PurchaseResult.Unavailable
        }

        val activity = resolveCurrentActivity() ?: return PurchaseResult.Error("No foreground Activity available")

        val productDetailsResult = queryProductDetails() ?: return PurchaseResult.Error("Failed to query product details")

        val productDetailsParams = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetailsResult)
                .build(),
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParams)
            .build()

        val billingResult = billingClient.launchBillingFlow(activity, billingFlowParams)

        return when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                // Purchase result is delivered asynchronously via PurchasesUpdatedListener.
                // The local cache will be updated in handlePurchase().
                PurchaseResult.Success
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> PurchaseResult.Cancelled
            else -> PurchaseResult.Error(billingResult.debugMessage)
        }
    }

    /**
     * Connects to Google Play Billing and queries existing purchases to restore
     * purchase state on app launch. This ensures "Buy Me a Coffee" persists
     * across reinstalls via Play Store account linking (Requirement 12.4).
     */
    private fun connectAndQueryPurchases() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryExistingPurchases()
                }
            }

            override fun onBillingServiceDisconnected() {
                // Billing service disconnected; will reconnect on next operation.
            }
        })
    }

    /**
     * Queries the Play Store for existing one-time purchases.
     * If [PRODUCT_ID] is found and acknowledged, updates local purchase state.
     */
    private fun queryExistingPurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                for (purchase in purchases) {
                    if (purchase.products.contains(PRODUCT_ID) &&
                        purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                    ) {
                        purchasedLocally = true
                        acknowledgePurchaseIfNeeded(purchase)
                    }
                }
            }
        }
    }

    /**
     * Handles a new purchase from the [PurchasesUpdatedListener].
     * Acknowledges the purchase if not already acknowledged.
     */
    private fun handlePurchase(purchase: Purchase) {
        if (purchase.products.contains(PRODUCT_ID) &&
            purchase.purchaseState == Purchase.PurchaseState.PURCHASED
        ) {
            purchasedLocally = true
            acknowledgePurchaseIfNeeded(purchase)
        }
    }

    /**
     * Acknowledges a purchase if it hasn't been acknowledged yet.
     * Google Play requires acknowledgment within 3 days or the purchase is refunded.
     */
    private fun acknowledgePurchaseIfNeeded(purchase: Purchase) {
        if (purchase.isAcknowledged) return

        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.acknowledgePurchase(acknowledgePurchaseParams) { _ ->
            // Acknowledgment completed; no additional state change needed.
        }
    }

    /**
     * Queries Play Store for product details of [PRODUCT_ID].
     * Returns null if the query fails or the product is not found.
     */
    private suspend fun queryProductDetails(): ProductDetails? = suspendCancellableCoroutine { continuation ->
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_ID)
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                continuation.resume(productDetailsList.firstOrNull())
            } else {
                continuation.resume(null)
            }
        }
    }

    /**
     * Resolves the current foreground Activity from context.
     * Required to launch the billing flow dialog.
     */
    private fun resolveCurrentActivity(): Activity? {
        var ctx = context
        while (ctx is android.content.ContextWrapper) {
            if (ctx is Activity) return ctx
            ctx = ctx.baseContext
        }
        return null
    }

    companion object {
        /**
         * Product ID for the "Buy Me a Coffee" one-time purchase.
         * Must match the product ID configured in Google Play Console.
         */
        const val PRODUCT_ID = "remove_ads_coffee"
    }
}
