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


}
