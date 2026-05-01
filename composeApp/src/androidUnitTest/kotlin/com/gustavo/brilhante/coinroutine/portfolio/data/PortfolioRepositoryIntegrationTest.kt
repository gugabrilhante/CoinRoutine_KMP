package com.gustavo.brilhante.coinroutine.portfolio.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.gustavo.brilhante.coinroutine.coins.data.FakeCoinsRemoteDataSource
import com.gustavo.brilhante.coinroutine.coins.domain.coin.Coin
import com.gustavo.brilhante.coinroutine.core.database.portifolio.PortfolioDatabase
import com.gustavo.brilhante.coinroutine.core.domain.Result
import com.gustavo.brilhante.coinroutine.portfolio.domain.PortfolioCoinModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class PortfolioRepositoryIntegrationTest {

    private lateinit var database: PortfolioDatabase
    private lateinit var repository: PortfolioRepositoryImpl
    private val remoteDataSource = FakeCoinsRemoteDataSource()

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PortfolioDatabase::class.java
        ).allowMainThreadQueries().build()
        
        repository = PortfolioRepositoryImpl(
            portfolioDao = database.portfolioDao(),
            userBalanceDao = database.userBalanceDao(),
            coinsRemoteDataSource = remoteDataSource
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `initializeBalance sets default balance when empty`() = runTest {
        repository.initializeBalance()
        val balance = repository.cashBalanceFlow().first()
        assertEquals(10000.0, balance)
    }

    @Test
    fun `savePortfolioCoin persists coin and allPortfolioCoinsFlow reflects it`() = runTest {
        val coinModel = PortfolioCoinModel(
            coin = Coin("bitcoin", "Bitcoin", "BTC", ""),
            performancePercent = 0.0,
            averagePurchasePrice = 50000.0,
            ownedAmountInUnit = 1.0,
            ownedAmountInFiat = 50000.0
        )
        
        repository.savePortfolioCoin(coinModel)
        
        val result = repository.allPortfolioCoinsFlow().first()
        assertIs<Result.Success<List<PortfolioCoinModel>>>(result)
        val coins = result.data
        assertEquals(1, coins.size)
        assertEquals("bitcoin", coins[0].coin.id)
    }

    @Test
    fun `updateCashBalance changes the balance`() = runTest {
        repository.initializeBalance()
        repository.updateCashBalance(5000.0)
        val balance = repository.cashBalanceFlow().first()
        assertEquals(5000.0, balance)
    }
}
