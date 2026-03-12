package com.gustavo.brilhante.coinroutine

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform