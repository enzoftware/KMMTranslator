package com.enzoftware.translatorapp.translate.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.enzoftware.translatorapp.TranslatorDatabase

actual class DatabaseDriverFactory {
    actual fun create(): SqlDriver {
        return NativeSqliteDriver(TranslatorDatabase.Schema, "translator.db")
    }
}