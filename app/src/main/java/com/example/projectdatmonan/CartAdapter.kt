package com.example.projectdatmonan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.projectdatmonan.Model.MonAn
import com.google.gson.Gson

class CartAdapter(
    private val cartItems: MutableList<MonAn>,
    private val onItemRemoved: (MutableList<MonAn>) -> Unit // Callback để cập nhật khi xóa
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemImage: ImageView = itemView.findViewById(R.id.image)
        val itemName: TextView = itemView.findViewById(R.id.tenMonAn)
        val itemPrice: TextView = itemView.findViewById(R.id.giaMonAn)
        val itemQuantity: TextView = itemView.findViewById(R.id.soluong)
        val buttonDecrease: ImageButton = itemView.findViewById(R.id.btn_giam)
        val buttonIncrease: ImageButton = itemView.findViewById(R.id.btn_tang)
        val buttonRemove: Button = itemView.findViewById(R.id.btn_xoa)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.spcanthem_layout, parent, false)
        return CartViewHolder(view)
    }

    override fun getItemCount(): Int {
        return cartItems.size
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = cartItems[position]

        holder.itemName.text = item.tenMonAn
        holder.itemPrice.text = "${item.gia} VNĐ"
        holder.itemQuantity.text = item.soLuong.toString()

        //holder.itemImage.setImageResource(item.hinhAnh?.get(0)?.toInt() ?: R.drawable.pho_bo_1)

        // Xóa item khỏi giỏ hàng
        holder.buttonRemove.setOnClickListener {
            val context = holder.itemView.context
            val alertDialog = android.app.AlertDialog.Builder(context)
            alertDialog.setTitle("Xác nhận xóa")
            alertDialog.setMessage("Bạn có chắc chắn muốn xóa món ăn này khỏi giỏ hàng không?")
            alertDialog.setPositiveButton("Có") { _, _ ->
                // Kiểm tra xem vị trí còn hợp lệ không trước khi xóa
                if (position >= 0 && position < cartItems.size) {
                    // Xóa món ăn nếu người dùng chọn "Có"
                    cartItems.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, cartItems.size) // Cập nhật lại danh sách

                    updateCartItemsInPreferences(context)
                    onItemRemoved(cartItems) // Gọi callback để cập nhật tổng tiền
                }
            }
            alertDialog.setNegativeButton("Không") { dialog, _ ->
                dialog.dismiss()
            }
            alertDialog.show()
        }

    }

    private fun updateCartItemsInPreferences(context: android.content.Context) {
        val sharedPreferences = context.getSharedPreferences("GioHang", android.content.Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val updatedCartItemsJson = Gson().toJson(cartItems)
        editor.putString("cart_items", updatedCartItemsJson)
        editor.apply()
    }
}
