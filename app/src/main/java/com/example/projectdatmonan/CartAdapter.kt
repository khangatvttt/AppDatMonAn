package com.example.projectdatmonan

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import java.text.DecimalFormat
import com.bumptech.glide.Glide
import com.example.projectdatmonan.Database.CRUD_GioHang
import com.example.projectdatmonan.Model.ListMonAn
import com.example.projectdatmonan.databinding.ViewHolderCartBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CartAdapter(
    private var cartItems: List<ListMonAn>,
    private val dishNames: Map<String, String?>,
    private val dishPrices: Map<String, Double?>,
    private val dishImages: Map<String, String?>,
    private val maNguoiDung: String,
    private val onItemClicked: (ListMonAn) -> Unit,
    private val onQuantityChanged: () -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    private val crudGioHang = CRUD_GioHang()

    inner class CartViewHolder(private val binding: ViewHolderCartBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ListMonAn) {
            binding.txtTitle.text = dishNames[item.maMonAn] ?: "Unknown Dish"
            binding.txtNumberItem.text = item.soLuong.toString()

            val price = dishPrices[item.maMonAn]
            val total = (item.soLuong ?: 0) * (price ?: 0.0)

            // Format giá tiền
            val formattedPrice = formatPrice(price)
            binding.txtFee.text = "$formattedPrice VND"

            val formattedTotal = formatPrice(total)
            binding.txtTotalItem.text = "$formattedTotal VND"

            // Load ảnh
            val imageUrl = dishImages[item.maMonAn]
            if (imageUrl != null) {
                Glide.with(binding.imgItemCart.context)
                    .load(imageUrl)
                    .into(binding.imgItemCart)
            } else {
                binding.imgItemCart.setImageResource(R.drawable.grey_bg)
            }

            // Xử lý nút cộng, trừ, và xóa
            binding.btnPlusCart.setOnClickListener {
                item.soLuong = (item.soLuong ?: 0) + 1
                crudGioHang.updateQuantityInDatabase(maNguoiDung, item, {
                    notifyItemChanged(adapterPosition)
                    onQuantityChanged()
                }, { error ->
                    Log.e("Firebase", "Failed to update quantity", error)
                })
            }

            binding.btnMinusCart.setOnClickListener {
                if ((item.soLuong ?: 0) > 1) {
                    item.soLuong = (item.soLuong ?: 0) - 1
                    crudGioHang.updateQuantityInDatabase(maNguoiDung, item, {
                        notifyItemChanged(adapterPosition)
                        onQuantityChanged()
                    }, { error ->
                        Log.e("Firebase", "Failed to update quantity", error)
                    })
                } else if ((item.soLuong ?: 0) == 1) {
                    crudGioHang.removeItemFromCart(maNguoiDung, item, {
                        cartItems = cartItems.filter { it.maMonAn != item.maMonAn }
                        notifyDataSetChanged()
                        onQuantityChanged()
                    }, { error ->
                        Log.e("Firebase", "Failed to remove item from cart", error)
                    })
                }
            }

            binding.btnDeleteCart.setOnClickListener {
                crudGioHang.removeItemFromCart(maNguoiDung, item, {
                    cartItems = cartItems.filter { it.maMonAn != item.maMonAn }
                    notifyDataSetChanged()
                    onQuantityChanged()
                }, { error ->
                    Log.e("Firebase", "Failed to remove item from cart", error)
                })
            }

            itemView.setOnClickListener {
                onItemClicked(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ViewHolderCartBinding.inflate(inflater, parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(cartItems[position])
    }

    override fun getItemCount(): Int {
        return cartItems.size
    }

    fun updateCartItems(newCartItems: List<ListMonAn>) {
        cartItems = newCartItems
        notifyDataSetChanged()
    }

    fun getQuantityForDish(maMonAn: String): Int? {
        return cartItems.find { it.maMonAn == maMonAn }?.soLuong
    }
    private fun formatPrice(price: Double?): String {
        val formatter = DecimalFormat("#,###") // Định dạng với dấu phẩy
        return price?.let { formatter.format(it) } ?: "N/A"
    }
}