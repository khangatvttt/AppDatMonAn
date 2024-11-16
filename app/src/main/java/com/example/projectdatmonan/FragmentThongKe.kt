package com.example.projectdatmonan

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.DatePicker
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import cn.pedant.SweetAlert.SweetAlertDialog
import com.anychart.APIlib
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.charts.Cartesian
import com.anychart.charts.Pie
import com.anychart.core.cartesian.series.Line
import com.example.projectdatdatHang.Database.CRUD_DatHang
import com.example.projectdatmonan.Database.CRUD_MonAn
import com.example.projectdatmonan.Model.MonAn
import com.google.android.material.textfield.TextInputEditText
import es.dmoral.toasty.Toasty
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit


class FragmentThongKe:Fragment(), DatePickerDialog.OnDateSetListener {

    private var calendar1 = Calendar.getInstance()
    private var calendar2 = Calendar.getInstance()
    private val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.US)
    private lateinit var tuNgay:TextInputEditText
    private lateinit var denNgay:TextInputEditText
    private var flagPickDate:Boolean = true
    private lateinit var pie:Pie
    private lateinit var cartesian:Cartesian
    private lateinit var tongSoMon:TextView
    private lateinit var tongDoanhThu:TextView
    private lateinit var chartView: AnyChartView
    private lateinit var chartViewLine: AnyChartView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_thong_ke, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        chartView = view.findViewById(R.id.pieChart2)
        APIlib.getInstance().setActiveAnyChartView(chartView)
        val pickTuNgay = view.findViewById<ImageButton>(R.id.iconButton1)
        val pickDenNgay = view.findViewById<ImageButton>(R.id.iconButton)
        tongSoMon = view.findViewById(R.id.tongSoMonText)
        tongDoanhThu = view.findViewById(R.id.tongdoanhthuText)


        pie = AnyChart.pie()
        tuNgay = view.findViewById(R.id.etTuNgay)
        denNgay = view.findViewById(R.id.etDenNgay)

        calendar1.add(Calendar.DAY_OF_MONTH, -30)

        tuNgay.setText(formatter.format(calendar1.timeInMillis))
        denNgay.setText(formatter.format(calendar2.timeInMillis))

        setUpPiechart(calendar1,calendar2,pie)



        pickTuNgay.setOnClickListener {
            flagPickDate = true
            DatePickerDialog(
                this.requireContext(),
                this,
                calendar1.get(Calendar.YEAR),
                calendar1.get(Calendar.MONTH),
                calendar1.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        pickDenNgay.setOnClickListener {
            flagPickDate = false
            DatePickerDialog(
                this.requireContext(),
                this,
                calendar2.get(Calendar.YEAR),
                calendar2.get(Calendar.MONTH),
                calendar2.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        chartView?.setChart(pie)


        chartViewLine = view.findViewById(R.id.lineChart)
        APIlib.getInstance().setActiveAnyChartView(chartViewLine)
        cartesian = AnyChart.line()
        APIlib.getInstance().setActiveAnyChartView(chartViewLine)
        setUpDoanhThu(calendar1,calendar2,cartesian)
        chartViewLine?.setChart(cartesian)
    }

    private fun setUpPiechart(tuNgay:Calendar, denNgay:Calendar, pie: Pie) {
        val dbDatHang = CRUD_DatHang()
        val dbMonAn = CRUD_MonAn()

        dbMonAn.getAllMonAn { mapMonAn ->
            if (mapMonAn == null) {
                showError("Lỗi khi load data")
                return@getAllMonAn
            }

            dbDatHang.getMonAnDaBan(tuNgay, denNgay) { listMonAn ->
                if (listMonAn == null) {
                    showError("Lỗi khi load data")
                    return@getMonAnDaBan
                }

                val sortedMonAnList = listMonAn.toList().sortedByDescending { (_, value) -> value }
                if (listMonAn.isNotEmpty()) {
                    val dataEntries = mutableListOf<DataEntry>()
                    sortedMonAnList.forEach { (maMonAn, soLuong) ->
                        val tenMonAn = mapMonAn[maMonAn]?.tenMonAn ?: "Unknown"
                        dataEntries.add(ValueDataEntry(tenMonAn, soLuong))
                    }
                    APIlib.getInstance().setActiveAnyChartView(chartView)
                    pie.background().fill("transparent")
                    pie.tooltip()
                        .titleFormat("{%X}")
                        .format("Số lượng: {%value}")

                    pie.data(dataEntries)
                    pie.title("Món ăn bán chạy")
                }
                else {
                    APIlib.getInstance().setActiveAnyChartView(chartView)
                    val dataEntriesEmpty = mutableListOf<DataEntry>()
                    dataEntriesEmpty.add(ValueDataEntry("Không có món nào đã bán", 0))
                    pie.data(dataEntriesEmpty)
                    pie.title("Không có món trong khoảng thời gian này")
                }
                val tongSoMonSL = sortedMonAnList.sumOf { (_,value)->value }
                tongSoMon.text = tongSoMonSL.toString()+" món ăn"
            }
        }
    }

    private fun showError(message: String) {
        Toasty.error(requireContext(), message, Toasty.LENGTH_SHORT).show()
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {

        if (flagPickDate) {
            calendar1.set(year,month,dayOfMonth)
            denNgay.setText("")
            tuNgay.setText(formatter.format(calendar1.timeInMillis))
        }
        else {
            calendar2.set(year,month,dayOfMonth)
            val diffInDays = TimeUnit.DAYS.convert(
                calendar2.timeInMillis-calendar1.timeInMillis,
                TimeUnit.MILLISECONDS)
            if (diffInDays<=0 || diffInDays>30){
                SweetAlertDialog(requireContext(), SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Lỗi")
                    .setContentText("Ngày không hợp lệ! Ngày kết thúc phải lớn hơn ngày bắt đầu và khoảng thời gian giữa hai ngày tối đa là 30 ngày.")
                    .setConfirmText("OK")
                    .show()
            }
            denNgay.setText(formatter.format(calendar2.timeInMillis))
            setUpPiechart(calendar1, calendar2, pie)
            setUpDoanhThu(calendar1, calendar2, cartesian)

        }
    }

    private fun setUpDoanhThu(calendar1: Calendar, calendar2: Calendar, cartesian:Cartesian){
        val db = CRUD_DatHang()
        db.getDoanhThu(calendar1,calendar2){mapDoanhThu->
            if (mapDoanhThu == null) {
                showError("Lỗi khi load data")
                return@getDoanhThu
            }

            val sortedMonAnList = mapDoanhThu.toList().sortedByDescending { (key, _) -> key }
            val tongDoanhThuSL = sortedMonAnList.sumOf { (_,value)->value }
            tongDoanhThu.text = convertMoney(tongDoanhThuSL)

            APIlib.getInstance().setActiveAnyChartView(chartViewLine)
            val calendarStart = calendar1.clone() as Calendar
            val calendarEnd = calendar2.clone() as Calendar
            calendarEnd.add(Calendar.DAY_OF_MONTH,1)
            cartesian.removeAllSeries()
            cartesian.title("Biểu đồ doanh thu")
            val seriesData: MutableList<DataEntry> = ArrayList()
            while (calendarStart.before(calendarEnd)) {
                val month = calendarStart.get(Calendar.MONTH) + 1
                val day = calendarStart.get(Calendar.DAY_OF_MONTH)

                val key = LocalDateTime.ofInstant(calendarStart.toInstant(), ZoneId.systemDefault()).toLocalDate()
                val value = mapDoanhThu.getOrDefault(key,0.0)/1000000
                seriesData.add(ValueDataEntry("$day/$month", value))
                calendarStart.add(Calendar.DAY_OF_MONTH, 1)
            }

            val series = cartesian.line(seriesData)
            series.name("Doanh thu")
            series.hovered().markers().enabled(true)
            cartesian.tooltip().format("Doanh thu: {%Value}{groupsSeparator: } tr")
            series.hovered().markers()
                .type("circle")
                .size(4)

            cartesian.xAxis(0).title("Ngày")
            cartesian.yAxis(0).labels().format("{%Value} tr")
            cartesian.yAxis(0).title("Doanh thu")

        }
    }

    fun formatYAxisLabels(value: Number): String {
        return when {
            value.toDouble() >= 1_000_000 -> "${(value.toDouble() / 1_000_000)}tr"
            else -> value.toString()
        }
    }

    private fun convertMoney(soTien: Double): String {
        return when {
            soTien >= 1_000_000_000 -> {
                String.format("%.0f tỉ VNĐ", soTien / 1_000_000_000)
            }
            soTien >= 1_000_000 -> {
                String.format("%.0f triệu VNĐ", soTien / 1_000_000)
            }
            soTien >= 1_000 -> {
                String.format("%.0f k VNĐ", soTien / 1_000)
            }
            else -> {
                String.format("%.0f VNĐ", soTien)
            }
        }
    }

}
