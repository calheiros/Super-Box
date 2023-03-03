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
package com.jefferson.application.br.fragment

import android.app.Activity
import android.content.Intent
import android.database.sqlite.SQLiteDatabaseCorruptException
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SmoothScroller
import com.jefferson.application.br.R
import com.jefferson.application.br.activity.MainActivity
import com.jefferson.application.br.activity.ViewAlbum
import com.jefferson.application.br.adapter.AlbumAdapter
import com.jefferson.application.br.app.SimpleDialog
import com.jefferson.application.br.app.SimpleDialog.OnDialogClickListener
import com.jefferson.application.br.database.PathsDatabase
import com.jefferson.application.br.database.PathsDatabase.Companion.getInstance
import com.jefferson.application.br.model.FileModel
import com.jefferson.application.br.model.AlbumModel
import com.jefferson.application.br.model.MediaModel
import com.jefferson.application.br.model.SimplifiedAlbum
import com.jefferson.application.br.task.DeleteFilesTask
import com.jefferson.application.br.task.JTask
import com.jefferson.application.br.util.AlbumUtils
import com.jefferson.application.br.util.JDebug
import com.jefferson.application.br.util.Storage
import java.io.File

class AlbumFragment : Fragment {
    private val corruptedWarnHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun dispatchMessage(msg: Message) {
            super.dispatchMessage(msg)
            val dialog = SimpleDialog(requireActivity())
            dialog.setTitle("Database corrupted!!!")
            dialog.setMessage("The database has been corrupted!")
            dialog.setCanceledOnTouchOutside(false)
            dialog.setPositiveButton("okay", null)
            dialog.show()
        }
    }
    var pagerPosition = 0
        private set
    private var albumAdapter: AlbumAdapter? = null
    private var view: View? = null
    private var retrieveMedia: JTask? = null
    private var recyclerView: RecyclerView? = null
    private var progressBar: View? = null
    private var emptyView: View? = null
    private var paddingBottom = 0

    constructor()
    constructor(position: Int, mainFragment: MainFragment?) {
        pagerPosition = position
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (view == null) {
            view = inflater.inflate(R.layout.main_gallery, container, false)
            progressBar = view?.findViewById(R.id.main_galery_progressBar)
            recyclerView = view?.findViewById(R.id.recyclerView)
            emptyView = view?.findViewById(R.id.empty_linearLayout)
            val storagePermissionView = view?.findViewById<View>(R.id.storage_permission_layout)
            val layoutManager = GridLayoutManager(activity, 2)
            recyclerView?.layoutManager = layoutManager
            recyclerView?.clipToPadding = false
            recyclerView?.setPadding(0, 0, 0, paddingBottom)
            populateRecyclerView()
        }
        return view
    }

    fun openAlbum(f_model: AlbumModel?) {
        val intent = Intent(context, ViewAlbum::class.java)
        intent.putExtra("position", pagerPosition)
        intent.putExtra("name", f_model!!.name)
        intent.putExtra("data", f_model.items)
        intent.putExtra("folder", f_model.path)
        requireActivity().startActivity(intent)
    }

    fun openAlbum(albumName: String?) {
        val pos = albumAdapter!!.getItemPositionByName(albumName!!)
        if (pos != -1) {
            openAlbum(albumAdapter!!.getItem(pos))
        }
    }

    fun buildModels(position: Int): ArrayList<AlbumModel> {
        val models = ArrayList<AlbumModel>()
        val root =
            Storage.getFolder(if (position == 0) Storage.IMAGE else Storage.VIDEO, requireContext())
                ?: return models
        root.mkdirs()
        val database: PathsDatabase? = try {
            getInstance(requireContext())
        } catch (e: SQLiteDatabaseCorruptException) {
            //do something
            return models
        }
        val bookmark = database?.favoritesFolder
        if (root.exists()) {
            val files = root.list()
            if (files != null) for (s in files) {
                val file = File(root, s)
                if (file.isDirectory) {
                    val folderList = file.listFiles()
                    var favorite = false
                    var folderName: String? = database?.getFolderName(
                        s!!,
                        if (position == 0) FileModel.IMAGE_TYPE else FileModel.VIDEO_TYPE
                    )
                    if (bookmark != null) {
                        favorite = java.lang.Boolean.TRUE == bookmark[file.name]
                    }
                    folderName = folderName ?: s
                    val model = AlbumModel()
                    model.name = folderName!!
                    model.path = file.absolutePath
                    model.isFavorite = favorite
                    if (folderList != null) for (value in folderList) {
                        val mm = MediaModel(value.absolutePath)
                        model.addItem(mm)
                    }
                    models.add(model)
                }
            }
        }
        AlbumModel.sort(models)
        database?.close()
        return models
    }

    private fun populateRecyclerView() {
        retrieveMedia = object : JTask() {
            private var albumsModel: ArrayList<AlbumModel>? = null
            override fun workingThread() {
                val result = buildModels(
                    pagerPosition
                )
                albumsModel = result
            }

            override fun onBeingStarted(){
            }

            override fun onFinished() {
                putModels(albumsModel)
                notifyDataUpdated()
            }

            override fun onException(e: Exception) {
                revokeFinish(true)
                Toast.makeText(context, "Unknown error occurred! " + e.message, Toast.LENGTH_LONG)
                    .show()
                JDebug.writeLog(e.cause, requireContext())
            }
        }
        (retrieveMedia as JTask).setThreadPriority(Thread.MAX_PRIORITY)
        (retrieveMedia as JTask).start()
    }

    fun scrollTo(position: Int) {
        recyclerView!!.scrollToPosition(position)
    }

    fun putModels(models: ArrayList<AlbumModel>?) {
        if (albumAdapter != null) {
            albumAdapter?.updateModels(models!!)
        } else {
            albumAdapter = AlbumAdapter(this@AlbumFragment, models!!)
            notifyDataUpdated()
        }
    }

    fun removeFolder(folderPosition: Int) {
        albumAdapter?.removeItem(folderPosition)
    }

    private fun notifyDataUpdated() {
        val visibility = if (albumAdapter?.itemCount == 0) View.VISIBLE else View.GONE
        recyclerView?.adapter = albumAdapter
        emptyView?.visibility = visibility
        progressBar?.visibility = View.GONE

    }

    private fun warnDatabaseCorrupted() {
        corruptedWarnHandler.sendEmptyMessage(0)
    }

    fun inputFolderDialog(model: AlbumModel?, action: Int) {
        val activity: Activity = requireActivity()
        val title: String?
        val dialog = SimpleDialog(activity, SimpleDialog.STYLE_INPUT)
        val editText = dialog.getInputEdiText()
        editText.requestFocus()

        if (action == ACTION_RENAME_FOLDER) {
            val name = model!!.name
            title = getString(R.string.renomear_pasta)
            editText.setText(name)
            editText.setSelection(name.length)
        } else {
            title = getString(R.string.criar_pasta)
        }
        dialog.setTitle(title)
        dialog.setPositiveButton(
            activity.getString(R.string.concluir),
            object : OnDialogClickListener() {
                override fun onClick(dialog: SimpleDialog): Boolean {
                    val text = editText.text.toString()
                    val result = AlbumUtils.validateName(text, context)
                    if (!result.ok) {
                        Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                        return false
                    }
                    val success = true
                    var message: String? = null
                    when (action) {
                        ACTION_RENAME_FOLDER -> if (success == AlbumUtils.renameAlbum(
                                context, model!!, text, pagerPosition
                            )
                        ) {
                            message = "Folder renamed to \"$text\"."
                            albumAdapter?.notifyItemChanged(model)
                        } else {
                            message = "Failed to rename folder! :("
                        }
                        ACTION_CREATE_FOLDER -> {
                            val folder = AlbumUtils.createAlbum(context, text, pagerPosition)
                            if (folder != null) {
                                message = "Folder \"$text\" created."
                                albumAdapter!!.insertItem(folder)
                            } else {
                                message = "Failed to create folder! :("
                            }
                        }
                    }
                    notifyDataUpdated()
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    return success
                }
            }).setNegativeButton(getString(R.string.cancelar), null).show()
    }

    fun deleteFolder(model: AlbumModel?) {
        if (model == null) {
            return
        }
        val name = model.name
        val simpleDialog = SimpleDialog(requireActivity(), SimpleDialog.STYLE_ALERT_HIGH)
        simpleDialog.setTitle(getString(R.string.apagar))
        simpleDialog.setMessage(getString(R.string.apagar_pasta_aviso, name))
        simpleDialog.setPositiveButton(getString(R.string.sim), object : OnDialogClickListener() {
            override fun onClick(dialog: SimpleDialog): Boolean {
                val root = File(model.path!!)
                val task = DeleteFilesTask(requireActivity(), model.itemsPath, pagerPosition, root)
                task.setOnFinishedListener {
                    if (task.deletedAll()) {
                        albumAdapter!!.removeItem(model)
                        notifyDataUpdated()
                    } else {
                        (requireActivity() as MainActivity).updateFragment(pagerPosition)
                    }
                }
                task.start()
                return true
            }
        })
        simpleDialog.setNegativeButton(getString(R.string.nao), null)
        simpleDialog.show()
    }

    fun update() {
        populateRecyclerView()
    }

    fun addToFavorites(f_model: AlbumModel) {
        val database = getInstance(requireContext())
        val file = File(f_model.path!!)
        val name = file.name
        val success = database.setFavoriteFolder(name)
        database.close()
        if (!success) {
            Toast.makeText(requireContext(), "failed to ADD to bookmark", Toast.LENGTH_SHORT).show()
            return
        }
        f_model.isFavorite = true
        Toast.makeText(requireContext(), "Added to Favorites", Toast.LENGTH_LONG).show()
    }

    fun reload() {
        if (emptyView != null && emptyView?.visibility == View.VISIBLE)
            emptyView?.visibility = View.GONE

        if (retrieveMedia != null && retrieveMedia?.getStatus() == JTask.Status.STARTED)
            retrieveMedia?.cancelTask()

        progressBar?.visibility = View.VISIBLE
        albumAdapter?.updateModels(ArrayList())
        populateRecyclerView()
    }

    fun setBottomPadding(paddingBottom: Int) {
        this.paddingBottom = paddingBottom
        if (recyclerView != null) {
            recyclerView!!.setPadding(0, 0, 0, paddingBottom)
        }
    }

    val isLoading: Boolean
        get() = retrieveMedia!!.getStatus() == JTask.Status.STARTED

    val simplifiedModels: ArrayList<SimplifiedAlbum>
        get() {
            val models = albumAdapter?.models!!
            val result = ArrayList<SimplifiedAlbum>()
            for (model in models) {
                result.add(model.getSimplifiedAlbum())
            }
            return result
        }

    fun scrollToAlbum(albumName: String?) {
        albumName ?: return
        val position = albumAdapter!!.getItemPositionByName(albumName)
        if (position != -1) {
            val smoothScroller: SmoothScroller = object : LinearSmoothScroller(requireContext()) {
                override fun getVerticalSnapPreference(): Int {
                    return SNAP_TO_START
                }

                override fun onStop() {
                    super.onStop()
                    albumAdapter?.setItemToHighlight(position)
                    albumAdapter?.notifyItemChanged(position)
                }
            }
            smoothScroller.targetPosition = position
            recyclerView?.layoutManager?.startSmoothScroll(smoothScroller)
        }
    }

    companion object {
        const val ALBUM_NAME_OKAY = "folder_name_okay"
        const val ACTION_CREATE_FOLDER = 122
        const val ACTION_RENAME_FOLDER = 54



    }
}