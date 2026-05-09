package com.gustavo.brilhante.coinroutine.core.util

class FakeClockProvider(private val fixedTime: Long = 1_000L) : ClockProvider {
    override fun nowEpochMilliseconds(): Long = fixedTime
}
