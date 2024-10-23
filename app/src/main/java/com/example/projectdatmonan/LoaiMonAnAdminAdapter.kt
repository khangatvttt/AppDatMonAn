package com.example.projectdatmonan

// FoodAdapter.kt
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projectdatmonan.Model.LoaiMonAn

class LoaiMonAnAdminAdapter(
    var foodMap: HashMap<String?, LoaiMonAn?>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<LoaiMonAnAdminAdapter.MonAnViewHolder>() {

    private var entries = foodMap.entries.toList().sortedWith(
        compareByDescending<Map.Entry<String?, LoaiMonAn?>> { it.value?.tenMonAn == "Tất cả món ăn" }
            .thenBy { it.value?.tenMonAn }
    )
    private val selectedItems = mutableListOf<Int>()
    var isSelectionMode = false
    class MonAnViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtTenLoaiAdmin: TextView = itemView.findViewById(R.id.txtTenLoaiAdmin)
        private val imgLoaiMonAn: ImageView = itemView.findViewById(R.id.imgLoaiMonAn)

        fun bind(
            loaiMonAnItem: LoaiMonAn,
            key: String,
            listener: OnItemClickListener,
            isSelected: Boolean,
            isSelectionMode: Boolean
        ) {
            // Set the data
            txtTenLoaiAdmin.text = loaiMonAnItem.tenMonAn
            Glide.with(itemView.context).load(loaiMonAnItem.hinhAnh).into(imgLoaiMonAn)

            val itemContainer = itemView.findViewById<RelativeLayout>(R.id.relativeLoai)

            itemContainer.setBackgroundColor(
                if (isSelected) itemView.context.getColor(R.color.lightBlue)
                else itemView.context.getColor(android.R.color.white)
            )

            // Click listeners
            itemView.setOnClickListener {
                listener.onItemClick(key, adapterPosition)
            }

            itemView.setOnLongClickListener {
                listener.onItemLongClick(key, adapterPosition)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonAnViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.loai_mon_an, parent, false)
        return MonAnViewHolder(view)
    }

    override fun onBindViewHolder(holder: MonAnViewHolder, position: Int) {
        if (position >= foodMap.size) return
        entries = foodMap.entries.toList().sortedWith(
            compareByDescending<Map.Entry<String?, LoaiMonAn?>> { it.value?.tenMonAn == "Tất cả món ăn" }
                .thenBy { it.value?.tenMonAn })
        val entry = entries[position]
        entry.value?.let { loaiMonAn ->
            holder.bind(
                loaiMonAn,
                entry.key ?: "",
                listener,
                selectedItems.contains(position), // Check if the item is selected
                isSelectionMode // Check if we are in selection mode
            )
        }
    }

    fun getSelectedItems(): List<Int> = selectedItems.toList()


    override fun getItemCount(): Int {
        return foodMap.size
    }

    // Function to toggle selection
    fun toggleSelection(position: Int) {
        if (selectedItems.contains(position)) {
            selectedItems.remove(position)
        } else {
            selectedItems.add(position)
        }
        notifyItemChanged(position)
    }

    // Clear all selections (used when exiting selection mode)
    fun clearSelections() {
        val oldSelected = selectedItems.toList()
        selectedItems.clear()
        oldSelected.forEach { notifyItemChanged(it) }
    }

    // Refresh data and notify adapter
    fun updateData(newFoodMap: HashMap<String?, LoaiMonAn?>) {
        foodMap = newFoodMap
        notifyDataSetChanged() // Notify the adapter that the data has changed
    }

    interface OnItemClickListener {
        fun onItemClick(key: String, position: Int)
        fun onItemLongClick(key: String, position: Int)
    }
}
