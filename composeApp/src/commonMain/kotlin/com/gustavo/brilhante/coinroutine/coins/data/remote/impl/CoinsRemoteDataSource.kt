package com.gustavo.brilhante.coinroutine.coins.data.remote.impl

import com.gustavo.brilhante.coinroutine.coins.data.remote.dto.CoinDetailsResponseDto
import com.gustavo.brilhante.coinroutine.coins.data.remote.dto.CoinPriceHistoryResponseDto
import com.gustavo.brilhante.coinroutine.coins.data.remote.dto.CoinsResponseDto
import com.gustavo.brilhante.coinroutine.core.domain.DataError
import com.gustavo.brilhante.coinroutine.core.domain.Result

interface CoinsRemoteDataSource {

    suspend fun getListOfCoins(): Result<CoinsResponseDto, DataError.Remote>

    suspend fun getPriceHistory(coinId: String): Result<CoinPriceHistoryResponseDto, DataError.Remote>

    suspend fun getCoinById(coinId: String): Result<CoinDetailsResponseDto, DataError.Remote>
}