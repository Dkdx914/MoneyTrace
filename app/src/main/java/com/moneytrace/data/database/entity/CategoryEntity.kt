package com.moneytrace.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 分类实体
 */
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val icon: String,      // emoji图标
    val color: String,     // 十六进制颜色 #RRGGBB
    val type: String,      // EXPENSE / INCOME
    val sortOrder: Int = 0,
    val isCustom: Boolean = false
)
