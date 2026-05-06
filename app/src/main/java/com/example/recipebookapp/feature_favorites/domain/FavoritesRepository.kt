package com.example.recipebookapp.feature_favorites.domain

import com.example.recipebookapp.core.common.Resource
import com.example.recipebookapp.core.model.Recipe

interface FavoritesRepository {
    suspend fun getFavorites(): Resource<List<Recipe>>
    suspend fun removeFavorite(recipeId: String): Resource<Unit>
}
