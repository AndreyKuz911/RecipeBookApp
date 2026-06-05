package com.example.recipebookapp.feature_feed.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipebookapp.core.common.Resource
import com.example.recipebookapp.core.model.CulinaryNews
import com.example.recipebookapp.core.presentation.AsyncState
import com.example.recipebookapp.feature_feed.domain.FeedRepository
import com.example.recipebookapp.feature_feed.domain.FeedUseCases
import com.example.recipebookapp.feature_feed.domain.feedUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val feedUseCases: FeedUseCases,
) : ViewModel() {
    constructor(repository: FeedRepository) : this(feedUseCases(repository))

    private val _state = MutableStateFlow<AsyncState<List<CulinaryNews>>>(AsyncState.Loading)
    val state = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = AsyncState.Loading
            when (val result = feedUseCases.loadFeed()) {
                is Resource.Success -> _state.value = if (result.data.isEmpty()) AsyncState.Empty else AsyncState.Success(result.data)
                is Resource.Error -> _state.value = AsyncState.Error(result.message)
            }
        }
    }
}
