package com.example.projectdatmonan

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projectdatmonan.Database.CRUD_GioHang
import com.example.projectdatmonan.Database.CRUD_NguoiDung
import com.example.projectdatmonan.Model.GioHang
import com.example.projectdatmonan.Model.ListMonAn
import com.example.projectdatmonan.Model.MonAn
import com.example.projectdatmonan.databinding.FragmentCartBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class CartFragment : Fragment(), dialog_thanhtoan.OrderListener {

    private lateinit var binding: FragmentCartBinding
    private lateinit var cartAdapter: CartAdapter
    private val dishNames = mutableMapOf<String, String?>()
    private val dishPrices = mutableMapOf<String, Double?>()
    private val dishImages = mutableMapOf<String, String?>()
    private lateinit var maNguoiDung: String


    private lateinit var txtTotal: TextView
    private val crudGioHang = CRUD_GioHang()
    private val crudNguoiDung = CRUD_NguoiDung()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            user.email?.let {
                getUserIdByEmail(it) { userId ->
                    if (userId != null) {
                        maNguoiDung = userId // Gán giá trị userId cho manguoidung
                        Log.d("abc",maNguoiDung)
                    } else {
                        Log.d("abc",maNguoiDung)
                    }
                }
            }
        }


        binding = FragmentCartBinding.inflate(inflater, container, false)
        txtTotal = binding.txtTotal

        binding.button2.setOnClickListener {
            val checkoutDialog = dialog_thanhtoan()
            checkoutDialog.setOrderListener(this)
            checkoutDialog.show(requireActivity().supportFragmentManager, "CheckoutDialog")
        }

        fetchCartData()
        setupRecyclerView()

        return binding.root

    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(listOf(), dishNames, dishPrices, dishImages, maNguoiDung, { item -> }, {
            updateTotal()
        })
        binding.viewCart.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = cartAdapter
        }
    }

    private fun fetchCartData() {
        crudGioHang.fetchCartData(maNguoiDung, { cartItems ->
            if (cartItems.isEmpty()) {
                binding.txtEmpty.visibility = View.VISIBLE
                binding.viewCart.visibility = View.GONE
                binding.button2.isEnabled = false
            } else {
                binding.txtEmpty.visibility = View.GONE
                binding.viewCart.visibility = View.VISIBLE
                binding.button2.isEnabled = true
                fetchDishDetails(cartItems)
            }
            updateTotal()
        }, { error ->
            Log.e("Firebase", "Failed to read data", error.toException())
        })
    }

    private fun fetchDishDetails(cartItems: List<ListMonAn>) {
        val dishRef = FirebaseDatabase.getInstance().getReference("MonAn")
        var completedFetches = 0

        for (item in cartItems) {
            val maMonAn = item.maMonAn
            if (maMonAn != null) {
                dishRef.child(maMonAn).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val monAn = snapshot.getValue(MonAn::class.java)
                        if (monAn != null) {
                            dishNames[maMonAn] = monAn.tenMonAn
                            val gia = monAn.gia ?: 0.0
                            val giamGia = monAn.trangThaiGiamGia ?: 0
                            dishPrices[maMonAn] = gia * (100 - giamGia) / 100
                            monAn.hinhAnh?.firstOrNull()?.let { imagePath ->
                                val storageRef = FirebaseStorage.getInstance().getReference(imagePath)
                                storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                                    dishImages[maMonAn] = downloadUrl.toString()
                                }.addOnCompleteListener {
                                    completedFetches++
                                    if (completedFetches == cartItems.size) {
                                        cartAdapter.updateCartItems(cartItems)
                                        updateTotal()
                                    }
                                }
                            } ?: run {
                                completedFetches++
                            }
                        } else {
                            completedFetches++
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("Firebase", "Failed to read data", error.toException())
                    }
                })
            } else {
                completedFetches++
            }
        }
    }

    fun updateTotal() {
        var totalAmount = 0.0

        for ((maMonAn, price) in dishPrices) {
            val quantity = cartAdapter.getQuantityForDish(maMonAn)
            totalAmount += (price ?: 0.0) * (quantity ?: 0)
        }

        txtTotal.text = "${totalAmount} VND"
    }

    override fun onOrderPlaced() {
        fetchCartData()
    }

    private fun getUserIdByEmail(email: String, callback: (String?) -> Unit) {
        crudNguoiDung.getUserByEmailSnap(email) { snapshot ->
            if (snapshot != null) {
                val userId = snapshot.key // Lấy userId từ key của snapshot
                callback(userId)
            } else {
                callback(null) // Trả về null nếu không tìm thấy người dùng
            }
        }
    }
}
