package com.example.recipebookapp

import androidx.lifecycle.SavedStateHandle
import com.example.recipebookapp.core.common.RecipeSyncNotifier
import com.example.recipebookapp.core.common.Resource
import com.example.recipebookapp.core.model.Recipe
import com.example.recipebookapp.core.model.UserProfile
import com.example.recipebookapp.core.model.UserSummary
import com.example.recipebookapp.core.presentation.AsyncState
import com.example.recipebookapp.feature_auth.domain.AuthRepository
import com.example.recipebookapp.feature_auth.domain.LogoutUseCase
import com.example.recipebookapp.feature_profile.domain.ProfileRepository
import com.example.recipebookapp.feature_profile.domain.model.ProfileWithRecipes
import com.example.recipebookapp.feature_profile.presentation.OtherProfileViewModel
import com.example.recipebookapp.feature_profile.presentation.ProfileViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @Test
    fun `profile is loaded on init`() = runTest {
        val repository = FakeProfileRepository()
        val viewModel = ProfileViewModel(repository, LogoutUseCase(FakeAuthRepositoryForProfile()))
        advanceUntilIdle()

        val profileState = viewModel.state.value.profileState
        assertTrue(profileState is AsyncState.Success)
        assertEquals("chef", (profileState as AsyncState.Success).data.profile.username)
    }

    @Test
    fun `saveProfile sends updated data and updates ui state`() = runTest {
        val repository = FakeProfileRepository()
        val viewModel = ProfileViewModel(repository, LogoutUseCase(FakeAuthRepositoryForProfile()))
        advanceUntilIdle()

        viewModel.updateUsername("chef_new")
        viewModel.updateBio("new bio")
        viewModel.updateAvatarUrl("https://img.example/avatar.png")
        viewModel.saveProfile()
        advanceUntilIdle()

        assertEquals("chef_new", repository.lastUsername)
        assertEquals("new bio", repository.lastBio)
        assertEquals("https://img.example/avatar.png", repository.lastAvatar)
        val profileState = viewModel.state.value.profileState as AsyncState.Success
        assertEquals("chef_new", profileState.data.profile.username)
    }

    @Test
    fun `toggleFollow success refreshes other profile`() = runTest {
        val repository = FakeProfileRepository()
        val viewModel = OtherProfileViewModel(repository, SavedStateHandle(mapOf("userId" to "u2")))
        advanceUntilIdle()

        viewModel.toggleFollow()
        advanceUntilIdle()

        assertEquals("u2", repository.lastFollowUserId)
        assertEquals(true, repository.lastFollowValue)
        assertEquals(2, repository.otherProfileLoadCount)
    }

    @Test
    fun `toggleFollow error exposes message`() = runTest {
        val repository = FakeProfileRepository().apply { failSetFollowing = true }
        val viewModel = OtherProfileViewModel(repository, SavedStateHandle(mapOf("userId" to "u2")))
        advanceUntilIdle()

        viewModel.toggleFollow()
        advanceUntilIdle()

        assertEquals("Follow failed", viewModel.state.value.error)
    }

    @Test
    fun `recipe mutation refreshes own profile`() = runTest {
        val repository = FakeProfileRepository()
        val notifier = RecipeSyncNotifier()
        val viewModel = ProfileViewModel(repository, LogoutUseCase(FakeAuthRepositoryForProfile()), notifier)
        advanceUntilIdle()

        notifier.notifyRecipeMutated()
        advanceUntilIdle()

        assertEquals(2, repository.myProfileLoadCount)
    }
}

private class FakeProfileRepository : ProfileRepository {
    var lastUsername: String? = null
    var lastBio: String? = null
    var lastAvatar: String? = null
    var lastFollowUserId: String? = null
    var lastFollowValue: Boolean? = null
    var otherProfileLoadCount: Int = 0
    var failSetFollowing: Boolean = false
    var myProfileLoadCount: Int = 0

    private var profile = sampleUserProfile()

    override suspend fun getMyProfileWithRecipes(): Resource<ProfileWithRecipes> {
        myProfileLoadCount += 1
        return Resource.Success(ProfileWithRecipes(profile, listOf(sampleRecipeForProfile())))
    }

    override suspend fun getOtherProfileWithRecipes(userId: String): Resource<ProfileWithRecipes> {
        otherProfileLoadCount += 1
        return Resource.Success(ProfileWithRecipes(profile.copy(id = userId), emptyList()))
    }

    override suspend fun updateProfile(username: String, bio: String, avatarUrl: String): Resource<UserProfile> {
        lastUsername = username
        lastBio = bio
        lastAvatar = avatarUrl
        profile = profile.copy(username = username, bio = bio, avatarUrl = avatarUrl)
        return Resource.Success(profile)
    }

    override suspend fun setFollowing(userId: String, shouldFollow: Boolean): Resource<Unit> {
        lastFollowUserId = userId
        lastFollowValue = shouldFollow
        return if (failSetFollowing) Resource.Error("Follow failed") else Resource.Success(Unit)
    }
}

private class FakeAuthRepositoryForProfile : AuthRepository {
    override val isAuthorized: Flow<Boolean> = flowOf(true)

    override suspend fun login(email: String, password: String) = error("Not used")

    override suspend fun register(email: String, username: String, password: String) = error("Not used")

    override suspend fun logout() = Unit
}

private fun sampleUserProfile(): UserProfile = UserProfile(
    id = "u1",
    email = "chef@example.com",
    username = "chef",
    bio = "bio",
    avatarUrl = null,
    createdAt = "2026-05-11T10:00:00",
    recipesCount = 1,
    followersCount = 0,
    followingCount = 0,
    isFollowing = false,
)

private fun sampleRecipeForProfile(): Recipe = Recipe(
    id = "r1",
    title = "Tomato soup",
    description = "Quick soup",
    category = "Soups",
    cookingTimeMinutes = 20,
    imageUrl = null,
    author = UserSummary("u1", "chef"),
    createdAt = "2026-05-11T10:00:00",
    updatedAt = "2026-05-11T10:00:00",
    likesCount = 0,
    dislikesCount = 0,
    rating = 0,
    isFavorite = false,
    myRating = null,
)
