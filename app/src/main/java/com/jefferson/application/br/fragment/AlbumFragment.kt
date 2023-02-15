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
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabaseCorruptException
import android.os.*
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
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
import com.jefferson.application.br.model.FolderModel
import com.jefferson.application.br.model.MediaModel
import com.jefferson.application.br.model.SimplifiedAlbum
import com.jefferson.application.br.task.DeleteFilesTask
import com.jefferson.application.br.task.JTask
import com.jefferson.application.br.util.JDebug
import com.jefferson.application.br.util.Storage
import com.jefferson.application.br.util.StringUtils
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
            emptyView = view?.findViewById(R.id.empty_linearLayout)
            val storagePermissionView = view?.findViewById<View>(R.id.storage_permission_layout)
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(activity)
            val root = Environment.getExternalStorageDirectory().absolutePath
            recyclerView = view?.findViewById(R.id.recyclerView)
            val layoutManager = GridLayoutManager(activity, 2)
            recyclerView?.layoutManager = layoutManager
            recyclerView?.clipToPadding = false
            recyclerView?.setPadding(0, 0, 0, paddingBottom)
            populateRecyclerView()
        }
        return view
    }

    fun openAlbum(f_model: FolderModel?) {
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

    class BuildResult {
        var simplifiedAlbums: ArrayList<SimplifiedAlbum> = ArrayList()
        var folderModels: ArrayList<FolderModel> = ArrayList()

    }

    fun buildModels(position: Int): BuildResult {
        val result = BuildResult()
        val simplifiedModels = ArrayList<SimplifiedAlbum>()
        val models = ArrayList<FolderModel>()
        val root =
            Storage.getFolder(if (position == 0) Storage.IMAGE else Storage.VIDEO, requireContext())
        root.mkdirs()
        val database: PathsDatabase? = try {
            getInstance(requireContext())
        } catch (e: SQLiteDatabaseCorruptException) {
            //do something
            return result
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
                    val model = FolderModel()
                    model.name = folderName!!
                    model.path = file.absolutePath
                    model.isFavorite = favorite
                    if (folderList != null) for (value in folderList) {
                        val mm = MediaModel(value.absolutePath)
                        model.addItem(mm)
                    }
                    val items = model.items
                    val thumb = if (items.size > 0) items[0].path else ""
                    simplifiedModels.add(SimplifiedAlbum(folderName, thumb ?: ""))
                    models.add(model)
                }
            }
        }
        FolderModel.sort(models)
        result.folderModels = models
        result.simplifiedAlbums = simplifiedModels
        database?.close()
        return result
    }

    private fun populateRecyclerView() {
        retrieveMedia = object : JTask() {
            private var simplifiedModels: ArrayList<SimplifiedAlbum>? = null
            private var albumsModel: ArrayList<FolderModel>? = null
            override fun workingThread() {
                val result = buildModels(
                    pagerPosition
                )
                albumsModel = result.folderModels
                this.simplifiedModels = result.simplifiedAlbums
            }

            override fun onBeingStarted(){
            }

            override fun onFinished() {
                putModels(albumsModel, simplifiedModels)
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

    fun putModels(models: ArrayList<FolderModel>?, simplifiedModels: ArrayList<SimplifiedAlbum>?) {
        if (albumAdapter != null) {
            albumAdapter?.updateModels(models!!, simplifiedModels!!)
        } else {
            albumAdapter = AlbumAdapter(this@AlbumFragment, models!!, simplifiedModels!!)
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

    fun inputFolderDialog(model: FolderModel?, action: Int) {
        val activity: Activity = requireActivity()
        val contentView = requireActivity().layoutInflater.inflate(R.layout.dialog_edit_text, null)
        val editText = contentView.findViewById<EditText>(R.id.editTextInput)
        val title: String?
        if (action == ACTION_RENAME_FOLDER) {
            val name = model!!.name
            title = getString(R.string.renomear_pasta)
            editText.setText(name)
            editText.setSelection(name.length)
        } else {
            title = getString(R.string.criar_pasta)
        }
        val dialog = SimpleDialog(activity, SimpleDialog.STYLE_INPUT)
        dialog.setTitle(title)
        dialog.setContentView(contentView)
        dialog.setPositiveButton(
            activity.getString(R.string.concluir),
            object : OnDialogClickListener() {
                override fun onClick(dialog: SimpleDialog): Boolean {
                    val text = editText.text.toString()
                    val result = validateFolderName(text, context)
                    if (result != FOLDER_NAME_OKAY) {
                        Toast.makeText(context, result, Toast.LENGTH_LONG).show()
                        return false
                    }
                    val success = true
                    var message: String? = null
                    when (action) {
                        ACTION_RENAME_FOLDER -> if (success == renameFolder(
                                context, model!!, text, pagerPosition
                            )
                        ) {
                            message = "Folder renamed to \"$text\"."
                            val index = albumAdapter?.getItemPosition(model.path!!)
                            //FolderModel model = albumAdapter.getItem(index);
                            if (index != -1) {
                                val simplifiedAlbum = albumAdapter!!
                                    .getSimplifiedAlbumByName(model.name)
                                if (simplifiedAlbum != null) {
                                    simplifiedAlbum.name = text
                                }
                                model.name = text
                                albumAdapter?.notifyItemChanged(index!!)
                            }
                        } else {
                            message = "Failed to rename folder! :("
                        }
                        ACTION_CREATE_FOLDER -> {
                            val folder = createFolder(context, text, pagerPosition)
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

    fun deleteFolder(model: FolderModel?) {
        if (model == null) {
            return
        }
        val name = model.name
        val simpleDialog = SimpleDialog(requireActivity(), SimpleDialog.STYLE_ALERT_HIGH)
        simpleDialog.setTitle(getString(R.string.apagar))
        simpleDialog.setMessage(getString(R.string.apagar_pasta_aviso, name))
        simpleDialog.setPositiveButton(getString(R.string.sim), object : OnDialogClickListener() {
            override fun onClick(dialog: SimpleDialog): Boolean {
                val root = File(model.path)
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

    fun addToFavorites(f_model: FolderModel) {
        val database = getInstance(requireContext())
        val file = File(f_model.path)
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
        albumAdapter?.updateModels(ArrayList(), ArrayList())
        populateRecyclerView()
    }

    fun removeFromFavorites(f_model: FolderModel) {
        val database = getInstance(requireContext())
        val file = File(f_model.path)
        val name = file.name
        if (!database.removeFavoriteFolder(name)) {
            Toast.makeText(requireContext(), "failed to remove from bookmark", Toast.LENGTH_SHORT)
                .show()
            return
        }
        f_model.isFavorite = false
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
        get() = albumAdapter!!.simplifiedModels

    fun scrollToAlbum(albumName: String?) {
        val position = albumAdapter!!.getItemPositionByName(albumName!!)
        if (position != -1) {
            val smoothScroller: SmoothScroller = object : LinearSmoothScroller(requireContext()) {
                override fun getVerticalSnapPreference(): Int {
                    return SNAP_TO_START
                }

                override fun onStop() {
                    super.onStop()
                    albumAdapter!!.setItemToHighlight(position)
                    albumAdapter!!.notifyItemChanged(position)
                }
            }
            smoothScroller.targetPosition = position
            recyclerView?.layoutManager?.startSmoothScroll(smoothScroller)
        }
    }

    companion object {
        const val FOLDER_NAME_OKAY = "folder_name_okay"
        const val ACTION_CREATE_FOLDER = 122
        const val ACTION_RENAME_FOLDER = 54
        fun renameFolder(
            context: Context?,
            model: FolderModel,
            newName: String,
            position: Int
        ): Boolean {
            var folderDatabase: PathsDatabase? = null
            try {
                val folderType = if (position == 0) FileModel.IMAGE_TYPE else FileModel.VIDEO_TYPE
                folderDatabase = getInstance(context!!)
                val file = File(model.path)
                val id = file.name
                val folderName = folderDatabase.getFolderName(id, folderType)
                //JDebug.toast("ID => " + folderName + "\n NAME => " + model.getName());
                val newFolderId = folderDatabase.getFolderIdFromName(newName, folderType)
                if (folderName != null && folderName == newName) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.pasta_mesmo_nome),
                        Toast.LENGTH_LONG
                    ).show()
                    folderDatabase.close()
                    return false
                }
                if (newFolderId != null) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.pasta_existe),
                        Toast.LENGTH_LONG
                    ).show()
                    folderDatabase.close()
                    return false
                }
                if (folderName == null) {
                    folderDatabase.addFolderName(id, newName, folderType)
                } else {
                    folderDatabase.updateFolderName(id, newName, folderType)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            } finally {
                folderDatabase?.close()
            }
            return true
        }

        fun createFolder(context: Context?, name: String, position: Int): FolderModel? {
            var database: PathsDatabase? = null
            var folder: FolderModel? = null
            try {
                val type = if (position == 0) FileModel.IMAGE_TYPE else FileModel.VIDEO_TYPE
                database = getInstance(context!!)
                var id = database.getFolderIdFromName(name, type)
                val randomStr = StringUtils.getRandomString(24)
                if (id == null) {
                    id = randomStr
                    val strType = if (position == 0) Storage.IMAGE else Storage.VIDEO
                    val file = File(Storage.getFolder(strType, context), randomStr)
                    if (file.mkdirs()) {
                        folder = FolderModel()
                        database.addFolderName(id, name, type)
                        folder.name = name
                        folder.path = file.absolutePath
                    }
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.pasta_existe),
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                database?.close()
            }
            return folder
        }

        fun validateFolderName(name: String, context: Context?): String {
            val noSpace = name.replace(" ", "")
            return if (noSpace.isEmpty()) {
                context!!.getString(R.string.pasta_nome_vazio)
            } else if (name.length > 50) {
                context!!.getString(R.string.pasta_nome_muito_grande)
            } else {
                FOLDER_NAME_OKAY
            }
        }
    }
}