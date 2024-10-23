import com.example.projectdatmonan.Model.ListMonAn

data class DatHang(
    val maDatHang: String? = null,
    val maNguoiDung: String? = null,
    val diaChiGiaoHang: String? = null,
    val tinhTrang: String? = null,
    val ngayGioDat: String? = null,
    val sdt: String? = null,
    val listMonAn: List<ListMonAn>? = null
)