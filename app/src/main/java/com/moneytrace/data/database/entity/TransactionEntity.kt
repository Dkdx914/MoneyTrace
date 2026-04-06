package com.moneytrace.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 账单交易实体（核心数据表）
 */
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_DEFAULT
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.SET_DEFAULT
        )
    ],
    indices = [
        Index("categoryId"),
        Index("accountId"),
        Index("createdAt")
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,                          // 金额（正数）
    val type: String,                            // EXPENSE / INCOME / TRANSFER
    val categoryId: Long = 1,                    // 分类ID
    val accountId: Long = 1,                     // 账户ID
    val merchant: String = "",                   // 商家名称
    val note: String = "",                       // 备注
    val source: String = "MANUAL",               // MANUAL / ALIPAY / WECHAT / SMS
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)
