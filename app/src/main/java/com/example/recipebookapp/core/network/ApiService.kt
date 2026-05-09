package com.example.recipebookapp.core.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import okhttp3.MultipartBody

interface ApiService {
    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequestDto): AuthResponseDto

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequestDto): AuthResponseDto

    @GET("recipes")
    suspend fun getRecipes(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("query") query: String? = null,
        @Query("category") category: String? = null,
        @Query("timeRange") timeRange: String? = null,
        @Query("sort") sort: String? = null,
    ): PagedRecipesResponseDto

    @GET("recipes/{id}")
    suspend fun getRecipe(@Path("id") recipeId: String): RecipeDetailsDto

    @POST("recipes")
    suspend fun createRecipe(@Body body: RecipeUpsertRequestDto): RecipeDetailsDto

    @PUT("recipes/{id}")
    suspend fun updateRecipe(@Path("id") recipeId: String, @Body body: RecipeUpsertRequestDto): RecipeDetailsDto

    @DELETE("recipes/{id}")
    suspend fun deleteRecipe(@Path("id") recipeId: String): Response<Unit>

    @POST("recipes/{id}/rating")
    suspend fun rateRecipe(@Path("id") recipeId: String, @Body body: RatingRequestDto): RecipeDetailsDto

    @DELETE("recipes/{id}/rating")
    suspend fun deleteRating(@Path("id") recipeId: String): Response<Unit>

    @GET("recipes/{id}/comments")
    suspend fun getComments(@Path("id") recipeId: String): List<CommentDto>

    @POST("recipes/{id}/comments")
    suspend fun createComment(@Path("id") recipeId: String, @Body body: CreateCommentRequestDto): CommentDto

    @POST("recipes/{id}/favorite")
    suspend fun addFavorite(@Path("id") recipeId: String): RecipeDetailsDto

    @DELETE("recipes/{id}/favorite")
    suspend fun removeFavorite(@Path("id") recipeId: String): Response<Unit>

    @GET("users/me")
    suspend fun getMe(): UserProfileDto

    @PUT("users/me")
    suspend fun updateMe(@Body body: UpdateProfileRequestDto): UserProfileDto

    @GET("users/{id}")
    suspend fun getUser(@Path("id") userId: String): UserProfileDto

    @GET("users/{id}/recipes")
    suspend fun getUserRecipes(@Path("id") userId: String): List<RecipeDto>

    @POST("users/{id}/follow")
    suspend fun follow(@Path("id") userId: String): Response<Unit>

    @DELETE("users/{id}/follow")
    suspend fun unfollow(@Path("id") userId: String): Response<Unit>

    @GET("favorites")
    suspend fun getFavorites(): List<RecipeDto>

    @GET("feed")
    suspend fun getFeed(): List<RecipeDto>

    @GET("news")
    suspend fun getNews(@Query("limit") limit: Int = 30): List<NewsItemDto>

    @Multipart
    @POST("media/upload")
    suspend fun uploadMedia(@Part file: MultipartBody.Part): UploadMediaResponseDto
}
