package com.jefferson.application.br.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jefferson.application.br.R

class NotesFragment: Fragment() {
    private var contentView: View? = null
    private lateinit var recyclerView: RecyclerView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if(contentView == null) {
           contentView = inflater.inflate(R.layout.notes_layout, container, false)
            recyclerView = contentView?.findViewById(R.id.notes_recycler_view) as RecyclerView
            recyclerView.layoutManager = GridLayoutManager(activity, 2)
        }
        return contentView
    }
}