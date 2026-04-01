package com.park.plugins

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import io.ktor.server.application.Application
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

object FirebaseAdminState {
    fun isInitialized(): Boolean = FirebaseApp.getApps().isNotEmpty()
}

fun Application.configureFirebaseAdmin() {
    val serviceAccountPath = environment.config.propertyOrNull("firebase.serviceAccountPath")
        ?.getString()
        ?.trim()
        .orEmpty()

    if (serviceAccountPath.isBlank()) {
        println("Firebase Admin disabled: firebase.serviceAccountPath is empty")
        return
    }

    val resolvedPath = Paths.get(serviceAccountPath)
    if (!resolvedPath.isReadableFile()) {
        println("Firebase Admin disabled: service account file not found at $serviceAccountPath")
        return
    }

    if (FirebaseAdminState.isInitialized()) {
        return
    }

    val projectId = environment.config.propertyOrNull("firebase.projectId")
        ?.getString()
        ?.trim()
        ?.takeIf { it.isNotEmpty() }

    FileInputStream(resolvedPath.toFile()).use { stream ->
        val optionsBuilder = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(stream))

        if (projectId != null) {
            optionsBuilder.setProjectId(projectId)
        }

        FirebaseApp.initializeApp(optionsBuilder.build())
    }

    println("Firebase Admin initialized successfully")
}

private fun Path.isReadableFile(): Boolean {
    return Files.exists(this) && Files.isRegularFile(this) && Files.isReadable(this)
}
