package com.example.projectdatmonan

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.projectdatmonan.Database.CRUD_GioHang
import com.example.projectdatmonan.Database.CRUD_LoaiMonAn
import com.example.projectdatmonan.Model.GioHang
import com.example.projectdatmonan.Model.ListMonAn
import com.example.projectdatmonan.Model.LoaiMonAn
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
    }
}