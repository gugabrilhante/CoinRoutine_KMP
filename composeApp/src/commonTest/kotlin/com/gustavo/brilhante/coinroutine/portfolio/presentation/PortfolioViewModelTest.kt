package com.gustavo.brilhante.coinroutine.portfolio.presentation

import androidx.lifecycle.viewModelScope
import app.cash.turbine.test
import com.gustavo.brilhante.coinroutine.core.domain.DataError
import com.gustavo.brilhante.coinroutine.core.util.formatFiat
import com.gustavo.brilhante.coinroutine.core.util.toUiText
import com.gustavo.brilhante.coinroutine.portfolio.data.FakePortfolioRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PortfolioViewModelTest {

    @Test
    fun `initial state shows empty portfolio with full cash balance`() = runTest {
        val dispatcher = StandardTestDispatcher(this.testScheduler)
        Dispatchers.setMain(dispatcher)
        val repository = FakePortfolioRepository()
        val viewModel = PortfolioViewModel(repository, dispatcher)

        viewModel.state.test {
            skipItems(1) // skip initialValue (isLoading=true) emitted before combine runs
            val state = awaitItem()
            assertTrue(state.coins.isEmpty())
            assertFalse(state.isLoading)
            assertNull(state.error)
            assertEquals(formatFiat(FakePortfolioRepository.cashBalance), state.portfolioValue)
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.viewModelScope.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun `coin added to portfolio appears in state`() = runTest {
        val dispatcher = StandardTestDispatcher(this.testScheduler)
        Dispatchers.setMain(dispatcher)
        val repository = FakePortfolioRepository()
        val viewModel = PortfolioViewModel(repository, dispatcher)

        viewModel.state.test {
            skipItems(1) // skip initialValue
            awaitItem()  // real empty state

            repository.savePortfolioCoin(FakePortfolioRepository.portfolioCoin)

            // StateFlow de-duplication means only 1 observable emission after save
            val updatedState = awaitItem()
            assertTrue(updatedState.coins.isNotEmpty())
            assertFalse(updatedState.isLoading)
            assertEquals(FakePortfolioRepository.portfolioCoin.coin.id, updatedState.coins.first().id)
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.viewModelScope.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun `total balance increases when a coin is added to the portfolio`() = runTest {
        val dispatcher = StandardTestDispatcher(this.testScheduler)
        Dispatchers.setMain(dispatcher)
        val repository = FakePortfolioRepository()
        val viewModel = PortfolioViewModel(repository, dispatcher)

        viewModel.state.test {
            skipItems(1) // skip initialValue
            val initialState = awaitItem()
            assertEquals(formatFiat(FakePortfolioRepository.cashBalance), initialState.portfolioValue)

            // Add a coin worth $1,000 → total = cash ($10,000) + coin ($1,000) = $11,000
            repository.savePortfolioCoin(
                FakePortfolioRepository.portfolioCoin.copy(
                    ownedAmountInUnit = 50.0,
                    ownedAmountInFiat = 1000.0
                )
            )

            val updatedState = awaitItem()
            assertEquals(formatFiat(11000.0), updatedState.portfolioValue)
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.viewModelScope.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun `showBuyButton is true when portfolio has coins and false when empty`() = runTest {
        val dispatcher = StandardTestDispatcher(this.testScheduler)
        Dispatchers.setMain(dispatcher)
        val repository = FakePortfolioRepository()
        val viewModel = PortfolioViewModel(repository, dispatcher)

        viewModel.state.test {
            skipItems(1) // skip initialValue
            val emptyState = awaitItem()
            assertFalse(emptyState.showBuyButton)

            repository.savePortfolioCoin(FakePortfolioRepository.portfolioCoin)

            val stateWithCoin = awaitItem()
            assertTrue(stateWithCoin.showBuyButton)
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.viewModelScope.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun `error state is set and loading is false when repository emits an error`() = runTest {
        val dispatcher = StandardTestDispatcher(this.testScheduler)
        Dispatchers.setMain(dispatcher)
        val repository = FakePortfolioRepository().apply { simulateError() }
        val viewModel = PortfolioViewModel(repository, dispatcher)

        viewModel.state.test {
            skipItems(1) // skip initialValue
            val errorState = awaitItem()
            assertFalse(errorState.isLoading)
            assertNotNull(errorState.error)
            assertEquals(DataError.Remote.SERVER.toUiText(), errorState.error)
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.viewModelScope.cancel()
        Dispatchers.resetMain()
    }

}
