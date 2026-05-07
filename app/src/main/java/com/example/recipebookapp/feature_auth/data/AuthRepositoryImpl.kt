package com.example.recipebookapp.feature_auth.data

import com.example.recipebookapp.core.datastore.SessionStorage
import com.example.recipebookapp.core.network.ApiService
import com.example.recipebookapp.core.network.LoginRequestDto
import com.example.recipebookapp.core.network.RegisterRequestDto
import com.example.recipebookapp.core.network.SafeApiCall
import com.example.recipebookapp.core.network.toDomain
import com.example.recipebookapp.core.common.Resource
import com.example.recipebookapp.feature_auth.domain.AuthRepository
import com.example.recipebookapp.feature_auth.domain.model.AuthSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val sessionStorage: SessionStorage,
    private val safeApiCall: SafeApiCall,
) : AuthRepository {
    override val isAuthorized: Flow<Boolean> = sessionStorage.tokenFlow.map { !it.isNullOrBlank() }

    override suspend fun login(email: String, password: String): Resource<AuthSession> {
        return safeApiCall.executeWithRetry(maxAttempts = 2) {
            apiService.login(LoginRequestDto(email = email.trim(), password = password)).toDomain()
        }.also { result ->
            if (result is Resource.Success) {
                sessionStorage.saveToken(result.data.token)
            }
        }
    }

    override suspend fun register(email: String, username: String, password: String): Resource<AuthSession> {
        val registerResult = safeApiCall.executeWithRetry(maxAttempts = 5) {
            apiService.register(
                RegisterRequestDto(
                    email = email.trim(),
                    username = username.trim(),
                    password = password,
                ),
            ).toDomain()
        }

        val finalResult = when (registerResult) {
            is Resource.Success -> registerResult
            is Resource.Error -> {
                if (registerResult.message.contains("already registered", ignoreCase = true)) {
                    safeApiCall.executeWithRetry(maxAttempts = 2) {
                        apiService.login(
                            LoginRequestDto(
                                email = email.trim(),
                                password = password,
                            ),
                        ).toDomain()
                    }
                } else {
                    registerResult
                }
            }
        }

        return finalResult.also { result ->
            if (result is Resource.Success) {
                sessionStorage.saveToken(result.data.token)
            }
        }
    }

    override suspend fun logout() {
        sessionStorage.clearToken()
    }
}
