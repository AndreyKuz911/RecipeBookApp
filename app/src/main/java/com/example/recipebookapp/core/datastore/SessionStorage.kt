package com.example.recipebookapp.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.sessionDataStore by preferencesDataStore(name = "recipe_book_session")

@Singleton
class SessionStorage @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val tokenKey = stringPreferencesKey("token")

    val tokenFlow: Flow<String?> = context.sessionDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences -> preferences[tokenKey] }

    suspend fun saveToken(token: String) {
        context.sessionDataStore.edit { preferences ->
            preferences[tokenKey] = token
        }
    }

    suspend fun clearToken() {
        context.sessionDataStore.edit { preferences ->
            preferences.remove(tokenKey)
        }
    }
}
