package com.example.projectdatmonan

import com.google.firebase.database.PropertyName

data class NguoiDung(
    @PropertyName("HoTen") val HoTen: String = "", // Họ tên người dùng
    @PropertyName("DiaChi") val DiaChi: String = "", // Địa chỉ người dùng
    @PropertyName("Email") val Email: String = "", // Email của người dùng
    @PropertyName("Password") val Password: String = "", // Mật khẩu của người dùng
    @PropertyName("SDT") val SDT: String = "", // Số điện thoại của người dùng
    @PropertyName("AvatarUrl") val AvatarUrl: String = "" // URL của avatar người dùng
)
