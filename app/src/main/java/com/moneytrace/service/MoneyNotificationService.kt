package com.moneytrace.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import com.moneytrace.MainActivity
import com.moneytrace.R
import com.moneytrace.data.database.AppDatabase
import com.moneytrace.data.database.entity.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * 核心通知监听服务
 * 监听微信/支付宝等支付通知，自动解析并记录账单
 */
class MoneyNotificationService : NotificationListenerService() {

    private val TAG = "MoneyNotificationSvc"
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val parsers: List<NotificationParser> = listOf(
        AlipayParser(),
        WeChatPayParser(),
        BankSmsParser()
    )

    private val categoryClassifier = CategoryClassifier()

    companion object {
        const val CHANNEL_ID = "money_trace_auto"
        const val CHANNEL_NAME = "自动记账通知"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        Log.i(TAG, "自动记账服务已启动")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName ?: return
        val extras = sbn.notification?.extras ?: return
        val title = extras.getString("android.title") ?: ""
        val content = extras.getCharSequence("android.text")?.toString() ?: ""

        if (title.isBlank() && content.isBlank()) return

        // 遍历所有解析器
        for (parser in parsers) {
            val payment = parser.parse(packageName, title, content) ?: continue
            handlePayment(payment)
            break
        }
    }

    private fun handlePayment(payment: ParsedPayment) {
        serviceScope.launch {
            val db = AppDatabase.getInstance(applicationContext)

            // 防重复：5秒内相同来源相同金额
            val since = System.currentTimeMillis() - 5000
            val duplicate = db.transactionDao().countRecentDuplicate(
                payment.source, payment.amount, since
            )
            if (duplicate > 0) {
                Log.d(TAG, "跳过重复账单: ${payment.amount}")
                return@launch
            }

            // 智能分类
            val categoryId = categoryClassifier.classify(
                payment.merchant, payment.amount, db
            )

            // 获取默认账户
            val accountId = when (payment.source) {
                "ALIPAY" -> db.accountDao().getAllAccounts().let {
                    // 查支付宝账户ID
                    var id = 1L
                    db.accountDao().getAllAccounts().collect { accounts ->
                        id = accounts.find { it.type == "ALIPAY" }?.id ?: 1L
                    }
                    id
                }
                "WECHAT" -> {
                    var id = 1L
                    db.accountDao().getAllAccounts().collect { accounts ->
                        id = accounts.find { it.type == "WECHAT" }?.id ?: 1L
                    }
                    id
                }
                else -> 1L
            }

            // 存入数据库
            val transaction = TransactionEntity(
                amount = payment.amount,
                type = payment.type,
                categoryId = categoryId,
                accountId = accountId,
                merchant = payment.merchant,
                source = payment.source
            )
            db.transactionDao().insert(transaction)

            Log.i(TAG, "自动记账: ${payment.merchant} ¥${payment.amount} [${payment.source}]")

            // 推送本地通知告知用户
            showRecordedNotification(payment)
        }
    }

    private fun showRecordedNotification(payment: ParsedPayment) {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val sourceLabel = when (payment.source) {
            "ALIPAY" -> "支付宝"
            "WECHAT" -> "微信"
            else -> "银行卡"
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("💰 已自动记账")
            .setContentText("$sourceLabel · ${payment.merchant} · -¥${String.format("%.2f", payment.amount)}")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "钱迹自动记账提醒"
        }
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }
}
