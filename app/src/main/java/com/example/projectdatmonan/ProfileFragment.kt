package com.example.projectdatmonan

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide

import com.example.projectdatmonan.Database.CRUD_NguoiDung
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ProfileFragment : Fragment() {

    private lateinit var fullNameTextView: TextView
    private lateinit var phoneTextView: TextView
    private lateinit var addressTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var editProfileButton: Button

    private lateinit var fullNameEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var addressEditText: EditText
    private lateinit var avatarImageView: ImageView
    private lateinit var changePasswordButton: Button
    private lateinit var logoutButton: Button

    private var isEditing = false


    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private val storageReference = FirebaseStorage.getInstance().reference
    private val databaseReference = FirebaseDatabase.getInstance().reference.child("NguoiDung")

    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        fullNameTextView = view.findViewById(R.id.fullNameTextView)
        phoneTextView = view.findViewById(R.id.phoneTextView)
        addressTextView = view.findViewById(R.id.addressTextView)
        emailTextView = view.findViewById(R.id.emailTextView)
        editProfileButton = view.findViewById(R.id.editProfileButton)

        // Gán các EditText (ban đầu ẩn đi)
        fullNameEditText = view.findViewById(R.id.fullNameEditText)
        phoneEditText = view.findViewById(R.id.phoneEditText)
        addressEditText = view.findViewById(R.id.addressEditText)

        // Thiết lập trạng thái ban đầu
        setEditMode(false)
        // Firebase instances
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Lấy thông tin người dùng từ Firebase
        loadUserProfile()
        // Tìm các view
        avatarImageView = view.findViewById(R.id.avatarImageView)

        // Xử lý sự kiện bấm vào avatar
        avatarImageView.setOnClickListener {
            openFileChooser()
        }


        // Xử lý sự kiện khi nhấn nút "Edit Profile"
        editProfileButton.setOnClickListener {
            if (isEditing) {
                // Nếu đang ở chế độ chỉnh sửa -> lưu và thoát khỏi chế độ chỉnh sửa
                saveProfileChanges()
                setEditMode(false)
            } else {
                // Nếu không ở chế độ chỉnh sửa -> chuyển sang chế độ chỉnh sửa
                setEditMode(true)
            }
        }

        changePasswordButton = view.findViewById(R.id.changePasswordButton)

        // Set click listener for Change Password button
        changePasswordButton.setOnClickListener {
            showChangePasswordDialog()
        }

        logoutButton = view.findViewById(R.id.logoutButton)

        // Set click listener for Log Out button
        logoutButton.setOnClickListener {
            logout()
        }

        return view
    }
    private fun logout() {
        // Sign out from Firebase
        firebaseAuth.signOut()

        // Redirect to LoginActivity
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Clear activity stack
        startActivity(intent)

        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
    }
    private fun showChangePasswordDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_change_password, null)
        builder.setView(dialogView)

        val oldPasswordInput = dialogView.findViewById<EditText>(R.id.oldPasswordInut)
        val newPasswordInput = dialogView.findViewById<EditText>(R.id.newPasswordInut)
        val confirmNewPasswordInput = dialogView.findViewById<EditText>(R.id.confirmNewPasswordInut)

        builder.setTitle("Change Password")
        builder.setPositiveButton("Confirm") { dialog, _ ->
            val oldPassword = oldPasswordInput.text.toString()
            val newPassword = newPasswordInput.text.toString()
            val confirmPassword = confirmNewPasswordInput.text.toString()

            if (newPassword != confirmPassword) {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
            } else {
                val user = firebaseAuth.currentUser
                if (user != null && !oldPassword.isEmpty()) {
                    // Reauthenticate the user before updating password
                    val credential = EmailAuthProvider.getCredential(user.email!!, oldPassword)
                    user.reauthenticate(credential)
                        .addOnCompleteListener { reauthTask ->
                            if (reauthTask.isSuccessful) {
                                // Update password
                                user.updatePassword(newPassword)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            Toast.makeText(requireContext(), "Password updated successfully", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(requireContext(), "Failed to update password", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            } else {
                                Toast.makeText(requireContext(), "Bạn nhập sai mật khẩu cũ", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        builder.create().show()
    }
    private fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }
    // Xử lý kết quả khi người dùng chọn ảnh
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            avatarImageView.setImageURI(imageUri) // Hiển thị ảnh tạm thời trên ImageView

            // Tải ảnh lên Firebase Storage
            uploadImageToFirebase()
        }
    }

    // Tải ảnh lên Firebase Storage và lưu URL vào Realtime Database
    private fun uploadImageToFirebase() {
        val user = firebaseAuth.currentUser
        if (user != null && imageUri != null) {
            val email = user.email // Lấy email từ FirebaseAuth
            if (email != null) {
                // Sử dụng hàm getUserByEmail từ class CRUD_NguoiDung để lấy thông tin người dùng
                CRUD_NguoiDung().getUserByEmailSnap(email) { userSnapshot ->
                    if (userSnapshot != null) {
                        val userId = userSnapshot.key // Lấy userId từ snapshot key

                        if (userId != null) {
                            // Đảm bảo đúng đường dẫn đến userId mà Firebase đã tạo sẵn
                            val fileReference = storageReference.child("avatars/${userId}.jpg")

                            // Thực hiện upload ảnh lên Firebase Storage
                            fileReference.putFile(imageUri!!)
                                .addOnSuccessListener {
                                    // Khi upload thành công, lấy URL tải xuống từ Firebase Storage
                                    fileReference.downloadUrl.addOnSuccessListener { uri ->
                                        val avatarUrl = uri.toString()

                                        // Cập nhật URL avatar trong Firebase Realtime Database đúng nhánh
                                        databaseReference.child(userId).child("avatarUrl").setValue(avatarUrl)
                                            .addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    Toast.makeText(context, "Avatar updated successfully!", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(context, "Failed to update avatar.", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                    }
                                }
                                .addOnFailureListener { e ->
                                    // Xử lý khi upload thất bại
                                    Toast.makeText(context, "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(context, "User ID not found.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // Không tìm thấy người dùng có email tương ứng
                        Toast.makeText(context, "User not found with email: $email", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                // Trường hợp email null
                Toast.makeText(context, "Email is null.", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Trường hợp user null hoặc không có imageUri
            Toast.makeText(context, "User is not logged in or imageUri is null.", Toast.LENGTH_SHORT).show()
        }
    }











    // Tải và hiển thị avatar từ Firebase Storage

    private fun loadUserProfile() {
        val user = firebaseAuth.currentUser
        user?.let {
            // Hiển thị email từ Firebase Authentication
            emailTextView.text = user.email

            // Lấy thông tin từ CRUD_NguoiDung bằng email
            val userEmail = user.email ?: return

            // Sử dụng hàm trong CRUD_NguoiDung để lấy thông tin người dùng
            val crudNguoiDung = CRUD_NguoiDung()
            crudNguoiDung.getUserByEmail(userEmail) { nguoiDung ->
                if (nguoiDung != null) {
                    // Hiển thị tên, số điện thoại, địa chỉ
                    fullNameTextView.text = nguoiDung.hoTen
                    phoneTextView.text = nguoiDung.sdt
                    addressTextView.text = nguoiDung.diaChi

                    // Hiển thị avatar từ URL
                    if (nguoiDung.avatarUrl != null && nguoiDung.avatarUrl.isNotEmpty()) {
                        // Sử dụng thư viện Glide để load ảnh từ avatarUrl
                        Glide.with(this)
                            .load(nguoiDung.avatarUrl)
                            .placeholder(R.drawable.ic_dangload) // Hình ảnh hiển thị khi đang tải
                            .error(R.drawable.ic_avatar) // Hình ảnh hiển thị khi có lỗi
                            .into(avatarImageView) // Thay avatarImageView bằng ImageView của bạn
                    }
                } else {
                    // Xử lý khi không tìm thấy người dùng
                    Toast.makeText(context, "User not found", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun setEditMode(editMode: Boolean) {
        isEditing = editMode

        if (editMode) {
            // Hiển thị EditText và ẩn TextView để cho phép chỉnh sửa
            fullNameEditText.visibility = View.VISIBLE
            phoneEditText.visibility = View.VISIBLE
            addressEditText.visibility = View.VISIBLE

            fullNameTextView.visibility = View.GONE
            phoneTextView.visibility = View.GONE
            addressTextView.visibility = View.GONE

            // Hiển thị thông tin hiện tại trong EditText để chỉnh sửa
            fullNameEditText.setText(fullNameTextView.text)
            phoneEditText.setText(phoneTextView.text)
            addressEditText.setText(addressTextView.text)

            editProfileButton.text = "Lưu"
        } else {
            // Hiển thị TextView và ẩn EditText khi hoàn thành chỉnh sửa
            fullNameEditText.visibility = View.GONE
            phoneEditText.visibility = View.GONE
            addressEditText.visibility = View.GONE

            fullNameTextView.visibility = View.VISIBLE
            phoneTextView.visibility = View.VISIBLE
            addressTextView.visibility = View.VISIBLE

            editProfileButton.text = "Chỉnh sửa thông tin cá nhân"
        }
    }

    private fun saveProfileChanges() {

        val user = firebaseAuth.currentUser

        user?.let {
            val email = user.email ?: return

            // Cập nhật các thông tin từ EditText
            val fullName = fullNameEditText.text.toString()
            val phone = phoneEditText.text.toString()
            val address = addressEditText.text.toString()

            // Kiểm tra số điện thoại phải đủ 10 số và chỉ chứa các chữ số
            if (phone.length != 10 || !phone.matches(Regex("\\d{10}"))) {
                Toast.makeText(context, "VUI LÒNG NHẬP ĐÚNG ĐỊNH DẠNG SỐ ĐIỆN THOẠI!", Toast.LENGTH_SHORT).show()
                return
            }
            // Cập nhật các TextView với giá trị từ EditText
            fullNameTextView.text = fullNameEditText.text.toString()
            phoneTextView.text = phoneEditText.text.toString()
            addressTextView.text = addressEditText.text.toString()

            // Sử dụng hàm getUserByEmail để lấy thông tin người dùng và cập nhật
            val crudNguoiDung = CRUD_NguoiDung()
            crudNguoiDung.getUserByEmailSnap(email) { nguoiDung ->
                nguoiDung?.let {
                    // Cập nhật thông tin trong Firebase Realtime Database
                    val userId = it.key // Lấy userId từ đối tượng NguoiDung
                    databaseReference.child(userId.toString()).child("hoTen").setValue(fullName)
                    databaseReference.child(userId.toString()).child("sdt").setValue(phone)
                    databaseReference.child(userId.toString()).child("diaChi").setValue(address)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(context, "CẬP NHẬT THÔNG TIN CÁ NHÂN THÀNH CÔNG!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Failed to update profile.", Toast.LENGTH_SHORT).show()
                            }
                        }
                } ?: run {
                    // Xử lý khi không tìm thấy người dùng
                    Toast.makeText(context, "User not found.", Toast.LENGTH_SHORT).show()
                }
            }
        } ?: run {
            // Trường hợp người dùng không đăng nhập
            Toast.makeText(context, "User is not logged in.", Toast.LENGTH_SHORT).show()
        }
    }


}
