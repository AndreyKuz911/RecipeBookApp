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
    buildProfileEditorFields = BuildProfileEditorFieldsUseCase(),
    mergeUpdatedProfile = MergeUpdatedProfileUseCase(),
)
