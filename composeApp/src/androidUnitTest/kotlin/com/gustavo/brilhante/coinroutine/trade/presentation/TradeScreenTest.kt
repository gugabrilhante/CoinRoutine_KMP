package com.gustavo.brilhante.coinroutine.trade.presentation

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import coinroutine.composeapp.generated.resources.Res
import coinroutine.composeapp.generated.resources.error_insufficient_balance
import com.gustavo.brilhante.coinroutine.trade.presentation.common.TradeScreen
import com.gustavo.brilhante.coinroutine.trade.presentation.common.TradeState
import com.gustavo.brilhante.coinroutine.trade.presentation.common.TradeType
import com.gustavo.brilhante.coinroutine.trade.presentation.common.UiTradeCoinItem
import com.gustavo.brilhante.coinroutine.utils.TestTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class TradeScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val fakeCoin = UiTradeCoinItem(
        id = "btc-1",
        name = "Bitcoin",
        symbol = "BTC",
        iconUrl = "",
        price = 50000.0,
    )

    @Test
    fun `shows coin name in trade screen`() {
        composeRule.setContent {
            TestTheme {
                TradeScreen(
                    state = TradeState(coin = fakeCoin, availableAmount = "$10,000"),
                    tradeType = TradeType.BUY,
                    onAmountChange = {},
                    onSubmitClicked = {},
                )
            }
        }
        composeRule.onNodeWithTag("trade_screen_coin_name")
            .assertIsDisplayed()
            .assert(hasText("Bitcoin"))
    }

    @Test
    fun `shows buy amount label for BUY trade type`() {
        composeRule.setContent {
            TestTheme {
                TradeScreen(
                    state = TradeState(coin = fakeCoin, availableAmount = "$10,000"),
                    tradeType = TradeType.BUY,
                    onAmountChange = {},
                    onSubmitClicked = {},
                )
            }
        }
        composeRule.onNodeWithTag("trade_type_label").assert(hasText("Buy Amount"))
    }

    @Test
    fun `shows sell amount label for SELL trade type`() {
        composeRule.setContent {
            TestTheme {
                TradeScreen(
                    state = TradeState(coin = fakeCoin, availableAmount = "$10,000"),
                    tradeType = TradeType.SELL,
                    onAmountChange = {},
                    onSubmitClicked = {},
                )
            }
        }
        composeRule.onNodeWithTag("trade_type_label").assert(hasText("Sell Amount"))
    }

    @Test
    fun `shows submit button`() {
        composeRule.setContent {
            TestTheme {
                TradeScreen(
                    state = TradeState(coin = fakeCoin, availableAmount = "$10,000"),
                    tradeType = TradeType.BUY,
                    onAmountChange = {},
                    onSubmitClicked = {},
                )
            }
        }
        composeRule.onNodeWithTag("trade_submit_button").assertIsDisplayed()
    }

    @Test
    fun `clicking submit button triggers onSubmitClicked`() {
        var submitted = false
        composeRule.setContent {
            TestTheme {
                TradeScreen(
                    state = TradeState(coin = fakeCoin, availableAmount = "$10,000"),
                    tradeType = TradeType.BUY,
                    onAmountChange = {},
                    onSubmitClicked = { submitted = true },
                )
            }
        }
        composeRule.onNodeWithTag("trade_submit_button").performClick()
        assertTrue(submitted)
    }

    @Test
    fun `shows error message when state has error`() {
        composeRule.setContent {
            TestTheme {
                TradeScreen(
                    state = TradeState(
                        coin = fakeCoin,
                        availableAmount = "$10,000",
                        error = Res.string.error_insufficient_balance,
                    ),
                    tradeType = TradeType.BUY,
                    onAmountChange = {},
                    onSubmitClicked = {},
                )
            }
        }
        composeRule.onNodeWithTag("trade_error").assertIsDisplayed()
        composeRule.onNodeWithText("Insufficient funds!").assertIsDisplayed()
    }

    @Test
    fun `shows available amount text`() {
        composeRule.setContent {
            TestTheme {
                TradeScreen(
                    state = TradeState(coin = fakeCoin, availableAmount = "Available: $10,000"),
                    tradeType = TradeType.BUY,
                    onAmountChange = {},
                    onSubmitClicked = {},
                )
            }
        }
        composeRule.onNodeWithText("Available: $10,000").assertIsDisplayed()
    }

    @Test
    fun `typing amount triggers onAmountChange`() {
        var amount = ""
        composeRule.setContent {
            TestTheme {
                TradeScreen(
                    state = TradeState(coin = fakeCoin, availableAmount = "$10,000"),
                    tradeType = TradeType.BUY,
                    onAmountChange = { amount = it },
                    onSubmitClicked = {},
                )
            }
        }
        composeRule.onNodeWithTag("trade_amount_input").performTextInput("100")
        assertTrue(amount == "100")
    }
}
