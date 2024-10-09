package com.example.projectdatmonan

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.DialogFragment
import com.example.projectdatmonan.Model.DatHang
import com.example.projectdatmonan.Model.GioHang
import com.example.projectdatmonan.Model.ListMonAn
import com.example.projectdatmonan.Model.MonAn
import com.example.projectdatmonan.Model.NguoiDung
import com.google.firebase.database.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class dialog_thanhtoan : DialogFragment() {

    private lateinit var txtCartSummary: TextView
    private val maNguoiDung = "user01"
    private lateinit var edtName: EditText
    private lateinit var edtPhoneNumber: EditText
    private lateinit var edtAddress: EditText
    private val items = mutableListOf<String>()
    private val listMonAn = mutableListOf<ListMonAn>()

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
                    edtPhoneNumber.setText(it.sDT)
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
                    val itemName = "- ${monAn.tenMonAn} (${monAn.gia} VND) - Số lượng: $soLuong"
                    items.add(itemName)
                    listMonAn.add(ListMonAn(maMonAn, soLuong))
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
        val orderId = FirebaseDatabase.getInstance().getReference("DatHang").push().key

        val diaChiGiaoHang = edtAddress.text.toString()
        val sdt = edtPhoneNumber.text.toString()

        val datHang = DatHang(
            maNguoiDung = maNguoiDung,
            listMonAn = listMonAn,
            ngayGioDat = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")),
            tinhTrang = "Đang xử lý",
            diaChiGiaoHang = diaChiGiaoHang,
            sdt = sdt
        )

        orderId?.let {
            FirebaseDatabase.getInstance().getReference("DatHang").child(it).setValue(datHang)
                .addOnSuccessListener {
                    clearCart(maNguoiDung)
                    listener.onOrderPlaced() // Gọi callback
                    dismiss()
                }
                .addOnFailureListener { e ->
                    Log.e("Firebase", "Failed to add order", e)
                }
        }
    }

    private fun clearCart(maNguoiDung: String) {
        val cartRef = FirebaseDatabase.getInstance().getReference("GioHang")

        cartRef.orderByChild("maNguoiDung").equalTo(maNguoiDung).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (cartSnapshot in snapshot.children) {
                    cartSnapshot.ref.removeValue()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Failed to clear cart", error.toException())
            }
        })
    }
}
