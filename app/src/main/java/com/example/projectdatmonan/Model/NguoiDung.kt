package com.example.projectdatmonan.Model

import com.google.firebase.database.PropertyName

data class NguoiDung(
    val hoTen: String = "", // Họ tên người dùng
    val diaChi: String = "", // Địa chỉ người dùng
    val email: String = "", // Email của người dùng
    val password: String = "", // Mật khẩu của người dùng
    val sDT: String = "", // Số điện thoại của người dùng
    val avatarUrl: String = "" // URL của avatar người dùng
)
