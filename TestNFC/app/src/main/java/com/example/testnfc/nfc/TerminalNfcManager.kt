package com.example.testnfc.nfc

import android.app.Activity
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class TerminalNfcStatus {
    IDLE,       // Chưa bật
    SCANNING,   // Đang chờ thẻ
    READING,    // Đang đọc thẻ
    SUCCESS,    // Đọc UID thành công
    ERROR       // Lỗi
}

/**
 * NFC Manager cho terminal - chỉ đọc virtualCardUid từ thẻ HCE (AppCongVien).
 * Protocol:
 *   1. SELECT AID (F001020304050607)
 *   2. GET UID (00 CA 00 00) → trả về uid bytes + 9000
 */
class TerminalNfcManager {

    private val _status = MutableStateFlow(TerminalNfcStatus.IDLE)
    val status: StateFlow<TerminalNfcStatus> = _status.asStateFlow()

    private val _statusMessage = MutableStateFlow("Chưa bắt đầu quét")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // AID phải trùng với apduservice.xml trong AppCongVien: F001020304050607
    private val AID = byteArrayOf(0xF0.toByte(), 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07)
    private val GET_UID_CMD = byteArrayOf(0x00, 0xCA.toByte(), 0x00, 0x00)

    private var onUidReadCallback: ((String) -> Unit)? = null

    fun enableReaderMode(activity: Activity, onUidRead: (String) -> Unit) {
        val nfcAdapter = NfcAdapter.getDefaultAdapter(activity)
        if (nfcAdapter == null || !nfcAdapter.isEnabled) {
            _status.value = TerminalNfcStatus.ERROR
            _statusMessage.value = "NFC không khả dụng hoặc chưa được bật"
            return
        }

        onUidReadCallback = onUidRead
        val flags = NfcAdapter.FLAG_READER_NFC_A or
                NfcAdapter.FLAG_READER_NFC_B or
                NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK

        nfcAdapter.enableReaderMode(activity, { tag ->
            scope.launch { handleTag(tag) }
        }, flags, null)

        _status.value = TerminalNfcStatus.SCANNING
        _statusMessage.value = "Đưa điện thoại người dùng lại gần để quét..."
    }

    fun disableReaderMode(activity: Activity) {
        try {
            NfcAdapter.getDefaultAdapter(activity)?.disableReaderMode(activity)
        } catch (_: Exception) {}
        onUidReadCallback = null
        _status.value = TerminalNfcStatus.IDLE
        _statusMessage.value = "Đã dừng quét"
    }

    fun reset() {
        _status.value = TerminalNfcStatus.SCANNING
        _statusMessage.value = "Đưa điện thoại người dùng lại gần để quét..."
    }

    private fun handleTag(tag: Tag) {
        val isoDep = IsoDep.get(tag) ?: run {
            _status.value = TerminalNfcStatus.ERROR
            _statusMessage.value = "Thẻ không hỗ trợ ISO-DEP"
            return
        }

        try {
            isoDep.connect()
            isoDep.timeout = 5000
            _status.value = TerminalNfcStatus.READING
            _statusMessage.value = "Đang đọc thẻ..."

            // Bước 1: SELECT AID
            val selectCmd = buildSelectAid()
            val selectResp = isoDep.transceive(selectCmd)
            if (!isSuccess(selectResp)) {
                _status.value = TerminalNfcStatus.ERROR
                _statusMessage.value = "Thẻ không nhận dạng được (AID không khớp)"
                return
            }

            // Bước 2: GET UID
            val uidResp = isoDep.transceive(GET_UID_CMD)
            if (!isSuccess(uidResp) || uidResp.size <= 2) {
                _status.value = TerminalNfcStatus.ERROR
                _statusMessage.value = "Thẻ chưa được liên kết với tài khoản"
                return
            }

            // Parse UID: bỏ 2 byte status (9000) ở cuối, convert bytes → hex string
            val uidBytes = uidResp.copyOfRange(0, uidResp.size - 2)
            val cardUid = uidBytes.joinToString("") { "%02X".format(it) }

            _status.value = TerminalNfcStatus.SUCCESS
            _statusMessage.value = "Đọc thẻ thành công: $cardUid"

            // Lấy callback ra và xóa ngay để các lần NFC fire sau không gọi lại
            val callback = onUidReadCallback
            onUidReadCallback = null

            CoroutineScope(Dispatchers.Main).launch {
                callback?.invoke(cardUid)
            }

        } catch (e: Exception) {
            _status.value = TerminalNfcStatus.ERROR
            _statusMessage.value = "Lỗi đọc thẻ: ${e.message}"
        } finally {
            try { isoDep.close() } catch (_: Exception) {}
        }
    }

    private fun buildSelectAid(): ByteArray {
        return byteArrayOf(0x00, 0xA4.toByte(), 0x04, 0x00, AID.size.toByte()) +
                AID + byteArrayOf(0x00)
    }

    private fun isSuccess(response: ByteArray): Boolean {
        if (response.size < 2) return false
        return response[response.size - 2] == 0x90.toByte() &&
                response[response.size - 1] == 0x00.toByte()
    }
}
