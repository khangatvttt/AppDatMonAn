package com.example.projectdatmonan

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.bumptech.glide.Glide
import com.example.projectdatmonan.Database.CRUD_MonAn
import com.example.projectdatmonan.Model.MonAn
import java.text.NumberFormat
import java.util.Locale


class MonAnAdminAdapter(private val foodMap: HashMap<String?,MonAn?>) :
    RecyclerView.Adapter<MonAnAdminAdapter.MonAnViewHolder>() {
    private var entries = foodMap.entries.toMutableList().sortedBy { it.value?.tenMonAn?.lowercase() }
    private var context: Context? = null

    class MonAnViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(context: Context, monAnItem: MonAn, key:String, adapter: MonAnAdminAdapter, position: Int) {
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
                SweetAlertDialog(itemView.context, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Xác nhận xóa")
                    .setContentText("Bạn có chắc chắn muốn xóa món ${monAnItem.tenMonAn}? Bạn không thể hoàn tác hành động này.")
                    .setCancelText("Hủy")
                    .setConfirmText("Xóa")
                    .showCancelButton(true)
                    .setConfirmClickListener { sDialog ->
                        sDialog.dismissWithAnimation()
                        val loadingDialog = SweetAlertDialog(itemView.context, SweetAlertDialog.PROGRESS_TYPE)
                        loadingDialog.setTitleText("Đang xóa...")
                        loadingDialog.setCancelable(false)
                        loadingDialog.show()

                        val db = CRUD_MonAn()
                        db.deleteMonAn(key){success->
                            if (success){
                                loadingDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
                                loadingDialog.setTitleText("Xóa thành công!")
                                loadingDialog.setContentText("Món ăn đã được xóa.")
                                loadingDialog.setConfirmText("OK")
                                loadingDialog.setConfirmClickListener { successDialog ->
                                    successDialog.dismissWithAnimation()
                                }
                                adapter.removeItem(position)
                            }
                            else{
                                loadingDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE)
                                loadingDialog.setTitleText("Xóa thất bại!")
                                loadingDialog.setContentText("Đã xảy ra lỗi khi xóa món ăn.")
                                loadingDialog.setConfirmText("OK")
                                loadingDialog.setConfirmClickListener { failDialog ->
                                    failDialog.dismissWithAnimation()
                                }
                            }
                        }
                    }
                    .setCancelClickListener { sDialog ->
                        sDialog.cancel()
                    }
                    .show()

            }

            itemView.findViewById<ImageButton>(R.id.btnSuaMonAn).setOnClickListener {
                val intent = Intent(context, SuaMonAnActivity::class.java)
                intent.putExtra("MonAnID",key)
                context.startActivity(intent)

            }

        }

        private fun formatCurrencyVND(amount: Int): String {
            val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
            return formatter.format(amount) + " VNĐ"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonAnViewHolder {
        context = parent.context
        val view = LayoutInflater.from(parent.context).inflate(R.layout.mon_an_admin, parent, false)
        return MonAnViewHolder(view)
    }

    override fun onBindViewHolder(holder: MonAnViewHolder, position: Int) {
        val entry = entries[position]
        entry.value?.let { entry.key?.let { it1 -> context?.let { it2 -> holder.bind(it2, it, it1, this, position) } } }
    }

    override fun getItemCount(): Int {
        return entries.size
    }

    fun removeItem(position: Int) {
        entries = entries.toMutableList().also {
            it.removeAt(position)
        }
        notifyItemRemoved(position)
    }
}
