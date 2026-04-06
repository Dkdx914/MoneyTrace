package com.moneytrace.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 账户实体（现金/银行卡/支付宝/微信等）
 */
@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String,      // CASH / BANK / CREDIT / ALIPAY / WECHAT
    val balance: Double = 0.0,
    val icon: String,
    val color: String,
    val isDefault: Boolean = false
)
