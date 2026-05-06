package com.example.recipebookapp.feature_auth.domain

import com.example.recipebookapp.core.common.Resource
import com.example.recipebookapp.feature_auth.domain.model.AuthSession
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val isAuthorized: Flow<Boolean>

    suspend fun login(email: String, password: String): Resource<AuthSession>

    suspend fun register(email: String, username: String, password: String): Resource<AuthSession>

    suspend fun logout()
}
