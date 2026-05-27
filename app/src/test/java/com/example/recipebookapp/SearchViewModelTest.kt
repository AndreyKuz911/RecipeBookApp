package com.example.recipebookapp

import com.example.recipebookapp.core.common.Resource
import com.example.recipebookapp.core.model.Comment
import com.example.recipebookapp.core.model.Recipe
import com.example.recipebookapp.core.model.RecipeDetails
import com.example.recipebookapp.core.model.UserSummary
import com.example.recipebookapp.core.presentation.AsyncState
import com.example.recipebookapp.feature_recipes.domain.RecipeDraft
import com.example.recipebookapp.feature_recipes.domain.RecipesRepository
import com.example.recipebookapp.feature_recipes.domain.model.PagedRecipes
import com.example.recipebookapp.feature_recipes.domain.model.RecipeFilters
import com.example.recipebookapp.feature_recipes.presentation.SearchViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @Test
    fun `search uses selected filters`() = runTest {
        val repository = FakeSearchRepository()
        val viewModel = SearchViewModel(repository)
        advanceUntilIdle()

        val filters = RecipeFilters(query = "soup", category = "Soups", sort = "newest")
        viewModel.updateFilters(filters)
        viewModel.search()
        advanceUntilIdle()

        assertEquals(filters, repository.lastFilters)
        val state = viewModel.state.value.state
        assertTrue(state is AsyncState.Success)
        assertEquals(1, (state as AsyncState.Success).data.size)
    }

    @Test
    fun `resetFilters clears query and category`() = runTest {
        val repository = FakeSearchRepository()
        val viewModel = SearchViewModel(repository)
        advanceUntilIdle()

        viewModel.updateFilters(RecipeFilters(query = "pasta", category = "Pasta"))
        viewModel.resetFilters()
        advanceUntilIdle()

        assertEquals("", viewModel.state.value.filters.query)
        assertEquals("", viewModel.state.value.filters.category)
    }

    @Test
    fun `search error shows error state`() = runTest {
        val repository = FakeSearchRepository().apply { failSearch = true }
        val viewModel = SearchViewModel(repository)
        advanceUntilIdle()

        viewModel.search()
        advanceUntilIdle()

        val state = viewModel.state.value.state
        assertTrue(state is AsyncState.Error)
        assertEquals("Search failed", (state as AsyncState.Error).message)
    }
}

private class FakeSearchRepository : RecipesRepository {
    var lastFilters: RecipeFilters? = null
    var failSearch: Boolean = false

    override suspend fun getRecipes(filters: RecipeFilters, page: Int, limit: Int): Resource<PagedRecipes> {
        lastFilters = filters
        return if (failSearch) {
            Resource.Error("Search failed")
        } else {
            Resource.Success(PagedRecipes(items = listOf(searchSampleRecipe()), page = 1, limit = 20, total = 1))
        }
    }

    override suspend fun getRecipeDetails(recipeId: String): Resource<RecipeDetails> = error("Not used")

    override suspend fun createRecipe(draft: RecipeDraft): Resource<RecipeDetails> = error("Not used")

    override suspend fun updateRecipe(recipeId: String, draft: RecipeDraft): Resource<RecipeDetails> = error("Not used")

    override suspend fun deleteRecipe(recipeId: String): Resource<Unit> = Resource.Success(Unit)

    override suspend fun rateRecipe(recipeId: String, value: Int): Resource<RecipeDetails> = error("Not used")

    override suspend fun clearRating(recipeId: String): Resource<Unit> = Resource.Success(Unit)

    override suspend fun toggleFavorite(recipeId: String, currentlyFavorite: Boolean): Resource<RecipeDetails> =
        error("Not used")

    override suspend fun getComments(recipeId: String): Resource<List<Comment>> = Resource.Success(emptyList())

    override suspend fun addComment(recipeId: String, text: String, parentCommentId: String?): Resource<Comment> =
        error("Not used")
}

private fun searchSampleRecipe(): Recipe = Recipe(
    id = "r-search",
    title = "Soup",
    description = "Description",
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
