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
    private val dbConnection = DBConnection()
    fun addToCart(gioHang: GioHang, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val gioHangRef = dbConnection.getGioHangRef()

        gioHangRef.orderByChild("maNguoiDung").equalTo(gioHang.maNguoiDung)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Nếu giỏ hàng đã tồn tại
                        for (snapshot in dataSnapshot.children) {
                            val existingGioHang = snapshot.getValue(GioHang::class.java)
                            existingGioHang?.let { currentCart ->
                                // Kiểm tra xem listMonAn có món ăn với mã maMonAn đã tồn tại chưa
                                val updatedListMonAn = currentCart.listMonAn?.toMutableList() ?: mutableListOf()
                                val existingMonAn = updatedListMonAn.find { it.maMonAn == gioHang.listMonAn?.firstOrNull()?.maMonAn }

                                if (existingMonAn != null) {
                                    // Nếu món ăn đã tồn tại, tăng số lượng
                                    existingMonAn.soLuong = existingMonAn.soLuong?.plus(gioHang.listMonAn?.firstOrNull()?.soLuong ?: 0)
                                } else {
                                    // Nếu chưa tồn tại, thêm món ăn mới vào danh sách
                                    gioHang.listMonAn?.let { updatedListMonAn.addAll(it) }
                                }

                                currentCart.listMonAn = updatedListMonAn

                                // Cập nhật giỏ hàng trong Firebase
                                snapshot.ref.setValue(currentCart)
                                    .addOnSuccessListener { onSuccess() }
                                    .addOnFailureListener { exception ->
                                        onFailure(exception.message ?: "Lỗi không xác định")
                                    }
                            }
                        }
                    } else {
                        // Nếu giỏ hàng chưa tồn tại, tạo mới
                        val newCartRef = gioHangRef.push() // Tạo mã mới cho giỏ hàng
                        newCartRef.setValue(gioHang)
                            .addOnSuccessListener { onSuccess() }
                            .addOnFailureListener { exception ->
                                onFailure(exception.message ?: "Lỗi không xác định")
                            }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    onFailure(databaseError.message)
                }
            })
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
