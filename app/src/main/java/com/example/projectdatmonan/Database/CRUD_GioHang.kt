package com.example.projectdatmonan.Database

import com.example.projectdatmonan.Model.GioHang
import com.example.projectdatmonan.Model.ListMonAn
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class CRUD_GioHang(private val dbConnection: DBConnection) {

    // Thêm món ăn vào giỏ hàng
    // Thêm món ăn vào giỏ hàng
    fun addToCart(gioHang: GioHang, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val gioHangRef = dbConnection.getGioHangRef()

        // Tìm giỏ hàng của người dùng dựa trên mã người dùng
        gioHangRef.orderByChild("maNguoiDung").equalTo(gioHang.maNguoiDung)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Nếu đã tồn tại giỏ hàng với mã người dùng này
                        for (snapshot in dataSnapshot.children) {
                            val existingGioHang = snapshot.getValue(GioHang::class.java)
                            existingGioHang?.let { currentCart ->
                                // Cập nhật danh sách ListMonAn của giỏ hàng hiện tại
                                val updatedListMonAn = currentCart.listMonAn?.toMutableList()
                                if (updatedListMonAn != null) {
                                    gioHang.listMonAn?.let { updatedListMonAn.addAll(it) }
                                } // Thêm món ăn mới vào danh sách
                                currentCart.listMonAn = updatedListMonAn

                                // Cập nhật giỏ hàng trong Firebase
                                snapshot.ref.setValue(currentCart)
                                    .addOnSuccessListener { onSuccess() }
                                    .addOnFailureListener { exception ->
                                        onFailure(exception.message ?: "Lỗi không xác định")
                                    }
                            }
                        }
                    } else {
                        // Nếu giỏ hàng chưa tồn tại, tạo mới
                        val newCartRef = gioHangRef.push() // Tạo mã mới cho giỏ hàng
                        newCartRef.setValue(gioHang)
                            .addOnSuccessListener { onSuccess() }
                            .addOnFailureListener { exception ->
                                onFailure(exception.message ?: "Lỗi không xác định")
                            }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    onFailure(databaseError.message)
                }
            })
    }


    // Cập nhật giỏ hàng trong Firebase
    fun updateCart(gioHang: GioHang, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        gioHang.maNguoiDung?.let { maNguoiDung ->
            val gioHangRef = dbConnection.getGioHangRef().child(maNguoiDung)
            gioHangRef.setValue(gioHang)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { exception -> onFailure(exception.message ?: "Lỗi không xác định") }
        }
    }

    // Xóa giỏ hàng từ Firebase
    fun deleteCart(maNguoiDung: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val gioHangRef = dbConnection.getGioHangRef().child(maNguoiDung)
        gioHangRef.removeValue()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onFailure(exception.message ?: "Lỗi không xác định") }
    }
}
