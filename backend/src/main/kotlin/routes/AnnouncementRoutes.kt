package com.park.routes

import com.park.dto.CreateAnnouncementRequest
import com.park.dto.UpdateAnnouncementRequest
import com.park.models.ErrorResponse
import com.park.services.AnnouncementService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.announcementRoutes() {
    val service = AnnouncementService()

    // ─── Public: Android app lấy danh sách banner đang active ───────────
    route("/api/announcements") {
        get {
            try {
                val items = service.getActiveAnnouncements()
                call.respond(HttpStatusCode.OK, mapOf("success" to true, "data" to items))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Lỗi hệ thống: ${e.message}"))
            }
        }
    }

    // ─── Admin: quản lý announcements (yêu cầu JWT + role ADMIN) ────────
    route("/api/admin/announcements") {
        authenticate("auth-jwt") {

            /** GET /api/admin/announcements — lấy tất cả (cả inactive) */
            get {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val role = principal?.payload?.getClaim("role")?.asString()
                    if (role != "ADMIN") return@get call.respond(
                        HttpStatusCode.Forbidden, ErrorResponse(message = "Yêu cầu quyền admin")
                    )
                    val items = service.getAllAnnouncements()
                    call.respond(HttpStatusCode.OK, mapOf("success" to true, "data" to items))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Lỗi hệ thống: ${e.message}"))
                }
            }

            /** POST /api/admin/announcements — tạo banner mới */
            post {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val role = principal?.payload?.getClaim("role")?.asString()
                    if (role != "ADMIN") return@post call.respond(
                        HttpStatusCode.Forbidden, ErrorResponse(message = "Yêu cầu quyền admin")
                    )
                    val request = call.receive<CreateAnnouncementRequest>()
                    service.createAnnouncement(request).fold(
                        onSuccess = { call.respond(HttpStatusCode.Created, mapOf("success" to true, "data" to it)) },
                        onFailure = { call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = it.message ?: "Lỗi tạo banner")) }
                    )
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Invalid request: ${e.message}"))
                }
            }

            /** PUT /api/admin/announcements/{id} — cập nhật banner */
            put("/{id}") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val role = principal?.payload?.getClaim("role")?.asString()
                    if (role != "ADMIN") return@put call.respond(
                        HttpStatusCode.Forbidden, ErrorResponse(message = "Yêu cầu quyền admin")
                    )
                    val id = call.parameters["id"] ?: return@put call.respond(
                        HttpStatusCode.BadRequest, ErrorResponse(message = "Thiếu ID")
                    )
                    val request = call.receive<UpdateAnnouncementRequest>()
                    service.updateAnnouncement(id, request).fold(
                        onSuccess = { call.respond(HttpStatusCode.OK, mapOf("success" to true)) },
                        onFailure = { call.respond(HttpStatusCode.NotFound, ErrorResponse(message = it.message ?: "Không tìm thấy")) }
                    )
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Invalid request: ${e.message}"))
                }
            }

            /** DELETE /api/admin/announcements/{id} — xóa banner */
            delete("/{id}") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val role = principal?.payload?.getClaim("role")?.asString()
                    if (role != "ADMIN") return@delete call.respond(
                        HttpStatusCode.Forbidden, ErrorResponse(message = "Yêu cầu quyền admin")
                    )
                    val id = call.parameters["id"] ?: return@delete call.respond(
                        HttpStatusCode.BadRequest, ErrorResponse(message = "Thiếu ID")
                    )
                    service.deleteAnnouncement(id).fold(
                        onSuccess = { call.respond(HttpStatusCode.OK, mapOf("success" to true)) },
                        onFailure = { call.respond(HttpStatusCode.NotFound, ErrorResponse(message = it.message ?: "Không tìm thấy")) }
                    )
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "Lỗi hệ thống: ${e.message}"))
                }
            }
        }
    }
}
