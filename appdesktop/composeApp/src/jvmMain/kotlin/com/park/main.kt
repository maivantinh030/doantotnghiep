package com.park

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Park Adventure - Admin Dashboard",
        state = WindowState(width = 1280.dp, height = 800.dp)
    ) {
        App()
    }
}
