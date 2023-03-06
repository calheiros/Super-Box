package com.jefferson.application.br.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jefferson.application.br.adapter.MultiSelectRecyclerViewAdapter
import com.jefferson.application.br.adapter.MultiSelectRecyclerViewAdapter.ViewHolder.ClickListener
import com.jefferson.application.br.R
import com.jefferson.application.br.model.MediaModel
import com.jefferson.application.br.util.BlurUtils
import eightbitlab.com.blurview.BlurView

class SelectionActivity : MyCompatActivity(), ClickListener, View.OnClickListener {
    private lateinit var adapter: MultiSelectRecyclerViewAdapter
    private var name: String? = null
    private var selectAllImageView: ImageView? = null
    private var models: ArrayList<MediaModel>? = null
    private var baseTitle: String? = null
    private var selectAllTextView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gallery_selection_layout)
        val importLayout = findViewById<View>(R.id.importView)
        val recyclerView = findViewById<View>(R.id.my_recycler_view) as RecyclerView
        selectAllTextView = findViewById<View>(R.id.options_selectTextView) as TextView
        val blurView = findViewById<BlurView>(R.id.blurView)
        val selectAllLayout = findViewById<View>(R.id.selectView)
        BlurUtils.setupWith(blurView, this, 13f)

        name = intent.getStringExtra("name")
        models = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            intent.getParcelableArrayListExtra("data", MediaModel::class.java)
        else @Suppress("DEPRECATION") intent.getParcelableArrayListExtra("data")
        val position = intent.getIntExtra("position", 0)
        val mLayoutManager = GridLayoutManager(this, 3)

        recyclerView.layoutManager = mLayoutManager
        adapter = MultiSelectRecyclerViewAdapter(this@SelectionActivity, models!!, this, position)
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)
        selectAllImageView = findViewById<View>(R.id.selectImageView) as ImageView
        importLayout.setOnClickListener(this)
        selectAllLayout.setOnClickListener(this)

        setupToolbar()
        updateActionBarTitle()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.selectView -> {
                if (adapter.selectedItemCount == models?.size) {
                    adapter.clearSelection()
                } else {
                    var i = 0
                    while (i < models!!.size) {
                        if (!adapter.isSelected(i)) {
                            adapter.toggleItemSelected(i, false)
                        }
                        i++
                    }
                    if (i > 0) {
                        adapter.notifyDataSetChanged()
                    }
                }
                updateActionBarTitle()
                toggleSelectViewIcon()
            }
            R.id.importView -> {
                val listPaths: ArrayList<String> = adapter.selectedItems
                if (listPaths.size > 0) {
                    val i = Intent()
                    i.putStringArrayListExtra("selection", listPaths)
                    setResult(RESULT_OK, i)
                    finish()
                } else {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.selecionar_um),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return true
    }

    private fun updateItem(position: Int) {
        adapter.toggleItemSelected(position, true)
        updateActionBarTitle()
        toggleSelectViewIcon()
    }

    override fun onItemClicked(position: Int, v: View?) {
        updateItem(position)
    }

    override fun onItemLongClicked(position: Int): Boolean {
        updateItem(position)
        return true
    }

    private fun setupToolbar() {
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun updateActionBarTitle() {
        if (baseTitle == null) baseTitle =
            if (name!!.length >= 20) name!!.substring(0, 20) + "... ( %d )" else "$name ( %d )"
        val count = adapter.selectedItemCount
        supportActionBar?.title = String.format(
            baseTitle!!, count
        )
    }

    private fun toggleSelectViewIcon() {
        val allSelected = adapter.selectedItemCount == models?.size
        val text = if (allSelected) "Unselect all" else "Select all"
        val resId = if (allSelected) R.drawable.ic_select else R.drawable.ic_select_all
        selectAllImageView?.setImageResource(resId)
        selectAllTextView?.text = text
    }
}