package com.example.projectdatmonan

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.DialogFragment
import com.example.projectdatdatHang.Database.CRUD_DatHang


import com.example.projectdatmonan.Model.GioHang
import com.example.projectdatmonan.Model.ListMonAn
import com.example.projectdatmonan.Model.MonAn
import com.example.projectdatmonan.Model.NguoiDung
import com.google.firebase.database.*

class dialog_thanhtoan(maNguoiDung: String) : DialogFragment() {

    private lateinit var txtCartSummary: TextView
    private val maNguoiDung = maNguoiDung
    private lateinit var edtName: EditText
    private lateinit var edtPhoneNumber: EditText
    private lateinit var edtAddress: EditText
    private val items = mutableListOf<String>()
    private val listMonAn = mutableListOf<ListMonAn>()
    private var tongTien = 0.0
    private val crudDatHang = CRUD_DatHang()

    private lateinit var listener: OrderListener

    interface OrderListener {
        fun onOrderPlaced()
    }

    fun setOrderListener(listener: OrderListener) {
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dialog_thanhtoan, container, false)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        edtName = view.findViewById(R.id.edtName)
        edtPhoneNumber = view.findViewById(R.id.edtPhoneNumber)
        edtAddress = view.findViewById(R.id.edtAddress)

        txtCartSummary = view.findViewById(R.id.txtCartSummary)
        val btnPlaceOrder = view.findViewById<AppCompatButton>(R.id.btnPlaceOrder)
        val btnCancel = view.findViewById<AppCompatButton>(R.id.btnCancel)
        val edtPaymentMethod = view.findViewById<EditText>(R.id.edtPaymentMethod)

        edtPaymentMethod.setText("Tiền mặt")
        edtPaymentMethod.isEnabled = false
        loadCartItems()
        loadUserData()

        btnPlaceOrder.setOnClickListener {
            placeOrder()
        }

        btnCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun loadUserData() {
        val userRef = FirebaseDatabase.getInstance().getReference("NguoiDung").child("-O87fUJYQQvBzE_ZaWzD") // ID Người dùng

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nguoiDung = snapshot.getValue(NguoiDung::class.java)
                nguoiDung?.let {
                    edtName.setText(it.hoTen)
                    edtPhoneNumber.setText(it.sdt)
                    edtAddress.setText(it.diaChi)
                    Log.d("Họ tên", it.hoTen)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Failed to read user data", error.toException())
            }
        })
    }

    private fun loadCartItems() {
        val cartRef = FirebaseDatabase.getInstance().getReference("GioHang")

        cartRef.orderByChild("maNguoiDung").equalTo(maNguoiDung).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                items.clear()
                listMonAn.clear()
                for (cartSnapshot in snapshot.children) {
                    val gioHang = cartSnapshot.getValue(GioHang::class.java)
                    gioHang?.listMonAn?.forEach { listMonAnItem ->
                        listMonAnItem.maMonAn?.let { maMonAn ->
                            fetchDishDetails(maMonAn, listMonAnItem.soLuong ?: 0)
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Failed to read data", error.toException())
            }
        })
    }

    private fun fetchDishDetails(maMonAn: String, soLuong: Int) {
        val dishRef = FirebaseDatabase.getInstance().getReference("MonAn").child(maMonAn)
        dishRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val monAn = snapshot.getValue(MonAn::class.java)
                if (monAn != null) {
                    val giaGiam = monAn.gia?.let {
                        it * (100 - (monAn.trangThaiGiamGia ?: 0)) / 100
                    } ?: 0.0
                    val itemName = "- ${monAn.tenMonAn} (${giaGiam} VND) - Số lượng: $soLuong"
                    items.add(itemName)
                    listMonAn.add(ListMonAn(maMonAn, soLuong))
                    val gia = monAn.gia ?: 0.0
                    val giamGia = monAn.trangThaiGiamGia ?: 0
                    tongTien += (gia*(100-giamGia)/100) * soLuong
                    updateCartSummary(items)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Failed to read data", error.toException())
            }
        })
    }

    private fun updateCartSummary(items: List<String>) {
        val summary = items.joinToString("\n")
        txtCartSummary.text = summary
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun placeOrder() {
        val name = edtName.text.toString().trim()
        val diaChiGiaoHang = edtAddress.text.toString().trim()
        val sdt = edtPhoneNumber.text.toString().trim()

        if (name.isEmpty() || diaChiGiaoHang.isEmpty() || sdt.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "Vui lòng nhập đầy đủ thông tin",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        crudDatHang.placeOrder(maNguoiDung, listMonAn, diaChiGiaoHang, sdt, tongTien, {
            crudDatHang.clearCart(maNguoiDung, {
                listener.onOrderPlaced()
                Toast.makeText(
                    requireContext(),
                    "Đặt hàng thành công! Đơn hàng của bạn sẽ được giao trong thời gian sớm nhất",
                    Toast.LENGTH_LONG
                ).show()
                dismiss()
            }, { error ->
                Log.e("Firebase", "Failed to clear cart", error)
            })
        }, { error ->
            Log.e("Firebase", "Failed to place order", error)
        })
    }
}
