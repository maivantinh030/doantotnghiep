package com.example.appcongvien.data

data class Voucher(
    val id: String,
    val title: String,
    val description: String,
    val discountType: DiscountType,
    val value: Int,
    val minPurchase: Int = 0,
    val expiryDate: String,
    val isExpiringSoon: Boolean = false
)

enum class DiscountType {
    PERCENTAGE, AMOUNT
}





