package com.example.recipebookapp

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.recipebookapp.core.database.CachedRecipeEntity
import com.example.recipebookapp.core.database.FavoriteRecipeEntity
import com.example.recipebookapp.core.database.RecipeDao
import com.example.recipebookapp.core.datastore.SessionStorage
import com.example.recipebookapp.core.network.ApiService
import com.example.recipebookapp.core.network.CommentDto
import com.example.recipebookapp.core.network.CreateCommentRequestDto
import com.example.recipebookapp.core.network.LoginRequestDto
import com.example.recipebookapp.core.network.PagedRecipesResponseDto
import com.example.recipebookapp.core.network.RatingRequestDto
import com.example.recipebookapp.core.network.RecipeDetailsDto
import com.example.recipebookapp.core.network.RecipeDto
import com.example.recipebookapp.core.network.RecipeUpsertRequestDto
import com.example.recipebookapp.core.network.RegisterRequestDto
import com.example.recipebookapp.core.network.SafeApiCall
import com.example.recipebookapp.core.network.UpdateProfileRequestDto
import com.example.recipebookapp.core.network.UserProfileDto
import com.example.recipebookapp.core.network.UserSummaryDto
import com.example.recipebookapp.core.common.Resource
import com.example.recipebookapp.feature_recipes.data.RecipesRepositoryImpl
import com.example.recipebookapp.feature_recipes.domain.model.RecipeFilters
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.Response

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class RecipesRepositoryImplTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @Test
    fun `repository returns cached recipes when api fails`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = RecipesRepositoryImpl(
            apiService = FailingRecipesApi(),
            recipeDao = FakeRecipeDao(
                cached = mutableListOf(
                    CachedRecipeEntity(
                        id = "1",
                        title = "Кэшированный рецепт",
                        description = "Описание",
                        category = "Супы",
                        cookingTimeMinutes = 20,
                        imageUrl = null,
                        authorId = "author",
                        authorUsername = "chef",
                        authorAvatarUrl = null,
                        createdAt = "2026-05-03T00:00",
                        updatedAt = "2026-05-03T00:00",
                        likesCount = 1,
                        dislikesCount = 0,
                        rating = 1,
                        isFavorite = false,
                        myRating = null,
                    ),
                ),
            ),
            safeApiCall = SafeApiCall(SessionStorage(context)),
        )

        val result = repository.getRecipes(RecipeFilters())

        require(result is Resource.Success)
        assertEquals(1, result.data.items.size)
        assertEquals("Кэшированный рецепт", result.data.items.first().title)
    }
}

private class FailingRecipesApi : ApiService {
    override suspend fun register(body: RegisterRequestDto) = error("Not used")
    override suspend fun login(body: LoginRequestDto) = error("Not used")
    override suspend fun getRecipes(page: Int, limit: Int, query: String?, category: String?, timeRange: String?, sort: String?) =
        throw java.io.IOException("Offline")
    override suspend fun getRecipe(recipeId: String) = error("Not used")
    override suspend fun createRecipe(body: RecipeUpsertRequestDto) = error("Not used")
    override suspend fun updateRecipe(recipeId: String, body: RecipeUpsertRequestDto) = error("Not used")
    override suspend fun deleteRecipe(recipeId: String): Response<Unit> = error("Not used")
    override suspend fun rateRecipe(recipeId: String, body: RatingRequestDto) = error("Not used")
    override suspend fun deleteRating(recipeId: String): Response<Unit> = error("Not used")
    override suspend fun getComments(recipeId: String): List<CommentDto> = error("Not used")
    override suspend fun createComment(recipeId: String, body: CreateCommentRequestDto): CommentDto = error("Not used")
    override suspend fun addFavorite(recipeId: String): RecipeDetailsDto = error("Not used")
    override suspend fun removeFavorite(recipeId: String): Response<Unit> = error("Not used")
    override suspend fun getMe(): UserProfileDto = error("Not used")
    override suspend fun updateMe(body: UpdateProfileRequestDto): UserProfileDto = error("Not used")
    override suspend fun getUser(userId: String): UserProfileDto = error("Not used")
    override suspend fun getUserRecipes(userId: String): List<RecipeDto> = error("Not used")
    override suspend fun follow(userId: String): Response<Unit> = error("Not used")
    override suspend fun unfollow(userId: String): Response<Unit> = error("Not used")
    override suspend fun getFavorites(): List<RecipeDto> = error("Not used")
    override suspend fun getFeed(): List<RecipeDto> = error("Not used")
}

private class FakeRecipeDao(
    private val cached: MutableList<CachedRecipeEntity> = mutableListOf(),
    private val favorites: MutableList<FavoriteRecipeEntity> = mutableListOf(),
) : RecipeDao {
    override suspend fun getCachedRecipes(): List<CachedRecipeEntity> = cached.toList()
    override suspend fun insertCachedRecipes(items: List<CachedRecipeEntity>) {
        cached.clear()
        cached.addAll(items)
    }
    override suspend fun clearCachedRecipes() { cached.clear() }
    override suspend fun getFavoriteRecipes(): List<FavoriteRecipeEntity> = favorites.toList()
    override suspend fun insertFavoriteRecipes(items: List<FavoriteRecipeEntity>) {
        favorites.clear()
        favorites.addAll(items)
    }
    override suspend fun clearFavoriteRecipes() { favorites.clear() }
}
