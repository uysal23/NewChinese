package com.uysal23.newchinese.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_preferences")

data class UserSettings(
    val userName: String = "",
    val darkMode: Boolean = false,
    val palette: String = "PURPLE",
    val showPinyin: Boolean = true,
    val showTurkish: Boolean = true
)

class UserPreferences(private val context: Context) {
    private object Keys {
        val USER_NAME = stringPreferencesKey("user_name")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val PALETTE = stringPreferencesKey("palette")
        val SHOW_PINYIN = booleanPreferencesKey("show_pinyin")
        val SHOW_TURKISH = booleanPreferencesKey("show_turkish")
    }

    val settings: Flow<UserSettings> = context.dataStore.data.map { p ->
        UserSettings(
            userName = p[Keys.USER_NAME] ?: "",
            darkMode = p[Keys.DARK_MODE] ?: false,
            palette = p[Keys.PALETTE] ?: "PURPLE",
            showPinyin = p[Keys.SHOW_PINYIN] ?: true,
            showTurkish = p[Keys.SHOW_TURKISH] ?: true
        )
    }

    suspend fun setUserName(value: String) = context.dataStore.edit { it[Keys.USER_NAME] = value.trim() }
    suspend fun setDarkMode(value: Boolean) = context.dataStore.edit { it[Keys.DARK_MODE] = value }
    suspend fun setPalette(value: String) = context.dataStore.edit { it[Keys.PALETTE] = value }
    suspend fun setShowPinyin(value: Boolean) = context.dataStore.edit { it[Keys.SHOW_PINYIN] = value }
    suspend fun setShowTurkish(value: Boolean) = context.dataStore.edit { it[Keys.SHOW_TURKISH] = value }
}
