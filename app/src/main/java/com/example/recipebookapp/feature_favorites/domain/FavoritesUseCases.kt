package com.example.recipebookapp.feature_favorites.domain

import javax.inject.Inject

data class FavoritesUseCases @Inject constructor(
    val loadFavorites: LoadFavoritesUseCase,
    val removeFavorite: RemoveFavoriteUseCase,
    val applyRemovedFavorite: ApplyRemovedFavoriteUseCase,
)

class LoadFavoritesUseCase @Inject constructor(
    private val repository: FavoritesRepository,
) {
    suspend operator fun invoke() = repository.getFavorites()
}

class RemoveFavoriteUseCase @Inject constructor(
    private val repository: FavoritesRepository,
) {
    suspend operator fun invoke(recipeId: String) = repository.removeFavorite(recipeId)
}

class ApplyRemovedFavoriteUseCase @Inject constructor() {
    operator fun invoke(current: List<com.example.recipebookapp.core.model.Recipe>, recipeId: String) =
        current.filterNot { it.id == recipeId }
}

fun favoritesUseCases(repository: FavoritesRepository): FavoritesUseCases = FavoritesUseCases(
    loadFavorites = LoadFavoritesUseCase(repository),
    removeFavorite = RemoveFavoriteUseCase(repository),
    applyRemovedFavorite = ApplyRemovedFavoriteUseCase(),
)
