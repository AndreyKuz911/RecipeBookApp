package com.example.recipebookapp

import com.example.recipebookapp.core.common.Resource
import com.example.recipebookapp.core.model.UserProfile
import com.example.recipebookapp.feature_auth.domain.AuthRepository
import com.example.recipebookapp.feature_auth.domain.LoginUseCase
import com.example.recipebookapp.feature_auth.domain.LogoutUseCase
import com.example.recipebookapp.feature_auth.domain.RegisterUseCase
import com.example.recipebookapp.feature_auth.domain.model.AuthSession
import com.example.recipebookapp.feature_auth.presentation.AuthViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AuthViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @Test
    fun `login success updates authenticated state`() = runTest {
        val viewModel = AuthViewModel(
            loginUseCase = LoginUseCase(FakeAuthRepository(Resource.Success(sampleSession()))),
            registerUseCase = RegisterUseCase(FakeAuthRepository(Resource.Success(sampleSession()))),
            logoutUseCase = LogoutUseCase(FakeAuthRepository(Resource.Success(sampleSession()))),
        )

        viewModel.updateEmail("chef@example.com")
        viewModel.updatePassword("password123")
        viewModel.login()

        assertTrue(viewModel.state.value.isAuthenticated)
    }

    @Test
    fun `login error updates error state`() = runTest {
        val viewModel = AuthViewModel(
            loginUseCase = LoginUseCase(FakeAuthRepository(Resource.Error("Invalid credentials"))),
            registerUseCase = RegisterUseCase(FakeAuthRepository(Resource.Success(sampleSession()))),
            logoutUseCase = LogoutUseCase(FakeAuthRepository(Resource.Success(sampleSession()))),
        )

        viewModel.updateEmail("chef@example.com")
        viewModel.updatePassword("wrong")
        viewModel.login()

        assertEquals("Invalid credentials", viewModel.state.value.error)
    }
}

private class FakeAuthRepository(
    private val result: Resource<AuthSession>,
) : AuthRepository {
    override val isAuthorized: Flow<Boolean> = flowOf(false)

    override suspend fun login(email: String, password: String): Resource<AuthSession> = result

    override suspend fun register(email: String, username: String, password: String): Resource<AuthSession> = result

    override suspend fun logout() = Unit
}

private fun sampleSession(): AuthSession = AuthSession(
    token = "token",
    user = UserProfile(
        id = "1",
        email = "chef@example.com",
        username = "chef",
        bio = null,
        avatarUrl = null,
        createdAt = "2026-05-03T00:00",
        recipesCount = 0,
        followersCount = 0,
        followingCount = 0,
        isFollowing = false,
    ),
)
