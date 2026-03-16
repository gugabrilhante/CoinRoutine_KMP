package com.gustavo.brilhante.coinroutine.coins.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gustavo.brilhante.coinroutine.coins.domain.GetCoinsListUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import com.gustavo.brilhante.coinroutine.core.domain.Result

class CoinsListViewModel(
    private val getCoinsListUseCase: GetCoinsListUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(CoinsState())
    val state = _state
        .onStart {
            getAllCoins()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CoinsState()
        )


    private suspend fun getAllCoins() {
        when(val coinsResponse = getCoinsListUseCase.execute()) {
            is Result.Success -> {
                _state.update {
                    CoinsState(
                        coins = coinsResponse.data.map { coinItem ->
                            UiCoinListItem(
                                id = coinItem.coin.id,
                                name = coinItem.coin.name,
                                iconUrl = coinItem.coin.iconUrl,
                                symbol = coinItem.coin.symbol,
                                formattedPrice = coinItem.price.toString(), //TODO: formatFiat(coinItem.price),
                                formattedChange = coinItem.change.toString(), //TODO: formatPercentage(coinItem.change),
                                isPositive = coinItem.change >= 0,
                            )
                        }
                    )
                }
            }
            is Result.Error -> {
                _state.update {
                    it.copy(
                        coins = emptyList(),
                        error = null //TODO: coinsResponse.error.toUiText()
                    )
                }
            }
        }
    }
}