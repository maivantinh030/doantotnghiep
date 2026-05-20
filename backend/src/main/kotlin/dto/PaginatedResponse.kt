package com.park.dto

import kotlinx.serialization.Serializable

@Serializable
data class PaginatedResponse<T>(
    val items: List<T>,
    val total: Long,
    val page: Int,
    val size: Int,
    val totalPages: Int
) {
    companion object {
        fun <T> build(items: List<T>, total: Long, page: Int, size: Int): PaginatedResponse<T> {
            val totalPages = if (size > 0) ((total + size - 1) / size).toInt() else 1
            return PaginatedResponse(
                items = items,
                total = total,
                page = page,
                size = size,
                totalPages = totalPages
            )
        }
    }
}
