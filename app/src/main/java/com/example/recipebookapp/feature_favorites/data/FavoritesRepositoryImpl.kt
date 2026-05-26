package com.example.recipebookapp.feature_favorites.data

import com.example.recipebookapp.core.database.RecipeCacheDao
import com.example.recipebookapp.core.database.toCacheEntity
import com.example.recipebookapp.core.database.toDomain
import com.example.recipebookapp.core.network.ApiService
import com.example.recipebookapp.core.network.SafeApiCall
import com.example.recipebookapp.core.network.toDomain
import com.example.recipebookapp.core.common.Resource
import com.example.recipebookapp.core.model.Recipe
import com.example.recipebookapp.feature_favorites.domain.FavoritesRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoritesRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val safeApiCall: SafeApiCall,
    private val recipeCacheDao: RecipeCacheDao,
) : FavoritesRepository {
    override suspend fun getFavorites(): Resource<List<Recipe>> {
        val result = safeApiCall.execute { apiService.getFavorites().map { it.toDomain() } }
        return when (result) {
            is Resource.Success -> {
                recipeCacheDao.clearFavoriteFlags()
                cacheFavorites(result.data)
                result
            }
            is Resource.Error -> {
                val cached = recipeCacheDao.getFavoriteRecipes().map { it.toDomain() }
                if (cached.isNotEmpty()) Resource.Success(cached) else result
            }
        }
    }

    override suspend fun removeFavorite(recipeId: String): Resource<Unit> {
        val result = safeApiCall.execute {
            apiService.removeFavorite(recipeId)
            Unit
        }
        if (result is Resource.Success) {
            recipeCacheDao.updateFavoriteState(recipeId, false)
        }
        return result
    }

    private suspend fun cacheFavorites(recipes: List<Recipe>) {
        if (recipes.isEmpty()) return
        val ids = recipes.map { it.id }
        recipeCacheDao.upsertRecipes(recipes.map { it.copy(isFavorite = true).toCacheEntity() })
        recipeCacheDao.markFavorites(ids)
    }
}
