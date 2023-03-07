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
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.jefferson.application.br.R
import com.jefferson.application.br.model.PickerModel

class FilePickerAdapter(
    var models: List<PickerModel>, private val context: Context, private val itemType: Int
) : BaseAdapter() {
    private val mLayoutInflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    var selectedItem = -1
        set(i) {
            field = i
            notifyDataSetChanged()
        }

    fun update(list: List<PickerModel>) {
        models = list
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return models.size
    }

    override fun getItem(i: Int): PickerModel {
        return models[i]
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }

    override fun getView(position: Int, view: View?, viewGroup: ViewGroup): View? {
        @Suppress("NAME_SHADOWING") var view = view
        val holder: Holder
        if (view == null) {
            view = mLayoutInflater.inflate(R.layout.file_picker_item, viewGroup, false)
            holder = Holder()
            holder.checkOverlay = view.findViewById(R.id.folder_picker_check_overlay)
            holder.titleLabel = view.findViewById(R.id.item_name)
            holder.sizeLabel = view.findViewById(R.id.item_size)
            holder.imageView = view.findViewById(R.id.tumbView)
            view.tag = holder
        } else {
            holder = view.tag as Holder
        }
        val pickerModel = models[position]
        val selected = position == selectedItem
        val color =
            if (selected) ContextCompat.getColor(context, R.color.colorAccent) else attrCommonColor
        val size = pickerModel.size
        val resId =
            if (itemType == 1) R.plurals.video_total_plural else R.plurals.imagem_total_plural
        val folderSize = context.resources.getQuantityString(resId, size, size)

        holder.checkOverlay?.visibility = if (selected) View.VISIBLE else View.GONE
        holder.titleLabel?.setTextColor(color)
        holder.titleLabel?.text = pickerModel.name
        holder.sizeLabel?.text = folderSize
        Glide.with(context).load("file://" + pickerModel.thumbnailPath).centerCrop()
            .into(holder.imageView!!)
        return view
    }

    class Holder {
        var imageView: ImageView? = null
        var titleLabel: TextView? = null
        var sizeLabel: TextView? = null
        var checkOverlay: View? = null
    }

    private val attrCommonColor: Int
        get() {
            val typedValue = TypedValue()
            val theme = context.theme
            theme.resolveAttribute(R.attr.commonColorLight, typedValue, true)
            return typedValue.data
        }
}