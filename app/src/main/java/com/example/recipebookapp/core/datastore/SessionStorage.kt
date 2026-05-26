package com.example.recipebookapp.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.sessionDataStore by preferencesDataStore(name = "recipe_book_session")

@Singleton
class SessionStorage @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val tokenKey = stringPreferencesKey("token")
    private val tokenState = MutableStateFlow<String?>(null)

    val tokenFlow: Flow<String?> = context.sessionDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences -> preferences[tokenKey] }
        .distinctUntilChanged()

    val currentToken: String?
        get() = tokenState.value

    init {
        scope.launch {
            tokenFlow.collect { tokenState.value = it }
        }
    }

    suspend fun saveToken(token: String) {
        tokenState.value = token
        context.sessionDataStore.edit { preferences ->
            preferences[tokenKey] = token
        }
    }

    suspend fun clearToken() {
        tokenState.value = null
        context.sessionDataStore.edit { preferences ->
            preferences.remove(tokenKey)
        }
    }
}
