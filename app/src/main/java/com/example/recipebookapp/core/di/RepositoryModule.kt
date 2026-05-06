package com.example.recipebookapp.core.di

import com.example.recipebookapp.feature_auth.data.AuthRepositoryImpl
import com.example.recipebookapp.feature_auth.domain.AuthRepository
import com.example.recipebookapp.feature_favorites.data.FavoritesRepositoryImpl
import com.example.recipebookapp.feature_favorites.domain.FavoritesRepository
import com.example.recipebookapp.feature_feed.data.FeedRepositoryImpl
import com.example.recipebookapp.feature_feed.domain.FeedRepository
import com.example.recipebookapp.feature_profile.data.ProfileRepositoryImpl
import com.example.recipebookapp.feature_profile.domain.ProfileRepository
import com.example.recipebookapp.feature_recipes.data.RecipesRepositoryImpl
import com.example.recipebookapp.feature_recipes.domain.RecipesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindRecipesRepository(impl: RecipesRepositoryImpl): RecipesRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindFavoritesRepository(impl: FavoritesRepositoryImpl): FavoritesRepository

    @Binds
    @Singleton
    abstract fun bindFeedRepository(impl: FeedRepositoryImpl): FeedRepository
}
