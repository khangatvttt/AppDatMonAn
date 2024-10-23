package com.example.projectdatmonan.Database

import com.example.projectdatmonan.Model.DanhGia
import com.example.projectdatmonan.Model.MonAn
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class CRUD_DanhGia {

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