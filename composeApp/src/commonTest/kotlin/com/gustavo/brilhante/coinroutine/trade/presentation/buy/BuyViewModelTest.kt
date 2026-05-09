package com.gustavo.brilhante.coinroutine.trade.presentation.buy

import androidx.lifecycle.viewModelScope
import app.cash.turbine.test
import com.gustavo.brilhante.coinroutine.coins.data.FakeCoinsRemoteDataSource
import com.gustavo.brilhante.coinroutine.coins.domain.GetCoinDetailsUseCase
import com.gustavo.brilhante.coinroutine.core.domain.DataError
import com.gustavo.brilhante.coinroutine.core.domain.Result
import com.gustavo.brilhante.coinroutine.portfolio.data.FakePortfolioRepository
import com.gustavo.brilhante.coinroutine.trade.domain.BuyCoinUseCase
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
 * Tests for [BuyViewModel].
 *p
 * Uses [StandardTestDispatcher] sharing [testScheduler] with [runTest] to keep
 * [viewModelScope] and the test coroutine on the same scheduler. Cancels
 * [viewModelScope] before [Dispatchers.resetMain] to avoid dispatch failures
 * during runTest cleanup.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BuyViewModelTest {

    private val coinId = FakeCoinsRemoteDataSource.defaultCoinDto.uuid

    @Test
    fun `coin details are loaded on initialization`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        val dataSource = FakeCoinsRemoteDataSource()
        val repository = FakePortfolioRepository()
        val viewModel = buildViewModel(dataSource, repository, dispatcher)
        val dto = FakeCoinsRemoteDataSource.defaultCoinDto

        viewModel.state.test {
            skipItems(1) // initial loading state

            val state = awaitItem()
            val coin = assertNotNull(state.coin)
            assertEquals(dto.name, coin.name)
            assertEquals(dto.symbol, coin.symbol)
            assertEquals(dto.price, coin.price)
            assertFalse(state.isLoading)
            assertNull(state.error)
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.viewModelScope.cancel()
        advanceUntilIdle()
        Dispatchers.resetMain()
    }

    @Test
    fun `available amount label reflects the current cash balance`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        val repository = FakePortfolioRepository().apply { setCashBalance(5000.0) }
        val viewModel = buildViewModel(FakeCoinsRemoteDataSource(), repository, dispatcher)

        viewModel.state.test {
            skipItems(1)
            val state = awaitItem()
            assertTrue(
                state.availableAmount.isNotBlank(),
                "availableAmount should display the formatted cash balance"
            )
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.viewModelScope.cancel()
        advanceUntilIdle()
        Dispatchers.resetMain()
    }

    @Test
    fun `error is set and coin is null when coin details fetch fails`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        val dataSource = FakeCoinsRemoteDataSource().apply {
            coinDetailsResult = Result.Error(DataError.Remote.NO_INTERNET)
        }
        val viewModel = buildViewModel(dataSource, FakePortfolioRepository(), dispatcher)

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
    fun `amount change is reflected in state`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        val viewModel = buildViewModel(FakeCoinsRemoteDataSource(), FakePortfolioRepository(), dispatcher)

        viewModel.state.test {
            skipItems(1)
            awaitItem() // coin loaded

            viewModel.onAmountChanged("250")

            val state = awaitItem()
            assertEquals("250", state.amount)
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.viewModelScope.cancel()
        advanceUntilIdle()
        Dispatchers.resetMain()
    }

    @Test
    fun `BuySuccess event is emitted after a successful purchase`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        val repository = FakePortfolioRepository().apply {
            getPortfolioCoinResult = Result.Success(null)
        }
        val viewModel = buildViewModel(FakeCoinsRemoteDataSource(), repository, dispatcher)

        viewModel.state.test {
            skipItems(1)
            awaitItem() // coin loaded

            viewModel.onAmountChanged("100")
            skipItems(1) // amount change emission

            viewModel.onBuyClicked()
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.events.test {
            assertEquals(BuyEvents.BuySuccess, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.viewModelScope.cancel()
        advanceUntilIdle()
        Dispatchers.resetMain()
    }

    @Test
    fun `error state is set when purchase fails due to insufficient funds`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        val repository = FakePortfolioRepository().apply {
            setCashBalance(0.01) // too little to buy anything
            getPortfolioCoinResult = Result.Success(null)
        }
        val viewModel = buildViewModel(FakeCoinsRemoteDataSource(), repository, dispatcher)

        viewModel.state.test {
            skipItems(1)
            awaitItem() // coin loaded

            viewModel.onAmountChanged("500")
            skipItems(1)

            viewModel.onBuyClicked()

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
        dispatcher: CoroutineDispatcher,
    ) = BuyViewModel(
        getCoinDetailsUseCase = GetCoinDetailsUseCase(dataSource),
        portfolioRepository = repository,
        buyCoinUseCase = BuyCoinUseCase(repository),
        coinId = coinId,
        coroutineDispatcher = dispatcher,
    )
}
