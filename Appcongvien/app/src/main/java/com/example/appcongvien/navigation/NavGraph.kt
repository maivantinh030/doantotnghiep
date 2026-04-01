package com.example.appcongvien.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.appcongvien.App
import com.example.appcongvien.screen.*
import com.example.appcongvien.screen.auth.*

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
    startDestination: String = Screen.Home.route
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
                HomeScreen(
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
                CardInfoScreen(
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
                CardRequestScreen(
                    repository = app.cardRequestRepository,
                    onBackClick = { navController.popBackStack() }
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
                BalanceScreen(
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
                TopUpScreen(
                    onBackClick = { navController.popBackStack() }
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
                GameDetailScreen(
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
                SettingsScreen(
                    onProfileClick = { navController.navigate(Screen.Profile.route) },
                    onBackClick = { navController.popBackStack() },
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
                ProfileScreen(
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
                SupportChatScreen(
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
                NotificationsScreen(
                    onBackClick = { navController.popBackStack() },
                    onNotificationOpen = { notification ->
                        val request = notification.toNavigationRequest()
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
                PaymentHistoryScreen(
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
                UsageHistoryScreen(
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
                GameListScreen(
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
