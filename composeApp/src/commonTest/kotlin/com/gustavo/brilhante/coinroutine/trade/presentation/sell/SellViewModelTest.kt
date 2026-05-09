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
 *
 * Uses [StandardTestDispatcher] sharing [testScheduler] with [runTest] to keep
 * [viewModelScope] and the test coroutine on the same scheduler. Cancels
 * [viewModelScope] before [Dispatchers.resetMain] to avoid dispatch failures
 * during runTest cleanup.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SellViewModelTest {

    private val coinId = FakeCoinsRemoteDataSource.defaultCoinDto.uuid

    private fun repositoryWithOwnedCoin() = FakePortfolioRepository().apply {
        getPortfolioCoinResult = Result.Success(FakePortfolioRepository.portfolioCoin)
    }

    @Test
    fun `coin details are loaded with available amount on initialization`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val viewModel = buildViewModel(FakeCoinsRemoteDataSource(), repositoryWithOwnedCoin())
        val dto = FakeCoinsRemoteDataSource.defaultCoinDto

        viewModel.state.test {
            skipItems(1) // initial loading state

            val state = awaitItem()
            val coin = assertNotNull(state.coin)
            assertEquals(dto.name, coin.name)
            assertFalse(state.isLoading)
            assertNull(state.error)
            assertTrue(
                state.availableAmount.isNotBlank(),
                "availableAmount should reflect the owned fiat value"
            )
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.viewModelScope.cancel()
        advanceUntilIdle()
        Dispatchers.resetMain()
    }

    @Test
    fun `error is set when coin details fetch fails`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val dataSource = FakeCoinsRemoteDataSource().apply {
            coinDetailsResult = Result.Error(DataError.Remote.SERVER)
        }
        val viewModel = buildViewModel(dataSource, repositoryWithOwnedCoin())

        viewModel.state.test {
            skipItems(1)
            val state = awaitItem()
            assertNull(state.coin)
            assertNotNull(state.error)
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.viewModelScope.cancel()
        advanceUntilIdle()
        Dispatchers.resetMain()
    }

    @Test
    fun `error is set when portfolio coin fetch fails`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repository = FakePortfolioRepository().apply {
            getPortfolioCoinResult = Result.Error(DataError.Remote.NO_INTERNET)
        }
        val viewModel = buildViewModel(FakeCoinsRemoteDataSource(), repository)

        viewModel.state.test {
            skipItems(1)
            val state = awaitItem()
            assertNotNull(state.error)
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.viewModelScope.cancel()
        advanceUntilIdle()
        Dispatchers.resetMain()
    }

    @Test
    fun `amount change is reflected in state`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val viewModel = buildViewModel(FakeCoinsRemoteDataSource(), repositoryWithOwnedCoin())

        viewModel.state.test {
            skipItems(1)
            awaitItem() // coin loaded

            viewModel.onAmountChanged("150")

            val state = awaitItem()
            assertEquals("150", state.amount)
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.viewModelScope.cancel()
        advanceUntilIdle()
        Dispatchers.resetMain()
    }

    @Test
    fun `SellSuccess event is emitted after a successful sale`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        // portfolioCoin: ownedAmountInUnit = 1000.0, selling $100 at $50,000 → 0.002 units ≪ owned
        val viewModel = buildViewModel(FakeCoinsRemoteDataSource(), repositoryWithOwnedCoin())

        viewModel.state.test {
            skipItems(1)
            awaitItem() // coin loaded

            viewModel.onAmountChanged("100")
            skipItems(1) // amount change

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
    fun `error state is set when selling more units than owned`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        // Own 0.001 BTC. Selling $200 at $50,000 = 0.004 units > 0.001 owned → error.
        val smallHolding = PortfolioCoinModel(
            coin = FakePortfolioRepository.fakeCoin,
            ownedAmountInUnit = 0.001,
            ownedAmountInFiat = 50.0,
            performancePercent = 0.0,
            averagePurchasePrice = 50000.0,
        )
        val repository = FakePortfolioRepository().apply {
            getPortfolioCoinResult = Result.Success(smallHolding)
        }
        val viewModel = buildViewModel(FakeCoinsRemoteDataSource(), repository)

        viewModel.state.test {
            skipItems(1)
            awaitItem()

            viewModel.onAmountChanged("200")
            skipItems(1)

            viewModel.onSellClicked()

            val errorState = awaitItem()
            assertNotNull(errorState.error)
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.viewModelScope.cancel()
        advanceUntilIdle()
        Dispatchers.resetMain()
    }

    private fun buildViewModel(
        dataSource: FakeCoinsRemoteDataSource,
        repository: FakePortfolioRepository,
    ) = SellViewModel(
        getCoinDetailsUseCase = GetCoinDetailsUseCase(dataSource),
        portfolioRepository = repository,
        sellCoinUseCase = SellCoinUseCase(repository),
        coinId = coinId,
        coroutineDispatcher = StandardTestDispatcher(testScheduler),
    )
}
