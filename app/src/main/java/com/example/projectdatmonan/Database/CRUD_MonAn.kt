package com.example.projectdatmonan.Database

import com.example.projectdatmonan.Model.MonAn
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class CRUD_MonAn {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    fun addMonAn(monAn: MonAn, onComplete: (Boolean) -> Unit) {
        val newUserId = database.child("MonAn").push().key
        if (newUserId != null) {
            database.child("MonAn").child(newUserId).setValue(monAn)
                .addOnSuccessListener {
                    onComplete(true)
                }
                .addOnFailureListener { exception ->
                    println("Error adding user: ${exception.message}")
                    onComplete(false)
                }
        } else {
            println("Error generating user ID")
            onComplete(false)
        }
    }


    fun getAllMonAn(onComplete: (HashMap<String?,MonAn?>?) -> Unit) {
        val database = FirebaseDatabase.getInstance()
        val monAnRef: DatabaseReference = database.getReference("MonAn")
        val listMonAn:HashMap<String?,MonAn?> = HashMap<String?,MonAn?>()

        monAnRef.get()
            .addOnSuccessListener { dataSnapshot ->
                    for (monAnSnapshot in dataSnapshot.children) {
                        val monAn = monAnSnapshot.getValue(MonAn::class.java)
                        listMonAn[monAnSnapshot.key] = monAn
//                        gioHangSnapshot.key
//                        if (monAn != null) {
//                            listMonAn.add(monAn)
//                        }
                    }
                onComplete(listMonAn)
            }
            .addOnFailureListener { exception ->
                println("Error retrieving data: ${exception.message}")
                onComplete(null)
            }
    }

    fun updateMonAn(maMonAn:String,updatedMonAn: MonAn, onComplete: (Boolean) -> Unit) {
        val database = FirebaseDatabase.getInstance()
        val monAnRef = database.getReference("MonAn").child(maMonAn)

        // Query to find the GioHang node by maNguoiDung
        monAnRef.setValue(updatedMonAn)
            .addOnSuccessListener { dataSnapshot ->
               onComplete(true)
            }
            .addOnFailureListener { exception ->
                onComplete(false)
            }
    }

    fun deleteMonAn(maMonAn: String, onComplete: (Boolean) -> Unit) {
        val database = FirebaseDatabase.getInstance().getReference("MonAn")
        // Xóa món ăn theo ID
        database.child(maMonAn).removeValue()
            .addOnSuccessListener { dataSnapshot ->
            onComplete(true)
        }
            .addOnFailureListener { exception ->
                onComplete(false)
            }
        }


}