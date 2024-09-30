package com.example.projectdatmonan

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

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

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Failed to load data: ${error.message}")
                progressBarFood.visibility = View.GONE
            }
        })
    }
    // Function to fetch all food items
    private fun fetchMonAnData() {
        progressBarPopular.visibility = View.VISIBLE
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                monAnList.clear()
                for (monAnSnapshot in snapshot.children) {
                    val monAn = monAnSnapshot.getValue(MonAn::class.java)
                    monAn?.let {
                        monAnList.add(it)
                        Log.d("MonAnData", "Món ăn: ${it.tenMonAn}, Giá: ${it.gia}, Trạng thái: ${it.trangThai}, Hình ảnh: ${it.hinhAnh}")
                    }
                }
                monAnAdapter.notifyDataSetChanged()
                progressBarPopular.visibility = View.GONE
            }

    // Hàm để load Fragment
    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.commit()
    }
}
