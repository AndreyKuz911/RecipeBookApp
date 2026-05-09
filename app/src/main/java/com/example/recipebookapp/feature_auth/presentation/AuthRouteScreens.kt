package com.example.recipebookapp.feature_auth.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.recipebookapp.core.ui.AppTextField
import com.example.recipebookapp.core.ui.PrimaryWideButton
import com.example.recipebookapp.core.ui.SecondaryWideButton

@Composable
fun LoginScreen(
    state: AuthUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
    onOpenRegister: () -> Unit,
) {
    AuthContainer(
        title = "С возвращением",
        subtitle = "Вход в RecipeBook",
    ) {
        AppTextField(state.email, onEmailChange, "Email")
        AppTextField(state.password, onPasswordChange, "Пароль")
        state.error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        PrimaryWideButton(
            text = if (state.isLoading) "Входим..." else "Войти",
            onClick = onLogin,
            enabled = !state.isLoading,
        )
        SecondaryWideButton(
            text = "Нет аккаунта? Регистрация",
            onClick = onOpenRegister,
        )
    }
}

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
    AuthContainer(
        title = "Создайте аккаунт",
        subtitle = "Присоединяйтесь к RecipeBook",
    ) {
        AppTextField(state.email, onEmailChange, "Email")
        AppTextField(state.username, onUsernameChange, "Имя пользователя")
        AppTextField(state.password, onPasswordChange, "Пароль")
        AppTextField(state.confirmPassword, onConfirmPasswordChange, "Подтвердите пароль")
        state.error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        PrimaryWideButton(
            text = if (state.isLoading) "Создаем..." else "Создать аккаунт",
            onClick = onRegister,
            enabled = !state.isLoading,
        )
        SecondaryWideButton(
            text = "Уже есть аккаунт",
            onClick = onOpenLogin,
        )
    }
}

@Composable
private fun AuthContainer(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                        MaterialTheme.colorScheme.background,
                    ),
                ),
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                tonalElevation = 2.dp,
                shadowElevation = 1.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    content = content,
                )
            }
        }
    }
}
