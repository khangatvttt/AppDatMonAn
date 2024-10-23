package com.example.projectdatmonan.Database

import com.example.projectdatmonan.Model.DanhGia
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

import com.example.projectdatmonan.Model.DanhGia
import com.example.projectdatmonan.Model.MonAn
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class CRUD_DanhGia {
    private val dbConnection = DBConnection()
    private val danhGiaRef: DatabaseReference = dbConnection.getDanhGiaRef()

    // Lấy danh sách đánh giá theo mã món ăn
    fun getDanhGiaByMonAn(maMonAn: String?, onComplete: (List<DanhGia>?) -> Unit) {
        if (maMonAn == null) {
            onComplete(null)
            return
        }

        danhGiaRef.orderByChild("maMonAn").equalTo(maMonAn)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val danhGiaList = mutableListOf<DanhGia>()
                    for (data in snapshot.children) {
                        val danhGia = data.getValue(DanhGia::class.java)
                        danhGia?.let {
                            it.key = data.key // Lấy key của đánh giá từ Firebase
                            danhGiaList.add(it)
                        }
                    }
                    onComplete(danhGiaList)
                }

                override fun onCancelled(error: DatabaseError) {
                    println("Error fetching reviews: ${error.message}")
                    onComplete(null)
                }
            })
    }

    // Thêm đánh giá vào Firebase
    fun addDanhGia(danhGia: DanhGia, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val danhGiaRef = dbConnection.getDanhGiaRef().push()
        danhGiaRef.setValue(danhGia)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onFailure(exception.message ?: "Lỗi không xác định") }
    }

    // Cập nhật đánh giá trong Firebase
    fun updateDanhGia(danhGia: DanhGia, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        danhGia.key?.let { key ->
            val danhGiaRef = dbConnection.getDanhGiaRef().child(key)
            danhGiaRef.setValue(danhGia)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { exception -> onFailure(exception.message ?: "Lỗi không xác định") }
        }
    }

    // Xóa đánh giá từ Firebase
    fun deleteDanhGia(key: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val danhGiaRef = dbConnection.getDanhGiaRef().child(key)
        danhGiaRef.removeValue()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onFailure(exception.message ?: "Lỗi không xác định") }
    }
    fun fetchDanhGia(
        monAnId: String,
        onComplete: (List<Int>) -> Unit,
        onError: (DatabaseError) -> Unit
    ) {
        val ref = FirebaseDatabase.getInstance().getReference("DanhGia")
            .orderByChild("maMonAn").equalTo(monAnId)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ratings = mutableListOf<Int>()
                for (childSnapshot in snapshot.children) {
                    val danhGia = childSnapshot.getValue(DanhGia::class.java)
                    danhGia?.soSao?.let { ratings.add(it.toInt()) }
                }
                onComplete(ratings)
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error)
            }
        })
    }

    fun getAllDanhGia(onComplete: (List<DanhGia>?) -> Unit) {
        val database = FirebaseDatabase.getInstance()
        val monAnRef: DatabaseReference = database.getReference("DanhGia")
        val listDanhGia = mutableListOf<DanhGia>()

        monAnRef.get()
            .addOnSuccessListener { dataSnapshot ->
                for (monAnSnapshot in dataSnapshot.children) {
                    val danhGia = monAnSnapshot.getValue(DanhGia::class.java)
                    listDanhGia.add(danhGia!!)
                }
                onComplete(listDanhGia)
            }
            .addOnFailureListener { exception ->
                println("Error retrieving data: ${exception.message}")
                onComplete(null)
            }
    }

}
