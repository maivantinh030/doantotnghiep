package com.example.appcongvien.nfc

import android.content.Context
import android.content.SharedPreferences
import android.nfc.cardemulation.HostApduService
import android.os.Bundle

/**
 * HCE (Host Card Emulation) Service - phát thẻ ảo qua NFC.
 *
 * Khi đầu đọc NFC quét điện thoại:
 * 1. Đầu đọc gửi SELECT AID command → service trả về 9000 (success)
 * 2. Đầu đọc gửi GET UID command (CLA=00, INS=CA) → service trả về virtualCardUid bytes + 9000
 */
class CardEmulatorService : HostApduService() {

    companion object {
        const val PREFS_NAME = "hce_prefs"
        const val KEY_VIRTUAL_UID = "virtual_card_uid"

        // AID phải trùng với apduservice.xml: F001020304050607
        private val SELECT_AID_HEADER = byteArrayOf(0x00, 0xA4.toByte(), 0x04, 0x00, 0x08)
        private val AID = byteArrayOf(0xF0.toByte(), 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07)

        // GET UID command: CLA=00, INS=CA, P1=00, P2=00
        private val GET_UID_COMMAND = byteArrayOf(0x00, 0xCA.toByte(), 0x00, 0x00)

        // Status codes
        private val SW_OK = byteArrayOf(0x90.toByte(), 0x00)
        private val SW_UNKNOWN_COMMAND = byteArrayOf(0x6D.toByte(), 0x00)
        private val SW_NO_VIRTUAL_CARD = byteArrayOf(0x6A.toByte(), 0x82.toByte())

        fun saveVirtualUid(context: Context, uid: String) {
            getPrefs(context).edit().putString(KEY_VIRTUAL_UID, uid).apply()
        }

        fun getVirtualUid(context: Context): String? {
            return getPrefs(context).getString(KEY_VIRTUAL_UID, null)
        }

        fun clearVirtualUid(context: Context) {
            getPrefs(context).edit().remove(KEY_VIRTUAL_UID).apply()
        }

        private fun getPrefs(context: Context): SharedPreferences {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    override fun processCommandApdu(commandApdu: ByteArray, extras: Bundle?): ByteArray {
        // Xử lý SELECT AID
        if (isSelectAidCommand(commandApdu)) {
            return SW_OK
        }

        // Xử lý GET UID
        if (isGetUidCommand(commandApdu)) {
            val virtualUid = getVirtualUid(applicationContext)
                ?: return SW_NO_VIRTUAL_CARD

            val uidBytes = hexStringToBytes(virtualUid)
            return uidBytes + SW_OK
        }

        return SW_UNKNOWN_COMMAND
    }

    override fun onDeactivated(reason: Int) {
        // NFC session kết thúc - không cần xử lý gì
    }

    private fun isSelectAidCommand(apdu: ByteArray): Boolean {
        if (apdu.size < SELECT_AID_HEADER.size + AID.size) return false
        for (i in SELECT_AID_HEADER.indices) {
            if (apdu[i] != SELECT_AID_HEADER[i]) return false
        }
        for (i in AID.indices) {
            if (apdu[SELECT_AID_HEADER.size + i] != AID[i]) return false
        }
        return true
    }

    private fun isGetUidCommand(apdu: ByteArray): Boolean {
        if (apdu.size < GET_UID_COMMAND.size) return false
        for (i in GET_UID_COMMAND.indices) {
            if (apdu[i] != GET_UID_COMMAND[i]) return false
        }
        return true
    }

    private fun hexStringToBytes(hex: String): ByteArray {
        val cleanHex = hex.replace(" ", "").replace(":", "")
        return ByteArray(cleanHex.length / 2) { i ->
            cleanHex.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }
    }
}
