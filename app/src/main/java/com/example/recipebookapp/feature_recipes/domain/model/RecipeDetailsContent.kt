package com.example.recipebookapp.feature_recipes.domain.model

import com.example.recipebookapp.core.common.Resource
import com.example.recipebookapp.core.model.Comment
import com.example.recipebookapp.core.model.RecipeDetails

data class RecipeDetailsContent(
    val details: Resource<RecipeDetails>,
    val comments: Resource<List<Comment>>,
)
