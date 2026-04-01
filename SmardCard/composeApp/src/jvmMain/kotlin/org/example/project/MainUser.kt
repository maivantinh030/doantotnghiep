package org.example.project

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "🎡 Park Adventure — Quầy Lễ Tân",
        state = rememberWindowState(width = 1100.dp, height = 720.dp)
    ) {
        StaffApp()
    }
}