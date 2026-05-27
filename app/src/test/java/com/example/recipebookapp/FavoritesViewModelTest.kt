package com.example.recipebookapp

import com.example.recipebookapp.core.common.Resource
import com.example.recipebookapp.core.model.Recipe
import com.example.recipebookapp.core.model.UserSummary
import com.example.recipebookapp.core.presentation.AsyncState
import com.example.recipebookapp.feature_favorites.domain.FavoritesRepository
import com.example.recipebookapp.feature_favorites.presentation.FavoritesViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @Test
    fun `favorites load success shows data`() = runTest {
        val viewModel = FavoritesViewModel(FakeFavoritesRepository())
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state is AsyncState.Success)
        assertEquals(1, (state as AsyncState.Success).data.size)
    }

    @Test
    fun `remove favorite updates local state`() = runTest {
        val repository = FakeFavoritesRepository()
        val viewModel = FavoritesViewModel(repository)
        advanceUntilIdle()

        viewModel.removeFromFavorites("fav-1")
        advanceUntilIdle()

        assertEquals("fav-1", repository.lastRemovedId)
        assertTrue(viewModel.state.value is AsyncState.Empty)
    }

    @Test
    fun `remove favorite error falls back to refresh`() = runTest {
        val repository = FakeFavoritesRepository().apply { failRemove = true }
        val viewModel = FavoritesViewModel(repository)
        advanceUntilIdle()

        viewModel.removeFromFavorites("fav-1")
        advanceUntilIdle()

        assertTrue(repository.refreshCount >= 2)
        assertTrue(viewModel.state.value is AsyncState.Success)
    }
}

private class FakeFavoritesRepository : FavoritesRepository {
    var failRemove: Boolean = false
    var lastRemovedId: String? = null
    var refreshCount: Int = 0

    override suspend fun getFavorites(): Resource<List<Recipe>> {
        refreshCount += 1
        return Resource.Success(listOf(sampleFavoriteRecipe()))
    }

    override suspend fun removeFavorite(recipeId: String): Resource<Unit> {
        lastRemovedId = recipeId
        return if (failRemove) Resource.Error("Remove failed") else Resource.Success(Unit)
    }
}

private fun sampleFavoriteRecipe(): Recipe = Recipe(
    id = "fav-1",
    title = "Favorite soup",
    description = "Quick soup",
    category = "Soups",
    cookingTimeMinutes = 15,
    imageUrl = null,
    author = UserSummary("u1", "chef"),
    createdAt = "2026-05-12T12:00:00",
    updatedAt = "2026-05-12T12:00:00",
    likesCount = 2,
    dislikesCount = 0,
    rating = 2,
    isFavorite = true,
    myRating = 1,
)
