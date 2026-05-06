package com.example.recipebookapp.feature_feed.domain

import com.example.recipebookapp.core.common.Resource
import com.example.recipebookapp.core.model.Recipe

interface FeedRepository {
    suspend fun getFeed(): Resource<List<Recipe>>
}
