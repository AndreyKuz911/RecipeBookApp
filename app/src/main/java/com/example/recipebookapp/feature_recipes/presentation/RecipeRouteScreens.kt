package com.example.recipebookapp.feature_recipes.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.recipebookapp.core.model.Recipe
import com.example.recipebookapp.core.presentation.AsyncState
import com.example.recipebookapp.core.ui.AppTextField
import com.example.recipebookapp.core.ui.EmptyState
import com.example.recipebookapp.core.ui.ErrorState
import com.example.recipebookapp.core.ui.LoadingState
import com.example.recipebookapp.core.ui.PrimaryWideButton
import com.example.recipebookapp.core.ui.RecipeCard
import com.example.recipebookapp.core.ui.RecipeList
import com.example.recipebookapp.core.ui.SecondaryWideButton
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
    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Новые рецепты") }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(14.dp),
        ) {
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
    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Поиск рецептов") }) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 22.dp),
        ) {
            item {
                Surface(shape = RoundedCornerShape(20.dp), tonalElevation = 2.dp) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        AppTextField(
                            value = state.filters.query,
                            onValueChange = { onFiltersChange(state.filters.copy(query = it)) },
                            label = "Название или ингредиент",
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Button(
                                modifier = Modifier.weight(1f),
                                onClick = onSearch,
                                shape = RoundedCornerShape(14.dp),
                            ) { Text("Найти") }
                            Button(
                                modifier = Modifier.weight(1f),
                                onClick = onReset,
                                shape = RoundedCornerShape(14.dp),
                            ) { Text("Сбросить") }
                        }
                    }
                }
            }
            when (val recipesState = state.state) {
                AsyncState.Empty -> item {
                    EmptyState("Ничего не найдено", "Сбросить фильтры", onReset)
                }
                is AsyncState.Error -> item {
                    ErrorState(recipesState.message, onSearch)
                }
                AsyncState.Loading -> item {
                    LoadingState()
                }
                is AsyncState.Success -> {
                    items(recipesState.data) { recipe ->
                        RecipeCard(
                            recipe = recipe,
                            onClick = { onRecipeClick(recipe) },
                            onAuthorClick = { onAuthorClick(recipe.author.id) },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterSelector(
    label: String,
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.titleSmall)
        androidx.compose.foundation.layout.FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            options.forEach { option ->
                FilterChip(
                    selected = option == selected,
                    onClick = { onSelected(option) },
                    label = { Text(if (option.isBlank()) "Все" else option.toUiLabel()) },
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
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 28.dp),
        ) {
            item {
                when (val detailsState = state.detailsState) {
                    AsyncState.Loading -> LoadingState()
                    is AsyncState.Error -> ErrorState(detailsState.message, viewModel::refresh)
                    AsyncState.Empty -> EmptyState("Нет данных")
                    is AsyncState.Success -> {
                        val recipe = detailsState.data
                        Card(shape = RoundedCornerShape(20.dp)) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                if (recipe.imageUrls.isNotEmpty()) {
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(recipe.imageUrls) { image ->
                                            AsyncImage(
                                                model = image,
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .width(260.dp)
                                                    .height(180.dp)
                                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                            )
                                        }
                                    }
                                }
                                Text(
                                    recipe.title,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    recipe.description,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text("Автор: ${recipe.author.username}")
                                    Text("${recipe.cookingTimeMinutes} мин")
                                }
                                Text("Категория: ${recipe.category}")
                                Text("Рейтинг: ${recipe.rating} (${recipe.likesCount}/${recipe.dislikesCount})")
                                SecondaryWideButton(
                                    text = "Открыть профиль автора",
                                    onClick = { onAuthorClick(recipe.author.id) },
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        modifier = Modifier.weight(1f),
                                        onClick = { viewModel.setRating(1) },
                                        shape = RoundedCornerShape(14.dp),
                                        enabled = !state.actionInProgress,
                                    ) {
                                        Icon(Icons.Outlined.ThumbUp, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Лайк")
                                    }
                                    Button(
                                        modifier = Modifier.weight(1f),
                                        onClick = { viewModel.setRating(-1) },
                                        shape = RoundedCornerShape(14.dp),
                                        enabled = !state.actionInProgress,
                                    ) {
                                        Icon(Icons.Outlined.ThumbDown, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Дизлайк")
                                    }
                                }
                                Button(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = viewModel::toggleFavorite,
                                    shape = RoundedCornerShape(14.dp),
                                    enabled = !state.actionInProgress,
                                ) {
                                    Icon(Icons.Outlined.Bookmark, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (recipe.isFavorite) "Убрать из избранного" else "Добавить в избранное")
                                }
                                Text("Ингредиенты", style = MaterialTheme.typography.titleMedium)
                                recipe.ingredients.forEach { Text("• $it") }
                                Text("Шаги", style = MaterialTheme.typography.titleMedium)
                                recipe.steps.forEachIndexed { index, step ->
                                    Text("${index + 1}. $step")
                                }
                            }
                        }
                    }
                }
            }
            item {
                Card(shape = RoundedCornerShape(20.dp)) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text("Комментарии", style = MaterialTheme.typography.titleMedium)
                        AppTextField(
                            value = state.commentText,
                            onValueChange = viewModel::updateComment,
                            label = "Добавить комментарий",
                        )
                        PrimaryWideButton("Отправить", onClick = viewModel::addComment)
                    }
                }
            }
            item {
                when (val commentsState = state.commentsState) {
                    AsyncState.Empty -> EmptyState("Комментариев пока нет")
                    is AsyncState.Error -> ErrorState(commentsState.message, viewModel::refresh)
                    AsyncState.Loading -> LoadingState()
                    is AsyncState.Success -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            commentsState.data.forEach { comment ->
                                Card(shape = RoundedCornerShape(14.dp)) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        Text(
                                            text = comment.author.username,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                        Text(comment.text)
                                        comment.replies.forEach { reply ->
                                            Text(
                                                text = "↳ ${reply.author.username}: ${reply.text}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    }
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
            modifier = Modifier.fillMaxSize().padding(padding).padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item {
                Surface(shape = RoundedCornerShape(20.dp), tonalElevation = 2.dp) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        AppTextField(
                            value = state.draft.title,
                            onValueChange = { value -> viewModel.updateDraft { it.copy(title = value) } },
                            label = "Название",
                        )
                        AppTextField(
                            value = state.draft.description,
                            onValueChange = { value -> viewModel.updateDraft { it.copy(description = value) } },
                            label = "Описание",
                            minLines = 3,
                        )
                        ExposedDropdownMenuBox(
                            expanded = categoriesExpanded,
                            onExpandedChange = { categoriesExpanded = !categoriesExpanded },
                        ) {
                            OutlinedTextField(
                                value = state.draft.category,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Категория") },
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
                        AppTextField(
                            value = state.draft.cookingTimeMinutes,
                            onValueChange = { value -> viewModel.updateDraft { it.copy(cookingTimeMinutes = value) } },
                            label = "Время приготовления (мин)",
                        )
                        EditableListSection("Ингредиенты", state.draft.ingredients) { updated ->
                            viewModel.updateDraft { it.copy(ingredients = updated) }
                        }
                        EditableListSection("Шаги", state.draft.steps) { updated ->
                            viewModel.updateDraft { it.copy(steps = updated) }
                        }
                    }
                }
            }
            item {
                Surface(shape = RoundedCornerShape(20.dp), tonalElevation = 2.dp) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text("Фото рецепта", style = MaterialTheme.typography.titleMedium)
                        Button(
                            onClick = { pickImageLauncher.launch("image/*") },
                            shape = RoundedCornerShape(14.dp),
                        ) {
                            Text("Выбрать фото")
                        }
                        if (state.draft.imageUrls.isNotEmpty()) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(state.draft.imageUrls) { image ->
                                    AsyncImage(
                                        model = image,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .width(180.dp)
                                            .height(120.dp),
                                    )
                                }
                            }
                        }
                        EditableListSection("Ссылки на фото", state.draft.imageUrls) { updated ->
                            viewModel.updateDraft { it.copy(imageUrls = updated) }
                        }
                    }
                }
            }
            item {
                state.error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                PrimaryWideButton(
                    text = if (state.isLoading) "Сохранение..." else "Сохранить рецепт",
                    onClick = viewModel::saveRecipe,
                    enabled = !state.isLoading,
                )
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
        Text(title, style = MaterialTheme.typography.titleSmall)
        values.forEachIndexed { index, value ->
            AppTextField(
                value = value,
                onValueChange = { newValue ->
                    onChange(values.toMutableList().apply { set(index, newValue) })
                },
                label = "$title ${index + 1}",
            )
        }
        SecondaryWideButton(
            text = "Добавить",
            onClick = { onChange(values + "") },
        )
    }
}

private fun String.toUiLabel(): String = when (this) {
    "up_to_15" -> "до 15 минут"
    "15-30" -> "15–30 минут"
    "30-60" -> "30–60 минут"
    "60+" -> "60+ минут"
    "newest" -> "сначала новые"
    "rating" -> "по рейтингу"
    else -> this
}
