package com.example.projectdatmonan.Model

import java.time.LocalDateTime

data class DatHang(
    var maNguoiDung: String? = null,
    var listMonAn: List<ListMonAn>? = null,
    var ngayGioDat: String? = null,
    var tinhTrang: String? = null,
    val diaChiGiaoHang: String? = null,
    val sdt: String? = null,
    var tongTien: Double? = null
)
