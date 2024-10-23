package com.example.projectdatmonan

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.denzcoskun.imageslider.interfaces.ItemClickListener
import com.denzcoskun.imageslider.models.SlideModel
import com.example.projectdatLoaiMonAn.Database.CRUD_LoaiLoaiMonAn
import com.example.projectdatmonan.Database.CRUD_MonAn
import com.example.projectdatmonan.Database.FirebaseStorageDB
import com.example.projectdatmonan.Model.LoaiMonAn
import com.example.projectdatmonan.Model.MonAn
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputEditText
import es.dmoral.toasty.Toasty


class SuaMonAnActivity : AppCompatActivity() {
    private lateinit var showImage: ImageSlider
    private val defaultImageUrl = "https://firebasestorage.googleapis.com/v0/b/projectdatmonan.appspot.com/o/defaultMonAn.jpg?alt=media&token=aa495980-a7b4-4804-ab24-3313aca9df48"
    private var listUri: MutableList<Uri> = mutableListOf()
    private var listOldImage: MutableList<String> = mutableListOf()
    private var mapMonAn:HashMap<String?, LoaiMonAn?>? = null
    private var monAn:MonAn? = null
    var imageList = ArrayList<SlideModel>()
    var listImageData = ArrayList<String>()
    private var anhNoiBat:Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        listUri = mutableListOf()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sua_mon_an)

        val dbLoaiMonAn = CRUD_LoaiLoaiMonAn()
        val slider = findViewById<Slider>(R.id.sliderGiamGia)
        val sliderLabel = findViewById<TextView>(R.id.textViewGiamGia)
        val giaMonAn = findViewById<TextInputEditText>(R.id.giaMonAnAdd)
        val comboboxLoaiMonAn = findViewById<AutoCompleteTextView>(R.id.loaiMonAnCombobox)
        val uploadImageButton = findViewById<ImageView>(R.id.uploadImage)
        val addMonAnButton = findViewById<Button>(R.id.themMonAn)
        val tenMonAn = findViewById<TextInputEditText>(R.id.tenMonAnAdd)
        val moTaMonAn = findViewById<TextInputEditText>(R.id.moTaMonAnAdd)
        val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)
        val radioConMon = findViewById<RadioButton>(R.id.radioConMon)
        val radioSapHet = findViewById<RadioButton>(R.id.radioSapHet)
        val radioHetMon = findViewById<RadioButton>(R.id.radioHetMon)

        showImage = findViewById(R.id.uploadAnh)
        giaMonAn.addPriceFormatting()

        val db = CRUD_MonAn()
        val keyMonAn = intent.getStringExtra("MonAnID")
        if (keyMonAn != null) {
            db.getMonAn(keyMonAn) { monAndata ->
                if (monAndata != null) {
                    monAn = monAndata
                    tenMonAn.setText(monAndata.tenMonAn)
                    moTaMonAn.setText(monAndata.moTaChiTiet)
                    giaMonAn.setText(monAndata.gia.toString())
                    if (monAndata.trangThai == "Còn món") {
                        radioConMon.isChecked = true
                    } else if (monAndata.trangThai == "Hết món") {
                        radioHetMon.isChecked = true
                    } else {
                        radioSapHet.isChecked = true
                    }

                    slider.value = monAndata.trangThaiGiamGia?.toFloat()!!
                    imageList = ArrayList()
                    listOldImage = monAndata.hinhAnh as MutableList<String>
                    for ((index, linkImage) in monAndata.hinhAnh!!.withIndex()) {
                        if (index == 0) {
                            imageList.add(SlideModel(linkImage, "                                Ảnh nổi bật"))
                        }
                        else {
                            imageList.add(SlideModel(linkImage))
                        }
                        listImageData.add(linkImage)
                    }
                    updateImageSlider()


                }
            }
        }




        // Set initial text based on the slider's current value
        sliderLabel.text = "${slider.value.toInt()}%"

        //Set label for percentage discount
        slider.addOnChangeListener { _, value, _ ->
            sliderLabel.text = "${value.toInt()}%"
        }

        //Set data for combobox LoaiMonAn
        dbLoaiMonAn.getAllLoaiLoaiMonAn { mapLoaiMapAn ->
            val data: List<String?> = mapLoaiMapAn?.values
                ?.map { loaiMonAn -> loaiMonAn?.tenMonAn }
                ?.filter { it != "Tất cả món ăn" }
                ?: emptyList()
            val sorted = data.sortedBy { it }
            val loaiMonAn: ArrayAdapter<String?> =
                ArrayAdapter<String?>(this, R.layout.list_item, sorted)
            val loai = mapLoaiMapAn?.get(monAn?.loaiMonAn)?.tenMonAn
            comboboxLoaiMonAn.setText(loai, false) // Set default to the first item
            comboboxLoaiMonAn.setAdapter(loaiMonAn)
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
        showImage.setImageList(imageList)

        //Add MonAn
        addMonAnButton.setOnClickListener {
            if (giaMonAn.text.toString() == "" || tenMonAn.text.toString() == "" || moTaMonAn.text.toString() == "") {
                Toasty.error(this, "Vui lòng điền đầy đủ thông tin của món ăn").show()
            } else {
                val tenMonAn = tenMonAn.text.toString()
                val mota = moTaMonAn.text.toString()
                val gia = giaMonAn.text.toString()
                val giaFormatted: Double = gia.replace(" ", "").toDouble()

                // Lấy tình trạng
                val selectedId: Int = radioGroup.checkedRadioButtonId
                val selectedRadioButton: RadioButton? = findViewById(selectedId)
                val tinhTrang: String = selectedRadioButton?.text.toString()

                val giamGia = sliderLabel.text.toString().replace("%", "").toInt()
                val loaiMonAn = comboboxLoaiMonAn.text.toString()
                val keyLoaiMonAn =
                    mapMonAn?.filterValues { it?.tenMonAn == loaiMonAn }?.keys?.first()

                val firebaseStorage = FirebaseStorageDB()
                val loadingDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
                loadingDialog.setTitleText("Đang cập nhật món ăn...")
                loadingDialog.setCancelable(false)
                loadingDialog.show()
                var total = 0
                var success = 0
                if (listUri.isNotEmpty()) {
                    for (uri in listUri) {
                        firebaseStorage.uploadImageToFirebase(uri) { linkImage ->
                            if (linkImage != null) {
                                listImageData.add((linkImage))
                                success++
                            }
                            total++
                            if (total == listUri.size) { // Upload finished
                                if (success != total) { // Upload fail
                                    loadingDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE)
                                    loadingDialog.setTitleText("Tải ảnh lên thất bại")
                                    loadingDialog.setContentText("Đã có lỗi xảy ra khi tải ảnh lên. Vui lòng thử lại sau.")
                                    loadingDialog.setConfirmText("OK")
                                    loadingDialog.setConfirmClickListener { failDialog ->
                                        failDialog.dismissWithAnimation()
                                        super.finish()
                                    }
                                } else {
                                    //Đổi ảnh nồi bật lên đầu
                                    val temp = listImageData[anhNoiBat]
                                    listImageData[anhNoiBat] = listImageData[0]
                                    listImageData[0] = temp

                                    val monAnAdd = MonAn(
                                        tenMonAn,
                                        giaFormatted,
                                        tinhTrang,
                                        mota,
                                        giamGia,
                                        listImageData,
                                        keyLoaiMonAn
                                    )
                                    val dbMonAn = CRUD_MonAn()
                                    dbMonAn.updateMonAn(keyMonAn!!, monAnAdd) { success1 ->
                                        if (success1) {
                                            loadingDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
                                            loadingDialog.setTitleText("Thành công")
                                            loadingDialog.setContentText("Cập nhật món ăn thành công")
                                            loadingDialog.setConfirmText("OK")
                                            loadingDialog.setConfirmClickListener { dialog ->
                                                dialog.dismissWithAnimation()
                                                super.finish()
                                            }
                                        } else {
                                            loadingDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE)
                                            loadingDialog.setTitleText("Thất bại")
                                            loadingDialog.setContentText("Cập nhật món ăn thất bại do lỗi hệ thống. Vui lòng thử lại sau")
                                            loadingDialog.setConfirmText("OK")
                                            loadingDialog.setConfirmClickListener { failDialog ->
                                                failDialog.dismissWithAnimation()
                                                super.finish()

                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                else {
                    //Đổi ảnh nồi bật lên đầu
                    val temp = listImageData[anhNoiBat]
                    listImageData[anhNoiBat] = listImageData[0]
                    listImageData[0] = temp

                    val monAnAdd = MonAn(
                        tenMonAn,
                        giaFormatted,
                        tinhTrang,
                        mota,
                        giamGia,
                        listImageData,
                        keyLoaiMonAn
                    )
                    val dbMonAn = CRUD_MonAn()
                    dbMonAn.updateMonAn(keyMonAn!!, monAnAdd) { success1 ->
                        if (success1) {
                            loadingDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
                            loadingDialog.setTitleText("Thành công")
                            loadingDialog.setContentText("Cập nhật món ăn thành công")
                            loadingDialog.setConfirmText("OK")
                            loadingDialog.setConfirmClickListener { dialog ->
                                dialog.dismissWithAnimation()
                                super.finish()
                            }
                        } else {
                            loadingDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE)
                            loadingDialog.setTitleText("Thất bại")
                            loadingDialog.setContentText("Cập nhật món ăn thất bại do lỗi hệ thống. Vui lòng thử lại sau")
                            loadingDialog.setConfirmText("OK")
                            loadingDialog.setConfirmClickListener { failDialog ->
                                failDialog.dismissWithAnimation()
                                super.finish()

                            }
                        }
                    }
                }

            }
        }

    }


    fun updateImageSlider() {
        var doubleClickDetected = false
        val handler = Handler(Looper.getMainLooper())
        var singleClickRunnable: Runnable? = null

        showImage.setImageList(imageList) // Update the image list

        showImage.setItemClickListener(object : ItemClickListener {
            override fun doubleClick(position: Int) {
                doubleClickDetected = true
                singleClickRunnable?.let { handler.removeCallbacks(it) }

                if (imageList.size==1){
                    Toasty.error(this@SuaMonAnActivity,"Vui lòng để lại ít nhất 1 ảnh!").show()
                    return
                }

                val confirmDialog = SweetAlertDialog(this@SuaMonAnActivity, SweetAlertDialog.WARNING_TYPE)
                confirmDialog.setTitleText("Xác nhận")
                confirmDialog.setConfirmText("OK")
                confirmDialog.setCancelable(true)
                confirmDialog.setContentText("Bạn có muốn XÓA ảnh này?")
                confirmDialog.setCancelText("Hủy")
                confirmDialog.setConfirmClickListener { sDialog ->
                    imageList.removeAt(position)
                    if (position == anhNoiBat){
                        imageList[0].title="                                Ảnh nổi bật"
                    }
                    if (position >= listImageData.size) {
                        listUri.removeAt(position - listImageData.size)
                    } else {
                        listImageData.removeAt(position)
                    }
                    updateImageSlider()
                    sDialog.dismissWithAnimation()
                }
                confirmDialog.setCancelClickListener { sDialog ->
                    sDialog.cancel()
                }
                confirmDialog.show()
            }

            override fun onItemSelected(position: Int) {
                if (doubleClickDetected) {
                    doubleClickDetected = false
                    handler.postDelayed({
                        doubleClickDetected = false
                    }, 300)
                    return
                }

                singleClickRunnable = Runnable {
                    val confirmDialog = SweetAlertDialog(this@SuaMonAnActivity, SweetAlertDialog.WARNING_TYPE)
                    confirmDialog.setTitleText("Xác nhận")
                    confirmDialog.setConfirmText("OK")
                    confirmDialog.setCancelable(true)
                    confirmDialog.setContentText("Bạn có muốn đặt ảnh này làm Ảnh nổi bật?")
                    confirmDialog.setCancelText("Hủy")
                    confirmDialog.setConfirmClickListener { sDialog ->
                        anhNoiBat = position
                        for ((index, image) in imageList.withIndex()){
                            if (index==position){
                                image.title = "                                Ảnh nổi bật"
                            }
                            else {
                                image.title = null
                            }
                        }
                        updateImageSlider()
                        sDialog.dismissWithAnimation()
                    }
                    confirmDialog.setCancelClickListener { sDialog ->
                        sDialog.cancel()
                    }
                    confirmDialog.show()
                }

                handler.postDelayed(singleClickRunnable!!, 300) // Delay for 300 milliseconds
            }
        })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 200 && resultCode == Activity.RESULT_OK && data != null) {

            // Check if multiple images were selected
            if (data.clipData != null) {
                val count = data.clipData!!.itemCount
                for (i in 0 until count) {
                    val imageUri = data.clipData!!.getItemAt(i).uri
                    // Convert the URI to a string or a resource that ImageSlider accepts
                    imageList.add(SlideModel(imageUri.toString()))
                    listUri.add(imageUri)
                }
            } else if (data.data != null) {
                // Single image selected
                val imageUri = data.data
                imageList.add(SlideModel(imageUri.toString())) // Add image to list
                if (imageUri != null) {
                    listUri.add(imageUri)
                }
            }

            updateImageSlider()
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