package com.example.recipebookapp

import androidx.lifecycle.SavedStateHandle
import com.example.recipebookapp.core.common.Resource
import com.example.recipebookapp.core.model.Comment
import com.example.recipebookapp.core.model.RecipeDetails
import com.example.recipebookapp.core.model.UserSummary
import com.example.recipebookapp.feature_recipes.domain.RecipeDraft
import com.example.recipebookapp.feature_recipes.domain.RecipesRepository
import com.example.recipebookapp.feature_recipes.domain.model.PagedRecipes
import com.example.recipebookapp.feature_recipes.domain.model.RecipeFilters
import com.example.recipebookapp.feature_recipes.presentation.EditRecipeViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditRecipeViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @Test
    fun `create recipe success sets savedRecipeId`() = runTest {
        val repository = FakeRecipesRepositoryForEditor()
        val viewModel = EditRecipeViewModel(repository, SavedStateHandle())
        advanceUntilIdle()

        viewModel.updateDraft { it.copy(title = "Soup", description = "Tasty", category = "Soups", cookingTimeMinutes = "20") }
        viewModel.saveRecipe()
        advanceUntilIdle()

        assertEquals("created-1", viewModel.state.value.savedRecipeId)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `create recipe error sets message`() = runTest {
        val repository = FakeRecipesRepositoryForEditor().apply { failCreate = true }
        val viewModel = EditRecipeViewModel(repository, SavedStateHandle())
        advanceUntilIdle()

        viewModel.updateDraft { it.copy(title = "Soup", description = "Tasty", category = "Soups", cookingTimeMinutes = "20") }
        viewModel.saveRecipe()
        advanceUntilIdle()

        assertEquals("Create failed", viewModel.state.value.error)
    }

    @Test
    fun `existing recipe is preloaded into draft`() = runTest {
        val repository = FakeRecipesRepositoryForEditor()
        val viewModel = EditRecipeViewModel(repository, SavedStateHandle(mapOf("recipeId" to "existing")))
        advanceUntilIdle()

        assertEquals("Recipe", viewModel.state.value.draft.title)
        assertEquals("Soups", viewModel.state.value.draft.category)
    }

    @Test
    fun `update recipe uses existing id`() = runTest {
        val repository = FakeRecipesRepositoryForEditor()
        val viewModel = EditRecipeViewModel(repository, SavedStateHandle(mapOf("recipeId" to "existing")))
        advanceUntilIdle()

        viewModel.updateDraft { it.copy(title = "Updated title") }
        viewModel.saveRecipe()
        advanceUntilIdle()

        assertEquals("existing", repository.lastUpdatedRecipeId)
        assertEquals("existing", viewModel.state.value.savedRecipeId)
    }
}

private class FakeRecipesRepositoryForEditor : RecipesRepository {
    var failCreate: Boolean = false
    var lastUpdatedRecipeId: String? = null

    override suspend fun getRecipes(
        filters: RecipeFilters,
        page: Int,
        limit: Int,
    ): Resource<PagedRecipes> = error("Not used")

    override suspend fun getRecipeDetails(recipeId: String): Resource<RecipeDetails> = Resource.Success(editorRecipe(recipeId))

    override suspend fun createRecipe(draft: RecipeDraft): Resource<RecipeDetails> {
        return if (failCreate) {
            Resource.Error("Create failed")
        } else {
            Resource.Success(editorRecipe("created-1").copy(title = draft.title, category = draft.category))
        }
    }

    override suspend fun updateRecipe(recipeId: String, draft: RecipeDraft): Resource<RecipeDetails> {
        lastUpdatedRecipeId = recipeId
        return Resource.Success(editorRecipe(recipeId).copy(title = draft.title, category = draft.category))
    }

    override suspend fun deleteRecipe(recipeId: String): Resource<Unit> = Resource.Success(Unit)

    override suspend fun rateRecipe(recipeId: String, value: Int): Resource<RecipeDetails> = error("Not used")

    override suspend fun clearRating(recipeId: String): Resource<Unit> = Resource.Success(Unit)

    override suspend fun toggleFavorite(recipeId: String, currentlyFavorite: Boolean): Resource<RecipeDetails> =
        Resource.Success(editorRecipe(recipeId))

    override suspend fun getComments(recipeId: String): Resource<List<Comment>> = Resource.Success(emptyList())

    override suspend fun addComment(recipeId: String, text: String, parentCommentId: String?): Resource<Comment> =
        error("Not used")
}

private fun editorRecipe(id: String): RecipeDetails = RecipeDetails(
    id = id,
    title = "Recipe",
    description = "Description",
    category = "Soups",
    cookingTimeMinutes = 20,
    ingredients = listOf("Water"),
    steps = listOf("Boil"),
    imageUrls = emptyList(),
    author = UserSummary("u1", "chef"),
    createdAt = "2026-05-11T10:00:00",
    updatedAt = "2026-05-11T10:00:00",
    likesCount = 0,
    dislikesCount = 0,
    rating = 0,
    isFavorite = false,
    myRating = null,
)
