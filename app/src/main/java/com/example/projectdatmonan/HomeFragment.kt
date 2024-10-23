package com.example.projectdatmonan

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.projectdatmonan.Model.MonAn
import com.google.android.gms.tasks.Tasks
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.database.*

class HomeFragment : Fragment() {

    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storage: FirebaseStorage

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Khởi tạo Firebase Realtime Database và Storage
        database = FirebaseDatabase.getInstance()
        databaseReference = database.getReference("MonAn")
        storage = FirebaseStorage.getInstance()

        val myButton: Button = view.findViewById(R.id.myButton)

        myButton.setOnClickListener {
            // Truy xuất món ăn từ Firebase
            val maMonAn = "-O87Vq7lUk3RiBbWuRVR" // Mã món ăn cần lấy
            databaseReference.child(maMonAn)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // Chuyển dữ liệu thành model MonAn
                            val monAn = dataSnapshot.getValue(MonAn::class.java)

                            if (monAn != null) {
                                // Kiểm tra hình ảnh tồn tại
                                val hinhAnhList = monAn.hinhAnh ?: listOf()

                                if (hinhAnhList.isNotEmpty()) {
                                    val urlList = mutableListOf<String>() // Danh sách URL hình ảnh

                                    // Lặp qua tất cả các hình ảnh
                                    val tasks = hinhAnhList.map { hinhAnh ->
                                        val imageRef = storage.reference.child(hinhAnh)
                                        imageRef.downloadUrl.addOnSuccessListener { uri ->
                                            urlList.add(uri.toString()) // Thêm URL vào danh sách
                                        }.addOnFailureListener {
                                            Log.e("Firebase", "Failed to get image URL for $hinhAnh", it)
                                        }
                                    }

                                    // Đợi tất cả các tác vụ hoàn thành
                                    Tasks.whenAllComplete(tasks).addOnCompleteListener {
                                        // Chuyển dữ liệu qua `ChiTietMonAnActivity`
                                        val intent = Intent(activity, ChiTietMonAnActivity::class.java)
                                        intent.putExtra("tenMonAn", monAn.tenMonAn)
                                        intent.putExtra("gia", monAn.gia)
                                        intent.putExtra("moTa", monAn.moTaChiTiet)
                                        intent.putStringArrayListExtra("hinhAnhList", ArrayList(urlList)) // Truyền danh sách URL hình ảnh
                                        intent.putExtra("maMonAn", maMonAn) // Truyền mã món ăn qua Intent
                                        startActivity(intent)
                                    }
                                } else {
                                    Log.e("Firebase", "No images available")
                                }
                            }
                        } else {
                            Log.e("Firebase", "Document not found")
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.e("Firebase", "Error fetching data", databaseError.toException())
                    }
                })
        }

        return view
    }
}
