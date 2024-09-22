package com.example.projectdatmonan

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    // Khởi tạo instance của Firestore
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Dữ liệu mẫu
        val user = hashMapOf(
            "name" to "John Doe",
            "address" to "123 Main St"
        )

        // Thêm dữ liệu vào collection "users"
        db.collection("users")
            .add(user)
            .addOnSuccessListener { documentReference ->
                // In ra log khi thêm thành công
                Log.d("Firestore", "DocumentSnapshot added with ID: ${documentReference.id}")

                // Hiển thị Toast thông báo thành công
                Toast.makeText(this, "Document added with ID: ${documentReference.id}", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                // In ra log khi thêm thất bại
                Log.w("Firestore", "Error adding document", e)

                // Hiển thị Toast thông báo lỗi
                Toast.makeText(this, "Error adding document: $e", Toast.LENGTH_SHORT).show()
            }
    }
}
