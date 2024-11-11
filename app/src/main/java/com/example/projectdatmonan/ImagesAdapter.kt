package com.example.projectdatmonan.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projectdatmonan.R

class ImagesAdapter(private val imageList: List<String>) :
    RecyclerView.Adapter<ImagesAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.image_item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.image_item_layout, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageUrl = imageList[position]

        // Kiểm tra xem imageUrl có rỗng không trước khi tải
        if (imageUrl.isNotEmpty()) {
            Glide.with(holder.imageView.context) // Sử dụng context từ imageView
                .load(imageUrl)
                .placeholder(R.drawable.testimage) // Hình ảnh placeholder
                .error(R.drawable.testimage) // Hình ảnh lỗi
                .into(holder.imageView)
        }
    }

    override fun getItemCount(): Int {
        return imageList.size
    }
}
