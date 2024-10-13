package com.example.projectdatmonan.Database

import com.example.projectdatmonan.Model.MonAn
import com.google.firebase.database.*

class CRUD_MonAn {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference.child("MonAn")

    fun fetchMonAnData(onDataFetched: (List<Pair<String, MonAn>>) -> Unit, onError: (DatabaseError) -> Unit) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val monAnList = mutableListOf<Pair<String, MonAn>>()
                for (monAnSnapshot in snapshot.children) {
                    val monAn = monAnSnapshot.getValue(MonAn::class.java)
                    val monAnId = monAnSnapshot.key
                    if (monAn != null && monAnId != null) {
                        monAnList.add(Pair(monAnId, monAn))
                    }
                }
                onDataFetched(monAnList)
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error)
            }
        })
    }
}
