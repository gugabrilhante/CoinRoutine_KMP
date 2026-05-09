package com.gustavo.brilhante.coinroutine.portfolio.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.gustavo.brilhante.coinroutine.coins.data.FakeCoinsRemoteDataSource
import com.gustavo.brilhante.coinroutine.coins.data.remote.dto.CoinsListDto
import com.gustavo.brilhante.coinroutine.coins.data.remote.dto.CoinsResponseDto
import com.gustavo.brilhante.coinroutine.coins.domain.coin.Coin
import com.gustavo.brilhante.coinroutine.core.database.portifolio.PortfolioDatabase
import com.gustavo.brilhante.coinroutine.core.domain.DataError
import com.gustavo.brilhante.coinroutine.core.domain.Result
import com.gustavo.brilhante.coinroutine.core.util.FakeClockProvider
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
import kotlin.test.assertNull
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
            coinsRemoteDataSource = remoteDataSource,
            clockProvider = FakeClockProvider(fixedTime = 1_700_000_000_000L),
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    // ── initializeBalance ────────────────────────────────────────────────────

    @Test
    fun `initializeBalance sets default balance when empty`() = runTest {
        repository.initializeBalance()
        assertEquals(10000.0, repository.cashBalanceFlow().first())
    }

    @Test
    fun `initializeBalance does not overwrite an existing balance`() = runTest {
        repository.initializeBalance()
        repository.updateCashBalance(5000.0)
        repository.initializeBalance() // called a second time
        assertEquals(5000.0, repository.cashBalanceFlow().first())
    }

    // ── updateCashBalance / cashBalanceFlow ──────────────────────────────────

    @Test
    fun `updateCashBalance changes the balance`() = runTest {
        repository.initializeBalance()
        repository.updateCashBalance(5000.0)
        assertEquals(5000.0, repository.cashBalanceFlow().first())
    }

    // ── savePortfolioCoin / allPortfolioCoinsFlow ────────────────────────────

    @Test
    fun `savePortfolioCoin persists coin and allPortfolioCoinsFlow reflects it`() = runTest {
        val coinModel = buildPortfolioCoin("bitcoin")
        remoteDataSource.coinsListResult = Result.Success(
            CoinsResponseDto(data = CoinsListDto(coins = listOf(FakeCoinsRemoteDataSource.defaultCoinDto)))
        )

        repository.savePortfolioCoin(coinModel)

        val result = repository.allPortfolioCoinsFlow().first()
        assertIs<Result.Success<List<PortfolioCoinModel>>>(result)
        assertEquals(1, result.data.size)
        assertEquals("bitcoin", result.data[0].coin.id)
    }

    @Test
    fun `allPortfolioCoinsFlow returns empty list when portfolio is empty`() = runTest {
        val result = repository.allPortfolioCoinsFlow().first()
        assertIs<Result.Success<List<PortfolioCoinModel>>>(result)
        assertTrue(result.data.isEmpty())
    }

    @Test
    fun `allPortfolioCoinsFlow returns error when remote fails and portfolio is not empty`() = runTest {
        repository.savePortfolioCoin(buildPortfolioCoin("bitcoin"))
        remoteDataSource.coinsListResult = Result.Error(DataError.Remote.NO_INTERNET)

        val result = repository.allPortfolioCoinsFlow().first()
        assertIs<Result.Error<DataError.Remote>>(result)
        assertEquals(DataError.Remote.NO_INTERNET, result.error)
    }

    // ── removeCoinFromPortfolio ──────────────────────────────────────────────

    @Test
    fun `removeCoinFromPortfolio removes the coin and flow emits empty list`() = runTest {
        remoteDataSource.coinsListResult = Result.Success(
            CoinsResponseDto(data = CoinsListDto(coins = listOf(FakeCoinsRemoteDataSource.defaultCoinDto)))
        )
        repository.savePortfolioCoin(buildPortfolioCoin("bitcoin"))
        repository.removeCoinFromPortfolio("bitcoin")

        val result = repository.allPortfolioCoinsFlow().first()
        assertIs<Result.Success<List<PortfolioCoinModel>>>(result)
        assertTrue(result.data.isEmpty())
    }

    // ── getPortfolioCoin ─────────────────────────────────────────────────────

    @Test
    fun `getPortfolioCoin returns null when coin not in local db`() = runTest {
        val result = repository.getPortfolioCoin(FakeCoinsRemoteDataSource.defaultCoinDto.uuid)
        assertIs<Result.Success<PortfolioCoinModel?>>(result)
        assertNull(result.data)
    }

    @Test
    fun `getPortfolioCoin returns coin when it exists in local db and remote succeeds`() = runTest {
        val coinId = FakeCoinsRemoteDataSource.defaultCoinDto.uuid
        repository.savePortfolioCoin(buildPortfolioCoin(coinId))

        val result = repository.getPortfolioCoin(coinId)
        assertIs<Result.Success<PortfolioCoinModel?>>(result)
        assertEquals(coinId, result.data?.coin?.id)
    }

    @Test
    fun `getPortfolioCoin returns error when remote fails`() = runTest {
        remoteDataSource.coinDetailsResult = Result.Error(DataError.Remote.NO_INTERNET)

        val result = repository.getPortfolioCoin("bitcoin")
        assertIs<Result.Error<DataError.Remote>>(result)
        assertEquals(DataError.Remote.NO_INTERNET, result.error)
    }

    // ── totalBalanceFlow ─────────────────────────────────────────────────────

    @Test
    fun `totalBalanceFlow returns cash balance when portfolio is empty`() = runTest {
        repository.initializeBalance()

        val result = repository.totalBalanceFlow().first()
        assertIs<Result.Success<Double>>(result)
        assertEquals(10000.0, result.data)
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private fun buildPortfolioCoin(coinId: String) = PortfolioCoinModel(
        coin = Coin(coinId, "Coin $coinId", "SYM", ""),
        performancePercent = 0.0,
        averagePurchasePrice = 50000.0,
        ownedAmountInUnit = 1.0,
        ownedAmountInFiat = 50000.0
    )
}
