package com.example.projectdatmonan

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.bumptech.glide.Glide
import com.example.projectdatmonan.Database.CRUD_LoaiMonAn
import com.example.projectdatmonan.Database.FirebaseStorageDB
import com.example.projectdatmonan.Model.LoaiMonAn
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import es.dmoral.toasty.Toasty


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class FragmentQuanLyLoaiMonAn : Fragment(), LoaiMonAnAdminAdapter.OnItemClickListener {
    private val firebaseStorage:FirebaseStorageDB = FirebaseStorageDB()
    private val db:CRUD_LoaiMonAn = CRUD_LoaiMonAn()
    private var imageUriUpload:Uri? = null
    private var recyclerView: RecyclerView? = null
    private lateinit var adapter: LoaiMonAnAdminAdapter
    private var deleteButton: Button? = null
    private var cancelButton: Button? = null
    private var editLoaiButton: Button? = null
    private var addButton: FloatingActionButton? = null
    private var imageView:ImageView?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_quan_ly_loai_mon_an, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.recyclerViewLoaiMonAn)
        deleteButton = view.findViewById(R.id.deleteLoaiMonAn)
        cancelButton = view.findViewById(R.id.cancelSelectMode)
        editLoaiButton = view.findViewById(R.id.editLoaiBtn)
        addButton = view.findViewById(R.id.addBtn)

        //While loading, hide other components, show loading component
        view.findViewById<ProgressBar>(R.id.progressBar).visibility = View.VISIBLE
        deleteButton?.visibility = View.GONE
        cancelButton?.visibility = View.GONE
        editLoaiButton?.visibility = View.GONE
        addButton?.visibility = View.GONE


        val db = CRUD_LoaiMonAn()
        db.getAllLoaiLoaiMonAn { mapLoaiMonAn ->
            recyclerView?.layoutManager = GridLayoutManager(this.context, 2)
            adapter = mapLoaiMonAn?.let { LoaiMonAnAdminAdapter(it, this) }!!
            recyclerView?.adapter = adapter
            view.findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE
            addButton?.visibility = View.VISIBLE

        }

        deleteButton?.setOnClickListener {
            val selectedItems = adapter.getSelectedItems()
            if (selectedItems.isEmpty()){
                this.context?.let { it1 -> Toasty.error(it1, "Vui lòng chọn ít nhất một món ăn để xóa").show() }
            }
            else {
                deleteSelectedItems(selectedItems)
            }
        }

        editLoaiButton?.setOnClickListener {
            val selectedItems = adapter.getSelectedItems().size
            if (selectedItems>1){
                openWarningDialog("Vui lòng chỉ chọn một loại để sửa trong một lần.")
            }
            else if (selectedItems==0){
                openWarningDialog("Vui lòng chọn một loại để sửa.")
            }
            else{
                val position:Int = adapter.getSelectedItems()[0]
                val keyToDelete = adapter.foodMap.entries.toList().sortedWith(
                    compareByDescending<Map.Entry<String?, LoaiMonAn?>> { it.value?.tenMonAn == "Tất cả món ăn" }
                        .thenBy { it.value?.tenMonAn })[position].key
                val loaiMonAn: LoaiMonAn? = adapter.foodMap[keyToDelete]
                if (loaiMonAn != null && keyToDelete != null) {
                    editLoaiMonAn(loaiMonAn, keyToDelete)
                }
            }

        }

        cancelButton?.setOnClickListener {
            exitSelectionMode()
        }

        addButton?.setOnClickListener {
            showCustomDialog()
        }
    }

    override fun onItemClick(key: String, position: Int) {
        if (adapter.isSelectionMode) {
            adapter.toggleSelection(position)
        } else {
            val fragmentQuanLyMonAn = FragmentQuanLyMonAn()
            val bundle = Bundle()
            bundle.putString("LoaiMonAnID", key)
            fragmentQuanLyMonAn.arguments = bundle

            // Navigate to FragmentQuanLyMonAn
            val transaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.container, fragmentQuanLyMonAn)
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }

    override fun onItemLongClick(key: String, position: Int) {
        if (!adapter.isSelectionMode) {
            // Enter selection mode
            adapter.isSelectionMode = true
            deleteButton?.visibility = View.VISIBLE
            cancelButton?.visibility = View.VISIBLE
            editLoaiButton?.visibility = View.VISIBLE
            addButton?.visibility = View.GONE
        }
        // Toggle selection for the long-pressed item
        adapter.toggleSelection(position)
    }

    private fun exitSelectionMode() {
        adapter.isSelectionMode = false
        adapter.clearSelections()
        deleteButton?.visibility = View.GONE
        cancelButton?.visibility = View.GONE
        editLoaiButton?.visibility = View.GONE
        addButton?.visibility = View.VISIBLE
    }

    private fun deleteSelectedItems(selectedPositions: List<Int>) {
        SweetAlertDialog(this.context, SweetAlertDialog.WARNING_TYPE)
            .setTitleText("Xác nhận xóa")
            .setContentText("Bạn có chắc chắn muốn xóa những loại đã chọn? Tất cả món thuộc loại đã chọn cũng sẽ bị xóa. Hành động này không thể hoàn tác.")
            .setCancelText("Hủy")
            .setConfirmText("Xóa")
            .showCancelButton(true)
            .setConfirmClickListener { sDialog ->
                sDialog.dismissWithAnimation()
                val loadingDialog = SweetAlertDialog(this.context, SweetAlertDialog.PROGRESS_TYPE)
                loadingDialog.setTitleText("Đang xóa...")
                loadingDialog.setCancelable(false)
                loadingDialog.show()

                selectedPositions.sortedDescending()
                val totalItems = selectedPositions.size
                var successCount = 0
                var failCount = 0

                val db = CRUD_LoaiMonAn()

                selectedPositions.forEachIndexed { index, position ->
                    val keyToDelete = adapter.foodMap.entries.toList().sortedWith(
                        compareByDescending<Map.Entry<String?, LoaiMonAn?>> { it.value?.tenMonAn == "Tất cả món ăn" }
                            .thenBy { it.value?.tenMonAn })[position].key
                    keyToDelete?.let {
                        db.deleteLoaiMonAn(it) { success ->
                            if (success) {
                                successCount++
                                adapter.foodMap.remove(it)
                                adapter.notifyItemRemoved(position)
                            } else {
                                failCount++
                            }

                            // If it's the last item being processed, show a summary
                            if (index == selectedPositions.lastIndex) {
                                if (successCount == 0) {
                                    loadingDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE)
                                    loadingDialog.setTitleText("Xóa thất bại!")
                                    loadingDialog.setContentText("Đã xảy ra lỗi vui lòng thử lại sau")
                                    loadingDialog.setConfirmText("OK")
                                    loadingDialog.setConfirmClickListener { failDialog ->
                                        failDialog.dismissWithAnimation()
                                    }
                                }
                                else{
                                    val message = "$successCount/$totalItems xóa thành công."
                                    loadingDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
                                    loadingDialog.setTitleText("Xóa thành công!")
                                    loadingDialog.setContentText(message)
                                    loadingDialog.setConfirmText("OK")
                                    loadingDialog.setConfirmClickListener { successDialog ->
                                        successDialog.dismissWithAnimation()
                                    }
                                }
                                exitSelectionMode()
                                // Refresh the list after deletion
                                adapter.notifyDataSetChanged()
                            }
                        }
                    }
                }
            }
            .setCancelClickListener { sDialog ->
                sDialog.cancel()
            }
            .show()

    }

    private fun showCustomDialog() {
        imageUriUpload = null
        val dialogView = layoutInflater.inflate(R.layout.add_loai_mon_an_dialog, null)
        val editText = dialogView.findViewById<TextInputEditText>(R.id.txtTenLoaiAdd)

        imageView = dialogView.findViewById(R.id.anhLoaiMonAnAdd)
        val loadingAdd = dialogView.findViewById<LinearProgressIndicator>(R.id.loadingAddLoai)
        loadingAdd.visibility = View.GONE

        val title = SpannableString("Thêm loại món ăn")
        title.setSpan(StyleSpan(Typeface.BOLD), 0, title.length, 0)

        val dialog = AlertDialog.Builder(this.context, R.style.CustomDialogStyle)
            .setTitle(title)
            .setView(dialogView)
            .setPositiveButton("Thêm" , null)
            .setNegativeButton("Hủy") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            val layoutParams = positiveButton.layoutParams as LinearLayout.LayoutParams
            layoutParams.setMargins(10, 0, 10, 0) // Set left and right margins
            negativeButton.layoutParams = layoutParams
            negativeButton.setTextColor(Color.BLACK)
            negativeButton.setBackgroundResource(R.drawable.custom_button_dialog)
            positiveButton.setTextColor(Color.BLACK)
            positiveButton.setBackgroundResource(R.drawable.custom_button_dialog)
            positiveButton.setOnClickListener {
                val tenLoai = editText.text.toString()
                if (tenLoai.isBlank()) {
                    this.context?.let { it1 -> Toasty.error(it1, "Vui lòng điền tên loại món ăn", Toast.LENGTH_SHORT, true).show() };
                } else {
                    if (imageUriUpload!=null){
                        loadingAdd.visibility = View.VISIBLE
                        firebaseStorage.uploadImageToFirebase(imageUriUpload!!){ linkImage->
                            if (linkImage==null){
                                openFailDialog("Đã có lỗi xảy ra vui lòng thử lại sau")
                            } else{
                                addLoaiMonAnVaoDB(tenLoai, linkImage)
                            }
                            dialog.dismiss()
                        }
                    }
                    else{
                        AlertDialog.Builder(this.context)
                            .setTitle("Xác nhận")
                            .setMessage("Bạn chưa chọn ảnh, ảnh mặc định sẽ được thêm khi tạo. Bạn muốn tiếp tục")
                            .setPositiveButton("Tiếp tục") { _, _ ->
                                val defaultImage = "https://firebasestorage.googleapis.com/v0/b/projectdatmonan.appspot.com/o/defaultLoaiMonAn.png?alt=media&token=6b3be4fc-94ab-4299-84eb-6bfcb83e3b8f"
                                addLoaiMonAnVaoDB(tenLoai, defaultImage)
                                dialog.dismiss()
                            }
                            .setNegativeButton("Quay lại"){ dialog, _ ->
                                dialog.dismiss()
                            }
                            .show()
                    }

                }
            }
        }
        dialog.show()

        imageView?.setOnClickListener {
            pickImageFromGallery()
        }

        dialog.show()
    }

    // Launch gallery to pick an image
    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 1000)
    }

    // Handle result of image selection
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 1000) {
            imageUriUpload = data?.data!!
            imageView?.setImageURI(imageUriUpload)
            imageView?.scaleType = ImageView.ScaleType.FIT_XY
        }
    }


    private fun openSuccessDialog(content:String){
        SweetAlertDialog(this.context, SweetAlertDialog.SUCCESS_TYPE)
            .setTitleText("Thành công!")
            .setContentText(content)
            .setConfirmText("OK")
            .setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
            }
            .show()
    }

    private fun openFailDialog(content:String){
        SweetAlertDialog(this.context, SweetAlertDialog.ERROR_TYPE)
            .setTitleText("Thất bại")
            .setContentText(content)
            .setConfirmText("OK")
            .setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
            }
            .show()
    }

    private fun openWarningDialog(content:String){
        SweetAlertDialog(this.context, SweetAlertDialog.WARNING_TYPE)
            .setContentText(content)
            .setConfirmText("OK")
            .setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
            }
            .show()
    }

    private fun addLoaiMonAnVaoDB(tenLoai:String, linkImage:String){
        val loaiMonAn = LoaiMonAn(tenLoai,linkImage)
        db.addLoaiMonAn(loaiMonAn){key->
            if (key!=null){
                val newfoodMap = adapter.foodMap
                newfoodMap[key] = loaiMonAn
                adapter.updateData(newfoodMap)
                openSuccessDialog("Đã thêm loại món ăn thành công.")
            }
            else{
                openFailDialog("Đã có lỗi xảy ra vui lòng thử lại sau")
            }
        }
    }

    private fun suaMonAnDB(tenLoai:String, linkImage:String, key:String){
        val loaiMonAn = LoaiMonAn(tenLoai,linkImage)
        db.updateLoaiMonAn(key,loaiMonAn){success->
            if (success){
                val newfoodMap = adapter.foodMap
                newfoodMap[key] = loaiMonAn
                adapter.updateData(newfoodMap)
                openSuccessDialog("Đã sửa loại món ăn thành công.")
            }
            else{
                openFailDialog("Đã có lỗi xảy ra vui lòng thử lại sau")
            }
        }
    }

    private fun editLoaiMonAn(loaiMonAn: LoaiMonAn, key:String){
        imageUriUpload = null
        val dialogView = layoutInflater.inflate(R.layout.add_loai_mon_an_dialog, null)
        val editText = dialogView.findViewById<TextInputEditText>(R.id.txtTenLoaiAdd)
        imageView = dialogView.findViewById(R.id.anhLoaiMonAnAdd)
        val loadingAdd = dialogView.findViewById<LinearProgressIndicator>(R.id.loadingAddLoai)

        if (imageView!=null) {
            this.context?.let { Glide.with(it).load(loaiMonAn.hinhAnh).into(imageView!!) }
        }
        editText.setText(loaiMonAn.tenMonAn)


        loadingAdd.visibility = View.GONE

        val title = SpannableString("Sửa loại món ăn")
        title.setSpan(StyleSpan(Typeface.BOLD), 0, title.length, 0)

        val dialog = AlertDialog.Builder(this.context, R.style.CustomDialogStyle)
            .setTitle(title)
            .setView(dialogView)
            .setPositiveButton("Sửa" , null)
            .setNegativeButton("Hủy") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            val layoutParams = positiveButton.layoutParams as LinearLayout.LayoutParams
            layoutParams.setMargins(10, 0, 10, 0) // Set left and right margins
            negativeButton.layoutParams = layoutParams
            negativeButton.setTextColor(Color.BLACK)
            negativeButton.setBackgroundResource(R.drawable.custom_button_dialog)
            positiveButton.setTextColor(Color.BLACK)
            positiveButton.setBackgroundResource(R.drawable.custom_button_dialog)
            positiveButton.setOnClickListener {
                val tenLoai = editText.text.toString()
                if (tenLoai.isBlank()) {
                    this.context?.let { it1 -> Toasty.error(it1, "Vui lòng điền tên loại món ăn", Toast.LENGTH_SHORT, true).show() };
                } else {
                    if (imageUriUpload!=null){
                        loadingAdd.visibility = View.VISIBLE
                        firebaseStorage.uploadImageToFirebase(imageUriUpload!!){ linkImage->
                            if (linkImage==null){
                                openFailDialog("Đã có lỗi xảy ra vui lòng thử lại sau")
                            } else{
                                loaiMonAn.hinhAnh = linkImage
                                loaiMonAn.tenMonAn = tenLoai
                                suaMonAnDB(tenLoai, linkImage, key)
                            }
                            dialog.dismiss()
                            exitSelectionMode()
                        }
                    }
                    else {
                        loaiMonAn.hinhAnh?.let { it1 -> suaMonAnDB(tenLoai, it1, key) }
                        dialog.dismiss()
                        exitSelectionMode()
                    }
                }
            }
        }
        dialog.show()

        imageView?.setOnClickListener {
            pickImageFromGallery()
        }

        dialog.show()
    }

}
