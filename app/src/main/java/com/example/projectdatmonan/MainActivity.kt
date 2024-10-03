package com.example.projectdatmonan
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectdatmonan.Database.CRUD_MonAn
import com.example.projectdatmonan.Database.DBConnection
import com.example.projectdatmonan.Model.MonAn
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.quan_ly_mon_an)

//        val button: Button = findViewById(R.id.buttonTest)

//        button.setOnClickListener {
//            val tv: TextView = findViewById(R.id.textViewShow)
//            val db: CRUD_MonAn = CRUD_MonAn()
//            val monAn = MonAn(
//                tenMonAn = "Súp cua",
//                gia = 30000.0,
//                trangThai = "Còn hàng",
//                moTaChiTiet = "Súp và cua",
//                trangThaiGiamGia = 32,
//                hinhAnh = listOf("pho_bo_1.jpg", "pho_bo_2.jpg"),
//                loaiMonAn = "-O86Xz9VZWTIVdVRcuyy"
//            )
//            Toast.makeText(this,"ok",Toast.LENGTH_SHORT).show()
//            db.getMonAnTheoLoai("-O86Xz9VZWTIVdVRcuyy"){list ->
//                if (list != null) {
//                    tv.setText(list.size.toString())
//                }
//            }
//        }

        loadFragment(FragmentQuanLyLoaiMonAn())
        bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.qlMonAn -> {
                    loadFragment(FragmentQuanLyLoaiMonAn())
                    true
                }

                R.id.ThongKe -> {
                    loadFragment(FragmentQuanLyLoaiMonAn())
                    true
                }

                R.id.DonHang -> {
                    loadFragment(FragmentQuanLyMonAn())
                    true
                }

                else -> {
                    loadFragment(FragmentQuanLyMonAn())
                    true
                }
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.commit()
    }
}












