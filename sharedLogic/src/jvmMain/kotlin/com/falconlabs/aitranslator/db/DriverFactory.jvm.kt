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
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver

import java.io.File

/**
 * JVM/Desktop [DriverFactory] implementation using [JdbcSqliteDriver].
 * Stores the database in the user's home directory under `.lexi/`.
 *
 * @param dbPath Optional path to the database file. Defaults to `~/.lexi/lexi.db`.
 */
actual class DriverFactory(private val dbPath: String = defaultDbPath()) {
    actual fun createDriver(): SqlDriver {
        val dbFile = File(dbPath)
        dbFile.parentFile?.mkdirs()

        val driver = JdbcSqliteDriver("jdbc:sqlite:$dbPath")

        if (!dbFile.exists() || dbFile.length() == 0L) {
            LexiDatabase.Schema.create(driver)
        }

        return driver
    }

    companion object {
        private fun defaultDbPath(): String {
            val userHome = System.getProperty("user.home")
            return "$userHome${File.separator}.lexi${File.separator}lexi.db"
        }
    }
}
