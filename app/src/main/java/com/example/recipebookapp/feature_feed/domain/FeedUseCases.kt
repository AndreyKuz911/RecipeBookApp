package com.example.recipebookapp.feature_feed.domain

import javax.inject.Inject

data class FeedUseCases @Inject constructor(
    val getCulinaryNews: GetCulinaryNewsUseCase,
)

class GetCulinaryNewsUseCase @Inject constructor(
    private val repository: FeedRepository,
) {
    suspend operator fun invoke(limit: Int = 30) = repository.getCulinaryNews(limit)
}

fun feedUseCases(repository: FeedRepository): FeedUseCases = FeedUseCases(
    getCulinaryNews = GetCulinaryNewsUseCase(repository),
)
