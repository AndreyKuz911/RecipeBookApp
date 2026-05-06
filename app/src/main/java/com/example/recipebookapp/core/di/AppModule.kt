package com.example.recipebookapp.core.di

import android.content.Context
import androidx.room.Room
import com.example.recipebookapp.BuildConfig
import com.example.recipebookapp.core.database.ProfileDao
import com.example.recipebookapp.core.database.RecipeBookDatabase
import com.example.recipebookapp.core.database.RecipeDao
import com.example.recipebookapp.core.network.ApiService
import com.example.recipebookapp.core.network.AuthInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Provides
    @Singleton
    fun provideOkHttp(authInterceptor: AuthInterceptor): OkHttpClient {
        val logger = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logger)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(json: Json, okHttpClient: OkHttpClient): ApiService {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): RecipeBookDatabase {
        return Room.databaseBuilder(
            context,
            RecipeBookDatabase::class.java,
            "recipe_book.db",
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideRecipeDao(database: RecipeBookDatabase): RecipeDao = database.recipeDao()

    @Provides
    fun provideProfileDao(database: RecipeBookDatabase): ProfileDao = database.profileDao()
}
