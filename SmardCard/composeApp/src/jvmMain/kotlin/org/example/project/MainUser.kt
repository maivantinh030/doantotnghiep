package org.example.project

import androidx.compose.runtime. Composable
import androidx.compose. runtime.getValue
import androidx.compose.runtime. mutableStateOf
import androidx. compose.runtime.remember
import androidx.compose.runtime. setValue
import androidx.compose.ui.window.Window
import androidx.compose. ui.window.application
import org.example.project.screen.ConnectScreen
import org.example. project.screen.PinEntryScreen
import org.example. project.screen.user.*

enum class UserScreen {
    CONNECT,
    PIN_ENTRY,
    MAIN,
    VIEW_INFO,
    UPDATE_INFO,
    BUY_TICKETS,  // ✅ ĐỔI TÊN
    GAMES,
    CHANGE_PIN,
    HISTORY
}

@Composable
fun UserApp() {
    var currentScreen by remember { mutableStateOf(UserScreen.CONNECT) }
    val smartCardManager = remember { SmartCardManager() }

    when (currentScreen) {
        UserScreen.CONNECT -> {
            ConnectScreen(
                onCardConnected = { currentScreen = UserScreen.PIN_ENTRY },
                smartCardManager = smartCardManager,
                requireRSAAuth = true
            )
        }

        UserScreen.PIN_ENTRY -> {
            PinEntryScreen(
                smartCardManager = smartCardManager,
                onPinVerified = { currentScreen = UserScreen.MAIN }
            )
        }

        UserScreen.MAIN -> {
            UserMainMenuScreen(
                smartCardManager = smartCardManager,
                onNavigateViewInfo = { currentScreen = UserScreen.VIEW_INFO },
                onNavigateUpdateInfo = { currentScreen = UserScreen.UPDATE_INFO },
                onNavigateBuyTickets = { currentScreen = UserScreen.BUY_TICKETS }, // ✅ ĐỔI TÊN
                onNavigateGames = { currentScreen = UserScreen.GAMES },
                onNavigateHistory = { currentScreen = UserScreen.HISTORY },
                onNavigateChangePin = { currentScreen = UserScreen. CHANGE_PIN },
                onDisconnect = {
                    smartCardManager.disconnect()
                    currentScreen = UserScreen.CONNECT
                }
            )
        }

        UserScreen.VIEW_INFO -> {
            UserViewInfoScreen(
                smartCardManager = smartCardManager,
                onBack = { currentScreen = UserScreen. MAIN }
            )
        }

        UserScreen.UPDATE_INFO -> {
            UserUpdateInfoScreen(
                smartCardManager = smartCardManager,
                onBack = { currentScreen = UserScreen.MAIN }
            )
        }

        UserScreen. BUY_TICKETS -> {  // ✅ ĐỔI TÊN
            UserBuyTicketsScreen(
                smartCardManager = smartCardManager,
                onBack = { currentScreen = UserScreen.MAIN }
            )
        }

        UserScreen.GAMES -> {
            UserGameListScreen(
                smartCardManager = smartCardManager,
                onBack = { currentScreen = UserScreen. MAIN }
            )
        }

        UserScreen. CHANGE_PIN -> {
            UserChangePinScreen(
                smartCardManager = smartCardManager,
                onBack = { currentScreen = UserScreen.MAIN }
            )
        }
        UserScreen.HISTORY -> {
            UserHistoryScreen(
                smartCardManager = smartCardManager,
                onBack = { currentScreen = UserScreen.MAIN }
            )
        }
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "🎡 SmartCard Park - USER"
    ) {
        UserApp()
    }
}