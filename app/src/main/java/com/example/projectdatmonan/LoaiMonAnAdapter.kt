package com.example.projectdatmonan

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projectdatmonan.Model.LoaiMonAn
import com.example.projectdatmonan.databinding.ViewHolderCategoryBinding

class LoaiMonAnAdapter(val items:MutableList<LoaiMonAn>):
    RecyclerView.Adapter<LoaiMonAnAdapter.Viewholder>() { 
    private var selectedPosition=-1
    private var lastSelectedPosition=-1
    private lateinit var context:Context
    class Viewholder(val binding:ViewHolderCategoryBinding):
        RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoaiMonAnAdapter.Viewholder {
        context=parent.context
        val binding=ViewHolderCategoryBinding.inflate(LayoutInflater.from(context),parent,false)
        return Viewholder(binding)
    }

    override fun onBindViewHolder(holder: LoaiMonAnAdapter.Viewholder, position: Int) {
        val item=items[position]
        holder.binding.txtCategory.text=item.title

        Glide.with(holder.itemView.context)
            .load(item.picUrl)
            .into(holder.binding.imgCategory)
        holder.binding.root.setOnClickListener{
            lastSelectedPosition=selectedPosition
            selectedPosition=position
            notifyItemChanged(lastSelectedPosition)
            notifyItemChanged(selectedPosition)
        }
        holder.binding.txtCategory.setTextColor(context.resources.getColor(R.color.white))
        if(selectedPosition==position){
            holder.binding.imgCategory.setBackgroundColor(0)
            holder.binding.layoutCategory.setBackgroundColor(R.drawable.grey_bg)
            ImageViewCompat.setImageTintList(holder.binding.imgCategory, ColorStateList.valueOf(context.getColor(R.color.white)))

            holder.binding.txtCategory.visibility=View.VISIBLE
        }else{
            holder.binding.imgCategory.setBackgroundColor(R.drawable.grey_bg)
            holder.binding.layoutCategory.setBackgroundColor(0)
            ImageViewCompat.setImageTintList(holder.binding.imgCategory, ColorStateList.valueOf(context.getColor(R.color.black)))

            holder.binding.txtCategory.visibility=View.GONE
        }
    }

    override fun getItemCount(): Int = items.size

}