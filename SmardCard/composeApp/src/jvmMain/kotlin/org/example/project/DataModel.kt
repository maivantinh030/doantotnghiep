package org.example.project

data class Customer(
    val maKH: String = "",
    val hoTen: String = "",
    val ngaySinh: String = "",
    val soDienThoai: String = "",
    val loaiThe: String = "",
    val anhKH: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Customer
        if (maKH != other.maKH) return false
        if (hoTen != other.hoTen) return false
        if (ngaySinh != other.ngaySinh) return false
        if (soDienThoai != other.soDienThoai) return false
        if (loaiThe != other.loaiThe) return false
        if (anhKH != null) {
            if (other.anhKH == null) return false
            if (!anhKH.contentEquals(other.anhKH)) return false
        } else if (other.anhKH != null) return false
        return true
    }

    override fun hashCode(): Int {
        var result = maKH.hashCode()
        result = 31 * result + hoTen.hashCode()
        result = 31 * result + ngaySinh.hashCode()
        result = 31 * result + soDienThoai.hashCode()
        result = 31 * result + loaiThe.hashCode()
        result = 31 * result + (anhKH?.contentHashCode() ?: 0)
        return result
    }
}

enum class CardType(val displayName: String, val value: String) {
    THUONG("Thẻ thường", "THUONG"),
    VANG("Thẻ vàng", "VANG"),
    BACH_KIM("Thẻ bạch kim", "BACHKIM"),
    KIM_CUONG("Thẻ kim cương", "KIMCUONG")
}
data class GameEntry(
    val gameCode:  Int,
    val tickets: Int
)