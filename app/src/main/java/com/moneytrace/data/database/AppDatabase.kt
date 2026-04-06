package com.moneytrace.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.moneytrace.data.database.dao.*
import com.moneytrace.data.database.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        AccountEntity::class,
        BudgetEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun accountDao(): AccountDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "money_trace.db"
                )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // 首次创建时预填充默认数据
                        INSTANCE?.let { database ->
                            CoroutineScope(Dispatchers.IO).launch {
                                prepopulate(database)
                            }
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun prepopulate(db: AppDatabase) {
            // 默认分类
            val defaultCategories = listOf(
                // 支出分类
                CategoryEntity(name = "餐饮美食", icon = "🍜", color = "#FF6B6B", type = "EXPENSE", sortOrder = 1),
                CategoryEntity(name = "交通出行", icon = "🚗", color = "#4ECDC4", type = "EXPENSE", sortOrder = 2),
                CategoryEntity(name = "购物消费", icon = "🛍️", color = "#45B7D1", type = "EXPENSE", sortOrder = 3),
                CategoryEntity(name = "娱乐休闲", icon = "🎮", color = "#96CEB4", type = "EXPENSE", sortOrder = 4),
                CategoryEntity(name = "医疗健康", icon = "💊", color = "#FFEAA7", type = "EXPENSE", sortOrder = 5),
                CategoryEntity(name = "教育学习", icon = "📚", color = "#DDA0DD", type = "EXPENSE", sortOrder = 6),
                CategoryEntity(name = "生活服务", icon = "🏠", color = "#98D8C8", type = "EXPENSE", sortOrder = 7),
                CategoryEntity(name = "金融理财", icon = "💰", color = "#F7DC6F", type = "EXPENSE", sortOrder = 8),
                CategoryEntity(name = "其他支出", icon = "📌", color = "#BDC3C7", type = "EXPENSE", sortOrder = 9),
                // 收入分类
                CategoryEntity(name = "工资薪资", icon = "💼", color = "#00C896", type = "INCOME", sortOrder = 1),
                CategoryEntity(name = "兼职收入", icon = "🔧", color = "#00B894", type = "INCOME", sortOrder = 2),
                CategoryEntity(name = "投资理财", icon = "📈", color = "#00CEC9", type = "INCOME", sortOrder = 3),
                CategoryEntity(name = "红包奖励", icon = "🧧", color = "#E17055", type = "INCOME", sortOrder = 4),
                CategoryEntity(name = "其他收入", icon = "✨", color = "#74B9FF", type = "INCOME", sortOrder = 5)
            )
            db.categoryDao().insertAll(defaultCategories)

            // 默认账户
            val defaultAccounts = listOf(
                AccountEntity(name = "支付宝", type = "ALIPAY", icon = "💙", color = "#1677FF", isDefault = true),
                AccountEntity(name = "微信", type = "WECHAT", icon = "💚", color = "#07C160"),
                AccountEntity(name = "现金", type = "CASH", icon = "💵", color = "#52C41A"),
                AccountEntity(name = "银行卡", type = "BANK", icon = "🏦", color = "#722ED1")
            )
            db.accountDao().insertAll(defaultAccounts)
        }
    }
}
