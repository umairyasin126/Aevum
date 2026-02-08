package com.example.aevum.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class UserPreferences(private val context: Context) {
    private val START_DATE_KEY = longPreferencesKey("start_date")

    val startDate: Flow<Long?> = context.dataStore.data
        .map { preferences ->
            preferences[START_DATE_KEY]
        }

    suspend fun setStartDate(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[START_DATE_KEY] = timestamp
        }
    }
}
