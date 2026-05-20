package com.park.smartcard.config

/**
 * Constants liên quan đến NFC card operations (PIN, AID, default values).
 * Tập trung ở đây để tránh hardcode rải rác trong ViewModel.
 */
object NfcConstants {
    /**
     * PIN admin mặc định dùng cho mọi thao tác staff ghi/đọc thẻ.
     *
     * TODO: chuyển sang đọc từ env var / config file khi triển khai production,
     * không nên hard-code PIN trong source code.
     */
    const val DEFAULT_ADMIN_PIN: String = "9999"
}
