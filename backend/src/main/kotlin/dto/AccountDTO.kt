package com.park.dto

import com.park.entities.Account

/**
 * DTO để tạo account mới
 */
data class CreateAccountDTO(
    val phoneNumber: String,
    val passwordHash: String,
    val role: String = "USER"
)

/**
 * DTO để trả về thông tin account (không bao gồm password)
 */
data class AccountDTO(
    val accountId: String,
    val phoneNumber: String,
    val role: String,
    val status: String
) {
    companion object {
        fun fromEntity(account: Account): AccountDTO {
            return AccountDTO(
                accountId = account.accountId,
                phoneNumber = account.phoneNumber,
                role = account.role,
                status = account.status
            )
        }
    }
}
