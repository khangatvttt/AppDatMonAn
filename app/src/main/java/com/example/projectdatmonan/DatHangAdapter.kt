package com.example.projectdatmonan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.projectdatmonan.Model.MonAn

class DatHangAdapter(private val orderItems: List<MonAn>) :
    RecyclerView.Adapter<DatHangAdapter.OrderViewHolder>(){
    class OrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemName: TextView = view.findViewById(R.id.tv_item_name)
        val itemPrice: TextView = view.findViewById(R.id.tv_item_price)
        val itemQuantity: TextView = view.findViewById(R.id.tv_item_quantity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.dathang_layout, parent, false)
        return OrderViewHolder(view)
    }
    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val item = orderItems[position]
        holder.itemName.text = "- ${item.tenMonAn}"
        holder.itemPrice.text = "(${item.gia} VNĐ)"
        holder.itemQuantity.text = "- Số lượng: ${item.soLuong}"
    }

    override fun getItemCount(): Int {
        return orderItems.size
    }


}