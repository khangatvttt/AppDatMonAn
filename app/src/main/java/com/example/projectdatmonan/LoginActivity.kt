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



}
