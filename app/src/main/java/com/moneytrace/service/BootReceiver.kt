package com.moneytrace.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * 开机广播：确保服务在重启后继续工作
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("MoneyTrace", "设备重启，通知监听服务将随系统自动恢复")
            // NotificationListenerService 在授权后由系统自动管理，无需手动启动
        }
    }
}
