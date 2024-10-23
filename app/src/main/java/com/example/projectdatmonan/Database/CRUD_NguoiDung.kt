package com.example.projectdatmonan.Database

import com.example.projectdatmonan.Model.MonAn
import com.example.projectdatmonan.Model.NguoiDung
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

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

}