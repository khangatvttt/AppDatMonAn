package com.example.projectdatmonan

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : Activity() {

    private lateinit var emailEditText: EditText
    private lateinit var resetPasswordButton: Button
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quenmatkhau)

        // Khởi tạo FirebaseAuth
        mAuth = FirebaseAuth.getInstance()

        // Liên kết các View
        emailEditText = findViewById(R.id.emailEditText)
        resetPasswordButton = findViewById(R.id.resetPasswordButton)

        // Xử lý khi nhấn nút "Reset Password"
        resetPasswordButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()

            if (email.isNotEmpty()) {
                // Gửi yêu cầu reset mật khẩu
                mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                this@ForgotPasswordActivity,
                                "Yêu cầu đặt lại mật khẩu đã được gửi. Vui lòng kiểm tra email.",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Toast.makeText(
                                this@ForgotPasswordActivity,
                                "Đã xảy ra lỗi: ${task.exception?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            } else {
                emailEditText.error = "Vui lòng nhập email"
                emailEditText.requestFocus()
            }
        }
    }
}
