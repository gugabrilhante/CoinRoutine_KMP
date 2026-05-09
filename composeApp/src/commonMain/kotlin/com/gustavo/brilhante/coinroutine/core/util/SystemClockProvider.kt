package com.gustavo.brilhante.coinroutine.core.util

import kotlin.time.Clock

class SystemClockProvider : ClockProvider {
    override fun nowEpochMilliseconds(): Long = Clock.System.now().toEpochMilliseconds()
}
