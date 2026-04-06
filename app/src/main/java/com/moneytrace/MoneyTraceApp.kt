package com.moneytrace

import android.app.Application
import com.moneytrace.data.database.AppDatabase
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MoneyTraceApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // 触发数据库初始化（首次运行预填充默认数据）
        AppDatabase.getInstance(this)
    }
}
