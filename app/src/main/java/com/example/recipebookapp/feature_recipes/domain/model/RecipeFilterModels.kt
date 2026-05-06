package com.example.recipebookapp.feature_recipes.domain.model

import com.example.recipebookapp.core.model.Recipe

data class RecipeFilters(
    val query: String = "",
    val category: String = "",
    val timeRange: String = "",
    val sort: String = "newest",
)

data class PagedRecipes(
    val items: List<Recipe>,
    val page: Int,
    val limit: Int,
    val total: Int,
)

object RecipeCatalog {
    val categories = listOf(
        "Супы",
        "Салаты",
        "Горячее",
        "Выпечка и десерты",
        "Напитки",
        "Закуски",
        "Завтраки",
    )

    val timeRanges = listOf("", "up_to_15", "15-30", "30-60", "60+")
    val sortOptions = listOf("newest", "rating")
}
