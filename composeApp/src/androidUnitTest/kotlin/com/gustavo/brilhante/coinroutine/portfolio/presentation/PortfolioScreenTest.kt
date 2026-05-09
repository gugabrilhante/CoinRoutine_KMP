package com.gustavo.brilhante.coinroutine.portfolio.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
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
class PortfolioScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val fakeCoin = UiPortfolioCoinItem(
        id = "btc-1",
        name = "Bitcoin",
        iconUrl = "",
        amountInUnitText = "0.5 BTC",
        amountInFiatText = "$25,000",
        performancePercentText = "+10%",
        isPositive = true,
    )

    @Test
    fun `shows total value label and portfolio value`() {
        composeRule.setContent {
            TestTheme {
                PortfolioContent(
                    state = PortfolioState(portfolioValue = "$100,000", cashBalance = "$10,000"),
                    onCoinItemClicked = {},
                    onDiscoverCoinsClicked = {},
                )
            }
        }
        composeRule.onNodeWithText("Total Value:").assertIsDisplayed()
        composeRule.onNodeWithTag("portfolio_value").assertIsDisplayed()
        composeRule.onNodeWithText("$100,000").assertIsDisplayed()
    }

    @Test
    fun `shows cash balance`() {
        composeRule.setContent {
            TestTheme {
                PortfolioContent(
                    state = PortfolioState(portfolioValue = "$100,000", cashBalance = "$10,000"),
                    onCoinItemClicked = {},
                    onDiscoverCoinsClicked = {},
                )
            }
        }
        composeRule.onNodeWithTag("cash_balance").assertIsDisplayed()
        composeRule.onNodeWithText("$10,000").assertIsDisplayed()
    }

    @Test
    fun `shows empty section message when portfolio has no coins`() {
        composeRule.setContent {
            TestTheme {
                PortfolioContent(
                    state = PortfolioState(coins = emptyList()),
                    onCoinItemClicked = {},
                    onDiscoverCoinsClicked = {},
                )
            }
        }
        composeRule.onNodeWithText("No coins in your portfolio yet.").assertIsDisplayed()
    }

    @Test
    fun `shows discover coins button when portfolio is empty`() {
        composeRule.setContent {
            TestTheme {
                PortfolioContent(
                    state = PortfolioState(coins = emptyList()),
                    onCoinItemClicked = {},
                    onDiscoverCoinsClicked = {},
                )
            }
        }
        composeRule.onNodeWithText("Discover coins").assertIsDisplayed()
    }

    @Test
    fun `clicking discover coins button triggers onDiscoverCoinsClicked`() {
        var clicked = false
        composeRule.setContent {
            TestTheme {
                PortfolioContent(
                    state = PortfolioState(coins = emptyList()),
                    onCoinItemClicked = {},
                    onDiscoverCoinsClicked = { clicked = true },
                )
            }
        }
        composeRule.onNodeWithText("Discover coins").performClick()
        assertTrue(clicked)
    }

    @Test
    fun `shows coin name and holdings when portfolio has coins`() {
        composeRule.setContent {
            TestTheme {
                PortfolioContent(
                    state = PortfolioState(coins = listOf(fakeCoin)),
                    onCoinItemClicked = {},
                    onDiscoverCoinsClicked = {},
                )
            }
        }
        composeRule.onNodeWithText("Bitcoin").assertIsDisplayed()
        composeRule.onNodeWithText("0.5 BTC").assertIsDisplayed()
        composeRule.onNodeWithText("$25,000").assertIsDisplayed()
    }

    @Test
    fun `clicking coin item triggers onCoinItemClicked with coin id`() {
        var clickedId = ""
        composeRule.setContent {
            TestTheme {
                PortfolioContent(
                    state = PortfolioState(coins = listOf(fakeCoin)),
                    onCoinItemClicked = { clickedId = it },
                    onDiscoverCoinsClicked = {},
                )
            }
        }
        composeRule.onNodeWithText("Bitcoin").performClick()
        assertEquals("btc-1", clickedId)
    }

    @Test
    fun `shows buy coin button when showBuyButton is true`() {
        composeRule.setContent {
            TestTheme {
                PortfolioContent(
                    state = PortfolioState(showBuyButton = true),
                    onCoinItemClicked = {},
                    onDiscoverCoinsClicked = {},
                )
            }
        }
        composeRule.onNodeWithTag("buy_coin_button").assertIsDisplayed()
        composeRule.onNodeWithText("Buy Coin").assertIsDisplayed()
    }
}
