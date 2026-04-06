package com.moneytrace.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneytrace.data.database.entity.CategoryEntity
import com.moneytrace.data.database.entity.AccountEntity
import com.moneytrace.data.database.entity.TransactionEntity
import com.moneytrace.data.database.dao.CategorySum
import com.moneytrace.data.database.dao.DailyStats
import com.moneytrace.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

// ---------- UI State ----------
data class HomeUiState(
    val monthlyExpense: Double = 0.0,
    val monthlyIncome: Double = 0.0,
    val todayExpense: Double = 0.0,
    val transactions: List<TransactionEntity> = emptyList(),
    val categories: Map<Long, CategoryEntity> = emptyMap(),
    val accounts: Map<Long, AccountEntity> = emptyMap(),
    val isLoading: Boolean = false
)

data class StatsUiState(
    val categoryStats: List<CategorySum> = emptyList(),
    val dailyStats: List<DailyStats> = emptyList(),
    val categories: Map<Long, CategoryEntity> = emptyMap(),
    val totalExpense: Double = 0.0
)

data class BudgetUiState(
    val totalBudget: Double = 0.0,
    val totalExpense: Double = 0.0,
    val categoryBudgets: List<CategoryBudgetItem> = emptyList()
)

data class CategoryBudgetItem(
    val category: CategoryEntity,
    val budget: Double,
    val spent: Double,
    val budgetId: Long
)

// ---------- ViewModel ----------
@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    private val now = Calendar.getInstance()
    private val _currentYear = MutableStateFlow(now.get(Calendar.YEAR))
    private val _currentMonth = MutableStateFlow(now.get(Calendar.MONTH) + 1)
    val currentYear: StateFlow<Int> = _currentYear.asStateFlow()
    val currentMonth: StateFlow<Int> = _currentMonth.asStateFlow()

    // 首页状态
    val homeUiState: StateFlow<HomeUiState> = combine(
        _currentYear,
        _currentMonth
    ) { year, month -> year to month }
        .flatMapLatest { (year, month) ->
            combine(
                repository.getMonthlyExpense(year, month),
                repository.getMonthlyIncome(year, month),
                repository.getTodayExpense(),
                repository.getTransactionsByMonth(year, month),
                repository.getAllCategories(),
                repository.getAllAccounts()
            ) { expense, income, todayExp, txs, cats, accounts ->
                HomeUiState(
                    monthlyExpense = expense,
                    monthlyIncome = income,
                    todayExpense = todayExp,
                    transactions = txs,
                    categories = cats.associateBy { it.id },
                    accounts = accounts.associateBy { it.id }
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState(isLoading = true))

    // 统计页状态
    val statsUiState: StateFlow<StatsUiState> = combine(
        _currentYear,
        _currentMonth
    ) { year, month -> year to month }
        .flatMapLatest { (year, month) ->
            combine(
                repository.getExpenseByCategory(year, month),
                repository.getDailyStats(30),
                repository.getAllCategories(),
                repository.getMonthlyExpense(year, month)
            ) { catStats, daily, cats, total ->
                StatsUiState(
                    categoryStats = catStats,
                    dailyStats = daily,
                    categories = cats.associateBy { it.id },
                    totalExpense = total
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatsUiState())

    // 搜索
    private val _searchQuery = MutableStateFlow("")
    val searchResults: StateFlow<List<TransactionEntity>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) flowOf(emptyList())
            else repository.searchTransactions(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearch(query: String) {
        _searchQuery.value = query
    }

    // 月份切换
    fun previousMonth() {
        val cal = Calendar.getInstance().apply {
            set(_currentYear.value, _currentMonth.value - 1, 1)
            add(Calendar.MONTH, -1)
        }
        _currentYear.value = cal.get(Calendar.YEAR)
        _currentMonth.value = cal.get(Calendar.MONTH) + 1
    }

    fun nextMonth() {
        val cal = Calendar.getInstance().apply {
            set(_currentYear.value, _currentMonth.value - 1, 1)
            add(Calendar.MONTH, 1)
        }
        _currentYear.value = cal.get(Calendar.YEAR)
        _currentMonth.value = cal.get(Calendar.MONTH) + 1
    }

    fun goToCurrentMonth() {
        val now2 = Calendar.getInstance()
        _currentYear.value = now2.get(Calendar.YEAR)
        _currentMonth.value = now2.get(Calendar.MONTH) + 1
    }

    // 增删改账单
    fun addTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.addTransaction(transaction)
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
        }
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
        }
    }

    // 分类和账户供UI使用
    val expenseCategories: StateFlow<List<CategoryEntity>> =
        repository.getExpenseCategories()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val incomeCategories: StateFlow<List<CategoryEntity>> =
        repository.getIncomeCategories()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAccounts: StateFlow<List<AccountEntity>> =
        repository.getAllAccounts()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
