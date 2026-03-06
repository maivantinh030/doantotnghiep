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
    val id: String,
    val userId: String,
    val voucherId: String,
    val source: String?,
    val isUsed: Boolean,
    val usedAt: String?,
    val usedOrderId: String?,
    val createdAt: String,
    val voucher: VoucherDTO? = null
)
