package com.example.projectdatmonan

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class DBConnection {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    // Thêm người dùng vào Firebase
    fun addNguoiDung(nguoiDung: NguoiDung, onComplete: (Boolean) -> Unit) {
        val newUserId = database.child("NguoiDung").push().key // Tạo ID ngẫu nhiên
        if (newUserId != null) {
            database.child("NguoiDung").child(newUserId).setValue(nguoiDung)
                .addOnSuccessListener {
                    onComplete(true) // Thêm thành công
                }
                .addOnFailureListener { exception ->
                    println("Error adding user: ${exception.message}")
                    onComplete(false) // Thêm thất bại
                }
        } else {
            println("Error generating user ID")
            onComplete(false)
        }
    }


    // Lấy thông tin người dùng theo ID
    fun getNguoiDung(maNguoiDung: String, onComplete: (NguoiDung?) -> Unit) {
        database.child("NguoiDung").child(maNguoiDung).get()
            .addOnSuccessListener { dataSnapshot ->
                val nguoiDung = dataSnapshot.getValue(NguoiDung::class.java)
                onComplete(nguoiDung) // Trả về người dùng
            }
            .addOnFailureListener { exception ->
                println("Error getting user: ${exception.message}")
                onComplete(null) // Trả về null nếu có lỗi
            }
    }
}
