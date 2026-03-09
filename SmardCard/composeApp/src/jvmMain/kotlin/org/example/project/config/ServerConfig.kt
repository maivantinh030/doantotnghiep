package org.example.project.config

object ServerConfig {
    // Change this when deploying backend elsewhere
    @Volatile
    var baseUrl: String = "http://localhost:8080/api/v1"
}
