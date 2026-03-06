package com.example.testnfc.nfc

import android.app.Activity
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import com.example.testnfc.utils.toHexString
import com.example.testnfc.utils.toReadableString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Trạng thái của đầu đọc NFC */
enum class ReaderStatus {
    IDLE,           // Chưa bắt đầu quét
    SCANNING,       // Đang chờ thẻ
    TAG_DETECTED,   // Đang giao tiếp với thẻ
    ERROR           // Xảy ra lỗi
}

/**
 * Quản lý chế độ đầu đọc NFC (Reader Mode).
 * Dùng IsoDep để giao tiếp APDU với thẻ HCE.
 */
class NfcReaderManager {

    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    private val _status = MutableStateFlow(ReaderStatus.IDLE)
    val status: StateFlow<ReaderStatus> = _status.asStateFlow()

    // Scope riêng để xử lý NFC trên background thread
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    private fun addLog(message: String) {
        val time = timeFormatter.format(Date())
        _logs.value = _logs.value + "[$time] $message"
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }

    /**
     * Bật chế độ đầu đọc NFC trên Activity.
     * Cờ SKIP_NDEF_CHECK giúp bỏ qua NDEF để xử lý APDU trực tiếp.
     */
    fun enableReaderMode(activity: Activity) {
        val nfcAdapter = NfcAdapter.getDefaultAdapter(activity)
        if (nfcAdapter == null) {
            addLog("❌ Thiết bị không hỗ trợ NFC")
            _status.value = ReaderStatus.ERROR
            return
        }

        if (!nfcAdapter.isEnabled) {
            addLog("❌ NFC chưa được bật. Vui lòng bật NFC trong cài đặt.")
            _status.value = ReaderStatus.ERROR
            return
        }

        val flags = NfcAdapter.FLAG_READER_NFC_A or
                NfcAdapter.FLAG_READER_NFC_B or
                NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK

        nfcAdapter.enableReaderMode(activity, { tag ->
            scope.launch { handleTag(tag) }
        }, flags, null)

        _status.value = ReaderStatus.SCANNING
        addLog("🔍 Bắt đầu quét NFC — đưa điện thoại lại gần thẻ HCE...")
    }

    /**
     * Tắt chế độ đầu đọc, trả Activity về trạng thái bình thường.
     */
    fun disableReaderMode(activity: Activity) {
        try {
            NfcAdapter.getDefaultAdapter(activity)?.disableReaderMode(activity)
        } catch (e: Exception) {
            // Bỏ qua lỗi khi tắt reader mode
        }
        _status.value = ReaderStatus.IDLE
        addLog("⏹️ Đã dừng quét NFC")
    }

    /**
     * Xử lý tag NFC được phát hiện.
     * Thực hiện chuỗi lệnh: SELECT → GET_MESSAGE → GET_COUNTER → ECHO
     */
    private fun handleTag(tag: Tag) {
        val isoDep = IsoDep.get(tag)
        if (isoDep == null) {
            addLog("❌ Tag không hỗ trợ IsoDep/ISO 7816-4")
            return
        }

        try {
            isoDep.connect()
            isoDep.timeout = 5000 // 5 giây timeout
            _status.value = ReaderStatus.TAG_DETECTED
            addLog("✅ Đã phát hiện thẻ NFC!")

            // ── Bước 1: SELECT AID ──────────────────────────────────────
            addLog("── Bước 1: SELECT AID ──")
            val selectCmd = buildSelectAidCommand()
            addLog("📨 Gửi: ${selectCmd.toHexString()}")
            val selectResp = isoDep.transceive(selectCmd)
            addLog("📥 Nhận: ${selectResp.toHexString()}")

            if (!isSuccess(selectResp)) {
                addLog("❌ SELECT AID thất bại — thẻ không nhận dạng được AID")
                return
            }
            addLog("✅ SELECT AID OK")

            // ── Bước 2: GET_MESSAGE ──────────────────────────────────────
            addLog("── Bước 2: GET_MESSAGE ──")
            val getMsgCmd = byteArrayOf(0x00, 0x01, 0x00, 0x00)
            addLog("📨 Gửi: ${getMsgCmd.toHexString()}")
            val getMsgResp = isoDep.transceive(getMsgCmd)
            addLog("📥 Nhận: ${getMsgResp.toHexString()}")
            if (getMsgResp.size > 2 && isSuccess(getMsgResp)) {
                val msgData = getMsgResp.copyOfRange(0, getMsgResp.size - 2)
                addLog("✅ Tin nhắn: \"${msgData.toReadableString()}\"")
            }

            // ── Bước 3: GET_COUNTER ──────────────────────────────────────
            addLog("── Bước 3: GET_COUNTER ──")
            val getCounterCmd = byteArrayOf(0x00, 0x02, 0x00, 0x00)
            addLog("📨 Gửi: ${getCounterCmd.toHexString()}")
            val getCounterResp = isoDep.transceive(getCounterCmd)
            addLog("📥 Nhận: ${getCounterResp.toHexString()}")
            if (getCounterResp.size > 2 && isSuccess(getCounterResp)) {
                val counterData = getCounterResp.copyOfRange(0, getCounterResp.size - 2)
                addLog("✅ ${counterData.toReadableString()}")
            }

            // ── Bước 4: ECHO ─────────────────────────────────────────────
            addLog("── Bước 4: ECHO ──")
            val echoPayload = "Hello NFC!".toByteArray(Charsets.UTF_8)
            val echoCmd = byteArrayOf(0x00, 0x03, 0x00, 0x00, echoPayload.size.toByte()) +
                    echoPayload
            addLog("📨 Gửi: ${echoCmd.toHexString()}")
            val echoResp = isoDep.transceive(echoCmd)
            addLog("📥 Nhận: ${echoResp.toHexString()}")
            if (echoResp.size > 2 && isSuccess(echoResp)) {
                val echoData = echoResp.copyOfRange(0, echoResp.size - 2)
                addLog("✅ Echo: \"${echoData.toReadableString()}\"")
            }

            addLog("🎉 Giao tiếp hoàn tất!")

        } catch (e: Exception) {
            addLog("❌ Lỗi giao tiếp: ${e.message}")
            _status.value = ReaderStatus.ERROR
        } finally {
            try {
                isoDep.close()
            } catch (e: Exception) {
                // Bỏ qua lỗi khi đóng kết nối
            }
            // Quay lại trạng thái quét sau khi xử lý xong
            if (_status.value != ReaderStatus.ERROR) {
                _status.value = ReaderStatus.SCANNING
                addLog("🔍 Tiếp tục quét...")
            }
        }
    }

    /**
     * Tạo lệnh SELECT AID theo chuẩn ISO 7816-4:
     * 00 A4 04 00 [Lc=08] [F0 01 02 03 04 05 06 07] [Le=00]
     */
    private fun buildSelectAidCommand(): ByteArray {
        val aid = byteArrayOf(0xF0.toByte(), 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07)
        return byteArrayOf(0x00, 0xA4.toByte(), 0x04, 0x00, aid.size.toByte()) +
                aid +
                byteArrayOf(0x00)
    }

    /** Kiểm tra response có kết thúc bằng 90 00 (thành công) không */
    private fun isSuccess(response: ByteArray): Boolean {
        if (response.size < 2) return false
        return response[response.size - 2] == 0x90.toByte() &&
                response[response.size - 1] == 0x00.toByte()
    }
}
