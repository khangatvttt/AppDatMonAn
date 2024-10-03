package com.example.projectdatmonan

// FoodAdapter.kt
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projectdatmonan.Model.MonAn
import kotlinx.coroutines.currentCoroutineContext
import java.text.NumberFormat
import java.util.Locale

class MonAnAdminAdapter(private val foodMap: HashMap<String?,MonAn?>) :
    RecyclerView.Adapter<MonAnAdminAdapter.MonAnViewHolder>() {
    private val entries = foodMap.entries.toList()

    class MonAnViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(monAnItem: MonAn, key:String) {
            itemView.findViewById<TextView>(R.id.txtName).setText(monAnItem.tenMonAn)

            val giamGia:Double = monAnItem.trangThaiGiamGia!!.toDouble()/100
            val gia = monAnItem.gia!!
            val priceSale = (gia*(1-giamGia) / 1000).toInt() * 1000
            itemView.findViewById<TextView>(R.id.txtPrice).setText(formatCurrencyVND(priceSale))
            if (monAnItem.trangThaiGiamGia!!!=0) {
                itemView.findViewById<TextView>(R.id.txtPriceOriginal)
                    .setText(formatCurrencyVND(monAnItem.gia!!.toInt()))
            }
            else{
                itemView.findViewById<TextView>(R.id.txtPriceOriginal)
                    .setText("")
            }

            val imageView:ImageView = itemView.findViewById(R.id.imgMonAn)
            itemView.findViewById<TextView>(R.id.txtDescription).setText(monAnItem.trangThai)
            Glide.with(itemView.context).load(monAnItem.hinhAnh?.get(0)).into(imageView)

            itemView.findViewById<ImageButton>(R.id.btnXoaMonAn).setOnClickListener {
                Toast.makeText(itemView.context, key, Toast.LENGTH_LONG).show()
            }

        }

        private fun formatCurrencyVND(amount: Int): String {
            val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
            return formatter.format(amount) + " VNĐ"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonAnViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.mon_an_admin, parent, false)
        return MonAnViewHolder(view)
    }

    override fun onBindViewHolder(holder: MonAnViewHolder, position: Int) {
        val entry = entries[position]
        entry.value?.let { entry.key?.let { it1 -> holder.bind(it, it1) } }
    }

    override fun getItemCount(): Int {
        return entries.size
    }
}
