package com.gustavo.brilhante.coinroutine.appsekret

import dev.coinroutine.app.BuildConfig

actual object AppSecrets {
    actual val apiSecret: String
        get() = BuildConfig.API_BASE_URL
    actual val apiKey: String
        get() = BuildConfig.API_KEY
}