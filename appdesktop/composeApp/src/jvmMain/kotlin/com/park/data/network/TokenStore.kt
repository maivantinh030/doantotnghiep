package com.park.data.network

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.PosixFilePermissions
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.notExists
import kotlin.io.path.readText
import kotlin.io.path.writeText

/**
 * Lưu JWT token vào file `~/.park-admin/token` để giữ phiên đăng nhập qua các lần mở app.
 *
 * Lưu ý bảo mật:
 * - Trên Unix-like (macOS/Linux) set quyền 600 — chỉ owner đọc được.
 * - Trên Windows file nằm trong user profile, đã isolate sẵn theo OS.
 * - Đây không phải lưu trữ "đủ an toàn cho prod hardening" — admin nội bộ là acceptable.
 *   Hardening sau: encrypt bằng OS keystore (Windows DPAPI / macOS Keychain).
 */
object TokenStore {

    private val tokenFile: Path = Paths.get(System.getProperty("user.home"), ".park-admin", "token")

    fun load(): String? {
        return try {
            if (tokenFile.notExists()) null
            else tokenFile.readText().trim().takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        }
    }

    fun save(token: String) {
        try {
            val dir = tokenFile.parent
            if (dir != null && !dir.exists()) {
                Files.createDirectories(dir)
            }
            tokenFile.writeText(token)
            runCatching {
                // Best-effort: hạn chế quyền trên hệ Unix. Windows bỏ qua silent.
                Files.setPosixFilePermissions(tokenFile, PosixFilePermissions.fromString("rw-------"))
            }
        } catch (_: Exception) {
            // Token vẫn còn in-memory ở ApiClient; mất file chỉ ảnh hưởng lần restart sau.
        }
    }

    fun clear() {
        runCatching { tokenFile.deleteIfExists() }
    }
}
