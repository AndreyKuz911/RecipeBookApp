package com.example.recipebookapp.feature_recipes.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipebookapp.core.common.Resource
import com.example.recipebookapp.core.model.Comment
import com.example.recipebookapp.core.model.Recipe
import com.example.recipebookapp.core.model.RecipeDetails
import com.example.recipebookapp.core.presentation.AsyncState
import com.example.recipebookapp.core.presentation.toAsyncState
import com.example.recipebookapp.feature_recipes.domain.RecipeDraft
import com.example.recipebookapp.feature_recipes.domain.RecipeUseCases
import com.example.recipebookapp.feature_recipes.domain.RecipesRepository
import com.example.recipebookapp.feature_recipes.domain.model.PagedRecipes
import com.example.recipebookapp.feature_recipes.domain.model.RecipeFilters
import com.example.recipebookapp.feature_recipes.domain.recipeUseCases
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
    val actionInProgress: Boolean = false,
)

data class RecipeEditorUiState(
    val draft: RecipeDraft = RecipeDraft(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val savedRecipeId: String? = null,
)

private fun Resource<PagedRecipes>.toRecipeListState(): AsyncState<List<Recipe>> = when (val state = toAsyncState { it.items.isEmpty() }) {
    is AsyncState.Success -> AsyncState.Success(state.data.items)
    AsyncState.Empty -> AsyncState.Empty
    is AsyncState.Error -> state
    AsyncState.Loading -> AsyncState.Loading
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val recipeUseCases: RecipeUseCases,
) : ViewModel() {
    constructor(repository: RecipesRepository) : this(recipeUseCases(repository))

    private val _state = MutableStateFlow(RecipeListUiState())
    val state = _state.asStateFlow()

    init {
        loadRecipes()
    }

    fun loadRecipes() {
        viewModelScope.launch {
            _state.value = _state.value.copy(state = AsyncState.Loading)
            val result = recipeUseCases.getRecipes(RecipeFilters())
            _state.value = _state.value.copy(state = result.toRecipeListState())
        }
    }
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val recipeUseCases: RecipeUseCases,
) : ViewModel() {
    constructor(repository: RecipesRepository) : this(recipeUseCases(repository))

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
            val result = recipeUseCases.getRecipes(_state.value.filters)
            _state.value = _state.value.copy(state = result.toRecipeListState())
        }
    }
}

@HiltViewModel
class RecipeDetailsViewModel @Inject constructor(
    private val recipeUseCases: RecipeUseCases,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    constructor(repository: RecipesRepository, savedStateHandle: SavedStateHandle) : this(
        recipeUseCases = recipeUseCases(repository),
        savedStateHandle = savedStateHandle,
    )

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
            val content = recipeUseCases.loadRecipeDetailsContent(recipeId)
            _state.value = _state.value.copy(
                detailsState = content.details.toAsyncState(),
                commentsState = content.comments.toAsyncState(List<Comment>::isEmpty),
            )
        }
    }

    fun setRating(value: Int) {
        val previous = (_state.value.detailsState as? AsyncState.Success)?.data ?: return
        val optimistic = recipeUseCases.applyOptimisticRating(previous, value)
        _state.value = _state.value.copy(
            detailsState = AsyncState.Success(optimistic),
            actionInProgress = true,
        )
        viewModelScope.launch {
            when (val result = recipeUseCases.rateRecipe(recipeId, value)) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        detailsState = AsyncState.Success(result.data),
                        actionInProgress = false,
                    )
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        detailsState = AsyncState.Success(previous),
                        actionInProgress = false,
                    )
                }
            }
        }
    }

    fun toggleFavorite() {
        val details = (_state.value.detailsState as? AsyncState.Success)?.data ?: return
        val optimistic = recipeUseCases.applyOptimisticFavorite(details)
        _state.value = _state.value.copy(
            detailsState = AsyncState.Success(optimistic),
            actionInProgress = true,
        )
        viewModelScope.launch {
            when (val result = recipeUseCases.toggleRecipeFavorite(recipeId, details.isFavorite)) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        detailsState = AsyncState.Success(result.data),
                        actionInProgress = false,
                    )
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        detailsState = AsyncState.Success(details),
                        actionInProgress = false,
                    )
                }
            }
        }
    }

    fun addComment() {
        val text = _state.value.commentText.trim()
        if (text.isBlank()) return
        viewModelScope.launch {
            when (val result = recipeUseCases.addRecipeComment(recipeId, text)) {
                is Resource.Success -> {
                    val existingComments = (_state.value.commentsState as? AsyncState.Success)?.data.orEmpty()
                    val updatedComments = recipeUseCases.prependComment(existingComments, result.data)
                    _state.value = _state.value.copy(
                        commentText = "",
                        commentsState = AsyncState.Success(updatedComments),
                    )
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(commentsState = AsyncState.Error(result.message))
                }
            }
        }
    }
}

@HiltViewModel
class EditRecipeViewModel @Inject constructor(
    private val recipeUseCases: RecipeUseCases,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    constructor(repository: RecipesRepository, savedStateHandle: SavedStateHandle) : this(
        recipeUseCases = recipeUseCases(repository),
        savedStateHandle = savedStateHandle,
    )

    private val recipeId: String? = savedStateHandle["recipeId"]
    private val _state = MutableStateFlow(RecipeEditorUiState())
    val state = _state.asStateFlow()

    init {
        if (!recipeId.isNullOrBlank()) {
            viewModelScope.launch {
                when (val result = recipeUseCases.getRecipeDetails(recipeId)) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(draft = recipeUseCases.buildRecipeDraft(result.data))
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
                recipeUseCases.saveRecipe.create(_state.value.draft)
            } else {
                recipeUseCases.saveRecipe.update(recipeId, _state.value.draft)
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
