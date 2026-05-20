package com.park.data.repository

import com.park.data.network.ApiClient
import com.park.data.network.apiCall
import io.ktor.client.request.forms.*
import io.ktor.http.*
import java.io.File

/**
 * Endpoint upload chung cho mọi loại ảnh (game thumbnail, gallery, announcement banner).
 * Trả về URL của file đã upload.
 */
class UploadRepository {

    suspend fun uploadImage(file: File): Result<String> {
        val mimeType = when (file.extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            else -> "application/octet-stream"
        }

        return apiCall<Map<String, String>>("Lỗi upload ảnh") {
            ApiClient.http.submitFormWithBinaryData(
                url = "/api/upload",
                formData = formData {
                    append("file", file.readBytes(), Headers.build {
                        append(HttpHeaders.ContentDisposition, "filename=\"${file.name}\"")
                        append(HttpHeaders.ContentType, mimeType)
                    })
                }
            )
        }.mapCatching { it["url"] ?: throw Exception("Server không trả về URL") }
    }
}
