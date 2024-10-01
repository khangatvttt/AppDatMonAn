package com.example.projectdatmonan
<<<<<<< HEAD
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialog


=======

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
>>>>>>> origin/TranVanHung

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

<<<<<<< HEAD
        // Find the button by ID
        val btnOpenGioHang: Button = findViewById(R.id.my_button)

        // Set click listener on the button
        btnOpenGioHang.setOnClickListener {
            // Gọi hàm hiển thị BottomSheetDialog
            showBottomSheetDialog()
        }
    }

    // Hàm hiển thị BottomSheetDialog với layout them_sp_vao_gio_hang
    private fun showBottomSheetDialog() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val dialogView = layoutInflater.inflate(R.layout.themspvaogiohang, null)

        // Khởi tạo các thành phần trong BottomSheetDialog
        val quantityTextView: TextView = dialogView.findViewById(R.id.soluong)
        val btnGiam: ImageButton = dialogView.findViewById(R.id.btn_giam)
        val btnTang: ImageButton = dialogView.findViewById(R.id.btn_tang)
        val btnAddToCart: Button = dialogView.findViewById(R.id.btn_themVaoGioHang)
        val btnCancel: Button = dialogView.findViewById(R.id.btn_huyBo)

        // Khởi tạo biến số lượng
        var quantity = 1
        quantityTextView.text = quantity.toString()

        // Thiết lập sự kiện click cho nút giảm số lượng
        btnGiam.setOnClickListener {
            if (quantity > 1) {
                quantity--
                quantityTextView.text = quantity.toString()
            }
        }

        // Thiết lập sự kiện click cho nút tăng số lượng
        btnTang.setOnClickListener {
            quantity++
            quantityTextView.text = quantity.toString()
        }

        // Thiết lập sự kiện click cho nút thêm vào giỏ hàng
        btnAddToCart.setOnClickListener {
            // Xử lý thêm vào giỏ hàng
            // Ở đây bạn có thể gọi hàm thêm vào giỏ hàng
            // hoặc sử dụng Intent để chuyển sang Activity khác
            Toast.makeText(this, "Thêm $quantity sản phẩm vào giỏ hàng!", Toast.LENGTH_SHORT).show()
            bottomSheetDialog.dismiss() // Đóng dialog sau khi thêm vào giỏ hàng
        }

        // Thiết lập sự kiện click cho nút hủy
        btnCancel.setOnClickListener {
            bottomSheetDialog.dismiss() // Đóng dialog khi hủy
        }

        // Đặt layout vào BottomSheetDialog
        bottomSheetDialog.setContentView(dialogView)
        bottomSheetDialog.show()
=======
        val bottomNav: BottomNavigationView = findViewById(R.id.bottom_navigation)

        // Đặt Fragment mặc định (ví dụ: HomeFragment)
        loadFragment(HomeFragment())

        bottomNav.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_cart -> {
                    loadFragment(CartFragment())
                    true
                }
                R.id.nav_history -> {
                    loadFragment(HistoryFragment())
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    // Hàm để load Fragment
    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.commit()
>>>>>>> origin/TranVanHung
    }
}


//    private lateinit var dbConnection: DBConnection
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        // Khởi tạo DVConnection
//        dbConnection = DBConnection()
//
//        // Tạo một đối tượng NguoiDung
//        val nguoiDung = NguoiDung(
//            HoTen = "Nguyễn Văn B",
//            DiaChi = "123 Đường ABC, Quận 1, TP.HCM",
//            Email = "nguyenvana@example.com",
//            Password = "password123",
//            SDT = "0123456789",
//            AvatarUrl = "http://example.com/avatar.jpg"
//        )
//
//        // Thêm người dùng vào Firebase
//        dbConnection.addNguoiDung(nguoiDung) { success ->
//            if (success) {
//                showToast("Người dùng đã được thêm thành công!")
//            } else {
//                showToast("Lỗi khi thêm người dùng!")
//            }
//        }
//    }
//
//
//    // Hàm để hiển thị thông báo toast
//    private fun showToast(message: String) {
//        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
//    }
