package com.example.recipebookapp.feature_profile.domain

import com.example.recipebookapp.core.model.UserProfile
import com.example.recipebookapp.feature_profile.domain.model.ProfileWithRecipes
import javax.inject.Inject

data class ProfileUseCases @Inject constructor(
    val getMyProfileWithRecipes: GetMyProfileWithRecipesUseCase,
    val getOtherProfileWithRecipes: GetOtherProfileWithRecipesUseCase,
    val updateProfile: UpdateProfileUseCase,
    val setFollowing: SetFollowingUseCase,
    val mergeUpdatedProfile: MergeUpdatedProfileUseCase,
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

class MergeUpdatedProfileUseCase @Inject constructor() {
    operator fun invoke(current: ProfileWithRecipes?, updatedProfile: UserProfile): ProfileWithRecipes =
        ProfileWithRecipes(
            profile = updatedProfile,
            recipes = current?.recipes.orEmpty(),
        )
}

fun profileUseCases(repository: ProfileRepository): ProfileUseCases = ProfileUseCases(
    getMyProfileWithRecipes = GetMyProfileWithRecipesUseCase(repository),
    getOtherProfileWithRecipes = GetOtherProfileWithRecipesUseCase(repository),
    updateProfile = UpdateProfileUseCase(repository),
    setFollowing = SetFollowingUseCase(repository),
    mergeUpdatedProfile = MergeUpdatedProfileUseCase(),
)
