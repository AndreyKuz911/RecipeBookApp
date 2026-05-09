package com.example.recipebookapp

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.recipebookapp.core.common.Resource
import com.example.recipebookapp.core.datastore.SessionStorage
import com.example.recipebookapp.core.network.ApiService
import com.example.recipebookapp.core.network.CommentDto
import com.example.recipebookapp.core.network.CreateCommentRequestDto
import com.example.recipebookapp.core.network.LoginRequestDto
import com.example.recipebookapp.core.network.MediaUploader
import com.example.recipebookapp.core.network.NewsItemDto
import com.example.recipebookapp.core.network.PagedRecipesResponseDto
import com.example.recipebookapp.core.network.RatingRequestDto
import com.example.recipebookapp.core.network.RecipeDetailsDto
import com.example.recipebookapp.core.network.RecipeDto
import com.example.recipebookapp.core.network.RecipeUpsertRequestDto
import com.example.recipebookapp.core.network.RegisterRequestDto
import com.example.recipebookapp.core.network.SafeApiCall
import com.example.recipebookapp.core.network.UpdateProfileRequestDto
import com.example.recipebookapp.core.network.UploadMediaResponseDto
import com.example.recipebookapp.core.network.UserProfileDto
import com.example.recipebookapp.feature_recipes.data.RecipesRepositoryImpl
import com.example.recipebookapp.feature_recipes.domain.model.RecipeFilters
import kotlinx.coroutines.test.runTest
import okhttp3.MultipartBody
import org.junit.Assert.assertTrue
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
    fun `repository returns error when api fails`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val api = FailingRecipesApi()
        val repository = RecipesRepositoryImpl(
            apiService = api,
            safeApiCall = SafeApiCall(SessionStorage(context)),
            mediaUploader = MediaUploader(context, api),
        )

        val result = repository.getRecipes(RecipeFilters())

        assertTrue(result is Resource.Error)
    }
}

private class FailingRecipesApi : ApiService {
    override suspend fun register(body: RegisterRequestDto) = error("Not used")
    override suspend fun login(body: LoginRequestDto) = error("Not used")
    override suspend fun getRecipes(page: Int, limit: Int, query: String?, category: String?, timeRange: String?, sort: String?): PagedRecipesResponseDto =
        throw java.io.IOException("Offline")
    override suspend fun getRecipe(recipeId: String): RecipeDetailsDto = error("Not used")
    override suspend fun createRecipe(body: RecipeUpsertRequestDto): RecipeDetailsDto = error("Not used")
    override suspend fun updateRecipe(recipeId: String, body: RecipeUpsertRequestDto): RecipeDetailsDto = error("Not used")
    override suspend fun deleteRecipe(recipeId: String): Response<Unit> = error("Not used")
    override suspend fun rateRecipe(recipeId: String, body: RatingRequestDto): RecipeDetailsDto = error("Not used")
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
    override suspend fun getNews(limit: Int): List<NewsItemDto> = error("Not used")
    override suspend fun uploadMedia(file: MultipartBody.Part): UploadMediaResponseDto = error("Not used")
}
