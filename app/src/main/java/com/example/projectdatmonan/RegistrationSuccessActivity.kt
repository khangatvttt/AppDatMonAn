package com.example.projectdatmonan

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button

class RegistrationSuccessActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration_success)

        // Khởi tạo nút
        val continueButton: Button = findViewById(R.id.continue_button)

        // Thiết lập listener cho nút
        continueButton.setOnClickListener {
            // Chuyển tới trang đăng nhập
            val intent = Intent(this@RegistrationSuccessActivity, LoginActivity::class.java)
            startActivity(intent)
            finish() // Đóng activity hiện tại để không quay lại trang này
        }
    }
}
