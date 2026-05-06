package com.example.recipebookapp.core.database

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import com.example.recipebookapp.core.model.Recipe
import com.example.recipebookapp.core.model.UserProfile
import com.example.recipebookapp.core.model.UserSummary

@Entity(tableName = "cached_recipes")
data class CachedRecipeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val category: String,
    val cookingTimeMinutes: Int,
    val imageUrl: String?,
    val authorId: String,
    val authorUsername: String,
    val authorAvatarUrl: String?,
    val createdAt: String,
    val updatedAt: String,
    val likesCount: Int,
    val dislikesCount: Int,
    val rating: Int,
    val isFavorite: Boolean,
    val myRating: Int?,
)

@Entity(tableName = "favorite_recipes")
data class FavoriteRecipeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val category: String,
    val cookingTimeMinutes: Int,
    val imageUrl: String?,
    val authorId: String,
    val authorUsername: String,
    val authorAvatarUrl: String?,
    val createdAt: String,
    val updatedAt: String,
    val likesCount: Int,
    val dislikesCount: Int,
    val rating: Int,
    val isFavorite: Boolean,
    val myRating: Int?,
)

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val id: String,
    val email: String,
    val username: String,
    val bio: String?,
    val avatarUrl: String?,
    val createdAt: String,
    val recipesCount: Int,
    val followersCount: Int,
    val followingCount: Int,
    val isFollowing: Boolean,
)

@Dao
interface RecipeDao {
    @Query("SELECT * FROM cached_recipes ORDER BY createdAt DESC")
    suspend fun getCachedRecipes(): List<CachedRecipeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedRecipes(items: List<CachedRecipeEntity>)

    @Query("DELETE FROM cached_recipes")
    suspend fun clearCachedRecipes()

    @Query("SELECT * FROM favorite_recipes ORDER BY createdAt DESC")
    suspend fun getFavoriteRecipes(): List<FavoriteRecipeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteRecipes(items: List<FavoriteRecipeEntity>)

    @Query("DELETE FROM favorite_recipes")
    suspend fun clearFavoriteRecipes()
}

@Dao
interface ProfileDao {
    @Query("SELECT * FROM user_profiles WHERE id = :userId LIMIT 1")
    suspend fun getProfile(userId: String): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(entity: UserProfileEntity)
}

@Database(
    entities = [CachedRecipeEntity::class, FavoriteRecipeEntity::class, UserProfileEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class RecipeBookDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun profileDao(): ProfileDao
}

fun Recipe.toCachedEntity(): CachedRecipeEntity = CachedRecipeEntity(
    id = id,
    title = title,
    description = description,
    category = category,
    cookingTimeMinutes = cookingTimeMinutes,
    imageUrl = imageUrl,
    authorId = author.id,
    authorUsername = author.username,
    authorAvatarUrl = author.avatarUrl,
    createdAt = createdAt,
    updatedAt = updatedAt,
    likesCount = likesCount,
    dislikesCount = dislikesCount,
    rating = rating,
    isFavorite = isFavorite,
    myRating = myRating,
)

fun Recipe.toFavoriteEntity(): FavoriteRecipeEntity = FavoriteRecipeEntity(
    id = id,
    title = title,
    description = description,
    category = category,
    cookingTimeMinutes = cookingTimeMinutes,
    imageUrl = imageUrl,
    authorId = author.id,
    authorUsername = author.username,
    authorAvatarUrl = author.avatarUrl,
    createdAt = createdAt,
    updatedAt = updatedAt,
    likesCount = likesCount,
    dislikesCount = dislikesCount,
    rating = rating,
    isFavorite = isFavorite,
    myRating = myRating,
)

fun CachedRecipeEntity.toDomain(): Recipe = Recipe(
    id = id,
    title = title,
    description = description,
    category = category,
    cookingTimeMinutes = cookingTimeMinutes,
    imageUrl = imageUrl,
    author = UserSummary(authorId, authorUsername, authorAvatarUrl),
    createdAt = createdAt,
    updatedAt = updatedAt,
    likesCount = likesCount,
    dislikesCount = dislikesCount,
    rating = rating,
    isFavorite = isFavorite,
    myRating = myRating,
)

fun FavoriteRecipeEntity.toDomain(): Recipe = Recipe(
    id = id,
    title = title,
    description = description,
    category = category,
    cookingTimeMinutes = cookingTimeMinutes,
    imageUrl = imageUrl,
    author = UserSummary(authorId, authorUsername, authorAvatarUrl),
    createdAt = createdAt,
    updatedAt = updatedAt,
    likesCount = likesCount,
    dislikesCount = dislikesCount,
    rating = rating,
    isFavorite = isFavorite,
    myRating = myRating,
)

fun UserProfile.toEntity(): UserProfileEntity = UserProfileEntity(
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

fun UserProfileEntity.toDomain(): UserProfile = UserProfile(
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
