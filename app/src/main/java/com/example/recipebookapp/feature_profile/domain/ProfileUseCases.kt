package com.example.recipebookapp.feature_profile.domain

import com.example.recipebookapp.core.model.UserProfile
import com.example.recipebookapp.core.common.Resource
import com.example.recipebookapp.feature_profile.domain.model.EditableProfileData
import com.example.recipebookapp.feature_profile.domain.model.ProfileEditorFields
import com.example.recipebookapp.feature_profile.domain.model.ProfileWithRecipes
import javax.inject.Inject

data class ProfileUseCases @Inject constructor(
    val getMyProfileWithRecipes: GetMyProfileWithRecipesUseCase,
    val getEditableMyProfile: GetEditableMyProfileUseCase,
    val getOtherProfileWithRecipes: GetOtherProfileWithRecipesUseCase,
    val updateProfile: UpdateProfileUseCase,
    val setFollowing: SetFollowingUseCase,
    val updateProfileSnapshot: UpdateProfileSnapshotUseCase,
    val toggleFollowingProfile: ToggleFollowingProfileUseCase,
    val buildProfileEditorFields: BuildProfileEditorFieldsUseCase,
    val mergeUpdatedProfile: MergeUpdatedProfileUseCase,
)

class GetMyProfileWithRecipesUseCase @Inject constructor(
    private val repository: ProfileRepository,
) {
    suspend operator fun invoke() = repository.getMyProfileWithRecipes()
}

class GetEditableMyProfileUseCase @Inject constructor(
    private val getMyProfileWithRecipes: GetMyProfileWithRecipesUseCase,
    private val buildProfileEditorFields: BuildProfileEditorFieldsUseCase,
) {
    suspend operator fun invoke(): Resource<EditableProfileData> = when (val result = getMyProfileWithRecipes()) {
        is Resource.Success -> Resource.Success(
            EditableProfileData(
                profile = result.data,
                editorFields = buildProfileEditorFields(result.data.profile),
            ),
        )
        is Resource.Error -> result
    }
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

class UpdateProfileSnapshotUseCase @Inject constructor(
    private val updateProfile: UpdateProfileUseCase,
    private val mergeUpdatedProfile: MergeUpdatedProfileUseCase,
) {
    suspend operator fun invoke(
        current: ProfileWithRecipes?,
        username: String,
        bio: String,
        avatarUrl: String,
    ) = when (val result = updateProfile(username, bio, avatarUrl)) {
        is com.example.recipebookapp.core.common.Resource.Success ->
            com.example.recipebookapp.core.common.Resource.Success(
                mergeUpdatedProfile(current, result.data),
            )
        is com.example.recipebookapp.core.common.Resource.Error -> result
    }
}

class ToggleFollowingProfileUseCase @Inject constructor(
    private val setFollowing: SetFollowingUseCase,
    private val getOtherProfileWithRecipes: GetOtherProfileWithRecipesUseCase,
) {
    suspend operator fun invoke(current: ProfileWithRecipes, userId: String) =
        when (val result = setFollowing(userId, !current.profile.isFollowing)) {
            is com.example.recipebookapp.core.common.Resource.Success -> getOtherProfileWithRecipes(userId)
            is com.example.recipebookapp.core.common.Resource.Error -> result
        }
}

class BuildProfileEditorFieldsUseCase @Inject constructor() {
    operator fun invoke(profile: UserProfile): ProfileEditorFields = ProfileEditorFields(
        username = profile.username,
        bio = profile.bio.orEmpty(),
        avatarUrl = profile.avatarUrl.orEmpty(),
    )
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
    getEditableMyProfile = GetEditableMyProfileUseCase(
        getMyProfileWithRecipes = GetMyProfileWithRecipesUseCase(repository),
        buildProfileEditorFields = BuildProfileEditorFieldsUseCase(),
    ),
    getOtherProfileWithRecipes = GetOtherProfileWithRecipesUseCase(repository),
    updateProfile = UpdateProfileUseCase(repository),
    setFollowing = SetFollowingUseCase(repository),
    updateProfileSnapshot = UpdateProfileSnapshotUseCase(
        updateProfile = UpdateProfileUseCase(repository),
        mergeUpdatedProfile = MergeUpdatedProfileUseCase(),
    ),
    toggleFollowingProfile = ToggleFollowingProfileUseCase(
        setFollowing = SetFollowingUseCase(repository),
        getOtherProfileWithRecipes = GetOtherProfileWithRecipesUseCase(repository),
    ),
    buildProfileEditorFields = BuildProfileEditorFieldsUseCase(),
    mergeUpdatedProfile = MergeUpdatedProfileUseCase(),
)
