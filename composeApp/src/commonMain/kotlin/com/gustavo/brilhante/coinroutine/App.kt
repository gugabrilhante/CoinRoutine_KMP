package com.gustavo.brilhante.coinroutine

import androidx.compose.runtime.*
import com.gustavo.brilhante.coinroutine.coins.presentation.CoinsListScreen
import com.gustavo.brilhante.coinroutine.theme.CoinRoutineTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    CoinRoutineTheme {

        CoinsListScreen {  }
    }
}