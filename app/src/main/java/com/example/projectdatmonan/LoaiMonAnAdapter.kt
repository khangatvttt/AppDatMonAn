package com.example.projectdatmonan

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projectdatmonan.Model.LoaiMonAn
import com.example.projectdatmonan.databinding.ViewHolderCategoryBinding

class LoaiMonAnAdapter(
    private val items: MutableList<LoaiMonAn>,
    private val onCategorySelected: (String) -> Unit
) : RecyclerView.Adapter<LoaiMonAnAdapter.ViewHolder>() {

    private var selectedPosition = 0
    private lateinit var context: Context

    class ViewHolder(val binding: ViewHolderCategoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val binding = ViewHolderCategoryBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.txtCategory.text = item.tenMonAn

        Glide.with(holder.itemView.context)
            .load(item.hinhAnh)
            .into(holder.binding.imgCategory)

        holder.binding.root.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)
            onCategorySelected(item.tenMonAn ?: "")
        }

        holder.binding.txtCategory.setTextColor(
            if (selectedPosition == position) context.getColor(R.color.black) else context.getColor(R.color.grey)
        )
        holder.binding.txtCategory.visibility = if (selectedPosition == position) View.VISIBLE else View.GONE
    }

    override fun getItemCount(): Int = items.size

    fun clearSelection() {
        val previousPosition = selectedPosition
        selectedPosition = -1
        notifyItemChanged(previousPosition)
    }
}
