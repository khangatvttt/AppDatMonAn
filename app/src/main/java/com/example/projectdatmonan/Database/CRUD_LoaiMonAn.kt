package com.example.projectdatmonan.Database

import com.example.projectdatmonan.Model.LoaiMonAn
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class CRUD_LoaiMonAn {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    fun addLoaiLoaiMonAn(loaiMonAn: LoaiMonAn, onComplete: (Boolean) -> Unit) {
        val newId = database.child("LoaiMonAn").push().key
        if (newId != null) {
            database.child("LoaiMonAn").child(newId).setValue(loaiMonAn)
                .addOnSuccessListener {
                    onComplete(true)
                }
                .addOnFailureListener { exception ->
                    println("Error adding LoaiMonAn: ${exception.message}")
                    onComplete(false)
                }
        } else {
            println("Error generating user ID")
            onComplete(false)
        }
    }


    fun getAllLoaiLoaiMonAn(onComplete: (HashMap<String?, LoaiMonAn?>?) -> Unit) {
        val database = FirebaseDatabase.getInstance()
        val LoaiMonAnRef: DatabaseReference = database.getReference("LoaiMonAn")
        val listLoaiMonAn: HashMap<String?, LoaiMonAn?> = HashMap<String?, LoaiMonAn?>()

        LoaiMonAnRef.get()
            .addOnSuccessListener { dataSnapshot ->
                for (LoaiMonAnSnapshot in dataSnapshot.children) {
                    val loaiMonAn = LoaiMonAnSnapshot.getValue(LoaiMonAn::class.java)
                    listLoaiMonAn[LoaiMonAnSnapshot.key] = loaiMonAn
                }
                onComplete(listLoaiMonAn)
            }
            .addOnFailureListener { exception ->
                println("Error retrieving data: ${exception.message}")
                onComplete(null)
            }
    }

    fun updateLoaiMonAn(maLoaiMonAn: String, updatedLoaiMonAn: LoaiMonAn, onComplete: (Boolean) -> Unit) {
        val database = FirebaseDatabase.getInstance()
        val LoaiMonAnRef = database.getReference("LoaiMonAn").child(maLoaiMonAn)

        // Query to find the GioHang node by maNguoiDung
        LoaiMonAnRef.setValue(updatedLoaiMonAn)
            .addOnSuccessListener { dataSnapshot ->
                onComplete(true)
            }
            .addOnFailureListener { exception ->
                onComplete(false)
            }
    }

    fun deleteLoaiMonAn(maLoaiMonAn: String, onComplete: (Boolean) -> Unit) {
        val database = FirebaseDatabase.getInstance().getReference("LoaiMonAn")
        database.child(maLoaiMonAn).removeValue()
            .addOnSuccessListener { dataSnapshot ->
                onComplete(true)
            }
            .addOnFailureListener { exception ->
                onComplete(false)
            }
    }
}