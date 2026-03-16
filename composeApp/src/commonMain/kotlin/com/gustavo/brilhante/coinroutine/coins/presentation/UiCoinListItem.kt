package com.gustavo.brilhante.coinroutine.coins.presentation

data class UiCoinListItem(
    val id: String,
    val name: String,
    val symbol: String,
    val iconUrl: String,
    val formattedPrice: String,
    val formattedChange: String,
    val isPositive: Boolean,
)