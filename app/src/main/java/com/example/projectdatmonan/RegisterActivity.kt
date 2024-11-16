package com.example.projectdatmonan

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.projectdatmonan.Database.CRUD_NguoiDung
import com.example.projectdatmonan.Model.NguoiDung
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class RegisterActivity : Activity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var fullNameEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var addressEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var emailErrorText: TextView
    private lateinit var passwordErrorText: TextView
    private lateinit var fullNameErrorText: TextView
    private lateinit var phoneErrorText: TextView
    private lateinit var addressErrorText: TextView
    private lateinit var mAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    private lateinit var handler: Handler
    private lateinit var checkEmailRunnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register) // Liên kết với giao diện đăng ký

        // Khởi tạo các View
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        fullNameEditText = findViewById(R.id.fullNameEditText)
        phoneEditText = findViewById(R.id.phoneEditText)
        addressEditText = findViewById(R.id.addressEditText)
        registerButton = findViewById(R.id.registerButton)

        // Khởi tạo TextView lỗi
        emailErrorText = findViewById(R.id.emailErrorText)
        passwordErrorText = findViewById(R.id.passwordErrorText)
        fullNameErrorText = findViewById(R.id.fullNameErrorText)
        phoneErrorText = findViewById(R.id.phoneErrorText)
        addressErrorText = findViewById(R.id.addressErrorText)

        // Khởi tạo FirebaseAuth
        mAuth = FirebaseAuth.getInstance()

        // Khởi tạo ProgressDialog
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Đang đăng ký...")
        progressDialog.setCancelable(false)

        // Đăng xuất người dùng nếu có
        mAuth.signOut()

        // Xử lý sự kiện khi nhấn nút "Đăng ký"
        registerButton.setOnClickListener {
            clearErrorMessages()

            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val fullName = fullNameEditText.text.toString().trim()
            val phone = phoneEditText.text.toString().trim()
            val address = addressEditText.text.toString().trim()

            // Kiểm tra tính hợp lệ của các trường nhập
            var isValid = true

            if (TextUtils.isEmpty(fullName)) {
                fullNameErrorText.text = "Vui lòng nhập họ tên"
                isValid = false
            }
            if (phone.length != 10) {
                phoneErrorText.text = "Số điện thoại phải là 10 chữ số"
                isValid = false
            }

            if (TextUtils.isEmpty(email)) {
                emailErrorText.text = "Vui lòng nhập email"
                isValid = false
            }
            if (TextUtils.isEmpty(password)) {
                passwordErrorText.text = "Vui lòng nhập mật khẩu"
                isValid = false
            } else if (password.length < 6) {
                passwordErrorText.text = "Mật khẩu phải có ít nhất 6 ký tự"
                isValid = false
            }
            if (TextUtils.isEmpty(address)) {
                addressErrorText.text = "Vui lòng nhập địa chỉ"
                isValid = false
            }

            // Nếu các trường hợp hợp lệ thì thực hiện đăng ký
            if (isValid) {
                progressDialog.show()

                mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        progressDialog.dismiss()

                        if (task.isSuccessful) {
                            val user = mAuth.currentUser
                            user?.let {
                                it.sendEmailVerification()
                                    .addOnCompleteListener { verificationTask ->
                                        if (verificationTask.isSuccessful) {
                                            Toast.makeText(this@RegisterActivity, "Đăng ký thành công. Vui lòng kiểm tra email để xác thực.", Toast.LENGTH_LONG).show()
                                            startEmailVerificationCheck()
                                        } else {
                                            emailErrorText.text = "Gửi email xác thực thất bại: ${verificationTask.exception?.message}"
                                        }
                                    }
                            }
                        } else {
                            val errorMessage = task.exception?.message ?: "Đăng ký thất bại"
                            if (errorMessage.contains("already in use")) {
                                emailErrorText.text = "Email đã được sử dụng"
                            } else {
                                Toast.makeText(this@RegisterActivity, "Đăng ký thất bại: $errorMessage", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
            }
        }
    }

    // Hàm xóa thông báo lỗi cũ
    private fun clearErrorMessages() {
        emailErrorText.text = ""
        passwordErrorText.text = ""
        fullNameErrorText.text = ""
        phoneErrorText.text = ""
        addressErrorText.text = ""
    }

    // Hàm bắt đầu kiểm tra xác thực email
    private fun startEmailVerificationCheck() {
        handler = Handler()
        checkEmailRunnable = object : Runnable {
            override fun run() {
                checkEmailVerification()
                handler.postDelayed(this, 3000) // Kiểm tra mỗi 3 giây
            }
        }
        handler.post(checkEmailRunnable)
    }

    // Hàm kiểm tra xem email đã được xác thực chưa
    private fun checkEmailVerification() {
        val user = FirebaseAuth.getInstance().currentUser

        user?.let {
            it.reload()
            if (it.isEmailVerified) {
                handler.removeCallbacks(checkEmailRunnable)

                // Tạo đối tượng NguoiDung
                val nguoiDung = NguoiDung().apply {
                    avatarUrl = "avartamacdinh.jpg"
                    email = it.email.toString()
                    hoTen = fullNameEditText.text.toString().trim()
                    sdt = phoneEditText.text.toString().trim()
                    diaChi = addressEditText.text.toString().trim()
                    role = "KH"
                }

                // Tạo đối tượng CRUD_NguoiDung
                val crudNguoiDung = CRUD_NguoiDung()

                // Thêm người dùng vào Firebase
                crudNguoiDung.addNguoiDung(nguoiDung) { success ->
                    if (success) {
                        val intent = Intent(this@RegisterActivity, RegistrationSuccessActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@RegisterActivity, "Thêm người dùng thất bại", Toast.LENGTH_SHORT).show()
                    }
                    null
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(checkEmailRunnable)
    }
}
