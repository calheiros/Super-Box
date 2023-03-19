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
package com.jefferson.application.br.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jefferson.application.br.R
import com.jefferson.application.br.adapter.MultiSelectRecyclerViewAdapter.ViewHolder.ClickListener
import com.jefferson.application.br.model.MediaModel

class MultiSelectRecyclerViewAdapter(
    private val context: Context, var items: ArrayList<MediaModel>,
    private val clickListener: ClickListener, private val mediaType: Int
) : SelectableAdapter<MultiSelectRecyclerViewAdapter.ViewHolder?>() {

    fun toggleItemSelected(position: Int, notifyAll: Boolean) {
        toggleSelection(position, notifyAll)

    }

    val selectedItems: ArrayList<String>
        get() {
            val selectedItemsPath = ArrayList<String>()
            for (position in getSelectedItems()) {
                val path: String? = items[position].path
                if (path != null) selectedItemsPath.add(path)
            }
            return selectedItemsPath
        }

    val listItemsPath: ArrayList<String>
        get() {
            val arrayListPath = ArrayList<String>()
            for (mm in items) {
                if (mm.path != null) {
                    arrayListPath.add(mm.path as String)
                }
            }
            return arrayListPath
        }

    fun updateVideoDuration(path: String, time: String?) {
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
        items.removeAll(deletionList.toSet())
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
        val rootView = LayoutInflater.from(parent.context).inflate(
            R.layout.recycler_view_item,
            parent, false
        )
        return ViewHolder(rootView, clickListener)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val mediaDuration = items[position].duration
        Glide.with(context).load("file://" + items[position].path)
            .skipMemoryCache(false).dontAnimate().into(viewHolder.imageView)
        if (mediaDuration != null) {
            viewHolder.durationLabel.visibility = View.VISIBLE
            viewHolder.durationLabel.text = mediaDuration
        }
        val isSelected = isSelected(position)
        viewHolder.selectionModeOverlay.visibility = if (isSelected) View.VISIBLE else View.INVISIBLE
        
        if (isSelected) {
            val realPosition = (getSelectedItemPosition(position) + 1).toString()
            viewHolder.selectedCountLabel.text = realPosition
        }
        val transitionName = "image_$position"
        ViewCompat.setTransitionName(viewHolder.rootView, transitionName)
        viewHolder.rootView.tag = transitionName
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun removeAt(position: Int) {
        if (position in 0 until itemCount) {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    class ViewHolder(var rootView: View, private val listener: ClickListener?) :
        RecyclerView.ViewHolder(rootView), View.OnClickListener, OnLongClickListener {
        val selectionModeOverlay: View = rootView.findViewById(R.id.item_selected_overlay)
        val selectedCountLabel: TextView = rootView.findViewById(R.id.selected_item_count_label)
        var imageView: ImageView = rootView.findViewById<View>(R.id.album_image_view) as ImageView
        var durationLabel: TextView = rootView.findViewById(R.id.gridview_itemTextView)

        init {
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