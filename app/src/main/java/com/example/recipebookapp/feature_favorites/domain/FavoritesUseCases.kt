package com.example.recipebookapp.feature_favorites.domain

import javax.inject.Inject

data class FavoritesUseCases @Inject constructor(
    val getFavorites: GetFavoritesUseCase,
    val removeFavorite: RemoveFavoriteUseCase,
)

class GetFavoritesUseCase @Inject constructor(
    private val repository: FavoritesRepository,
) {
    suspend operator fun invoke() = repository.getFavorites()
}

class RemoveFavoriteUseCase @Inject constructor(
    private val repository: FavoritesRepository,
) {
    suspend operator fun invoke(recipeId: String) = repository.removeFavorite(recipeId)
}

fun favoritesUseCases(repository: FavoritesRepository): FavoritesUseCases = FavoritesUseCases(
    getFavorites = GetFavoritesUseCase(repository),
    removeFavorite = RemoveFavoriteUseCase(repository),
)
