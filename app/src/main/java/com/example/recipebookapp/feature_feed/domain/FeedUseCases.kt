package com.example.recipebookapp.feature_feed.domain

import javax.inject.Inject

data class FeedUseCases @Inject constructor(
    val getCulinaryNews: GetCulinaryNewsUseCase,
    val loadFeed: LoadFeedUseCase,
)

class GetCulinaryNewsUseCase @Inject constructor(
    private val repository: FeedRepository,
) {
    suspend operator fun invoke(limit: Int = 30) = repository.getCulinaryNews(limit)
}

class LoadFeedUseCase @Inject constructor(
    private val getCulinaryNews: GetCulinaryNewsUseCase,
) {
    suspend operator fun invoke(limit: Int = 30) = getCulinaryNews(limit)
}

fun feedUseCases(repository: FeedRepository): FeedUseCases = FeedUseCases(
    getCulinaryNews = GetCulinaryNewsUseCase(repository),
    loadFeed = LoadFeedUseCase(
        getCulinaryNews = GetCulinaryNewsUseCase(repository),
    ),
)
