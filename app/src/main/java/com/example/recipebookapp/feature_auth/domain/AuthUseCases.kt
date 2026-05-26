package com.example.recipebookapp.feature_auth.domain

import javax.inject.Inject

data class AuthUseCases @Inject constructor(
    val login: LoginUseCase,
    val register: RegisterUseCase,
    val logout: LogoutUseCase,
    val observeAuthorization: ObserveAuthorizationUseCase,
)

class LoginUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String) = repository.login(email, password)
}

class RegisterUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(email: String, username: String, password: String) =
        repository.register(email, username, password)
}

class LogoutUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke() = repository.logout()
}

class ObserveAuthorizationUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    operator fun invoke() = repository.isAuthorized
}

fun authUseCases(repository: AuthRepository): AuthUseCases = AuthUseCases(
    login = LoginUseCase(repository),
    register = RegisterUseCase(repository),
    logout = LogoutUseCase(repository),
    observeAuthorization = ObserveAuthorizationUseCase(repository),
)
