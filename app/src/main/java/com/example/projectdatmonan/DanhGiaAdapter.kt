package com.example.projectdatmonan.Adapter

import android.app.AlertDialog
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projectdatmonan.Model.DanhGia
import com.example.projectdatmonan.R
import com.example.projectdatmonan.Database.CRUD_DanhGia
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DanhGiaAdapter(
    private val danhGiaList: MutableList<DanhGia>,
    private val crudDanhGia: CRUD_DanhGia
) : RecyclerView.Adapter<DanhGiaAdapter.DanhGiaViewHolder>() {

    inner class DanhGiaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ratingBar: RatingBar = itemView.findViewById(R.id.rating)
        val ngayGioTextView: TextView = itemView.findViewById(R.id.NgayGio)
        val noiDungTextView: TextView = itemView.findViewById(R.id.NoiDungBinhLuan)
        val avatarImageView: ImageView = itemView.findViewById(R.id.avatar)
        val tenTextView: TextView = itemView.findViewById(R.id.txtTen)
        val btnXoa: ImageView = itemView.findViewById(R.id.btnXoa)
        val btnSua: ImageView = itemView.findViewById(R.id.btnSua)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DanhGiaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.binhluan_layout, parent, false)
        return DanhGiaViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: DanhGiaViewHolder, position: Int) {
        val danhGia = danhGiaList[position]

        // Hiển thị số sao
        holder.ratingBar.rating = danhGia.soSao?.toFloat() ?: 0f

        // Tính thời gian "bao lâu trước"
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val thoiGianDanhGia = LocalDateTime.parse(danhGia.thoiGian, formatter)
        val thoiGianHienTai = LocalDateTime.now()
        val duration = Duration.between(thoiGianDanhGia, thoiGianHienTai)
        val thoiGianTruoc = when {
            duration.toDays() > 0 -> "${duration.toDays()} ngày trước"
            duration.toHours() > 0 -> "${duration.toHours()} giờ trước"
            duration.toMinutes() > 0 -> "${duration.toMinutes()} phút trước"
            else -> "Vừa xong"
        }

        holder.ngayGioTextView.text = thoiGianTruoc

        // Hiển thị nội dung bình luận
        holder.noiDungTextView.text = danhGia.noiDung

        // Hiển thị avatar (dùng Glide để load hình ảnh từ URL nếu có)
        Glide.with(holder.itemView.context).load(R.drawable.testimage).into(holder.avatarImageView)

        holder.btnXoa.setOnClickListener {
            if (position != RecyclerView.NO_POSITION) {
                val context = holder.itemView.context

                // Tạo một AlertDialog để xác nhận việc xóa
                AlertDialog.Builder(context)
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có chắc chắn muốn xóa đánh giá này không?")
                    .setPositiveButton("Xóa") { dialog, _ ->
                        // Xóa dữ liệu từ Firebase
                        danhGia.key?.let { key ->
                            crudDanhGia.deleteDanhGia(key, onSuccess = {
                                // Xóa khỏi danh sách cục bộ và cập nhật RecyclerView sau khi Firebase xóa thành công
                                danhGiaList.removeAt(position)
                                notifyItemRemoved(position)
                                notifyItemRangeChanged(position, danhGiaList.size)

                                // Hiển thị thông báo xóa thành công
                                Toast.makeText(context, "Xóa đánh giá thành công", Toast.LENGTH_SHORT).show()
                            }, onFailure = { error ->
                                // Hiển thị thông báo lỗi khi xóa thất bại
                                Toast.makeText(context, "Lỗi khi xóa đánh giá: $error", Toast.LENGTH_SHORT).show()
                                Log.e("DanhGiaAdapter", "Lỗi khi xóa đánh giá: $error")
                            })
                        }
                    }
                    .setNegativeButton("Hủy") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }

        // Xử lý nút sửa
        holder.btnSua.setOnClickListener {
            if (position != RecyclerView.NO_POSITION) {
                val context = holder.itemView.context
                val dialogView = LayoutInflater.from(context).inflate(R.layout.suadanhgia_layout, null)
                val editRatingBar = dialogView.findViewById<RatingBar>(R.id.DiemDanhGiaNhanXet)
                val editNoiDung = dialogView.findViewById<TextView>(R.id.edtNhanXet)

                // Điền dữ liệu hiện tại của đánh giá vào dialog
                editRatingBar.rating = danhGia.soSao ?: 0f
                editNoiDung.text = danhGia.noiDung

                AlertDialog.Builder(context)
                    .setView(dialogView)
                    .setPositiveButton("Lưu") { dialog, _ ->
                        // Cập nhật dữ liệu đánh giá
                        val updatedRating = editRatingBar.rating
                        val updatedNoiDung = editNoiDung.text.toString()

                        val updatedDanhGia = danhGia.copy(
                            soSao = updatedRating,
                            noiDung = updatedNoiDung,
                            thoiGian = LocalDateTime.now().format(formatter)  // Cập nhật thời gian
                        )

                        // Gọi phương thức updateDanhGia để cập nhật Firebase
                        crudDanhGia.updateDanhGia(updatedDanhGia, onSuccess = {
                            danhGiaList[position] = updatedDanhGia
                            notifyItemChanged(position)
                            Toast.makeText(context, "Cập nhật đánh giá thành công", Toast.LENGTH_SHORT).show()
                        }, onFailure = { error ->
                            Toast.makeText(context, "Lỗi khi cập nhật đánh giá: $error", Toast.LENGTH_SHORT).show()
                            Log.e("DanhGiaAdapter", "Lỗi khi cập nhật đánh giá: $error")
                        })
                    }
                    .setNegativeButton("Hủy") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }

    }

    override fun getItemCount(): Int {
        return danhGiaList.size
    }

    // Thêm đánh giá vào danh sách và cập nhật RecyclerView
    fun addDanhGia(danhGia: DanhGia) {
        danhGiaList.add(danhGia)
        notifyItemInserted(danhGiaList.size - 1)
    }

    fun clearDanhGia() {
        danhGiaList.clear()
        notifyDataSetChanged()
    }

    fun addAllDanhGia(danhGiaList: List<DanhGia>) {
        this.danhGiaList.addAll(danhGiaList)
        notifyDataSetChanged()
    }

    fun updateDanhGia(danhGiaList: List<DanhGia>) {
        this.danhGiaList.clear()
        this.danhGiaList.addAll(danhGiaList)
        notifyDataSetChanged()
    }

    fun getDanhSachDanhGia(): List<DanhGia> {
        return danhGiaList
    }
}