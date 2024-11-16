package com.example.projectdatmonan

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DatabaseReference

class MainActivityAdmin : AppCompatActivity() {
    private lateinit var database: DatabaseReference

    lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.quan_ly_mon_an)



        loadFragment(DuyetDonFragment())
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
                    loadFragment(DuyetDonFragment())
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












