package com.example.projectdatmonan

import android.content.Intent
import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projectdatmonan.Model.MonAn
import com.example.projectdatmonan.databinding.ViewHolderRecommendatedBinding
import com.google.firebase.storage.FirebaseStorage
import kotlin.math.log

class MonAnAdapter(private var monAnList: List<MonAn>) :
    RecyclerView.Adapter<MonAnAdapter.MonAnViewHolder>() {

    private var displayedList = monAnList.toMutableList()
    private val averageRatings = mutableMapOf<String, Double>()
    private val monAnIdMap = mutableMapOf<MonAn, String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonAnViewHolder {
        val binding = ViewHolderRecommendatedBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MonAnViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MonAnViewHolder, position: Int) {
        val monAn = displayedList[position]
        val monAnId = monAnIdMap[monAn]
        val averageRating = averageRatings[monAn.tenMonAn ?: ""] ?: 0.0
        holder.bind(monAn, monAnId?:"",averageRating)
    }

    override fun getItemCount(): Int = displayedList.size

    inner class MonAnViewHolder(private val binding: ViewHolderRecommendatedBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(monAn: MonAn, monAnId:String ,averageRating:Double) {
            val originalPrice = monAn.gia?.toDouble() ?: 0.00
            val discount = monAn.trangThaiGiamGia?.toDouble() ?: 0.0
            binding.txtSosao.text = String.format("%.1f", averageRating)
            val discountedPrice = if (discount > 0) {
                originalPrice * (100 - discount) / 100
            } else {
                originalPrice
            }
            binding.textFoodName.text = monAn.tenMonAn
            binding.textFoodPrice.text = "$${String.format("%.2f", originalPrice)}"
            binding.textFoodAvailability.text = monAn.trangThai
            if (discount > 0) {
                binding.textFoodPrice.paintFlags = binding.textFoodPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                binding.textFoodDiscount.text = "$${String.format("%.2f", discountedPrice)}"
                binding.textFoodDiscount.visibility = View.VISIBLE
            } else {
                binding.textFoodPrice.paintFlags = binding.textFoodPrice.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                binding.textFoodDiscount.visibility = View.GONE
            }
            if (!monAn.hinhAnh.isNullOrEmpty()) {
                val imageUrl = monAn.hinhAnh?.get(0)

                if (imageUrl?.startsWith("http") == true || imageUrl?.startsWith("https") == true) {
                    Glide.with(itemView.context).load(imageUrl).into(binding.imageFood)
                } else {
                    val storageReference = FirebaseStorage.getInstance().reference.child(imageUrl!!)
                    storageReference.downloadUrl.addOnSuccessListener { uri ->
                        Glide.with(itemView.context).load(uri).into(binding.imageFood)
                    }.addOnFailureListener {
                        // Handle any errors here
                    }
                }
            }
            itemView.setOnClickListener {
                Log.d("ItemClick", "Món ăn: ${monAn.tenMonAn}, ID: $monAnId")
                val intent = Intent(itemView.context, ChiTietMonAnActivity::class.java)
                intent.putExtra("monAnId", monAnId)
                intent.putExtra("tenMonAn", monAn.tenMonAn)
                intent.putExtra("gia", monAn.gia)
                intent.putExtra("moTa", monAn.moTaChiTiet)
                intent.putExtra("trangThaiGiamGia", monAn.trangThaiGiamGia)
                Log.d("ItemClick", "Món ăn: ${monAn.trangThaiGiamGia}, ID: $monAnId")
                intent.putStringArrayListExtra("hinhAnhList", ArrayList(monAn.hinhAnh))

                itemView.context.startActivity(intent)
            }
        }

    }
    fun updateRating(monAn: MonAn, averageRating: Double) {
        val position = monAnList.indexOf(monAn)
        averageRatings[monAn.tenMonAn ?: ""] = averageRating
        Log.d("Position1", position.toString())
        if (position != -1) {
            notifyItemChanged(position)
        }
    }
    fun addMonAnWithId(id: String?, monAn: MonAn) {
        id?.let {
            monAnIdMap[monAn] = it
        }
    }
    fun updateList(newList: List<MonAn>) {
        displayedList.clear()
        displayedList.addAll(newList)
        notifyDataSetChanged()
    }
}