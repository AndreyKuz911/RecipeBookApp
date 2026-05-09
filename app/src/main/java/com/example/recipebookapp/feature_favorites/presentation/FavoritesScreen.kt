package com.example.recipebookapp.feature_favorites.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.recipebookapp.core.model.Recipe
import com.example.recipebookapp.core.presentation.AsyncState
import com.example.recipebookapp.core.ui.EmptyState
import com.example.recipebookapp.core.ui.ErrorState
import com.example.recipebookapp.core.ui.LoadingState
import com.example.recipebookapp.core.ui.RecipeCard
import com.example.recipebookapp.core.ui.SecondaryWideButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel,
    onRecipeClick: (Recipe) -> Unit,
    onAuthorClick: (String) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(topBar = { CenterAlignedTopAppBar(title = { Text("Избранное") }) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(14.dp),
        ) {
            when (val uiState = state) {
                AsyncState.Empty -> EmptyState("Вы еще не добавили рецепты в избранное")
                is AsyncState.Error -> ErrorState(uiState.message, viewModel::refresh)
                AsyncState.Loading -> LoadingState()
                is AsyncState.Success -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(uiState.data) { recipe ->
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                RecipeCard(
                                    recipe = recipe,
                                    onClick = { onRecipeClick(recipe) },
                                    onAuthorClick = { onAuthorClick(recipe.author.id) },
                                )
                                SecondaryWideButton(
                                    text = "Убрать из избранного",
                                    onClick = { viewModel.removeFromFavorites(recipe.id) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
