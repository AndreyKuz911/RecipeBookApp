package com.example.recipebookapp.feature_feed.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.recipebookapp.core.presentation.AsyncState
import com.example.recipebookapp.core.ui.EmptyState
import com.example.recipebookapp.core.ui.ErrorState
import com.example.recipebookapp.core.ui.LoadingState
import com.example.recipebookapp.core.ui.RecipeList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    viewModel: FeedViewModel,
    onRecipeClick: (com.example.recipebookapp.core.model.Recipe) -> Unit,
    onAuthorClick: (String) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    Scaffold(topBar = { CenterAlignedTopAppBar(title = { Text("Новости") }) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            when (val uiState = state) {
                AsyncState.Empty -> EmptyState("Пусто")
                is AsyncState.Error -> ErrorState(uiState.message, viewModel::refresh)
                AsyncState.Loading -> LoadingState()
                is AsyncState.Success -> RecipeList(uiState.data, onRecipeClick, onAuthorClick)
            }
        }
    }
}
