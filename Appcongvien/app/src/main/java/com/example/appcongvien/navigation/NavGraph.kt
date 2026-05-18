package com.example.appcongvien.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.appcongvien.App
import com.example.appcongvien.screen.auth.ChangePasswordScreen
import com.example.appcongvien.screen.auth.ForgotPasswordScreen
import com.example.appcongvien.screen.auth.LoginScreen
import com.example.appcongvien.screen.auth.RegisterScreen
import com.example.appcongvien.ui.theme.ThemeMode

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object ChangePassword : Screen("change_password")
    object Home : Screen("home")
    object CardInfo : Screen("card_info")
    object CardRequest : Screen("card_request")
    object Balance : Screen("balance")
    object TopUp : Screen("top_up")
    object GameDetail : Screen("game_detail/{gameId}") {
        fun createRoute(gameId: String) = "game_detail/$gameId"
    }
    object Settings : Screen("settings")
    object Profile : Screen("profile")
    object SupportChat : Screen("support_chat")
    object Notifications : Screen("notifications")
    object PaymentHistory : Screen("payment_history")
    object UsageHistory : Screen("usage_history")
    object GameList: Screen("game_list")
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Home.route,
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    onThemeModeChange: (ThemeMode) -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onRegisterClick = { navController.navigate(Screen.Register.route) },
                onForgotPasswordClick = { navController.navigate(Screen.ForgotPassword.route) }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.ChangePassword.route) {
            ChangePasswordScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Home.route) {
            RequireLogin(
                onRequireLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            ) {
                com.example.appcongvien.screen.HomeScreen(
                    onCardInfoClick = { navController.navigate(Screen.CardInfo.route) },
                    onCardRequestClick = { navController.navigate(Screen.CardRequest.route) },
                    onBalanceClick = { navController.navigate(Screen.Balance.route) },
                    onTopUpClick = { navController.navigate(Screen.TopUp.route) },
                    onGameClick = { gameId -> navController.navigate(Screen.GameDetail.createRoute(gameId)) },
                    onGameListClick = { navController.navigate(Screen.GameList.route) },
                    onSettingsClick = { navController.navigate(Screen.Settings.route) },
                    onSupportClick = { navController.navigate(Screen.SupportChat.route) },
                    onNotificationsClick = { navController.navigate(Screen.Notifications.route) }
                )
            }
        }

        composable(Screen.CardInfo.route) {
            RequireLogin(
                onRequireLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            ) {
                com.example.appcongvien.screen.CardInfoRoute(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        composable(Screen.CardRequest.route) {
            RequireLogin(
                onRequireLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            ) {
                val app = LocalContext.current.applicationContext as App
                com.example.appcongvien.screen.CardRequestRoute(
                    repository = app.cardRequestRepository,
                    onBackClick = { navController.popBackStack() },
                    onNavigateTopUp = { navController.navigate(Screen.TopUp.route) }
                )
            }
        }

        composable(Screen.Balance.route) {
            RequireLogin(
                onRequireLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            ) {
                com.example.appcongvien.screen.BalanceScreen(
                    onTopUpClick = { navController.navigate(Screen.TopUp.route) },
                    onPaymentHistoryClick = { navController.navigate(Screen.PaymentHistory.route) },
                    onUsageHistoryClick = { navController.navigate(Screen.UsageHistory.route) },
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        composable(Screen.TopUp.route) {
            RequireLogin(
                onRequireLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            ) {
                com.example.appcongvien.screen.TopUpScreen(
                    onBackClick = { navController.popBackStack() },
                    onTopUpSuccess = {
                        navController.navigate(Screen.Balance.route) {
                            popUpTo(Screen.TopUp.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }

        composable(
            route = Screen.GameDetail.route,
            arguments = listOf(
                androidx.navigation.navArgument("gameId") {
                    type = androidx.navigation.NavType.StringType
                }
            )
        ) { backStackEntry ->
            RequireLogin(
                onRequireLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            ) {
                val gameId = backStackEntry.arguments?.getString("gameId") ?: ""
                com.example.appcongvien.screen.GameDetailScreen(
                    gameId = gameId,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        composable(Screen.Settings.route) {
            RequireLogin(
                onRequireLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            ) {
                val app = LocalContext.current.applicationContext as App
                com.example.appcongvien.screen.SettingsScreen(
                    onProfileClick = { navController.navigate(Screen.Profile.route) },
                    onBackClick = { navController.popBackStack() },
                    themeMode = themeMode,
                    onThemeModeChange = onThemeModeChange,
                    onHelpClick = { navController.navigate(Screen.SupportChat.route) },
                    onLogoutClick = {
                        app.authRepository.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable(Screen.Profile.route) {
            RequireLogin(
                onRequireLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            ) {
                com.example.appcongvien.screen.ProfileRoute(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        composable(Screen.SupportChat.route) {
            RequireLogin(
                onRequireLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            ) {
                com.example.appcongvien.screen.SupportChatRoute(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        composable(Screen.Notifications.route) {
            RequireLogin(
                onRequireLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            ) {
                com.example.appcongvien.screen.NotificationsRoute(
                    onBackClick = { navController.popBackStack() },
                    onNotificationOpen = { notification ->
                        val request = com.example.appcongvien.navigation.mapNotificationToRoute(notification)
                        navController.navigate(request.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }
        }

        composable(Screen.PaymentHistory.route) {
            RequireLogin(
                onRequireLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            ) {
                com.example.appcongvien.screen.PaymentHistoryScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        composable(Screen.UsageHistory.route) {
            RequireLogin(
                onRequireLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            ) {
                com.example.appcongvien.screen.UsageHistoryScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
        composable(Screen.GameList.route) {
            RequireLogin(
                onRequireLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            ) {
                com.example.appcongvien.screen.GameListScreen(
                    onGameClick = { gameId -> navController.navigate(Screen.GameDetail.createRoute(gameId)) },
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun RequireLogin(
    onRequireLogin: () -> Unit,
    content: @Composable () -> Unit
) {
    val app = LocalContext.current.applicationContext as App
    val isLoggedIn = app.authRepository.isLoggedIn()

    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn) {
            onRequireLogin()
        }
    }

    if (isLoggedIn) {
        content()
    }
}
