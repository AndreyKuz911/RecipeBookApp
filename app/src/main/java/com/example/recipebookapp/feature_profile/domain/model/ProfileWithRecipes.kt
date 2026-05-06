package com.example.recipebookapp.feature_profile.domain.model

import com.example.recipebookapp.core.model.Recipe
import com.example.recipebookapp.core.model.UserProfile

data class ProfileWithRecipes(
    val profile: UserProfile,
    val recipes: List<Recipe>,
)
