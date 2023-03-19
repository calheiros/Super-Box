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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jefferson.application.br.R
import com.jefferson.application.br.app.SimpleDialog
import com.jefferson.application.br.app.SimpleDialog.Companion.createMenuItems
import com.jefferson.application.br.fragment.AlbumFragment
import com.jefferson.application.br.model.SimpleAlbumModel
import com.jefferson.application.br.util.AlbumUtils

class AlbumAdapter(
    private val fragment: AlbumFragment,
    var models: ArrayList<SimpleAlbumModel>,
) : RecyclerView.Adapter<AlbumAdapter.ViewHolder>() {
    private var group: View? = null
    private var itemToHighlight = -1


    fun getItem(itemPosition: Int): SimpleAlbumModel? {
        return if (itemPosition in (0 until itemCount)) {
            models[itemPosition]
        } else null
    }

    fun insertItem(item: SimpleAlbumModel) {
        models.add(item)
        SimpleAlbumModel.sort(models)
        val position = models.indexOf(item)
        if (position != -1) {
            notifyItemInserted(position)
            fragment.scrollTo(position)
            fragment.onItemsChanged(itemCount)
        } else {
            notifyDataSetChanged()
        }
    }

    fun removeItem(position: Int) {
        if (position in (0 until itemCount)) {
            models.removeAt(position)
            fragment.onItemsChanged(itemCount)
            notifyItemRemoved(position)
        } else {
            Toast.makeText(
                fragment.requireContext(),
                "Can not remove item at: $position",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun updateModels(
        newAlbumModels: ArrayList<SimpleAlbumModel>,
    ) {
        models = newAlbumModels
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, p2: Int): ViewHolder {
        group = parent
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.adapter_photosfolder, parent, false)
        return ViewHolder(view)
    }

    fun notifyItemChanged(albumModel: SimpleAlbumModel) {
        for (i in 0 until itemCount) {
            if ((albumModel.albumPath == getItem(i)?.albumPath)) {
                notifyItemChanged(i)
                return
            }
        }
    }

    fun removeItem(item: SimpleAlbumModel) {
        val key = models.indexOf(item)
        if (key != -1) {
            models.removeAt(key)
            notifyItemRemoved(key)
        } else {
            Toast.makeText(
                fragment.requireContext(),
                "Could not find folder index for item " + item.albumName,
                Toast.LENGTH_LONG
            ).show()
        }
        fragment.onItemsChanged(itemCount)
    }

    fun getAlbumPositionByPath(path: String): Int {
        for (i in models.indices) {
            val model = models[i]
            if ((model.albumPath == path)) {
                return i
            }
        }
        return -1
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = models[position]
        holder.folderName.text = model.albumName
        holder.folderSize.text = model.itemCount.toString()
        val isEmpty = model.itemCount == 0
        holder.favoriteView.visibility = if (model.isFavorite) View.VISIBLE else View.GONE
        if (!isEmpty) {
            Glide.with(fragment.requireContext()).load(
                "file://" + model.thumbnailPath
            ).skipMemoryCache(true).into(holder.imageView)
        } else {
            holder.imageView.setImageBitmap(null)
            holder.smallView.setImageResource(R.drawable.ic_image_broken_variant)
        }
        val visibility = if ((isEmpty)) View.VISIBLE else View.GONE
        if (holder.smallView.visibility != visibility) {
            holder.smallView.visibility = visibility
        }
        holder.parent.setOnClickListener { fragment.openAlbum(model, holder.cardView) }
        holder.parent.setOnLongClickListener { view ->
            val context = view.context
            val options = arrayOf(
                context.getString(R.string.renomear),
                context.getString(R.string.apagar),
                if (model.isFavorite) "Remove from favorites" else "Add to Favorites"
            )
            val icons = intArrayOf(
                R.drawable.ic_rename, R.drawable.ic_delete_outline,
                if (model.isFavorite) R.drawable.ic_bookmark_remove_outline
                else R.drawable.ic_bookmark_add_outline
            )
            val dialog = SimpleDialog(fragment.requireActivity())
            dialog.setMenuItems(
                createMenuItems(options, icons),
                DialogMenuListener(model, dialog)
            )
            dialog.show()
            false
        }
        if (position == itemToHighlight) {
            holder.itemView.startAnimation(
                AnimationUtils.loadAnimation(
                    fragment.context, R.anim.blink
                )
            )
            itemToHighlight = -1
        }
    }

    override fun getItemCount(): Int {
        return models.size
    }

    fun getItemPositionByName(albumName: String): Int {
        for (i in models.indices) {
            val model = models[i]
            if ((model.albumName == albumName)) {
                return i
            }
        }
        return -1
    }

    private fun getAlbumPositionByPath(f_model: SimpleAlbumModel): Int {
        for (i in 0 until itemCount) {
            if ((getItem(i) == f_model)) {
                notifyItemChanged(i)
                return i
            }
        }
        return -1
    }

    fun setItemToHighlight(position: Int) {
        itemToHighlight = position
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: View
        var folderName: TextView
        var folderSize: TextView
        var imageView: ImageView
        var favoriteView: ImageView
        var parent: RelativeLayout
        var smallView: ImageView

        init {
            cardView = view.findViewById(R.id.card_view)
            folderName = view.findViewById(R.id.tv_folder)
            folderSize = view.findViewById(R.id.folder_size_label)
            imageView = view.findViewById(R.id.iv_image)
            parent = view.findViewById(R.id.adapter_photosfolderParentView)
            smallView = view.findViewById(R.id.folder_small_icon_view)
            favoriteView = view.findViewById(R.id.folder_favorite_icon_view)
        }
    }

    private inner class DialogMenuListener(
        var f_model: SimpleAlbumModel,
        var dialog: SimpleDialog
    ) :
        OnItemClickListener {
        override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
            when (position) {
                0 -> fragment.inputFolderDialog(f_model, AlbumFragment.ACTION_RENAME_FOLDER)
                1 -> fragment.deleteFolder(f_model)
                2 -> {
                    val startPosition = getAlbumPositionByPath(f_model)
                    if (f_model.isFavorite) AlbumUtils.removeFromFavorites(
                        f_model, fragment.requireContext()
                    ) else fragment.addToFavorites(f_model)
                    SimpleAlbumModel.sort(models)
                    val endPosition = getAlbumPositionByPath(f_model)
                    notifyItemMoved(startPosition, endPosition)
                }
            }
            dialog.dismiss()
        }
    }
}