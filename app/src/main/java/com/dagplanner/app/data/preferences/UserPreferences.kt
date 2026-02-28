package com.dagplanner.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dagplanner.app.ui.theme.AppTheme
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val GOOGLE_ACCOUNT_NAME = stringPreferencesKey("google_account_name")
        val SELECTED_THEME = stringPreferencesKey("selected_theme")
        val HOUSEHOLD_ID = stringPreferencesKey("household_id")
    }

    /** Het gekoppelde Google account (null als niet gekoppeld) */
    val googleAccountName: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[GOOGLE_ACCOUNT_NAME]
    }

    /** Het geselecteerde thema */
    val selectedTheme: Flow<AppTheme> = context.dataStore.data.map { prefs ->
        val name = prefs[SELECTED_THEME] ?: AppTheme.OCEAAN_BLAUW.name
        try { AppTheme.valueOf(name) } catch (e: IllegalArgumentException) { AppTheme.OCEAAN_BLAUW }
    }

    suspend fun setGoogleAccountName(name: String) {
        context.dataStore.edit { prefs ->
            prefs[GOOGLE_ACCOUNT_NAME] = name
        }
    }

    suspend fun clearGoogleAccount() {
        context.dataStore.edit { prefs ->
            prefs.remove(GOOGLE_ACCOUNT_NAME)
        }
    }

    val householdId: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[HOUSEHOLD_ID]
    }

    suspend fun setHouseholdId(id: String) {
        context.dataStore.edit { prefs ->
            prefs[HOUSEHOLD_ID] = id
        }
    }

    suspend fun clearHouseholdId() {
        context.dataStore.edit { prefs ->
            prefs.remove(HOUSEHOLD_ID)
        }
    }

    suspend fun setTheme(theme: AppTheme) {
        context.dataStore.edit { prefs ->
            prefs[SELECTED_THEME] = theme.name
        }
    }
}
