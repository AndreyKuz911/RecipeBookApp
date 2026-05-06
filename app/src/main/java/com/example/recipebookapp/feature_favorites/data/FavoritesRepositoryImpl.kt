package com.example.recipebookapp.feature_favorites.data

import com.example.recipebookapp.core.database.RecipeDao
import com.example.recipebookapp.core.database.toDomain
import com.example.recipebookapp.core.database.toFavoriteEntity
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
    private val recipeDao: RecipeDao,
) : FavoritesRepository {
    override suspend fun getFavorites(): Resource<List<Recipe>> {
        return when (val result = safeApiCall.execute { apiService.getFavorites().map { it.toDomain() } }) {
            is Resource.Success -> {
                recipeDao.clearFavoriteRecipes()
                recipeDao.insertFavoriteRecipes(result.data.map(Recipe::toFavoriteEntity))
                result
            }
            is Resource.Error -> result
        }
    }

    override suspend fun removeFavorite(recipeId: String): Resource<Unit> {
        return safeApiCall.execute {
            apiService.removeFavorite(recipeId)
            Unit
        }
    }
}
