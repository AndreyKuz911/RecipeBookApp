package com.example.recipebookapp.core.model

data class CulinaryNews(
    val title: String,
    val summary: String,
    val url: String,
    val imageUrl: String? = null,
    val publishedAt: String,
    val source: String,
)
