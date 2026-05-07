package com.example.recipebookapp.feature_recipes.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.recipebookapp.core.model.Recipe
import com.example.recipebookapp.core.presentation.AsyncState
import com.example.recipebookapp.core.ui.AppTextField
import com.example.recipebookapp.core.ui.EmptyState
import com.example.recipebookapp.core.ui.ErrorState
import com.example.recipebookapp.core.ui.LoadingState
import com.example.recipebookapp.core.ui.RecipeList
import com.example.recipebookapp.feature_recipes.domain.model.RecipeCatalog
import com.example.recipebookapp.feature_recipes.domain.model.RecipeFilters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: AsyncState<List<Recipe>>,
    onRetry: () -> Unit,
    onRecipeClick: (Recipe) -> Unit,
    onAuthorClick: (String) -> Unit,
) {
    Scaffold(topBar = { CenterAlignedTopAppBar(title = { Text("Новые рецепты") }) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            when (state) {
                AsyncState.Empty -> EmptyState("Пока рецептов нет")
                is AsyncState.Error -> ErrorState(state.message, onRetry)
                AsyncState.Loading -> LoadingState()
                is AsyncState.Success -> RecipeList(state.data, onRecipeClick, onAuthorClick)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    state: RecipeListUiState,
    onFiltersChange: (RecipeFilters) -> Unit,
    onSearch: () -> Unit,
    onReset: () -> Unit,
    onRecipeClick: (Recipe) -> Unit,
    onAuthorClick: (String) -> Unit,
) {
    Scaffold(topBar = { CenterAlignedTopAppBar(title = { Text("Поиск") }) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AppTextField(
                value = state.filters.query,
                onValueChange = { onFiltersChange(state.filters.copy(query = it)) },
                label = "Поиск по названию",
            )
            FilterSelector(
                label = "Категория",
                options = listOf("") + RecipeCatalog.categories,
                selected = state.filters.category,
                onSelected = { onFiltersChange(state.filters.copy(category = it)) },
            )
            FilterSelector(
                label = "Время",
                options = RecipeCatalog.timeRanges,
                selected = state.filters.timeRange,
                onSelected = { onFiltersChange(state.filters.copy(timeRange = it)) },
            )
            FilterSelector(
                label = "Сортировка",
                options = RecipeCatalog.sortOptions,
                selected = state.filters.sort,
                onSelected = { onFiltersChange(state.filters.copy(sort = it)) },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onSearch) { Text("Найти") }
                Button(onClick = onReset) { Text("Сбросить") }
            }
            when (val recipesState = state.state) {
                AsyncState.Empty -> EmptyState("Ничего не найдено", "Сбросить фильтры", onReset)
                is AsyncState.Error -> ErrorState(recipesState.message, onSearch)
                AsyncState.Loading -> LoadingState()
                is AsyncState.Success -> RecipeList(recipesState.data, onRecipeClick, onAuthorClick)
            }
        }
    }
}

@Composable
fun FilterSelector(
    label: String,
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.titleSmall)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(options.size) { index ->
                val option = options[index]
                FilterChip(
                    selected = option == selected,
                    onClick = { onSelected(option) },
                    label = { Text(if (option.isBlank()) "Все" else option) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailsScreen(
    viewModel: RecipeDetailsViewModel,
    onAuthorClick: (String) -> Unit,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Рецепт") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {
            item {
                when (val detailsState = state.detailsState) {
                    AsyncState.Loading -> LoadingState()
                    is AsyncState.Error -> ErrorState(detailsState.message, viewModel::refresh)
                    AsyncState.Empty -> EmptyState("Нет данных")
                    is AsyncState.Success -> {
                        val recipe = detailsState.data
                        Text(recipe.title, style = MaterialTheme.typography.headlineSmall)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(recipe.imageUrls.size) { index ->
                                AsyncImage(
                                    model = recipe.imageUrls[index],
                                    contentDescription = null,
                                    modifier = Modifier.width(260.dp).height(180.dp),
                                )
                            }
                        }
                        Text(recipe.description)
                        Text("Автор: ${recipe.author.username}", modifier = Modifier.padding(top = 6.dp))
                        Button(onClick = { onAuthorClick(recipe.author.id) }) { Text("Открыть профиль автора") }
                        Text("Категория: ${recipe.category}")
                        Text("Время: ${recipe.cookingTimeMinutes} мин")
                        Text("Рейтинг: ${recipe.rating} (${recipe.likesCount}/${recipe.dislikesCount})")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { viewModel.setRating(1) }) { Text("Лайк") }
                            Button(onClick = { viewModel.setRating(-1) }) { Text("Дизлайк") }
                            Button(onClick = viewModel::toggleFavorite) {
                                Text(if (recipe.isFavorite) "Убрать из избранного" else "В избранное")
                            }
                        }
                        Text("Ингредиенты", style = MaterialTheme.typography.titleMedium)
                        recipe.ingredients.forEach { Text("• $it") }
                        Text("Шаги", style = MaterialTheme.typography.titleMedium)
                        recipe.steps.forEachIndexed { index, step -> Text("${index + 1}. $step") }
                    }
                }
            }
            item {
                Text("Комментарии", style = MaterialTheme.typography.titleMedium)
                AppTextField(state.commentText, viewModel::updateComment, "Добавить комментарий")
                Spacer(Modifier.height(8.dp))
                Button(onClick = viewModel::addComment) { Text("Отправить") }
            }
            item {
                when (val commentsState = state.commentsState) {
                    AsyncState.Empty -> EmptyState("Комментариев пока нет")
                    is AsyncState.Error -> ErrorState(commentsState.message, viewModel::refresh)
                    AsyncState.Loading -> LoadingState()
                    is AsyncState.Success -> {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            commentsState.data.forEach { comment ->
                                Text("${comment.author.username}: ${comment.text}")
                                comment.replies.forEach { reply ->
                                    Text("↳ ${reply.author.username}: ${reply.text}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeEditorScreen(
    viewModel: EditRecipeViewModel,
    onSaved: (String) -> Unit,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    var categoriesExpanded by remember { mutableStateOf(false) }
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                viewModel.updateDraft { draft -> draft.copy(imageUrls = draft.imageUrls + uri.toString()) }
            }
        },
    )

    LaunchedEffect(state.savedRecipeId) {
        state.savedRecipeId?.let(onSaved)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Редактор рецепта") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                AppTextField(
                    value = state.draft.title,
                    onValueChange = { value -> viewModel.updateDraft { it.copy(title = value) } },
                    label = "Название",
                )
            }
            item {
                AppTextField(
                    value = state.draft.description,
                    onValueChange = { value -> viewModel.updateDraft { it.copy(description = value) } },
                    label = "Описание",
                    minLines = 3,
                )
            }
            item {
                AppTextField(
                    value = state.draft.category,
                    onValueChange = {},
                    label = "Категория",
                )
            }
            item {
                ExposedDropdownMenuBox(
                    expanded = categoriesExpanded,
                    onExpandedChange = { categoriesExpanded = !categoriesExpanded },
                ) {
                    OutlinedTextField(
                        value = state.draft.category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoriesExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                    )
                    DropdownMenu(
                        expanded = categoriesExpanded,
                        onDismissRequest = { categoriesExpanded = false },
                    ) {
                        RecipeCatalog.categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    viewModel.updateDraft { it.copy(category = category) }
                                    categoriesExpanded = false
                                },
                            )
                        }
                    }
                }
            }
            item {
                AppTextField(
                    value = state.draft.cookingTimeMinutes,
                    onValueChange = { value -> viewModel.updateDraft { it.copy(cookingTimeMinutes = value) } },
                    label = "Время приготовления (мин)",
                )
            }
            item {
                EditableListSection("Ингредиенты", state.draft.ingredients) { updated ->
                    viewModel.updateDraft { it.copy(ingredients = updated) }
                }
            }
            item {
                EditableListSection("Шаги", state.draft.steps) { updated ->
                    viewModel.updateDraft { it.copy(steps = updated) }
                }
            }
            item {
                Text("Фото рецепта", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { pickImageLauncher.launch("image/*") }) { Text("Выбрать фото") }
                }
                if (state.draft.imageUrls.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.draft.imageUrls.size) { index ->
                            AsyncImage(
                                model = state.draft.imageUrls[index],
                                contentDescription = null,
                                modifier = Modifier.width(180.dp).height(120.dp),
                            )
                        }
                    }
                }
            }
            item {
                EditableListSection("Ссылки на фото", state.draft.imageUrls) { updated ->
                    viewModel.updateDraft { it.copy(imageUrls = updated) }
                }
            }
            item {
                state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Button(onClick = viewModel::saveRecipe, modifier = Modifier.fillMaxWidth()) {
                    Text(if (state.isLoading) "Сохранение..." else "Сохранить рецепт")
                }
            }
        }
    }
}

@Composable
fun EditableListSection(
    title: String,
    values: List<String>,
    onChange: (List<String>) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        values.forEachIndexed { index, value ->
            AppTextField(
                value = value,
                onValueChange = { newValue ->
                    onChange(values.toMutableList().apply { set(index, newValue) })
                },
                label = "$title ${index + 1}",
            )
        }
        Button(onClick = { onChange(values + "") }) { Text("Добавить") }
    }
}
