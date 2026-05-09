package com.gustavo.brilhante.coinroutine.trade.presentation.mapper

import com.gustavo.brilhante.coinroutine.trade.presentation.common.UiTradeCoinItem
import kotlin.test.Test
import kotlin.test.assertEquals

class TradeCoinMapperTest {

    private val uiItem = UiTradeCoinItem(
        id = "bitcoin",
        name = "Bitcoin",
        symbol = "BTC",
        iconUrl = "https://example.com/btc.svg",
        price = 50000.0,
    )

    @Test
    fun `UiTradeCoinItem maps to Coin with id preserved`() {
        assertEquals(uiItem.id, uiItem.toCoin().id)
    }

    @Test
    fun `UiTradeCoinItem maps to Coin with name preserved`() {
        assertEquals(uiItem.name, uiItem.toCoin().name)
    }

    @Test
    fun `UiTradeCoinItem maps to Coin with symbol preserved`() {
        assertEquals(uiItem.symbol, uiItem.toCoin().symbol)
    }

    @Test
    fun `UiTradeCoinItem maps to Coin with iconUrl preserved`() {
        assertEquals(uiItem.iconUrl, uiItem.toCoin().iconUrl)
    }

    @Test
    fun `different coins produce distinct Coin instances`() {
        val eth = UiTradeCoinItem(
            id = "ethereum",
            name = "Ethereum",
            symbol = "ETH",
            iconUrl = "https://example.com/eth.svg",
            price = 3000.0,
        )
        val coin = eth.toCoin()
        assertEquals("ethereum", coin.id)
        assertEquals("ETH", coin.symbol)
    }
}
