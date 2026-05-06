package com.example.recipebookapp.feature_profile.domain

import com.example.recipebookapp.core.common.Resource
import com.example.recipebookapp.core.model.UserProfile
import com.example.recipebookapp.feature_profile.domain.model.ProfileWithRecipes

interface ProfileRepository {
    suspend fun getMyProfileWithRecipes(): Resource<ProfileWithRecipes>

    suspend fun getOtherProfileWithRecipes(userId: String): Resource<ProfileWithRecipes>

    suspend fun updateProfile(username: String, bio: String, avatarUrl: String): Resource<UserProfile>

    suspend fun setFollowing(userId: String, shouldFollow: Boolean): Resource<Unit>
}
