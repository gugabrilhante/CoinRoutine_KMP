package com.gustavo.brilhante.coinroutine.coins.domain.mapper

import com.gustavo.brilhante.coinroutine.coins.data.remote.dto.CoinItemDto
import com.gustavo.brilhante.coinroutine.coins.data.remote.dto.CoinPriceDto
import com.gustavo.brilhante.coinroutine.coins.domain.coin.Coin
import com.gustavo.brilhante.coinroutine.coins.domain.model.CoinModel
import com.gustavo.brilhante.coinroutine.coins.domain.model.PriceModel

fun CoinItemDto.toCoinModel() = CoinModel(
    coin = Coin(
        id = uuid,
        name = name,
        symbol = symbol,
        iconUrl = iconUrl,
    ),
    price = price,
    change = change,
)

fun CoinPriceDto.toPriceModel() = PriceModel(
    price = price ?: 0.0,
    timestamp = timestamp,
)