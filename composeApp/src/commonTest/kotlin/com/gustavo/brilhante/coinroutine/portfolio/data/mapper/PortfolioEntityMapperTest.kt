package com.gustavo.brilhante.coinroutine.portfolio.data.mapper

import com.gustavo.brilhante.coinroutine.coins.domain.coin.Coin
import com.gustavo.brilhante.coinroutine.portfolio.data.local.PortfolioCoinEntity
import com.gustavo.brilhante.coinroutine.portfolio.domain.PortfolioCoinModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PortfolioEntityMapperTest {

    private val entity = PortfolioCoinEntity(
        coinId = "bitcoin",
        name = "Bitcoin",
        symbol = "BTC",
        iconUrl = "https://example.com/btc.svg",
        averagePurchasePrice = 40000.0,
        amountOwned = 0.5,
        timestamp = 1700000000L,
    )

    @Test
    fun `entity maps to domain model with all coin fields`() {
        // Act
        val model = entity.toPortfolioCoinModel(currentPrice = 50000.0)

        // Assert
        assertEquals(entity.coinId, model.coin.id)
        assertEquals(entity.name, model.coin.name)
        assertEquals(entity.symbol, model.coin.symbol)
        assertEquals(entity.iconUrl, model.coin.iconUrl)
        assertEquals(entity.averagePurchasePrice, model.averagePurchasePrice)
        assertEquals(entity.amountOwned, model.ownedAmountInUnit)
    }

    @Test
    fun `entity maps to domain model with correct positive performance percent`() {
        // Arrange: bought at $40,000, now at $50,000 → +25%
        val currentPrice = 50000.0

        // Act
        val model = entity.toPortfolioCoinModel(currentPrice)

        // Assert: ((50000 - 40000) / 40000) * 100 = 25%
        assertEquals(25.0, model.performancePercent, absoluteTolerance = 0.001)
    }

    @Test
    fun `entity maps to domain model with correct negative performance percent`() {
        // Arrange: bought at $40,000, now at $30,000 → -25%
        val currentPrice = 30000.0

        // Act
        val model = entity.toPortfolioCoinModel(currentPrice)

        // Assert: ((30000 - 40000) / 40000) * 100 = -25%
        assertEquals(-25.0, model.performancePercent, absoluteTolerance = 0.001)
    }

    @Test
    fun `entity maps to domain model computing ownedAmountInFiat from current price`() {
        // Arrange: 0.5 BTC at $50,000 = $25,000
        val currentPrice = 50000.0

        // Act
        val model = entity.toPortfolioCoinModel(currentPrice)

        // Assert
        assertEquals(0.5 * 50000.0, model.ownedAmountInFiat)
    }

    @Test
    fun `domain model maps to entity with all fields`() {
        // Arrange
        val coin = Coin(id = "bitcoin", name = "Bitcoin", symbol = "BTC", iconUrl = "https://example.com/btc.svg")
        val model = PortfolioCoinModel(
            coin = coin,
            ownedAmountInUnit = 0.5,
            ownedAmountInFiat = 25000.0,
            performancePercent = 25.0,
            averagePurchasePrice = 40000.0,
        )
        val fixedTimestamp = 1_700_000_000_000L

        // Act
        val entity = model.toPortfolioCoinEntity(fixedTimestamp)

        // Assert
        assertEquals(coin.id, entity.coinId)
        assertEquals(coin.name, entity.name)
        assertEquals(coin.symbol, entity.symbol)
        assertEquals(coin.iconUrl, entity.iconUrl)
        assertEquals(model.ownedAmountInUnit, entity.amountOwned)
        assertEquals(model.averagePurchasePrice, entity.averagePurchasePrice)
        assertEquals(fixedTimestamp, entity.timestamp)
    }
}

private fun assertEquals(expected: Double, actual: Double, absoluteTolerance: Double) {
    assertTrue(
        kotlin.math.abs(expected - actual) <= absoluteTolerance,
        "Expected $expected but was $actual (tolerance $absoluteTolerance)"
    )
}