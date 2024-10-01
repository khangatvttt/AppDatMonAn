package com.example.projectdatmonan.Database

import com.example.projectdatmonan.Model.NguoiDung
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class DBConnection {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference




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
