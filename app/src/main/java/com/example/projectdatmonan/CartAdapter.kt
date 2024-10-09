package com.example.projectdatmonan

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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

    inner class CartViewHolder(private val binding: ViewHolderCartBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ListMonAn) {
            binding.txtTitle.text = dishNames[item.maMonAn] ?: "Unknown Dish"
            binding.txtNumberItem.text = item.soLuong.toString()
            val price = dishPrices[item.maMonAn]
            binding.txtFee.text = price?.let { "$it VND" } ?: "N/A"
            val total = (item.soLuong ?: 0) * (price ?: 0.0)
            binding.txtTotalItem.text = "${total} VND"
            val imageUrl = dishImages[item.maMonAn]
            if (imageUrl != null) {
                Glide.with(binding.imgItemCart.context)
                    .load(imageUrl)
                    .into(binding.imgItemCart)
            } else {
                binding.imgItemCart.setImageResource(R.drawable.grey_bg)
            }

            binding.btnPlusCart.setOnClickListener {
                item.soLuong = (item.soLuong ?: 0) + 1
                updateQuantityInDatabase(item)
                notifyItemChanged(adapterPosition)
                onQuantityChanged()
            }

            binding.btnMinusCart.setOnClickListener {
                if ((item.soLuong ?: 0) > 1) {
                    item.soLuong = (item.soLuong ?: 0) - 1
                    updateQuantityInDatabase(item)
                    notifyItemChanged(adapterPosition)
                    onQuantityChanged()
                } else if ((item.soLuong ?: 0) == 1) {
                    removeItemFromCart(item)
                    onQuantityChanged()
                }
            }
            binding.btnDeleteCart.setOnClickListener{
                removeItemFromCart(item)
                notifyItemChanged(adapterPosition)
                onQuantityChanged()
            }

            itemView.setOnClickListener {
                onItemClicked(item)
            }
        }

        private fun updateQuantityInDatabase(item: ListMonAn) {
            val cartRef = FirebaseDatabase.getInstance().getReference("GioHang")
            cartRef.orderByChild("maNguoiDung").equalTo(maNguoiDung).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (cartSnapshot in snapshot.children) {
                            val listMonAnRef = cartSnapshot.child("listMonAn").ref
                            listMonAnRef.orderByChild("maMonAn").equalTo(item.maMonAn).addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(itemSnapshot: DataSnapshot) {
                                    if (itemSnapshot.exists()) {
                                        for (itemRef in itemSnapshot.children) {
                                            itemRef.ref.child("soLuong").setValue(item.soLuong)
                                                .addOnSuccessListener {
                                                    Log.d("Firebase", "Quantity updated successfully for user $maNguoiDung")
                                                    (itemView.context as? CartFragment)?.updateTotal()
                                                }
                                                .addOnFailureListener { error ->
                                                    Log.e("Firebase", "Failed to update quantity for user $maNguoiDung", error)
                                                }
                                        }
                                    } else {
                                        Log.d("Firebase", "Item not found for maMonAn: ${item.maMonAn}")
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.e("Firebase", "Error querying cart items: ${error.message}")
                                }
                            })
                        }
                    } else {
                        Log.d("Firebase", "No cart found for user: $maNguoiDung")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Error fetching user cart: ${error.message}")
                }
            })
        }

        private fun removeItemFromCart(item: ListMonAn) {
            val cartRef = FirebaseDatabase.getInstance().getReference("GioHang")
            cartRef.orderByChild("maNguoiDung").equalTo(maNguoiDung).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (cartSnapshot in snapshot.children) {
                            val listMonAnRef = cartSnapshot.child("listMonAn").ref
                            listMonAnRef.orderByChild("maMonAn").equalTo(item.maMonAn).addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(itemSnapshot: DataSnapshot) {
                                    if (itemSnapshot.exists()) {
                                        for (itemRef in itemSnapshot.children) {
                                            itemRef.ref.removeValue()
                                                .addOnSuccessListener {
                                                    Log.d("Firebase", "Item removed successfully from cart for user $maNguoiDung")
                                                    cartItems = cartItems.filter { it.maMonAn != item.maMonAn }
                                                    notifyDataSetChanged()
                                                    (itemView.context as? CartFragment)?.updateTotal()
                                                }
                                                .addOnFailureListener { error ->
                                                    Log.e("Firebase", "Failed to remove item from cart for user $maNguoiDung", error)
                                                }
                                        }
                                    } else {
                                        Log.d("Firebase", "Item not found for maMonAn: ${item.maMonAn}")
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.e("Firebase", "Error querying cart items: ${error.message}")
                                }
                            })
                        }
                    } else {
                        Log.d("Firebase", "No cart found for user: $maNguoiDung")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Error fetching user cart: ${error.message}")
                }
            })
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
}