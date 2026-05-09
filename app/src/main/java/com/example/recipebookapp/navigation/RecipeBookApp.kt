package com.example.recipebookapp.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.recipebookapp.core.ui.LoadingState
import com.example.recipebookapp.feature_auth.presentation.AuthViewModel
import com.example.recipebookapp.feature_auth.presentation.LoginScreen
import com.example.recipebookapp.feature_auth.presentation.RegisterScreen
import com.example.recipebookapp.feature_auth.presentation.SplashViewModel
import com.example.recipebookapp.feature_favorites.presentation.FavoritesScreen
import com.example.recipebookapp.feature_favorites.presentation.FavoritesViewModel
import com.example.recipebookapp.feature_feed.presentation.FeedScreen
import com.example.recipebookapp.feature_feed.presentation.FeedViewModel
import com.example.recipebookapp.feature_profile.presentation.OtherProfileScreen
import com.example.recipebookapp.feature_profile.presentation.OtherProfileViewModel
import com.example.recipebookapp.feature_profile.presentation.ProfileScreen
import com.example.recipebookapp.feature_profile.presentation.ProfileViewModel
import com.example.recipebookapp.feature_recipes.presentation.EditRecipeViewModel
import com.example.recipebookapp.feature_recipes.presentation.HomeScreen
import com.example.recipebookapp.feature_recipes.presentation.HomeViewModel
import com.example.recipebookapp.feature_recipes.presentation.RecipeDetailsScreen
import com.example.recipebookapp.feature_recipes.presentation.RecipeDetailsViewModel
import com.example.recipebookapp.feature_recipes.presentation.RecipeEditorScreen
import com.example.recipebookapp.feature_recipes.presentation.SearchScreen
import com.example.recipebookapp.feature_recipes.presentation.SearchViewModel

object AppRoutes {
    const val Splash = "splash"
    const val Login = "login"
    const val Register = "register"
    const val Main = "main"
    const val Home = "home"
    const val Search = "search"
    const val Feed = "feed"
    const val Favorites = "favorites"
    const val Profile = "profile"
    const val RecipeDetails = "recipe/{recipeId}"
    const val RecipeDetailsPath = "recipe/"
    const val EditRecipe = "edit?recipeId={recipeId}"
    const val EditRecipePath = "edit"
    const val OtherProfile = "user/{userId}"
    const val OtherProfilePath = "user/"
}

private data class BottomItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

@Composable
fun RecipeBookApp() {
    val rootNavController = rememberNavController()
    val splashViewModel: SplashViewModel = hiltViewModel()
    val isAuthorized by splashViewModel.isAuthorized.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    NavHost(navController = rootNavController, startDestination = AppRoutes.Splash) {
        composable(AppRoutes.Splash) {
            LaunchedEffect(isAuthorized) {
                if (isAuthorized != null) {
                    rootNavController.navigate(if (isAuthorized == true) AppRoutes.Main else AppRoutes.Login) {
                        popUpTo(AppRoutes.Splash) { inclusive = true }
                    }
                }
            }
            LoadingState(Modifier)
        }

        composable(AppRoutes.Login) {
            val viewModel: AuthViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()

            LaunchedEffect(state.isAuthenticated) {
                if (state.isAuthenticated) {
                    viewModel.resetAuthState()
                    rootNavController.navigate(AppRoutes.Main) {
                        popUpTo(AppRoutes.Login) { inclusive = true }
                    }
                }
            }

            LoginScreen(
                state = state,
                onEmailChange = viewModel::updateEmail,
                onPasswordChange = viewModel::updatePassword,
                onLogin = viewModel::login,
                onOpenRegister = { rootNavController.navigate(AppRoutes.Register) },
            )
        }

        composable(AppRoutes.Register) {
            val viewModel: AuthViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()

            LaunchedEffect(state.isAuthenticated) {
                if (state.isAuthenticated) {
                    viewModel.resetAuthState()
                    rootNavController.navigate(AppRoutes.Main) {
                        popUpTo(AppRoutes.Login) { inclusive = true }
                    }
                }
            }

            RegisterScreen(
                state = state,
                onEmailChange = viewModel::updateEmail,
                onUsernameChange = viewModel::updateUsername,
                onPasswordChange = viewModel::updatePassword,
                onConfirmPasswordChange = viewModel::updateConfirmPassword,
                onRegister = viewModel::register,
                onOpenLogin = { rootNavController.popBackStack() },
            )
        }

        composable(AppRoutes.Main) {
            val mainAuthViewModel: SplashViewModel = hiltViewModel()
            val isAuthorizedInMain by mainAuthViewModel.isAuthorized.collectAsState()
            LaunchedEffect(isAuthorizedInMain) {
                if (isAuthorizedInMain == false) {
                    rootNavController.navigate(AppRoutes.Login) {
                        popUpTo(AppRoutes.Main) { inclusive = true }
                    }
                }
            }
            MainShell(rootNavController, snackbarHostState)
        }

        composable(AppRoutes.RecipeDetails) {
            val viewModel: RecipeDetailsViewModel = hiltViewModel()
            RecipeDetailsScreen(
                viewModel = viewModel,
                onAuthorClick = { rootNavController.navigate("${AppRoutes.OtherProfilePath}$it") },
                onBack = { rootNavController.popBackStack() },
            )
        }

        composable(AppRoutes.EditRecipe) {
            val viewModel: EditRecipeViewModel = hiltViewModel()
            RecipeEditorScreen(
                viewModel = viewModel,
                onSaved = {
                    rootNavController.navigate("${AppRoutes.RecipeDetailsPath}$it") {
                        popUpTo(AppRoutes.EditRecipePath) { inclusive = true }
                    }
                },
                onBack = { rootNavController.popBackStack() },
            )
        }

        composable(AppRoutes.OtherProfile) {
            val viewModel: OtherProfileViewModel = hiltViewModel()
            OtherProfileScreen(
                viewModel = viewModel,
                onRecipeClick = { rootNavController.navigate("${AppRoutes.RecipeDetailsPath}$it") },
                onAuthorClick = {},
                onBack = { rootNavController.popBackStack() },
            )
        }
    }
}

