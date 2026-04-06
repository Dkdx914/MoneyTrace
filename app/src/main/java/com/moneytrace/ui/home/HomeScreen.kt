package com.moneytrace.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.moneytrace.data.database.entity.TransactionEntity
import com.moneytrace.ui.theme.ExpenseRed
import com.moneytrace.ui.theme.IncomeGreen
import com.moneytrace.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onAddClick: () -> Unit,
    onTransactionClick: (TransactionEntity) -> Unit
) {
    val uiState by viewModel.homeUiState.collectAsState()
    val year by viewModel.currentYear.collectAsState()
    val month by viewModel.currentMonth.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 顶部月份选择器
        MonthSelector(
            year = year, month = month,
            onPrev = { viewModel.previousMonth() },
            onNext = { viewModel.nextMonth() }
        )

        // 收支概览卡片
        SummaryCard(
            expense = uiState.monthlyExpense,
            income = uiState.monthlyIncome,
            todayExpense = uiState.todayExpense
        )

        // 账单列表
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (uiState.transactions.isEmpty()) {
            EmptyState()
        } else {
            TransactionList(
                transactions = uiState.transactions,
                categories = uiState.categories,
                accounts = uiState.accounts,
                onItemClick = onTransactionClick
            )
        }
    }
}

@Composable
private fun MonthSelector(year: Int, month: Int, onPrev: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrev) {
            Icon(Icons.Default.KeyboardArrowLeft, "上月")
        }
        Text(
            text = "${year}年${month}月",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        IconButton(onClick = onNext) {
            Icon(Icons.Default.KeyboardArrowRight, "下月")
        }
    }
}

@Composable
private fun SummaryCard(expense: Double, income: Double, todayExpense: Double) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryItem(label = "本月支出", amount = expense, color = Color.White)
                SummaryItem(label = "本月收入", amount = income, color = Color(0xFFB2F5E5))
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "今日支出  ¥${String.format("%.2f", todayExpense)}",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun SummaryItem(label: String, amount: Double, color: Color) {
    Column {
        Text(text = label, color = color.copy(alpha = 0.8f), fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "¥${String.format("%.2f", amount)}",
            color = color,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun TransactionList(
    transactions: List<TransactionEntity>,
    categories: Map<Long, Any>,
    accounts: Map<Long, Any>,
    onItemClick: (TransactionEntity) -> Unit
) {
    // 按日期分组
    val grouped = transactions.groupBy { tx ->
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(tx.createdAt))
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        grouped.forEach { (day, txs) ->
            item {
                DayHeader(day = day, total = txs.filter { it.type == "EXPENSE" }.sumOf { it.amount })
            }
            items(txs, key = { it.id }) { tx ->
                TransactionItem(
                    transaction = tx,
                    categoryIcon = (categories[tx.categoryId] as? com.moneytrace.data.database.entity.CategoryEntity)?.icon ?: "📌",
                    categoryName = (categories[tx.categoryId] as? com.moneytrace.data.database.entity.CategoryEntity)?.name ?: "其他",
                    accountName = (accounts[tx.accountId] as? com.moneytrace.data.database.entity.AccountEntity)?.name ?: "",
                    onClick = { onItemClick(tx) }
                )
            }
        }
    }
}

@Composable
private fun DayHeader(day: String, total: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val label = when (day) {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) -> "今天"
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(System.currentTimeMillis() - 86400000L)) -> "昨天"
            else -> day.substring(5) // MM-dd
        }
        Text(text = label, style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = "-¥${String.format("%.2f", total)}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun TransactionItem(
    transaction: TransactionEntity,
    categoryIcon: String,
    categoryName: String,
    accountName: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(text = categoryIcon, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            // 信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.merchant.ifBlank { categoryName },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = buildString {
                        append(categoryName)
                        if (accountName.isNotBlank()) append(" · $accountName")
                        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(transaction.createdAt))
                        append(" · $time")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // 金额
            Text(
                text = "${if (transaction.type == "EXPENSE") "-" else "+"}¥${String.format("%.2f", transaction.amount)}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (transaction.type == "EXPENSE") ExpenseRed else IncomeGreen
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("💰", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text("本月暂无账单", style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("授权通知权限后，支付宝/微信付款将自动记录",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, start = 32.dp, end = 32.dp))
        }
    }
}
