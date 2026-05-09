package com.gustavo.brilhante.coinroutine.trade.presentation.sell

import androidx.lifecycle.viewModelScope
import app.cash.turbine.test
import com.gustavo.brilhante.coinroutine.coins.data.FakeCoinsRemoteDataSource
import com.gustavo.brilhante.coinroutine.coins.domain.GetCoinDetailsUseCase
import com.gustavo.brilhante.coinroutine.core.domain.DataError
import com.gustavo.brilhante.coinroutine.core.domain.Result
import com.gustavo.brilhante.coinroutine.portfolio.data.FakePortfolioRepository
import com.gustavo.brilhante.coinroutine.portfolio.domain.PortfolioCoinModel
import com.gustavo.brilhante.coinroutine.trade.domain.SellCoinUseCase
import com.gustavo.brilhante.coinroutine.trade.presentation.common.TradeState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for [SellViewModel].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SellViewModelTest {

    private val coinId = FakeCoinsRemoteDataSource.defaultCoinDto.uuid

    private val defaultDomainCoin = com.gustavo.brilhante.coinroutine.coins.domain.coin.Coin(
        id = FakeCoinsRemoteDataSource.defaultCoinDto.uuid,
        name = FakeCoinsRemoteDataSource.defaultCoinDto.name,
        symbol = FakeCoinsRemoteDataSource.defaultCoinDto.symbol,
        iconUrl = FakeCoinsRemoteDataSource.defaultCoinDto.iconUrl,
    )

    @Test
    fun `coin details are loaded on initialization`() = runTest {
        val dispatcher = StandardTestDispatcher(this.testScheduler)
        Dispatchers.setMain(dispatcher)
        val dataSource = FakeCoinsRemoteDataSource()
        val repository = FakePortfolioRepository().apply {
            getPortfolioCoinResult = Result.Success(
                PortfolioCoinModel(
                    coin = defaultDomainCoin,
                    ownedAmountInUnit = 1.0,
                    ownedAmountInFiat = 50000.0,
                    averagePurchasePrice = 50000.0,
                    performancePercent = 0.0
                )
            )
        }
        val viewModel = buildViewModel(dataSource, repository, dispatcher)
        val dto = FakeCoinsRemoteDataSource.defaultCoinDto

        viewModel.state.test {
            val state = awaitItemUntil { it.coin != null }
            val coin = assertNotNull(state.coin)
            assertEquals(dto.name, coin.name)
            assertFalse(state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.viewModelScope.cancel()
        advanceUntilIdle()
        Dispatchers.resetMain()
    }

    @Test
    fun `available amount label reflects owned units`() = runTest {
        val dispatcher = StandardTestDispatcher(this.testScheduler)
        Dispatchers.setMain(dispatcher)
        val units = 2.5
        val price = 50000.0
        val repository = FakePortfolioRepository().apply {
            getPortfolioCoinResult = Result.Success(
                PortfolioCoinModel(
                    coin = defaultDomainCoin,
                    ownedAmountInUnit = units,
                    ownedAmountInFiat = units * price,
                    averagePurchasePrice = price,
                    performancePercent = 0.0
                )
            )
        }
        val viewModel = buildViewModel(FakeCoinsRemoteDataSource(), repository, dispatcher)

        viewModel.state.test {
            // 2.5 units * $50,000 = $125,000
            val expectedFiat = com.gustavo.brilhante.coinroutine.core.util.formatFiat(units * price)
            val state = awaitItemUntil { it.availableAmount.contains(expectedFiat) }
            assertTrue(state.availableAmount.contains(expectedFiat))
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.viewModelScope.cancel()
        advanceUntilIdle()
        Dispatchers.resetMain()
    }

    @Test
    fun `SellSuccess event emitted on successful sale`() = runTest {
        val dispatcher = StandardTestDispatcher(this.testScheduler)
        Dispatchers.setMain(dispatcher)
        val repository = FakePortfolioRepository().apply {
            getPortfolioCoinResult = Result.Success(
                PortfolioCoinModel(
                    coin = defaultDomainCoin,
                    ownedAmountInUnit = 10.0,
                    ownedAmountInFiat = 500000.0,
                    averagePurchasePrice = 50000.0,
                    performancePercent = 0.0
                )
            )
        }
        val viewModel = buildViewModel(FakeCoinsRemoteDataSource(), repository, dispatcher)

        viewModel.state.test {
            awaitItemUntil { it.coin != null }
            viewModel.onAmountChanged("100")
            awaitItemUntil { it.amount == "100" }
            viewModel.onSellClicked()
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.events.test {
            assertEquals(SellEvents.SellSuccess, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.viewModelScope.cancel()
        advanceUntilIdle()
        Dispatchers.resetMain()
    }

    @Test
    fun `error state is set when selling more than owned`() = runTest {
        val dispatcher = StandardTestDispatcher(this.testScheduler)
        Dispatchers.setMain(dispatcher)
        val repository = FakePortfolioRepository().apply {
            getPortfolioCoinResult = Result.Success(
                PortfolioCoinModel(
                    coin = defaultDomainCoin,
                    ownedAmountInUnit = 0.001,
                    ownedAmountInFiat = 50.0,
                    averagePurchasePrice = 50000.0,
                    performancePercent = 0.0
                )
            )
        }
        val viewModel = buildViewModel(FakeCoinsRemoteDataSource(), repository, dispatcher)

        viewModel.state.test {
            awaitItemUntil { it.coin != null }
            viewModel.onAmountChanged("1000")
            awaitItemUntil { it.amount == "1000" }
            viewModel.onSellClicked()

            val state = awaitItemUntil { it.error != null }
            assertNotNull(state.error)
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.viewModelScope.cancel()
        advanceUntilIdle()
        Dispatchers.resetMain()
    }

    @Test
    fun `error is set when fetching coin details fails`() = runTest {
        val dispatcher = StandardTestDispatcher(this.testScheduler)
        Dispatchers.setMain(dispatcher)
        val dataSource = FakeCoinsRemoteDataSource().apply {
            coinDetailsResult = Result.Error(DataError.Remote.SERVER)
        }
        val repository = FakePortfolioRepository().apply {
            getPortfolioCoinResult = Result.Success(
                PortfolioCoinModel(
                    coin = defaultDomainCoin,
                    ownedAmountInUnit = 1.0,
                    ownedAmountInFiat = 50000.0,
                    averagePurchasePrice = 50000.0,
                    performancePercent = 0.0
                )
            )
        }
        val viewModel = buildViewModel(dataSource, repository, dispatcher)

        viewModel.state.test {
            val state = awaitItemUntil { it.error != null }
            assertNotNull(state.error)
            assertNull(state.coin)
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.viewModelScope.cancel()
        advanceUntilIdle()
        Dispatchers.resetMain()
    }

    private fun buildViewModel(
        dataSource: FakeCoinsRemoteDataSource,
        repository: FakePortfolioRepository,
        dispatcher: CoroutineDispatcher,
    ) = SellViewModel(
        getCoinDetailsUseCase = GetCoinDetailsUseCase(dataSource),
        portfolioRepository = repository,
        sellCoinUseCase = SellCoinUseCase(repository),
        coinId = coinId,
        coroutineDispatcher = dispatcher,
    )

    private suspend fun <T> app.cash.turbine.ReceiveTurbine<T>.awaitItemUntil(predicate: (T) -> Boolean): T {
        while (true) {
            val item = awaitItem()
            if (predicate(item)) return item
        }
    }
}
