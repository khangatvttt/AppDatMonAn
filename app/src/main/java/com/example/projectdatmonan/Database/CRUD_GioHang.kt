package com.example.projectdatmonan.Database

import com.example.projectdatmonan.Model.GioHang
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class CRUD_GioHang {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    fun addGioHang(gioHang: GioHang, onComplete: (Boolean) -> Unit) {
        val newId = database.child("GioHang").push().key
        if (newId != null) {
            database.child("GioHang").child(newId).setValue(gioHang)
                .addOnSuccessListener {
                    onComplete(true)
                }
                .addOnFailureListener { exception ->
                    println("Error adding GioHang: ${exception.message}")
                    onComplete(false)
                }
        } else {
            println("Error generating cart ID")
            onComplete(false)
        }
    }
}