package com.example.projectdatmonan.Database

import com.example.projectdatmonan.Model.MonAn
import com.example.projectdatmonan.Model.NguoiDung
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

import com.google.firebase.database.ValueEventListener

class CRUD_NguoiDung {
    fun getNguoiDung(maNguoiDung:String, onComplete: (NguoiDung?) -> Unit) {
        val database = FirebaseDatabase.getInstance()
        val nguoiDungRef = database.getReference("NguoiDung").child(maNguoiDung)

        // Query to find the GioHang node by maNguoiDung
        nguoiDungRef.get()
            .addOnSuccessListener { dataSnapshot ->
                val nguoiDung = dataSnapshot.getValue(NguoiDung::class.java)
                onComplete(nguoiDung)
            }
            .addOnFailureListener { exception ->
                onComplete(null)
            }
    }

    fun getAllNguoiDung(onComplete: (HashMap<String?,NguoiDung?>?) -> Unit) {
        val database = FirebaseDatabase.getInstance()
        val userRef: DatabaseReference = database.getReference("NguoiDung")
        val listUser:HashMap<String?,NguoiDung?> = HashMap<String?,NguoiDung?>()

        userRef.get()
            .addOnSuccessListener { dataSnapshot ->
                for (userSnapshot in dataSnapshot.children) {
                    val monAn = userSnapshot.getValue(NguoiDung::class.java)
                    listUser[userSnapshot.key] = monAn
                }
                onComplete(listUser)
            }
            .addOnFailureListener { exception ->
                println("Error retrieving data: ${exception.message}")
                onComplete(null)
            }
    }



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
    // Hàm kiểm tra vai trò của người dùng theo email
    fun checkRoleByEmail(email: String, onComplete: (Boolean) -> Unit) {
        // Truy vấn thông tin người dùng dựa trên email
        database.child("NguoiDung").orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val isKH = snapshot.children.any { userSnapshot ->
                        userSnapshot.child("role").getValue(String::class.java) == "KH"
                    }
                    onComplete(isKH)
                }

                override fun onCancelled(error: DatabaseError) {
                    println("Database error: ${error.message}")
                    onComplete(false)
                }
            })
    }
    fun getUserByEmail(email: String, onComplete: (NguoiDung?) -> Unit) {
        database.child("NguoiDung").orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (userSnapshot in snapshot.children) {
                        val nguoiDung = userSnapshot.getValue(NguoiDung::class.java)
                        onComplete(nguoiDung) // Trả về thông tin người dùng
                        return
                    }
                    onComplete(null) // Không tìm thấy người dùng
                }

                override fun onCancelled(error: DatabaseError) {
                    println("Database error: ${error.message}")
                    onComplete(null) // Lỗi khi truy vấn
                }
            })
    }
    fun getUserByEmailSnap(email: String, onComplete: (DataSnapshot?) -> Unit) {
        // Truy vấn người dùng dựa trên email
        val databaseReference = FirebaseDatabase.getInstance().getReference("NguoiDung")
        databaseReference.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Kiểm tra xem có kết quả trả về hay không
                    if (snapshot.exists()) {
                        // Lấy kết quả đầu tiên tìm thấy
                        for (userSnapshot in snapshot.children) {
                            onComplete(userSnapshot) // Trả về snapshot chứa thông tin người dùng
                            return
                        }
                    }
                    onComplete(null) // Không tìm thấy người dùng với email này
                }

                override fun onCancelled(error: DatabaseError) {
                    println("Database error: ${error.message}")
                    onComplete(null) // Trả về null nếu có lỗi xảy ra
                }
            })
    }





}