// ThemSPVaoGioHangActivity.kt
package com.example.projectdatmonan

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.projectdatmonan.Model.MonAn

class ThemSPVaoGioHangActivity : AppCompatActivity() {

    private lateinit var quantityTextView: TextView
    private var quantity = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.themspvaogiohang)

        quantityTextView = findViewById(R.id.soluong)
        val btnGiam: ImageButton = findViewById(R.id.btn_giam)
        val btnTang: ImageButton = findViewById(R.id.btn_tang)
        val btnAddToCart: Button = findViewById(R.id.btn_themVaoGioHang)
        val btnCancel: Button = findViewById(R.id.btn_huyBo)

        quantityTextView.text = quantity.toString()

        btnGiam.setOnClickListener {
            if (quantity > 1) {
                quantity--
                updateQuantityText()
            } else {
                Toast.makeText(this, "Số lượng không thể nhỏ hơn 1", Toast.LENGTH_SHORT).show()
            }
        }

        btnTang.setOnClickListener {
            quantity++
            updateQuantityText()
        }

        btnAddToCart.setOnClickListener {
            // Chỉ cho phép thêm một lần
            btnAddToCart.isEnabled = false

            // Lấy thông tin sản phẩm
            val productName = "Gà rang muối" // Bạn có thể lấy từ intent hoặc một nơi khác
            val productPrice = 76000.0 // Tương tự
            val productImageRes = R.drawable.testimage // Tương tự

            // Tạo đối tượng sản phẩm
            val monan = MonAn(productName, productPrice, quantity, productImageRes)

            // Khởi tạo Intent để mở GioHangActivity
            val intent = Intent(this, GioHangActivity::class.java).apply {
            }
            startActivity(intent)

            Toast.makeText(this, "Thêm $quantity sản phẩm vào giỏ hàng!", Toast.LENGTH_SHORT).show()
            finish() // Đóng activity sau khi thêm sản phẩm
        }

        btnCancel.setOnClickListener {
            finish() // Đóng activity
        }
    }

    private fun updateQuantityText() {
        quantityTextView.text = quantity.toString()
    }
}
