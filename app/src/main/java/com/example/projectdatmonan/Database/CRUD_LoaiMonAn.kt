package com.example.projectdatLoaiMonAn.Database

import com.example.projectdatmonan.Database.CRUD_MonAn
import com.example.projectdatmonan.Model.LoaiMonAn
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class CRUD_LoaiLoaiMonAn {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    fun addLoaiLoaiMonAn(loaiMonAn: LoaiMonAn, onComplete: (String?) -> Unit) {
        val newId = database.child("LoaiMonAn").push().key
        if (newId != null) {
            database.child("LoaiMonAn").child(newId).setValue(loaiMonAn)
                .addOnSuccessListener {
                    onComplete(newId)
                }
                .addOnFailureListener { exception ->
                    println("Error adding LoaiMonAn: ${exception.message}")
                    onComplete(null)
                }
        } else {
            println("Error generating user ID")
            onComplete(null)
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

        //Xóa hết món nă của loại đó
        val dbMonAn = CRUD_MonAn()
        dbMonAn.getMonAnTheoLoai(maLoaiMonAn){data->
            for (i in data?.keys!!){
                if (i != null) {
                    dbMonAn.deleteMonAn(i){success->
                        if (!success){
                            onComplete(false)
                        }
                    }
                }
            }
        }

        database.child(maLoaiMonAn).removeValue()
            .addOnSuccessListener { dataSnapshot ->
                onComplete(true)
            }
            .addOnFailureListener { exception ->
                onComplete(false)
            }
    }
}

package com.example.projectdatmonan.Database

import com.example.projectdatmonan.Model.LoaiMonAn
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CRUD_LoaiMonAn {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference


    fun loadCategory(onComplete: (List<LoaiMonAn>, Map<String, String>) -> Unit, onError: (String) -> Unit) {
        val ref = database.child("LoaiMonAn")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lists = mutableListOf<LoaiMonAn>()
                val categoryIdMap = mutableMapOf<String, String>()

                for (childSnapshot in snapshot.children) {
                    val category = childSnapshot.getValue(LoaiMonAn::class.java)
                    val key = childSnapshot.key
                    if (category != null && key != "All") {
                        lists.add(category)
                        categoryIdMap[category.tenMonAn ?: ""] = key ?: ""
                    }
                }
                onComplete(lists, categoryIdMap)
            }

            override fun onCancelled(error: DatabaseError) {
                onError("Failed to load data: ${error.message}")
            }
        })
    }

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