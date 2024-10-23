package com.example.projectdatmonan

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectdatmonan.Database.CRUD_MonAn
import com.example.projectdatmonan.Model.MonAn
import com.google.android.material.floatingactionbutton.FloatingActionButton

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
class FragmentQuanLyMonAn : Fragment() {
    private var recyclerView: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_quan_ly_mon_an, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val addButton: FloatingActionButton? = getView()?.findViewById(R.id.addMonAnBtn)
        val searchBar:SearchView? = getView()?.findViewById(R.id.search)
        getData("All")
        addButton?.setOnClickListener {
            val intent = Intent(this.context, ThemMonAnActivity::class.java)
            val loaiMonAnID = arguments?.getString("LoaiMonAnID")
            intent.putExtra("loaiMonAnID",loaiMonAnID)
            startActivity(intent)
        }

        searchBar?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query == "" || query == null) {
                    getData("All")
                } else {
                    getData(query)
                }

                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText==""){
                    getData("All")
                }
                return true
            }
        })


    }

    override fun onStart() {
        super.onStart()
        getData("All")
    }

    private fun getData(filter:String){
        recyclerView = view?.findViewById(R.id.recyclerViewMonAn)
        val progressBar: ProgressBar? = view?.findViewById(R.id.progressBarMonAn)
        val addButton: FloatingActionButton? = view?.findViewById(R.id.addMonAnBtn)
        if (progressBar != null) {
            progressBar.visibility = View.VISIBLE
        }
        if (addButton != null) {
            addButton.visibility = View.GONE
        }
        val loaiMonAnID = arguments?.getString("LoaiMonAnID")
        val db = CRUD_MonAn()

        if (loaiMonAnID != null) {
            if (loaiMonAnID=="All") {
                db.getAllMonAn { list ->
                    recyclerView?.layoutManager = LinearLayoutManager(this.context)
                    val newList:HashMap<String?, MonAn?>? = if (filter!="All") {
                        list?.filter { it.value?.tenMonAn?.contains(filter)!! }
                            ?.let { HashMap(it) }
                    } else {
                        list
                    }
                    recyclerView?.adapter = MonAnAdminAdapter(newList!!)
                    if (progressBar != null) {
                        progressBar.visibility = View.GONE
                    }
                    if (addButton != null) {
                        addButton.visibility = View.VISIBLE
                    }

                }
            }
            else {
                db.getMonAnTheoLoai(loaiMonAnID){list->
                    recyclerView?.layoutManager = LinearLayoutManager(this.context)
                    recyclerView?.adapter = list?.let { MonAnAdminAdapter(it) }
                    if (progressBar != null) {
                        progressBar.visibility = View.GONE
                    }
                    if (addButton != null) {
                        addButton.visibility = View.VISIBLE
                    }

                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FragmentQuanLyMonAn().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}