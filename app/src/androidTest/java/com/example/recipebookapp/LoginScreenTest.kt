package com.example.recipebookapp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.recipebookapp.core.ui.RecipeBookTheme
import com.example.recipebookapp.feature_auth.presentation.AuthUiState
import com.example.recipebookapp.feature_auth.presentation.LoginScreen
import org.junit.Assert.assertEquals
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

        composeRule.onNodeWithText("Email").assertIsDisplayed()
        composeRule.onAllNodes(hasSetTextAction())[1].assertIsDisplayed()
    }

    @Test
    fun canInputEmailAndPassword() {
        var uiState by mutableStateOf(AuthUiState())
        composeRule.setContent {
            RecipeBookTheme {
                LoginScreen(
                    state = uiState,
                    onEmailChange = { uiState = uiState.copy(email = it) },
                    onPasswordChange = { uiState = uiState.copy(password = it) },
                    onLogin = {},
                    onOpenRegister = {},
                )
            }
        }

        composeRule.onAllNodes(hasSetTextAction())[0].performTextInput("chef@example.com")
        composeRule.onAllNodes(hasSetTextAction())[1].performTextInput("password123")

        composeRule.runOnIdle {
            assertEquals("chef@example.com", uiState.email)
            assertEquals("password123", uiState.password)
        }
    }
}
