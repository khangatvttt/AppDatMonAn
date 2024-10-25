package com.example.projectdatmonan

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectdatdatHang.Database.CRUD_DatHang
import com.example.projectdatmonan.Database.CRUD_DanhGia
import com.example.projectdatmonan.Database.CRUD_MonAn
import com.example.projectdatmonan.Database.DBConnection
import com.example.projectdatmonan.Model.MonAn
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DatabaseReference

class MainActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference

    lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.quan_ly_mon_an)



        loadFragment(FragmentQuanLyLoaiMonAn())
        bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.qlMonAn -> {
                    loadFragment(FragmentQuanLyLoaiMonAn())
                    true
                }

                R.id.ThongKe -> {
                    loadFragment(FragmentThongKe())
                    true
                }

                R.id.DonHang -> {
                    loadFragment(FragmentQuanLyMonAn())
                    true
                }

                R.id.Feedback -> {
                    loadFragment(FragmentXemDanhGia())
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












