package com.example.projectdatmonan

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.projectdatmonan.Database.CRUD_NguoiDung
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class LoginActivity : Activity() {

    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerTextView: TextView
    private lateinit var forgetPassTextView: TextView
    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Khởi tạo FirebaseAuth và DatabaseReference
        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("NguoiDung")

        // Liên kết các View
        usernameEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        registerTextView = findViewById(R.id.registerTextView)
        forgetPassTextView = findViewById(R.id.forgetPassTextView)

        // Xử lý khi nhấn nút đăng nhập
        loginButton.setOnClickListener {
            val email = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            }
        }

        // Xử lý khi nhấn vào TextView đăng ký
        registerTextView.setOnClickListener {
            val registerIntent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(registerIntent)
        }

        // Xử lý khi nhấn vào TextView quên mật khẩu
        forgetPassTextView.setOnClickListener {
            val forgetPassIntent = Intent(this@LoginActivity, ForgotPasswordActivity::class.java)
            startActivity(forgetPassIntent)
        }
    }

    // Hàm đăng nhập sử dụng Firebase Authentication
    private fun loginUser(email: String, password: String) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Đăng nhập thành công, kiểm tra vai trò của người dùng theo email
                    checkUserRole(email)
                } else {
                    // Đăng nhập thất bại
                    Toast.makeText(this@LoginActivity, "BẠN NHẬP SAI TÀI KHOẢN HOẶC MẬT KHẨU",Toast.LENGTH_SHORT).show()
                }
            }
    }


    // Hàm kiểm tra vai trò của người dùng từ Firebase Realtime Database
    private fun checkUserRole(userId: String) {
        val crudNguoiDung = CRUD_NguoiDung()

        crudNguoiDung.checkRoleByEmail(userId) { isKH ->
            if (isKH) {
                // Chuyển tới MainActivity nếu role là "KH"
                val mainIntent = Intent(this@LoginActivity, MainActivity::class.java)
                startActivity(mainIntent)
                finish()
            } else {
                // Hiển thị thông báo nếu không phải "KH"
                Toast.makeText(this@LoginActivity, "Bạn không có quyền truy cập.", Toast.LENGTH_SHORT).show()
            }
        }
    }


}