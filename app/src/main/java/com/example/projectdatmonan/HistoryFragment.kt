package com.example.projectdatmonan

import DatHang1
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectdatmonan.Database.CRUD_NguoiDung
import com.example.projectdatmonan.Model.ListMonAn
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HistoryFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private var orderList = mutableListOf<DatHang1>()
    private lateinit var recyclerViewOrders: RecyclerView
    private var currentUserId: String? = null
    private var selectedDate: String? = null
    private var minPrice: Double? = null
    private var maxPrice: Double? = null
    var totalPrice = 0.0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase reference
        database = FirebaseDatabase.getInstance().getReference("DatHang")

        // Find RecyclerView by ID
        recyclerViewOrders = view.findViewById(R.id.recyclerViewOrders)

        // Setup RecyclerView
        recyclerViewOrders.layoutManager = LinearLayoutManager(context)
        val tvSelectedDate = view.findViewById<TextView>(R.id.tvSelectedDate)
        val btnSelectDate = view.findViewById<Button>(R.id.btnDateFilter)
        val etMinPrice = view.findViewById<EditText>(R.id.etMinPrice)
        val etMaxPrice = view.findViewById<EditText>(R.id.etMaxPrice)
        val btnApplyFilter = view.findViewById<Button>(R.id.btnFilter)

        btnSelectDate.setOnClickListener {
            // Mở DatePicker và lấy ngày đã chọn
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    selectedDate = "$dayOfMonth/${month + 1}/$year"
                    tvSelectedDate.text = "Ngày đã chọn: $selectedDate"
                    tvSelectedDate.visibility = View.VISIBLE // Hiện TextView sau khi chọn ngày
                },
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        // Get current user ID and fetch orders
        getCurrentUserId { userId ->
            currentUserId = userId
            fetchOrdersFromFirebase()
        }
        btnApplyFilter.setOnClickListener {
            minPrice = etMinPrice.text.toString().toDoubleOrNull()
            maxPrice = etMaxPrice.text.toString().toDoubleOrNull()
            fetchOrdersFromFirebase() // Gọi lại hàm fetchOrders để lọc theo điều kiện
        }
    }

    // Function to get current user ID by email
    private fun getCurrentUserId(callback: (String?) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val email = it.email
            email?.let {
                // Use your CRUD_NguoiDung class to get user information by email
                val crudNguoiDung = CRUD_NguoiDung()
                crudNguoiDung.getUserByEmailSnap(email) { nguoiDung ->
                    nguoiDung?.let { userData ->
                        callback(userData.key) // Return userId
                    } ?: run {
                        Toast.makeText(context, "User not found", Toast.LENGTH_SHORT).show()
                        callback(null)
                    }
                }
            }
        } ?: run {
            Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
            callback(null)
        }
    }

    private fun fetchOrdersFromFirebase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                orderList.clear()
                for (orderSnapshot in snapshot.children) {
                    val orderKey = orderSnapshot.key // Lấy mã đặt hàng (order key)
                    val orderData = orderSnapshot.value as Map<String, Any>?

                    if (orderData != null) {
                        val maNguoiDung = orderData["maNguoiDung"] as? String
                        val diaChiGiaoHang = orderData["diaChiGiaoHang"] as? String
                        val tinhTrang = orderData["tinhTrang"] as? String
                        val ngayGioDat = orderData["ngayGioDat"] as? String
                        val sdt = orderData["sdt"] as? String
                        val tongTien=orderData["tongTien"] as? Long


                        // Lấy danh sách món ăn từ ArrayIDMaMonAn
                        val arrayIDMaMonAn = orderData["listMonAn"] as? List<Map<String, Any>>


                        val listMonAn = mutableListOf<ListMonAn>()


                        arrayIDMaMonAn?.forEach { item ->
                            val maMonAn = item["maMonAn"] as? String
                            val soLuong = (item["soLuong"] as? Long)?.toInt() // Chuyển đổi Long thành Int
                            val monAn = ListMonAn(maMonAn, soLuong)
                            listMonAn.add(monAn)
                        }



                        // Tạo đối tượng DatHang1 với mã đặt hàng
                        val order = DatHang1(
                            maDatHang = orderKey, // Gán mã đặt hàng vào
                            maNguoiDung = maNguoiDung,
                            diaChiGiaoHang = diaChiGiaoHang,
                            tinhTrang = tinhTrang,
                            ngayGioDat = ngayGioDat,
                            sdt = sdt,
                            tongTien = tongTien,
                            listMonAn = listMonAn
                        )

                        // Kiểm tra nếu mã người dùng trùng khớp với mã người dùng hiện tại
                        if (maNguoiDung == currentUserId && filterOrder(order)) {
                            orderList.add(order)
                        }

                    }
                }
                // Refresh RecyclerView with data
                setupRecyclerView(orderList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load orders", Toast.LENGTH_SHORT).show()
            }
        })
    }
    fun isSameDate(dateStr1: String?, dateStr2: String?): Boolean {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return try {
            val date1 = dateStr1?.let { dateFormat.parse(it) }
            val date2 = dateStr2?.let { dateFormat.parse(it) }
            date1?.time == date2?.time
        } catch (e: Exception) {
            false
        }
    }
    private fun filterOrder(order: DatHang1): Boolean {
        // Lọc theo ngày nếu người dùng chọn
        val ngayDat = order.ngayGioDat?.split(" ")?.get(0) ?: ""
        if (selectedDate != null && !isSameDate(ngayDat, selectedDate)) {
            return false
        }

        // Lọc theo giá



        // Kiểm tra nếu tổng tiền nằm trong khoảng giá
        val totalPrice = order.tongTien?.toDouble() ?: return false // Nếu tongTien là null, trả về false

        if (minPrice != null && totalPrice < minPrice!!) {
            return false
        }
        if (maxPrice != null && totalPrice > maxPrice!!) {
            return false
        }

        return true


    }




