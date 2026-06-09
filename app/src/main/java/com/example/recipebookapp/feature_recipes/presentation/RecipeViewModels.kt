package com.example.recipebookapp.feature_recipes.presentation

import com.example.recipebookapp.core.common.RecipeSyncNotifier
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
import com.example.recipebookapp.feature_recipes.domain.model.RecipeFilters
import com.example.recipebookapp.feature_recipes.domain.recipeUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
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
    val ratingInProgress: Boolean = false,
    val favoriteInProgress: Boolean = false,
)

data class RecipeEditorUiState(
    val draft: RecipeDraft = RecipeDraft(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val savedRecipeId: String? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val recipeUseCases: RecipeUseCases,
    private val recipeSyncNotifier: RecipeSyncNotifier,
) : ViewModel() {
    constructor(
        repository: RecipesRepository,
        recipeSyncNotifier: RecipeSyncNotifier = RecipeSyncNotifier(),
    ) : this(recipeUseCases(repository), recipeSyncNotifier)

    private val _state = MutableStateFlow(RecipeListUiState())
    val state = _state.asStateFlow()

    init {
        loadRecipes()
        viewModelScope.launch {
            recipeSyncNotifier.recipeMutations.collect {
                loadRecipes()
            }
        }
    }

    fun loadRecipes() {
        viewModelScope.launch {
            _state.value = _state.value.copy(state = AsyncState.Loading)
            val result = recipeUseCases.loadRecipeList(RecipeFilters())
            _state.value = _state.value.copy(state = result.toAsyncState(List<Recipe>::isEmpty))
        }
    }
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val recipeUseCases: RecipeUseCases,
    private val recipeSyncNotifier: RecipeSyncNotifier,
) : ViewModel() {
    constructor(
        repository: RecipesRepository,
        recipeSyncNotifier: RecipeSyncNotifier = RecipeSyncNotifier(),
    ) : this(recipeUseCases(repository), recipeSyncNotifier)

    private val _state = MutableStateFlow(RecipeListUiState(filters = RecipeFilters()))
    val state = _state.asStateFlow()

    init {
        search()
        viewModelScope.launch {
            recipeSyncNotifier.recipeMutations.collect {
                search()
            }
        }
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
            val result = recipeUseCases.loadRecipeList(_state.value.filters)
            _state.value = _state.value.copy(state = result.toAsyncState(List<Recipe>::isEmpty))
        }
    }
}

@HiltViewModel
class RecipeDetailsViewModel @Inject constructor(
    private val recipeUseCases: RecipeUseCases,
    private val recipeSyncNotifier: RecipeSyncNotifier,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    constructor(
        repository: RecipesRepository,
        savedStateHandle: SavedStateHandle,
        recipeSyncNotifier: RecipeSyncNotifier = RecipeSyncNotifier(),
    ) : this(
        recipeUseCases = recipeUseCases(repository),
        recipeSyncNotifier = recipeSyncNotifier,
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
            ratingInProgress = true,
        )
        viewModelScope.launch {
            when (val result = recipeUseCases.rateRecipe(recipeId, value)) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        detailsState = AsyncState.Success(result.data),
                        ratingInProgress = false,
                    )
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        detailsState = AsyncState.Success(previous),
                        ratingInProgress = false,
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
            favoriteInProgress = true,
        )
        viewModelScope.launch {
            when (val result = recipeUseCases.toggleRecipeFavorite(recipeId, details.isFavorite)) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        detailsState = AsyncState.Success(result.data),
                        favoriteInProgress = false,
                    )
                    recipeSyncNotifier.notifyFavoriteMutated()
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        detailsState = AsyncState.Success(details),
                        favoriteInProgress = false,
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
    private val recipeSyncNotifier: RecipeSyncNotifier,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    constructor(
        repository: RecipesRepository,
        savedStateHandle: SavedStateHandle,
        recipeSyncNotifier: RecipeSyncNotifier = RecipeSyncNotifier(),
    ) : this(
        recipeUseCases = recipeUseCases(repository),
        recipeSyncNotifier = recipeSyncNotifier,
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
                is Resource.Success -> {
                    recipeSyncNotifier.notifyRecipeMutated()
                    _state.value = _state.value.copy(
                        isLoading = false,
                        savedRecipeId = result.data.id,
                    )
                }
                is Resource.Error -> _state.value = _state.value.copy(isLoading = false, error = result.message)
            }
        }
    }
}
