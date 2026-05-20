package com.park.smartcard.data.repository

import io.ktor.client.request.*
import io.ktor.http.*
import com.park.smartcard.data.model.CustomerDTO
import com.park.smartcard.data.model.TopUpRequest
import com.park.smartcard.data.model.TopUpResult
import com.park.smartcard.data.network.ApiClient
import com.park.smartcard.data.network.apiCall

/**
 * Thao tác liên quan đến khách hàng tại quầy: tra cứu, nạp tiền, kiểm tra trạng thái nạp.
 */
class CustomerRepository {

    suspend fun findCustomerByPhone(phone: String): Result<CustomerDTO> =
        apiCall("Không tìm thấy khách hàng") {
            ApiClient.http.get("/api/staff/customers/search") {
                parameter("phone", phone)
            }
        }

    suspend fun getCustomerById(userId: String): Result<CustomerDTO> =
        apiCall("Không tìm thấy khách hàng") {
            ApiClient.http.get("/api/staff/customers/$userId")
        }

    suspend fun topUpForCustomer(userId: String, amount: String, method: String = "CASH"): Result<TopUpResult> =
        apiCall("Lỗi nạp tiền") {
            ApiClient.http.post("/api/staff/customers/$userId/topup") {
                contentType(ContentType.Application.Json)
                setBody(TopUpRequest(amount = amount, method = method))
            }
        }

    suspend fun getTopUpStatus(userId: String, orderId: String): Result<TopUpResult> =
        apiCall("Không thể kiểm tra trạng thái nạp tiền") {
            ApiClient.http.get("/api/staff/customers/$userId/topup/status/$orderId")
        }
}
