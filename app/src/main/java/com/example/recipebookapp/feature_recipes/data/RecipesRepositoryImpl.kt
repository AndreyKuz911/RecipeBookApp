package com.example.recipebookapp.feature_recipes.data

import com.example.recipebookapp.core.network.ApiService
import com.example.recipebookapp.core.network.CreateCommentRequestDto
import com.example.recipebookapp.core.network.MediaUploader
import com.example.recipebookapp.core.network.RatingRequestDto
import com.example.recipebookapp.core.network.RecipeUpsertRequestDto
import com.example.recipebookapp.core.network.SafeApiCall
import com.example.recipebookapp.core.network.toDomain
import com.example.recipebookapp.core.common.Resource
import com.example.recipebookapp.core.model.Comment
import com.example.recipebookapp.core.model.Recipe
import com.example.recipebookapp.core.model.RecipeDetails
import com.example.recipebookapp.feature_recipes.domain.RecipeDraft
import com.example.recipebookapp.feature_recipes.domain.RecipesRepository
import com.example.recipebookapp.feature_recipes.domain.model.PagedRecipes
import com.example.recipebookapp.feature_recipes.domain.model.RecipeFilters
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipesRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val safeApiCall: SafeApiCall,
    private val mediaUploader: MediaUploader,
) : RecipesRepository {
    override suspend fun getRecipes(filters: RecipeFilters, page: Int, limit: Int): Resource<PagedRecipes> {
        return safeApiCall.execute {
            apiService.getRecipes(
                page = page,
                limit = limit,
                query = filters.query.ifBlank { null },
                category = filters.category.ifBlank { null },
                timeRange = filters.timeRange.ifBlank { null },
                sort = filters.sort,
            ).toDomain()
        }
    }

    override suspend fun getRecipeDetails(recipeId: String): Resource<RecipeDetails> =
        safeApiCall.execute { apiService.getRecipe(recipeId).toDomain() }

    override suspend fun createRecipe(draft: RecipeDraft): Resource<RecipeDetails> =
        safeApiCall.execute { apiService.createRecipe(draft.toRequest(mediaUploader)).toDomain() }

    override suspend fun updateRecipe(recipeId: String, draft: RecipeDraft): Resource<RecipeDetails> =
        safeApiCall.execute { apiService.updateRecipe(recipeId, draft.toRequest(mediaUploader)).toDomain() }

    override suspend fun deleteRecipe(recipeId: String): Resource<Unit> =
        safeApiCall.execute { apiService.deleteRecipe(recipeId); Unit }

    override suspend fun rateRecipe(recipeId: String, value: Int): Resource<RecipeDetails> =
        safeApiCall.execute { apiService.rateRecipe(recipeId, RatingRequestDto(value)).toDomain() }

    override suspend fun clearRating(recipeId: String): Resource<Unit> =
        safeApiCall.execute { apiService.deleteRating(recipeId); Unit }

    override suspend fun toggleFavorite(recipeId: String, currentlyFavorite: Boolean): Resource<Unit> {
        return safeApiCall.execute {
            if (currentlyFavorite) {
                apiService.removeFavorite(recipeId)
            } else {
                apiService.addFavorite(recipeId)
            }
            Unit
        }
    }

    override suspend fun getComments(recipeId: String): Resource<List<Comment>> =
        safeApiCall.execute { apiService.getComments(recipeId).map { it.toDomain() } }

    override suspend fun addComment(recipeId: String, text: String, parentCommentId: String?): Resource<Comment> =
        safeApiCall.execute {
            apiService.createComment(
                recipeId = recipeId,
                body = CreateCommentRequestDto(text = text, parentCommentId = parentCommentId),
            ).toDomain()
        }

}

private suspend fun RecipeDraft.toRequest(mediaUploader: MediaUploader): RecipeUpsertRequestDto {
    val uploadedImageUrls = imageUrls
        .filter { it.isNotBlank() }
        .map { value ->
            if (mediaUploader.isLocalUri(value)) mediaUploader.uploadFromUri(value) else value
        }

    return RecipeUpsertRequestDto(
        title = title.trim(),
        description = description.trim(),
        category = category,
        cookingTimeMinutes = cookingTimeMinutes.toIntOrNull() ?: 0,
        ingredients = ingredients.filter { it.isNotBlank() },
        steps = steps.filter { it.isNotBlank() },
        imageUrls = uploadedImageUrls,
    )
}
