package com.example.testnfc.nfc

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import com.example.testnfc.utils.toHexString
import com.example.testnfc.utils.toReadableString
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * HCE Service - giả lập thẻ NFC.
 * AID đăng ký: F0 01 02 03 04 05 06 07
 *
 * State (logs, isActive) được lưu trong companion object để UI có thể
 * quan sát mà không cần bind service.
 */
class CardEmulatorService : HostApduService() {

    companion object {
        // AID target: F001020304050607
        private val TARGET_AID = byteArrayOf(
            0xF0.toByte(), 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07
        )

        // Status bytes
        val STATUS_SUCCESS = byteArrayOf(0x90.toByte(), 0x00.toByte())
        val STATUS_FAILURE = byteArrayOf(0x6F.toByte(), 0x00.toByte())
        val STATUS_NOT_FOUND = byteArrayOf(0x6A.toByte(), 0x82.toByte())

        // StateFlow chia sẻ state với UI
        private val _logs = MutableStateFlow<List<String>>(emptyList())
        val logs: StateFlow<List<String>> = _logs.asStateFlow()

        private val _isActive = MutableStateFlow(false)
        val isActive: StateFlow<Boolean> = _isActive.asStateFlow()

        // Bộ đếm tăng mỗi lần nhận lệnh GET_COUNTER
        private var counter = 0

        private val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        fun clearLogs() {
            _logs.value = emptyList()
        }

        private fun addLog(message: String) {
            val time = timeFormatter.format(Date())
            _logs.value = _logs.value + "[$time] $message"
        }
    }

    /**
     * Xử lý lệnh APDU nhận được từ đầu đọc NFC.
     * Trả về response APDU (dữ liệu + status bytes).
     */
    override fun processCommandApdu(commandApdu: ByteArray, extras: Bundle?): ByteArray {
        addLog("📨 Nhận: ${commandApdu.toHexString()}")

        return try {
            when {
                // Lệnh SELECT AID: 00 A4 04 00 [len] [AID] [Le]
                isSelectAidCommand(commandApdu) -> handleSelectAid()

                // Lệnh GET_MESSAGE: 00 01 00 00
                isCommand(commandApdu, 0x00, 0x01) -> handleGetMessage()

                // Lệnh GET_COUNTER: 00 02 00 00
                isCommand(commandApdu, 0x00, 0x02) -> handleGetCounter()

                // Lệnh ECHO: 00 03 00 00 [Lc] [data]
                isCommand(commandApdu, 0x00, 0x03) -> handleEcho(commandApdu)

                else -> {
                    addLog("❌ Lệnh không hỗ trợ: ${commandApdu.toHexString()}")
                    STATUS_FAILURE
                }
            }
        } catch (e: Exception) {
            addLog("❌ Lỗi xử lý: ${e.message}")
            STATUS_FAILURE
        }
    }

    /** Kiểm tra có phải lệnh SELECT AID không */
    private fun isSelectAidCommand(apdu: ByteArray): Boolean {
        if (apdu.size < 5) return false
        // CLA=00, INS=A4, P1=04, P2=00
        if (apdu[0] != 0x00.toByte() || apdu[1] != 0xA4.toByte() ||
            apdu[2] != 0x04.toByte() || apdu[3] != 0x00.toByte()
        ) return false

        val aidLen = apdu[4].toInt() and 0xFF
        if (apdu.size < 5 + aidLen) return false

        val receivedAid = apdu.copyOfRange(5, 5 + aidLen)
        return receivedAid.contentEquals(TARGET_AID)
    }

    /** Kiểm tra CLA và INS của lệnh */
    private fun isCommand(apdu: ByteArray, cla: Int, ins: Int): Boolean =
        apdu.size >= 2 && apdu[0] == cla.toByte() && apdu[1] == ins.toByte()

    private fun handleSelectAid(): ByteArray {
        _isActive.value = true
        addLog("✅ SELECT AID thành công — kết nối được thiết lập")
        return STATUS_SUCCESS
    }

    private fun handleGetMessage(): ByteArray {
        val message = "Xin chào từ HCE! 👋"
        val data = message.toByteArray(Charsets.UTF_8)
        addLog("📤 GET_MESSAGE → \"$message\"")
        return data + STATUS_SUCCESS
    }

    private fun handleGetCounter(): ByteArray {
        counter++
        val response = "Counter: $counter"
        val data = response.toByteArray(Charsets.UTF_8)
        addLog("📤 GET_COUNTER → $response")
        return data + STATUS_SUCCESS
    }

    private fun handleEcho(apdu: ByteArray): ByteArray {
        // Dữ liệu echo nằm sau: CLA INS P1 P2 Lc
        val data = if (apdu.size > 5) apdu.copyOfRange(5, apdu.size) else byteArrayOf()
        val echoText = if (data.isNotEmpty()) data.toReadableString() else "(rỗng)"
        addLog("📤 ECHO → \"$echoText\"")
        return data + STATUS_SUCCESS
    }

    /**
     * Gọi khi NFC bị ngắt kết nối (tag loss hoặc bị deselect).
     */
    override fun onDeactivated(reason: Int) {
        _isActive.value = false
        val reasonText = when (reason) {
            DEACTIVATION_LINK_LOSS -> "mất tín hiệu NFC"
            DEACTIVATION_DESELECTED -> "bị đầu đọc hủy chọn"
            else -> "lý do không xác định ($reason)"
        }
        addLog("🔌 Ngắt kết nối: $reasonText")
    }
}
