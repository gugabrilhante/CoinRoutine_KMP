package com.gustavo.brilhante.coinroutine

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.gustavo.brilhante.coinroutine.coins.presentation.CoinsListScreen
import com.gustavo.brilhante.coinroutine.core.navigation.Buy
import com.gustavo.brilhante.coinroutine.core.navigation.Coins
import com.gustavo.brilhante.coinroutine.core.navigation.Portfolio
import com.gustavo.brilhante.coinroutine.core.navigation.Sell
import com.gustavo.brilhante.coinroutine.portfolio.presentation.PortfolioScreen
import com.gustavo.brilhante.coinroutine.theme.CoinRoutineTheme
import com.gustavo.brilhante.coinroutine.trade.presentation.buy.BuyScreen
import com.gustavo.brilhante.coinroutine.trade.presentation.sell.SellScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    val navController = rememberNavController()
    CoinRoutineTheme {
        NavHost(
            navController = navController,
            modifier = Modifier.fillMaxSize(),
            startDestination = Portfolio
        ){
            composable<Portfolio> {
                PortfolioScreen(
                    onCoinItemClicked = { coinId ->
                        navController.navigate(Sell(coinId))
                    },
                    onDiscoverCoinsClicked = {
                        navController.navigate(Coins)
                    }
                )
            }

            composable<Coins> {
                CoinsListScreen { coinId ->
                    navController.navigate(Buy(coinId))
                }
            }

            composable<Buy> { navBackStackEntry ->
                val coinId: String = navBackStackEntry.toRoute<Buy>().coinId
                BuyScreen(
                    coinId = coinId,
                    navigateToPortfolio = {
                        navController.navigate(Portfolio) {
                            popUpTo(Portfolio) { inclusive = true }
                        }
                    }
                )
            }

            composable<Sell> { navBackStackEntry ->
                val coinId: String = navBackStackEntry.toRoute<Sell>().coinId
                SellScreen(
                    coinId = coinId,
                    navigateToPortfolio = {
                        navController.navigate(Portfolio) {
                            popUpTo(Portfolio) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}