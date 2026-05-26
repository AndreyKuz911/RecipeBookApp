package com.example.recipebookapp.feature_recipes.data

import com.example.recipebookapp.core.database.RecipeCacheDao
import com.example.recipebookapp.core.database.toCacheEntity
import com.example.recipebookapp.core.database.toDomain
import com.example.recipebookapp.core.network.ApiService
import com.example.recipebookapp.core.network.CreateCommentRequestDto
import com.example.recipebookapp.core.network.MediaUploader
import com.example.recipebookapp.core.network.RatingRequestDto
import com.example.recipebookapp.core.network.RecipeUpsertRequestDto
import com.example.recipebookapp.core.network.SafeApiCall
import com.example.recipebookapp.core.network.toDomain
import com.example.recipebookapp.core.common.Resource
import com.example.recipebookapp.core.model.Comment
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
    private val recipeCacheDao: RecipeCacheDao,
) : RecipesRepository {
    override suspend fun getRecipes(filters: RecipeFilters, page: Int, limit: Int): Resource<PagedRecipes> {
        val result = safeApiCall.execute {
            apiService.getRecipes(
                page = page,
                limit = limit,
                query = filters.query.ifBlank { null },
                category = filters.category.ifBlank { null },
                timeRange = filters.timeRange.ifBlank { null },
                sort = filters.sort,
            ).toDomain()
        }
        return when (result) {
            is Resource.Success -> {
                cacheRecipes(result.data.items)
                result
            }
            is Resource.Error -> {
                if (filters.isDefaultCatalog() && page == 1) {
                    val cached = recipeCacheDao.getRecentRecipes(limit).map { it.toDomain() }
                    if (cached.isNotEmpty()) {
                        Resource.Success(PagedRecipes(items = cached, page = 1, limit = limit, total = cached.size))
                    } else {
                        result
                    }
                } else {
                    result
                }
            }
        }
    }

    override suspend fun getRecipeDetails(recipeId: String): Resource<RecipeDetails> {
        val result = safeApiCall.executeWithRetry(maxAttempts = 2) { apiService.getRecipe(recipeId).toDomain() }
        if (result is Resource.Success) {
            cacheRecipe(result.data)
        }
        return result
    }

    override suspend fun createRecipe(draft: RecipeDraft): Resource<RecipeDetails> {
        val result = safeApiCall.execute { apiService.createRecipe(draft.toRequest(mediaUploader)).toDomain() }
        if (result is Resource.Success) {
            cacheRecipe(result.data)
        }
        return result
    }

    override suspend fun updateRecipe(recipeId: String, draft: RecipeDraft): Resource<RecipeDetails> {
        val result = safeApiCall.execute { apiService.updateRecipe(recipeId, draft.toRequest(mediaUploader)).toDomain() }
        if (result is Resource.Success) {
            cacheRecipe(result.data)
        }
        return result
    }

    override suspend fun deleteRecipe(recipeId: String): Resource<Unit> {
        val result = safeApiCall.execute { deleteRecipeRequest(recipeId) }
        if (result is Resource.Success) {
            recipeCacheDao.deleteRecipe(recipeId)
        }
        return result
    }

    override suspend fun rateRecipe(recipeId: String, value: Int): Resource<RecipeDetails> {
        val result = safeApiCall.executeWithRetry(maxAttempts = 2) {
            apiService.rateRecipe(recipeId, RatingRequestDto(value)).toDomain()
        }
        if (result is Resource.Success) {
            cacheRecipe(result.data)
        }
        return result
    }

    override suspend fun clearRating(recipeId: String): Resource<Unit> =
        safeApiCall.execute { clearRatingRequest(recipeId) }

    override suspend fun toggleFavorite(recipeId: String, currentlyFavorite: Boolean): Resource<RecipeDetails> {
        return safeApiCall.execute {
            if (currentlyFavorite) {
                removeFavoriteRequest(recipeId)
                apiService.getRecipe(recipeId).toDomain()
            } else {
                apiService.addFavorite(recipeId).toDomain()
            }
        }
            .also { result ->
                if (result is Resource.Success) {
                    cacheRecipe(result.data)
                }
            }
    }

    override suspend fun getComments(recipeId: String): Resource<List<Comment>> =
        safeApiCall.executeWithRetry(maxAttempts = 2) { apiService.getComments(recipeId).map { it.toDomain() } }

    override suspend fun addComment(recipeId: String, text: String, parentCommentId: String?): Resource<Comment> =
        safeApiCall.execute {
            apiService.createComment(
                recipeId = recipeId,
                body = CreateCommentRequestDto(text = text, parentCommentId = parentCommentId),
            ).toDomain()
        }

    private suspend fun cacheRecipes(recipes: List<com.example.recipebookapp.core.model.Recipe>) {
        if (recipes.isEmpty()) return
        recipeCacheDao.upsertRecipes(recipes.map { it.toCacheEntity() })
    }

    private suspend fun cacheRecipe(recipe: RecipeDetails) {
        recipeCacheDao.upsertRecipe(recipe.toCacheEntity())
    }

    private suspend fun deleteRecipeRequest(recipeId: String) {
        apiService.deleteRecipe(recipeId)
    }

    private suspend fun clearRatingRequest(recipeId: String) {
        apiService.deleteRating(recipeId)
    }

    private suspend fun removeFavoriteRequest(recipeId: String) {
        apiService.removeFavorite(recipeId)
    }
}

private fun RecipeFilters.isDefaultCatalog(): Boolean =
    query.isBlank() && category.isBlank() && timeRange.isBlank() && sort == "newest"

private suspend fun RecipeDraft.toRequest(mediaUploader: MediaUploader): RecipeUpsertRequestDto {
    val uploadedImageUrls = imageUrls
        .filter { it.isNotBlank() }
        .map { value -> mediaUploader.resolveForServerStorage(value) }

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
