package com.example.projectdatmonan

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projectdatmonan.Adapter.DanhGiaAdapter
import com.example.projectdatmonan.Adapter.ImagesAdapter
import com.example.projectdatmonan.Database.CRUD_DanhGia
import com.example.projectdatmonan.Database.CRUD_GioHang
import com.example.projectdatmonan.Database.DBConnection
import com.example.projectdatmonan.Model.DanhGia
import com.example.projectdatmonan.Model.GioHang
import com.example.projectdatmonan.Model.ListMonAn
import com.google.android.material.bottomsheet.BottomSheetDialog

class   ChiTietMonAnActivity : AppCompatActivity() {

    private lateinit var danhGiaAdapter: DanhGiaAdapter
    private lateinit var dbConnection: DBConnection
    private lateinit var imagesAdapter: ImagesAdapter
    private lateinit var crudDanhGia: CRUD_DanhGia
    private lateinit var crudGioHang: CRUD_GioHang
    private lateinit var soLuongDanhGiaTextView: TextView
    private lateinit var sapXepTheoTextView: AutoCompleteTextView
    private lateinit var maNguoiDung1 :String

    private val danhSachTieuChi = listOf(
        "Gần đây nhất",
        "Số sao tăng dần",
        "Số sao giảm dần"
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chitietmonan)

        // Initialize DBConnection and CRUD classes

        crudDanhGia = CRUD_DanhGia()
        crudGioHang = CRUD_GioHang()

