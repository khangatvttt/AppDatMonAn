package com.example.projectdatmonan

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import cn.pedant.SweetAlert.SweetAlertDialog
import com.denzcoskun.imageslider.ImageSlider
import com.denzcoskun.imageslider.models.SlideModel
import com.example.projectdatmonan.Database.CRUD_LoaiMonAn
import com.example.projectdatmonan.Database.CRUD_MonAn
import com.example.projectdatmonan.Database.FirebaseStorageDB
import com.example.projectdatmonan.Model.LoaiMonAn
import com.example.projectdatmonan.Model.MonAn
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputEditText
import es.dmoral.toasty.Toasty


class ThemMonAnActivity : AppCompatActivity() {
    private var showImage: ImageSlider? = null
    private val defaultImageUrl = "https://firebasestorage.googleapis.com/v0/b/projectdatmonan.appspot.com/o/defaultMonAn.jpg?alt=media&token=aa495980-a7b4-4804-ab24-3313aca9df48"
    private var listUri: MutableList<Uri> = mutableListOf()
    private var mapMonAn:HashMap<String?, LoaiMonAn?>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        listUri = mutableListOf()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.them_mon_an)

        val dbLoaiMonAn = CRUD_LoaiMonAn()
        val slider = findViewById<Slider>(R.id.sliderGiamGia)
        val sliderLabel = findViewById<TextView>(R.id.textViewGiamGia)
        val giaMonAn = findViewById<TextInputEditText>(R.id.giaMonAnAdd)
        val comboboxLoaiMonAn = findViewById<AutoCompleteTextView>(R.id.loaiMonAnCombobox)
        val uploadImageButton = findViewById<ImageView>(R.id.uploadImage)
        val addMonAnButton = findViewById<Button>(R.id.themMonAn)
        val tenMonAn = findViewById<TextInputEditText>(R.id.tenMonAnAdd)
        val moTaMonAn = findViewById<TextInputEditText>(R.id.moTaMonAnAdd)
        val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)
        //btnLoading.startAnimation();
        showImage = findViewById(R.id.uploadAnh)
        giaMonAn.addPriceFormatting()


        // Set initial text based on the slider's current value
        sliderLabel.text = "${slider.value.toInt()}%"

        //Set label for percentage discount
        slider.addOnChangeListener { _, value, _ ->
            sliderLabel.text = "${value.toInt()}%"
        }

        //Set data for combobox LoaiMonAn
        dbLoaiMonAn.getAllLoaiLoaiMonAn { mapLoaiMapAn->
            val data: List<String?> = mapLoaiMapAn?.values
                ?.map { loaiMonAn -> loaiMonAn?.tenMonAn }
                ?.filter { it != "Tất cả món ăn" }
                ?: emptyList()
            val sorted = data.sortedBy { it }
            val loaiMonAn: ArrayAdapter<String?> =
                ArrayAdapter<String?>(this, R.layout.list_item, sorted)
            comboboxLoaiMonAn.setAdapter(loaiMonAn)
            val extras = intent.extras
            var loaiMonAnId = ""
            if (extras != null) {
                loaiMonAnId = extras.getString("loaiMonAnID").toString()
            }
            var loaiMonAnCbb = mapLoaiMapAn?.get(loaiMonAnId)?.tenMonAn
            if (loaiMonAnCbb == "Tất cả món ăn"){
                loaiMonAnCbb = sorted[0]
            }
            comboboxLoaiMonAn.setText(loaiMonAnCbb, false)
            mapMonAn = mapLoaiMapAn
        }

        //User upload images
        uploadImageButton.setOnClickListener {
            tenMonAn.clearFocus()
            moTaMonAn.clearFocus()
            giaMonAn.clearFocus()
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true) // Allow multiple image selection
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), 200)
        }

        //Default image
        val imageList = ArrayList<SlideModel>()
        imageList.add(SlideModel(defaultImageUrl))
        showImage!!.setImageList(imageList)

        //Add MonAn
        addMonAnButton.setOnClickListener {
            if (giaMonAn.text.toString()=="" || tenMonAn.text.toString()=="" || moTaMonAn.text.toString()==""){
                Toasty.error(this,"Vui lòng điền đầy đủ thông tin của món ăn").show()
            }
            else {
                val tenMonAn = tenMonAn.text.toString()
                val mota = moTaMonAn.text.toString()
                val gia = giaMonAn.text.toString()
                val giaFormatted: Double = gia.replace(" ","").toDouble()

                // Lấy tình trạng
                val selectedId: Int = radioGroup.checkedRadioButtonId
                val selectedRadioButton: RadioButton? = findViewById(selectedId)
                val tinhTrang: String = selectedRadioButton?.text.toString()

                val giamGia = sliderLabel.text.toString().replace("%","").toInt()
                val loaiMonAn = comboboxLoaiMonAn.text.toString()
                val keyLoaiMonAn = mapMonAn?.filterValues { it?.tenMonAn == loaiMonAn }?.keys?.first()
                val listOfImage = mutableListOf<String>()

                val firebaseStorage = FirebaseStorageDB()
                if (listUri.size==0){
                    SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Xác nhận")
                        .setContentText("Bạn chưa chọn ảnh cho món ăn, ảnh mặc định sẽ được chọn. Bạn có muốn tiếp tục?")
                        .setCancelText("Hủy")
                        .setConfirmText("OK")
                        .showCancelButton(true)
                        .setConfirmClickListener { sDialog ->
                            val loadingDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
                            loadingDialog.setTitleText("Đang thêm món ăn...")
                            loadingDialog.setCancelable(false)
                            loadingDialog.show()
                            val monAnAdd = MonAn(tenMonAn,giaFormatted, tinhTrang, mota, giamGia, listOf(defaultImageUrl) , keyLoaiMonAn)
                            val dbMonAn = CRUD_MonAn()
                            dbMonAn.addMonAn(monAnAdd){success ->
                                if (success){
                                    loadingDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
                                    loadingDialog.setTitleText("Thành công")
                                    loadingDialog.setContentText("Thêm món ăn thành công")
                                    loadingDialog.setConfirmText("OK")
                                    loadingDialog.setConfirmClickListener { dialog ->
                                        dialog.dismissWithAnimation()
                                    }
                                }
                                else {
                                    loadingDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE)
                                    loadingDialog.setTitleText("Thêm thất bại")
                                    loadingDialog.setContentText("Thêm món ăn thất bại. Vui lòng thử lại sau")
                                    loadingDialog.setConfirmText("OK")
                                    loadingDialog.setConfirmClickListener { failDialog ->
                                        failDialog.dismissWithAnimation()
                                    }
                                }
                            }
                            sDialog.dismiss()
                        }
                        .setCancelClickListener { sDialog ->
                            sDialog.cancel()
                        }
                        .show()
                }
                else {
                    val loadingDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
                    loadingDialog.setTitleText("Đang thêm món ăn...")
                    loadingDialog.setCancelable(false)
                    loadingDialog.show()
                    var total = 0
                    var success = 0
                    for (uri in listUri){
                        firebaseStorage.uploadImageToFirebase(uri){linkImage->
                            if (linkImage!=null) {
                                listOfImage.add((linkImage))
                                success++
                            }
                            total++
                            if (total==listUri.size){ // Upload finished
                                if (success!=total) { // Upload fail
                                    loadingDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE)
                                    loadingDialog.setTitleText("Tải ảnh lên thất bại")
                                    loadingDialog.setContentText("Đã có lỗi xảy ra khi tải ảnh lên. Vui lòng thử lại sau.")
                                    loadingDialog.setConfirmText("OK")
                                    loadingDialog.setConfirmClickListener { failDialog ->
                                        failDialog.dismissWithAnimation()
                                    }
                                }
                                else{ // Upload successfully
                                    val monAnAdd = MonAn(tenMonAn,giaFormatted, tinhTrang, mota, giamGia, listOfImage, keyLoaiMonAn)
                                    val dbMonAn = CRUD_MonAn()
                                    dbMonAn.addMonAn(monAnAdd){success ->
                                        if (success){
                                            loadingDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
                                            loadingDialog.setTitleText("Thành công")
                                            loadingDialog.setContentText("Thêm món ăn thành công")
                                            loadingDialog.setConfirmText("OK")
                                            loadingDialog.setConfirmClickListener { dialog ->
                                                dialog.dismissWithAnimation()
                                            }
                                        }
                                        else {
                                            loadingDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE)
                                            loadingDialog.setTitleText("Thêm thất bại")
                                            loadingDialog.setContentText("Thêm món ăn thất bại. Vui lòng thử lại sau")
                                            loadingDialog.setConfirmText("OK")
                                            loadingDialog.setConfirmClickListener { failDialog ->
                                                failDialog.dismissWithAnimation()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }




    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        listUri = mutableListOf()
        if (requestCode == 200 && resultCode == Activity.RESULT_OK && data != null) {
            val imageList = ArrayList<SlideModel>()

            // Check if multiple images were selected
            if (data.clipData != null) {
                val count = data.clipData!!.itemCount
                for (i in 0 until count) {
                    val imageUri = data.clipData!!.getItemAt(i).uri
                    // Convert the URI to a string or a resource that ImageSlider accepts
                    imageList.add(SlideModel(imageUri.toString())) // Add image to list
                    listUri.add(imageUri)
                }
            } else if (data.data != null) {
                // Single image selected
                val imageUri = data.data
                imageList.add(SlideModel(imageUri.toString())) // Add image to list
                if (imageUri != null) {
                    listUri?.add(imageUri)
                }
            }

            // Set images to ImageSlider
            showImage?.setImageList(imageList)
        }
    }


    fun TextInputEditText.addPriceFormatting() {
        this.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false

            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return
                isUpdating = true

                // Remove spaces from the original input
                val originalString = s?.toString()?.replace(" ", "") ?: ""
                if (originalString.isNotEmpty()) {
                    // Split the string into integer and decimal parts
                    val parts = originalString.split(".")
                    val integerPart = parts[0]
                    val decimalPart = if (parts.size > 1) parts[1] else null

                    // Format the integer part
                    val formattedIntegerPart = integerPart.reversed()
                        .chunked(3)
                        .joinToString(" ")
                        .reversed()

                    // Rebuild the formatted string with the decimal part if it exists
                    val formattedString = if (decimalPart != null) {
                        "$formattedIntegerPart.$decimalPart"
                    } else {
                        formattedIntegerPart
                    }

                    // Set the formatted text and move the cursor to the correct position
                    this@addPriceFormatting.setText(formattedString)
                    this@addPriceFormatting.setSelection(formattedString.length)
                }

                isUpdating = false
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

}