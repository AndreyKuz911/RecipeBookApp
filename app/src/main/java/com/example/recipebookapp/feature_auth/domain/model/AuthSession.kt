package com.example.recipebookapp.feature_auth.domain.model

import com.example.recipebookapp.core.model.UserProfile

data class AuthSession(
    val token: String,
    val user: UserProfile,
)
