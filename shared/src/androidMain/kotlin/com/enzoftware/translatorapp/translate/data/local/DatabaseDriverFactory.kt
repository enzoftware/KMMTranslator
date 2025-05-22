package com.enzoftware.translatorapp.translate.data.local

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.enzoftware.translatorapp.TranslatorDatabase

actual class DatabaseDriverFactory(
    private val context: Context
) {
    actual fun create(): SqlDriver {
        return AndroidSqliteDriver(
            TranslatorDatabase.Schema, context, "translator.db",
        )
    }
}