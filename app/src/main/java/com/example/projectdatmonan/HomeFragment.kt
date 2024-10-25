package com.example.projectdatmonan


import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

import com.example.projectdatmonan.Database.CRUD_DanhGia
import com.example.projectdatmonan.Database.CRUD_LoaiMonAn

import com.example.projectdatmonan.Database.CRUD_MonAn
import com.example.projectdatmonan.Model.LoaiMonAn
import com.example.projectdatmonan.Model.MonAn
import com.google.firebase.database.*
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator

class HomeFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var monAnAdapter: MonAnAdapter
    private val monAnList = mutableListOf<MonAn>()
    private lateinit var progressBarPopular: ProgressBar
    private lateinit var loaiMonAnAdapter: LoaiMonAnAdapter
    private val loaiMonAnList = mutableListOf<LoaiMonAn>()
    private lateinit var progressBarFood: ProgressBar
    private lateinit var viewFood: RecyclerView
    private lateinit var viewPopular: RecyclerView
    private lateinit var textSeeAll: TextView
    private lateinit var viewPager: ViewPager2
    private lateinit var dotsIndicator: DotsIndicator
    private lateinit var sliderAdapter: SliderAdapter
    private lateinit var noFoodTextView: TextView
    private var selectedCategoryName: String? = null
    private val _category = MutableLiveData<List<LoaiMonAn>>()
    private val categoryIdMap = mutableMapOf<String, String>()
    private val crudMonAn = CRUD_MonAn()
    private val crudLoaiMonAn = CRUD_LoaiMonAn()
    private val crudDanhGia = CRUD_DanhGia()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        viewPopular = view.findViewById(R.id.viewPopular)
        progressBarPopular = view.findViewById(R.id.progressBarPopular)
        viewPopular.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        val searchEditText = view.findViewById<EditText>(R.id.searchEditText)
        val searchIcon = view.findViewById<ImageView>(R.id.imageView2)
        noFoodTextView = view.findViewById(R.id.txt_noFood)
        searchIcon.setOnClickListener {
            selectedCategoryName = null
            loaiMonAnAdapter.clearSelection()

            monAnAdapter.updateList(emptyList())
            if (searchEditText.visibility == View.GONE) {
                searchEditText.visibility = View.VISIBLE
                searchEditText.requestFocus()
                val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT)
            } else {
                searchEditText.visibility = View.GONE
                val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
            }
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterMonAnBySearch(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        database = FirebaseDatabase.getInstance().getReference().child("MonAn")

        monAnAdapter = MonAnAdapter(monAnList)
        viewPopular.adapter = monAnAdapter

        fetchMonAnData()

        viewFood = view.findViewById(R.id.viewFood)
        progressBarFood = view.findViewById(R.id.progressBarFood)
        viewFood.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        loaiMonAnAdapter = LoaiMonAnAdapter(loaiMonAnList) { categoryName ->
            selectedCategoryName = categoryName
            val categoryId = categoryIdMap[categoryName]
            filterMonAnByCategory(categoryId)
        }
        viewFood.adapter = loaiMonAnAdapter

        loadCategory()

        _category.observe(viewLifecycleOwner, Observer { categories ->
            loaiMonAnList.clear()
            loaiMonAnList.addAll(categories)
            loaiMonAnAdapter.notifyDataSetChanged()
            progressBarFood.visibility = View.GONE
        })

        viewPager = view.findViewById(R.id.viewpageSlider)
        dotsIndicator = view.findViewById(R.id.dotsIndicator)

        val images = intArrayOf(R.drawable.food_discount, R.drawable.images)
        sliderAdapter = SliderAdapter(requireContext(), images)
        viewPager.adapter = sliderAdapter
        dotsIndicator.setViewPager2(viewPager)

        textSeeAll = view.findViewById(R.id.textView62)
        textSeeAll.setOnClickListener {
            selectedCategoryName = null
            loaiMonAnAdapter.clearSelection()
            showAllMonAn()
        }

        return view
    }

    private fun showAllMonAn() {
        monAnAdapter.updateList(monAnList)
    }

    private fun filterMonAnBySearch(query: String) {
        selectedCategoryName = null
        loaiMonAnAdapter.clearSelection()
        val filteredList = monAnList.filter { monAn ->
            monAn.tenMonAn?.contains(query, ignoreCase = true) == true
        }
        if (filteredList.isEmpty()) {
            noFoodTextView.setText("Không tìm thấy món ăn phù hợp với tìm kiếm của bạn")
            noFoodTextView.visibility = View.VISIBLE
            viewPopular.visibility = View.GONE
        } else {
            noFoodTextView.visibility = View.GONE
            viewPopular.visibility = View.VISIBLE
            monAnAdapter.updateList(filteredList)
        }
    }

    private fun loadCategory() {
        progressBarFood.visibility = View.VISIBLE

        crudLoaiMonAn.loadCategory(
            onComplete = { categories, idMap ->
                _category.value = categories
                categoryIdMap.putAll(idMap)

                if (categories.isNotEmpty()) {
                    val firstCategoryId = categoryIdMap[categories[0].tenMonAn]
                    filterMonAnByCategory(firstCategoryId)
                }
            },
            onError = { errorMessage ->
                Log.e("Firebase", errorMessage)
                progressBarFood.visibility = View.GONE
            }
        )
    }

    private fun fetchMonAnData() {
        progressBarPopular.visibility = View.VISIBLE
        crudMonAn.fetchMonAnData(
            onDataFetched = { data ->
                monAnList.clear()
                for ((monAnId, monAn) in data) {
                    monAnList.add(monAn)
                    monAnAdapter.addMonAnWithId(monAnId, monAn)
                    crudDanhGia.fetchDanhGia(
                        monAnId,
                        onComplete = { ratings ->
                            updateAverageRating(ratings, monAn)
                        },
                        onError = { error ->
                            Log.e("Firebase", "Failed to read data: ${error.message}", error.toException())
                        }
                    )                }
                monAnAdapter.notifyDataSetChanged()
                progressBarPopular.visibility = View.GONE
            },
            onError = { error ->
                Log.e("Firebase", "Failed to read data", error.toException())
                progressBarPopular.visibility = View.GONE
            }
        )
    }

    private fun filterMonAnByCategory(categoryId: String?) {
        if (categoryId == null) return
        val filteredList = monAnList.filter { monAn ->
            monAn.loaiMonAn?.equals(categoryId, ignoreCase = true) == true
        }
        if (filteredList.isEmpty()) {
            noFoodTextView.visibility = View.VISIBLE
            viewPopular.visibility = View.GONE
        } else {
            noFoodTextView.visibility = View.GONE
            viewPopular.visibility = View.VISIBLE
            monAnAdapter.updateList(filteredList)
        }
    }

    private fun updateAverageRating(ratings: List<Int>, monAn: MonAn) {
        val average = if (ratings.isNotEmpty()) {
            ratings.average()
        } else {
            0.0
        }
        Log.d("UpdateAverageRating", "Món ăn: ${monAn.tenMonAn}, Điểm trung bình: $average")
        monAnAdapter.updateRating(monAn, average)
    }
}