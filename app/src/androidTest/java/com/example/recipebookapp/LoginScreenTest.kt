package com.example.recipebookapp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import com.example.recipebookapp.core.ui.RecipeBookTheme
import com.example.recipebookapp.feature_auth.presentation.AuthUiState
import com.example.recipebookapp.feature_auth.presentation.LoginScreen
import org.junit.Rule
import org.junit.Test

class LoginScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun loginScreenIsDisplayed() {
        composeRule.setContent {
            RecipeBookTheme {
                LoginScreen(
                    state = AuthUiState(),
                    onEmailChange = {},
                    onPasswordChange = {},
                    onLogin = {},
                    onOpenRegister = {},
                )
            }
        }

        composeRule.onNodeWithText("Вход").assertIsDisplayed()
        composeRule.onNodeWithText("Email").assertIsDisplayed()
        composeRule.onNodeWithText("Пароль").assertIsDisplayed()
    }

    @Test
    fun canInputEmailAndPassword() {
        composeRule.setContent {
            RecipeBookTheme {
                LoginScreen(
                    state = AuthUiState(),
                    onEmailChange = {},
                    onPasswordChange = {},
                    onLogin = {},
                    onOpenRegister = {},
                )
            }
        }

        composeRule.onNodeWithText("Email").performTextInput("chef@example.com")
        composeRule.onNodeWithText("Пароль").performTextInput("password123")
    }
}
