package org.example.project.config

object ServerConfig {
    // Base URL phải khớp với backend Ktor (xem backend/API_TESTING.http)
    // Các client phía dưới sẽ gọi như: GET baseUrl + "/games" = "http://localhost:8080/api/games"
    @Volatile
    var baseUrl: String = "http://192.168.0.102:8080/api"
}