@Composable
private fun MainShell(
    rootNavController: NavHostController,
    snackbarHostState: SnackbarHostState,
) {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val items = listOf(
        BottomItem(AppRoutes.Home, "Главная", Icons.Outlined.Home),
        BottomItem(AppRoutes.Search, "Поиск", Icons.Outlined.Search),
        BottomItem(AppRoutes.Feed, "Новости", Icons.Outlined.Newspaper),
        BottomItem(AppRoutes.Favorites, "Избранное", Icons.Outlined.Bookmark),
        BottomItem(AppRoutes.Profile, "Профиль", Icons.Outlined.Person),
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = null) },
                        label = { Text(item.label) },
                    )
                }
            }
        },
        floatingActionButton = {
            if (currentRoute == AppRoutes.Profile || currentRoute == AppRoutes.Home) {
                FloatingActionButton(
                    onClick = { rootNavController.navigate(AppRoutes.EditRecipePath) },
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null)
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = AppRoutes.Home,
            modifier = Modifier.padding(padding),
        ) {
            composable(AppRoutes.Home) {
                val viewModel: HomeViewModel = hiltViewModel()
                HomeScreen(
                    state = viewModel.state.collectAsState().value.state,
                    onRetry = viewModel::loadRecipes,
                    onRecipeClick = { rootNavController.navigate("${AppRoutes.RecipeDetailsPath}${it.id}") },
                    onAuthorClick = { rootNavController.navigate("${AppRoutes.OtherProfilePath}$it") },
                )
            }

            composable(AppRoutes.Search) {
                val viewModel: SearchViewModel = hiltViewModel()
                SearchScreen(
                    state = viewModel.state.collectAsState().value,
                    onFiltersChange = viewModel::updateFilters,
                    onSearch = viewModel::search,
                    onReset = viewModel::resetFilters,
                    onRecipeClick = { rootNavController.navigate("${AppRoutes.RecipeDetailsPath}${it.id}") },
                    onAuthorClick = { rootNavController.navigate("${AppRoutes.OtherProfilePath}$it") },
                )
            }

            composable(AppRoutes.Feed) {
                val viewModel: FeedViewModel = hiltViewModel()
                FeedScreen(viewModel = viewModel)
            }

            composable(AppRoutes.Favorites) {
                val viewModel: FavoritesViewModel = hiltViewModel()
                FavoritesScreen(
                    viewModel = viewModel,
                    onRecipeClick = { rootNavController.navigate("${AppRoutes.RecipeDetailsPath}${it.id}") },
                    onAuthorClick = { rootNavController.navigate("${AppRoutes.OtherProfilePath}$it") },
                )
            }

            composable(AppRoutes.Profile) {
                val viewModel: ProfileViewModel = hiltViewModel()
                ProfileScreen(
                    viewModel = viewModel,
                    onRecipeClick = { rootNavController.navigate("${AppRoutes.RecipeDetailsPath}$it") },
                    onAuthorClick = {},
                    onCreateRecipe = { rootNavController.navigate(AppRoutes.EditRecipePath) },
                    onLoggedOut = {
                        rootNavController.navigate(AppRoutes.Login) {
                            popUpTo(AppRoutes.Main) { inclusive = true }
                        }
                    },
                )
            }
        }
    }
}
