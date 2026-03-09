package org.example.project.screen

import java.text.SimpleDateFormat
import java. util.*

data class ValidationState(
    val isValid: Boolean = true,
    val errorMessage: String = ""
)

object ValidationUtils {

    fun validateCustomerID(customerID: String): ValidationState {
        return when {
            customerID.isEmpty() -> ValidationState(false, "Mã khách hàng không được để trống")
            customerID.length < 3 -> ValidationState(false, "Mã khách hàng phải ít nhất 3 ký tự")
            customerID.length > 15 -> ValidationState(false, "Mã khách hàng không được quá 15 ký tự")
            !customerID.matches(Regex("^[A-Za-z0-9]+$")) -> ValidationState(false, "Mã khách hàng chỉ được chứa chữ cái và số")
            else -> ValidationState(true, "")
        }
    }

    fun validateName(name: String): ValidationState {
        return when {
            name.isEmpty() -> ValidationState(false, "Họ tên không được để trống")
            name.trim().length < 2 -> ValidationState(false, "Họ tên phải ít nhất 2 ký tự")
            name.length > 50 -> ValidationState(false, "Họ tên không được quá 50 ký tự")
            !name.matches(Regex("^[a-zA-ZÀ-ỹ\\s]+$")) -> ValidationState(false, "Họ tên chỉ được chứa chữ cái và khoảng trắng")
            name.trim().split("\\s+". toRegex()).size < 2 -> ValidationState(false, "Vui lòng nhập đầy đủ họ và tên")
            else -> ValidationState(true, "")
        }
    }

    fun validateDateOfBirth(dateStr: String): ValidationState {
        return when {
            dateStr.isEmpty() -> ValidationState(false, "Ngày sinh không được để trống")
            !dateStr. matches(Regex("^\\d{2}/\\d{2}/\\d{4}$")) -> ValidationState(false, "Định dạng ngày sinh:  DD/MM/YYYY")
            else -> {
                try {
                    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    formatter.isLenient = false
                    val date = formatter.parse(dateStr)
                    val calendar = Calendar.getInstance()
                    calendar.time = date!!

                    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                    val birthYear = calendar.get(Calendar. YEAR)
                    val age = currentYear - birthYear

                    when {
                        age < 16 -> ValidationState(false, "Khách hàng phải từ 16 tuổi trở lên")
                        age > 100 -> ValidationState(false, "Tuổi không hợp lệ")
                        else -> ValidationState(true, "")
                    }
                } catch (e: Exception) {
                    ValidationState(false, "Ngày sinh không hợp lệ")
                }
            }
        }
    }

    fun validatePhoneNumber(phoneNumber: String): ValidationState {
        return when {
            phoneNumber.isEmpty() -> ValidationState(false, "Số điện thoại không được để trống")
            !phoneNumber.matches(Regex("^0\\d{9}$")) -> ValidationState(false, "Số điện thoại phải có 10 số và bắt đầu bằng 0")
            !phoneNumber.matches(Regex("^(032|033|034|035|036|037|038|039|096|097|098|086|083|084|085|081|082|088|091|094|070|079|077|076|078|090|093|089|056|058|092|059|099)[0-9]{7}$")) ->
                ValidationState(false, "Đầu số điện thoại không hợp lệ")
            else -> ValidationState(true, "")
        }
    }

    fun validateImage(imageBytes: ByteArray? ): ValidationState {
        return when {
            imageBytes == null -> ValidationState(false, "Vui lòng chọn ảnh khách hàng")
            imageBytes.size > 8192 -> ValidationState(false, "Kích thước ảnh quá lớn (tối đa 8KB)")
            else -> ValidationState(true, "")
        }
    }

    fun formatDateInput(input: String): String {
        val cleaned = input.replace(Regex("[^\\d]"), "")
        return when {
            cleaned.length <= 2 -> cleaned
            cleaned.length <= 4 -> "${cleaned.substring(0, 2)}/${cleaned.substring(2)}"
            cleaned.length <= 8 -> "${cleaned.substring(0, 2)}/${cleaned.substring(2, 4)}/${cleaned.substring(4)}"
            else -> "${cleaned.substring(0, 2)}/${cleaned.substring(2, 4)}/${cleaned.substring(4, 8)}"
        }
    }
}