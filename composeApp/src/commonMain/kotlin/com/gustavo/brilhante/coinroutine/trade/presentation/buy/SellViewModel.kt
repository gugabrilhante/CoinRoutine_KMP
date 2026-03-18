package com.gustavo.brilhante.coinroutine.trade.presentation.buy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gustavo.brilhante.coinroutine.coins.domain.GetCoinDetailsUseCase
import com.gustavo.brilhante.coinroutine.core.domain.Result
import com.gustavo.brilhante.coinroutine.core.util.formatFiat
import com.gustavo.brilhante.coinroutine.core.util.toUiText
import com.gustavo.brilhante.coinroutine.portfolio.domain.PortfolioRepository
import com.gustavo.brilhante.coinroutine.trade.domain.SellCoinUseCase
import com.gustavo.brilhante.coinroutine.trade.presentation.common.TradeState
import com.gustavo.brilhante.coinroutine.trade.presentation.common.UiTradeCoinItem
import com.gustavo.brilhante.coinroutine.trade.presentation.mapper.toCoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SellViewModel(
    private val getCoinDetailsUseCase: GetCoinDetailsUseCase,
    private val portfolioRepository: PortfolioRepository,
    private val sellCoinUseCase: SellCoinUseCase,
) : ViewModel() {

    private val tempCoinId = "1" // TODO: will be removed

    private val _amount = MutableStateFlow("")
    private val _state = MutableStateFlow(TradeState())
    val state = combine(
        _state,
        _amount,
    ) { state, amount ->
        state.copy(
            amount = amount
        )
    }.onStart {
        when (val portfolioCoinResponse = portfolioRepository.getPortfolioCoin(tempCoinId)) {
            is Result.Success -> {
                portfolioCoinResponse.data?.ownedAmountInUnit?.let {
                    getCoinDetails(it)
                }
            }

            is Result.Error -> {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = portfolioCoinResponse.error.toUiText()
                    )
                }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = TradeState(isLoading = true)
    )

    fun onAmountChanged(amount: String) {
        _amount.value = amount
    }

    private suspend fun getCoinDetails(ownedAmountInUnit: Double) {
        when (val coinResponse = getCoinDetailsUseCase.execute(tempCoinId)) {
            is Result.Success -> {
                val availableAmountInFiat = ownedAmountInUnit * coinResponse.data.price
                _state.update {
                    it.copy(
                        coin = UiTradeCoinItem(
                            id = coinResponse.data.coin.id,
                            name = coinResponse.data.coin.name,
                            symbol = coinResponse.data.coin.symbol,
                            iconUrl = coinResponse.data.coin.iconUrl,
                            price = coinResponse.data.price,
                        ),
                        availableAmount = "Available: ${formatFiat(availableAmountInFiat)}"
                    )
                }
            }

            is Result.Error -> {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = coinResponse.error.toUiText()
                    )
                }
            }
        }
    }

    fun onSellClicked() {
        val tradeCoin = state.value.coin ?: return
        viewModelScope.launch {
            val sellCoinResponse = sellCoinUseCase.sellCoin(
                coin = tradeCoin.toCoin(),
                amountInFiat = _amount.value.toDouble(),
                price = tradeCoin.price
            )
            when (sellCoinResponse) {
                is Result.Success -> {
                    // TODO: add event and navigation
                }

                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = sellCoinResponse.error.toUiText()
                        )
                    }
                }
            }
        }
    }
}