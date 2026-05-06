package com.example.recipebookapp.feature_recipes.domain

import com.example.recipebookapp.feature_recipes.domain.model.RecipeFilters
import javax.inject.Inject

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

class SaveRecipeUseCase @Inject constructor(
    private val repository: RecipesRepository,
) {
    suspend fun create(draft: RecipeDraft) = repository.createRecipe(draft)
    suspend fun update(recipeId: String, draft: RecipeDraft) = repository.updateRecipe(recipeId, draft)
}
