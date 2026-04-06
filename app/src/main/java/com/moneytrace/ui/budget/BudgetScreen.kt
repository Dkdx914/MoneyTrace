package com.moneytrace.ui.budget

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.moneytrace.data.database.entity.BudgetEntity
import com.moneytrace.data.repository.TransactionRepository
import com.moneytrace.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(viewModel: MainViewModel = hiltViewModel()) {
    val year by viewModel.currentYear.collectAsState()
    val month by viewModel.currentMonth.collectAsState()
    val expense by viewModel.homeUiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, "设置预算", tint = Color.White)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            item {
                Text(
                    "${year}年${month}月 · 预算管理",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 总预算卡片
            item {
                OverallBudgetCard(
                    spent = expense.monthlyExpense,
                    budget = 5000.0 // TODO: 从数据库读取
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Text("温馨提示", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("点击右下角 + 设置月度总预算，超出80%时会自动提醒",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }

    if (showAddDialog) {
        SetBudgetDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { amount ->
                // TODO: 保存预算到数据库
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun OverallBudgetCard(spent: Double, budget: Double) {
    val ratio = if (budget > 0) (spent / budget).toFloat().coerceIn(0f, 1f) else 0f
    val color = when {
        ratio >= 1.0f -> Color(0xFFFF6B6B)
        ratio >= 0.8f -> Color(0xFFFFB347)
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("月度总预算", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("¥${String.format("%.0f", budget)}", fontWeight = FontWeight.Bold, color = color)
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { ratio },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = color,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("已用 ¥${String.format("%.2f", spent)}", fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("剩余 ¥${String.format("%.2f", (budget - spent).coerceAtLeast(0.0))}",
                    fontSize = 13.sp, color = color)
            }
            if (ratio >= 0.8f) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (ratio >= 1f) "⚠️ 已超出本月预算！" else "⚠️ 已使用预算 ${String.format("%.0f", ratio * 100)}%，请注意消费",
                    color = color,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SetBudgetDialog(onDismiss: () -> Unit, onConfirm: (Double) -> Unit) {
    var input by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("设置月度预算") },
        text = {
            OutlinedTextField(
                value = input,
                onValueChange = { if (it.matches(Regex("""^\d{0,7}(\.\d{0,2})?$"""))) input = it },
                label = { Text("预算金额（元）") },
                prefix = { Text("¥") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        },
        confirmButton = {
            TextButton(onClick = {
                val amount = input.toDoubleOrNull()
                if (amount != null && amount > 0) onConfirm(amount)
            }) { Text("确定") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
