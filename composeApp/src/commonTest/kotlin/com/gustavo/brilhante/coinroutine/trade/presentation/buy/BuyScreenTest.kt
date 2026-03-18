package com.gustavo.brilhante.coinroutine.trade.presentation.buy

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import coinroutine.composeapp.generated.resources.Res
import coinroutine.composeapp.generated.resources.error_unknown
import com.gustavo.brilhante.coinroutine.trade.presentation.common.TradeScreen
import com.gustavo.brilhante.coinroutine.trade.presentation.common.TradeState
import com.gustavo.brilhante.coinroutine.trade.presentation.common.TradeType
import com.gustavo.brilhante.coinroutine.trade.presentation.common.UiTradeCoinItem
import kotlin.test.Test

class BuyScreenTest {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun checkSubmitButtonLabelChangesWithTradeType() = runComposeUiTest {
        val state = TradeState(
            coin = UiTradeCoinItem(
                id = "bitcoin",
                name = "Bitcoin",
                symbol = "BTC",
                iconUrl = "url",
                price = 50000.0
            )
        )

        setContent {
            TradeScreen(
                state = state,
                tradeType = TradeType.BUY,
                onAmountChange = {},
                onSubmitClicked = {}
            )
        }

        onNodeWithText("Sell Now").assertDoesNotExist()
        onNodeWithText("Buy Now").assertExists()
        onNodeWithText("Buy Now").assertIsDisplayed()

        setContent {
            TradeScreen(
                state = state,
                tradeType = TradeType.SELL,
                onAmountChange = {},
                onSubmitClicked = {}
            )
        }

        onNodeWithText("Buy Now").assertDoesNotExist()
        onNodeWithText("Sell Now").assertExists()
        onNodeWithText("Sell Now").assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun checkIfCoinNameShowProperlyInBuy() = runComposeUiTest {
        val state = TradeState(
            coin = UiTradeCoinItem(
                id = "bitcoin",
                name = "Bitcoin",
                symbol = "BTC",
                iconUrl = "url",
                price = 50000.0
            )
        )

        setContent {
            TradeScreen(
                state = state,
                tradeType = TradeType.BUY,
                onAmountChange = {},
                onSubmitClicked = {}
            )
        }

        onNodeWithTag("trade_screen_coin_name").assertExists()
        onNodeWithTag("trade_screen_coin_name").assertTextEquals("Bitcoin")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun checkErrorIsShownProperly() = runComposeUiTest {
        val state = TradeState(
            coin = UiTradeCoinItem(
                id = "bitcoin",
                name = "Bitcoin",
                symbol = "BTC",
                iconUrl = "url",
                price = 50000.0
            ),
            error = Res.string.error_unknown
        )

        setContent {
            TradeScreen(
                state = state,
                tradeType = TradeType.BUY,
                onAmountChange = {},
                onSubmitClicked = {}
            )
        }

        onNodeWithTag("trade_error").assertExists()
        onNodeWithTag("trade_error").assertIsDisplayed()
    }
}