package com.example.projectdatmonan

import DatHang
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectdatmonan.Model.ListMonAn
import com.google.firebase.database.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class DuyetDonFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private var orderList = mutableListOf<DatHang>()
    private lateinit var recyclerViewOrders: RecyclerView
    private lateinit var spinnerFilterStatus: Spinner
    private lateinit var btnDatePicker: Button
    private lateinit var tvSelectedDate: TextView  // TextView để hiển thị ngày đã chọn
    private var selectedDate: Calendar? = null
    private var filteredOrderList = mutableListOf<DatHang>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_duyetdon, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase reference
        database = FirebaseDatabase.getInstance().getReference("DatHang")

        // Find RecyclerView, Spinner, and Button by ID
        recyclerViewOrders = view.findViewById(R.id.recyclerOrderList)
        spinnerFilterStatus = view.findViewById(R.id.spinnerStatusFilter)
        btnDatePicker = view.findViewById(R.id.btnDatePicker)
        tvSelectedDate = view.findViewById(R.id.tvSelectedDate)

        // Setup RecyclerView
        recyclerViewOrders.layoutManager = LinearLayoutManager(context)

        // Setup Spinner for filtering order status
        setupFilterSpinner()

        // Setup DatePicker button
        btnDatePicker.setOnClickListener {
            showDatePickerDialog()
        }

        // Fetch all orders from Firebase
        fetchOrdersFromFirebase()
    }

    private fun setupFilterSpinner() {
        val orderStatusList = listOf("Tất cả", "Chờ xử lý", "Đang giao", "Hoàn thành", "Đã hủy")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, orderStatusList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFilterStatus.adapter = adapter

        spinnerFilterStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                filterOrders()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                selectedDate = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }

                // Cập nhật TextView hiển thị ngày đã chọn
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val selectedDateString = dateFormat.format(selectedDate?.time)
                tvSelectedDate.text = selectedDateString

                // Lọc đơn hàng sau khi chọn ngày
                filterOrders()
            }, year, month, day
        )

        datePickerDialog.show()
    }

    private fun filterOrders() {
        filteredOrderList.clear()

        val selectedStatus = spinnerFilterStatus.selectedItem.toString()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val selectedDateString = selectedDate?.let { dateFormat.format(it.time) }

        filteredOrderList.addAll(orderList.filter { order ->
            val matchesStatus = if (selectedStatus == "Tất cả") true else order.tinhTrang == selectedStatus
            val matchesDate = selectedDateString?.let {
                val orderDate = order.ngayGioDat
                orderDate?.let {
                    val orderFormattedDate = dateFormat.format(dateFormat.parse(it))
                    selectedDateString == orderFormattedDate
                } ?: false
            } ?: true

            matchesStatus && matchesDate
        })

        setupRecyclerView(filteredOrderList)
    }

    private fun fetchOrdersFromFirebase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                orderList.clear()
                for (orderSnapshot in snapshot.children) {
                    val orderKey = orderSnapshot.key
                    val orderData = orderSnapshot.value as Map<String, Any>?

                    if (orderData != null) {
                        val maNguoiDung = orderData["maNguoiDung"] as? String
                        val diaChiGiaoHang = orderData["diaChiGiaoHang"] as? String
                        val tinhTrang = orderData["tinhTrang"] as? String
                        val ngayGioDat = orderData["ngayGioDat"] as? String
                        val sdt = orderData["sdt"] as? String
                        val tongTien = orderData["tongTien"] as? Long
                        val arrayIDMaMonAn = orderData["listMonAn"] as? List<Map<String, Any>>

                        val listMonAn = mutableListOf<ListMonAn>()
                        arrayIDMaMonAn?.forEach { item ->
                            val maMonAn = item["maMonAn"] as? String
                            val soLuong = (item["soLuong"] as? Long)?.toInt()
                            val monAn = ListMonAn(maMonAn, soLuong)
                            listMonAn.add(monAn)
                        }

                        val order = DatHang(
                            maDatHang = orderKey,
                            maNguoiDung = maNguoiDung,
                            diaChiGiaoHang = diaChiGiaoHang,
                            tinhTrang = tinhTrang,
                            ngayGioDat = ngayGioDat,
                            sdt = sdt,
                            tongTien = tongTien,
                            listMonAn = listMonAn
                        )

                        orderList.add(order)
                    }
                }
                filterOrders()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load orders", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupRecyclerView(orders: List<DatHang>) {
        recyclerViewOrders.adapter = object : RecyclerView.Adapter<OrderViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_duyetdon, parent, false)
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


    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvOrderID: TextView = itemView.findViewById(R.id.tvOrderID)
        private val tvOrderDate: TextView = itemView.findViewById(R.id.tvOrderDate)
        private val tvOrderAddress: TextView = itemView.findViewById(R.id.tvOrderAddress)
        private val tvOrderItems: TextView = itemView.findViewById(R.id.tvOrderItems)
        private val tvTotalPrice: TextView = itemView.findViewById(R.id.tvTotalPrice)
        private val tvPaymentMethod: TextView = itemView.findViewById(R.id.tvPaymentMethod)
        private val tvCustomerName: TextView = itemView.findViewById(R.id.tvCustomerName)
        private val tvPhoneNumber: TextView = itemView.findViewById(R.id.tvPhoneNumber)
        private val spinnerOrderStatus: Spinner = itemView.findViewById(R.id.spinnerOrderStatus)

        fun bind(order: DatHang) {
            tvOrderID.text = "Mã đơn hàng: ${order.maDatHang}"
            tvOrderDate.text = "Ngày đặt: ${order.ngayGioDat}"
            tvOrderAddress.text = "Địa chỉ giao: ${order.diaChiGiaoHang}"
            tvPhoneNumber.text = "Số điện thoại: ${order.sdt}"

            val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
            val formattedPrice = formatter.format(order.tongTien)
            tvTotalPrice.text = "Tổng tiền: $formattedPrice VND"

            val userRef = FirebaseDatabase.getInstance().getReference("NguoiDung").child(order.maNguoiDung!!)
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(userSnapshot: DataSnapshot) {
                    val hoTen = userSnapshot.child("hoTen").value as? String
                    tvCustomerName.text = hoTen
                }

                override fun onCancelled(userError: DatabaseError) {
                    Toast.makeText(context, "Lỗi khi lấy thông tin người dùng", Toast.LENGTH_SHORT).show()
                }
            })

            val itemsStringBuilder = StringBuilder("Thực đơn:\n")
            var itemsProcessed = 0
            order.listMonAn?.forEach { item ->
                val maMonAn = item.maMonAn
                val soLuong = item.soLuong

                val monAnRef = FirebaseDatabase.getInstance().getReference("MonAn").child(maMonAn.toString())
                monAnRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val tenMonAn = snapshot.child("tenMonAn").value as? String ?: "Không xác định"
                        itemsStringBuilder.append("- $tenMonAn  || Số lượng: $soLuong\n")
                        itemsProcessed++
                        if (itemsProcessed == order.listMonAn?.size) {
                            tvOrderItems.text = itemsStringBuilder.toString()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(itemView.context, "Lỗi khi tải thông tin món ăn", Toast.LENGTH_SHORT).show()
                    }
                })
            }

            tvPaymentMethod.text = "Thanh toán: Tiền Mặt"

            val orderStatusList = listOf("Chờ xử lý", "Đang giao", "Hoàn thành", "Đã hủy")
            val adapter = ArrayAdapter(itemView.context, android.R.layout.simple_spinner_item, orderStatusList)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerOrderStatus.adapter = adapter

            val currentStatusIndex = orderStatusList.indexOf(order.tinhTrang)
            if (currentStatusIndex >= 0) {
                spinnerOrderStatus.setSelection(currentStatusIndex)
            }

            spinnerOrderStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    val newStatus = orderStatusList[position]
                    if (newStatus != order.tinhTrang) {
                        val orderRef = FirebaseDatabase.getInstance().getReference("DatHang").child(order.maDatHang!!)
                        orderRef.child("tinhTrang").setValue(newStatus)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }
    }


}
