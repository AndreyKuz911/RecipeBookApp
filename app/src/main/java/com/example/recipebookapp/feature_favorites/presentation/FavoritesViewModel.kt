package com.example.recipebookapp.feature_favorites.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipebookapp.core.common.Resource
import com.example.recipebookapp.core.model.Recipe
import com.example.recipebookapp.core.presentation.AsyncState
import com.example.recipebookapp.feature_favorites.domain.FavoritesRepository
import com.example.recipebookapp.feature_favorites.domain.FavoritesUseCases
import com.example.recipebookapp.feature_favorites.domain.favoritesUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoritesUseCases: FavoritesUseCases,
) : ViewModel() {
    constructor(repository: FavoritesRepository) : this(favoritesUseCases(repository))

    private val _state = MutableStateFlow<AsyncState<List<Recipe>>>(AsyncState.Loading)
    val state = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = AsyncState.Loading
            when (val result = favoritesUseCases.loadFavorites()) {
                is Resource.Success -> _state.value = if (result.data.isEmpty()) AsyncState.Empty else AsyncState.Success(result.data)
                is Resource.Error -> _state.value = AsyncState.Error(result.message)
            }
        }
    }

    fun removeFromFavorites(recipeId: String) {
        val current = (_state.value as? AsyncState.Success)?.data ?: return
        viewModelScope.launch {
            when (favoritesUseCases.removeFavorite(recipeId)) {
                is Resource.Success -> {
                    val updated = favoritesUseCases.applyRemovedFavorite(current, recipeId)
                    _state.value = if (updated.isEmpty()) AsyncState.Empty else AsyncState.Success(updated)
                }
                is Resource.Error -> refresh()
            }
        }
    }
}
