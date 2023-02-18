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
package com.jefferson.application.br.activity

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.DisplayMetrics
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jefferson.application.br.adapter.MultiSelectRecyclerViewAdapter
import com.jefferson.application.br.adapter.MultiSelectRecyclerViewAdapter.ViewHolder.ClickListener
import com.jefferson.application.br.R
import com.jefferson.application.br.app.ProgressThreadUpdate
import com.jefferson.application.br.app.SimpleDialog
import com.jefferson.application.br.app.SimpleDialog.OnDialogClickListener
import com.jefferson.application.br.database.PathsDatabase
import com.jefferson.application.br.model.FileModel
import com.jefferson.application.br.model.MediaModel
import com.jefferson.application.br.task.DeleteFilesTask
import com.jefferson.application.br.task.JTask
import com.jefferson.application.br.util.*
import com.jefferson.application.br.view.RoundedImageView
import eightbitlab.com.blurview.BlurView
import java.io.*
import java.util.*
import kotlin.math.abs

class ViewAlbum : MyCompatActivity(), ClickListener, View.OnClickListener {
    private val minItemWidth = 110
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MultiSelectRecyclerViewAdapter
    private lateinit var filePaths: ArrayList<MediaModel>
    private lateinit var fab: FloatingActionButton
    private lateinit var emptyView: View
    private lateinit var menuLayout: View
    private lateinit var toolbar: Toolbar

