package com.example.recipebookapp.feature_recipes.domain

import com.example.recipebookapp.core.common.Resource
import com.example.recipebookapp.core.model.Comment
import com.example.recipebookapp.core.model.RecipeDetails
import com.example.recipebookapp.feature_recipes.domain.model.PagedRecipes
import com.example.recipebookapp.feature_recipes.domain.model.RecipeFilters

data class RecipeDraft(
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val cookingTimeMinutes: String = "",
    val ingredients: List<String> = listOf(""),
    val steps: List<String> = listOf(""),
    val imageUrls: List<String> = listOf(""),
)

interface RecipesRepository {
    suspend fun getRecipes(filters: RecipeFilters, page: Int = 1, limit: Int = 20): Resource<PagedRecipes>

    suspend fun getRecipeDetails(recipeId: String): Resource<RecipeDetails>

    suspend fun createRecipe(draft: RecipeDraft): Resource<RecipeDetails>

    suspend fun updateRecipe(recipeId: String, draft: RecipeDraft): Resource<RecipeDetails>

    suspend fun deleteRecipe(recipeId: String): Resource<Unit>

    suspend fun rateRecipe(recipeId: String, value: Int): Resource<RecipeDetails>

    suspend fun clearRating(recipeId: String): Resource<Unit>

    suspend fun toggleFavorite(recipeId: String, currentlyFavorite: Boolean): Resource<Unit>

    suspend fun getComments(recipeId: String): Resource<List<Comment>>

    suspend fun addComment(recipeId: String, text: String, parentCommentId: String? = null): Resource<Comment>
}
