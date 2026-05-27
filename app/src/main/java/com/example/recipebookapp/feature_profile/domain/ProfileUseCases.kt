package com.example.recipebookapp.feature_profile.domain

import javax.inject.Inject

data class ProfileUseCases @Inject constructor(
    val getMyProfileWithRecipes: GetMyProfileWithRecipesUseCase,
    val getOtherProfileWithRecipes: GetOtherProfileWithRecipesUseCase,
    val updateProfile: UpdateProfileUseCase,
    val setFollowing: SetFollowingUseCase,
)

class GetMyProfileWithRecipesUseCase @Inject constructor(
    private val repository: ProfileRepository,
) {
    suspend operator fun invoke() = repository.getMyProfileWithRecipes()
}

class GetOtherProfileWithRecipesUseCase @Inject constructor(
    private val repository: ProfileRepository,
) {
    suspend operator fun invoke(userId: String) = repository.getOtherProfileWithRecipes(userId)
}

class UpdateProfileUseCase @Inject constructor(
    private val repository: ProfileRepository,
) {
    suspend operator fun invoke(username: String, bio: String, avatarUrl: String) =
        repository.updateProfile(username, bio, avatarUrl)
}

class SetFollowingUseCase @Inject constructor(
    private val repository: ProfileRepository,
) {
    suspend operator fun invoke(userId: String, shouldFollow: Boolean) =
        repository.setFollowing(userId, shouldFollow)
}

fun profileUseCases(repository: ProfileRepository): ProfileUseCases = ProfileUseCases(
    getMyProfileWithRecipes = GetMyProfileWithRecipesUseCase(repository),
    getOtherProfileWithRecipes = GetOtherProfileWithRecipesUseCase(repository),
    updateProfile = UpdateProfileUseCase(repository),
    setFollowing = SetFollowingUseCase(repository),
)
