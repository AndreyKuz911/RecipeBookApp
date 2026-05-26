package com.example.recipebookapp

import com.example.recipebookapp.core.common.Resource
import com.example.recipebookapp.core.model.UserProfile
import com.example.recipebookapp.feature_auth.domain.AuthRepository
import com.example.recipebookapp.feature_auth.domain.model.AuthSession
import com.example.recipebookapp.feature_auth.presentation.AuthViewModel
import com.example.recipebookapp.feature_auth.presentation.SplashViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @Test
    fun `splash mirrors authorization flow`() = runTest {
        val viewModel = SplashViewModel(FakeAuthRepository(Resource.Success(sampleSession()), flowOf(true)))
        advanceUntilIdle()

        assertEquals(true, viewModel.isAuthorized.value)
    }

    @Test
    fun `login success updates authenticated state`() = runTest {
        val viewModel = AuthViewModel(FakeAuthRepository(Resource.Success(sampleSession())))

        viewModel.updateEmail("chef@example.com")
        viewModel.updatePassword("password123")
        viewModel.login()
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isAuthenticated)
    }

    @Test
    fun `login error updates error state`() = runTest {
        val viewModel = AuthViewModel(FakeAuthRepository(Resource.Error("Invalid credentials")))

        viewModel.updateEmail("chef@example.com")
        viewModel.updatePassword("wrong")
        viewModel.login()
        advanceUntilIdle()

        assertEquals("Invalid credentials", viewModel.state.value.error)
    }

    @Test
    fun `login validation requires email and password`() = runTest {
        val viewModel = AuthViewModel(FakeAuthRepository(Resource.Success(sampleSession())))

        viewModel.login()

        assertEquals("Введите email и пароль", viewModel.state.value.error)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `register validation rejects mismatched passwords`() = runTest {
        val viewModel = AuthViewModel(FakeAuthRepository(Resource.Success(sampleSession())))

        viewModel.updateEmail("chef@example.com")
        viewModel.updateUsername("chef")
        viewModel.updatePassword("one")
        viewModel.updateConfirmPassword("two")
        viewModel.register()

        assertEquals("Пароли не совпадают", viewModel.state.value.error)
    }

    @Test
    fun `logout clears local auth state`() = runTest {
        val repository = FakeAuthRepository(Resource.Success(sampleSession()))
        val viewModel = AuthViewModel(repository)

        viewModel.updateEmail("chef@example.com")
        viewModel.updatePassword("password123")
        viewModel.login()
        advanceUntilIdle()

        viewModel.logout()
        advanceUntilIdle()

        assertTrue(repository.logoutCalled)
        assertEquals("", viewModel.state.value.email)
        assertFalse(viewModel.state.value.isAuthenticated)
    }
}

private class FakeAuthRepository(
    private val result: Resource<AuthSession>,
    override val isAuthorized: Flow<Boolean> = flowOf(false),
) : AuthRepository {
    var logoutCalled: Boolean = false

    override suspend fun login(email: String, password: String): Resource<AuthSession> = result

    override suspend fun register(email: String, username: String, password: String): Resource<AuthSession> = result

    override suspend fun logout() {
        logoutCalled = true
    }
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
