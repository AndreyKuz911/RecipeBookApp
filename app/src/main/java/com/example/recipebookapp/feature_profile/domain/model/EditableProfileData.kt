package com.example.recipebookapp.feature_profile.domain.model

data class EditableProfileData(
    val profile: ProfileWithRecipes,
    val editorFields: ProfileEditorFields,
)
