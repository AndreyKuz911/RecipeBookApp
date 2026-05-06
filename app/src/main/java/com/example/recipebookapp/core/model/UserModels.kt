package com.example.recipebookapp.core.model

data class UserSummary(
    val id: String,
    val username: String,
    val avatarUrl: String? = null,
)

data class UserProfile(
    val id: String,
    val email: String,
    val username: String,
    val bio: String?,
    val avatarUrl: String?,
    val createdAt: String,
    val recipesCount: Int,
    val followersCount: Int,
    val followingCount: Int,
    val isFollowing: Boolean = false,
)
