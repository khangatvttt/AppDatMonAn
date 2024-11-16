package com.example.projectdatmonan


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectdatmonan.Database.CRUD_DanhGia
import com.example.projectdatmonan.Database.CRUD_MonAn
import com.example.projectdatmonan.Database.CRUD_NguoiDung
import com.example.projectdatmonan.Model.DanhGia
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class FragmentXemDanhGia:Fragment() {

    lateinit var recyclerView:RecyclerView
    lateinit var listDanhGia:List<DanhGia>
    lateinit var filteredList:List<DanhGia>
    var startFilter:List<Int> = listOf()
    var monAnFilter:String = "Tất cả"
    lateinit var adapter: DanhGiaAdminAdapter
    var sortType:Int = 3

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_feedback, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.recyclerViewDanhGiaAdmin)
        val cbbMonAn:AutoCompleteTextView = view.findViewById(R.id.CbbMonAn)
        val cbbSapXep:AutoCompleteTextView = view.findViewById(R.id.sapXepCbb)
        val progressBar:ProgressBar = view.findViewById(R.id.loadingFeedback)

        progressBar.visibility = View.VISIBLE

        val db = CRUD_DanhGia()
        val dbMonAN = CRUD_MonAn()
        val dbNguoiDung = CRUD_NguoiDung()
        db.getAllDanhGia { listDanhGiaData->
            if (listDanhGiaData==null)
                return@getAllDanhGia
            dbMonAN.getAllMonAn { mapMonAn->
                listDanhGiaData.forEach { danhGia ->
                    danhGia.maMonAn = mapMonAn?.get(danhGia.maMonAn)?.tenMonAn }
                listDanhGia = listDanhGiaData

                dbNguoiDung.getAllNguoiDung { mapNguoiDung->
                    listDanhGia.forEach { danhGia ->
                        danhGia.maNguoiDung = mapNguoiDung?.get(danhGia.maNguoiDung)?.hoTen+";"+mapNguoiDung?.get(danhGia.maNguoiDung)?.avatarUrl }

                    //Thêm data combobox món ăn
                    val dataMonAn: List<String?> = listDanhGia.map { it.maMonAn }
                    val sorted = dataMonAn.filterNotNull().distinct().sortedBy { it }
                    val updatedList = mutableListOf("Tất cả")
                    updatedList.addAll(sorted)
                    val loaiMonAn: ArrayAdapter<String?> =
                        ArrayAdapter<String?>(requireContext(), R.layout.list_item,
                            updatedList as List<String?>
                        )

                    filteredList=listDanhGia
                    cbbMonAn.setAdapter(loaiMonAn)
                    cbbMonAn.setText("Tất cả", false)

                    recyclerView.layoutManager = LinearLayoutManager(this.context)
                    adapter = DanhGiaAdminAdapter(listDanhGia)
                    recyclerView.adapter = adapter

                    sortDanhGia(sortType)
                    progressBar.visibility = View.GONE

                }

            }

        }

        val sapXep: ArrayAdapter<String?> =
            ArrayAdapter<String?>(requireContext(), R.layout.list_item,
                listOf(
                    "Số sao ↓",
                    "Số sao ↑",
                    "Thời gian ↓",
                    "Thời gian ↑"
                )
            )

        cbbSapXep.setAdapter(sapXep)
        cbbSapXep.setText("Thời gian ↓", false)


        val chipGroup:ChipGroup = view.findViewById(R.id.chipGroup)

        chipGroup.setOnCheckedStateChangeListener{ group, checkedIds ->
            val selectedNumbers = mutableListOf<Int>()

            // Map checked chip IDs to their corresponding numbers
            checkedIds.forEach { id ->
                val chip: Chip? = view.findViewById(id)
                chip?.let {
                    selectedNumbers.add(it.text.first().toString().toInt())
                }
            }

            startFilter = selectedNumbers

            filteredList = if (startFilter.isEmpty()) {
                listDanhGia
            } else {
                // Filter the original list based on selected numbers
                listDanhGia.filter { item ->
                    selectedNumbers.contains(item.soSao?.toInt())
                }
            }

            if (monAnFilter!="Tất cả"){
                filteredList = filteredList.filter { it.maMonAn==monAnFilter }
            }

            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            filteredList = filteredList.sortedByDescending {
                LocalDateTime.parse(it.thoiGian, formatter)
            }

            sortDanhGia(sortType)
            adapter.updateData(filteredList)
        }

        cbbMonAn.setOnItemClickListener { parent, view, position, id ->
            val selectedItem = parent.getItemAtPosition(position) as String

            monAnFilter = selectedItem

            filteredList = if (startFilter.isEmpty()) {
                listDanhGia
            } else {
                listDanhGia.filter { item ->
                    startFilter.contains(item.soSao?.toInt())
                }
            }

            if (monAnFilter!="Tất cả"){
                filteredList = filteredList.filter { it.maMonAn==monAnFilter }
            }

            sortDanhGia(sortType)
            adapter.updateData(filteredList)
        }

        cbbSapXep.setOnItemClickListener { parent, view, position, _ ->
            val selectedItem = parent.getItemAtPosition(position) as String

            when (selectedItem){
                "Số sao ↓"-> {
                    sortType = 1
                }
                "Số sao ↑"-> {
                    sortType = 2
                }
                "Thời gian ↓"-> {
                    sortType = 3
                }
                "Thời gian ↑"-> {
                    sortType = 4
                }
            }

            sortDanhGia(sortType)
        }

    }

    private fun sortDanhGia(sortType:Int){
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        when (sortType){
            1-> {
                filteredList = filteredList.sortedByDescending { it.soSao }
            }
            2-> {
                filteredList = filteredList.sortedBy { it.soSao }

            }
            3-> {
                filteredList = filteredList.sortedByDescending {
                    LocalDateTime.parse(it.thoiGian, formatter)
                }
            }
            4-> {
                filteredList = filteredList.sortedBy {
                    LocalDateTime.parse(it.thoiGian, formatter)
                }
            }
        }
        adapter.updateData(filteredList)
    }
}