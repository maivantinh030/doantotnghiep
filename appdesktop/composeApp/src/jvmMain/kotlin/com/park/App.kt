package com.park

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.park.navigation.AdminScreen
import com.park.ui.component.SideNav
import com.park.ui.screen.*
import com.park.ui.theme.AdminTheme
import com.park.viewmodel.*

@Composable
fun App() {
    AdminTheme {
        val authViewModel: AuthViewModel = viewModel { AuthViewModel() }
        val authState by authViewModel.uiState.collectAsStateWithLifecycle()

        if (!authState.isLoggedIn) {
            LoginScreen(
                onLoginSuccess = { /* state updates automatically via flow */ },
                viewModel = authViewModel
            )
        } else {
            var currentScreen by remember { mutableStateOf(AdminScreen.DASHBOARD) }
            val adminName = authState.adminProfile?.fullName ?: authState.adminProfile?.fullName ?: "Admin"

            Row(modifier = Modifier.fillMaxSize()) {
                SideNav(
                    currentScreen = currentScreen,
                    adminName = adminName,
                    onNavigate = { currentScreen = it },
                    onLogout = { authViewModel.logout() }
                )

                when (currentScreen) {
                    AdminScreen.DASHBOARD -> DashboardScreen(viewModel = viewModel { DashboardViewModel() })
                    AdminScreen.USERS -> UserManagementScreen(viewModel = viewModel { UserManagementViewModel() })
                    AdminScreen.GAMES -> GameManagementScreen(viewModel = viewModel { GameManagementViewModel() })
                    AdminScreen.VOUCHERS -> VoucherManagementScreen(viewModel = viewModel { VoucherManagementViewModel() })
                    AdminScreen.FINANCE -> FinanceScreen(viewModel = viewModel { FinanceViewModel() })
                    AdminScreen.NOTIFICATIONS -> NotificationScreen(viewModel = viewModel { NotificationViewModel() })
                    AdminScreen.SUPPORT -> SupportScreen(viewModel = viewModel { SupportViewModel() })
                    AdminScreen.LOGIN -> {}
                }
            }
        }
    }
}
