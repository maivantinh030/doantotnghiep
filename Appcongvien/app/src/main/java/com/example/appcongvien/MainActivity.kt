package com.example.appcongvien

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.outlined.AccountBox
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.appcongvien.components.BottomBar
import com.example.appcongvien.navigation.AppNavGraph
import com.example.appcongvien.navigation.NotificationNavigationRequest
import com.example.appcongvien.navigation.Screen
import com.example.appcongvien.ui.theme.ParkTheme
import com.example.appcongvien.ui.theme.AppcongvienTheme
import com.example.appcongvien.ui.theme.ThemeMode

class MainActivity : ComponentActivity() {
    private var pendingNotificationRequest by mutableStateOf<NotificationNavigationRequest?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        pendingNotificationRequest = com.example.appcongvien.navigation.extractNotificationNavigationRequest(intent)
        val app = application as App
        val startDestination = if (app.authRepository.isLoggedIn()) {
            Screen.Home.route
        } else {
            Screen.Login.route
        }
        setContent {
            val themeMode by app.themePreferenceManager.themeMode.collectAsState()
            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            AppcongvienTheme(darkTheme = darkTheme) {
                SystemBarEffect(darkTheme = darkTheme)
                AppcongvienApp(
                    startDestination = startDestination,
                    themeMode = themeMode,
                    onThemeModeChange = app.themePreferenceManager::setThemeMode,
                    pendingNotificationRequest = pendingNotificationRequest,
                    onNotificationHandled = { pendingNotificationRequest = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingNotificationRequest = com.example.appcongvien.navigation.extractNotificationNavigationRequest(intent)
    }
}

@PreviewScreenSizes
@Composable
fun AppcongvienApp(
    startDestination: String = Screen.Login.route,
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    onThemeModeChange: (ThemeMode) -> Unit = {},
    pendingNotificationRequest: NotificationNavigationRequest? = null,
    onNotificationHandled: () -> Unit = {}
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val colors = ParkTheme.colors
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NotificationPermissionEffect()

    LaunchedEffect(pendingNotificationRequest, currentRoute) {
        val request = pendingNotificationRequest ?: return@LaunchedEffect
        val app = context.applicationContext as App

        if (!app.authRepository.isLoggedIn()) {
            return@LaunchedEffect
        }

        request.notificationId?.let { app.notificationRepository.markAsRead(it) }
        if (currentRoute != request.route) {
            navController.navigate(request.route) {
                launchSingleTop = true
            }
        }

        onNotificationHandled()
    }

    val currentDestination = when (currentRoute) {
        Screen.Home.route -> AppDestinations.HOME
        Screen.Balance.route, Screen.PaymentHistory.route, Screen.TopUp.route -> AppDestinations.BALANCE
        Screen.Profile.route, Screen.Settings.route -> AppDestinations.PROFILE
        Screen.CardInfo.route, Screen.CardRequest.route -> AppDestinations.CARD
        else -> AppDestinations.HOME
    }

    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Balance.route,
        Screen.Profile.route,
        Screen.Settings.route,
        Screen.CardInfo.route,
        Screen.CardRequest.route
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.backgroundBase,
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
        Surface(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            color = colors.backgroundBase
        ) {
            AppNavGraph(
                navController = navController,
                startDestination = startDestination,
                themeMode = themeMode,
                onThemeModeChange = onThemeModeChange
            )
        }
    }
}

@Composable
private fun SystemBarEffect(darkTheme: Boolean) {
    val view = LocalView.current
    SideEffect {
        val window = (view.context as? ComponentActivity)?.window ?: return@SideEffect
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = !darkTheme
            isAppearanceLightNavigationBars = !darkTheme
        }
    }
}

@Composable
private fun NotificationPermissionEffect() {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

private fun navigateToDestination(
    destination: AppDestinations,
    navController: NavController,
    currentRoute: String?
) {
    when (destination) {
        AppDestinations.HOME -> {
            if (currentRoute != Screen.Home.route) {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Home.route) { inclusive = false; saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
        AppDestinations.BALANCE -> {
            if (currentRoute != Screen.Balance.route) {
                navController.navigate(Screen.Balance.route) {
                    popUpTo(Screen.Home.route) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
        AppDestinations.PROFILE -> {
            if (currentRoute != Screen.Profile.route) {
                navController.navigate(Screen.Profile.route) {
                    popUpTo(Screen.Home.route) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
        AppDestinations.CARD -> {
            if (currentRoute != Screen.CardInfo.route) {
                navController.navigate(Screen.CardInfo.route) {
                    popUpTo(Screen.Home.route) { saveState = true }
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
    BALANCE("Số dư", Icons.Filled.Wallet, Icons.Filled.Wallet),
    CARD("Thẻ", Icons.Filled.CreditCard, Icons.Outlined.CreditCard),
    PROFILE("Cá nhân", Icons.Filled.AccountBox, Icons.Outlined.AccountBox),
}
