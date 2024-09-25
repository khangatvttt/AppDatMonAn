package com.example.projectdatmonan
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.projectdatmonan.Database.DBConnection
import com.example.projectdatmonan.Model.NguoiDung

class MainActivity : AppCompatActivity() {

    private lateinit var dbConnection: DBConnection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Khởi tạo DVConnection
        dbConnection = DBConnection()

        // Tạo một đối tượng NguoiDung
        val nguoiDung = NguoiDung(
            HoTen = "Nguyễn Văn B",
            DiaChi = "123 Đường ABC, Quận 1, TP.HCM",
            Email = "nguyenvana@example.com",
            Password = "password123",
            SDT = "0123456789",
            AvatarUrl = "http://example.com/avatar.jpg"
        )

        // Thêm người dùng vào Firebase
        dbConnection.addNguoiDung(nguoiDung) { success ->
            if (success) {
                showToast("Người dùng đã được thêm thành công!")
            } else {
                showToast("Lỗi khi thêm người dùng!")
            }
        }
    }

    // Hàm để hiển thị thông báo toast
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


}
