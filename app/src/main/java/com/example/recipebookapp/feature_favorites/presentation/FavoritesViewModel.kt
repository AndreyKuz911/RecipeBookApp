package com.example.recipebookapp.feature_favorites.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipebookapp.core.common.RecipeSyncNotifier
import com.example.recipebookapp.core.common.Resource
import com.example.recipebookapp.core.model.Recipe
import com.example.recipebookapp.core.presentation.AsyncState
import com.example.recipebookapp.core.presentation.toAsyncState
import com.example.recipebookapp.feature_favorites.domain.FavoritesRepository
import com.example.recipebookapp.feature_favorites.domain.FavoritesUseCases
import com.example.recipebookapp.feature_favorites.domain.favoritesUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoritesUseCases: FavoritesUseCases,
    private val recipeSyncNotifier: RecipeSyncNotifier,
) : ViewModel() {
    constructor(
        repository: FavoritesRepository,
        recipeSyncNotifier: RecipeSyncNotifier = RecipeSyncNotifier(),
    ) : this(favoritesUseCases(repository), recipeSyncNotifier)

    private val _state = MutableStateFlow<AsyncState<List<Recipe>>>(AsyncState.Loading)
    val state = _state.asStateFlow()

    init {
        refresh()
        viewModelScope.launch {
            recipeSyncNotifier.favoriteMutations.collect {
                refresh()
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = AsyncState.Loading
            _state.value = favoritesUseCases.loadFavorites().toAsyncState(List<Recipe>::isEmpty)
        }
    }

    fun removeFromFavorites(recipeId: String) {
        val current = (_state.value as? AsyncState.Success)?.data ?: return
        viewModelScope.launch {
            when (favoritesUseCases.removeFavorite(recipeId)) {
                is Resource.Success -> {
                    val updated = favoritesUseCases.applyRemovedFavorite(current, recipeId)
                    _state.value = if (updated.isEmpty()) AsyncState.Empty else AsyncState.Success(updated)
                    recipeSyncNotifier.notifyFavoriteMutated()
                }
                is Resource.Error -> refresh()
            }
        }
    }
}
