package com.example.recipebookapp.feature_recipes.domain

import com.example.recipebookapp.core.model.Comment
import com.example.recipebookapp.core.model.Recipe
import com.example.recipebookapp.core.model.RecipeDetails
import com.example.recipebookapp.feature_recipes.domain.model.RecipeDetailsContent
import com.example.recipebookapp.feature_recipes.domain.model.RecipeFilters
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

data class RecipeUseCases @Inject constructor(
    val getRecipes: GetRecipesUseCase,
    val loadRecipeList: LoadRecipeListUseCase,
    val getRecipeDetails: GetRecipeDetailsUseCase,
    val getRecipeComments: GetRecipeCommentsUseCase,
    val loadRecipeDetailsContent: LoadRecipeDetailsContentUseCase,
    val rateRecipe: RateRecipeUseCase,
    val toggleRecipeFavorite: ToggleRecipeFavoriteUseCase,
    val addRecipeComment: AddRecipeCommentUseCase,
    val saveRecipe: SaveRecipeUseCase,
    val buildRecipeDraft: BuildRecipeDraftUseCase,
    val applyOptimisticRating: ApplyOptimisticRatingUseCase,
    val applyOptimisticFavorite: ApplyOptimisticFavoriteUseCase,
    val prependComment: PrependCommentUseCase,
)

class GetRecipesUseCase @Inject constructor(
    private val repository: RecipesRepository,
) {
    suspend operator fun invoke(filters: RecipeFilters) = repository.getRecipes(filters)
}

class LoadRecipeListUseCase @Inject constructor(
    private val getRecipes: GetRecipesUseCase,
) {
    suspend operator fun invoke(filters: RecipeFilters) = when (val result = getRecipes(filters)) {
        is com.example.recipebookapp.core.common.Resource.Success ->
            com.example.recipebookapp.core.common.Resource.Success(result.data.items)
        is com.example.recipebookapp.core.common.Resource.Error -> result
    }
}

class GetRecipeDetailsUseCase @Inject constructor(
    private val repository: RecipesRepository,
) {
    suspend operator fun invoke(recipeId: String) = repository.getRecipeDetails(recipeId)
}

class GetRecipeCommentsUseCase @Inject constructor(
    private val repository: RecipesRepository,
) {
    suspend operator fun invoke(recipeId: String) = repository.getComments(recipeId)
}

class LoadRecipeDetailsContentUseCase @Inject constructor(
    private val getRecipeDetails: GetRecipeDetailsUseCase,
    private val getRecipeComments: GetRecipeCommentsUseCase,
) {
    suspend operator fun invoke(recipeId: String): RecipeDetailsContent = coroutineScope {
        val detailsDeferred = async { getRecipeDetails(recipeId) }
        val commentsDeferred = async { getRecipeComments(recipeId) }
        RecipeDetailsContent(
            details = detailsDeferred.await(),
            comments = commentsDeferred.await(),
        )
    }
}

class RateRecipeUseCase @Inject constructor(
    private val repository: RecipesRepository,
) {
    suspend operator fun invoke(recipeId: String, value: Int) = repository.rateRecipe(recipeId, value)
}

class ToggleRecipeFavoriteUseCase @Inject constructor(
    private val repository: RecipesRepository,
) {
    suspend operator fun invoke(recipeId: String, currentlyFavorite: Boolean) =
        repository.toggleFavorite(recipeId, currentlyFavorite)
}

class AddRecipeCommentUseCase @Inject constructor(
    private val repository: RecipesRepository,
) {
    suspend operator fun invoke(recipeId: String, text: String, parentCommentId: String? = null) =
        repository.addComment(recipeId, text, parentCommentId)
}

class SaveRecipeUseCase @Inject constructor(
    private val repository: RecipesRepository,
) {
    suspend fun create(draft: RecipeDraft) = repository.createRecipe(draft)
    suspend fun update(recipeId: String, draft: RecipeDraft) = repository.updateRecipe(recipeId, draft)
}

class BuildRecipeDraftUseCase @Inject constructor() {
    operator fun invoke(details: RecipeDetails): RecipeDraft = RecipeDraft(
        title = details.title,
        description = details.description,
        category = details.category,
        cookingTimeMinutes = details.cookingTimeMinutes.toString(),
        ingredients = details.ingredients.ifEmpty { listOf("") },
        steps = details.steps.ifEmpty { listOf("") },
        imageUrls = details.imageUrls.ifEmpty { listOf("") },
    )
}

class ApplyOptimisticRatingUseCase @Inject constructor() {
    operator fun invoke(details: RecipeDetails, newValue: Int): RecipeDetails {
        val previousRating = details.myRating
        if (previousRating == newValue) {
            return when (newValue) {
                1 -> details.copy(
                    likesCount = (details.likesCount - 1).coerceAtLeast(0),
                    rating = details.rating - 1,
                    myRating = null,
                )
                -1 -> details.copy(
                    dislikesCount = (details.dislikesCount - 1).coerceAtLeast(0),
                    rating = details.rating + 1,
                    myRating = null,
                )
                else -> details
            }
        }

        return when (newValue) {
            1 -> when (previousRating) {
                -1 -> details.copy(
                    likesCount = details.likesCount + 1,
                    dislikesCount = (details.dislikesCount - 1).coerceAtLeast(0),
                    rating = details.rating + 2,
                    myRating = 1,
                )
                null -> details.copy(
                    likesCount = details.likesCount + 1,
                    rating = details.rating + 1,
                    myRating = 1,
                )
                else -> details
            }
            -1 -> when (previousRating) {
                1 -> details.copy(
                    likesCount = (details.likesCount - 1).coerceAtLeast(0),
                    dislikesCount = details.dislikesCount + 1,
                    rating = details.rating - 2,
                    myRating = -1,
                )
                null -> details.copy(
                    dislikesCount = details.dislikesCount + 1,
                    rating = details.rating - 1,
                    myRating = -1,
                )
                else -> details
            }
            else -> details
        }
    }
}

class ApplyOptimisticFavoriteUseCase @Inject constructor() {
    operator fun invoke(details: RecipeDetails): RecipeDetails = details.copy(
        isFavorite = !details.isFavorite,
    )
}

class PrependCommentUseCase @Inject constructor() {
    operator fun invoke(existing: List<Comment>, comment: Comment): List<Comment> = listOf(comment) + existing
}

fun recipeUseCases(repository: RecipesRepository): RecipeUseCases = RecipeUseCases(
    getRecipes = GetRecipesUseCase(repository),
    loadRecipeList = LoadRecipeListUseCase(
        getRecipes = GetRecipesUseCase(repository),
    ),
    getRecipeDetails = GetRecipeDetailsUseCase(repository),
    getRecipeComments = GetRecipeCommentsUseCase(repository),
    loadRecipeDetailsContent = LoadRecipeDetailsContentUseCase(
        getRecipeDetails = GetRecipeDetailsUseCase(repository),
        getRecipeComments = GetRecipeCommentsUseCase(repository),
    ),
    rateRecipe = RateRecipeUseCase(repository),
    toggleRecipeFavorite = ToggleRecipeFavoriteUseCase(repository),
    addRecipeComment = AddRecipeCommentUseCase(repository),
    saveRecipe = SaveRecipeUseCase(repository),
    buildRecipeDraft = BuildRecipeDraftUseCase(),
    applyOptimisticRating = ApplyOptimisticRatingUseCase(),
    applyOptimisticFavorite = ApplyOptimisticFavoriteUseCase(),
    prependComment = PrependCommentUseCase(),
)
