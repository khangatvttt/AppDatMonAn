package com.example.projectdatmonan.Model

import java.time.LocalDateTime

data class DanhGia (
    var maMonAn: String? = null,
    var maNguoiDung: String? = null,
    var thoiGian: LocalDateTime? = null,
    var noiDung: String? = null,
    var soSao: Int? = null,
)