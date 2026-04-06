package com.moneytrace.ui.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat

@Composable
fun SettingsScreen() {
    val context = LocalContext.current

    // 检查通知监听权限状态
    val hasNotificationPermission = remember {
        val enabledListeners = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        ) ?: ""
        enabledListeners.contains(context.packageName)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Text("设置", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 权限状态卡片
        item {
            PermissionStatusCard(
                hasPermission = hasNotificationPermission,
                onGrantClick = {
                    context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        item { SectionTitle("自动记账") }

        item {
            SettingsItem(
                icon = Icons.Default.Notifications,
                title = "通知访问权限",
                subtitle = if (hasNotificationPermission) "已授权 ✅" else "未授权，点击前往开启 ❌",
                onClick = { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }
            )
        }

        item {
            SettingsItem(
                icon = Icons.Default.ShoppingCart,
                title = "支持的支付平台",
                subtitle = "微信支付、支付宝、银行短信",
                onClick = {}
            )
        }

        item { SectionTitle("数据管理") }

        item {
            SettingsItem(
                icon = Icons.Default.Download,
                title = "备份数据",
                subtitle = "将账单数据备份到本地存储",
                onClick = { /* TODO */ }
            )
        }

        item {
            SettingsItem(
                icon = Icons.Default.Upload,
                title = "恢复数据",
                subtitle = "从备份文件恢复账单",
                onClick = { /* TODO */ }
            )
        }

        item { SectionTitle("关于") }

        item {
            SettingsItem(
                icon = Icons.Default.Info,
                title = "钱迹 MoneyTrace",
                subtitle = "版本 1.0.0 · 数据100%本地存储",
                onClick = {}
            )
        }

        item {
            SettingsItem(
                icon = Icons.Default.Security,
                title = "隐私说明",
                subtitle = "本应用不收集任何个人数据，账单仅存储在您的手机本地",
                onClick = {}
            )
        }
    }
}

@Composable
private fun PermissionStatusCard(hasPermission: Boolean, onGrantClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (hasPermission)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(if (hasPermission) "✅" else "⚠️", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (hasPermission) "自动记账已开启" else "需要开启通知访问权限",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = if (hasPermission)
                        "支付宝/微信付款后将自动记录账单"
                    else
                        "点击下方按钮开启，即可自动记录微信/支付宝付款",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!hasPermission) {
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onGrantClick, modifier = Modifier.wrapContentWidth()) {
                    Text("去开启")
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, title, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
