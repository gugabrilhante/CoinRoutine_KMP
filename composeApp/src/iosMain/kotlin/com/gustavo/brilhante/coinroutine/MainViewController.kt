package com.gustavo.brilhante.coinroutine

import androidx.compose.ui.window.ComposeUIViewController
import com.gustavo.brilhante.coinroutine.di.initKoin

fun MainViewController() = ComposeUIViewController(
    configure = {
        initKoin()
    }
) { App() }