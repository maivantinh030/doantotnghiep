package com.example.appcongvien.data.model

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null
)

data class PaginatedData<T>(
    val items: List<T> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    val size: Int = 10,
    val totalPages: Int = 1
)

sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String, val code: Int = -1) : Resource<Nothing>()
    object Loading : Resource<Nothing>()
}
