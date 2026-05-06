package com.example.recipebookapp.feature_auth.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.recipebookapp.core.ui.AppTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    state: AuthUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
    onOpenRegister: () -> Unit,
) {
    Scaffold(topBar = { CenterAlignedTopAppBar(title = { Text("Вход") }) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("RecipeBook", style = MaterialTheme.typography.headlineMedium)
            Text("Публикуйте рецепты, сохраняйте избранное и следите за любимыми авторами.")
            AppTextField(state.email, onEmailChange, "Email")
            AppTextField(state.password, onPasswordChange, "Пароль")
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(onClick = onLogin, enabled = !state.isLoading, modifier = Modifier.fillMaxWidth()) {
                Text(if (state.isLoading) "Загрузка..." else "Войти")
            }
            Button(onClick = onOpenRegister, modifier = Modifier.fillMaxWidth()) {
                Text("Нет аккаунта? Регистрация")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    state: AuthUiState,
    onEmailChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onRegister: () -> Unit,
    onOpenLogin: () -> Unit,
) {
    Scaffold(topBar = { CenterAlignedTopAppBar(title = { Text("Регистрация") }) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("Создайте аккаунт", style = MaterialTheme.typography.headlineMedium)
            AppTextField(state.email, onEmailChange, "Email")
            AppTextField(state.username, onUsernameChange, "Username")
            AppTextField(state.password, onPasswordChange, "Пароль")
            AppTextField(state.confirmPassword, onConfirmPasswordChange, "Подтвердите пароль")
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(onClick = onRegister, enabled = !state.isLoading, modifier = Modifier.fillMaxWidth()) {
                Text(if (state.isLoading) "Загрузка..." else "Создать аккаунт")
            }
            Button(onClick = onOpenLogin, modifier = Modifier.fillMaxWidth()) {
                Text("Уже есть аккаунт")
            }
        }
    }
}
