package com.gustavo.brilhante.coinroutine.coins.presentation

import androidx.lifecycle.viewModelScope
import app.cash.turbine.test
import com.gustavo.brilhante.coinroutine.coins.data.FakeCoinsRemoteDataSource
import com.gustavo.brilhante.coinroutine.coins.data.remote.dto.CoinsListDto
import com.gustavo.brilhante.coinroutine.coins.data.remote.dto.CoinsResponseDto
import com.gustavo.brilhante.coinroutine.coins.domain.GetCoinPriceHistoryUseCase
import com.gustavo.brilhante.coinroutine.coins.domain.GetCoinsListUseCase
import com.gustavo.brilhante.coinroutine.core.domain.DataError
import com.gustavo.brilhante.coinroutine.core.domain.Result
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
 * Tests for [CoinsListViewModel].
 *
 * ViewModels without an injectable dispatcher use [viewModelScope] which runs on
 * [Dispatchers.Main]. To control scheduling, we set [Dispatchers.Main] to a
 * [StandardTestDispatcher] that shares [testScheduler] with [runTest]. This lets
 * [awaitItem] suspend and yield control so that viewModelScope coroutines can
 * advance on the shared scheduler.
 *
 * Each test cancels [viewModelScope] before calling [Dispatchers.resetMain] to prevent
 * active viewModelScope coroutines from attempting to dispatch to the restored Android
 * main dispatcher during runTest cleanup.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CoinsListViewModelTest {

    @Test
    fun `coins are loaded and mapped on initialization`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val dataSource = FakeCoinsRemoteDataSource()
        val viewModel = buildViewModel(dataSource)
        val dto = FakeCoinsRemoteDataSource.defaultCoinDto

        viewModel.state.test {
            skipItems(1) // initial empty CoinsState
            val state = awaitItem()
            assertEquals(1, state.coins.size)
            assertEquals(dto.uuid, state.coins.first().id)
            assertEquals(dto.name, state.coins.first().name)
            assertNull(state.error)
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.viewModelScope.cancel()
        advanceUntilIdle()
        Dispatchers.resetMain()
    }

    @Test
    fun `positive price change sets isPositive to true`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        // defaultCoinDto has change = 2.5 (positive)
        val viewModel = buildViewModel(FakeCoinsRemoteDataSource())

        viewModel.state.test {
            skipItems(1)
            val state = awaitItem()
            assertTrue(state.coins.first().isPositive)
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.viewModelScope.cancel()
        advanceUntilIdle()
        Dispatchers.resetMain()
    }

    @Test
    fun `negative price change sets isPositive to false`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val dataSource = FakeCoinsRemoteDataSource().apply {
            coinsListResult = Result.Success(
                CoinsResponseDto(data = CoinsListDto(coins = listOf(FakeCoinsRemoteDataSource.secondCoinDto)))
            )
        }
        val viewModel = buildViewModel(dataSource)

        viewModel.state.test {
            skipItems(1)
            val state = awaitItem()
            assertFalse(state.coins.first().isPositive)
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.viewModelScope.cancel()
        advanceUntilIdle()
        Dispatchers.resetMain()
    }

    @Test
    fun `error state is set and coin list is empty on network failure`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val dataSource = FakeCoinsRemoteDataSource().apply {
            coinsListResult = Result.Error(DataError.Remote.NO_INTERNET)
        }
        val viewModel = buildViewModel(dataSource)

        viewModel.state.test {
            skipItems(1)
            val state = awaitItem()
            assertTrue(state.coins.isEmpty())
            assertNotNull(state.error)
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.viewModelScope.cancel()
        advanceUntilIdle()
        Dispatchers.resetMain()
    }

    @Test
    fun `price history loads and chartState is populated on coin long press`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val dataSource = FakeCoinsRemoteDataSource()
        val viewModel = buildViewModel(dataSource)

        viewModel.state.test {
            skipItems(1)
            awaitItem() // coins loaded

            viewModel.onCoinLongPressed(coinId = FakeCoinsRemoteDataSource.defaultCoinDto.uuid)

            val loadingState = awaitItem()
            val loadingChartState = assertNotNull(loadingState.chartState)
            assertTrue(loadingChartState.isLoading)

            val loadedState = awaitItem()
            val loadedChartState = assertNotNull(loadedState.chartState)
            assertFalse(loadedChartState.isLoading)
            assertEquals(3, loadedChartState.sparkLine.size)
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.viewModelScope.cancel()
        advanceUntilIdle()
        Dispatchers.resetMain()
    }

    @Test
    fun `price history sparkLine is sorted by timestamp ascending`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val dataSource = FakeCoinsRemoteDataSource()
        val viewModel = buildViewModel(dataSource)

        viewModel.state.test {
            skipItems(1)
            awaitItem()
            viewModel.onCoinLongPressed(FakeCoinsRemoteDataSource.defaultCoinDto.uuid)
            skipItems(1) // loading state

            val state = awaitItem()
            val chartState = assertNotNull(state.chartState)
            assertEquals(listOf(45000.0, 47000.0, 50000.0), chartState.sparkLine)
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.viewModelScope.cancel()
        advanceUntilIdle()
        Dispatchers.resetMain()
    }

    @Test
    fun `chartState is null after onDismissChart`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val dataSource = FakeCoinsRemoteDataSource()
        val viewModel = buildViewModel(dataSource)

        viewModel.state.test {
            skipItems(1)
            awaitItem()
            viewModel.onCoinLongPressed(FakeCoinsRemoteDataSource.defaultCoinDto.uuid)
            skipItems(2) // loading + loaded chart states

            viewModel.onDismissChart()

            val state = awaitItem()
            assertNull(state.chartState)
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.viewModelScope.cancel()
        advanceUntilIdle()
        Dispatchers.resetMain()
    }

    @Test
    fun `chartState has empty sparkLine when price history fetch fails`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val dataSource = FakeCoinsRemoteDataSource().apply {
            priceHistoryResult = Result.Error(DataError.Remote.SERVER)
        }
        val viewModel = buildViewModel(dataSource)

        viewModel.state.test {
            skipItems(1)
            awaitItem()
            viewModel.onCoinLongPressed(FakeCoinsRemoteDataSource.defaultCoinDto.uuid)
            skipItems(1)

            val state = awaitItem()
            val chartState = assertNotNull(state.chartState)
            assertFalse(chartState.isLoading)
            assertTrue(chartState.sparkLine.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.viewModelScope.cancel()
        advanceUntilIdle()
        Dispatchers.resetMain()
    }

    private fun buildViewModel(dataSource: FakeCoinsRemoteDataSource) = CoinsListViewModel(
        getCoinsListUseCase = GetCoinsListUseCase(dataSource),
        getCoinPriceHistoryUseCase = GetCoinPriceHistoryUseCase(dataSource),
        coroutineDispatcher = StandardTestDispatcher(testScheduler),
    )
}