private fun setupRecyclerView(orders: List<DatHang1>) {
    recyclerViewOrders.adapter = object : RecyclerView.Adapter<OrderViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_order_history, parent, false)
            return OrderViewHolder(view)
        }

        override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
            val order = orders[position]
            holder.bind(order)
        }

        override fun getItemCount(): Int {
            return orders.size
        }
    }
}

// ViewHolder class for RecyclerView
inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val tvOrderID: TextView = itemView.findViewById(R.id.tvOrderID)
    private val tvOrderDate: TextView = itemView.findViewById(R.id.tvOrderDate)
    private val tvOrderAddress: TextView = itemView.findViewById(R.id.tvOrderAddress)
    private val tvOrderItems: TextView = itemView.findViewById(R.id.tvOrderItems)
    private val tvTotalPrice: TextView = itemView.findViewById(R.id.tvTotalPrice)
    private val tvPaymentMethod: TextView = itemView.findViewById(R.id.tvPaymentMethod)
    private val tvCustomerName: TextView = itemView.findViewById(R.id.tvCustomerName)
    private val tvPhoneNumber: TextView = itemView.findViewById(R.id.tvPhoneNumber)


    fun bind(order: DatHang1) {

        tvOrderID.text = "Mã đơn hàng: ${order.maDatHang}"
        tvOrderDate.text = "Ngày đặt: ${order.ngayGioDat.toString()}"
        tvOrderAddress.text = "Địa chỉ giao: ${order.diaChiGiaoHang}"
        tvPhoneNumber.text="Số điện thoại: ${order.sdt}"


        val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
        val formattedPrice = formatter.format(order.tongTien)

// Hiển thị giá trị đã định dạng
        tvTotalPrice.text = "Tổng tiền: $formattedPrice VND"



        // Lấy họ tên từ Firebase
        val userRef = FirebaseDatabase.getInstance().getReference("NguoiDung").child(order.maNguoiDung!!)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(userSnapshot: DataSnapshot) {
                val hoTen = userSnapshot.child("hoTen").value as? String
                // Cập nhật lại danh sách đơn hàng với họ tên
                tvCustomerName.text = hoTen // Nếu bạn thêm thuộc tính hoTen vào DatHang1
                // Có thể hiển thị họ tên ở nơi khác trong UI nếu cần
            }

            override fun onCancelled(userError: DatabaseError) {
                Toast.makeText(context, "Lỗi khi lấy thông tin người dùng", Toast.LENGTH_SHORT).show()
            }
        })







        val itemsStringBuilder = StringBuilder("Thực đơn:\n")
        var totalAmount = 0.0
        var itemsProcessed = 0 // Đếm số món ăn đã được xử lý

        order.listMonAn?.let { listMonAn ->
            // Kiểm tra nếu danh sách món ăn không rỗng
            listMonAn.forEach { item ->
                val maMonAn = item.maMonAn
                val soLuong = item.soLuong

                // Lấy thông tin từ Firebase về món ăn theo mã
                val monAnRef = FirebaseDatabase.getInstance().getReference("MonAn").child(maMonAn.toString())
                monAnRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val tenMonAn = snapshot.child("tenMonAn").value as? String ?: "Không xác định"
                        val gia = snapshot.child("gia").value as? Long ?: 0




                        // Cập nhật chuỗi món ăn
                        itemsStringBuilder.append("- $tenMonAn  - Số lượng: $soLuong\n")

                        // Kiểm tra nếu tất cả các món ăn đã được xử lý
                        itemsProcessed++
                        if (itemsProcessed == listMonAn.size) {
                            // Cập nhật danh sách món ăn và tổng tiền lên giao diện
                            tvOrderItems.text = itemsStringBuilder.toString()

                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(itemView.context, "Lỗi khi tải thông tin món ăn", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }

        // Hiển thị phương thức thanh toán (giả định là luôn là "Tiền Mặt")
        tvPaymentMethod.text = "Thanh toán: Tiền Mặt"
    }


}
}
