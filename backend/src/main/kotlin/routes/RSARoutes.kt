package com.park.routes

import com.park.dto.RSAVerifyRequest
import com.park.dto.RegisterKeyRequest
import com.park.models.ErrorResponse
import com.park.services.RSAService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

private val rsaService = RSAService()

fun Route.rsaRoutes() {
    route("/api/rsa") {
        post("/register-key") {
            try {
                val request = call.receive<RegisterKeyRequest>()
                val result = rsaService.registerPublicKey(request.cardId, request.publicKey)
                result.fold(
                    onSuccess = {
                        call.respond(
                            HttpStatusCode.OK,
                            mapOf("success" to true, "message" to "Dang ky public key thanh cong")
                        )
                    },
                    onFailure = { e ->
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse(message = e.message ?: "Dang ky public key that bai")
                        )
                    }
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(message = "Invalid request: ${e.message}")
                )
            }
        }

        get("/challenge") {
            val challenge = rsaService.createChallenge()
            call.respond(HttpStatusCode.OK, challenge)
        }

        post("/verify") {
            try {
                val request = call.receive<RSAVerifyRequest>()
                val result = rsaService.verifySignature(request)
                call.respond(HttpStatusCode.OK, result)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(message = "Invalid request: ${e.message}")
                )
            }
        }
    }
}

