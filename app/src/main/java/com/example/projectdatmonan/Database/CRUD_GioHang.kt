package com.example.projectdatmonan.Database

import android.util.Log
import com.example.projectdatmonan.Model.GioHang
import com.example.projectdatmonan.Model.ListMonAn
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

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
    fun fetchCartData(maNguoiDung: String, onComplete: (List<ListMonAn>) -> Unit, onError: (DatabaseError) -> Unit) {
        val cartRef = FirebaseDatabase.getInstance().getReference("GioHang")

        cartRef.orderByChild("maNguoiDung").equalTo(maNguoiDung).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val cartItems = mutableListOf<ListMonAn>()
                for (cartSnapshot in snapshot.children) {
                    val gioHang = cartSnapshot.getValue(GioHang::class.java)
                    gioHang?.let {
                        it.listMonAn?.let { items -> cartItems.addAll(items) }
                    }
                }
                onComplete(cartItems)
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error)
            }
        })
    }
    fun updateQuantityInDatabase(maNguoiDung: String, item: ListMonAn, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val cartRef = FirebaseDatabase.getInstance().getReference("GioHang")
        cartRef.orderByChild("maNguoiDung").equalTo(maNguoiDung).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (cartSnapshot in snapshot.children) {
                        val listMonAnRef = cartSnapshot.child("listMonAn").ref
                        listMonAnRef.orderByChild("maMonAn").equalTo(item.maMonAn).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(itemSnapshot: DataSnapshot) {
                                if (itemSnapshot.exists()) {
                                    for (itemRef in itemSnapshot.children) {
                                        itemRef.ref.child("soLuong").setValue(item.soLuong)
                                            .addOnSuccessListener {
                                                Log.d("Firebase", "Quantity updated successfully for user $maNguoiDung")
                                                onSuccess()
                                            }
                                            .addOnFailureListener { error ->
                                                Log.e("Firebase", "Failed to update quantity for user $maNguoiDung", error)
                                                onFailure(error)
                                            }
                                    }
                                } else {
                                    Log.d("Firebase", "Item not found for maMonAn: ${item.maMonAn}")
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e("Firebase", "Error querying cart items: ${error.message}")
                                onFailure(Exception(error.message))
                            }
                        })
                    }
                } else {
                    Log.d("Firebase", "No cart found for user: $maNguoiDung")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error fetching user cart: ${error.message}")
                onFailure(Exception(error.message))
            }
        })
    }
    fun removeItemFromCart(maNguoiDung: String, item: ListMonAn, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val cartRef = FirebaseDatabase.getInstance().getReference("GioHang")
        cartRef.orderByChild("maNguoiDung").equalTo(maNguoiDung).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (cartSnapshot in snapshot.children) {
                        val listMonAnRef = cartSnapshot.child("listMonAn").ref
                        listMonAnRef.orderByChild("maMonAn").equalTo(item.maMonAn).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(itemSnapshot: DataSnapshot) {
                                if (itemSnapshot.exists()) {
                                    for (itemRef in itemSnapshot.children) {
                                        itemRef.ref.removeValue()
                                            .addOnSuccessListener {
                                                Log.d("Firebase", "Item removed successfully from cart for user $maNguoiDung")
                                                onSuccess()
                                            }
                                            .addOnFailureListener { error ->
                                                Log.e("Firebase", "Failed to remove item from cart for user $maNguoiDung", error)
                                                onFailure(error)
                                            }
                                    }
                                } else {
                                    Log.d("Firebase", "Item not found for maMonAn: ${item.maMonAn}")
                                    onFailure(Exception("Item not found"))
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e("Firebase", "Error querying cart items: ${error.message}")
                                onFailure(Exception(error.message))
                            }
                        })
                    }
                } else {
                    Log.d("Firebase", "No cart found for user: $maNguoiDung")
                    onFailure(Exception("No cart found"))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error fetching user cart: ${error.message}")
                onFailure(Exception(error.message))
            }
        })
    }
}