    private var selectionMode = false
    private var position = 0
    private var title: String? = null
    private var folder: File? = null
    private var myThread: RetrieverDataTask? = null
    private var baseNameDirectory: String? = null
    private var selectAllTextView: TextView? = null
    private var selectImageView: ImageView? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_album_layout)

        position = intent.getIntExtra("position", -1)
        title = intent.getStringExtra("name")
        folder = intent.getStringExtra("folder")?.let { File(it) }
        filePaths = intent.getParcelableArrayListExtra("data")!!

        val layoutManager = GridLayoutManager(this, autoSpan)
        recyclerView = findViewById(R.id.my_recycler_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = layoutManager
        adapter = MultiSelectRecyclerViewAdapter(this@ViewAlbum, filePaths, this, position)
        recyclerView.adapter = adapter

        fab = findViewById(R.id.view_album_fab_button)
        menuLayout = findViewById(R.id.lock_layout)
        val mViewUnlock = findViewById<View>(R.id.unlockView)
        val mViewDelete = findViewById<View>(R.id.deleteView)
        val mViewSelect = findViewById<View>(R.id.selectView)
        selectAllTextView = findViewById(R.id.options_album_selectTextView)
        selectImageView = findViewById(R.id.selectImageView)
        val mViewMove = findViewById<View>(R.id.moveView)
        emptyView = findViewById(R.id.view_album_empty_view)
        mViewUnlock.setOnClickListener(this)
        mViewDelete.setOnClickListener(this)
        mViewMove.setOnClickListener(this)
        mViewSelect.setOnClickListener(this)
        fab.setOnClickListener(this)
        initToolbar()
        configureBlurView(recyclerView)

        if (filePaths.isEmpty()) {
            emptyView.visibility = View.VISIBLE
        }
        if (position == 1) {
            updateDatabase(filePaths, adapter)
        }
        val threshold = heightPixels / 100 //1%
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (selectionMode) {
                    return
                }
                if (abs(n = dy) > threshold) {
                    if (dy > 0) {
                        // Scroll Down
                        if (fab.isShown) {
                            fab.hide()
                        }
                    } else {
                        // Scroll Up
                        if (!fab.isShown) {
                            fab.show()
                        }
                    }
                }
            }
        })
    }

    // Convert the minimum width to pixels
    private val autoSpan: Int
        get() {
            // Convert the minimum width to pixels
            val scale = resources.displayMetrics.density
            val minItemWidthPx = (minItemWidth * scale + 0.5f).toInt()
            val screenWidthPx = resources.displayMetrics.widthPixels
            return screenWidthPx / minItemWidthPx
        }
    private val heightPixels: Float
        get() {
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            return displayMetrics.heightPixels.toFloat()
        }

    private fun configureBlurView(view: ViewGroup?) {
        val blurView = findViewById<BlurView>(R.id.blurView)
        val radius = 13f
        blurView.outlineProvider = ViewOutlineProvider.BACKGROUND
        blurView.clipToOutline = true
        val render = BlurUtils.getRenderAlgorithm(this)
        blurView.setupWith(view!!, render) // or RenderEffectBlur
            .setBlurRadius(radius)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.deleteView -> {
                deleteFilesDialog()
            }
            R.id.moveView -> {
                changeFilesDirectory()
            }
            R.id.unlockView -> {
                exportToGallery()
            }
            R.id.selectView -> {
                toggleSelection()
            }
            R.id.view_album_fab_button -> {
                importFromGallery()
            }
        }
    }

    private fun importFromGallery() {
        val intent = Intent(this, ImportGalleryActivity::class.java)
        intent.putExtra("position", position)
        startActivityForResult(intent, IMPORT_FROM_GALLERY_CODE)
    }

    private fun updateDatabase(
        list: ArrayList<MediaModel>?,
        adapter: MultiSelectRecyclerViewAdapter?
    ) {
        myThread = RetrieverDataTask(list, adapter)
        myThread?.priority = Thread.MAX_PRIORITY
        myThread?.start()
    }

    private fun toggleSelection() {
        if (adapter.selectedItemCount == adapter.itemCount) {
            adapter.clearSelection()
        } else {
            for (i in 0 until adapter.itemCount) {
                if (!adapter.isSelected(i)) {
                    toggleItemSelected(i, false)
                }
            }
        }
        invalidateOptionsMenu()
        switchSelectButtonIcon()
    }

    private fun exportToGallery() {
        if (adapter.selectedItemCount == 0) {
            Toast.makeText(applicationContext, getString(R.string.selecionar_um), Toast.LENGTH_LONG)
                .show()
        } else {
            val count = adapter.selectedItemCount
            val item = count.toString() + " " + getItemName(count)
            val message = String.format(getString(R.string.mover_galeria), item)
            val dialog = SimpleDialog(this@ViewAlbum, SimpleDialog.STYLE_ALERT)
            dialog.setIcon(R.drawable.ic_info_twotone)
            dialog.setTitle(getString(R.string.mover)).setMessage(message)
                .setPositiveButton(getString(R.string.sim), object : OnDialogClickListener() {
                    override fun onClick(dialog: SimpleDialog): Boolean {
                        val task = ExportTask(selectedItemsPath, dialog)
                        exitSelectionMode()
                        task.start()
                        return false
                    }
                }).setNegativeButton(getString(R.string.cancelar), null).show()
        }
    }

    private fun deleteFilesDialog() {
        val count = adapter.selectedItemCount
        if (count == 0) {
            Toast.makeText(this@ViewAlbum, getString(R.string.selecionar_um), Toast.LENGTH_SHORT)
                .show()
            return
        }
        val item = count.toString() + " " + getItemName(count)
        val message = String.format(getString(R.string.apagar_mensagem), item)
        val dialog = SimpleDialog(this@ViewAlbum, SimpleDialog.STYLE_ALERT_HIGH)
        dialog.showProgressBar(false)
        dialog.setTitle(getString(R.string.excluir))
        dialog.setMessage(message)
        dialog.setPositiveButton(getString(R.string.sim), object : OnDialogClickListener() {
            override fun onClick(dialog: SimpleDialog): Boolean {
                dialog.dismiss()
                val task = DeleteFiles(this@ViewAlbum, selectedItemsPath, position, folder)
                task.start()
                return true
            }
        })
        dialog.setNegativeButton(getString(R.string.cancelar), null)
        dialog.show()
    }

    private fun changeFilesDirectory() {
        if (adapter.selectedItemCount == 0) {
            Toast.makeText(this@ViewAlbum, getString(R.string.selecionar_um), Toast.LENGTH_SHORT)
                .show()
            return
        }
        val intent = Intent(this@ViewAlbum, FolderPickerActivity::class.java)
        intent.putExtra("selection", selectedItemsPath)
        intent.putExtra("position", position)
        intent.putExtra("current_path", folder!!.absolutePath)
        startActivityForResult(intent, CHANGE_DIRECTORY_CODE)
    }

    private fun getItemName(count: Int): String {
        return (if (position == 0) if (count > 1) getString(R.string.imagens) else getString(R.string.imagem) else if (count > 1) getString(
            R.string.videos
        ) else getString(R.string.video)).lowercase(
            Locale.getDefault()
        )
    }

    private fun showFilesInfo() {
        val files = adapter.selectedItems
        val size = files.size
        val resId: Int
        val view: View
        val database = PathsDatabase.getInstance(this, Storage.getDefaultStoragePath(this))
        if (size == 1) {
            resId = R.layout.files_info_layout
            view = layoutInflater.inflate(resId, null)
            val file = File(files[0])
            val nameText = view.findViewById<TextView>(R.id.file_name_info)
            val sizeText = view.findViewById<TextView>(R.id.file_size_info)
            val originText = view.findViewById<TextView>(R.id.file_origin_text_view)
            val filePath = database.getMediaPath(file.name)
            if (filePath != null) {
                val name = File(filePath).name
                nameText.text = name
                sizeText.text = StringUtils.getFormattedFileSize(file.length())
                originText.text = filePath
            }
        } else return
        SimpleDialog(this).setTitle(getString(R.string.information)).setContentView(view)
            .setPositiveButton(android.R.string.ok, null).show()
        database.close()
    }

    val type: String?
        get() = when (position) {
            0 -> FileModel.IMAGE_TYPE
            1 -> FileModel.VIDEO_TYPE
            else -> null
        }

    private fun synchronizeMainActivity() {
        val visibility = if (adapter.itemCount == 0) View.VISIBLE else View.GONE
        val mainActivity = MainActivity.instance
        emptyView.visibility = visibility
        mainActivity?.updateFragment(position) ?:
            Toast.makeText(this, "Can't synchronize MainActivity!", Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                CHANGE_DIRECTORY_CODE -> {
                    exitSelectionMode()
                    val list = data!!.getStringArrayListExtra("moved_files")
                    Toast.makeText(
                        this,
                        list!!.size.toString() + " file(s) moved",
                        Toast.LENGTH_SHORT
                    ).show()
                    adapter.removeAll(list)
                    synchronizeMainActivity()
                }
                VIDEO_PLAY_CODE -> {
                    val index = data!!.getIntExtra("index", 0)
                    recyclerView.post { recyclerView.smoothScrollToPosition(index) }
                }
                IMPORT_FROM_GALLERY_CODE -> {
                    val paths = data!!.getStringArrayListExtra("selection")
                    val type = data.getStringExtra("type")
                    val intent = Intent(this, ImportMediaActivity::class.java)
                    intent.putStringArrayListExtra(ImportMediaActivity.MEDIA_LIST_KEY, paths)
                    intent.putExtra(ImportMediaActivity.TYPE_KEY, type)
                    //intent.putExtra(ImportMediaActivity.POSITION_KEY, position);
                    intent.putExtra(ImportMediaActivity.PARENT_KEY, folder!!.absolutePath)
                    startActivityForResult(intent, IMPORT_FROM_VIEW_ALBUM_CODE)
                }
                IMPORT_FROM_VIEW_ALBUM_CODE -> {
                    updateRecyclerView()
                    synchronizeMainActivity()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun updateRecyclerView() {
        val listItemsPath = ArrayList<MediaModel>()
        for (file in Objects.requireNonNull(
            folder!!.listFiles()
        )) {
            listItemsPath.add(MediaModel(file.absolutePath))
        }
        adapter.items = listItemsPath
        adapter.notifyDataSetChanged()
        if (position == 1) {
            updateDatabase(listItemsPath, adapter)
        }
    }

    private fun retrieveDataAndUpdate() {
        val list = adapter.items
        updateDatabase(list, adapter)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (selectionMode) {
            menuInflater.inflate(R.menu.view_album_menu, menu)
            if (baseNameDirectory == null) {
                baseNameDirectory =
                    if (title!!.length <= 20) "$title ( %s )" else title!!.substring(
                        0,
                        20
                    ) + "... ( %s )"
            }
            val count = adapter.selectedItemCount.toString()
            toolbar.title = String.format(baseNameDirectory!!, count)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            if (selectionMode) {
                exitSelectionMode()
            } else {
                finish()
            }
        } else if (item.itemId == R.id.info_item_menu) {
            showFilesInfo()
        }
        return false
    }

    override fun onItemClicked(item_position: Int, v: View?) {
        if (!selectionMode) {
            val mClass: Class<*>
            when (position) {
                0 -> startPreviewActivity(ImagePreviewActivity::class.java, item_position, v)
                1 -> {
                    mClass = VideoPlayerActivity::class.java
                    startPreviewActivity(mClass, item_position, v)
                }
            }
            return
        }
        toggleItemSelected(item_position, true)
        invalidateOptionsMenu()
        switchSelectButtonIcon()
    }

    private fun startPreviewActivity(previewActivity: Class<*>, position: Int, v: View?) {
        val intent = Intent(this, previewActivity)
        intent.putExtra("position", position)
        intent.putExtra("filepath", adapter.listItemsPath)
        val opts = ActivityOptions.makeScaleUpAnimation(
            v,
            0,
            0,
            v!!.width,
            v.height
        ) // Request the activity be started, using the custom animation options.
        startActivityForResult(intent, VIDEO_PLAY_CODE, opts.toBundle())
    }

    override fun onItemLongClicked(position: Int): Boolean {
        toggleItemSelected(position, true)
        invalidateOptionsMenu()
        switchSelectButtonIcon()
        if (!selectionMode) {
            enterSelectionMode()
        }
        return true
    }

    private val selectedItemsPath: ArrayList<String?>
        get() {
            val selectedItems = ArrayList<String?>()
            for (i in adapter.getSelectedItems()) {
                selectedItems.add(adapter.items[i].path)
            }
            return selectedItems
        }

    private fun toggleItemSelected(position: Int, notifyAll: Boolean) {
        adapter.toggleItemSelected(position, notifyAll)
    }

    private fun switchSelectButtonIcon() {
        val allSelected = adapter.selectedItemCount == adapter.itemCount
        val text = if (allSelected) "Unselect all" else "Select all"
        selectAllTextView!!.text = text
        selectImageView!!.setImageResource(if (allSelected) R.drawable.ic_select else R.drawable.ic_select_all)
    }

    private fun enterSelectionMode() {
        selectionMode = true
        invalidateOptionsMenu()
        menuLayout.visibility = View.VISIBLE
        val anim = AnimationUtils.loadAnimation(applicationContext, R.anim.slide_up)
        anim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(anim: Animation) {}
            override fun onAnimationEnd(anim: Animation) {
                val base = resources.getDimension(R.dimen.recycler_view_padding).toInt()
                val button = menuLayout.height
                recyclerView.setPadding(base, base, base, button)
                recyclerView.clipToPadding = false
            }

            override fun onAnimationRepeat(p1: Animation) {}
        })
        menuLayout.animation = anim
        fab.hide()
    }

    fun exitSelectionMode() {
        selectionMode = false
        invalidateOptionsMenu()
        if (adapter.items.isNotEmpty()) {
            adapter.clearSelection()
        }
        menuLayout.animation =
            AnimationUtils.loadAnimation(applicationContext, R.anim.slide_bottom)
        menuLayout.visibility = View.GONE
        val dimen = resources.getDimension(R.dimen.recycler_view_padding).toInt()
        recyclerView.setPadding(dimen, dimen, dimen, dimen)
        toolbar.title = title
        fab.show()
    }

    override fun onBackPressed() {
        if (selectionMode) {
            exitSelectionMode()
            return
        }
        super.onBackPressed()
    }

    private fun initToolbar() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setTitleTextColor(resources.getColor(android.R.color.white))
        supportActionBar?.title = title
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val collapsingToolbar = findViewById<CollapsingToolbarLayout>(R.id.collapsing_toolbar)
        collapsingToolbar.title = title // sets the title of the toolbar
        collapsingToolbar.setCollapsedTitleTextColor(Color.WHITE) // sets the text color of the collapsed title
        collapsingToolbar.setExpandedTitleColor(Color.TRANSPARENT) // sets the text color of the expanded title
        collapsingToolbar.scrimAnimationDuration = 150
        setAlbumDetails()
    }

    private fun setAlbumDetails() {
        val roundedImageView = findViewById<RoundedImageView>(R.id.thumbnail_image_view)
        val itemCountLabel = findViewById<TextView>(R.id.text_view_item_count)
        val albumNameLabel = findViewById<TextView>(R.id.album_name_label)
        val resId =
            if (position == 0) R.plurals.imagem_total_plural else R.plurals.video_total_plural
        var itemCount = resources.getQuantityString(resId, filePaths.size)
        itemCount = String.format(itemCount, filePaths.size)
        itemCountLabel.text = itemCount
        albumNameLabel.text = title
        roundedImageView.setRadius(15f)
        if (filePaths.isNotEmpty()) {
            val path = filePaths[0].path
            Glide.with(this).load(path).into(roundedImageView)
        }
    }

    inner class DeleteFiles(activity: Activity?, p1: ArrayList<String?>?, p3: Int, p4: File?) :
        DeleteFilesTask(activity, p1, p3, p4) {
        private var threadInterrupted = false
        override fun onBeingStarted() {
            super.onBeingStarted()
            if (myThread != null && myThread!!.isWorking) {
                myThread!!.stopWork()
                threadInterrupted = true
            }
            exitSelectionMode()
        }

        override fun onInterrupted() {
            super.onInterrupted()
            if (threadInterrupted && adapter.items.isNotEmpty()) {
                updateRecyclerView()
            }
            synchronizeMainActivity()
        }

        override fun onFinished() {
            super.onFinished()
            if (adapter.items.isEmpty()) {
                finish()
            } else if (threadInterrupted) {
                updateRecyclerView()
            }
            synchronizeMainActivity()
        }

        override fun onUpdated(get: Array<Any>) {
            super.onUpdated(get)
            adapter.removeItem((get[0] as String))
        }
    }

    inner class RetrieverDataTask(
        private val list: ArrayList<MediaModel>?,
        private val adapter: MultiSelectRecyclerViewAdapter?
    ) : Thread() {
        var isWorking = true
            private set
        private var cancelled = false
        override fun run() {
            try {
                PathsDatabase.getInstance(this@ViewAlbum, Storage.getDefaultStoragePath(this@ViewAlbum))
                    .use { database ->
                        for (model in list!!) {
                            if (!isWorking) break
                            try {
                                val file = File(model.path)
                                var duration = database.getDuration(file.name)
                                if (duration == -1 || duration == 0) {
                                    duration = try {
                                        val uri = Uri.parse(model.path)
                                        val mmr = MediaMetadataRetriever()
                                        mmr.setDataSource(this@ViewAlbum, uri)
                                        val durationStr =
                                            mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                                        durationStr!!.toInt()
                                    } catch (e: Exception) {
                                        -2
                                    }
                                    database.updateMediaDuration(file.name, duration)
                                }
                                val time =
                                    StringUtils.getFormattedVideoDuration(duration.toString())
                                runOnUiThread { adapter?.updateItemDuration(model.path!!, time) }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
            } finally {
                isWorking = false
            }
        }

        fun cancelled(): Boolean {
            return cancelled
        }

        fun stopWork() {
            cancelled = true
            isWorking = false
        }
    }

    inner class ExportTask(
        private val selectedItems: List<String?>,
        private val mySimpleDialog: SimpleDialog
    ) : JTask() {
        private val mArrayPath = ArrayList<String>()
        private val mTransfer = FileTransfer()
        private val junkList = ArrayList<String?>()
        private val mUpdate: ProgressThreadUpdate = ProgressThreadUpdate(mTransfer, mySimpleDialog)
        private val database: PathsDatabase =
            PathsDatabase.getInstance(this@ViewAlbum, Storage.getDefaultStoragePath(this@ViewAlbum))
        private var allowListModification = true

        override fun workingThread() {
            try {
                var max: Long = 0
                for (item in selectedItems) {
                    val file = File(item!!)
                    max += file.length()
                }
                max /= 1024
                mUpdate.setMax(max)
                mUpdate.start()
                var start = System.currentTimeMillis()
                for (item in selectedItems) {
                    try {
                        if (this.isInterrupted) {
                            break
                        }
                        val file = File(item!!)
                        val path = database.getMediaPath(file.name)
                            ?: //need something 0.o
                            continue
                        var fileOut = File(path)
                        if (fileOut.exists()) fileOut = File(getNewFileName(fileOut))
                        Objects.requireNonNull(fileOut.parentFile).mkdirs()
                        sendUpdate(null, fileOut.name)
                        if (file.renameTo(fileOut)) {
                            mArrayPath.add(fileOut.absolutePath)
                            database.deleteMediaData(file.name)
                            addJunkItem(item)
                            //sendUpdate(ACTION_ADD_JUNK, item);
                            mTransfer.increment((fileOut.length() / 1024f).toDouble())
                        } else {
                            val output = getOutputStream(fileOut)
                            val input: InputStream = FileInputStream(file)
                            val response = mTransfer.transferStream(input, output)
                            if (FileTransfer.OK == response) {
                                if (file.delete()) {
                                    mArrayPath.add(fileOut.absolutePath)
                                    database.deleteMediaData(file.name)
                                    addJunkItem(item)
                                    //sendUpdate(ACTION_ADD_JUNK, item);
                                }
                            } else {
                                Storage.deleteFile(fileOut, this@ViewAlbum)
                            }
                        }
                        if (System.currentTimeMillis() - start >= 600 && junkList.size > 0) {
                            sendUpdate(Companion.ACTION_UPDATE_ADAPTER)
                            start = System.currentTimeMillis()
                        }
                    } catch (ignored: Exception) {
                    }
                }
                if (junkList.isNotEmpty()) {
                    sendUpdate(Companion.ACTION_UPDATE_ADAPTER)
                }
            } finally {
                mUpdate.destroy()
                database.close()
            }
        }

        override fun onBeingStarted() {
            if (myThread != null && myThread!!.isWorking) {
                myThread!!.stopWork()
            }
            mySimpleDialog.resetDialog()
            mySimpleDialog.showProgressBar(true)
            mySimpleDialog.setTitle(getString(R.string.mover))
            mySimpleDialog.setMessage("")
            mySimpleDialog.setSingleLineMessage(true)
            mySimpleDialog.setCancelable(false)
            mySimpleDialog.setNegativeButton(
                getString(R.string.cancelar),
                object : OnDialogClickListener() {
                    override fun onClick(dialog: SimpleDialog): Boolean {
                        mTransfer.cancel()
                        interrupt()
                        return true
                    }
                })
        }

        override fun onUpdated(get: Array<Any>) {
            if (Companion.ACTION_UPDATE_ADAPTER == get[0]) {
                updateAdapter()
            } else {
                val name = get[1] as String
                mySimpleDialog.setMessage(name)
            }
        }

        override fun onFinished() {
            retrieveInfo()
            kill()
        }

        private fun retrieveInfo() {
            if (myThread != null && myThread!!.cancelled() && adapter.itemCount > 0) {
                retrieveDataAndUpdate()
            }
        }

        private fun getAlternativePath(type: Int): String {
            var file = File(
                Environment.getExternalStoragePublicDirectory(if (type == 0) Environment.DIRECTORY_PICTURES else Environment.DIRECTORY_MOVIES),
                StringUtils.getFormattedDate("yyyy.MM.dd 'at' HH:mm:ss z") + if (type == 0) ".jpeg" else ".mp4"
            )
            if (file.exists()) {
                file = File(getNewFileName(file))
            }
            return file.absolutePath
        }

        override fun onException(e: Exception) {
            Toast.makeText(this@ViewAlbum, "Finished with error!", Toast.LENGTH_SHORT).show()
        }

        private fun kill() {
            Storage.scanMediaFiles(mArrayPath.toTypedArray(), this@ViewAlbum)
            mySimpleDialog.dismiss()
            if (adapter.items.isEmpty()) {
                deleteFolder()
                finish()
            }
            synchronizeMainActivity()
        }

        private fun updateAdapter() {
            allowListModification = false
            if (junkList.isNotEmpty()) {
                for (s in junkList) {
                    adapter.removeItem(s!!)
                }
                junkList.clear()
            }
            allowListModification = true
        }

        private fun deleteFolder() {
            val database = PathsDatabase.getInstance(this@ViewAlbum)
            if (folder!!.delete()) {
                database.deleteFolder(
                    folder!!.name,
                    if (position == 0) FileModel.IMAGE_TYPE else FileModel.VIDEO_TYPE
                )
            }
            database.close()
        }

        private fun addJunkItem(item: String?) {
            while (!allowListModification) {
                try {
                    Thread.sleep(10)
                } catch (ignored: InterruptedException) {
                }
            }
            junkList.add(item)
        }

        private fun getNewFileName(file: File): String {
            val path = file.absolutePath
            val lasIndexOf = path.lastIndexOf(".")
            return if (lasIndexOf != -1) concatenateParts(
                path.substring(0, lasIndexOf),
                path.substring(lasIndexOf),
                1
            ) else concatenateParts(path, "", 1)
        }

        private fun concatenateParts(part1: String, part2: String, time: Int): String {
            val file = File("$part1($time)$part2")
            return if (file.exists()) concatenateParts(
                part1,
                part2,
                time + 1
            ) else file.absolutePath
        }

        @Throws(FileNotFoundException::class)
        fun getOutputStream(file: File): OutputStream {
            var result: OutputStream? = null
            if (Build.VERSION.SDK_INT >= 21) if (Environment.isExternalStorageRemovable(file)) {
                val document = DocumentUtil.getDocumentFile(file, true, this@ViewAlbum)
                if (document != null) result =
                    this@ViewAlbum.contentResolver.openOutputStream(document.uri)
            } else {
                val parentFile = file.parentFile
                if (parentFile != null && !parentFile.exists()) {
                    parentFile.mkdirs()
                }
            }
            return result ?: FileOutputStream(file)
        }
    }

    companion object {
        private const val ACTION_UPDATE_ADAPTER = "action_update"
        private const val IMPORT_FROM_VIEW_ALBUM_CODE = 9
        private const val VIDEO_PLAY_CODE = 7
        private const val CHANGE_DIRECTORY_CODE = 3
        private const val IMPORT_FROM_GALLERY_CODE = 6
    }
}