package com.example.projectdatmonan

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projectdatmonan.Database.CRUD_GioHang
import com.example.projectdatmonan.Database.CRUD_NguoiDung
import com.example.projectdatmonan.Model.ListMonAn
import com.example.projectdatmonan.Model.MonAn
import com.example.projectdatmonan.databinding.FragmentCartBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.DecimalFormat
import com.google.firebase.storage.FirebaseStorage

class CartFragment : Fragment(), dialog_thanhtoan.OrderListener {

    private lateinit var binding: FragmentCartBinding
    private lateinit var cartAdapter: CartAdapter
    private val dishNames = mutableMapOf<String, String?>()
    private val dishPrices = mutableMapOf<String, Double?>()
    private val dishImages = mutableMapOf<String, String?>()
    private lateinit var maNguoiDung :String
    private lateinit var txtTotal: TextView
    private val crudGioHang = CRUD_GioHang()
    private val crudNguoiDung = CRUD_NguoiDung()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        maNguoiDung = arguments?.getString("USER_ID").toString()


        if (maNguoiDung != null) {
            Log.d("abcd",maNguoiDung)
        }


        binding = FragmentCartBinding.inflate(inflater, container, false)
        txtTotal = binding.txtTotal

        binding.button2.setOnClickListener {
            val checkoutDialog = dialog_thanhtoan(maNguoiDung)
            checkoutDialog.setOrderListener(this)
            checkoutDialog.show(requireActivity().supportFragmentManager, "CheckoutDialog")

        }

        fetchCartData()
        setupRecyclerView()

        return binding.root

    }

    private fun setupRecyclerView() {
        cartAdapter = maNguoiDung?.let {
            CartAdapter(listOf(), dishNames, dishPrices, dishImages, it, { item -> }, {
                updateTotal()
            })
        }!!
        binding.viewCart.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = cartAdapter
        }
    }

    private fun fetchCartData() {
        if (maNguoiDung != null) {
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
                                dishImages[maMonAn] = imagePath
                                completedFetches++
                                if (completedFetches == cartItems.size) {
                                    cartAdapter.updateCartItems(cartItems)
                                    updateTotal()
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
    private fun formatPrice(price: Double): String {
        val formatter = DecimalFormat("#,###") // Định dạng với dấu phẩy phân cách hàng nghìn
        return formatter.format(price)
    }

    fun updateTotal() {
        var totalAmount = 0.0

        for ((maMonAn, price) in dishPrices) {
            val quantity = cartAdapter.getQuantityForDish(maMonAn)
            totalAmount += (price ?: 0.0) * (quantity ?: 0)
        }

        txtTotal.text = "${formatPrice(totalAmount)} VND"
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
