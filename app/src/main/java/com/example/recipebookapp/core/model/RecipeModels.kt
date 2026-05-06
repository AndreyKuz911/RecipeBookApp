package com.example.recipebookapp.core.model

data class Recipe(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val cookingTimeMinutes: Int,
    val imageUrl: String?,
    val author: UserSummary,
    val createdAt: String,
    val updatedAt: String,
    val likesCount: Int,
    val dislikesCount: Int,
    val rating: Int,
    val isFavorite: Boolean,
    val myRating: Int?,
)

data class RecipeDetails(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val cookingTimeMinutes: Int,
    val ingredients: List<String>,
    val steps: List<String>,
    val imageUrls: List<String>,
    val author: UserSummary,
    val createdAt: String,
    val updatedAt: String,
    val likesCount: Int,
    val dislikesCount: Int,
    val rating: Int,
    val isFavorite: Boolean,
    val myRating: Int?,
)

data class Comment(
    val id: String,
    val recipeId: String,
    val parentCommentId: String?,
    val text: String,
    val createdAt: String,
    val author: UserSummary,
    val replies: List<Comment> = emptyList(),
)
