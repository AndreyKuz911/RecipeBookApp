package com.example.recipebookapp.feature_profile.data

import com.example.recipebookapp.core.network.ApiService
import com.example.recipebookapp.core.network.MediaUploader
import com.example.recipebookapp.core.network.SafeApiCall
import com.example.recipebookapp.core.network.UpdateProfileRequestDto
import com.example.recipebookapp.core.network.toDomain
import com.example.recipebookapp.core.common.Resource
import com.example.recipebookapp.core.model.UserProfile
import com.example.recipebookapp.feature_profile.domain.ProfileRepository
import com.example.recipebookapp.feature_profile.domain.model.ProfileWithRecipes
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val safeApiCall: SafeApiCall,
    private val mediaUploader: MediaUploader,
) : ProfileRepository {
    override suspend fun getMyProfileWithRecipes(): Resource<ProfileWithRecipes> {
        return when (val result = safeApiCall.execute { apiService.getMe().toDomain() }) {
            is Resource.Success -> {
                val recipesResult = safeApiCall.execute {
                    apiService.getUserRecipes(result.data.id).map { it.toDomain() }
                }
                when (recipesResult) {
                    is Resource.Success -> Resource.Success(ProfileWithRecipes(result.data, recipesResult.data))
                    is Resource.Error -> Resource.Success(ProfileWithRecipes(result.data, emptyList()))
                }
            }
            is Resource.Error -> result
        }
    }

    override suspend fun getOtherProfileWithRecipes(userId: String): Resource<ProfileWithRecipes> {
        return when (val result = safeApiCall.execute { apiService.getUser(userId).toDomain() }) {
            is Resource.Success -> {
                val recipesResult = safeApiCall.execute { apiService.getUserRecipes(userId).map { it.toDomain() } }
                when (recipesResult) {
                    is Resource.Success -> Resource.Success(ProfileWithRecipes(result.data, recipesResult.data))
                    is Resource.Error -> Resource.Success(ProfileWithRecipes(result.data, emptyList()))
                }
            }
            is Resource.Error -> result
        }
    }

    override suspend fun updateProfile(username: String, bio: String, avatarUrl: String): Resource<UserProfile> {
        return safeApiCall.execute {
            val resolvedAvatarUrl = if (mediaUploader.isLocalUri(avatarUrl)) {
                mediaUploader.uploadFromUri(avatarUrl)
            } else {
                avatarUrl.ifBlank { "" }
            }
            apiService.updateMe(
                UpdateProfileRequestDto(
                    username = username.trim(),
                    bio = bio.ifBlank { null },
                    avatarUrl = resolvedAvatarUrl.ifBlank { null },
                ),
            ).toDomain()
        }
    }

    override suspend fun setFollowing(userId: String, shouldFollow: Boolean): Resource<Unit> {
        return safeApiCall.execute {
            if (shouldFollow) {
                apiService.follow(userId)
            } else {
                apiService.unfollow(userId)
            }
            Unit
        }
    }
}
