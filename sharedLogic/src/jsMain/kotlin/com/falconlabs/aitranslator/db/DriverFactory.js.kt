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

import app.cash.sqldelight.db.SqlDriver

/**
 * JS/Web [DriverFactory] implementation placeholder.
 * The web target uses the SQLDelight web-worker-driver which requires async initialization.
 * Full implementation will be wired in a future task when the web target is fully supported.
 */
actual class DriverFactory {
    actual fun createDriver(): SqlDriver = throw UnsupportedOperationException(
        "JS driver requires async initialization via initSqlDriver(). " +
            "Use the async createDriverAsync() API instead."
    )
}
