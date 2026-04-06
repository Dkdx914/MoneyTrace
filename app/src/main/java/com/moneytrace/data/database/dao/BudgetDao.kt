package com.moneytrace.data.database.dao

import androidx.room.*
import com.moneytrace.data.database.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: BudgetEntity): Long

    @Update
    suspend fun update(budget: BudgetEntity)

    @Delete
    suspend fun delete(budget: BudgetEntity)

    @Query("SELECT * FROM budgets WHERE year = :year AND month = :month ORDER BY categoryId ASC NULLS FIRST")
    fun getBudgetsByMonth(year: Int, month: Int): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE year = :year AND month = :month AND categoryId IS NULL LIMIT 1")
    suspend fun getTotalBudget(year: Int, month: Int): BudgetEntity?
}
