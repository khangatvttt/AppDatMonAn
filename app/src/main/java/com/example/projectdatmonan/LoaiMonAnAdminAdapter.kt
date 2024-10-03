package com.example.projectdatmonan

// FoodAdapter.kt
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projectdatmonan.Model.LoaiMonAn

class LoaiMonAnAdminAdapter(
    private val foodMap: HashMap<String?, LoaiMonAn?>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<LoaiMonAnAdminAdapter.MonAnViewHolder>() {

    private val entries = foodMap.entries.toList()

    class MonAnViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(loaiMonAnItem: LoaiMonAn, key: String, listener: OnItemClickListener) {
            itemView.findViewById<TextView>(R.id.txtTenLoaiAdmin).setText(loaiMonAnItem.tenMonAn)
            val imgview: ImageView = itemView.findViewById(R.id.imgLoaiMonAn)
            Glide.with(itemView.context).load(loaiMonAnItem.hinhAnh).into(imgview)

            // Set onClickListener to pass the key
            itemView.setOnClickListener {
                listener.onItemClick(key)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonAnViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.loai_mon_an, parent, false)
        return MonAnViewHolder(view)
    }

    override fun onBindViewHolder(holder: MonAnViewHolder, position: Int) {
        val entry = entries[position]
        entry.value?.let { entry.key?.let { it1 -> holder.bind(it, it1, listener) } }
    }

    override fun getItemCount(): Int {
        return entries.size
    }

    interface OnItemClickListener {
        fun onItemClick(key: String)
    }

}
