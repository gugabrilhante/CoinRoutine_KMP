package com.gustavo.brilhante.coinroutine.coins.domain

import com.gustavo.brilhante.coinroutine.coins.domain.api.CoinsRemoteDataSource
import com.gustavo.brilhante.coinroutine.coins.domain.mapper.toCoinModel
import com.gustavo.brilhante.coinroutine.coins.domain.model.CoinModel
import com.gustavo.brilhante.coinroutine.core.domain.Result
import com.gustavo.brilhante.coinroutine.core.domain.DataError
import com.gustavo.brilhante.coinroutine.core.domain.map

class GetCoinsListUseCase(
    private val client: CoinsRemoteDataSource,
) {
    suspend fun execute(): Result<List<CoinModel>, DataError.Remote> {
        return client.getListOfCoins().map { dto ->
            dto.data.coins.map { it.toCoinModel() }
        }
    }
}