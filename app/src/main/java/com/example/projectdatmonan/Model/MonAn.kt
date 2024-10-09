package com.example.projectdatmonan.Model

data class MonAn(
    var tenMonAn: String? = null,
    var gia: Double? = null,
    var trangThai: String? = null,
    var moTaChiTiet: String? = null,
    var trangThaiGiamGia: Int? = null,
    var hinhAnh: List<String>? = null,
    var loaiMonAn: String? = null
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MonAn) return false

        return tenMonAn == other.tenMonAn && gia == other.gia && trangThai == other.trangThai
        // So sánh thêm các trường quan trọng nếu cần
    }

    override fun hashCode(): Int {
        return tenMonAn.hashCode() + (gia?.hashCode() ?: 0) + (trangThai?.hashCode() ?: 0)
    }
}