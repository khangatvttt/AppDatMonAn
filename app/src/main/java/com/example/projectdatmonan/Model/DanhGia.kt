package com.example.projectdatmonan.Model

data class DanhGia(
//    var id: String? = null,
    var maMonAn: String? = null,
    var maNguoiDung: String? = null,
    var thoiGian: String? = null,
    var noiDung: String? = null,
    var soSao: Float? = null,
    var key: String? = null
){
    constructor() : this(null, null, null, null, null,null)
}
