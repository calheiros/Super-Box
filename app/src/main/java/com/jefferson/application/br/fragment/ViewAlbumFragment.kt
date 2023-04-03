package com.jefferson.application.br.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.*
import android.view.View.OnClickListener
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jefferson.application.br.R
import com.jefferson.application.br.activity.*
import com.jefferson.application.br.adapter.MultiSelectRecyclerViewAdapter
import com.jefferson.application.br.app.SimpleDialog
import com.jefferson.application.br.database.AlbumDatabase
import com.jefferson.application.br.model.FileModel
import com.jefferson.application.br.model.MediaModel
import com.jefferson.application.br.task.DeleteAlbumTask
import com.jefferson.application.br.task.ExportMediaTask
import com.jefferson.application.br.task.JTask
import com.jefferson.application.br.task.VideoDurationUpdaterTask
import com.jefferson.application.br.util.*
import com.jefferson.application.br.view.RoundedImageView
import eightbitlab.com.blurview.BlurView
import java.io.*
import java.util.*
import kotlin.math.abs


class ViewAlbumFragment(
    private var title: String,
    private var mPagerPosition: Int,
    private var albumDirFile: File,
    private var viewAlbum: ViewAlbum
) : Fragment(),
    MultiSelectRecyclerViewAdapter.ViewHolder.ClickListener, OnClickListener {

    private var colorAccent: Int = 0
    private var colorPrimary: Int = 0
    private var currentTheme: Int = 0
    private var videoDurationUpdaterTask: VideoDurationUpdaterTask? = null
    private val minItemWidth = 110
    lateinit var recyclerView: RecyclerView
        private set
    lateinit var adapter: MultiSelectRecyclerViewAdapter
        private set
    private lateinit var floatingButton: FloatingActionButton
    private lateinit var emptyView: View
    private lateinit var menuLayout: View
    private lateinit var toolbar: Toolbar
    private lateinit var albumLabel: TextView

    //activity result register
    private lateinit var changeDirectoryResult: ActivityResultLauncher<Intent>
    private lateinit var galleryResult: ActivityResultLauncher<Intent>
    private lateinit var importResult: ActivityResultLauncher<Intent>

    private var selectionMode = false
    private var populateAlbumTask: JTask? = null
    private var selectAllTextView: TextView? = null
    private var selectImageView: ImageView? = null
    private lateinit var appbar: AppBarLayout
    private var parent: View? = null
    private var layoutManager: LayoutManager? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (parent == null) {
            parent = inflater.inflate(R.layout.fragment_view_album, container, false)
            viewAlbum = (requireActivity() as ViewAlbum)
            layoutManager = GridLayoutManager(requireContext(), autoSpan)
            appbar = parent!!.findViewById(R.id.appbar)
            recyclerView = parent!!.findViewById(R.id.my_recycler_view)
            recyclerView.setHasFixedSize(true)
            recyclerView.layoutManager = layoutManager
            adapter =
                MultiSelectRecyclerViewAdapter(requireContext(), ArrayList(), this, mPagerPosition)
            recyclerView.adapter = adapter
            val mViewUnlock = parent!!.findViewById<View>(R.id.unlockView)
            val mViewDelete = parent!!.findViewById<View>(R.id.deleteView)
            val mViewSelect = parent!!.findViewById<View>(R.id.selectView)
            val mViewMove = parent!!.findViewById<View>(R.id.moveView)

            mViewUnlock.setOnClickListener(this)
            mViewDelete.setOnClickListener(this)
            mViewMove.setOnClickListener(this)
            mViewSelect.setOnClickListener(this)

            menuLayout = parent!!.findViewById(R.id.lock_layout)
            albumLabel = parent!!.findViewById(R.id.album_name_label)
            selectAllTextView = parent!!.findViewById(R.id.options_album_selectTextView)
            selectImageView = parent!!.findViewById(R.id.selectImageView)
            emptyView = parent!!.findViewById(R.id.view_album_empty_view)
            albumLabel.setOnClickListener(this)

            defineThemeVariables()
            configureBlurView(recyclerView)
            configureFloatingButton()
            populateRecyclerView()
            initToolbar()
            setAlbumHeader()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val window = requireActivity().window
                window.statusBarColor = Color.TRANSPARENT
                window.decorView.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            }
        }
        return parent
    }

    private fun defineThemeVariables() {
        currentTheme = ThemeConfig.getTheme(requireContext())
        colorPrimary = viewAlbum.getThemeAttributeColor(currentTheme, R.attr.colorPrimary)
        colorAccent = viewAlbum.getThemeAttributeColor(currentTheme, R.attr.colorAccent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changeDirectoryResult = registerChangeDirectoryResult()
        galleryResult = registerGalleryResult()
        importResult = registerImportResult()
    }

    private fun configureFloatingButton() {
        val threshold = heightPixels / 100 //1%
        floatingButton = parent!!.findViewById(R.id.view_album_fab_button)
        floatingButton.setOnClickListener(this)
        floatingButton.backgroundTintList = ColorStateList.valueOf(colorAccent)
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
            return mPagerPosition == 1
        }

    private val startPreviewForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val extras = result.data?.extras
            val index = extras?.getInt("index", 0)
            val removeItems = extras?.getStringArrayList(PreviewFragment.EXTRA_REMOVED_ITEMS)

            if (removeItems != null && removeItems.isNotEmpty()) {
                adapter.removeAll(removeItems)
                setAlbumHeader()
            }
            if (index != null) recyclerView.post {
                recyclerView.smoothScrollToPosition(index)
            }
        }
    }

    private fun registerChangeDirectoryResult(): ActivityResultLauncher<Intent> {
        return registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                exitSelectionMode()
                val list = result.data!!.getStringArrayListExtra("moved_files")
                Toast.makeText(
                    requireContext(), list!!.size.toString() + " file(s) moved", Toast.LENGTH_SHORT
                ).show()
                adapter.removeAll(list)
                notifyChanges()
            }
        }
    }

    private fun registerImportResult(): ActivityResultLauncher<Intent> {
        return registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                populateRecyclerView()
                notifyChanges()
            }
        }
    }

    private fun registerGalleryResult(): ActivityResultLauncher<Intent> {
        return registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val data = result.data
                val paths = data!!.getStringArrayListExtra("selection")
                val type = data.getStringExtra("type")
                val intent = Intent(requireContext(), ImportMediaActivity::class.java)
                intent.putStringArrayListExtra(ImportMediaActivity.KEY_MEDIA_LIST, paths)
                intent.putExtra(ImportMediaActivity.KEY_TYPE, type)
                intent.putExtra(ImportMediaActivity.KEY_PARENT, albumDirFile.absolutePath)
                importResult.launch(intent)
            }
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
            return requireActivity().resources.displayMetrics.heightPixels.toFloat()
        }

    private fun configureBlurView(view: ViewGroup) {
        val blurView = parent!!.findViewById<BlurView>(R.id.blurView)
        val radius = 13f
        blurView.outlineProvider = ViewOutlineProvider.BACKGROUND
        blurView.clipToOutline = true
        val render = BlurUtils.getRenderAlgorithm(requireContext())
        blurView.setupWith(view, render) // or RenderEffectBlur
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

    private fun importFromGallery() {
        val intent = Intent(viewAlbum, GalleryImportActivity::class.java)
        intent.putExtra("position", mPagerPosition)
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
        requireActivity().invalidateOptionsMenu()
        switchSelectAllButtonIcon()
    }

    private fun exportToGallery() {
        val itemsPath = selectedItemsPath
        if (itemsPath.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.selecionar_um), Toast.LENGTH_LONG)
                .show()
        } else {
            val count = adapter.selectedItemCount
            val item = count.toString() + " " + getItemName(count)
            val message = String.format(getString(R.string.mover_galeria), item)
            val dialog = SimpleDialog(requireActivity(), SimpleDialog.STYLE_ALERT)
            dialog.setIcon(R.drawable.ic_info_twotone)
            dialog.setTitle(getString(R.string.mover)).setMessage(message)
                .setPositiveButton(
                    getString(R.string.sim),
                    object : SimpleDialog.OnDialogClickListener() {
                        override fun onClick(dialog: SimpleDialog): Boolean {
                            exitSelectionMode()
                            val task = ExportMediaTask(itemsPath, dialog, mPagerPosition)
                            task.setOnFinishedListener {
                                task.simpleDialog.dismiss()
                                syncExportChanges(task.filesPath)
                            }
                            task.adapter = adapter
                            task.start()
                            return false
                        }
                    }).setNegativeButton(getString(R.string.cancelar), null).show()
        }
    }

    private fun syncExportChanges(filesPath: ArrayList<String>) {
        Storage.scanMediaFiles(filesPath.toTypedArray(), requireContext())
        if (adapter.items.isEmpty()) {
            AlbumUtils.deleteEmptyAlbum(
                albumDirFile,
                if (mPagerPosition == 0) AlbumDatabase.IMAGE_TYPE else AlbumDatabase.VIDEO_TYPE,
                requireContext()
            )
            viewAlbum.finish()
        }
        notifyChanges()
    }

    private fun deleteFilesDialog() {
        val count = adapter.selectedItemCount
        if (count == 0) {
            Toast.makeText(requireContext(), getString(R.string.selecionar_um), Toast.LENGTH_SHORT)
                .show()
            return
        }
        val item = count.toString() + " " + getItemName(count)
        val message = String.format(getString(R.string.apagar_mensagem), item)
        val dialog = SimpleDialog(requireActivity(), SimpleDialog.STYLE_ALERT_HIGH)
        dialog.showProgressBar(false)
        dialog.setTitle(getString(R.string.excluir))
        dialog.setMessage(message)
        dialog.setPositiveButton(
            getString(R.string.sim),
            object : SimpleDialog.OnDialogClickListener() {
                override fun onClick(dialog: SimpleDialog): Boolean {
                    dialog.dismiss()
                    val task =
                        DeleteFiles(requireActivity(), selectedItemsPath, mPagerPosition, albumDirFile)
                    task.start()
                    return true
                }
            })
        dialog.setNegativeButton(getString(R.string.cancelar), null)
        dialog.show()
    }

    private fun changeFilesDirectory() {
        if (adapter.selectedItemCount == 0) {
            Toast.makeText(requireContext(), getString(R.string.selecionar_um), Toast.LENGTH_SHORT)
                .show()
            return
        }
        val intent = Intent(requireContext(), FolderPickerActivity::class.java)
        intent.putExtra("selection", selectedItemsPath)
        intent.putExtra("position", mPagerPosition)
        intent.putExtra("current_path", albumDirFile.absolutePath)
        changeDirectoryResult.launch(intent)
    }

    private fun getItemName(count: Int): String {
        return (if (mPagerPosition == 0) if (count > 1) getString(R.string.imagens) else getString(R.string.imagem) else if (count > 1) getString(
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
        val database = AlbumDatabase.getInstance(
            requireContext(),
            Storage.getDefaultStoragePath(requireContext())
        )
        if (size == 1) {
            resId = R.layout.dialog_files_info
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
        SimpleDialog(requireActivity()).setTitle(getString(R.string.information))
            .setContentView(view)
            .setPositiveButton(android.R.string.ok, null).show()
        database.close()
    }

    val type: String?
        get() = when (mPagerPosition) {
            0 -> FileModel.IMAGE_TYPE
            1 -> FileModel.VIDEO_TYPE
            else -> null
        }

    private fun notifyChanges() {
        val visibility = if (adapter.itemCount == 0) View.VISIBLE else View.GONE
        val mainActivity = MainActivity.instance
        emptyView.visibility = visibility
        mainActivity?.updateFragment(mPagerPosition) ?: Toast.makeText(
            requireContext(), "Can't synchronize MainActivity!", Toast.LENGTH_SHORT
        ).show()
        setAlbumHeader()
    }

    private fun populateRecyclerView() {
        populateAlbumTask = PopulateAlbumTask(
            context = requireContext(), dir = albumDirFile,
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (selectionMode) {
            inflater.inflate(R.menu.view_album_menu, menu)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }
/*
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
    }*/

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (selectionMode) {
                    exitSelectionMode()
                } else {
                    requireActivity().finish()
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
            viewAlbum.startPreview(position, v!!)
            return
        }
        toggleItemSelected(position, true)
        requireActivity().invalidateOptionsMenu()
        switchSelectAllButtonIcon()
    }


    override fun onItemLongClicked(position: Int): Boolean {
        toggleItemSelected(position, true)
        requireActivity().invalidateOptionsMenu()
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
        requireActivity().invalidateOptionsMenu()
        menuLayout.visibility = View.VISIBLE
        val anim = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)
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
        val path = albumDirFile.absolutePath
        val dialog = SimpleDialog(requireActivity(), SimpleDialog.STYLE_INPUT)
        val input = dialog.getInputEdiText()
        input.requestFocus()
        input.setText(title)
        input.setSelection(title.length)
        dialog.setTitle(getString(R.string.renomear_pasta))
        dialog.setPositiveButton(
            getString(R.string.renomear),
            object : SimpleDialog.OnDialogClickListener() {
                override fun onClick(dialog: SimpleDialog): Boolean {
                    val inputText = dialog.getInputText()
                    if (inputText.isEmpty()) {
                        return false
                    }
                    val success =
                        AlbumUtils.renameAlbum(requireContext(), path, inputText, mPagerPosition)
                    if (success) {
                        MainActivity.instance?.updateFragment(mPagerPosition)
                        albumLabel.text = inputText
                    }
                    return true
                }
            })
        dialog.setNegativeButton(R.string.cancelar, null)
        dialog.show()
    }

    fun toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    private fun startVideoUpdaterTask() {
        videoDurationUpdaterTask = VideoDurationUpdaterTask(requireContext(), adapter)
        videoDurationUpdaterTask?.start()
    }

    fun exitSelectionMode() {
        selectionMode = false
        requireActivity().invalidateOptionsMenu()
        if (adapter.items.isNotEmpty()) {
            adapter.clearSelection()
        }
        menuLayout.animation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_bottom)
        menuLayout.visibility = View.GONE
        val dimen = resources.getDimension(R.dimen.recycler_view_padding).toInt()
        recyclerView.setPadding(dimen, dimen, dimen, dimen)
        toolbar.title = title
        floatingButton.show()
    }

    private fun initToolbar() {
        requireActivity().window.statusBarColor = colorPrimary
        toolbar = parent!!.findViewById(R.id.toolbar)
        toolbar.setTitleTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
        viewAlbum.setSupportActionBar(toolbar)
        val actionBar = viewAlbum.supportActionBar
        actionBar?.title = title
        actionBar?.setDisplayHomeAsUpEnabled(true)
        val collapsingToolbar =
            parent!!.findViewById<CollapsingToolbarLayout>(R.id.collapsing_toolbar)
        collapsingToolbar.title = title // sets the title of the toolbar
        collapsingToolbar.setCollapsedTitleTextColor(Color.WHITE) // sets the text color of the collapsed title
        collapsingToolbar.setExpandedTitleColor(Color.TRANSPARENT) // sets the text color of the expanded title
        collapsingToolbar.scrimAnimationDuration = 150
        collapsingToolbar.setContentScrimColor(colorPrimary)
        collapsingToolbar.setBackgroundColor(colorPrimary)
        collapsingToolbar.setStatusBarScrimColor(Color.TRANSPARENT)
    }

    private fun setAlbumHeader() {
        val roundedImageView = parent!!.findViewById<RoundedImageView>(R.id.thumbnail_image_view)
        val itemCountLabel = parent!!.findViewById<TextView>(R.id.text_view_item_count)
        val albumNameLabel = parent!!.findViewById<TextView>(R.id.album_name_label)
        val resId =
            if (mPagerPosition == 0) R.plurals.imagem_total_plural else R.plurals.video_total_plural
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

    fun onBackPressed(): Boolean {
        if (selectionMode) {
            exitSelectionMode()
            return false
        }
        return true
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
                requireActivity().finish()
                return
            }
            if (videoTaskInterrupted) {
                startVideoUpdaterTask()
            }
        }

        override fun onUpdated(args: Array<out Any>?) {
            super.onUpdated(args)
            adapter.removeItem((args?.get(0) as String))
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

    companion object {
        const val ACTION_UPDATE_ADAPTER = "action_update"
    }

}