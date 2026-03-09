package org.example.project

/**
 * Singleton object để lưu session key chung cho tất cả các màn hình
 * Session key được tạo một lần và chia sẻ giữa tất cả các instance của SmartCardManager
 */
object SessionKeyStore {
    // Session key cố định - định nghĩa sẵn trong code
    private val SESSION_KEY = byteArrayOf(
        0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
        0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10
    )
    
    private var keySentToCard: Boolean = false // Flag để biết đã gửi key xuống card chưa
    
    /**
     * Lấy session key (luôn trả về key cố định)
     */
    fun getSessionKey(): ByteArray {
        return SESSION_KEY
    }
    
    /**
     * Đánh dấu đã gửi key xuống card
     */
    fun markKeySentToCard() {
        keySentToCard = true
        println("✅ Đã đánh dấu session key đã được gửi xuống card")
    }
    
    /**
     * Kiểm tra đã gửi key xuống card chưa
     */
    fun isKeySentToCard(): Boolean {
        return keySentToCard
    }
    
    /**
     * Reset flag (dùng khi cần gửi lại key)
     */
    fun resetKeySentFlag() {
        keySentToCard = false
        println("🔄 Đã reset flag gửi key")
    }
    
    /**
     * Kiểm tra session key đã sẵn sàng (luôn true vì đã định nghĩa sẵn)
     */
    fun hasSessionKey(): Boolean {
        return true
    }
    
    /**
     * Lấy session key dạng hex string
     */
    fun getSessionKeyHex(): String {
        return SESSION_KEY.joinToString("") { "%02X".format(it) }
    }
    
    /**
     * In session key ra console (để debug)
     */
    fun printSessionKey() {
        val hex = SESSION_KEY.joinToString(" ") { "%02X".format(it) }
        println("🔑 Session Key (cố định, ${SESSION_KEY.size} bytes): $hex")
        println("🔑 Session Key (hex string): ${getSessionKeyHex()}")
    }
}

