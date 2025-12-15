package com.example.brazememoryleak.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

enum class ActivityTag {
    MAIN,
    SECOND,
    THIRD
}

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val lastActivityKey = stringPreferencesKey("last_activity")

    val lastActivityFlow: Flow<ActivityTag> = context.dataStore.data.map { preferences ->
        val tag = preferences[lastActivityKey] ?: ActivityTag.MAIN.name
        try {
            ActivityTag.valueOf(tag)
        } catch (e: IllegalArgumentException) {
            ActivityTag.MAIN
        }
    }

    suspend fun getLastActivity(): ActivityTag {
        return lastActivityFlow.first()
    }

    suspend fun setLastActivity(tag: ActivityTag) {
        context.dataStore.edit { preferences ->
            preferences[lastActivityKey] = tag.name
        }
    }
}
