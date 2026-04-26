package com.example.firstprac.data.local
import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


// Создаем инстанс DataStore
private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {

    // Ключи, по которым будем сохранять данные
    private val KEY_USERNAME = stringPreferencesKey("pref_username")
    private val KEY_MIN_STARS = intPreferencesKey("pref_min_stars")
    private val KEY_LANGUAGE = stringPreferencesKey("pref_language")
    private val NAME_KEY = stringPreferencesKey("user_name")
    private val AVATAR_KEY = stringPreferencesKey("avatar_uri")
    private val RESUME_KEY = stringPreferencesKey("resume_url")

    // Получаем настройки
    val settingsFlow: Flow<UserSettings> = context.dataStore.data.map { pref ->
        UserSettings(
            username = pref[KEY_USERNAME] ?: "google", // значение по умолчанию
            minStars = pref[KEY_MIN_STARS] ?: 0,
            language = pref[KEY_LANGUAGE] ?: ""
        )
    }

    // Сохраняем новые настройки
    suspend fun saveSettings(settings: UserSettings) {
        context.dataStore.edit { pref ->
            pref[KEY_USERNAME] = settings.username
            pref[KEY_MIN_STARS] = settings.minStars
            pref[KEY_LANGUAGE] = settings.language
        }
    }

    // Чтение профиля
    val profileFlow: Flow<UserProfile> = context.dataStore.data.map { prefs ->
        UserProfile(
            name = prefs[NAME_KEY] ?: "",
            avatarUri = prefs[AVATAR_KEY] ?: "",
            resumeUrl = prefs[RESUME_KEY] ?: ""
        )
    }

    // Сохранение профиля
    suspend fun saveProfile(profile: UserProfile) {
        context.dataStore.edit { prefs ->
            prefs[NAME_KEY] = profile.name
            prefs[AVATAR_KEY] = profile.avatarUri
            prefs[RESUME_KEY] = profile.resumeUrl
        }
    }
}

// Класс для группировки данных
data class UserSettings(
    val username: String,
    val minStars: Int,
    val language: String
)

