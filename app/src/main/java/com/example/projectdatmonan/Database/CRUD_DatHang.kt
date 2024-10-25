package com.example.projectdatdatHang.Database

import DatHang
import com.example.projectdatmonan.Database.CRUD_MonAn
import com.example.projectdatmonan.Model.MonAn
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar




class CRUD_DatHang {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val dbMonAn = CRUD_MonAn()

    fun getMonAnDaBan(tuNgay: Calendar, denNgay: Calendar, onComplete: (HashMap<String?, Int>?) -> Unit) {
        val datHangRef = database.child("DatHang")
        val listMonAnDaBan: HashMap<String?, Int> = HashMap()

        datHangRef.get()
            .addOnSuccessListener { dataSnapshot ->
                for (datHangSnapshot in dataSnapshot.children) {
                    val datHang = datHangSnapshot.getValue(DatHang::class.java)
                    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                    val ngayGioDat = LocalDateTime.parse(datHang!!.ngayGioDat, formatter)

                    val tuNgayLocal = LocalDateTime.ofInstant(tuNgay.toInstant(), ZoneId.systemDefault())
                    val denNgayLocal = LocalDateTime.ofInstant(denNgay.toInstant(), ZoneId.systemDefault())

                    if (ngayGioDat.isAfter(tuNgayLocal) && ngayGioDat.isBefore(denNgayLocal)){
                        for (monAn in datHang.listMonAn!!){
                            listMonAnDaBan[monAn.maMonAn] = listMonAnDaBan.getOrDefault(monAn.maMonAn, 0) + monAn.soLuong!!
                        }
                    }
                }
                onComplete(listMonAnDaBan)
            }
            .addOnFailureListener { exception ->
                println("Error retrieving data: ${exception.message}")
                onComplete(null)
            }
    }

    fun getDoanhThu(tuNgay: Calendar, denNgay: Calendar, onComplete: (HashMap<LocalDate?, Double>?) -> Unit) {
        val datHangRef = database.child("DatHang")
        val listMonAnDaBan: HashMap<LocalDate?, Double> = HashMap()

        datHangRef.get()
            .addOnSuccessListener { dataSnapshot ->
                for (datHangSnapshot in dataSnapshot.children) {
                    val datHang = datHangSnapshot.getValue(DatHang::class.java)
                    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                    val ngayGioDat = LocalDateTime.parse(datHang!!.ngayGioDat, formatter)

                    val tuNgayLocal = LocalDateTime.ofInstant(tuNgay.toInstant(), ZoneId.systemDefault())
                    val denNgayLocal = LocalDateTime.ofInstant(denNgay.toInstant(), ZoneId.systemDefault()).withHour(23).withMinute(59).withSecond(59)

                    if (ngayGioDat.isAfter(tuNgayLocal) && ngayGioDat.isBefore(denNgayLocal)){
                        listMonAnDaBan[ngayGioDat.toLocalDate()] = listMonAnDaBan.getOrDefault(ngayGioDat.toLocalDate(), 0.0) + datHang.tongTien!!
                    }
                }
                onComplete(listMonAnDaBan)
            }
            .addOnFailureListener { exception ->
                println("Error retrieving data: ${exception.message}")
                onComplete(null)
            }
    }

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
