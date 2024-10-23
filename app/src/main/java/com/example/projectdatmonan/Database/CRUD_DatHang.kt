package com.example.projectdatmonan.Database


import DatHang
import com.google.firebase.database.FirebaseDatabase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CRUD_DatHang {

    fun placeOrder(
        maNguoiDung: String,
        listMonAn: List<com.example.projectdatmonan.Model.ListMonAn>,
        diaChiGiaoHang: String,
        sdt: String,
        tongTien: Double,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val orderId = FirebaseDatabase.getInstance().getReference("DatHang").push().key

        val datHang = DatHang(
            maNguoiDung = maNguoiDung,
            listMonAn = listMonAn,
            ngayGioDat = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")),
            tinhTrang = "Đang xử lý",
            diaChiGiaoHang = diaChiGiaoHang,
            sdt = sdt,
            tongTien = tongTien
        )

        orderId?.let {
            FirebaseDatabase.getInstance().getReference("DatHang").child(it).setValue(datHang)
                .addOnSuccessListener {
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    onFailure(e)
                }
        } ?: onFailure(Exception("Failed to generate order ID"))
    }

    fun clearCart(maNguoiDung: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val cartRef = FirebaseDatabase.getInstance().getReference("GioHang")

        cartRef.orderByChild("maNguoiDung").equalTo(maNguoiDung).addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                for (cartSnapshot in snapshot.children) {
                    cartSnapshot.ref.removeValue()
                }
                onSuccess()
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                onFailure(Exception("Failed to clear cart: ${error.message}"))
            }
        })
    }
}
