package com.example.recipebookapp.feature_auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipebookapp.core.common.Resource
import com.example.recipebookapp.feature_auth.domain.AuthRepository
import com.example.recipebookapp.feature_auth.domain.AuthUseCases
import com.example.recipebookapp.feature_auth.domain.ObserveAuthorizationUseCase
import com.example.recipebookapp.feature_auth.domain.authUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val email: String = "",
    val username: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAuthenticated: Boolean = false,
)

@HiltViewModel
class SplashViewModel @Inject constructor(
    observeAuthorizationUseCase: ObserveAuthorizationUseCase,
) : ViewModel() {
    constructor(repository: AuthRepository) : this(ObserveAuthorizationUseCase(repository))

    val isAuthorized: StateFlow<Boolean?> = observeAuthorizationUseCase()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authUseCases: AuthUseCases,
) : ViewModel() {
    constructor(repository: AuthRepository) : this(authUseCases(repository))

    private val _state = MutableStateFlow(AuthUiState())
    val state = _state.asStateFlow()

    fun updateEmail(value: String) = _state.update { it.copy(email = value, error = null) }

    fun updateUsername(value: String) = _state.update { it.copy(username = value, error = null) }

    fun updatePassword(value: String) = _state.update { it.copy(password = value, error = null) }

    fun updateConfirmPassword(value: String) = _state.update { it.copy(confirmPassword = value, error = null) }

    fun login() {
        val current = _state.value
        if (current.email.isBlank() || current.password.isBlank()) {
            _state.update { it.copy(error = "Введите email и пароль") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = authUseCases.login(current.email, current.password)) {
                is Resource.Success -> _state.update { it.copy(isLoading = false, isAuthenticated = true) }
                is Resource.Error -> _state.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    fun register() {
        val current = _state.value
        when {
            current.email.isBlank() || current.username.isBlank() || current.password.isBlank() ->
                _state.update { it.copy(error = "Заполните все поля") }
            current.password != current.confirmPassword ->
                _state.update { it.copy(error = "Пароли не совпадают") }
            else -> viewModelScope.launch {
                _state.update { it.copy(isLoading = true, error = null) }
                when (val result = authUseCases.register(current.email, current.username, current.password)) {
                    is Resource.Success -> _state.update { it.copy(isLoading = false, isAuthenticated = true) }
                    is Resource.Error -> _state.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }

    fun resetAuthState() {
        _state.update { it.copy(isAuthenticated = false) }
    }

    fun logout() {
        viewModelScope.launch {
            authUseCases.logout()
            _state.value = AuthUiState()
        }
    }
}

private inline fun <T> MutableStateFlow<T>.update(transform: (T) -> T) {
    value = transform(value)
}
