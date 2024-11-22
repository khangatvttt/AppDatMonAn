package com.example.projectdatmonan.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projectdatmonan.R

class ImagesAdapter(
    private val imageList: List<String>,
    private val onImageClick: (String) -> Unit // Hàm callback để xử lý khi ảnh được nhấn
) : RecyclerView.Adapter<ImagesAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.image_item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.image_item_layout, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageUrl = imageList[position]

        if (imageUrl.isNotEmpty()) {
            Glide.with(holder.imageView.context)
                .load(imageUrl)
                .placeholder(R.drawable.testimage)
                .error(R.drawable.testimage)
                .into(holder.imageView)
        }

        // Gắn sự kiện click vào hình ảnh
        holder.imageView.setOnClickListener {
            onImageClick(imageUrl) // Gọi callback khi ảnh được nhấn
        }
    }

    override fun getItemCount(): Int {
        return imageList.size
    }
}