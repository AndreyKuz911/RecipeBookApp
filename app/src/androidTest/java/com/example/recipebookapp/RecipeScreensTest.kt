package com.example.recipebookapp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.recipebookapp.core.model.Recipe
import com.example.recipebookapp.core.model.UserSummary
import com.example.recipebookapp.core.presentation.AsyncState
import com.example.recipebookapp.core.ui.RecipeBookTheme
import com.example.recipebookapp.feature_recipes.domain.model.RecipeFilters
import com.example.recipebookapp.feature_recipes.presentation.HomeScreen
import com.example.recipebookapp.feature_recipes.presentation.RecipeListUiState
import com.example.recipebookapp.feature_recipes.presentation.SearchScreen
import org.junit.Rule
import org.junit.Test

class RecipeScreensTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun recipeListIsDisplayed() {
        composeRule.setContent {
            RecipeBookTheme {
                HomeScreen(
                    state = AsyncState.Success(listOf(sampleRecipe())),
                    onRetry = {},
                    onRecipeClick = {},
                    onAuthorClick = {},
                )
            }
        }

        composeRule.onNodeWithText("Test soup").assertIsDisplayed()
    }

    @Test
    fun emptySearchStateIsDisplayed() {
        composeRule.setContent {
            RecipeBookTheme {
                SearchScreen(
                    state = RecipeListUiState(
                        filters = RecipeFilters(query = "zzz"),
                        state = AsyncState.Empty,
                    ),
                    onFiltersChange = {},
                    onSearch = {},
                    onReset = {},
                    onRecipeClick = {},
                    onAuthorClick = {},
                )
            }
        }

        composeRule.onAllNodes(hasSetTextAction())[0].assertTextContains("zzz")
    }
}

private fun sampleRecipe(): Recipe = Recipe(
    id = "1",
    title = "Test soup",
    description = "Description",
    category = "Soups",
    cookingTimeMinutes = 20,
    imageUrl = null,
    author = UserSummary("author", "chef"),
    createdAt = "2026-05-03T00:00",
    updatedAt = "2026-05-03T00:00",
    likesCount = 1,
    dislikesCount = 0,
    rating = 1,
    isFavorite = false,
    myRating = null,
)
