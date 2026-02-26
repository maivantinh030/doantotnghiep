package com.park.services

import com.park.dto.*
import com.park.entities.UserVoucher
import com.park.entities.Voucher
import com.park.repositories.*
import java.math.BigDecimal
import java.time.Instant
import java.util.*

class VoucherService(
    private val voucherRepository: IVoucherRepository = VoucherRepository(),
    private val userVoucherRepository: IUserVoucherRepository = UserVoucherRepository()
) {

    fun getAvailableVouchers(page: Int, size: Int): Map<String, Any> {
        val offset = ((page - 1) * size).toLong()
        val vouchers = voucherRepository.findAllActive(size, offset)
        val total = voucherRepository.countActive()

        return mapOf(
            "items" to vouchers.map { VoucherDTO.fromEntity(it) },
            "total" to total,
            "page" to page,
            "size" to size
        )
    }

    fun getVoucherByCode(code: String): VoucherDTO? {
        val voucher = voucherRepository.findByCode(code) ?: return null
        return VoucherDTO.fromEntity(voucher)
    }

    fun claimVoucher(userId: String, voucherId: String): Result<UserVoucherDTO> {
        val voucher = voucherRepository.findById(voucherId)
            ?: return Result.failure(NoSuchElementException("Voucher không tồn tại"))

        val now = Instant.now()
        if (!voucher.isActive || now.isBefore(voucher.startDate) || now.isAfter(voucher.endDate)) {
            return Result.failure(IllegalStateException("Voucher không còn hiệu lực"))
        }

        if (voucher.usageLimit != null && voucher.usedCount >= voucher.usageLimit) {
            return Result.failure(IllegalStateException("Voucher đã hết lượt sử dụng"))
        }

        val userCount = userVoucherRepository.countByUserAndVoucher(userId, voucherId)
        if (userCount >= voucher.perUserLimit) {
            return Result.failure(IllegalStateException("Bạn đã nhận voucher này rồi"))
        }

        val userVoucher = UserVoucher(
            id = UUID.randomUUID().toString(),
            userId = userId,
            voucherId = voucherId,
            source = "CLAIMED",
            isUsed = false,
            usedAt = null,
            usedOrderId = null,
            createdAt = now
        )

        val created = userVoucherRepository.create(userVoucher)
        return Result.success(UserVoucherDTO.fromEntity(created, voucher))
    }

    fun getMyVouchers(userId: String, page: Int, size: Int): Map<String, Any> {
        val offset = ((page - 1) * size).toLong()
        val userVouchers = userVoucherRepository.findByUserId(userId, size, offset)
        val total = userVoucherRepository.countByUserId(userId)

        val dtos = userVouchers.map { uv ->
            val voucher = voucherRepository.findById(uv.voucherId)
            UserVoucherDTO.fromEntity(uv, voucher)
        }

        return mapOf(
            "items" to dtos,
            "total" to total,
            "page" to page,
            "size" to size
        )
    }

    // === Admin ===

    fun createVoucher(request: CreateVoucherRequest): Result<VoucherDTO> {
        if (request.code.isBlank()) {
            return Result.failure(IllegalArgumentException("Mã voucher không được để trống"))
        }
        if (request.discountType !in listOf("PERCENT", "FIXED_AMOUNT")) {
            return Result.failure(IllegalArgumentException("Loại giảm giá phải là PERCENT hoặc FIXED_AMOUNT"))
        }

        val existing = voucherRepository.findByCode(request.code)
        if (existing != null) {
            return Result.failure(IllegalStateException("Mã voucher đã tồn tại"))
        }

        val now = Instant.now()
        val voucher = Voucher(
            voucherId = UUID.randomUUID().toString(),
            code = request.code.uppercase(),
            title = request.title,
            description = request.description,
            discountType = request.discountType,
            discountValue = BigDecimal(request.discountValue),
            maxDiscount = request.maxDiscount?.let { BigDecimal(it) },
            minOrderValue = BigDecimal(request.minOrderValue),
            usageLimit = request.usageLimit,
            usedCount = 0,
            perUserLimit = request.perUserLimit,
            applicableGames = request.applicableGames?.let {
                "[${it.joinToString(",") { id -> "\"$id\"" }}]"
            },
            startDate = Instant.parse(request.startDate),
            endDate = Instant.parse(request.endDate),
            isActive = request.isActive,
            createdAt = now,
            updatedAt = now
        )

        val created = voucherRepository.create(voucher)
        return Result.success(VoucherDTO.fromEntity(created))
    }

    fun updateVoucher(voucherId: String, request: UpdateVoucherRequest): Result<VoucherDTO> {
        voucherRepository.findById(voucherId)
            ?: return Result.failure(NoSuchElementException("Voucher không tồn tại"))

        val updates = mutableMapOf<String, Any?>()
        request.title?.let { updates["title"] = it }
        request.description?.let { updates["description"] = it }
        request.discountValue?.let { updates["discountValue"] = BigDecimal(it) }
        request.maxDiscount?.let { updates["maxDiscount"] = BigDecimal(it) }
        request.minOrderValue?.let { updates["minOrderValue"] = BigDecimal(it) }
        request.usageLimit?.let { updates["usageLimit"] = it }
        request.perUserLimit?.let { updates["perUserLimit"] = it }
        request.isActive?.let { updates["isActive"] = it }
        request.endDate?.let { updates["endDate"] = Instant.parse(it) }

        if (updates.isNotEmpty()) {
            voucherRepository.update(voucherId, updates)
        }

        val updated = voucherRepository.findById(voucherId)!!
        return Result.success(VoucherDTO.fromEntity(updated))
    }

    fun deleteVoucher(voucherId: String): Boolean {
        return voucherRepository.delete(voucherId)
    }
}
