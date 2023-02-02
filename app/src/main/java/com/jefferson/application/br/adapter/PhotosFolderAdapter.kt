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

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.jefferson.application.br.R
import com.jefferson.application.br.activity.ImportGalleryActivity
import com.jefferson.application.br.activity.SelectionActivity
import com.jefferson.application.br.model.FolderModel

class PhotosFolderAdapter(
    private val mGalleryAlbum: ImportGalleryActivity,
    private var items: ArrayList<FolderModel>,
    private val option: Int
) : ArrayAdapter<FolderModel>(
    mGalleryAlbum, R.layout.adapter_photosfolder, items
) {
    fun set(newItems: ArrayList<FolderModel>) {
        items = newItems
        if (newItems.size > 0) notifyDataSetChanged() else notifyDataSetInvalidated()
    }

    override fun getCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getViewTypeCount(): Int {
        return if (items.size > 0) {
            items.size
        } else {
            1
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        @Suppress("NAME_SHADOWING")
        var convertView = convertView
        val mViewHolder: ViewHolder
        if (convertView == null) {
            mViewHolder = ViewHolder()
            convertView =
                LayoutInflater.from(context).inflate(R.layout.adapter_photosfolder, parent, false)
            mViewHolder.folderNameLabel = convertView.findViewById<View>(R.id.tv_folder) as TextView
            mViewHolder.itemCountLabel = convertView.findViewById<View>(R.id.tv_folder2) as TextView
            mViewHolder.imageView = convertView.findViewById<View>(R.id.iv_image) as ImageView
            mViewHolder.parentLayout =
                convertView.findViewById<View>(R.id.adapter_photosfolderParentView) as RelativeLayout
            //mViewHolder.smallView = (ImageView) convertView.findViewById(R.id.folder_small_icon_view);
            mViewHolder.parentLayout?.setOnClickListener {
                val intent = Intent(mGalleryAlbum, SelectionActivity::class.java)
                intent.putExtra("name", items[position].name)
                intent.putExtra("data", items[position].items)
                intent.putExtra("type", mGalleryAlbum.type)
                intent.putExtra("position", option)
                mGalleryAlbum.startActivityForResult(intent, ImportGalleryActivity.GET_CODE)
            }
            convertView.tag = mViewHolder
        } else {
            mViewHolder = convertView.tag as ViewHolder
        }
        val item = items[position]
        mViewHolder.folderNameLabel?.text = item.name
        mViewHolder.itemCountLabel?.text = item.items.size.toString()

        if (item.items.size != 0) {
            Glide.with(mGalleryAlbum).load("file://" + item.items[0])
                .skipMemoryCache(true)
                .into(mViewHolder.imageView!!)
        }
        return convertView!!
    }

    private class ViewHolder {
        var folderNameLabel: TextView? = null
        var itemCountLabel: TextView? = null
        var imageView: ImageView? = null
        var parentLayout: RelativeLayout? = null
    }
}