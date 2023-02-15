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
import com.jefferson.application.br.app.SimpleDialog.Companion.getMenuItems
import com.jefferson.application.br.fragment.AlbumFragment
import com.jefferson.application.br.model.FolderModel
import com.jefferson.application.br.model.SimplifiedAlbum
import com.jefferson.application.br.model.SimplifiedAlbum.Companion.createFrom

class AlbumAdapter(
    private val fragment: AlbumFragment,
    private var models: ArrayList<FolderModel>,
    simplifiedModels: ArrayList<SimplifiedAlbum>
) : RecyclerView.Adapter<AlbumAdapter.ViewHolder>() {
    private val pagerPosition: Int = fragment.pagerPosition
    private var group: View? = null
    var simplifiedModels: ArrayList<SimplifiedAlbum>
        private set
    private var itemToHighlight = -1

    init {
        this.simplifiedModels = simplifiedModels
    }

    fun getItem(itemPosition: Int): FolderModel? {
        return if (itemPosition in (0 until itemCount)) {
            models[itemPosition]
        } else null
    }

    fun insertItem(item: FolderModel) {
        models.add(item)
        FolderModel.sort(models)
        simplifiedModels.add(createFrom(item))
        val position = models.indexOf(item)
        if (position != -1) {
            notifyItemInserted(position)
            fragment.scrollTo(position)
        } else {
            notifyDataSetChanged()
        }
    }

    fun removeItem(position: Int) {
        if (position in (0 until itemCount)) {
            removeSimplifiedItem(models[position].name)
            models.removeAt(position)
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
        newAlbumModels: ArrayList<FolderModel>,
        newSimplifiedAlbums: ArrayList<SimplifiedAlbum>
    ) {
        models = newAlbumModels
        simplifiedModels = newSimplifiedAlbums
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, p2: Int): ViewHolder {
        group = parent
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.adapter_photosfolder, parent, false)
        return ViewHolder(view)
    }

    fun notifyItemChanged(f_model: FolderModel) {
        for (i in 0 until itemCount) {
            if ((f_model.name == getItem(i)!!.name)) {
                notifyItemChanged(i)
                break
            }
        }
    }

    fun removeItem(item: FolderModel) {
        val key = models.indexOf(item)
        if (key != -1) {
            models.removeAt(key)
            notifyItemRemoved(key)
        } else {
            Toast.makeText(
                fragment.requireContext(),
                "Can not find folder index for item " + item.name,
                Toast.LENGTH_LONG
            ).show()
        }
        removeSimplifiedItem(item.name)
    }

    private fun removeSimplifiedItem(name: String) {
        val model = getSimplifiedAlbumByName(name)
        if (model != null) {
            simplifiedModels.remove(model)
        }
    }

    fun getItemPosition(path: String): Int {
        for (i in models.indices) {
            val model = models[i]
            if ((model.path == path)) {
                return i
            }
        }
        return -1
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = models[position]
        holder.folderName.text = model.name
        holder.folderSize.text = model.items.size.toString()
        val isEmpty = model.items.isEmpty()
        holder.favoriteView.visibility = if (model.isFavorite) View.VISIBLE else View.GONE
        if (!isEmpty) {
            Glide.with(fragment.requireContext()).load(
                "file://" + model.items[0]
                    .path
            ).skipMemoryCache(true).into(holder.imageView)
        } else {
            holder.imageView.setImageBitmap(null)
            holder.smallView.setImageResource(R.drawable.ic_image_broken_variant)
        }
        val visibility = if ((isEmpty)) View.VISIBLE else View.GONE
        if (holder.smallView.visibility != visibility) {
            holder.smallView.visibility = visibility
        }
        holder.parent.setOnClickListener{ fragment.openAlbum(model) }
        holder.parent.setOnLongClickListener { view ->
            val context = view.context
            val options = arrayOf(
                context.getString(R.string.renomear),
                context.getString(R.string.apagar),
                if (model.isFavorite) "Remove from favorites" else "Add to Favorites"
            )
            val icons = intArrayOf(
                R.drawable.ic_rename, R.drawable.ic_delete_outline,
                if (model.isFavorite) R.drawable.ic_bookmark_remove_outline else R.drawable.ic_bookmark_add_outline
            )
            val dialog = SimpleDialog(fragment.requireActivity())
            dialog.setMenuItems(
                getMenuItems(options, icons),
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
            if ((model.name == albumName)) {
                return i
            }
        }
        return -1
    }

    private fun getItemPosition(f_model: FolderModel): Int {
        for (i in 0 until itemCount) {
            if ((getItem(i) == f_model)) {
                notifyItemChanged(i)
                return i
            }
        }
        return -1
    }

    fun getSimplifiedAlbumByName(name: String): SimplifiedAlbum? {
        for (item: SimplifiedAlbum in simplifiedModels) {
            if ((name == item.name)) {
                return item
            }
        }
        return null
    }

    fun setItemToHighlight(position: Int) {
        itemToHighlight = position
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var folderName: TextView
        var folderSize: TextView
        var imageView: ImageView
        var favoriteView: ImageView
        var parent: RelativeLayout
        var smallView: ImageView

        init {
            folderName = view.findViewById(R.id.tv_folder)
            folderSize = view.findViewById(R.id.folder_size_label)
            imageView = view.findViewById(R.id.iv_image)
            parent = view.findViewById(R.id.adapter_photosfolderParentView)
            smallView = view.findViewById(R.id.folder_small_icon_view)
            favoriteView = view.findViewById(R.id.folder_favorite_icon_view)
        }
    }

    private inner class DialogMenuListener(var f_model: FolderModel, var dialog: SimpleDialog) :
        OnItemClickListener {
        override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
            when (position) {
                0 -> fragment.inputFolderDialog(f_model, AlbumFragment.ACTION_RENAME_FOLDER)
                1 -> fragment.deleteFolder(f_model)
                2 -> {
                    val startPosition = getItemPosition(f_model)
                    if (f_model.isFavorite) fragment.removeFromFavorites(f_model) else fragment.addToFavorites(
                        f_model
                    )
                    FolderModel.sort(models)
                    val endPosition = getItemPosition(f_model)
                    notifyItemMoved(startPosition, endPosition)
                }
            }
            dialog.dismiss()
        }
    }
}