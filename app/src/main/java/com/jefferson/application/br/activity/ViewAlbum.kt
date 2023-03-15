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
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.DisplayMetrics
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jefferson.application.br.R
import com.jefferson.application.br.adapter.MultiSelectRecyclerViewAdapter
import com.jefferson.application.br.adapter.MultiSelectRecyclerViewAdapter.ViewHolder.ClickListener
import com.jefferson.application.br.app.ProgressWatcher
import com.jefferson.application.br.app.SimpleDialog
import com.jefferson.application.br.app.SimpleDialog.OnDialogClickListener
import com.jefferson.application.br.database.AlbumDatabase
import com.jefferson.application.br.fragment.PreviewFragment
import com.jefferson.application.br.model.AlbumModel
import com.jefferson.application.br.model.FileModel
import com.jefferson.application.br.model.MediaModel
import com.jefferson.application.br.task.DeleteAlbumTask
import com.jefferson.application.br.task.JTask
import com.jefferson.application.br.task.VideoDurationUpdaterTask
import com.jefferson.application.br.util.*
import com.jefferson.application.br.view.RoundedImageView
import eightbitlab.com.blurview.BlurView
import java.io.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs

class ViewAlbum : MyCompatActivity(), ClickListener, View.OnClickListener {
    private var videoDurationUpdaterTask: VideoDurationUpdaterTask? = null
    private val minItemWidth = 110
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MultiSelectRecyclerViewAdapter
    private lateinit var floatingButton: FloatingActionButton
    private lateinit var emptyView: View
    private lateinit var menuLayout: View
    private lateinit var toolbar: Toolbar
    private lateinit var albumLabel: TextView
    private lateinit var previewContainer: FrameLayout
    private lateinit var albumDir: File
    private var previewFragment: PreviewFragment? = null
    private var selectionMode = false
    private var populateAlbumTask: JTask? = null
    private var position = 0
    private var title: String? = null

