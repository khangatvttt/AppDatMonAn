package com.example.projectdatmonan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.projectdatmonan.Model.MonAn
import com.example.projectdatmonan.databinding.ViewHolderRecommendatedBinding

class MonAnAdapter(private val monAnList: List<MonAn>) :
    RecyclerView.Adapter<MonAnAdapter.MonAnViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonAnViewHolder {
        // Sử dụng View Binding để inflate layout
        val binding = ViewHolderRecommendatedBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MonAnViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MonAnViewHolder, position: Int) {
        val monAn = monAnList[position]
        holder.bind(monAn)
    }

    override fun getItemCount(): Int = monAnList.size

    inner class MonAnViewHolder(private val binding: ViewHolderRecommendatedBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(monAn: MonAn) {
            // Truy cập các thành phần giao diện qua binding
            binding.textFoodName.text = monAn.tenMonAn
            binding.textFoodPrice.text = "$${monAn.gia?.toString() ?: "0.00"}"
            binding.textFoodAvailability.text = monAn.trangThai
            if (monAn.trangThaiGiamGia == null || monAn.trangThaiGiamGia == 0) {
                binding.textFoodDiscount.visibility = View.GONE
            } else {
                binding.textFoodDiscount.text = "$${monAn.trangThaiGiamGia}"
                binding.textFoodDiscount.visibility = View.VISIBLE
            }            // Nếu có hình ảnh, bạn có thể load vào đây
            // Glide.with(itemView.context).load(monAn.hinhAnh?.get(0)).into(binding.imageFood)
        }
    }
}