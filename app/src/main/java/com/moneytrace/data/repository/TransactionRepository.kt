package com.moneytrace.data.repository

import com.moneytrace.data.database.dao.*
import com.moneytrace.data.database.entity.*
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val accountDao: AccountDao,
    private val budgetDao: BudgetDao
) {
    fun getAllTransactions(): Flow<List<TransactionEntity>> =
        transactionDao.getAllTransactions()

    fun getTransactionsByMonth(year: Int, month: Int): Flow<List<TransactionEntity>> {
        val (start, end) = monthRange(year, month)
        return transactionDao.getTransactionsByMonth(start, end)
    }

    fun getMonthlyExpense(year: Int, month: Int): Flow<Double> {
        val (start, end) = monthRange(year, month)
        return transactionDao.getMonthlyExpense(start, end)
    }

    fun getMonthlyIncome(year: Int, month: Int): Flow<Double> {
        val (start, end) = monthRange(year, month)
        return transactionDao.getMonthlyIncome(start, end)
    }

    fun getTodayExpense(): Flow<Double> {
        val (start, end) = todayRange()
        return transactionDao.getTodayExpense(start, end)
    }

    fun getExpenseByCategory(year: Int, month: Int): Flow<List<CategorySum>> {
        val (start, end) = monthRange(year, month)
        return transactionDao.getExpenseByCategory(start, end)
    }

    fun getDailyStats(days: Int = 30): Flow<List<DailyStats>> {
        val startMs = System.currentTimeMillis() - days * 24 * 60 * 60 * 1000L
        return transactionDao.getDailyStats(startMs)
    }

    fun searchTransactions(keyword: String): Flow<List<TransactionEntity>> =
        transactionDao.searchTransactions(keyword)

    suspend fun addTransaction(transaction: TransactionEntity): Long =
        transactionDao.insert(transaction)

    suspend fun updateTransaction(transaction: TransactionEntity) =
        transactionDao.update(transaction)

    suspend fun deleteTransaction(id: Long) =
        transactionDao.softDelete(id)

    suspend fun getTransactionById(id: Long): TransactionEntity? =
        transactionDao.getById(id)

    // 分类
    fun getExpenseCategories(): Flow<List<CategoryEntity>> =
        categoryDao.getCategoriesByType("EXPENSE")

    fun getIncomeCategories(): Flow<List<CategoryEntity>> =
        categoryDao.getCategoriesByType("INCOME")

    fun getAllCategories(): Flow<List<CategoryEntity>> =
        categoryDao.getAllCategories()

    suspend fun getCategoryById(id: Long): CategoryEntity? =
        categoryDao.getById(id)

    // 账户
    fun getAllAccounts(): Flow<List<AccountEntity>> =
        accountDao.getAllAccounts()

    suspend fun getAccountById(id: Long): AccountEntity? =
        accountDao.getById(id)

    // 预算
    fun getBudgetsByMonth(year: Int, month: Int): Flow<List<BudgetEntity>> =
        budgetDao.getBudgetsByMonth(year, month)

    suspend fun saveBudget(budget: BudgetEntity) =
        budgetDao.insert(budget)

    suspend fun deleteBudget(budget: BudgetEntity) =
        budgetDao.delete(budget)

    // 工具方法
    private fun monthRange(year: Int, month: Int): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, 1, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        val end = cal.timeInMillis
        return start to end
    }

    private fun todayRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.add(Calendar.DAY_OF_MONTH, 1)
        return start to cal.timeInMillis
    }
}
