package com.gustavo.brilhante.coinroutine.coins.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gustavo.brilhante.coinroutine.utils.TestTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class CoinsListScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val fakeCoin = UiCoinListItem(
        id = "btc-1",
        name = "Bitcoin",
        symbol = "BTC",
        iconUrl = "",
        formattedPrice = "$50,000",
        formattedChange = "+2.5%",
        isPositive = true,
    )

    @Test
    fun `shows top coins title`() {
        composeRule.setContent {
            TestTheme {
                CoinsListContent(
                    state = CoinsState(),
                    onDismissChart = {},
                    onCoinLongPressed = {},
                    onCoinClicked = {},
                )
            }
        }
        composeRule.onNodeWithText("Top Coins:").assertIsDisplayed()
    }

    @Test
    fun `shows coin name symbol and formatted price when coins are loaded`() {
        composeRule.setContent {
            TestTheme {
                CoinsListContent(
                    state = CoinsState(coins = listOf(fakeCoin)),
                    onDismissChart = {},
                    onCoinLongPressed = {},
                    onCoinClicked = {},
                )
            }
        }
        composeRule.onNodeWithText("Bitcoin").assertIsDisplayed()
        composeRule.onNodeWithText("BTC").assertIsDisplayed()
        composeRule.onNodeWithText("$50,000").assertIsDisplayed()
        composeRule.onNodeWithText("+2.5%").assertIsDisplayed()
    }

    @Test
    fun `clicking coin triggers onCoinClicked with coin id`() {
        var clickedId = ""
        composeRule.setContent {
            TestTheme {
                CoinsListContent(
                    state = CoinsState(coins = listOf(fakeCoin)),
                    onDismissChart = {},
                    onCoinLongPressed = {},
                    onCoinClicked = { clickedId = it },
                )
            }
        }
        composeRule.onNodeWithText("Bitcoin").performClick()
        assertEquals("btc-1", clickedId)
    }

    @Test
    fun `chart dialog is shown when chartState is not null`() {
        val chartState = UiChartState(isLoading = false, coinName = "Bitcoin", sparkLine = listOf(45000.0))
        composeRule.setContent {
            TestTheme {
                CoinsListContent(
                    state = CoinsState(chartState = chartState),
                    onDismissChart = {},
                    onCoinLongPressed = {},
                    onCoinClicked = {},
                )
            }
        }
        composeRule.onNodeWithText("24h Price chart for Bitcoin").assertIsDisplayed()
    }

    @Test
    fun `close button in chart dialog triggers onDismissChart`() {
        var dismissed = false
        val chartState = UiChartState(isLoading = false, coinName = "Bitcoin", sparkLine = emptyList())
        composeRule.setContent {
            TestTheme {
                CoinsListContent(
                    state = CoinsState(chartState = chartState),
                    onDismissChart = { dismissed = true },
                    onCoinLongPressed = {},
                    onCoinClicked = {},
                )
            }
        }
        composeRule.onNodeWithText("Close").performClick()
        assertTrue(dismissed)
    }
}
