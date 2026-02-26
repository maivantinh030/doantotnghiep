package com.park.dto

import com.park.entities.UserVoucher
import com.park.entities.Voucher
import kotlinx.serialization.Serializable

@Serializable
data class VoucherDTO(
    val voucherId: String,
    val code: String,
    val title: String,
    val description: String?,
    val discountType: String,
    val discountValue: String,
    val maxDiscount: String?,
    val minOrderValue: String,
    val usageLimit: Int?,
    val usedCount: Int,
    val perUserLimit: Int,
    val applicableGames: List<String>?,
    val startDate: String,
    val endDate: String,
    val isActive: Boolean
) {
    companion object {
        fun fromEntity(voucher: Voucher): VoucherDTO {
            return VoucherDTO(
                voucherId = voucher.voucherId,
                code = voucher.code,
                title = voucher.title,
                description = voucher.description,
                discountType = voucher.discountType,
                discountValue = voucher.discountValue.toString(),
                maxDiscount = voucher.maxDiscount?.toString(),
                minOrderValue = voucher.minOrderValue.toString(),
                usageLimit = voucher.usageLimit,
                usedCount = voucher.usedCount,
                perUserLimit = voucher.perUserLimit,
                applicableGames = parseJsonArray(voucher.applicableGames),
                startDate = voucher.startDate.toString(),
                endDate = voucher.endDate.toString(),
                isActive = voucher.isActive
            )
        }

        private fun parseJsonArray(json: String?): List<String>? {
            if (json.isNullOrBlank()) return null
            return try {
                json.removeSurrounding("[", "]")
                    .split(",")
                    .map { it.trim().removeSurrounding("\"") }
                    .filter { it.isNotBlank() }
            } catch (e: Exception) {
                null
            }
        }
    }
}

@Serializable
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
) {
    companion object {
        fun fromEntity(uv: UserVoucher, voucher: Voucher? = null): UserVoucherDTO {
            return UserVoucherDTO(
                id = uv.id,
                userId = uv.userId,
                voucherId = uv.voucherId,
                source = uv.source,
                isUsed = uv.isUsed,
                usedAt = uv.usedAt?.toString(),
                usedOrderId = uv.usedOrderId,
                createdAt = uv.createdAt.toString(),
                voucher = voucher?.let { VoucherDTO.fromEntity(it) }
            )
        }
    }
}

@Serializable
data class CreateVoucherRequest(
    val code: String,
    val title: String,
    val description: String? = null,
    val discountType: String,
    val discountValue: String,
    val maxDiscount: String? = null,
    val minOrderValue: String = "0",
    val usageLimit: Int? = null,
    val perUserLimit: Int = 1,
    val applicableGames: List<String>? = null,
    val startDate: String,
    val endDate: String,
    val isActive: Boolean = true
)

@Serializable
data class UpdateVoucherRequest(
    val title: String? = null,
    val description: String? = null,
    val discountValue: String? = null,
    val maxDiscount: String? = null,
    val minOrderValue: String? = null,
    val usageLimit: Int? = null,
    val perUserLimit: Int? = null,
    val isActive: Boolean? = null,
    val endDate: String? = null
)
