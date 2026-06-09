package com.example.recipebookapp.feature_profile.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipebookapp.core.common.RecipeSyncNotifier
import com.example.recipebookapp.core.common.Resource
import com.example.recipebookapp.core.presentation.AsyncState
import com.example.recipebookapp.core.presentation.toAsyncState
import com.example.recipebookapp.feature_auth.domain.LogoutUseCase
import com.example.recipebookapp.feature_profile.domain.ProfileRepository
import com.example.recipebookapp.feature_profile.domain.ProfileUseCases
import com.example.recipebookapp.feature_profile.domain.model.EditableProfileData
import com.example.recipebookapp.feature_profile.domain.model.ProfileWithRecipes
import com.example.recipebookapp.feature_profile.domain.profileUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val profileState: AsyncState<ProfileWithRecipes> = AsyncState.Loading,
    val editUsername: String = "",
    val editBio: String = "",
    val editAvatarUrl: String = "",
    val isSaving: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileUseCases: ProfileUseCases,
    private val logoutUseCase: LogoutUseCase,
    private val recipeSyncNotifier: RecipeSyncNotifier,
) : ViewModel() {
    constructor(
        repository: ProfileRepository,
        logoutUseCase: LogoutUseCase,
        recipeSyncNotifier: RecipeSyncNotifier = RecipeSyncNotifier(),
    ) : this(
        profileUseCases = profileUseCases(repository),
        logoutUseCase = logoutUseCase,
        recipeSyncNotifier = recipeSyncNotifier,
    )

    private val _state = MutableStateFlow(ProfileUiState())
    val state = _state.asStateFlow()

    init {
        refresh()
        viewModelScope.launch {
            recipeSyncNotifier.recipeMutations.collect {
                refresh()
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(profileState = AsyncState.Loading)
            when (val state = profileUseCases.getEditableMyProfile().toAsyncState()) {
                is AsyncState.Success -> applyEditableProfile(state.data)
                is AsyncState.Error -> _state.value = _state.value.copy(profileState = state)
                AsyncState.Empty -> _state.value = _state.value.copy(profileState = AsyncState.Empty)
                AsyncState.Loading -> Unit
            }
        }
    }

    private fun applyEditableProfile(data: EditableProfileData) {
        val fields = data.editorFields
        _state.value = _state.value.copy(
            profileState = AsyncState.Success(data.profile),
            editUsername = fields.username,
            editBio = fields.bio,
            editAvatarUrl = fields.avatarUrl,
        )
    }

    fun updateUsername(value: String) { _state.value = _state.value.copy(editUsername = value, error = null) }
    fun updateBio(value: String) { _state.value = _state.value.copy(editBio = value, error = null) }
    fun updateAvatarUrl(value: String) { _state.value = _state.value.copy(editAvatarUrl = value, error = null) }

    fun saveProfile() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true, error = null)
            val current = (_state.value.profileState as? AsyncState.Success)?.data
            when (val result = profileUseCases.updateProfileSnapshot(current, _state.value.editUsername, _state.value.editBio, _state.value.editAvatarUrl)) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        isSaving = false,
                        profileState = AsyncState.Success(result.data),
                        error = null,
                    )
                }
                is Resource.Error -> _state.value = _state.value.copy(isSaving = false, error = result.message)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
        }
    }
}

@HiltViewModel
class OtherProfileViewModel @Inject constructor(
    private val profileUseCases: ProfileUseCases,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    constructor(repository: ProfileRepository, savedStateHandle: SavedStateHandle) : this(
        profileUseCases = profileUseCases(repository),
        savedStateHandle = savedStateHandle,
    )

    private val userId: String = checkNotNull(savedStateHandle["userId"])
    private val _state = MutableStateFlow(ProfileUiState())
    val state = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(profileState = AsyncState.Loading)
            _state.value = _state.value.copy(
                profileState = profileUseCases.getOtherProfileWithRecipes(userId).toAsyncState(),
            )
        }
    }

    fun toggleFollow() {
        val data = (_state.value.profileState as? AsyncState.Success)?.data ?: return
        viewModelScope.launch {
            when (val result = profileUseCases.toggleFollowingProfile(data, userId)) {
                is Resource.Success -> _state.value = _state.value.copy(
                    profileState = AsyncState.Success(result.data),
                    error = null,
                )
                is Resource.Error -> _state.value = _state.value.copy(error = result.message)
            }
        }
    }
}
