package com.gustavo.brilhante.coinroutine.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.gustavo.brilhante.coinroutine.theme.LightCoinRoutineColorsPalette
import com.gustavo.brilhante.coinroutine.theme.LocalCoinRoutineColorsPalette

@Composable
fun TestTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalCoinRoutineColorsPalette provides LightCoinRoutineColorsPalette,
    ) {
        MaterialTheme(content = content)
    }
}
