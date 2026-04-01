package org.example.project

import java.math.BigInteger
import java.security.KeyFactory
import java.security.spec.RSAPublicKeySpec
import java.util.Base64
import javax.smartcardio.Card
import javax.smartcardio.CardChannel
import javax.smartcardio.CardTerminals
import javax.smartcardio.CommandAPDU
import javax.smartcardio.TerminalFactory

class SmartCardManager {
    private var terminals: CardTerminals? = null
    private var card: Card? = null
    private var channel: CardChannel? = null
    
    // Session key được lưu trong SessionKeyStore để chia sẻ giữa tất cả các instance
    // Không cần lưu riêng trong mỗi instance nữa

    init {
        try {
            val factory = TerminalFactory.getDefault()
            terminals = factory. terminals()
        } catch (e: Exception) {
            println("Error initializing SmartCard: ${e.message}")
        }
    }

    fun listReaders(): List<String> {
        return try {
            terminals?.list()?.map { it.name } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun connectToCard(readerName: String?  = null): Boolean {
        return try {
            val terminal = if (readerName != null) {
                terminals?.list()?.find { it.name == readerName }
            } else {
                terminals?.list()?.firstOrNull()
            }

            if (terminal?. isCardPresent == true) {
                card = terminal.connect("*")
                channel = card?.basicChannel
                if (selectApplet()) {
                    // Session key đã được định nghĩa sẵn trong SessionKeyStore
                    println("🔑 Sử dụng session key cố định")
                    SessionKeyStore.printSessionKey() // In ra để debug
                    
                    // Kiểm tra và gửi session key nếu chưa có (check trước, chỉ gửi khi cần)
                    if (sendSessionKeyToCard()) {
                        println("✅ Session key đã sẵn sàng trong card")
                    } else {
                        println("⚠️ Không thể setup session key")
                    }
                    return true
                }
                return false
            } else {
                println("No card present in reader:  ${readerName ?: "default"}")
                false
            }
        } catch (e: Exception) {
            println("Error connecting to card: ${e.message}")
            false
        }
    }

    fun connectAndVerifyAdminPINEncrypted(
        readerName: String? = null,
        adminPin: String = "9999"
    ): Result<Unit> {
        if (!connectToCard(readerName)) {
            return Result.failure(Exception("Không kết nối được thẻ."))
        }

        if (!verifyAdminPINEncrypted(adminPin)) {
            disconnect()
            return Result.failure(Exception("Xác thực admin PIN thất bại."))
        }

        return Result.success(Unit)
    }
    
    /**
     * Kiểm tra xem session key đã được set trong card chưa
     * INS: 0x26
     * Return: true nếu đã set, false nếu chưa set hoặc có lỗi
     */
    fun getSessionKeyStatus(): Boolean {
        return try {
            val cmd = byteArrayOf(0x80.toByte(), 0x26, 0x00, 0x00, 0x01)
            val response = sendCommand(cmd) ?: return false
            
            val sw = getStatusWord(response)
            if (sw == 0x9000 && response.size >= 3) {
                val status = response[response.size - 3].toInt() and 0xFF
                status == 0x01
            } else {
                false
            }
        } catch (e: Exception) {
            println("Error checking session key status: ${e.message}")
            false
        }
    }
    
    /**
     * Gửi session key xuống card (chỉ gửi nếu chưa có)
     * Session key được lưu trong SessionKeyStore và không đổi
     */
    private fun sendSessionKeyToCard(): Boolean {
        return try {
            // Kiểm tra xem session key đã được set chưa
            if (getSessionKeyStatus()) {
                println("✅ Session key đã có sẵn trong card, không cần gửi lại")
                return true
            }
            
            val sessionKey = SessionKeyStore.getSessionKey()
            if (sessionKey == null) {
                println("❌ Session key chưa được khởi tạo trong SessionKeyStore")
                return false
            }
            
            // Gửi session key xuống card
            println("📤 Gửi session key xuống card...")
            val cmd = byteArrayOf(0x80.toByte(), 0x23, 0x00, 0x00, 0x10) + sessionKey
            val response = sendCommand(cmd) ?: return false
            
            val sw = getStatusWord(response)
            when (sw) {
                0x9000 -> {
                    println("✅ Session key đã được gửi và lưu trong card")
                    true
                }
                else -> {
                    println("❌ Không thể gửi session key: SW=${sw.toString(16)}")
                    false
                }
            }
        } catch (e: Exception) {
            println("Error sending session key: ${e.message}")
            false
        }
    }

    fun sendCommand(commandApdu: ByteArray): ByteArray? {
        return try {
            val command = CommandAPDU(commandApdu)
            val response = channel?.transmit(command)
            response?.bytes
        } catch (e: Exception) {
            null
        }
    }

    private fun selectApplet(): Boolean {
        return try {
            val selectCmd = CommandAPDU(0x00, 0xA4, 0x04, 0x00,
                byteArrayOf(0x11, 0x11, 0x11, 0x11, 0x11, 0x00))
            val response = channel?.transmit(selectCmd)
            response?.sw == 0x9000
        } catch (e: Exception) {
            println("Error selecting applet: ${e.message}")
            false
        }
    }

    fun disconnect() {
        try {
            card?.disconnect(true)
            card = null
            channel = null
        } catch (e: Exception) {
            println("Error disconnecting:  ${e.message}")
        }
    }

    /**
     * Kiểm tra nhanh xem có thẻ trên đầu đọc không (không cần kết nối)
     */
    fun isCardPresent(readerName: String? = null): Boolean {
        return try {
            val terminal = if (readerName != null) {
                terminals?.list()?.find { it.name == readerName }
            } else {
                terminals?.list()?.firstOrNull()
            }
            terminal?.isCardPresent == true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Đọc Physical UID của thẻ NFC bằng lệnh PC/SC chuẩn FF CA 00 00 00.
     * Không cần chọn applet — đọc thẳng UID từ reader.
     * Trả về chuỗi HEX (ví dụ "04A3B2C1") hoặc null nếu thất bại.
     */
    fun readCardUID(readerName: String? = null): String? {
        return try {
            val terminal = if (readerName != null) {
                terminals?.list()?.find { it.name == readerName }
            } else {
                terminals?.list()?.firstOrNull()
            } ?: return null

            if (!terminal.isCardPresent) return null

            val tempCard = terminal.connect("*")
            try {
                val getUidApdu = byteArrayOf(0xFF.toByte(), 0xCA.toByte(), 0x00, 0x00, 0x00)
                val response = tempCard.basicChannel.transmit(CommandAPDU(getUidApdu)).bytes
                if (response.size < 2) return null
                val sw = ((response[response.size - 2].toInt() and 0xFF) shl 8) or
                         (response[response.size - 1].toInt() and 0xFF)
                if (sw != 0x9000) return null
                response.dropLast(2).toByteArray().joinToString("") { "%02X".format(it) }
            } finally {
                tempCard.disconnect(false)
            }
        } catch (e: Exception) {
            println("Error reading card UID: ${e.message}")
            null
        }
    }

    /**
     * Read physical card UID from the current active connection.
     * This is useful after the card was already connected and RSA/admin checks were done.
     */
    fun readConnectedCardUID(): String? {
        return try {
            val response = sendCommand(byteArrayOf(0xFF.toByte(), 0xCA.toByte(), 0x00, 0x00, 0x00))
                ?: return null
            if (response.size < 2) return null

            val sw = ((response[response.size - 2].toInt() and 0xFF) shl 8) or
                (response[response.size - 1].toInt() and 0xFF)
            if (sw != 0x9000) return null

            response.dropLast(2).toByteArray().joinToString("") { "%02X".format(it) }
        } catch (e: Exception) {
            println("Error reading connected card UID: ${e.message}")
            null
        }
    }
    
    /**
     * Reset card nhanh (disconnect và reconnect ngay)
     * Dùng để reset card state nhanh khi test
     */
    fun quickReset(readerName: String? = null): Boolean {
        disconnect()
        // Đợi một chút để card reset
        Thread.sleep(100)
        return connectToCard(readerName)
    }

    fun createPIN(pin: String): Boolean {
        println("User PIN is disabled on card. Skipping createPIN().")
        return true
    }

    /**
     * Verify User PIN đã được mã hóa bằng session key
     * INS: 0x25
     * User PIN được mã hóa bằng session key trước khi gửi xuống card
     */
    fun verifyPINEncrypted(pin: String): Boolean {
        println("User PIN flow was removed. Routing to admin PIN verification.")
        return verifyAdminPINEncrypted(pin)
    }

    fun changePIN(oldPin: String, newPin: String): Boolean {
        println("User PIN is disabled on card. changePIN() is not supported.")
        return false
    }

    fun getPINStatus(): Triple<Int, Boolean, Boolean> {
        // Compatibility bridge for old UI flow: user PIN status now maps to admin PIN status.
        return getAdminPINStatus()
    }

    // ==================== ADMIN PIN MANAGEMENT ====================

    /**
     * Create Admin PIN
     * INS: 0x20
     */
    fun createAdminPIN(pin: String): Boolean {
        return try {
            val pinBytes = pin.toByteArray()
            if (pinBytes.size < 4 || pinBytes.size > 8) {
                println("Admin PIN must be 4-8 characters")
                return false
            }

            val cmd = byteArrayOf(0x80.toByte(), 0x20, 0x00, 0x00, pinBytes.size.toByte()) + pinBytes
            val response = sendCommand(cmd) ?: return false

            val sw = getStatusWord(response)
            when (sw) {
                0x9000 -> {
                    println("Admin PIN created successfully")
                    true
                }
                0x6981 -> {
                    println("Admin PIN already created")
                    false
                }
                else -> {
                    println("Failed to create Admin PIN: SW=${sw.toString(16)}")
                    false
                }
            }
        } catch (e: Exception) {
            println("Error creating Admin PIN: ${e.message}")
            false
        }
    }

    /**
     * Verify Admin PIN đã được mã hóa bằng session key
     * INS: 0x24
     * Admin PIN được mã hóa bằng session key trước khi gửi xuống card
     * Card sẽ dùng session key đã lưu từ lần đầu để giải mã PIN
     */
    fun verifyAdminPINEncrypted(pin: String): Boolean {
        return try {
            val sessionKey = SessionKeyStore.getSessionKey()
            
            // Đảm bảo session key đã được gửi xuống card
            // Chỉ check nếu chưa có (tối ưu: tránh check lại nhiều lần)
            if (!getSessionKeyStatus()) {
                // Chưa có, cần gửi
                if (!sendSessionKeyToCard()) {
                    println("❌ Không thể gửi session key xuống card")
                    return false
                }
            }
            
            val pinBytes = pin.toByteArray()
            
            // Pad PIN lên 16 bytes (AES block size)
            val paddedPin = ByteArray(16) { if (it < pinBytes.size) pinBytes[it] else 0x00 }
            
            // Mã hóa PIN bằng session key từ SessionKeyStore (AES-ECB)
            val encryptedPin = encryptWithSessionKey(paddedPin, sessionKey)
            
            println("🔐 Đã mã hóa admin PIN bằng session key từ SessionKeyStore")
            
            // Gửi admin PIN đã mã hóa xuống card
            // Card sẽ dùng session key đã lưu từ lần đầu để giải mã
            val cmd = byteArrayOf(0x80.toByte(), 0x24, 0x00, 0x00, 0x10) + encryptedPin
            val response = sendCommand(cmd) ?: return false

            val sw = getStatusWord(response)
            when (sw) {
                0x9000 -> {
                    println("✅ Admin PIN verified successfully (encrypted)")
                    true
                }
                0x6983 -> {
                    println("❌ Admin PIN blocked - too many wrong attempts")
                    false
                }
                0x6A80 -> {
                    println("❌ Wrong Admin PIN")
                    false
                }
                0x6985 -> {
                    println("❌ Session key not set on card - card có thể đã bị deselect")
                    // Reset flag để gửi lại key lần sau
                    SessionKeyStore.resetKeySentFlag()
                    false
                }
                else -> {
                    println("❌ Admin PIN verification failed: SW=${sw.toString(16)}")
                    false
                }
            }
        } catch (e: Exception) {
            println("Error verifying Admin PIN (encrypted): ${e.message}")
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Mã hóa data bằng session key (AES-ECB)
     */
    private fun encryptWithSessionKey(data: ByteArray, key: ByteArray): ByteArray {
        val cipher = javax.crypto.Cipher.getInstance("AES/ECB/NoPadding")
        val secretKey = javax.crypto.spec.SecretKeySpec(key, "AES")
        cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, secretKey)
        return cipher.doFinal(data)
    }
    
    /**
     * Lấy session key hiện tại (dạng hex string để debug)
     * Trả về null nếu chưa có session key
     */
    fun getSessionKeyHex(): String? {
        return SessionKeyStore.getSessionKeyHex()
    }
    
    /**
     * Kiểm tra session key đã được khởi tạo chưa
     */
    fun isSessionKeyReady(): Boolean {
        return SessionKeyStore.hasSessionKey()
    }
    
    /**
     * In session key ra console (để debug)
     */
    fun printSessionKey() {
        SessionKeyStore.printSessionKey()
    }

    /**
     * Get Admin PIN Status
     * INS: 0x22
     * Returns: Triple(triesRemaining, pinCreated, pinValidated)
     */
    fun getAdminPINStatus(): Triple<Int, Boolean, Boolean> {
        return try {
            val cmd = byteArrayOf(0x80.toByte(), 0x22, 0x00, 0x00, 0x00)
            println("Sending Admin PIN Status Command: ${cmd.joinToString(" ") { String.format("%02X", it) }}")
            val response = this.sendCommand(cmd) ?: return Triple(-1, false, false)
            println("Admin PIN Status Response: ${response.joinToString(" ") { String.format("%02X", it) }}")
            val sw = getStatusWord(response)
            if (sw == 0x9000 && response.size >= 5) {
                val data = response.dropLast(2).toByteArray()
                val triesLeft = data[0].toInt() and 0xFF
                val pinCreated = data[1].toInt() == 1
                val pinValidated = data[2].toInt() == 1

                Triple(triesLeft, pinCreated, pinValidated)
            } else {
                Triple(-1, false, false)
            }
        } catch (e: Exception) {
            println("Error getting Admin PIN status: ${e.message}")
            Triple(-1, false, false)
        }
    }

    /**
     * Reset User PIN (Admin only)
     * INS: 0x21
     * Input: [newPinLength(1)][newPin bytes]
     */
    fun resetUserPIN(newPin: String): Boolean {
        println("User PIN is disabled on card. resetUserPIN() is not supported.")
        return false
    }

    fun resetPinCounter(): Boolean {
        println("User PIN is disabled on card. resetPinCounter() is not supported.")
        return false
    }

    /**
     * Ghi thông tin khách hàng lên thẻ.
     * Phải gọi connectToCard() + verifyAdminPINEncrypted() trước.
     * Format: [customerID 15][cardUUID 16 raw bytes][name 64][dateOfBirth 16][phoneNumber 16] = 127 bytes.
     * cardId truyền vào dạng ASCII string tối đa 16 ký tự (vd: "CARD260324143022").
     */
    fun writeCustomerInfo(customerID: String, cardId: String, name: String, dateOfBirth: String, phoneNumber: String): Boolean {
        return try {
            val LEN_ID    = 15
            val LEN_UUID  = 16
            val LEN_NAME  = 64
            val LEN_DOB   = 16
            val LEN_PHONE = 16
            val data = ByteArray(LEN_ID + LEN_UUID + LEN_NAME + LEN_DOB + LEN_PHONE) // 127 bytes

            customerID.toByteArray(Charsets.UTF_8).let { it.copyInto(data, 0, 0, minOf(it.size, LEN_ID)) }
            cardId.toByteArray(Charsets.UTF_8).let { it.copyInto(data, LEN_ID, 0, minOf(it.size, LEN_UUID)) }
            name.toByteArray(Charsets.UTF_8).let { it.copyInto(data, LEN_ID + LEN_UUID, 0, minOf(it.size, LEN_NAME)) }
            dateOfBirth.toByteArray(Charsets.UTF_8).let { it.copyInto(data, LEN_ID + LEN_UUID + LEN_NAME, 0, minOf(it.size, LEN_DOB)) }
            phoneNumber.toByteArray(Charsets.UTF_8).let { it.copyInto(data, LEN_ID + LEN_UUID + LEN_NAME + LEN_DOB, 0, minOf(it.size, LEN_PHONE)) }

            val command = byteArrayOf(0x80.toByte(), 0x07, 0x00, 0x00, data.size.toByte()) + data
            val response = sendCommand(command)
            response?.takeLast(2)?.toByteArray()?.contentEquals(byteArrayOf(0x90.toByte(), 0x00)) ?: false
        } catch (e: Exception) {
            println("Error writing data to card: ${e.message}")
            false
        }
    }

    /** Parse UUID string "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx" → 16 raw bytes. */
    private fun uuidToBytes(uuid: String): ByteArray {
        val hex = uuid.replace("-", "")
        return ByteArray(16) { hex.substring(it * 2, it * 2 + 2).toInt(16).toByte() }
    }

    /** Convert 16 raw bytes → UUID string "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx". */
    fun bytesToUuid(bytes: ByteArray): String {
        val h = bytes.joinToString("") { "%02x".format(it) }
        return "${h.substring(0,8)}-${h.substring(8,12)}-${h.substring(12,16)}-${h.substring(16,20)}-${h.substring(20,32)}"
    }

    /**
     * Set customerID (plain text) lên thẻ — không cần admin PIN.
     * INS: 0x17, tối đa 15 bytes.
     */
//    fun setCustomerID(customerID: String): Boolean {
//        return try {
//            val bytes = customerID.toByteArray(Charsets.UTF_8).let {
//                if (it.size > 15) it.copyOf(15) else it
//            }
//            val command = byteArrayOf(0x80.toByte(), 0x17, 0x00, 0x00, bytes.size.toByte()) + bytes
//            val response = sendCommand(command)
//            response?.takeLast(2)?.toByteArray()?.contentEquals(byteArrayOf(0x90.toByte(), 0x00)) ?: false
//        } catch (e: Exception) {
//            println("Error setting customerID: ${e.message}")
//            false
//        }
//    }

    fun startPhotoWrite(): Boolean {
        val command = byteArrayOf(0x80.toByte(), 0x08, 0x00, 0x00, 0x00)
        val response = this.sendCommand(command) ?: return false
        println(response)
        return response.takeLast(2).toByteArray().contentEquals(byteArrayOf(0x90.toByte(), 0x00. toByte()))
    }

    fun writePhotoChunk(photoChunk: ByteArray): Boolean {
        val command = byteArrayOf(0x80.toByte(), 0x09, 0x00, 0x00, photoChunk.size.toByte()) + photoChunk
        val response = this.sendCommand(command) ?: return false
        println(response)
        return response.takeLast(2).toByteArray().contentEquals(byteArrayOf(0x90.toByte(), 0x00.toByte()))
    }

    fun finishPhotoWrite(): Boolean {
        val command = byteArrayOf(0x80.toByte(), 0x0A, 0x00, 0x00, 0x00)
        val response = this.sendCommand(command) ?: return false
        return response.takeLast(2).toByteArray().contentEquals(byteArrayOf(0x90.toByte(), 0x00.toByte()))
    }

    fun writeCustomerImage(imageData: ByteArray): Boolean {
        println("🖼️ Bắt đầu ghi ảnh (${imageData.size} bytes)...")
        if (!startPhotoWrite()) {
            println("❌ Không thể khởi tạo ghi ảnh!")
            return false
        }
        val chunkSize = 200
        var offset = 0
        var chunkCount = 0
        while (offset < imageData.size) {
            val end = minOf(offset + chunkSize, imageData.size)
            val chunk = imageData.copyOfRange(offset, end)
            println("  📤 Ghi chunk ${++chunkCount}: offset=$offset, size=${chunk.size}")
            val ok = writePhotoChunk(chunk)
            if (!ok) {
                println("  ❌ Lỗi ghi chunk tại offset $offset")
                return false
            }
            offset = end
        }
        println("  ✅ Đã ghi xong tất cả chunks")
        val finishCmd = byteArrayOf(0x80.toByte(), 0x0A, 0x00, 0x00, 0x00)
        val finishResponse = sendCommand(finishCmd) ?: return false
        val finishOk = finishResponse.takeLast(2).toByteArray()
            .contentEquals(byteArrayOf(0x90.toByte(), 0x00))

        if (finishOk) {
            println("Photo upload completed successfully!")
            return true
        }
        return true
    }

    /**
     * Đọc thông tin khách hàng từ thẻ.
     * Phải gọi connectToCard() + verifyAdminPINEncrypted() trước.
     * Format nhận: [customerID 15][cardUUID 16 raw][name 64][dateOfBirth 16][phoneNumber 16] = 127 bytes.
     * Trả về map: customerID, cardUUID (UUID string), name, dateOfBirth, phoneNumber.
     */
    fun readCustomerInfo(): Map<String, String> {
        return try {
            val LEN_ID    = 15
            val LEN_UUID  = 16
            val LEN_NAME  = 64
            val LEN_DOB   = 16
            val LEN_PHONE = 16
            val INFO_LEN  = LEN_ID + LEN_UUID + LEN_NAME + LEN_DOB + LEN_PHONE // 127 bytes

            val cmd = byteArrayOf(0x80.toByte(), 0x0B, 0x00, 0x00, INFO_LEN.toByte())
            val response = sendCommand(cmd) ?: return emptyMap()
            if (getStatusWord(response) != 0x9000) return emptyMap()

            val data = response.dropLast(2).toByteArray()
            if (data.size < INFO_LEN) return emptyMap()

            var pos = 0
            val customerID  = String(data, pos, LEN_ID, Charsets.UTF_8).trim('\u0000', ' ')
            pos += LEN_ID
            val cardUUID    = String(data, pos, LEN_UUID, Charsets.UTF_8).trim('\u0000', ' ')
            pos += LEN_UUID
            val name        = String(data, pos, LEN_NAME, Charsets.UTF_8).trim('\u0000', ' ')
            pos += LEN_NAME
            val dateOfBirth = String(data, pos, LEN_DOB, Charsets.UTF_8).trim('\u0000', ' ')
            pos += LEN_DOB
            val phoneNumber = String(data, pos, LEN_PHONE, Charsets.UTF_8).trim('\u0000', ' ')

            mapOf(
                "customerID"  to customerID,
                "cardUUID"    to cardUUID,
                "name"        to name,
                "dateOfBirth" to dateOfBirth,
                "phoneNumber" to phoneNumber
            )
        } catch (e: Exception) {
            println("❌ Lỗi đọc thông tin: ${e.message}")
            emptyMap()
        }
    }
    // ✅ HÀM DEBUG - Kiểm tra photoLength
    fun debugPhotoInfo() {
        try {
            val LEN_ID = 15; val LEN_NAME = 64; val LEN_DOB = 16; val LEN_PHONE = 16
            val INFO_LEN = LEN_ID + LEN_NAME + LEN_DOB + LEN_PHONE + 2
            val cmd = byteArrayOf(0x80.toByte(), 0x0B, 0x00, 0x00, INFO_LEN.toByte())
            val response = sendCommand(cmd)

            if (response != null && response.size >= INFO_LEN + 2) {
                val data = response.dropLast(2).toByteArray()
                val photoLenHigh = data[LEN_ID + LEN_NAME + LEN_DOB + LEN_PHONE].toInt() and 0xFF
                val photoLenLow = data[LEN_ID + LEN_NAME + LEN_DOB + LEN_PHONE + 1].toInt() and 0xFF
                val photoLength = (photoLenHigh shl 8) or photoLenLow

                println("🔍 DEBUG INFO:")
                println("   Response size: ${response.size}")
                println("   Photo Length: $photoLength bytes")
                println("   Raw bytes [${INFO_LEN - 2}-${INFO_LEN - 1}]: ${data[INFO_LEN - 2].toString(16)}, ${data[INFO_LEN - 1].toString(16)}")
            } else {
                println("❌ Invalid response: ${response?.size ?: 0} bytes")
            }
        } catch (e: Exception) {
            println("❌ Error: ${e.message}")
        }
    }
    // ✅ HÀM MỚI:  Đọc ảnh khách hàng
    fun readCustomerImage(): ByteArray? {
        return try {
            val LEN_ID = 15  // ✅ FIX: Match với readCustomerInfo()
            val LEN_NAME = 64; val LEN_DOB = 16; val LEN_PHONE = 16
            val INFO_LEN = LEN_ID + LEN_NAME + LEN_DOB + LEN_PHONE + 2

            println("📸 Đọc info để lấy photo length...")
            val infoCmd = byteArrayOf(0x80.toByte(), 0x0B, 0x00, 0x00, INFO_LEN.toByte())
            val infoResponse = sendCommand(infoCmd) ?: run {
                println("❌ Lệnh đọc info thất bại")
                return null
            }

            println("📦 Info response size: ${infoResponse.size}")
            if (infoResponse.size < INFO_LEN + 2) {
                println("❌ Response quá nhỏ: ${infoResponse.size} < ${INFO_LEN + 2}")
                return null
            }

            val data = infoResponse.dropLast(2).toByteArray()
            val pos = LEN_ID + LEN_NAME + LEN_DOB + LEN_PHONE
            val photoLengthHigh = data[pos].toInt() and 0xFF
            val photoLengthLow = data[pos + 1].toInt() and 0xFF
            val photoLength = (photoLengthHigh shl 8) or photoLengthLow

            println("🖼️ Photo length từ card: $photoLength bytes (high=$photoLengthHigh, low=$photoLengthLow)")

            if (photoLength == 0) {
                println("⚠️ Không có ảnh trên thẻ")
                return null
            }
            if (photoLength > 8000) {
                println("⚠️ Kích thước ảnh quá lớn: $photoLength > 8000")
                return null
            }

            val photoData = mutableListOf<Byte>()
            var offset = 0
            val chunkSize = 240 // Đọc 240 bytes mỗi lần (phù hợp với card side limit)

            println("🔄 Bắt đầu đọc ảnh từ thẻ...")
            while (offset < photoLength) {
                val p1 = (offset shr 8).toByte()
                val p2 = (offset and 0xFF).toByte()
                val requestSize = minOf(chunkSize, photoLength - offset)

                println("  📥 Đọc chunk: offset=$offset, size=$requestSize")
                val readCmd = byteArrayOf(0x80.toByte(), 0x0C, p1, p2, requestSize.toByte())
                val response = sendCommand(readCmd)
                
                if (response == null) {
                    println("  ❌ Chunk response null")
                    break
                }

                val sw = getStatusWord(response)
                println("  Status word: 0x${sw.toString(16).padStart(4, '0')}")
                
                if (sw != 0x9000) {
                    println("  ❌ Lỗi đọc chunk")
                    break
                }

                val chunk = response.dropLast(2).toByteArray()
                println("  ✅ Đã đọc ${chunk.size} bytes")
                
                if (chunk.isEmpty()) {
                    println("  ❌ Chunk rỗng")
                    break
                }

                photoData.addAll(chunk.toList())
                offset += chunk.size
            }

            println("✅ Đã đọc xong ảnh: ${photoData.size} bytes")
            if (photoData.isEmpty()) null else photoData.toByteArray()
        } catch (e: Exception) {
            println("❌ Exception đọc ảnh: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    fun readCustomerDataComplete(): Customer? {
        return try {
            val basicInfo = readCustomerBasicInfo()
            if (basicInfo == null) {
                println("Failed to read basic info")
                return null
            }
            val customer = parseCustomerBasicInfo(basicInfo)
            val photoData = readPhotoSimple()
            customer. copy(anhKH = photoData)
        } catch (e: Exception) {
            println("Error reading complete data: ${e.message}")
            null
        }
    }

    private fun readCustomerBasicInfo(): ByteArray? {
        val LEN_ID = 15; val LEN_NAME = 64; val LEN_DOB = 16; val LEN_PHONE = 16
        val INFO_LEN = LEN_ID + LEN_NAME + LEN_DOB + LEN_PHONE + 2
        val readCmd = byteArrayOf(0x80.toByte(), 0x0B, 0x00, 0x00, INFO_LEN.toByte())
        val response = sendCommand(readCmd) ?: return null

        val sw = getStatusWord(response)
        return if (sw == 0x9000) {
            response.dropLast(2).toByteArray()
        } else {
            println("Failed to read basic info: SW=${sw.toString(16)}")
            null
        }
    }

    private fun readPhotoSimple(): ByteArray? {
        return try {
            val chunkSize = 240 // Đọc 240 bytes mỗi lần (phù hợp với card side limit)
            var offset = 0
            val photoData = mutableListOf<Byte>()

            println("Reading photo in chunks...")

            while (true) {
                val p1 = (offset shr 8) and 0xFF
                val p2 = offset and 0xFF

                val chunkCmd = byteArrayOf(
                    0x80.toByte(), 0x0C,
                    p1.toByte(), p2.toByte(),
                    chunkSize.toByte()
                )

                val response = sendCommand(chunkCmd)
                if (response == null) {
                    println("No response at offset $offset")
                    break
                }

                val sw = getStatusWord(response)
                if (sw != 0x9000) {
                    if (sw == 0x6A86) {
                        println("End of photo reached")
                    } else {
                        println("Error reading chunk:  SW=${sw.toString(16)}")
                    }
                    break
                }

                val chunk = response.dropLast(2).toByteArray()

                if (chunk.isEmpty()) {
                    println("Empty chunk - end of photo")
                    break
                }

                photoData.addAll(chunk.toList())

                if (chunk.size < chunkSize) {
                    println("Last chunk:  ${chunk.size} bytes")
                    break
                }

                offset += chunk.size
                println("Read chunk: ${chunk. size} bytes, total: ${photoData.size}")
            }

            return if (photoData.isNotEmpty()) {
                println("Photo read complete: ${photoData.size} bytes")
                photoData. toByteArray()
            } else {
                println("No photo data")
                null
            }

        } catch (e: Exception) {
            println("Error reading photo: ${e.message}")
            null
        }
    }
    private fun parseCustomerBasicInfo(data: ByteArray): Customer {
        val LEN_ID = 16; val LEN_NAME = 64; val LEN_DOB = 16; val LEN_PHONE = 16
        var pos = 0
        val maKH = String(data, pos, LEN_ID).trim('\u0000', ' ')
        pos += LEN_ID
        val hoTen = String(data, pos, LEN_NAME).trim('\u0000', ' ')
        pos += LEN_NAME
        val ngaySinh = String(data, pos, LEN_DOB).trim('\u0000', ' ')
        pos += LEN_DOB
        val soDienThoai = String(data, pos, LEN_PHONE).trim('\u0000', ' ')
        pos += LEN_PHONE

        val photoLen = if (data.size >= pos + 2) {
            ((data[pos].toInt() and 0xFF) shl 8) or (data[pos + 1].toInt() and 0xFF)
        } else 0

        val loaiThe = "" // removed from card model

        println("Customer: '$maKH', '$hoTen', Photo length: $photoLen")
        return Customer(maKH, hoTen, ngaySinh, soDienThoai, loaiThe, null)
    }

    private fun getStatusWord(response: ByteArray): Int {
        return if (response.size >= 2) {
            val sw1 = response[response.size - 2].toInt() and 0xFF
            val sw2 = response[response. size - 1].toInt() and 0xFF
            (sw1 shl 8) or sw2
        } else -1
    }

    // ==================== BALANCE MANAGEMENT ====================

    fun rechargeBalance(amount: Int): Boolean {
        return try {
            if (amount <= 0 || amount > 30000) {
                println("Invalid amount:  $amount")
                return false
            }
            val amountBytes = byteArrayOf(
                (amount shr 8).toByte(),
                (amount and 0xFF).toByte()
            )
            val cmd = byteArrayOf(0x80.toByte(), 0x0D, 0x00, 0x00, 0x02) + amountBytes
            val response = sendCommand(cmd) ?: return false

            val sw = getStatusWord(response)
            when (sw) {
                0x9000 -> {
                    println("Recharge successful: $amount VNĐ")
                    true
                }
                else -> {
                    println("Recharge failed: SW=${sw. toString(16)}")
                    false
                }
            }
        } catch (e: Exception) {
            println("Error recharging balance: ${e.message}")
            false
        }
    }

    fun checkBalance(): Int {
        return try {
            val cmd = byteArrayOf(0x80.toByte(), 0x0E, 0x00, 0x00, 0x02)
            val response = sendCommand(cmd) ?: return -1

            val sw = getStatusWord(response)
            if (sw == 0x9000 && response.size >= 4) {
                val data = response.dropLast(2).toByteArray()
                val balance = ((data[0].toInt() and 0xFF) shl 8) or (data[1].toInt() and 0xFF)
                println("Current balance: $balance VNĐ")
                balance
            } else {
                println("Failed to check balance: SW=${sw. toString(16)}")
                -1
            }
        } catch (e: Exception) {
            println("Error checking balance: ${e.message}")
            -1
        }
    }

    fun makePayment(amount: Int): Boolean {
        return try {
            if (amount <= 0) {
                println("Invalid payment amount: $amount")
                return false
            }

            val amountBytes = byteArrayOf(
                (amount shr 8).toByte(),
                (amount and 0xFF).toByte()
            )

            val cmd = byteArrayOf(0x80.toByte(), 0x0F, 0x00, 0x00, 0x02) + amountBytes
            val response = sendCommand(cmd) ?: return false

            val sw = getStatusWord(response)
            when (sw) {
                0x9000 -> {
                    println("Payment successful: $amount VNĐ")
                    true
                }
                0x6901 -> {
                    println("Insufficient balance")
                    false
                }
                else -> {
                    println("Payment failed: SW=${sw. toString(16)}")
                    false
                }
            }
        } catch (e: Exception) {
            println("Error making payment: ${e.message}")
            false
        }
    }

    // ==================== GAME MANAGEMENT ====================

    fun readGames(): List<GameEntry> {
        return try {
            val cmd = byteArrayOf(0x80.toByte(), 0x13, 0x00, 0x00, 0x00)
            val response = sendCommand(cmd) ?: return emptyList()

            val sw = getStatusWord(response)
            if (sw != 0x9000) {
                println("Failed to read games: SW=${sw.toString(16)}")
                return emptyList()
            }

            val data = response.dropLast(2).toByteArray()
            if (data.size < 2) {
                return emptyList()
            }

            val gameCount = ((data[0].toInt() and 0xFF) shl 8) or (data[1].toInt() and 0xFF)

            if (gameCount == 0) {
                println("No games found")
                return emptyList()
            }

            val games = mutableListOf<GameEntry>()
            var pos = 2

            for (i in 0 until gameCount) {
                if (pos + 3 > data.size) break

                val tickets = data[pos].toInt() and 0xFF
                val gameCodeHigh = data[pos + 1].toInt() and 0xFF
                val gameCodeLow = data[pos + 2]. toInt() and 0xFF
                val gameCode = (gameCodeHigh shl 8) or gameCodeLow

                games.add(GameEntry(gameCode, tickets))
                pos += 3
            }

            println("Read ${games.size} games")
            games

        } catch (e: Exception) {
            println("Error reading games: ${e.message}")
            emptyList()
        }
    }

    fun addOrIncreaseTickets(gameCode: Int, tickets: Int): Boolean {
        return try {
            if (tickets <= 0 || tickets > 255) {
                println("Invalid tickets amount: $tickets")
                return false
            }

            val data = byteArrayOf(
                tickets.toByte(),
                (gameCode shr 8).toByte(),
                (gameCode and 0xFF).toByte()
            )

            val cmd = byteArrayOf(0x80.toByte(), 0x11, 0x00, 0x00, 0x03) + data
            val response = sendCommand(cmd) ?: return false

            val sw = getStatusWord(response)
            when (sw) {
                0x9000 -> {
                    println("Added/Increased $tickets tickets for game $gameCode")
                    true
                }
                else -> {
                    println("Failed to add tickets: SW=${sw.toString(16)}")
                    false
                }
            }
        } catch (e: Exception) {
            println("Error adding tickets: ${e.message}")
            false
        }
    }

    fun decreaseGameTickets(gameCode: Int, tickets: Int): Boolean {
        return try {
            if (tickets <= 0 || tickets > 255) {
                println("Invalid tickets amount: $tickets")
                return false
            }

            val data = byteArrayOf(
                (gameCode shr 8).toByte(),
                (gameCode and 0xFF).toByte(),
                tickets.toByte()
            )

            val cmd = byteArrayOf(0x80.toByte(), 0x12, 0x00, 0x00, 0x03) + data
            val response = sendCommand(cmd) ?: return false

            val sw = getStatusWord(response)
            when (sw) {
                0x9000 -> {
                    println("Decreased $tickets tickets for game $gameCode")
                    true
                }
                0x6901 -> {
                    println("Insufficient tickets")
                    false
                }
                else -> {
                    println("Failed to decrease tickets: SW=${sw.toString(16)}")
                    false
                }
            }
        } catch (e: Exception) {
            println("Error decreasing tickets: ${e. message}")
            false
        }
    }

    fun updateGameTickets(gameCode: Int, newTickets: Int): Boolean {
        return try {
            if (newTickets < 0 || newTickets > 255) {
                println("Invalid tickets amount: $newTickets")
                return false
            }

            val data = byteArrayOf(
                (gameCode shr 8).toByte(),
                (gameCode and 0xFF).toByte(),
                newTickets.toByte()
            )

            val cmd = byteArrayOf(0x80.toByte(), 0x14, 0x00, 0x00, 0x03) + data
            val response = sendCommand(cmd) ?: return false

            val sw = getStatusWord(response)
            when (sw) {
                0x9000 -> {
                    println("Updated game $gameCode to $newTickets tickets")
                    true
                }
                else -> {
                    println("Failed to update tickets: SW=${sw. toString(16)}")
                    false
                }
            }
        } catch (e: Exception) {
            println("Error updating tickets: ${e.message}")
            false
        }
    }

    fun findGame(gameCode: Int): GameEntry? {
        return try {
            val data = byteArrayOf(
                (gameCode shr 8).toByte(),
                (gameCode and 0xFF).toByte()
            )

            val cmd = byteArrayOf(0x80.toByte(), 0x15, 0x00, 0x00, 0x02) + data
            val response = sendCommand(cmd) ?: return null

            val sw = getStatusWord(response)
            if (sw != 0x9000) {
                println("Failed to find game: SW=${sw.toString(16)}")
                return null
            }

            val responseData = response.dropLast(2).toByteArray()
            if (responseData.isEmpty() || responseData[0].toInt() == 0) {
                println("Game $gameCode not found")
                return null
            }

            if (responseData.size >= 4) {
                val tickets = responseData[1].toInt() and 0xFF
                val foundGameCodeHigh = responseData[2]. toInt() and 0xFF
                val foundGameCodeLow = responseData[3].toInt() and 0xFF
                val foundGameCode = (foundGameCodeHigh shl 8) or foundGameCodeLow

                println("Found game $foundGameCode with $tickets tickets")
                GameEntry(foundGameCode, tickets)
            } else {
                null
            }
        } catch (e: Exception) {
            println("Error finding game: ${e. message}")
            null
        }
    }

    fun removeGame(gameCode: Int): Boolean {
        return try {
            val data = byteArrayOf(
                (gameCode shr 8).toByte(),
                (gameCode and 0xFF).toByte()
            )

            val cmd = byteArrayOf(0x80.toByte(), 0x16, 0x00, 0x00, 0x02) + data
            val response = sendCommand(cmd) ?: return false

            val sw = getStatusWord(response)
            when (sw) {
                0x9000 -> {
                    println("Removed game $gameCode")
                    true
                }
                0x6A82 -> {
                    println("Game $gameCode not found")
                    false
                }
                else -> {
                    println("Failed to remove game: SW=${sw.toString(16)}")
                    false
                }
            }
        } catch (e: Exception) {
            println("Error removing game: ${e.message}")
            false
        }
    }

    // ==================== RSA AUTHENTICATION ====================

    /**
     * Set Customer ID for RSA authentication (plain text, max 15 bytes)
     * INS: 0x90
     */
    fun setCustomerID(customerId: String): Boolean {
        return try {
            val idBytes = customerId.toByteArray().take(15).toByteArray()
            val cmd = byteArrayOf(0x80.toByte(), 0x17, 0x00, 0x00, idBytes.size.toByte()) + idBytes
            val response = sendCommand(cmd) ?: return false
            
            val sw = getStatusWord(response)
            when (sw) {
                0x9000 -> {
                    println("Customer ID set successfully")
                    true
                }
                else -> {
                    println("Failed to set customer ID: SW=${sw.toString(16)}")
                    false
                }
            }
        } catch (e: Exception) {
            println("Error setting customer ID: ${e.message}")
            false
        }
    }

    /**
     * Get Customer ID (no PIN required, plain text)
     * INS: 0x91
     */
    fun getCustomerIDRSA(): String? {
        return try {
            val cmd = byteArrayOf(0x80.toByte(), 0x18, 0x00, 0x00, 0x0F) // 15 bytes
            val response = sendCommand(cmd) ?: return null
            
            val sw = getStatusWord(response)
            if (sw == 0x9000) {
                val idBytes = response.dropLast(2).toByteArray()
                String(idBytes).trim('\u0000', ' ')
            } else {
                println("Failed to get customer ID: SW=${sw.toString(16)}")
                null
            }
        } catch (e: Exception) {
            println("Error getting customer ID: ${e.message}")
            null
        }
    }



    fun signChallenge(challenge: ByteArray): ByteArray? {
        return try {
            if (challenge.size != 32) {
                println("Challenge must be 32 bytes, got ${challenge.size}")
                return null
            }
            // Card expects exactly 32 bytes in the incoming buffer; no Le byte is needed.
            val cmd = byteArrayOf(0x80.toByte(), 0x1B, 0x00, 0x00, 0x20) + challenge
            val response = sendCommand(cmd) ?: return null
            
            val sw = getStatusWord(response)
            when (sw) {
                0x9000 -> {
                    val signature = response.dropLast(2).toByteArray()
                    println("Challenge signed successfully, signature size: ${signature.size}")
                    signature
                }
                0x6A88 -> {
                    println("RSA not ready (keys not set)")
                    null
                }
                else -> {
                    println("Failed to sign challenge: SW=${sw.toString(16)}")
                    null
                }
            }
        } catch (e: Exception) {
            println("Error signing challenge: ${e.message}")
            null
        }
    }

    /**
     * Get RSA status (check if keys are configured)
     * INS: 0x1C
     * Returns: true if RSA ready, false otherwise
     */
    fun getRSAStatus(): Boolean {
        return try {
            val cmd = byteArrayOf(0x80.toByte(), 0x1C, 0x00, 0x00, 0x01)
            val response = sendCommand(cmd) ?: return false
            
            val sw = getStatusWord(response)
            if (sw == 0x9000) {
                val status = response.dropLast(2).firstOrNull() ?: 0x00
                status.toInt() == 0x01
            } else {
                println("Failed to get RSA status: SW=${sw.toString(16)}")
                false
            }
        } catch (e: Exception) {
            println("Error getting RSA status: ${e.message}")
            false
        }
    }
    
    /**
     * INS_GENERATE_RSA_KEYPAIR (0x1D)
     * Generate RSA keypair trong thẻ
     * Lưu ý: Command này mất vài giây để thực hiện
     */
    fun generateRSAKeyPair(): Boolean {
        return try {
            println("🔄 Generating RSA keypair in card...")
            val command = byteArrayOf(0x80.toByte(), 0x1D, 0x00, 0x00, 0x00)
            val response = this.sendCommand(command) ?: return false
            
            val success = getStatusWord(response) == 0x9000
            if (success) {
                println("✅ RSA keypair generated successfully")
            } else {
                println("❌ Failed to generate RSA keypair: ${getStatusWord(response).toString(16)}")
            }
            success
        } catch (e: Exception) {
            println("⚠️ generateRSAKeyPair error: ${e.message}")
            false
        }
    }
    
    /**
     * INS_GET_PUBLIC_KEY (0x1E)
     * Lấy RSA public key từ thẻ
     * component: 0x00 = modulus (128 bytes), 0x01 = exponent (3 bytes)
     */
    fun getPublicKeyComponent(component: Byte): ByteArray? {
        return try {
            val command = byteArrayOf(0x80.toByte(), 0x1E, 0x00, 0x00, 0x01, component, 0x00)
            val response = this.sendCommand(command) ?: return null
            
            if (getStatusWord(response) == 0x9000) {
                // Return data without status word
                response.dropLast(2).toByteArray()
            } else {
                println("❌ Failed to get public key component: ${getStatusWord(response).toString(16)}")
                null
            }
        } catch (e: Exception) {
            println("⚠️ getPublicKeyComponent error: ${e.message}")
            null
        }
    }
    
    /**
     * Lấy full RSA public key (modulus + exponent)
     * Return: Pair(modulus, exponent) hoặc null nếu thất bại
     */
    fun getPublicKey(): Pair<ByteArray, ByteArray>? {
        return try {
            val modulus = getPublicKeyComponent(0x00) ?: return null
            val exponent = getPublicKeyComponent(0x01) ?: return null
            
            println("✅ Got public key: modulus=${modulus.size} bytes, exponent=${exponent.size} bytes")
            Pair(modulus, exponent)
        } catch (e: Exception) {
            println("⚠️ getPublicKey error: ${e.message}")
            null
        }
    }
    
    /**
     * Lấy public key và convert sang PEM format
     * Return: PEM string hoặc null nếu thất bại
     */
    fun getPublicKeyAsPEM(): String? {
        return try {
            val (modulusBytes, exponentBytes) = getPublicKey() ?: return null
            
            // Convert bytes to BigInteger
            val modulus = BigInteger(1, modulusBytes)
            val exponent = BigInteger(1, exponentBytes)
            
            // Create RSA public key
            val keySpec = RSAPublicKeySpec(modulus, exponent)
            val keyFactory = KeyFactory.getInstance("RSA")
            val publicKey = keyFactory.generatePublic(keySpec)
            
            // Convert to PEM format
            val encoded = publicKey.encoded
            val base64 = Base64.getEncoder().encodeToString(encoded)
            val pem = "-----BEGIN PUBLIC KEY-----\n" +
                    base64.chunked(64).joinToString("\n") +
                    "\n-----END PUBLIC KEY-----"
            
            println("✅ Public key converted to PEM format")
            pem
        } catch (e: Exception) {
            println("⚠️ getPublicKeyAsPEM error: ${e.message}")
            null
        }
    }
    
    /**
     * Xác thực RSA: Sign challenge → Verify với server
     * Lưu ý: Admin PIN đã được verify ở ConnectScreen, không cần verify lại
     * Master key và RSA private key đã được unwrap/decrypt sẵn
     */
    fun authenticateRSA(adminPin: String, rsaApi: org.example.project.network.RSAApiClient): Boolean {
        return try {
            // Admin PIN đã được verify ở ConnectScreen, chỉ cần sign challenge và verify với server
            // 1. Lấy challenge và sign
            val custId = getCustomerIDRSA() ?: return false
            val challengeDto = rsaApi.getChallenge().getOrElse { return false }
            val challengeBytes = Base64.getDecoder().decode(challengeDto.challenge)
            if (challengeBytes.size != 32) return false
            
            val signature = signChallenge(challengeBytes) ?: return false
            
            // 2. Verify với server
            val sigB64 = Base64.getEncoder().encodeToString(signature)
            val verifyResp = rsaApi.verifySignature(custId, challengeDto.challenge, sigB64).getOrElse { return false }
            
            verifyResp.success
        } catch (e: Exception) {
            println("❌ Lỗi xác thực RSA: ${e.message}")
            false
        }
    }

}
