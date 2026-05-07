package com.example.recipebookapp.feature_favorites.data

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
) : FavoritesRepository {
    override suspend fun getFavorites(): Resource<List<Recipe>> {
        return safeApiCall.execute { apiService.getFavorites().map { it.toDomain() } }
    }

    override suspend fun removeFavorite(recipeId: String): Resource<Unit> {
        return safeApiCall.execute {
            apiService.removeFavorite(recipeId)
            Unit
        }
    }
}
