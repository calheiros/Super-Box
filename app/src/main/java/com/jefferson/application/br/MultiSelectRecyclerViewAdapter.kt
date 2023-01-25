/*
 * Copyright (C) 2023 Jefferson Calheiros


 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jefferson.application.br

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jefferson.application.br.MultiSelectRecyclerViewAdapter.ViewHolder.ClickListener
import com.jefferson.application.br.model.MediaModel

class MultiSelectRecyclerViewAdapter(
    private val context: Context, var items: ArrayList<MediaModel>,
    private val clickListener: ClickListener, private val mediaType: Int
) : SelectableAdapter<MultiSelectRecyclerViewAdapter.ViewHolder?>() {
    fun toggleItemSelected(position: Int) {
        toggleSelection(position)
        for (i in selectedItemsHash.keys) {
            if (i != position) {
                notifyItemChanged(i)
            }
        }
    }

    private fun getItem(position: Int): Any {
        return items[position]
    }

    val selectedItemsPath: ArrayList<String>
        get() {
            val selectedItemsPath = ArrayList<String>()
            for (position in getSelectedItems()) {
                selectedItemsPath.add(items[position].path)
            }
            return selectedItemsPath
        }
    val listItemsPath: ArrayList<String>
        get() {
            val arrayListPath = ArrayList<String>()
            for (mm in items) {
                arrayListPath.add(mm.path)
            }
            return arrayListPath
        }

    fun updateItemDuration(path: String, time: String?) {
        for (i in items.indices) {
            val model = items[i]
            if (model.path == path) {
                model.duration = time
                notifyItemChanged(i)
                break
            }
        }
    }

    fun removeAll(paths: ArrayList<String?>) {
        val deletionList = ArrayList<MediaModel>()
        for (item in items) {
            if (paths.contains(item.path)) {
                deletionList.add(item)
            }
        }
        items.removeAll(deletionList)
        notifyDataSetChanged()
    }

    fun removeItem(path: String) {
        val iterator: Iterator<MediaModel> = items.iterator()
        var item: MediaModel? = null
        while (iterator.hasNext()) {
            item = iterator.next()
            if (item.path == path) {
                break
            }
        }
        item?.let { removeItem(it) }
    }

    fun removeItem(item: MediaModel) {
        val index = items.indexOf(item)
        if (index != -1) {
            items.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemLayoutView = LayoutInflater.from(parent.context).inflate(
            R.layout.generic_gridview_item,
            parent, false
        )
        return ViewHolder(itemLayoutView, clickListener)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val mediaDuration = items[position].duration
        Glide.with(App.getAppContext()).load("file://" + items[position].path)
            .skipMemoryCache(false).dontAnimate().into(viewHolder.imageView)
        if (mediaDuration != null) {
            viewHolder.durationLabel.visibility = View.VISIBLE
            viewHolder.durationLabel.text = mediaDuration
        }
        val isSelected = isSelected(position)
        viewHolder.selectionModeOverlay.visibility =
            if (isSelected) View.VISIBLE else View.INVISIBLE
        if (isSelected) {
            viewHolder.selectedCountLabel.text = (getSelectedItemPosition(position) + 1).toString()
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ViewHolder(rootView: View, private val listener: ClickListener?) :
        RecyclerView.ViewHolder(rootView), View.OnClickListener, OnLongClickListener {
        private val selectionModeOverlay: View
        private val selectedCountLabel: TextView
        var imageView: ImageView
        var durationLabel: TextView
        var smallView: ImageView

        init {
            imageView = rootView.findViewById<View>(R.id.image) as ImageView
            smallView = rootView.findViewById<View>(R.id.folder_small_icon_view) as ImageView
            durationLabel = rootView.findViewById(R.id.gridview_itemTextView)
            selectedCountLabel = rootView.findViewById(R.id.selected_item_count_label)
            selectionModeOverlay = rootView.findViewById(R.id.item_selected_overlay)
            rootView.setOnClickListener(this)
            rootView.setOnLongClickListener(this)
        }

        override fun onClick(v: View) {
            listener?.onItemClicked(adapterPosition, v)
        }

        override fun onLongClick(view: View): Boolean {
            return listener?.onItemLongClicked(adapterPosition) ?: false
        }

        interface ClickListener {
            fun onItemClicked(position: Int, v: View?)
            fun onItemLongClicked(position: Int): Boolean
        }
    }
}