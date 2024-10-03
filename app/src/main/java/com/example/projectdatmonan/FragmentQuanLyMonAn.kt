package com.example.projectdatmonan

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectdatmonan.Database.CRUD_MonAn
import com.example.projectdatmonan.Model.MonAn

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
        recyclerView = getView()?.findViewById(R.id.recyclerViewMonAn)
        val progressBar: ProgressBar? = getView()?.findViewById(R.id.progressBarMonAn)
        if (progressBar != null) {
            progressBar.visibility = View.VISIBLE
        }
        val loaiMonAnID = arguments?.getString("LoaiMonAnID")
        val db = CRUD_MonAn()

        if (loaiMonAnID != null) {
            if (loaiMonAnID=="All") {
                db.getAllMonAn { list ->
                    recyclerView?.layoutManager = LinearLayoutManager(this.context)
                    recyclerView?.adapter = list?.let { MonAnAdminAdapter(it) }
                    if (progressBar != null) {
                        progressBar.visibility = View.GONE
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