package com.example.projectdatmonan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projectdatmonan.Database.CRUD_MonAn
import com.example.projectdatmonan.Database.CRUD_NguoiDung
import com.example.projectdatmonan.Model.DanhGia
import java.time.format.DateTimeFormatter


class DanhGiaAdminAdapter(private var listDanhGia: List<DanhGia>) : RecyclerView.Adapter<DanhGiaAdminAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.feedback_admin, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val danhGia = listDanhGia[position]
        Glide.with(holder.itemView.context).load(danhGia.maNguoiDung?.split(";")?.get(1)).into(holder.avatar)
        holder.tenNguoiDung.text = danhGia.maNguoiDung?.split(";")?.get(0)
        val PATTERN = "dd-MM-yyyy HH:mm:ss"
        val formatter = DateTimeFormatter.ofPattern(PATTERN)
        holder.ngayThang.text = danhGia.thoiGian?.format(formatter)
        holder.ratingbar.rating = danhGia.soSao!!
        holder.tenMonAn.text = danhGia.maMonAn
        holder.noiDung.text = danhGia.noiDung
    }

    override fun getItemCount(): Int {
        return listDanhGia.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val avatar: ImageView = itemView.findViewById(R.id.avatar)
        val ngayThang: TextView = itemView.findViewById(R.id.ngayGioDanhGia)
        val ratingbar: RatingBar = itemView.findViewById(R.id.ratingBar)
        val tenNguoiDung: TextView = itemView.findViewById(R.id.tenNguoiDung)
        val tenMonAn: TextView = itemView.findViewById(R.id.monAnDanhGia)
        val noiDung: TextView = itemView.findViewById(R.id.noiDungDanhGia)

    }

    fun updateData(newList: List<DanhGia>) {
        listDanhGia = newList
        notifyDataSetChanged()  // Notify the adapter that the data has changed
    }
}
