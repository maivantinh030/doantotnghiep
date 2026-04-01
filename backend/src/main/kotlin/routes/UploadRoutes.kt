package com.park.routes

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File
import java.util.UUID

private val ALLOWED_CONTENT_TYPES = setOf(
    "image/jpeg",
    "image/png",
    "image/gif",
    "image/webp"
)

private val MIME_TO_EXT = mapOf(
    "image/jpeg" to "jpg",
    "image/png"  to "png",
    "image/gif"  to "gif",
    "image/webp" to "webp"
)

fun Route.uploadRoutes() {
    val uploadsDir = File(System.getProperty("user.dir"), "uploads").also { it.mkdirs() }

    post("/api/upload") {
        val multipart = call.receiveMultipart()
        var savedUrl: String? = null

        multipart.forEachPart { part ->
            if (part is PartData.FileItem && savedUrl == null) {
                val contentType = part.contentType?.toString() ?: "application/octet-stream"

                if (contentType !in ALLOWED_CONTENT_TYPES) {
                    call.respond(
                        HttpStatusCode.UnsupportedMediaType,
                        mapOf("success" to false, "message" to "Loại file không hỗ trợ: $contentType. Chỉ chấp nhận jpg, png, gif, webp")
                    )
                    part.dispose()
                    return@forEachPart
                }

                val ext = MIME_TO_EXT[contentType] ?: "bin"
                val filename = "${UUID.randomUUID()}.$ext"
                val targetFile = File(uploadsDir, filename)

                part.streamProvider().use { input ->
                    targetFile.outputStream().buffered().use { output ->
                        input.copyTo(output)
                    }
                }

                savedUrl = "/uploads/$filename"
            }
            part.dispose()
        }

        if (savedUrl != null) {
            call.respond(
                HttpStatusCode.Created,
                mapOf("success" to true, "message" to "Upload thành công", "data" to mapOf("url" to savedUrl))
            )
        } else {
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("success" to false, "message" to "Không tìm thấy file trong request. Gửi multipart/form-data với field chứa file ảnh.")
            )
        }
    }

    staticFiles("/uploads", uploadsDir)
}
