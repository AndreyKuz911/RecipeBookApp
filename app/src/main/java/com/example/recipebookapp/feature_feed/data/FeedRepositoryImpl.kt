package com.example.recipebookapp.feature_feed.data

import com.example.recipebookapp.core.network.ApiService
import com.example.recipebookapp.core.network.SafeApiCall
import com.example.recipebookapp.core.network.toDomain
import com.example.recipebookapp.core.common.Resource
import com.example.recipebookapp.core.model.CulinaryNews
import com.example.recipebookapp.feature_feed.domain.FeedRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val safeApiCall: SafeApiCall,
) : FeedRepository {
    override suspend fun getCulinaryNews(limit: Int): Resource<List<CulinaryNews>> =
        safeApiCall.executeWithRetry(maxAttempts = 2) {
            apiService.getNews(limit = limit).map { it.toDomain() }
        }
}
