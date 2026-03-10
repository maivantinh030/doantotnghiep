
        package org.example.project

        import androidx.compose.runtime.Composable
        import androidx.compose.runtime.getValue
        import androidx.compose.runtime.mutableStateOf
        import androidx.compose.runtime.remember
        import androidx.compose.runtime.setValue
        import androidx.compose.ui.window.Window
        import androidx.compose.ui.window.application
        import org.example.project.config.ServerConfig
import org.example.project.screen.ConnectScreen
import org.example.project.screen.admin.AdminGameManagementScreen
        import org.example.project.screen.admin.AdminLoginScreen
        import org.example.project.screen.admin.AdminMainMenuScreen
        import org.example.project.screen.admin.AdminRechargeScreen
        import org.example.project.screen.admin.AdminRevenueScreen
        import org.example.project.screen.admin.AdminRSAAuthScreen
        import org.example.project.screen.admin.AdminSettingsScreen
        import org.example.project.screen.admin.AdminViewCustomerScreen
        import org.example.project.screen.admin.AdminWriteInfoScreen
        import org.example.project.screen.admin.AdminResetUserPinScreen

        enum class AdminScreen {
            ADMIN_LOGIN,
            CONNECT,
            MAIN,
            WRITE_INFO,
            VIEW_CUSTOMER,
            RECHARGE,
            GAME_MANAGEMENT,
            REVENUE,
            RSA_AUTH,
            SETTINGS,
            RESET_USER_PIN
        }

        @Composable
        fun AdminApp() {
            var currentScreen by remember { mutableStateOf(AdminScreen.ADMIN_LOGIN) }
            val smartCardManager = remember { SmartCardManager() }
            var baseUrl by remember { mutableStateOf(ServerConfig.baseUrl) }

            when (currentScreen) {
                AdminScreen.ADMIN_LOGIN -> {
                    AdminLoginScreen(
                        onLoggedIn = { currentScreen = AdminScreen.CONNECT },
                        baseUrl = baseUrl,
                        onBaseUrlChange = {
                            baseUrl = it
                            ServerConfig.baseUrl = it
                        }
                    )
                }
                AdminScreen.CONNECT -> {
                    ConnectScreen(
                        onCardConnected = { currentScreen = AdminScreen.MAIN },
                        onRequireRSASetup = { currentScreen = AdminScreen.RSA_AUTH },
                        smartCardManager = smartCardManager,
                        requireRSAAuth = false
                    )
                }
                AdminScreen.MAIN -> {
                    AdminMainMenuScreen(
                        smartCardManager = smartCardManager,
                        onNavigateWriteInfo = { currentScreen = AdminScreen.WRITE_INFO },
                        onNavigateRecharge = { currentScreen = AdminScreen.RECHARGE },
                        onNavigateGameManagement = { currentScreen = AdminScreen.GAME_MANAGEMENT },
                        onNavigateRevenue = { currentScreen = AdminScreen.REVENUE },
                        onNavigateViewCustomer = { currentScreen = AdminScreen.VIEW_CUSTOMER },
                        onNavigateSettings = { currentScreen = AdminScreen.SETTINGS },
                        onNavigateResetUserPin = { currentScreen = AdminScreen.RESET_USER_PIN },
                        onDisconnect = {
                            smartCardManager.disconnect()
                            currentScreen = AdminScreen.ADMIN_LOGIN
                        }
                    )
                }
                AdminScreen.WRITE_INFO -> {
                    AdminWriteInfoScreen(
                        smartCardManager = smartCardManager,
                        onBack = { currentScreen = AdminScreen.MAIN }
                    )
                }
                AdminScreen.VIEW_CUSTOMER -> {
                    AdminViewCustomerScreen(
                        smartCardManager = smartCardManager,
                        onBack = { currentScreen = AdminScreen.MAIN }
                    )
                }
                AdminScreen.RECHARGE -> {
                    AdminRechargeScreen(
                        smartCardManager = smartCardManager,
                        onBack = { currentScreen = AdminScreen.MAIN }
                    )
                }
                AdminScreen.GAME_MANAGEMENT -> {
                    AdminGameManagementScreen(
                        smartCardManager = smartCardManager,
                        onBack = { currentScreen = AdminScreen.MAIN }
                    )
                }
                AdminScreen.RSA_AUTH -> {
                    AdminRSAAuthScreen(
                        smartCardManager = smartCardManager,
                        onBack = { currentScreen = AdminScreen.MAIN }
                    )
                }
                AdminScreen.REVENUE -> {
                    AdminRevenueScreen(
                        onBack = { currentScreen = AdminScreen.MAIN }
                    )
                }
                AdminScreen.SETTINGS -> {
                    AdminSettingsScreen(
                        smartCardManager = smartCardManager,
                        onBack = { currentScreen = AdminScreen.MAIN }
                    )
                }
                AdminScreen.RESET_USER_PIN -> {
                    AdminResetUserPinScreen(
                        smartCardManager = smartCardManager,
                        onBack = { currentScreen = AdminScreen.MAIN }
                    )
                }
            }
        }

        fun main() = application {
            Window(
                onCloseRequest = ::exitApplication,
                title = "🛡️ SmartCard Park - ADMIN"
            ) {
                AdminApp()
            }
        }