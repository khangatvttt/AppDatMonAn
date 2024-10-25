package com.example.projectdatmonan.Model

import com.google.firebase.database.PropertyName

data class NguoiDung(
    var hoTen: String = "", // Họ tên người dùng
    var diaChi: String = "", // Địa chỉ người dùng
    var email: String = "", // Email của người dùng
    var sdt: String = "", // Số điện thoại của người dùng
    var avatarUrl: String = "" ,// URL của avatar người dùng
    var role: String = ""

)
