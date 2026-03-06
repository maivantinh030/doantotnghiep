package com.example.testnfc.utils

/**
 * Chuyển ByteArray thành chuỗi hex để hiển thị (ví dụ: "00 A4 04 00")
 */
fun ByteArray.toHexString(): String =
    joinToString(" ") { byte -> "%02X".format(byte) }

/**
 * Chuyển chuỗi hex (có hoặc không có dấu cách) thành ByteArray
 */
fun String.hexToByteArray(): ByteArray {
    val hex = replace(" ", "").replace(":", "")
    require(hex.length % 2 == 0) { "Chuỗi hex phải có độ dài chẵn" }
    return ByteArray(hex.length / 2) { i ->
        hex.substring(i * 2, i * 2 + 2).toInt(16).toByte()
    }
}

/**
 * Cố gắng đọc ByteArray như UTF-8 text, fallback về hex nếu không phải text
 */
fun ByteArray.toReadableString(): String =
    try {
        val str = String(this, Charsets.UTF_8)
        // Kiểm tra có phải printable text không
        if (str.all { it.code in 32..126 || it.code > 127 }) str
        else toHexString()
    } catch (e: Exception) {
        toHexString()
    }
