package com.example.projectdatmonan

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectdatLoaiMonAn.Database.CRUD_LoaiLoaiMonAn

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class FragmentQuanLyLoaiMonAn : Fragment(), LoaiMonAnAdminAdapter.OnItemClickListener {
    private var recyclerView: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_quan_ly_loai_mon_an, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = getView()?.findViewById(R.id.recyclerViewLoaiMonAn)
        val progressBar = getView()?.findViewById<ProgressBar>(R.id.progressBar)
        if (progressBar != null) {
            progressBar.visibility = View.VISIBLE
        }
        val db = CRUD_LoaiLoaiMonAn()
        db.getAllLoaiLoaiMonAn { mapLoaiMonAn ->
            recyclerView?.layoutManager = GridLayoutManager(this.context, 2)
            recyclerView?.adapter = mapLoaiMonAn?.let { LoaiMonAnAdminAdapter(it, this) }
            if (progressBar != null) {
                progressBar.visibility = View.GONE
            };
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

    override fun onItemClick(key: String) {
        val fragmentQuanLyMonAn = FragmentQuanLyMonAn()

        val bundle = Bundle()
        bundle.putString("LoaiMonAnID", key)
        fragmentQuanLyMonAn.arguments = bundle

        val transaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragmentQuanLyMonAn)
        transaction.addToBackStack(null)
        transaction.commit()

    }
}