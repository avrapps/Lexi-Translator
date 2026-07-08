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

package com.falconlabs.aitranslator.db

import android.content.Context

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import net.sqlcipher.database.SupportFactory

/**
 * Android [DriverFactory] implementation using [AndroidSqliteDriver] with SQLCipher
 * AES-256 encryption. The database file is stored in the app's private data directory.
 *
 * @param context Application context for database file location
 * @param passphrase AES-256 encryption passphrase for SQLCipher
 */
actual class DriverFactory(private val context: Context, private val passphrase: ByteArray) {
    actual fun createDriver(): SqlDriver {
        val factory = SupportFactory(passphrase)
        return AndroidSqliteDriver(
            schema = LexiDatabase.Schema,
            context = context,
            name = "lexi.db",
            factory = factory
        )
    }
}
