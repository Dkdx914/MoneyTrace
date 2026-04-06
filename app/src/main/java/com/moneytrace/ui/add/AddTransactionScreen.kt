package com.moneytrace.ui.add

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
import com.moneytrace.data.database.entity.CategoryEntity
import com.moneytrace.data.database.entity.TransactionEntity
import com.moneytrace.viewmodel.MainViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    var selectedType by remember { mutableStateOf("EXPENSE") }
    var amountText by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableLongStateOf(0L) }
    var merchant by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedAccountId by remember { mutableLongStateOf(0L) }

    val expenseCategories by viewModel.expenseCategories.collectAsState()
    val incomeCategories by viewModel.incomeCategories.collectAsState()
    val accounts by viewModel.allAccounts.collectAsState()

    val categories = if (selectedType == "EXPENSE") expenseCategories else incomeCategories

    // 初始化默认选中
    LaunchedEffect(categories) {
        if (selectedCategoryId == 0L && categories.isNotEmpty()) {
            selectedCategoryId = categories.first().id
        }
    }
    LaunchedEffect(accounts) {
        if (selectedAccountId == 0L && accounts.isNotEmpty()) {
            selectedAccountId = accounts.first().id
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("记一笔", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val amount = amountText.toDoubleOrNull()
                            if (amount != null && amount > 0 && selectedCategoryId != 0L) {
                                viewModel.addTransaction(
                                    TransactionEntity(
                                        amount = amount,
                                        type = selectedType,
                                        categoryId = selectedCategoryId,
                                        accountId = selectedAccountId,
                                        merchant = merchant.trim(),
                                        note = note.trim(),
                                        source = "MANUAL"
                                    )
                                )
                                onBack()
                            }
                        }
                    ) {
                        Icon(Icons.Default.Check, "保存", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // 收支切换
            TypeSelector(
                selected = selectedType,
                onSelect = { selectedType = it; selectedCategoryId = 0L }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 金额输入
            AmountInput(
                value = amountText,
                onValueChange = { amountText = it }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 分类选择
            Text("选择分类", style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            CategoryGrid(
                categories = categories,
                selectedId = selectedCategoryId,
                onSelect = { selectedCategoryId = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 商家/备注/账户
            OutlinedTextField(
                value = merchant,
                onValueChange = { merchant = it },
                label = { Text("商家名称（可选）") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("备注（可选）") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // 账户选择
            var expanded by remember { mutableStateOf(false) }
            val selectedAccount = accounts.find { it.id == selectedAccountId }
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = selectedAccount?.let { "${it.icon} ${it.name}" } ?: "选择账户",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("账户") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    accounts.forEach { account ->
                        DropdownMenuItem(
                            text = { Text("${account.icon} ${account.name}") },
                            onClick = {
                                selectedAccountId = account.id
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TypeSelector(selected: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        listOf("EXPENSE" to "支出", "INCOME" to "收入").forEach { (type, label) ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (selected == type) MaterialTheme.colorScheme.primary
                        else Color.Transparent
                    )
                    .clickable { onSelect(type) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontWeight = FontWeight.Medium,
                    color = if (selected == type) Color.White
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AmountInput(value: String, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("¥", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedTextField(
            value = value,
            onValueChange = {
                // 只允许数字和一个小数点，最多两位小数
                if (it.isEmpty() || it.matches(Regex("""^\d{0,7}(\.\d{0,2})?$"""))) {
                    onValueChange(it)
                }
            },
            placeholder = { Text("0.00", fontSize = 36.sp) },
            textStyle = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent
            )
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.primary, thickness = 2.dp)
    }
}

@Composable
private fun CategoryGrid(
    categories: List<CategoryEntity>,
    selectedId: Long,
    onSelect: (Long) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        modifier = Modifier.heightIn(max = 200.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { cat ->
            val isSelected = cat.id == selectedId
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onSelect(cat.id) }
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = cat.icon, fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = cat.name.take(3),
                    fontSize = 10.sp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
