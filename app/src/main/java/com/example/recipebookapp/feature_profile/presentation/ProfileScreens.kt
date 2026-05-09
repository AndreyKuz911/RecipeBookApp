package com.example.recipebookapp.feature_profile.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.recipebookapp.core.presentation.AsyncState
import com.example.recipebookapp.core.ui.AppTextField
import com.example.recipebookapp.core.ui.EmptyState
import com.example.recipebookapp.core.ui.ErrorState
import com.example.recipebookapp.core.ui.LoadingState
import com.example.recipebookapp.core.ui.PrimaryWideButton
import com.example.recipebookapp.core.ui.RecipeCard
import com.example.recipebookapp.core.ui.SecondaryWideButton
import com.example.recipebookapp.feature_profile.domain.model.ProfileWithRecipes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onRecipeClick: (String) -> Unit,
    onAuthorClick: (String) -> Unit,
    onCreateRecipe: () -> Unit,
    onLoggedOut: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val pickAvatarLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) viewModel.updateAvatarUrl(uri.toString())
        },
    )

    LaunchedEffect(state.profileState) {
        val errorMessage = (state.profileState as? AsyncState.Error)?.message.orEmpty()
        val shouldLogout = errorMessage.contains("401") ||
            errorMessage.contains("User not found", ignoreCase = true) ||
            errorMessage.contains("Session is no longer valid", ignoreCase = true)
        if (shouldLogout) {
            viewModel.logout()
            onLoggedOut()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Профиль") },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.logout()
                            onLoggedOut()
                        },
                    ) {
                        Text("Выйти")
                    }
                },
            )
        },
    ) { padding ->
        ProfileContent(
            padding = padding,
            state = state.profileState,
            onRetry = viewModel::refresh,
            onRecipeClick = onRecipeClick,
            onAuthorClick = onAuthorClick,
            editSection = {
                Card(shape = RoundedCornerShape(20.dp)) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text("Редактирование", style = MaterialTheme.typography.titleMedium)
                        AppTextField(state.editUsername, viewModel::updateUsername, "Имя пользователя")
                        AppTextField(state.editBio, viewModel::updateBio, "О себе")
                        AppTextField(state.editAvatarUrl, viewModel::updateAvatarUrl, "Ссылка на аватар")

                        if (state.editAvatarUrl.isNotBlank()) {
                            AsyncImage(
                                model = state.editAvatarUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxWidth(),
                                contentScale = ContentScale.Crop,
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                modifier = Modifier.weight(1f),
                                onClick = viewModel::saveProfile,
                                shape = RoundedCornerShape(14.dp),
                            ) { Text("Сохранить") }
                            Button(
                                modifier = Modifier.weight(1f),
                                onClick = { pickAvatarLauncher.launch("image/*") },
                                shape = RoundedCornerShape(14.dp),
                            ) { Text("Выбрать фото") }
                        }
                        SecondaryWideButton(
                            text = "Удалить аватар",
                            onClick = { viewModel.updateAvatarUrl("") },
                        )
                        PrimaryWideButton(
                            text = "Создать рецепт",
                            onClick = onCreateRecipe,
                        )
                    }
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtherProfileScreen(
    viewModel: OtherProfileViewModel,
    onRecipeClick: (String) -> Unit,
    onAuthorClick: (String) -> Unit,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Профиль автора") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        ProfileContent(
            padding = padding,
            state = state.profileState,
            onRetry = viewModel::refresh,
            onRecipeClick = onRecipeClick,
            onAuthorClick = onAuthorClick,
            editSection = {
                PrimaryWideButton(
                    text = "Подписаться / Отписаться",
                    onClick = viewModel::toggleFollow,
                )
            },
        )
    }
}

@Composable
private fun ProfileContent(
    padding: PaddingValues,
    state: AsyncState<ProfileWithRecipes>,
    onRetry: () -> Unit,
    onRecipeClick: (String) -> Unit,
    onAuthorClick: (String) -> Unit,
    editSection: @Composable () -> Unit,
) {
    when (state) {
        AsyncState.Empty -> {
            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                EmptyState("Нет данных")
            }
        }
        is AsyncState.Error -> {
            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                ErrorState(state.message, onRetry)
            }
        }
        AsyncState.Loading -> {
            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                LoadingState()
            }
        }
        is AsyncState.Success -> {
            val profile = state.data.profile
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Card(shape = RoundedCornerShape(20.dp)) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(profile.username, style = MaterialTheme.typography.headlineSmall)
                            Text(
                                profile.bio.orEmpty().ifBlank { "Расскажите о себе в профиле" },
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                "Рецептов: ${profile.recipesCount} | Подписчики: ${profile.followersCount} | Подписок: ${profile.followingCount}",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
                item { editSection() }
                item {
                    Text("Рецепты", style = MaterialTheme.typography.titleMedium)
                }
                items(state.data.recipes) { recipe ->
                    RecipeCard(
                        recipe = recipe,
                        onClick = { onRecipeClick(recipe.id) },
                        onAuthorClick = { onAuthorClick(recipe.author.id) },
                    )
                }
            }
        }
    }
}
