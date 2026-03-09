package com.example.testnfc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.testnfc.ui.screens.GameListScreen
import com.example.testnfc.ui.screens.LoginScreen
import com.example.testnfc.ui.screens.TerminalScreen
import com.example.testnfc.ui.theme.AppColors
import com.example.testnfc.ui.theme.TestNFCTheme
import com.example.testnfc.ui.viewmodel.TerminalViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TestNFCTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = AppColors.SurfaceLight
                ) {
                    TerminalApp(activity = this)
                }
            }
        }
    }
}

@Composable
fun TerminalApp(activity: MainActivity) {
    val navController = rememberNavController()
    val viewModel: TerminalViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    NavHost(navController = navController, startDestination = "login") {

        composable("login") {
            LoginScreen(
                isLoading = uiState.loginLoading,
                errorMessage = uiState.loginError,
                onLogin = { phone, pass -> viewModel.login(phone, pass) }
            )
            LaunchedEffect(uiState.isLoggedIn) {
                if (uiState.isLoggedIn) {
                    navController.navigate("games") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            }
        }

        composable("games") {
            GameListScreen(
                adminName = uiState.adminName,
                games = uiState.games,
                isLoading = uiState.gamesLoading,
                errorMessage = uiState.gamesError,
                onGameSelected = { game ->
                    viewModel.selectGame(game)
                    navController.navigate("terminal")
                },
                onRefresh = { viewModel.loadGames() },
                onLogout = {
                    viewModel.logout()
                    navController.navigate("login") {
                        popUpTo("games") { inclusive = true }
                    }
                }
            )
        }

        composable("terminal") {
            val selectedGame = uiState.selectedGame
            LaunchedEffect(selectedGame) {
                if (selectedGame == null) {
                    navController.popBackStack()
                }
            }
            if (selectedGame == null) return@composable
            TerminalScreen(
                activity = activity,
                game = selectedGame,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
