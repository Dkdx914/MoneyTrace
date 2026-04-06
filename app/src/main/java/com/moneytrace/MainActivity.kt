package com.moneytrace

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.moneytrace.ui.add.AddTransactionScreen
import com.moneytrace.ui.budget.BudgetScreen
import com.moneytrace.ui.home.HomeScreen
import com.moneytrace.ui.settings.SettingsScreen
import com.moneytrace.ui.stats.StatsScreen
import com.moneytrace.ui.theme.MoneyTraceTheme
import dagger.hilt.android.AndroidEntryPoint

sealed class Screen(val route: String, val label: String, val icon: ImageVector, val iconOutlined: ImageVector) {
    object Home : Screen("home", "账单", Icons.Filled.Home, Icons.Outlined.Home)
    object Stats : Screen("stats", "统计", Icons.Filled.BarChart, Icons.Outlined.BarChart)
    object Add : Screen("add", "记账", Icons.Filled.Add, Icons.Filled.Add)
    object Budget : Screen("budget", "预算", Icons.Filled.Wallet, Icons.Outlined.Wallet)
    object Settings : Screen("settings", "我的", Icons.Filled.Person, Icons.Outlined.Person)
}

val bottomNavItems = listOf(Screen.Home, Screen.Stats, Screen.Budget, Screen.Settings)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MoneyTraceTheme {
                MoneyTraceApp()
            }
        }
    }
}

@Composable
fun MoneyTraceApp() {
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute != Screen.Add.route) {
                MoneyBottomBar(navController = navController, currentRoute = currentRoute)
            }
        },
        floatingActionButton = {
            if (currentRoute != Screen.Add.route) {
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.Add.route) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    modifier = Modifier.offset(y = 52.dp)
                ) {
                    Icon(Icons.Filled.Add, "记一笔", modifier = Modifier.size(28.dp))
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onAddClick = { navController.navigate(Screen.Add.route) },
                    onTransactionClick = { /* TODO: 详情页 */ }
                )
            }
            composable(Screen.Stats.route) { StatsScreen() }
            composable(Screen.Add.route) {
                AddTransactionScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Budget.route) { BudgetScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }
        }
    }
}

@Composable
private fun MoneyBottomBar(navController: NavController, currentRoute: String?) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        bottomNavItems.forEachIndexed { index, screen ->
            // 中间留空给FAB
            if (index == 2) {
                NavigationBarItem(
                    selected = false,
                    onClick = {},
                    icon = { Spacer(Modifier.width(48.dp)) },
                    enabled = false
                )
            }
            val isSelected = currentRoute == screen.route
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(Screen.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        if (isSelected) screen.icon else screen.iconOutlined,
                        contentDescription = screen.label
                    )
                },
                label = { Text(screen.label, fontSize = 11.sp) }
            )
        }
    }
}
