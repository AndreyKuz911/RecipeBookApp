package com.example.recipebookapp.feature_feed.domain

import com.example.recipebookapp.core.common.Resource
import com.example.recipebookapp.core.model.CulinaryNews

interface FeedRepository {
    suspend fun getCulinaryNews(limit: Int = 30): Resource<List<CulinaryNews>>
}
