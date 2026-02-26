package com.example.appcongvien.data.model

// ===== Responses =====
data class VoucherDTO(
    val voucherId: String,
    val code: String,
    val title: String,
    val description: String?,
    val discountType: String,
    val discountValue: String,
    val maxDiscount: String?,
    val minOrderValue: String?,
    val usageLimit: Int?,
    val perUserLimit: Int?,
    val startDate: String?,
    val endDate: String?,
    val isActive: Boolean
)

data class UserVoucherDTO(
    val userVoucherId: String,
    val voucher: VoucherDTO,
    val claimedAt: String,
    val usedAt: String?,
    val status: String
)
