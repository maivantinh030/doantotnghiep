package com.example.appcongvien.data.model

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null
)

data class PaginatedData<T>(
    val items: List<T>,
    val total: Int,
    val page: Int,
    val size: Int,
    val totalPages: Int
)

sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String, val code: Int = -1) : Resource<Nothing>()
    object Loading : Resource<Nothing>()
}
