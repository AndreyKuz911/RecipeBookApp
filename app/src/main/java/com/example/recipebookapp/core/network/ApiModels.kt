package com.example.recipebookapp.core.network

import com.example.recipebookapp.core.model.Comment
import com.example.recipebookapp.core.model.Recipe
import com.example.recipebookapp.core.model.RecipeDetails
import com.example.recipebookapp.core.model.UserProfile
import com.example.recipebookapp.core.model.UserSummary
import com.example.recipebookapp.feature_auth.domain.model.AuthSession
import com.example.recipebookapp.feature_recipes.domain.model.PagedRecipes
import kotlinx.serialization.Serializable

@Serializable
data class ErrorDto(val error: String)

@Serializable
data class RegisterRequestDto(
    val email: String,
    val username: String,
    val password: String,
)

@Serializable
data class LoginRequestDto(
    val email: String,
    val password: String,
)

@Serializable
data class UpdateProfileRequestDto(
    val username: String,
    val bio: String? = null,
    val avatarUrl: String? = null,
)

@Serializable
data class RecipeUpsertRequestDto(
    val title: String,
    val description: String,
    val category: String,
    val cookingTimeMinutes: Int,
    val ingredients: List<String>,
    val steps: List<String>,
    val imageUrls: List<String>,
)

@Serializable
data class RatingRequestDto(val value: Int)

@Serializable
data class CreateCommentRequestDto(
    val text: String,
    val parentCommentId: String? = null,
)

@Serializable
data class UserSummaryDto(
    val id: String,
    val username: String,
    val avatarUrl: String? = null,
)

@Serializable
data class UserProfileDto(
    val id: String,
    val email: String,
    val username: String,
    val bio: String? = null,
    val avatarUrl: String? = null,
    val createdAt: String,
    val recipesCount: Int,
    val followersCount: Int,
    val followingCount: Int,
    val isFollowing: Boolean = false,
)

@Serializable
data class AuthResponseDto(
    val token: String,
    val user: UserProfileDto,
)

@Serializable
data class RecipeDto(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val cookingTimeMinutes: Int,
    val imageUrl: String? = null,
    val author: UserSummaryDto,
    val createdAt: String,
    val updatedAt: String,
    val likesCount: Int,
    val dislikesCount: Int,
    val rating: Int,
    val isFavorite: Boolean,
    val myRating: Int? = null,
)

@Serializable
data class RecipeDetailsDto(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val cookingTimeMinutes: Int,
    val ingredients: List<String>,
    val steps: List<String>,
    val imageUrls: List<String>,
    val author: UserSummaryDto,
    val createdAt: String,
    val updatedAt: String,
    val likesCount: Int,
    val dislikesCount: Int,
    val rating: Int,
    val isFavorite: Boolean,
    val myRating: Int? = null,
)

@Serializable
data class CommentDto(
    val id: String,
    val recipeId: String,
    val parentCommentId: String? = null,
    val text: String,
    val createdAt: String,
    val author: UserSummaryDto,
    val replies: List<CommentDto> = emptyList(),
)

@Serializable
data class PagedRecipesResponseDto(
    val items: List<RecipeDto>,
    val page: Int,
    val limit: Int,
    val total: Int,
)

@Serializable
data class UploadMediaResponseDto(
    val url: String,
)

fun AuthResponseDto.toDomain(): AuthSession = AuthSession(token = token, user = user.toDomain())

fun UserSummaryDto.toDomain(): UserSummary = UserSummary(id = id, username = username, avatarUrl = avatarUrl)

fun UserProfileDto.toDomain(): UserProfile = UserProfile(
    id = id,
    email = email,
    username = username,
    bio = bio,
    avatarUrl = avatarUrl,
    createdAt = createdAt,
    recipesCount = recipesCount,
    followersCount = followersCount,
    followingCount = followingCount,
    isFollowing = isFollowing,
)

fun RecipeDto.toDomain(): Recipe = Recipe(
    id = id,
    title = title,
    description = description,
    category = category,
    cookingTimeMinutes = cookingTimeMinutes,
    imageUrl = imageUrl,
    author = author.toDomain(),
    createdAt = createdAt,
    updatedAt = updatedAt,
    likesCount = likesCount,
    dislikesCount = dislikesCount,
    rating = rating,
    isFavorite = isFavorite,
    myRating = myRating,
)

fun RecipeDetailsDto.toDomain(): RecipeDetails = RecipeDetails(
    id = id,
    title = title,
    description = description,
    category = category,
    cookingTimeMinutes = cookingTimeMinutes,
    ingredients = ingredients,
    steps = steps,
    imageUrls = imageUrls,
    author = author.toDomain(),
    createdAt = createdAt,
    updatedAt = updatedAt,
    likesCount = likesCount,
    dislikesCount = dislikesCount,
    rating = rating,
    isFavorite = isFavorite,
    myRating = myRating,
)

fun CommentDto.toDomain(): Comment = Comment(
    id = id,
    recipeId = recipeId,
    parentCommentId = parentCommentId,
    text = text,
    createdAt = createdAt,
    author = author.toDomain(),
    replies = replies.map(CommentDto::toDomain),
)

fun PagedRecipesResponseDto.toDomain(): PagedRecipes = PagedRecipes(
    items = items.map(RecipeDto::toDomain),
    page = page,
    limit = limit,
    total = total,
)
