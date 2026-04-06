package com.moneytrace.di

import android.content.Context
import com.moneytrace.data.database.AppDatabase
import com.moneytrace.data.database.dao.*
import com.moneytrace.data.repository.TransactionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        AppDatabase.getInstance(context)

    @Provides
    fun provideTransactionDao(db: AppDatabase): TransactionDao = db.transactionDao()

    @Provides
    fun provideCategoryDao(db: AppDatabase): CategoryDao = db.categoryDao()

    @Provides
    fun provideAccountDao(db: AppDatabase): AccountDao = db.accountDao()

    @Provides
    fun provideBudgetDao(db: AppDatabase): BudgetDao = db.budgetDao()

    @Provides
    @Singleton
    fun provideRepository(
        transactionDao: TransactionDao,
        categoryDao: CategoryDao,
        accountDao: AccountDao,
        budgetDao: BudgetDao
    ): TransactionRepository = TransactionRepository(transactionDao, categoryDao, accountDao, budgetDao)
}
