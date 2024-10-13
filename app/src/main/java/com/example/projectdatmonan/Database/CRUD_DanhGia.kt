package com.example.projectdatmonan.Database

import com.example.projectdatmonan.Model.DanhGia
import com.example.projectdatmonan.Model.MonAn
import com.google.firebase.database.*

class CRUD_DanhGia {

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
                    danhGia?.soSao?.let { ratings.add(it) }
                }
                onComplete(ratings)
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error)
            }
        })
    }
}
