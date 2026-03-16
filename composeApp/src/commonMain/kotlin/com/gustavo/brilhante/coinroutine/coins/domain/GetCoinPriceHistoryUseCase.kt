package com.gustavo.brilhante.coinroutine.coins.domain

import com.gustavo.brilhante.coinroutine.coins.domain.api.CoinsRemoteDataSource
import com.gustavo.brilhante.coinroutine.coins.domain.mapper.toPriceModel
import com.gustavo.brilhante.coinroutine.coins.domain.model.PriceModel
import com.gustavo.brilhante.coinroutine.core.domain.Result
import com.gustavo.brilhante.coinroutine.core.domain.DataError
import com.gustavo.brilhante.coinroutine.core.domain.map

class GetCoinPriceHistoryUseCase(
    private val client: CoinsRemoteDataSource,
) {

    suspend fun execute(coinId: String): Result<List<PriceModel>, DataError.Remote> {
        return client.getPriceHistory(coinId).map { dto ->
            dto.data.history.map { it.toPriceModel() }
        }
    }
}