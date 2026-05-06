package com.example.recipebookapp

import com.example.recipebookapp.core.common.Resource
import com.example.recipebookapp.core.model.Recipe
import com.example.recipebookapp.core.model.UserSummary
import com.example.recipebookapp.core.presentation.AsyncState
import com.example.recipebookapp.feature_recipes.domain.RecipeDraft
import com.example.recipebookapp.feature_recipes.domain.RecipesRepository
import com.example.recipebookapp.feature_recipes.domain.model.PagedRecipes
import com.example.recipebookapp.feature_recipes.domain.model.RecipeFilters
import com.example.recipebookapp.feature_recipes.presentation.HomeViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @Test
    fun `recipes loading success shows data`() = runTest {
        val viewModel = HomeViewModel(FakeRecipesRepository(Resource.Success(PagedRecipes(listOf(sampleRecipe()), 1, 20, 1))))

        val state = viewModel.state.value.state
        assertTrue(state is AsyncState.Success)
        assertEquals(1, (state as AsyncState.Success).data.size)
    }

    @Test
    fun `recipes loading error shows error state`() = runTest {
        val viewModel = HomeViewModel(FakeRecipesRepository(Resource.Error("Network error")))

        val state = viewModel.state.value.state
        assertTrue(state is AsyncState.Error)
        assertEquals("Network error", (state as AsyncState.Error).message)
    }
}

private class FakeRecipesRepository(
    private val result: Resource<PagedRecipes>,
) : RecipesRepository {
    override suspend fun getRecipes(filters: RecipeFilters, page: Int, limit: Int): Resource<PagedRecipes> = result
    override suspend fun getRecipeDetails(recipeId: String) = error("Not used")
    override suspend fun createRecipe(draft: RecipeDraft) = error("Not used")
    override suspend fun updateRecipe(recipeId: String, draft: RecipeDraft) = error("Not used")
    override suspend fun deleteRecipe(recipeId: String) = error("Not used")
    override suspend fun rateRecipe(recipeId: String, value: Int) = error("Not used")
    override suspend fun clearRating(recipeId: String) = error("Not used")
    override suspend fun toggleFavorite(recipeId: String, currentlyFavorite: Boolean) = error("Not used")
    override suspend fun getComments(recipeId: String) = error("Not used")
    override suspend fun addComment(recipeId: String, text: String, parentCommentId: String?) = error("Not used")
}

private fun sampleRecipe(): Recipe = Recipe(
    id = "1",
    title = "Тестовый суп",
    description = "Описание",
    category = "Супы",
    cookingTimeMinutes = 20,
    imageUrl = null,
    author = UserSummary("author", "chef", null),
    createdAt = "2026-05-03T00:00",
    updatedAt = "2026-05-03T00:00",
    likesCount = 1,
    dislikesCount = 0,
    rating = 1,
    isFavorite = false,
    myRating = null,
)
