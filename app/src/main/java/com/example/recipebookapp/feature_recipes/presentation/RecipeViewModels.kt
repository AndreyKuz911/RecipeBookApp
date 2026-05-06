package com.example.recipebookapp.feature_recipes.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipebookapp.core.common.Resource
import com.example.recipebookapp.core.model.Comment
import com.example.recipebookapp.core.model.Recipe
import com.example.recipebookapp.core.model.RecipeDetails
import com.example.recipebookapp.core.presentation.AsyncState
import com.example.recipebookapp.feature_recipes.domain.RecipeDraft
import com.example.recipebookapp.feature_recipes.domain.RecipesRepository
import com.example.recipebookapp.feature_recipes.domain.model.RecipeFilters
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecipeListUiState(
    val filters: RecipeFilters = RecipeFilters(),
    val state: AsyncState<List<Recipe>> = AsyncState.Loading,
)

data class RecipeDetailsUiState(
    val detailsState: AsyncState<RecipeDetails> = AsyncState.Loading,
    val commentsState: AsyncState<List<Comment>> = AsyncState.Loading,
    val commentText: String = "",
)

data class RecipeEditorUiState(
    val draft: RecipeDraft = RecipeDraft(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val savedRecipeId: String? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: RecipesRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(RecipeListUiState())
    val state = _state.asStateFlow()

    init {
        loadRecipes()
    }

    fun loadRecipes() {
        viewModelScope.launch {
            _state.value = _state.value.copy(state = AsyncState.Loading)
            when (val result = repository.getRecipes(RecipeFilters())) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        state = if (result.data.items.isEmpty()) AsyncState.Empty else AsyncState.Success(result.data.items),
                    )
                }
                is Resource.Error -> _state.value = _state.value.copy(state = AsyncState.Error(result.message))
            }
        }
    }
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: RecipesRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(RecipeListUiState(filters = RecipeFilters()))
    val state = _state.asStateFlow()

    init {
        search()
    }

    fun updateFilters(filters: RecipeFilters) {
        _state.value = _state.value.copy(filters = filters)
    }

    fun resetFilters() {
        _state.value = _state.value.copy(filters = RecipeFilters())
        search()
    }

    fun search() {
        viewModelScope.launch {
            _state.value = _state.value.copy(state = AsyncState.Loading)
            when (val result = repository.getRecipes(_state.value.filters)) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        state = if (result.data.items.isEmpty()) AsyncState.Empty else AsyncState.Success(result.data.items),
                    )
                }
                is Resource.Error -> _state.value = _state.value.copy(state = AsyncState.Error(result.message))
            }
        }
    }
}

@HiltViewModel
class RecipeDetailsViewModel @Inject constructor(
    private val repository: RecipesRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val recipeId: String = checkNotNull(savedStateHandle["recipeId"])
    private val _state = MutableStateFlow(RecipeDetailsUiState())
    val state = _state.asStateFlow()

    init {
        refresh()
    }

    fun updateComment(value: String) {
        _state.value = _state.value.copy(commentText = value)
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(detailsState = AsyncState.Loading, commentsState = AsyncState.Loading)
            when (val details = repository.getRecipeDetails(recipeId)) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(detailsState = AsyncState.Success(details.data))
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(detailsState = AsyncState.Error(details.message))
                }
            }
            when (val comments = repository.getComments(recipeId)) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        commentsState = if (comments.data.isEmpty()) AsyncState.Empty else AsyncState.Success(comments.data),
                    )
                }
                is Resource.Error -> _state.value = _state.value.copy(commentsState = AsyncState.Error(comments.message))
            }
        }
    }

    fun setRating(value: Int) {
        viewModelScope.launch {
            repository.rateRecipe(recipeId, value)
            refresh()
        }
    }

    fun toggleFavorite() {
        val details = (_state.value.detailsState as? AsyncState.Success)?.data ?: return
        viewModelScope.launch {
            repository.toggleFavorite(recipeId, details.isFavorite)
            refresh()
        }
    }

    fun addComment() {
        val text = _state.value.commentText.trim()
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.addComment(recipeId, text)
            _state.value = _state.value.copy(commentText = "")
            refresh()
        }
    }
}

@HiltViewModel
class EditRecipeViewModel @Inject constructor(
    private val repository: RecipesRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val recipeId: String? = savedStateHandle["recipeId"]
    private val _state = MutableStateFlow(RecipeEditorUiState())
    val state = _state.asStateFlow()

    init {
        if (!recipeId.isNullOrBlank()) {
            viewModelScope.launch {
                when (val result = repository.getRecipeDetails(recipeId)) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(draft = result.data.toDraft())
                    }
                    is Resource.Error -> _state.value = _state.value.copy(error = result.message)
                }
            }
        }
    }

    fun updateDraft(transform: (RecipeDraft) -> RecipeDraft) {
        _state.value = _state.value.copy(draft = transform(_state.value.draft), error = null)
    }

    fun saveRecipe() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val result = if (recipeId.isNullOrBlank()) {
                repository.createRecipe(_state.value.draft)
            } else {
                repository.updateRecipe(recipeId, _state.value.draft)
            }
            when (result) {
                is Resource.Success -> _state.value = _state.value.copy(
                    isLoading = false,
                    savedRecipeId = result.data.id,
                )
                is Resource.Error -> _state.value = _state.value.copy(isLoading = false, error = result.message)
            }
        }
    }
}

private fun RecipeDetails.toDraft(): RecipeDraft = RecipeDraft(
    title = title,
    description = description,
    category = category,
    cookingTimeMinutes = cookingTimeMinutes.toString(),
    ingredients = ingredients.ifEmpty { listOf("") },
    steps = steps.ifEmpty { listOf("") },
    imageUrls = imageUrls.ifEmpty { listOf("") },
)
