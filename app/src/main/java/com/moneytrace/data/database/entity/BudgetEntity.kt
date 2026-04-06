package com.moneytrace.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 预算实体
 */
@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val categoryId: Long? = null,   // null 表示总预算
    val amount: Double,
    val period: String = "MONTHLY", // MONTHLY / WEEKLY / YEARLY
    val alertRatio: Double = 0.8,   // 达到80%时提醒
    val year: Int,
    val month: Int                  // 1-12
)
