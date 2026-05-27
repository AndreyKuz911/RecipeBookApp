package com.example.recipebookapp

import androidx.lifecycle.SavedStateHandle
import com.example.recipebookapp.core.common.Resource
import com.example.recipebookapp.core.model.Comment
import com.example.recipebookapp.core.model.RecipeDetails
import com.example.recipebookapp.core.model.UserSummary
import com.example.recipebookapp.core.presentation.AsyncState
import com.example.recipebookapp.feature_recipes.domain.RecipeDraft
import com.example.recipebookapp.feature_recipes.domain.RecipesRepository
import com.example.recipebookapp.feature_recipes.domain.model.PagedRecipes
import com.example.recipebookapp.feature_recipes.domain.model.RecipeFilters
import com.example.recipebookapp.feature_recipes.presentation.RecipeDetailsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecipeDetailsViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @Test
    fun `setRating updates details on success`() = runTest {
        val repository = FakeRecipesRepositoryForDetails()
        val viewModel = RecipeDetailsViewModel(repository, SavedStateHandle(mapOf("recipeId" to RECIPE_ID)))
        advanceUntilIdle()

        viewModel.setRating(1)
        advanceUntilIdle()

        val state = viewModel.state.value.detailsState as AsyncState.Success
        assertEquals(1, state.data.myRating)
        assertEquals(1, state.data.likesCount)
        assertEquals(1, state.data.rating)
    }

    @Test
    fun `setRating reverts optimistic update on error`() = runTest {
        val repository = FakeRecipesRepositoryForDetails().apply { failRate = true }
        val viewModel = RecipeDetailsViewModel(repository, SavedStateHandle(mapOf("recipeId" to RECIPE_ID)))
        advanceUntilIdle()

        viewModel.setRating(1)
        advanceUntilIdle()

        val state = viewModel.state.value.detailsState as AsyncState.Success
        assertEquals(null, state.data.myRating)
        assertEquals(0, state.data.likesCount)
        assertEquals(0, state.data.rating)
    }

    @Test
    fun `toggleFavorite updates favorite flag`() = runTest {
        val repository = FakeRecipesRepositoryForDetails()
        val viewModel = RecipeDetailsViewModel(repository, SavedStateHandle(mapOf("recipeId" to RECIPE_ID)))
        advanceUntilIdle()

        viewModel.toggleFavorite()
        advanceUntilIdle()

        val state = viewModel.state.value.detailsState as AsyncState.Success
        assertTrue(state.data.isFavorite)
    }

    @Test
    fun `addComment prepends comment and clears input`() = runTest {
        val repository = FakeRecipesRepositoryForDetails()
        val viewModel = RecipeDetailsViewModel(repository, SavedStateHandle(mapOf("recipeId" to RECIPE_ID)))
        advanceUntilIdle()

        viewModel.updateComment("Great recipe")
        viewModel.addComment()
        advanceUntilIdle()

        val commentsState = viewModel.state.value.commentsState as AsyncState.Success
        assertEquals("Great recipe", commentsState.data.first().text)
        assertEquals("", viewModel.state.value.commentText)
    }

    @Test
    fun `blank comment is ignored`() = runTest {
        val repository = FakeRecipesRepositoryForDetails()
        val viewModel = RecipeDetailsViewModel(repository, SavedStateHandle(mapOf("recipeId" to RECIPE_ID)))
        advanceUntilIdle()

        viewModel.updateComment("   ")
        viewModel.addComment()
        advanceUntilIdle()

        assertEquals(0, repository.commentCount)
        assertTrue(viewModel.state.value.commentsState is AsyncState.Empty)
    }
}

private class FakeRecipesRepositoryForDetails : RecipesRepository {
    var failRate: Boolean = false
    var commentCount: Int = 0
    private var details = sampleRecipeDetails()
    private val comments = mutableListOf<Comment>()

    override suspend fun getRecipes(
        filters: RecipeFilters,
        page: Int,
        limit: Int,
    ): Resource<PagedRecipes> = error("Not used")

    override suspend fun getRecipeDetails(recipeId: String): Resource<RecipeDetails> = Resource.Success(details)

    override suspend fun createRecipe(draft: RecipeDraft): Resource<RecipeDetails> = error("Not used")

    override suspend fun updateRecipe(recipeId: String, draft: RecipeDraft): Resource<RecipeDetails> = error("Not used")

    override suspend fun deleteRecipe(recipeId: String): Resource<Unit> = error("Not used")

    override suspend fun rateRecipe(recipeId: String, value: Int): Resource<RecipeDetails> {
        if (failRate) return Resource.Error("Network error")
        details = when (value) {
            1 -> details.copy(likesCount = 1, dislikesCount = 0, rating = 1, myRating = 1)
            -1 -> details.copy(likesCount = 0, dislikesCount = 1, rating = -1, myRating = -1)
            else -> details
        }
        return Resource.Success(details)
    }

    override suspend fun clearRating(recipeId: String): Resource<Unit> = error("Not used")

    override suspend fun toggleFavorite(recipeId: String, currentlyFavorite: Boolean): Resource<RecipeDetails> {
        details = details.copy(isFavorite = !currentlyFavorite)
        return Resource.Success(details)
    }

    override suspend fun getComments(recipeId: String): Resource<List<Comment>> = Resource.Success(comments.toList())

    override suspend fun addComment(recipeId: String, text: String, parentCommentId: String?): Resource<Comment> {
        commentCount += 1
        val comment = Comment(
            id = "c_${comments.size + 1}",
            recipeId = recipeId,
            parentCommentId = parentCommentId,
            text = text,
            createdAt = "2026-05-11T10:00:00",
            author = UserSummary("u1", "tester"),
            replies = emptyList(),
        )
        comments.add(0, comment)
        return Resource.Success(comment)
    }
}

private const val RECIPE_ID = "r1"

private fun sampleRecipeDetails(): RecipeDetails = RecipeDetails(
    id = RECIPE_ID,
    title = "Tomato soup",
    description = "Quick soup",
    category = "Soups",
    cookingTimeMinutes = 20,
    ingredients = listOf("Tomatoes", "Salt"),
    steps = listOf("Cook", "Blend"),
    imageUrls = emptyList(),
    author = UserSummary("u1", "tester"),
    createdAt = "2026-05-11T10:00:00",
    updatedAt = "2026-05-11T10:00:00",
    likesCount = 0,
    dislikesCount = 0,
    rating = 0,
    isFavorite = false,
    myRating = null,
)
