package com.example.appcongvien

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.AccountBox
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.appcongvien.components.BottomBar
import com.example.appcongvien.navigation.AppNavGraph
import com.example.appcongvien.navigation.Screen
import com.example.appcongvien.ui.theme.AppColors.SurfaceLight
import com.example.appcongvien.ui.theme.AppcongvienTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppcongvienTheme {
                AppcongvienApp()
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun AppcongvienApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Map route sang Enum AppDestinations
    val currentDestination = when (currentRoute) {
        Screen.Home.route -> AppDestinations.HOME
        Screen.VoucherWallet.route, Screen.Vouchers.route -> AppDestinations.VOUCHERS
        Screen.Profile.route, Screen.Settings.route, Screen.MemberCard.route -> AppDestinations.PROFILE
        Screen.CardInfo.route -> AppDestinations.CARD // Sửa lại logic duplicate case cũ
        else -> AppDestinations.HOME // Mặc định về Home nếu không match (ví dụ trang Login)
    }

    // Kiểm tra xem màn hình hiện tại có cần hiện BottomBar không
    // (Ví dụ: Login, Camera, GameDetail thì thường ẩn BottomBar đi)
    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Vouchers.route, Screen.VoucherWallet.route,
        Screen.Profile.route, Screen.Settings.route, Screen.MemberCard.route,
        Screen.CardInfo.route
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = SurfaceLight, // Màu nền chung của app
        bottomBar = {
            if (showBottomBar) {
                BottomBar(
                    currentDestination = currentDestination,
                    onNavigate = { destination ->
                        navigateToDestination(destination, navController, currentRoute)
                    }
                )
            }
        }
    ) { innerPadding ->
        // Nội dung chính
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = SurfaceLight
        ) {
            AppNavGraph(
                navController = navController,
                startDestination = Screen.Home.route
            )
        }
    }
}

private fun navigateToDestination(
    destination: AppDestinations,
    navController: NavController,
    currentRoute: String?
) {
    // Avoid navigating to the same destination
    when (destination) {
        AppDestinations.HOME -> {
            if (currentRoute != Screen.Home.route) {
                navController.navigate(Screen.Home.route) {
                    // Clear back stack when going to home
                    popUpTo(Screen.Home.route) {
                        inclusive = false
                        saveState = true
                    }
                    // Avoid multiple copies of the same destination
                    launchSingleTop = true
                    // Restore state when re-selecting a previously selected item
                    restoreState = true
                }
            }
        }

        AppDestinations.VOUCHERS -> {
            if (currentRoute != Screen.VoucherWallet.route) {
                navController.navigate(Screen.VoucherWallet.route) {
                    popUpTo(Screen.Home.route) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }

        AppDestinations.PROFILE -> {
            if (currentRoute != Screen.Profile.route) {
                navController.navigate(Screen.Profile.route) {
                    popUpTo(Screen.Home.route) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }

        AppDestinations.CARD -> {
            if (currentRoute != Screen.CardInfo.route) {
                navController.navigate(Screen.CardInfo.route) {
                    popUpTo(Screen.Home.route) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    HOME("Trang chủ", Icons.Filled.Home, Icons.Outlined.Home),
    VOUCHERS("Voucher", Icons.Filled.CardGiftcard, Icons.Outlined.CardGiftcard),
    CARD("Thẻ", Icons.Filled.CreditCard, Icons.Outlined.CreditCard),
    PROFILE("Cá nhân", Icons.Filled.AccountBox, Icons.Outlined.AccountBox),
}