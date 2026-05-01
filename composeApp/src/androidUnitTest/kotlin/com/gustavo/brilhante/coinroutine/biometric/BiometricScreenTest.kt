package com.gustavo.brilhante.coinroutine.biometric

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gustavo.brilhante.coinroutine.utils.TestTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class BiometricScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `shows app name and login button`() {
        composeRule.setContent {
            TestTheme {
                BiometricContent(
                    onAuthenticate = { true },
                    onSuccess = {}
                )
            }
        }
        composeRule.onNodeWithText("CoinRoutine").assertIsDisplayed()
        composeRule.onNodeWithText("Login").assertIsDisplayed()
    }

    @Test
    fun `clicking login triggers onAuthenticate and then onSuccess on success`() {
        var authenticatedCalled = false
        var successCalled = false
        composeRule.setContent {
            TestTheme {
                BiometricContent(
                    onAuthenticate = {
                        authenticatedCalled = true
                        true
                    },
                    onSuccess = { successCalled = true }
                )
            }
        }
        composeRule.onNodeWithText("Login").performClick()
        
        composeRule.waitForIdle()
        assertTrue(authenticatedCalled)
        assertTrue(successCalled)
    }

    @Test
    fun `shows error message when authentication fails with exception`() {
        val errorMessage = "Auth Failed"
        composeRule.setContent {
            TestTheme {
                BiometricContent(
                    onAuthenticate = { throw Exception(errorMessage) },
                    onSuccess = {}
                )
            }
        }
        composeRule.onNodeWithText("Login").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(errorMessage).assertIsDisplayed()
    }
}
