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

/**
 * Platform-agnostic interface for in-app purchase operations.
 *
 * Monetization rules (Requirement 12):
 * - "Buy Me a Coffee" is a one-time $20.00 USD purchase.
 * - The purchase removes ALL ads permanently.
 * - Purchase state persists across reinstalls via Play Store account linking.
 * - Purchase state is verified via the platform billing service and cached locally.
 *
 * Platform implementations:
 * - Android: [PlayBillingRepository] — Google Play Billing Library.
 * - Desktop / iOS: No-op implementation (no in-app purchases or platform-specific billing).
 */
interface BillingRepository {

    /**
     * Returns true if the user has an active "remove_ads_coffee" purchase.
     *
     * The implementation should check the local cache first for instant response,
     * and periodically verify with the billing service in the background.
     *
     * @return true if the user owns the "Buy Me a Coffee" product.
     */
    fun hasRemoveAdsPurchase(): Boolean

    /**
     * Initiates the "Buy Me a Coffee" purchase flow.
     *
     * On Android, this launches the Google Play billing dialog.
     * On completion (success or failure), the purchase state is updated locally.
     *
     * @return [PurchaseResult] indicating success, cancellation, or error.
     */
    suspend fun purchaseRemoveAds(): PurchaseResult
}

/**
 * Result of a purchase attempt.
 */
sealed interface PurchaseResult {
    /** Purchase completed successfully. Ads are now removed. */
    data object Success : PurchaseResult

    /** User cancelled the purchase flow. */
    data object Cancelled : PurchaseResult

    /** Purchase failed with an error. */
    data class Error(val message: String) : PurchaseResult

    /** Billing service is unavailable (e.g., offline, unsupported platform). */
    data object Unavailable : PurchaseResult
}

/**
 * No-op [BillingRepository] for platforms without billing support (Desktop, iOS stub).
 * [hasRemoveAdsPurchase] always returns true (ad-free by default on these platforms).
 */
class NoOpBillingRepository : BillingRepository {

    override fun hasRemoveAdsPurchase(): Boolean = true

    override suspend fun purchaseRemoveAds(): PurchaseResult = PurchaseResult.Unavailable
}
