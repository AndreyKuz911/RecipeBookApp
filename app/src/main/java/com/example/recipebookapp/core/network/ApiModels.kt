package com.example.recipebookapp.core.network

import com.example.recipebookapp.BuildConfig
import com.example.recipebookapp.core.model.CulinaryNews
import com.example.recipebookapp.core.model.Comment
import com.example.recipebookapp.core.model.Recipe
import com.example.recipebookapp.core.model.RecipeDetails
import com.example.recipebookapp.core.model.UserProfile
import com.example.recipebookapp.core.model.UserSummary
import com.example.recipebookapp.feature_auth.domain.model.AuthSession
import com.example.recipebookapp.feature_recipes.domain.model.PagedRecipes
import kotlinx.serialization.Serializable
import java.nio.charset.StandardCharsets
import java.nio.charset.Charset

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

@Serializable
data class NewsItemDto(
    val title: String,
    val summary: String,
    val url: String,
    val imageUrl: String? = null,
    val publishedAt: String,
    val source: String,
)

fun AuthResponseDto.toDomain(): AuthSession = AuthSession(token = token, user = user.toDomain())

fun UserSummaryDto.toDomain(): UserSummary = UserSummary(
    id = id,
    username = username.normalizeText(fallback = "unknown"),
    avatarUrl = avatarUrl.toServerUrlOrNull(),
)

fun UserProfileDto.toDomain(): UserProfile = UserProfile(
    id = id,
    email = email,
    username = username.normalizeText(fallback = "unknown"),
    bio = bio?.normalizeText(fallback = ""),
    avatarUrl = avatarUrl.toServerUrlOrNull(),
    createdAt = createdAt,
    recipesCount = recipesCount,
    followersCount = followersCount,
    followingCount = followingCount,
    isFollowing = isFollowing,
)

fun RecipeDto.toDomain(): Recipe = Recipe(
    id = id,
    title = title.normalizeText(fallback = "Без названия"),
    description = description.normalizeText(fallback = "Без описания"),
    category = category.normalizeText(fallback = "Без категории"),
    cookingTimeMinutes = cookingTimeMinutes,
    imageUrl = imageUrl.toServerUrlOrNull(),
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
    title = title.normalizeText(fallback = "Без названия"),
    description = description.normalizeText(fallback = "Без описания"),
    category = category.normalizeText(fallback = "Без категории"),
    cookingTimeMinutes = cookingTimeMinutes,
    ingredients = ingredients.map { it.normalizeText(fallback = "—") },
    steps = steps.map { it.normalizeText(fallback = "—") },
    imageUrls = imageUrls.mapNotNull { it.toServerUrlOrNull() },
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
    text = text.normalizeText(fallback = ""),
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

fun NewsItemDto.toDomain(): CulinaryNews = CulinaryNews(
    title = title.normalizeText(fallback = "Без названия"),
    summary = summary.normalizeText(fallback = ""),
    url = url,
    imageUrl = imageUrl.toServerUrlOrNull(),
    publishedAt = publishedAt,
    source = source.normalizeText(fallback = "Источник"),
)

private fun String.normalizeText(fallback: String): String {
    val repaired = repairCommonMojibake().trim()
    if (repaired.isBlank()) return fallback
    val questionMarks = repaired.count { it == '?' }
    if (repaired.length >= 3 && questionMarks * 2 >= repaired.length) {
        return fallback
    }
    return repaired
}

private fun String.repairCommonMojibake(): String {
    if (isBlank()) return this
    val cp1251 = runCatching {
        String(toByteArray(Charset.forName("windows-1251")), Charsets.UTF_8)
    }.getOrDefault(this)
    if (isClearlyBetter(candidate = cp1251, original = this)) {
        return cp1251
    }
    val iso = runCatching {
        String(toByteArray(StandardCharsets.ISO_8859_1), Charsets.UTF_8)
    }.getOrDefault(this)
    return if (isClearlyBetter(candidate = iso, original = this)) iso else this
}

private fun isClearlyBetter(candidate: String, original: String): Boolean {
    val candidateScore = candidate.count { it in '\u0400'..'\u04FF' } - candidate.count { it == '�' }
    val originalScore = original.count { it in '\u0400'..'\u04FF' } - original.count { it == '�' }
    return candidateScore > originalScore + 2
}

private fun String?.toServerUrlOrNull(): String? {
    val value = this?.trim().orEmpty()
    if (value.isBlank()) return null
    if (
        value.startsWith("http://", ignoreCase = true) ||
        value.startsWith("https://", ignoreCase = true) ||
        value.startsWith("content://", ignoreCase = true) ||
        value.startsWith("file://", ignoreCase = true)
    ) {
        return value
    }
    val base = BuildConfig.BASE_URL.trimEnd('/')
    val path = if (value.startsWith("/")) value else "/$value"
    return "$base$path"
}
