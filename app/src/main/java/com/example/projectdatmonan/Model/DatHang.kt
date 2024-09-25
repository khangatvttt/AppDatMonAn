package com.example.projectdatmonan.Model

import java.time.LocalDateTime

data class DatHang(
    var maNguoiDung: String? = null,
    var listMonAn: List<ListMonAn>? = null,
    var ngayGioDat: LocalDateTime? = null,
    var trinhTrang: String? = null,
)
