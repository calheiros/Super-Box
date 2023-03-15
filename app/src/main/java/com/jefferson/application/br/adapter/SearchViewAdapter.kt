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
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.jefferson.application.br.R
import com.jefferson.application.br.model.SimpleAlbumModel
import java.util.*

class SearchViewAdapter(
    private val items: ArrayList<SimpleAlbumModel>,
    private val context: Context
) : BaseAdapter() {
    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val filteredItems: ArrayList<SimpleAlbumModel> = ArrayList()

    init {
        //when unfiltered, shows all items in alphabetical order.
        sort(items)
        filteredItems.addAll(items)
    }

    private fun sort(albums: ArrayList<SimpleAlbumModel>?) {
        albums?.sortWith { o1, o2 ->
            o1.albumName.lowercase(Locale.getDefault()).compareTo(
                o2.albumName.lowercase(
                    Locale.getDefault()
                )
            )
        }
    }

    override fun getCount(): Int {
        return filteredItems.size
    }

    override fun getItem(position: Int): SimpleAlbumModel {
        return filteredItems[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var tempConvertView: View? = convertView
        var holder = Holder()
        val item = getItem(position)
        if (convertView == null) {
            tempConvertView = inflater.inflate(R.layout.search_item_layout, parent, false)
            holder.textView = tempConvertView.findViewById(R.id.search_item_text_view)
            holder.thumbView = tempConvertView.findViewById(R.id.thumb_view)
            tempConvertView.tag = holder
        } else {
            holder = convertView.tag as Holder
        }
        holder.textView?.text = item.albumName
        Glide.with(context).load(item.thumbnailPath).into(holder.thumbView!!)
        return tempConvertView!!
    }

    fun filter(keyword: String) {
        filteredItems.clear()
        if (keyword.isEmpty()) {
            filteredItems.addAll(items)
        } else {
            for (item in items) {
                if (item.albumName.lowercase(Locale.getDefault())
                        .startsWith(keyword.lowercase(Locale.getDefault()))
                ) {
                    filteredItems.add(item)
                }
            }
        }
        notifyDataSetChanged()
    }

    private class Holder {
        var thumbView: ImageView? = null
        var textView: TextView? = null
    }
}