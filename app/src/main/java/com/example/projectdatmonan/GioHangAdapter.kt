package com.example.projectdatmonan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.projectdatmonan.Model.MonAn
class GioHangAdapter(private val items: List<MonAn>) : RecyclerView.Adapter<GioHangAdapter.GioHangViewHolder>() {

        class GioHangViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val productImage: ImageView = itemView.findViewById(R.id.a)
            val productName: TextView = itemView.findViewById(R.id.b)
            val productPrice: TextView = itemView.findViewById(R.id.c)
            val productQuantity: TextView = itemView.findViewById(R.id.e)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GioHangViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.spcanthem, parent, false)
            return GioHangViewHolder(view)
        }

        override fun onBindViewHolder(holder: GioHangViewHolder, position: Int) {
            val product = items[position]
            holder.productName.text = product.tenMonAn
            holder.productPrice.text = "${product.gia} VND"
            holder.productQuantity.text = product.soluong.toString()
            holder.productImage.setImageResource(R.drawable.testimage)
        }

        override fun getItemCount(): Int {
            return items.size
        }
        }
