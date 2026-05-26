package com.example.recipebookapp.feature_recipes.domain

import com.example.recipebookapp.feature_recipes.domain.model.RecipeFilters
import javax.inject.Inject

data class RecipeUseCases @Inject constructor(
    val getRecipes: GetRecipesUseCase,
    val getRecipeDetails: GetRecipeDetailsUseCase,
    val getRecipeComments: GetRecipeCommentsUseCase,
    val rateRecipe: RateRecipeUseCase,
    val toggleRecipeFavorite: ToggleRecipeFavoriteUseCase,
    val addRecipeComment: AddRecipeCommentUseCase,
    val saveRecipe: SaveRecipeUseCase,
)

class GetRecipesUseCase @Inject constructor(
    private val repository: RecipesRepository,
) {
    suspend operator fun invoke(filters: RecipeFilters) = repository.getRecipes(filters)
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

fun recipeUseCases(repository: RecipesRepository): RecipeUseCases = RecipeUseCases(
    getRecipes = GetRecipesUseCase(repository),
    getRecipeDetails = GetRecipeDetailsUseCase(repository),
    getRecipeComments = GetRecipeCommentsUseCase(repository),
    rateRecipe = RateRecipeUseCase(repository),
    toggleRecipeFavorite = ToggleRecipeFavoriteUseCase(repository),
    addRecipeComment = AddRecipeCommentUseCase(repository),
    saveRecipe = SaveRecipeUseCase(repository),
)