    private var baseNameDirectory: String? = null
    private var selectAllTextView: TextView? = null
    private var selectImageView: ImageView? = null
    private lateinit var appbar: AppBarLayout
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_album_layout)
        position = intent.getIntExtra("position", -1)
        title = intent.getStringExtra("name")
        albumDir = File(intent.getStringExtra("folder")!!)
        //albumModel = intent.getParcelableExtra("model")
        val filePaths: ArrayList<MediaModel>? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) intent.getParcelableArrayListExtra(
                "data",
                MediaModel::class.java
            )
            else @Suppress("DEPRECATION") intent.getParcelableArrayListExtra("data")
        val layoutManager = GridLayoutManager(this, autoSpan)
        appbar = findViewById(R.id.appbar_view_album)
        recyclerView = findViewById(R.id.my_recycler_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = layoutManager
        adapter =
            MultiSelectRecyclerViewAdapter(this@ViewAlbum, filePaths ?: ArrayList(), this, position)
        recyclerView.adapter = adapter
        val mViewUnlock = findViewById<View>(R.id.unlockView)
        val mViewDelete = findViewById<View>(R.id.deleteView)
        val mViewSelect = findViewById<View>(R.id.selectView)
        val mViewMove = findViewById<View>(R.id.moveView)

        mViewUnlock.setOnClickListener(this)
        mViewDelete.setOnClickListener(this)
        mViewMove.setOnClickListener(this)
        mViewSelect.setOnClickListener(this)
        previewContainer = findViewById(R.id.fragment_container)
        floatingButton = findViewById(R.id.view_album_fab_button)
        menuLayout = findViewById(R.id.lock_layout)
        albumLabel = findViewById(R.id.album_name_label)
        selectAllTextView = findViewById(R.id.options_album_selectTextView)
        selectImageView = findViewById(R.id.selectImageView)
        emptyView = findViewById(R.id.view_album_empty_view)
        floatingButton.setOnClickListener(this)
        albumLabel.setOnClickListener(this)

        initToolbar()
        configureBlurView(recyclerView)
        configureFloatingButton()
        populateRecyclerView()
        addBackPressedCallback()
    }

    private fun configureFloatingButton() {
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
                        if (floatingButton.isShown) {
                            floatingButton.hide()
                        }
                    } else {
                        // Scroll Up
                        if (!floatingButton.isShown) {
                            floatingButton.show()
                        }
                    }
                }
            }
        })
    }

    private val isVideoSession: Boolean
        get() {
            return position == 1
        }

    private fun addBackPressedCallback() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (previewFragment != null) {
                    recyclerView.scrollToPosition(previewFragment?.currentItem!!)
                    removePreviewFragment()
                    exitScreenCleanMode()
                    return
                }
                if (selectionMode) {
                    exitSelectionMode()
                    return
                }
                finish()
            }
        })
    }

    private fun removePreviewFragment() {
        if (previewFragment == null) return
        supportFragmentManager.beginTransaction()
            .remove(previewFragment!!).commit()
        previewFragment = null
    }

    private fun exitScreenCleanMode() {
        floatingButton.show()
        appbar.visibility = View.VISIBLE
    }

    private fun enterScreenCleanMode() {
        floatingButton.hide()
        appbar.postDelayed({
            appbar.visibility = View.INVISIBLE
        }, 150)
    }

    private val startPreviewForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val extras = result.data?.extras
            val index = extras?.getInt("index", 0)
            val removeItems = extras?.getStringArrayList(PreviewFragment.EXTRA_REMOVED_ITEMS)
            //remove all deleted/exported items in preview activity
            if (removeItems != null && removeItems.isNotEmpty()) {
                adapter.removeAll(removeItems)
                setAlbumHeader()
            }
            //scroll to last viewed item in preview activity
            if (index != null) recyclerView.post {
                recyclerView.smoothScrollToPosition(index)
            }
        }
    }

    private val changeDirectoryResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            exitSelectionMode()
            val list = result.data!!.getStringArrayListExtra("moved_files")
            Toast.makeText(
                this, list!!.size.toString() + " file(s) moved", Toast.LENGTH_SHORT
            ).show()
            adapter.removeAll(list)
            notifyChanges()
        }
    }

    private val importResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            populateRecyclerView()
            notifyChanges()
        }
    }

    private val galleryResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val data = result.data
            val paths = data!!.getStringArrayListExtra("selection")
            val type = data.getStringExtra("type")
            val intent = Intent(this, ImportMediaActivity::class.java)
            intent.putStringArrayListExtra(ImportMediaActivity.MEDIA_LIST_KEY, paths)
            intent.putExtra(ImportMediaActivity.TYPE_KEY, type)
            //intent.putExtra(ImportMediaActivity.POSITION_KEY, position);
            intent.putExtra(ImportMediaActivity.PARENT_KEY, albumDir.absolutePath)
            importResult.launch(intent)
        }
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
            R.id.album_name_label -> {
                renameAlbum()
            }
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

    override fun onDestroy() {
        if (videoDurationUpdaterTask?.status == JTask.Status.STARTED)
            videoDurationUpdaterTask?.cancelTask()

        if (populateAlbumTask?.status == JTask.Status.STARTED)
            populateAlbumTask?.cancelTask()
        super.onDestroy()
    }

    private fun importFromGallery() {
        val intent = Intent(this, ImportGalleryActivity::class.java)
        intent.putExtra("position", position)
        galleryResult.launch(intent)
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
        switchSelectAllButtonIcon()
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
                val task = DeleteFiles(this@ViewAlbum, selectedItemsPath, position, albumDir)
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
        intent.putExtra("current_path", albumDir.absolutePath)
        changeDirectoryResult.launch(intent)
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
        val database = AlbumDatabase.getInstance(this, Storage.getDefaultStoragePath(this))
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

    private fun notifyChanges() {
        val visibility = if (adapter.itemCount == 0) View.VISIBLE else View.GONE
        val mainActivity = MainActivity.instance
        emptyView.visibility = visibility
        mainActivity?.updateFragment(position) ?: Toast.makeText(
            this, "Can't synchronize MainActivity!", Toast.LENGTH_SHORT
        ).show()
        setAlbumHeader()
    }

    private fun populateRecyclerView() {
        populateAlbumTask = PopulateAlbumTask(
            context = this, dir = albumDir,
            adapter = adapter, progressBar = null,
            emptyListHint = emptyView
        )
        populateAlbumTask?.start()
        populateAlbumTask?.setOnFinishedListener {
            if (isVideoSession) {
                startVideoUpdaterTask()
            }
            setAlbumHeader()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (selectionMode) {
            menuInflater.inflate(R.menu.view_album_menu, menu)
            if (baseNameDirectory == null) {
                baseNameDirectory =
                    if (title!!.length <= 20) "$title ( %s )" else title!!.substring(
                        0, 20
                    ) + "... ( %s )"
            }
            val count = adapter.selectedItemCount.toString()
            toolbar.title = String.format(baseNameDirectory!!, count)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (selectionMode) {
                    exitSelectionMode()
                } else {
                    finish()
                }
            }
            R.id.info_item_menu -> {
                showFilesInfo()
            }
        }
        return false
    }

    override fun onItemClicked(position: Int, v: View?) {
        if (!selectionMode) {
            startPreviewActivity(position, v!!)
            return
        }
        toggleItemSelected(position, true)
        invalidateOptionsMenu()
        switchSelectAllButtonIcon()
    }

    private fun startPreviewActivity(itemPosition: Int, view: View) {
        previewFragment = PreviewFragment(adapter, itemPosition, position)
        supportFragmentManager.beginTransaction()
            .addSharedElement(
                view,
                view.transitionName
            ) /* Transition works only between fragments? Help wanted */
            .replace(R.id.fragment_container, previewFragment!!)
            .addToBackStack(null)
            .commit()
        enterScreenCleanMode()
    }

    override fun onItemLongClicked(position: Int): Boolean {
        toggleItemSelected(position, true)
        invalidateOptionsMenu()
        switchSelectAllButtonIcon()
        if (!selectionMode) {
            enterSelectionMode()
        }
        return true
    }

    private val selectedItemsPath: ArrayList<String>
        get() {
            val selectedItems = ArrayList<String>()
            for (i in adapter.getSelectedItems()) {
                selectedItems.add(adapter.items[i].path!!)
            }
            return selectedItems
        }

    private fun toggleItemSelected(position: Int, notifyAll: Boolean) {
        adapter.toggleItemSelected(position, notifyAll)
    }

    private fun switchSelectAllButtonIcon() {
        val allSelected = adapter.selectedItemCount == adapter.itemCount
        val text = if (allSelected) "Unselect all" else "Select all"
        selectAllTextView!!.text = text
        selectImageView!!.setImageResource(
            if (allSelected) R.drawable.ic_select else R.drawable.ic_select_all
        )
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
        floatingButton.hide()
    }

    private fun renameAlbum() {
        val path = albumDir.absolutePath
        val dialog = SimpleDialog(this, SimpleDialog.STYLE_INPUT)
        val input = dialog.getInputEdiText()
        input.requestFocus()
        input.setText(title)
        input.setSelection(title!!.length)
        dialog.setTitle(getString(R.string.renomear_pasta))
        dialog.setPositiveButton(getString(R.string.renomear), object : OnDialogClickListener() {
            override fun onClick(dialog: SimpleDialog): Boolean {
                val inputText = dialog.getInputText()
                if (inputText.isEmpty()) {
                    return false
                }
                val success = AlbumUtils.renameAlbum(this@ViewAlbum, path, inputText, position)
                if (success) {
                    MainActivity.instance?.updateFragment(position)
                    albumLabel.text = inputText
                }
                return true
            }
        })
        dialog.show()
    }

    fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun startVideoUpdaterTask() {
        videoDurationUpdaterTask = VideoDurationUpdaterTask(this, adapter)
        videoDurationUpdaterTask?.start()
    }

    fun exitSelectionMode() {
        selectionMode = false
        invalidateOptionsMenu()
        if (adapter.items.isNotEmpty()) {
            adapter.clearSelection()
        }
        menuLayout.animation = AnimationUtils.loadAnimation(applicationContext, R.anim.slide_bottom)
        menuLayout.visibility = View.GONE
        val dimen = resources.getDimension(R.dimen.recycler_view_padding).toInt()
        recyclerView.setPadding(dimen, dimen, dimen, dimen)
        toolbar.title = title
        floatingButton.show()
    }

    private fun initToolbar() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setTitleTextColor(ContextCompat.getColor(this, android.R.color.white))
        supportActionBar?.title = title
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val collapsingToolbar = findViewById<CollapsingToolbarLayout>(R.id.collapsing_toolbar)
        collapsingToolbar.title = title // sets the title of the toolbar
        collapsingToolbar.setCollapsedTitleTextColor(Color.WHITE) // sets the text color of the collapsed title
        collapsingToolbar.setExpandedTitleColor(Color.TRANSPARENT) // sets the text color of the expanded title
        collapsingToolbar.scrimAnimationDuration = 150
    }

    private fun setAlbumHeader() {
        val roundedImageView = findViewById<RoundedImageView>(R.id.thumbnail_image_view)
        val itemCountLabel = findViewById<TextView>(R.id.text_view_item_count)
        val albumNameLabel = findViewById<TextView>(R.id.album_name_label)
        val resId =
            if (position == 0) R.plurals.imagem_total_plural else R.plurals.video_total_plural
        var itemCount = resources.getQuantityString(resId, adapter.itemCount)
        itemCount = String.format(itemCount, adapter.itemCount)
        itemCountLabel.text = itemCount
        albumNameLabel.text = title
        roundedImageView.cornersRadius = 18f
        if (adapter.items.isNotEmpty()) {
            val path = adapter.listItemsPath[0]
            Glide.with(this).load(path).into(roundedImageView)
        } else {
            roundedImageView.setImageResource(R.drawable.ic_folder_image)
        }
    }

    fun cancelVideoUpdaterTask(): Boolean {
        if (videoDurationUpdaterTask?.status == JTask.Status.STARTED) {
            videoDurationUpdaterTask?.cancelTask()
            return true
        }
        return false
    }

    inner class DeleteFiles(activity: Activity, p1: ArrayList<String>, p3: Int, p4: File) :
        DeleteAlbumTask(activity, p1, p3, p4) {
        private var videoTaskInterrupted = false
        override fun onStarted() {
            super.onStarted()
            videoTaskInterrupted = cancelVideoUpdaterTask()
            exitSelectionMode()
        }

        override fun onInterrupted() {
            super.onInterrupted()
            if (videoTaskInterrupted && adapter.items.isNotEmpty()) {
                startVideoUpdaterTask()
            }
            notifyChanges()
        }

        override fun onFinished() {
            super.onFinished()
            notifyChanges()
            if (adapter.items.isEmpty()) {
                finish()
                return
            }
            if (videoTaskInterrupted) {
                startVideoUpdaterTask()
            }
        }

        override fun onUpdated(get: Array<Any>) {
            super.onUpdated(get)
            adapter.removeItem((get[0] as String))
        }
    }

    inner class PopulateAlbumTask(
        private val context: Context,
        private val dir: File,
        private val adapter: MultiSelectRecyclerViewAdapter,
        private val progressBar: View?,
        private val emptyListHint: View
    ) : JTask() {
        private val models: ArrayList<MediaModel> = ArrayList()
        private var showProgressRun: Runnable? = null

        override fun workingThread() {
            val database = AlbumDatabase.getInstance(context)
            for (file in dir.listFiles()!!) {
                if (isCancelled) break
                val model = MediaModel(file.absolutePath)
                models.add(model)
            }
            database.close()
        }

        override fun onStarted() {
            showProgressRun = Runnable { progressBar?.visibility = View.VISIBLE }
            progressBar?.postDelayed(showProgressRun, 100)
        }

        override fun onFinished() {
            progressBar?.removeCallbacks(showProgressRun)
            progressBar?.visibility = View.GONE

            if (models.isEmpty()) {
                emptyListHint.visibility = View.VISIBLE
            } else {
                adapter.items = models
                adapter.notifyDataSetChanged()
                emptyListHint.visibility = View.GONE
            }
        }

        override fun onException(e: java.lang.Exception?) {
            Toast.makeText(context, "Exception: ${e?.message}", Toast.LENGTH_SHORT).show()
            e?.printStackTrace()
        }
    }

    inner class ExportTask(
        private val selectedItems: List<String?>, private val simpleDialog: SimpleDialog
    ) : JTask() {
        private val filesPath = ArrayList<String>()
        private val fileTransfer = FileTransfer()
        private val junkList = ArrayList<String?>()
        private val progressUpdater: ProgressWatcher =
            ProgressWatcher(
                fileTransfer,
                simpleDialog
            )
        private val database: AlbumDatabase = AlbumDatabase.getInstance(
            this@ViewAlbum, Storage.getDefaultStoragePath(this@ViewAlbum)
        )
        private var allowListModification = true

        override fun workingThread() {
            try {
                var max: Long = 0
                for (item in selectedItems) {
                    val file = File(item!!)
                    max += file.length()
                }
                max /= 1024
                progressUpdater.setMax(max)
                progressUpdater.start()
                var start = System.currentTimeMillis()
                for (item in selectedItems) {
                    try {
                        if (this.isInterrupted) {
                            break
                        }
                        val file = File(item!!)
                        val path = database.getMediaPath(file.name) ?: //need something 0.o
                        continue
                        var fileOutput = File(path)
                        if (fileOutput.exists()) {
                            fileOutput = File(getNewFileName(fileOutput))
                        }
                        fileOutput.parentFile?.mkdirs()
                        sendUpdate(null, fileOutput.name)
                        if (file.renameTo(fileOutput)) {
                            filesPath.add(fileOutput.absolutePath)
                            database.deleteMediaData(file.name)
                            addJunkItem(item)
                            //sendUpdate(ACTION_ADD_JUNK, item);
                            fileTransfer.increment((fileOutput.length() / 1024f).toDouble())
                        } else {
                            val output = getOutputStream(fileOutput)
                            val input: InputStream = FileInputStream(file)
                            val response = fileTransfer.transferStream(input, output)
                            if (FileTransfer.OK == response) {
                                if (file.delete()) {
                                    filesPath.add(fileOutput.absolutePath)
                                    database.deleteMediaData(file.name)
                                    addJunkItem(item)
                                    //sendUpdate(ACTION_ADD_JUNK, item);
                                }
                            } else {
                                Storage.deleteFile(fileOutput, this@ViewAlbum)
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
                progressUpdater.destroy()
                database.close()
            }
        }

        override fun onStarted() {
            simpleDialog.resetDialog()
            simpleDialog.showProgressBar(true)
            simpleDialog.setTitle(getString(R.string.mover))
            simpleDialog.setMessage("")
            simpleDialog.setSingleLineMessage(true)
            simpleDialog.setCancelable(false)
            simpleDialog.setNegativeButton(
                getString(R.string.cancelar),
                object : OnDialogClickListener() {
                    override fun onClick(dialog: SimpleDialog): Boolean {
                        fileTransfer.cancel()
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
                simpleDialog.setMessage(name)
            }
        }

        override fun onFinished() {
            syncAllChanges()
        }

        private fun getAlternativePath(type: Int): String {
            var file = File(
                Environment.getExternalStoragePublicDirectory(
                    if (type == 0) Environment.DIRECTORY_PICTURES else Environment.DIRECTORY_MOVIES
                ),
                StringUtils.getFormattedDate("yyyy.MM.dd 'at' HH:mm:ss z") +
                        if (type == 0) ".jpeg" else ".mp4"
            )
            if (file.exists()) {
                file = File(getNewFileName(file))
            }
            return file.absolutePath
        }

        override fun onException(e: Exception) {
            Toast.makeText(this@ViewAlbum, "Finished with error!", Toast.LENGTH_SHORT).show()
        }

        private fun syncAllChanges() {
            Storage.scanMediaFiles(filesPath.toTypedArray(), this@ViewAlbum)
            simpleDialog.dismiss()
            if (adapter.items.isEmpty()) {
                deleteFolder()
                finish()
            }
            notifyChanges()
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
            val database = AlbumDatabase.getInstance(this@ViewAlbum)
            if (albumDir.delete()) {
                database.deleteAlbum(
                    albumDir.name,
                    if (position == 0) FileModel.IMAGE_TYPE else FileModel.VIDEO_TYPE
                )
            }
            database.close()
        }

        private fun addJunkItem(item: String?) {
            while (!allowListModification) {
                try {
                    Thread.sleep(10)
                } catch (ignored: InterruptedException) { }
            }
            junkList.add(item)
        }

        private fun getNewFileName(file: File): String {
            val path = file.absolutePath
            val lasIndexOf = path.lastIndexOf(".")
            return if (lasIndexOf != -1) concatenateParts(
                path.substring(0, lasIndexOf), path.substring(lasIndexOf), 1
            ) else concatenateParts(path, "", 1)
        }

        private fun concatenateParts(part1: String, part2: String, time: Int): String {
            val file = File("$part1($time)$part2")
            return if (file.exists()) concatenateParts(
                part1, part2, time + 1
            ) else file.absolutePath
        }

        @Throws(FileNotFoundException::class)
        fun getOutputStream(file: File): OutputStream {
            var result: OutputStream? = null
            if (Environment.isExternalStorageRemovable(file)) {
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
    }
}