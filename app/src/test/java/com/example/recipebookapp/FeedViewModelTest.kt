package com.example.recipebookapp

import com.example.recipebookapp.core.common.Resource
import com.example.recipebookapp.core.model.CulinaryNews
import com.example.recipebookapp.core.presentation.AsyncState
import com.example.recipebookapp.feature_feed.domain.FeedRepository
import com.example.recipebookapp.feature_feed.presentation.FeedViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FeedViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @Test
    fun `feed success shows news items`() = runTest {
        val viewModel = FeedViewModel(FakeFeedRepository())
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state is AsyncState.Success)
        assertEquals(1, (state as AsyncState.Success).data.size)
    }

    @Test
    fun `feed error shows error state`() = runTest {
        val viewModel = FeedViewModel(FakeFeedRepository(shouldFail = true))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state is AsyncState.Error)
        assertEquals("News unavailable", (state as AsyncState.Error).message)
    }
}

private class FakeFeedRepository(
    private val shouldFail: Boolean = false,
) : FeedRepository {
    override suspend fun getCulinaryNews(limit: Int): Resource<List<CulinaryNews>> {
        return if (shouldFail) {
            Resource.Error("News unavailable")
        } else {
            Resource.Success(
                listOf(
                    CulinaryNews(
                        title = "Fresh recipes",
                        summary = "Summary",
                        url = "https://example.com/news",
                        publishedAt = "2026-05-12T12:00:00",
                        source = "Test Source",
                    ),
                ),
            )
        }
    }
}
