package com.gustavo.brilhante.coinroutine.di

import androidx.room.RoomDatabase
import com.gustavo.brilhante.coinroutine.coins.data.remote.impl.KtorCoinsRemoteDataSource
import com.gustavo.brilhante.coinroutine.coins.domain.GetCoinDetailsUseCase
import com.gustavo.brilhante.coinroutine.coins.domain.GetCoinPriceHistoryUseCase
import com.gustavo.brilhante.coinroutine.coins.domain.GetCoinsListUseCase
import com.gustavo.brilhante.coinroutine.coins.domain.api.CoinsRemoteDataSource
import com.gustavo.brilhante.coinroutine.coins.presentation.CoinsListViewModel
import com.gustavo.brilhante.coinroutine.core.database.portifolio.PortfolioDatabase
import com.gustavo.brilhante.coinroutine.core.database.portifolio.getPortfolioDatabase
import com.gustavo.brilhante.coinroutine.core.network.HttpClientFactory
import com.gustavo.brilhante.coinroutine.portfolio.data.PortfolioRepositoryImpl
import com.gustavo.brilhante.coinroutine.portfolio.domain.PortfolioRepository
import com.gustavo.brilhante.coinroutine.portfolio.presentation.PortfolioViewModel
import io.ktor.client.HttpClient
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.bind
import org.koin.dsl.module

fun initKoin(config: KoinAppDeclaration? = null) =
    startKoin {
        config?.invoke(this)
        modules(
            sharedModule,
            platformModule,
        )
    }


expect val platformModule: Module

val sharedModule = module {

    // core
    single<HttpClient> { HttpClientFactory.create(get()) }

    // portfolio
    single {
        getPortfolioDatabase(get<RoomDatabase.Builder<PortfolioDatabase>>())
    }
    singleOf(::PortfolioRepositoryImpl).bind<PortfolioRepository>()
    single { get<PortfolioDatabase>().portfolioDao() }
    single { get<PortfolioDatabase>().userBalanceDao() }
    viewModel { PortfolioViewModel(get()) }

    // coins list
    viewModel { CoinsListViewModel(get(), get()) }
    singleOf(::GetCoinsListUseCase)
    singleOf(::KtorCoinsRemoteDataSource).bind<CoinsRemoteDataSource>()
    singleOf(::GetCoinDetailsUseCase)
    singleOf(::GetCoinPriceHistoryUseCase)
}