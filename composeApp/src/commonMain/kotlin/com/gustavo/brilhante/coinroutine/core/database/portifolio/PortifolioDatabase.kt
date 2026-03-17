package com.gustavo.brilhante.coinroutine.core.database.portifolio

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import com.gustavo.brilhante.coinroutine.portfolio.data.local.PortfolioCoinEntity
import com.gustavo.brilhante.coinroutine.portfolio.data.local.PortfolioDao
import com.gustavo.brilhante.coinroutine.portfolio.data.local.UserBalanceDao
import com.gustavo.brilhante.coinroutine.portfolio.data.local.UserBalanceEntity

@Database(entities = [PortfolioCoinEntity::class, UserBalanceEntity::class], version = 2)
@ConstructedBy(PortfolioDatabaseCreator::class)
abstract class PortfolioDatabase: RoomDatabase() {
    abstract fun portfolioDao(): PortfolioDao
    abstract fun userBalanceDao(): UserBalanceDao
}