import com.example.projectdatmonan.Model.ListMonAn

data class DatHang1(
    val listMonAn: List<ListMonAn>? = null,
    val maDatHang: String? = null, // Thêm mã đặt hàng
    val maNguoiDung: String? = null,
    val diaChiGiaoHang: String? = null,
    val tinhTrang: String? = null,
    val ngayGioDat: String? = null,
    val sdt: String? = null,
    val tongTien: Long?=null,

    )
