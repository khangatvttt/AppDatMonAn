package com.example.projectdatmonan
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Firebase Database instance and reference to 'message'
        val database = FirebaseDatabase.getInstance().getReference("message")

        // Set the value "Hello, World!" to the 'message' node in the database
        database.setValue("Hungf cho")
            .addOnSuccessListener {
                showToast("Message added successfully!")
            }
            .addOnFailureListener { exception ->
                showToast("Error adding message: ${exception.message}")
            }
    }

    // Extension function to show toast messages
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
