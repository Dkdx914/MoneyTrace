package com.moneytrace.data.database.dao

import androidx.room.*
import com.moneytrace.data.database.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Query("UPDATE transactions SET isDeleted = 1, updatedAt = :time WHERE id = :id")
    suspend fun softDelete(id: Long, time: Long = System.currentTimeMillis())

    // 查询所有未删除的账单，按时间降序
    @Query("SELECT * FROM transactions WHERE isDeleted = 0 ORDER BY createdAt DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    // 按月查询
    @Query("""
        SELECT * FROM transactions 
        WHERE isDeleted = 0 
        AND createdAt >= :startMs 
        AND createdAt < :endMs 
        ORDER BY createdAt DESC
    """)
    fun getTransactionsByMonth(startMs: Long, endMs: Long): Flow<List<TransactionEntity>>

    // 本月支出总额
    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE isDeleted = 0 AND type = 'EXPENSE'
        AND createdAt >= :startMs AND createdAt < :endMs
    """)
    fun getMonthlyExpense(startMs: Long, endMs: Long): Flow<Double>

    // 本月收入总额
    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE isDeleted = 0 AND type = 'INCOME'
        AND createdAt >= :startMs AND createdAt < :endMs
    """)
    fun getMonthlyIncome(startMs: Long, endMs: Long): Flow<Double>

    // 今日支出
    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE isDeleted = 0 AND type = 'EXPENSE'
        AND createdAt >= :startMs AND createdAt < :endMs
    """)
    fun getTodayExpense(startMs: Long, endMs: Long): Flow<Double>

    // 按分类统计支出
    @Query("""
        SELECT categoryId, SUM(amount) as total FROM transactions
        WHERE isDeleted = 0 AND type = 'EXPENSE'
        AND createdAt >= :startMs AND createdAt < :endMs
        GROUP BY categoryId
        ORDER BY total DESC
    """)
    fun getExpenseByCategory(startMs: Long, endMs: Long): Flow<List<CategorySum>>

    // 按日统计（近30天）
    @Query("""
        SELECT strftime('%Y-%m-%d', createdAt/1000, 'unixepoch', 'localtime') as day,
               SUM(CASE WHEN type='EXPENSE' THEN amount ELSE 0 END) as expense,
               SUM(CASE WHEN type='INCOME' THEN amount ELSE 0 END) as income
        FROM transactions
        WHERE isDeleted = 0 AND createdAt >= :startMs
        GROUP BY day ORDER BY day ASC
    """)
    fun getDailyStats(startMs: Long): Flow<List<DailyStats>>

    // 搜索
    @Query("""
        SELECT * FROM transactions 
        WHERE isDeleted = 0 
        AND (merchant LIKE '%' || :keyword || '%' OR note LIKE '%' || :keyword || '%')
        ORDER BY createdAt DESC
        LIMIT 100
    """)
    fun searchTransactions(keyword: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionEntity?

    // 防重复：5秒内相同来源相同金额
    @Query("""
        SELECT COUNT(*) FROM transactions
        WHERE source = :source AND amount = :amount AND isDeleted = 0
        AND createdAt > :since
    """)
    suspend fun countRecentDuplicate(source: String, amount: Double, since: Long): Int
}

data class CategorySum(
    val categoryId: Long,
    val total: Double
)

data class DailyStats(
    val day: String,
    val expense: Double,
    val income: Double
)
