package com.gustavo.brilhante.coinroutine.coins.domain

import com.gustavo.brilhante.coinroutine.coins.domain.api.CoinsRemoteDataSource
import com.gustavo.brilhante.coinroutine.coins.domain.mapper.toCoinModel
import com.gustavo.brilhante.coinroutine.coins.domain.model.CoinModel
import com.gustavo.brilhante.coinroutine.core.domain.DataError
import com.gustavo.brilhante.coinroutine.core.domain.Result
import com.gustavo.brilhante.coinroutine.core.domain.map

class GetCoinDetailsUseCase(
    private val client: CoinsRemoteDataSource,
) {
    suspend fun execute(coinId: String): Result<CoinModel, DataError.Remote> {
        return client.getCoinById(coinId).map { dto ->
            dto.data.coin.toCoinModel()
        }
    }
}