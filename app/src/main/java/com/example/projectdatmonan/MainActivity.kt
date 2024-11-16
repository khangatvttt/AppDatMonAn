package com.example.projectdatmonan

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav: BottomNavigationView = findViewById(R.id.bottom_navigation)
        val userId = intent.getStringExtra("USER_ID")


        // Đặt Fragment mặc định (ví dụ: HomeFragment)
        loadFragment(HomeFragment(),userId)

        bottomNav.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment(),userId)
                    true
                }
                R.id.nav_cart -> {
                    loadFragment(CartFragment(),userId)
                    true
                }
                R.id.nav_history -> {
                    loadFragment(HistoryFragment(),userId)
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(ProfileFragment(),userId)
                    true
                }
                else -> false
            }
        }
    }

    // Hàm để load Fragment
    private fun loadFragment(fragment: Fragment, userId: String?) {
        // Set arguments if userId is available
        if (userId != null) {
            val bundle = Bundle()
            bundle.putString("USER_ID", userId)
            fragment.arguments = bundle
        }

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.commit()
    }
}