        soLuongDanhGiaTextView = findViewById(R.id.soLuongDanhGia)
        sapXepTheoTextView = findViewById(R.id.SapXepTheo)

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, danhSachTieuChi)
        sapXepTheoTextView.setAdapter(adapter)

        val btnThem = findViewById<Button>(R.id.btn_Them)
        val btn_GioHang = findViewById<ImageButton>(R.id.btn_GioHang)
        val btn_back = findViewById<ImageButton>(R.id.btn_back)
        val rvImages = findViewById<RecyclerView>(R.id.rv_images)



        // Get the data passed from HomeFragment
        val foodName = intent.getStringExtra("tenMonAn")
        val foodPrice = intent.getDoubleExtra("gia", 0.0)
        val moTa = intent.getStringExtra("moTa")
        val maMonAn = intent.getStringExtra("monAnId")
        maNguoiDung1 = intent.getStringExtra("userID").toString()

        val trangThaiGiamGia = intent.getIntExtra("trangThaiGiamGia",100)


        val hinhAnhList = intent.getStringArrayListExtra("hinhAnhList") ?: arrayListOf()


        val foodNameTextView = findViewById<TextView>(R.id.tv_food_name)
        val foodPriceTextView = findViewById<TextView>(R.id.tv_giaGoc)
        val descriptionTextView = findViewById<TextView>(R.id.tv_moTa)
        val foodImageView = findViewById<ImageView>(R.id.img_food)
        val giamGia = findViewById<TextView>(R.id.tv_giaGiam)
        val tinhGiaGiam = foodPrice * (100 - trangThaiGiamGia?.toInt()!!) / 100


        // Display food details
        foodNameTextView.text = foodName
        foodPriceTextView.text = "$foodPrice VNĐ"
        descriptionTextView.text = moTa
        if(trangThaiGiamGia!=0)
            giamGia.text = "→ "+tinhGiaGiam.toString() + " VNĐ"
        else
            giamGia.isVisible=false


        Glide.with(this).load(hinhAnhList[0]).into(foodImageView)

        // Set up the back button
        btn_back.setOnClickListener {
            finish()
        }

        // Set up Add to Cart button click listener
        btnThem.setOnClickListener {
            ThemMonAnVaoGioHang(foodName, foodPrice, hinhAnhList[0], maMonAn)
        }

        btn_GioHang.setOnClickListener {
            ThemMonAnVaoGioHang(foodName, foodPrice, hinhAnhList[0], maMonAn)
        }

        val vietDanhGiaTextView = findViewById<TextView>(R.id.vietDanhGia)
        vietDanhGiaTextView.setOnClickListener {
            ThemDanhGia(maMonAn)
        }

        // Set up the review list
        val danhGiaList = mutableListOf<DanhGia>()
        danhGiaAdapter = DanhGiaAdapter(danhGiaList,crudDanhGia,maNguoiDung1)

        val recyclerView = findViewById<RecyclerView>(R.id.lvBinhLuanMonAn)
        val horizontalLayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = horizontalLayoutManager
        recyclerView.adapter = danhGiaAdapter

        // Load reviews from Firebase
        loadDanhGia(maMonAn)


        // Set up RecyclerView for additional images
        imagesAdapter = ImagesAdapter(hinhAnhList)
        rvImages.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvImages.adapter = imagesAdapter

        sapXepTheoTextView.setOnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> sapXepTheoThoiGianGiamDan()
                1 -> sapXepTheoSoSaoTangDan()
                2 -> sapXepTheoSoSaoGiamDan()
            }
        }
    }


    // Show BottomSheet to add food to cart
    private fun ThemMonAnVaoGioHang(
        foodName: String?,
        foodPrice: Double?,
        hinhAnhUrl: String?,
        maMonAn: String?
    ) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val dialogView = layoutInflater.inflate(R.layout.themspvaogiohang, null)

        val quantityTextView: TextView = dialogView.findViewById(R.id.soluong)
        val btnGiam: ImageButton = dialogView.findViewById(R.id.btn_giam)
        val btnTang: ImageButton = dialogView.findViewById(R.id.btn_tang)
        val btnAddToCart: Button = dialogView.findViewById(R.id.btn_themVaoGioHang)
        val btnCancel: Button = dialogView.findViewById(R.id.btn_huyBo)

        val foodNameTextView = dialogView.findViewById<TextView>(R.id.item_ten)
        val foodPriceTextView = dialogView.findViewById<TextView>(R.id.item_gia)
        val foodImageView = dialogView.findViewById<ImageView>(R.id.item_anh)

        foodNameTextView.text = foodName
        foodPriceTextView.text = "$foodPrice VNĐ"

        Glide.with(this).load(hinhAnhUrl).into(foodImageView)

        var quantity = 1
        quantityTextView.text = quantity.toString()

        btnGiam.setOnClickListener {
            if (quantity > 1) {
                quantity--
                quantityTextView.text = quantity.toString()
            }
        }

        btnTang.setOnClickListener {
            quantity++
            quantityTextView.text = quantity.toString()
        }

        btnAddToCart.setOnClickListener {
            val monAn = ListMonAn(maMonAn = maMonAn, soLuong = quantity)
            val gioHang = GioHang(maNguoiDung1, listMonAn = listOf(monAn))

            // Call CRUD_GioHang to handle adding to cart
            crudGioHang.addToCart(gioHang, {
                Toast.makeText(this, "$foodName đã được thêm vào giỏ hàng", Toast.LENGTH_SHORT).show()
            }, { errorMessage ->
                Toast.makeText(this, "Thêm vào giỏ hàng thất bại: $errorMessage", Toast.LENGTH_SHORT).show()
            })

            bottomSheetDialog.dismiss()
        }

        btnCancel.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.setContentView(dialogView)
        bottomSheetDialog.show()
    }

    // Show BottomSheet to add a review
    private fun ThemDanhGia(maMonAn: String?) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val dialogView = layoutInflater.inflate(R.layout.vietdanhgia_layout, null)

        val ratingBar = dialogView.findViewById<RatingBar>(R.id.DiemDanhGiaNhanXet)
        val editText = dialogView.findViewById<EditText>(R.id.edtNhanXet)
        val btnGuiDanhGia = dialogView.findViewById<Button>(R.id.btnGuiDanhGia)

        btnGuiDanhGia.setOnClickListener {

            val rating = ratingBar.rating
            val reviewContent = editText.text.toString().trim()
            // Kiểm tra rating và nội dung đánh giá
            if (rating == 0.0f) {
                Toast.makeText(this, "Vui lòng chọn điểm đánh giá", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (reviewContent.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập nội dung đánh giá", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            val currentTime = System.currentTimeMillis()
            val formattedTime =
                java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                    .format(currentTime)

            // Tạo đối tượng đánh giá
            val danhGia = DanhGia(
                maMonAn = maMonAn,
                maNguoiDung = maNguoiDung1,
                thoiGian = formattedTime,
                noiDung = reviewContent,
                soSao = rating
            )


            // Call CRUD_DanhGia to add the review
            crudDanhGia.addDanhGia(danhGia, {
                Toast.makeText(this, "Đánh giá đã được gửi", Toast.LENGTH_SHORT).show()
                // Reload reviews after adding a new one
                loadDanhGia(maMonAn) // Refresh the list
            }, { errorMessage ->
                Toast.makeText(this, "Gửi đánh giá thất bại: $errorMessage", Toast.LENGTH_SHORT).show()
            })

            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.setContentView(dialogView)
        bottomSheetDialog.show()
    }

    // Load reviews from Firebase via CRUD_DanhGia
    private fun loadDanhGia(maMonAn: String?) {
        crudDanhGia.getDanhGiaByMonAn(maMonAn) { danhGiaList ->
            if (danhGiaList != null) {
                danhGiaAdapter.clearDanhGia()
                danhGiaAdapter.addAllDanhGia(danhGiaList)

                val soDanhGia = danhGiaList.size
                soLuongDanhGiaTextView.text = "($soDanhGia lượt)"


            } else {
                Toast.makeText(this, "Lỗi tải đánh giá", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sapXepTheoThoiGianGiamDan() {
        val danhGiaMoiNhat = danhGiaAdapter.getDanhSachDanhGia()
            .sortedByDescending { it.thoiGian }
        danhGiaAdapter.updateDanhGia(danhGiaMoiNhat)
    }

    private fun sapXepTheoSoSaoTangDan() {
        val danhGiaSoSaoTangDan = danhGiaAdapter.getDanhSachDanhGia()
            .sortedBy { it.soSao }
        danhGiaAdapter.updateDanhGia(danhGiaSoSaoTangDan)
    }

    private fun sapXepTheoSoSaoGiamDan() {
        val danhGiaSoSaoGiamDan = danhGiaAdapter.getDanhSachDanhGia()
            .sortedByDescending { it.soSao }
        danhGiaAdapter.updateDanhGia(danhGiaSoSaoGiamDan)
    }
}