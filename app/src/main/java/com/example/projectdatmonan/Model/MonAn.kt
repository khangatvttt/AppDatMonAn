package com.example.projectdatmonan.Model

data class MonAn(
    var tenMonAn: String? = null,
    var gia: Double? = null,
    var trangThai: String? = null,
    var moTaChiTiet: String? = null,
    var trangThaiGiamGia: Int? = null,
    var hinhAnh: List<String>? = null
)