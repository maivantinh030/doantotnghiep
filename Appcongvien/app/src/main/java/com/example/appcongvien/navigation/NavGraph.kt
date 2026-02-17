package com.example.appcongvien.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.appcongvien.screen.*
import com.example.appcongvien.screen.auth.*

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object ChangePassword : Screen("change_password")
    object Home : Screen("home")
    object CardInfo : Screen("card_info")
    object LockCard : Screen("lock_card")
    object Balance : Screen("balance")
    object TopUp : Screen("top_up")
    object Checkout : Screen("check_out")
    object GameDetail : Screen("game_detail/{gameId}") {
        fun createRoute(gameId: String) = "game_detail/$gameId"
    }
    object Vouchers : Screen("vouchers")
    object VoucherWallet : Screen("voucher_wallet")
    object ReferralCode : Screen("referral_code")
    object MemberCard : Screen("member_card")
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
                onLoginSuccess = { navController.navigate(Screen.Home.route) },
                onRegisterClick = { navController.navigate(Screen.Register.route) },
                onForgotPasswordClick = { navController.navigate(Screen.ForgotPassword.route) }
            )
        }
        
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = { navController.navigate(Screen.Home.route) },
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
            HomeScreen(
                onCardInfoClick = { navController.navigate(Screen.CardInfo.route) },
//                onLockCardClick = { navController.navigate(Screen.LockCard.route) },
                onBalanceClick = { navController.navigate(Screen.Balance.route) },
                onTopUpClick = { navController.navigate(Screen.TopUp.route) },
                onBuyGameClick = { navController.navigate(Screen.Checkout.route) },
                onVouchersClick = { navController.navigate(Screen.Vouchers.route) },
                onVoucherWalletClick = { navController.navigate(Screen.VoucherWallet.route) },
                onReferralClick = { navController.navigate(Screen.ReferralCode.route) },
                onMemberCardClick = { navController.navigate(Screen.MemberCard.route) },
                onGameClick = { gameId -> navController.navigate(Screen.GameDetail.createRoute(gameId)) },
                onGameListClick = { navController.navigate(Screen.GameList.route) },
                onSettingsClick = { navController.navigate(Screen.Settings.route) },
                onSupportClick = { navController.navigate(Screen.SupportChat.route) },
                onNotificationsClick = { navController.navigate(Screen.Notifications.route) }
            )
        }
        
        composable(Screen.CardInfo.route) {
            CardInfoScreen(
                onBackClick = { navController.popBackStack() },
                onMembershipDetailsClick = {
                    navController.navigate(Screen.MemberCard.route)
                }
            )
        }
        
        composable(Screen.LockCard.route) {
            LockCardScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Balance.route) {
            BalanceScreen(
                onTopUpClick = { navController.navigate(Screen.TopUp.route) },
                onPaymentHistoryClick = { navController.navigate(Screen.PaymentHistory.route) },
                onUsageHistoryClick = { navController.navigate(Screen.UsageHistory.route) },
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable(Screen.TopUp.route) {
            TopUpScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Checkout.route) {
            CheckoutScreen(
                onBackClick = { navController.popBackStack() },
                navController = navController
            )
        }
        
        composable(
            route = Screen.GameDetail.route,
            arguments = listOf(
                androidx.navigation.navArgument("gameId") {
                    type = androidx.navigation.NavType.StringType
                }
            )
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId") ?: ""
            GameDetailScreen(
                gameId = gameId,
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Vouchers.route) {
            VouchersScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable(Screen.VoucherWallet.route) {
            VoucherWalletScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable(Screen.ReferralCode.route) {
            ReferralCodeScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable(Screen.MemberCard.route) {
            MemberCardScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onProfileClick = { navController.navigate(Screen.Profile.route) },
                onBackClick = { navController.popBackStack() },
                onHelpClick = {
                    navController.navigate(Screen.SupportChat.route)
                }
            )
        }
        
        composable(Screen.Profile.route) {
            ProfileScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable(Screen.SupportChat.route) {
            SupportChatScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Notifications.route) {
            NotificationsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable(Screen.PaymentHistory.route) {
            PaymentHistoryScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable(Screen.UsageHistory.route) {
            UsageHistoryScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(Screen.GameList.route) {
            GameListScreen(
                onGameClick = { gameId -> navController.navigate(Screen.GameDetail.createRoute(gameId)) },
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

