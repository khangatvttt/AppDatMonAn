package com.example.projectdatdatHang.Database

import com.example.projectdatmonan.Database.CRUD_MonAn
import com.example.projectdatmonan.Model.DatHang
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
}