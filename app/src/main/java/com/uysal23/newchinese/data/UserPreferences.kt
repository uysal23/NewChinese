package com.uysal23.newchinese.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.uysal23.newchinese.notifications.ReminderSpec
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_preferences")

data class UserSettings(
    val userName: String = "",
    val darkMode: Boolean = false,
    val palette: String = "PURPLE",
    val showPinyin: Boolean = true,
    val showTurkish: Boolean = true,
    val favoriteWordIds: Set<String> = emptySet(),
    val reminders: List<ReminderSpec> = emptyList(),
    val playbackSpeed: Float = 1.0f
)

class UserPreferences(private val context: Context) {
    private object Keys {
        val USER_NAME = stringPreferencesKey("user_name")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val PALETTE = stringPreferencesKey("palette")
        val SHOW_PINYIN = booleanPreferencesKey("show_pinyin")
        val SHOW_TURKISH = booleanPreferencesKey("show_turkish")
        val FAVORITE_WORD_IDS = stringSetPreferencesKey("favorite_word_ids")
        val REMINDERS = stringSetPreferencesKey("reminders")
        val PLAYBACK_SPEED = floatPreferencesKey("playback_speed")
    }

    val settings: Flow<UserSettings> = context.dataStore.data.map { p ->
        UserSettings(
            userName = p[Keys.USER_NAME] ?: "",
            darkMode = p[Keys.DARK_MODE] ?: false,
            palette = p[Keys.PALETTE] ?: "PURPLE",
            showPinyin = p[Keys.SHOW_PINYIN] ?: true,
            showTurkish = p[Keys.SHOW_TURKISH] ?: true,
            favoriteWordIds = p[Keys.FAVORITE_WORD_IDS] ?: emptySet(),
            reminders = (p[Keys.REMINDERS] ?: emptySet())
                .mapNotNull(ReminderSpec::decode)
                .sortedWith(compareBy({ it.hour }, { it.minute }, { it.id })),
            playbackSpeed = p[Keys.PLAYBACK_SPEED] ?: 1.0f
        )
    }

    suspend fun setUserName(value: String) = context.dataStore.edit { it[Keys.USER_NAME] = value.trim() }
    suspend fun setDarkMode(value: Boolean) = context.dataStore.edit { it[Keys.DARK_MODE] = value }
    suspend fun setPalette(value: String) = context.dataStore.edit { it[Keys.PALETTE] = value }
    suspend fun setShowPinyin(value: Boolean) = context.dataStore.edit { it[Keys.SHOW_PINYIN] = value }
    suspend fun setShowTurkish(value: Boolean) = context.dataStore.edit { it[Keys.SHOW_TURKISH] = value }
    suspend fun setPlaybackSpeed(value: Float) = context.dataStore.edit { it[Keys.PLAYBACK_SPEED] = value }

    suspend fun toggleFavorite(wordId: String) = context.dataStore.edit { prefs ->
        val current = prefs[Keys.FAVORITE_WORD_IDS] ?: emptySet()
        prefs[Keys.FAVORITE_WORD_IDS] =
            if (wordId in current) current - wordId else current + wordId
    }

    suspend fun upsertReminder(reminder: ReminderSpec) = context.dataStore.edit { prefs ->
        val current = (prefs[Keys.REMINDERS] ?: emptySet())
            .mapNotNull(ReminderSpec::decode)
            .filterNot { it.id == reminder.id }
        prefs[Keys.REMINDERS] = (current + reminder).map { it.encode() }.toSet()
    }

    suspend fun deleteReminder(reminderId: Int) = context.dataStore.edit { prefs ->
        val current = (prefs[Keys.REMINDERS] ?: emptySet())
            .mapNotNull(ReminderSpec::decode)
            .filterNot { it.id == reminderId }
        prefs[Keys.REMINDERS] = current.map { it.encode() }.toSet()
    }
}
