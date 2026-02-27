package com.dagplanner.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
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
    }

    /** Het gekoppelde Google account (null als niet gekoppeld) */
    val googleAccountName: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[GOOGLE_ACCOUNT_NAME]
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
}
