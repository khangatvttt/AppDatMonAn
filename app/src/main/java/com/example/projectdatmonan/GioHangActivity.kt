// GioHangActivity.kt
package com.example.projectdatmonan

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectdatmonan.Model.MonAn

class GioHangActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var cartAdapter: GioHangAdapter
    private var productList = mutableListOf<MonAn>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.giohang)

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Giả sử bạn đã thêm sản phẩm vào giỏ hàng trước đó, ở đây chỉ là ví dụ
        productList.add(MonAn("Gà rang muối", 76000.0, 1, R.drawable.testimage))

        cartAdapter = GioHangAdapter(productList)
        recyclerView.adapter = cartAdapter
    }
}
