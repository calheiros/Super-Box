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

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialFade

import com.google.android.material.transition.MaterialFadeThrough
import com.jefferson.application.br.R
import com.jefferson.application.br.adapter.MultiSelectRecyclerViewAdapter
import com.jefferson.application.br.fragment.PreviewFragment
import com.jefferson.application.br.fragment.ViewAlbumFragment
import java.io.File


class ViewAlbum : MyCompatActivity() {
    private var currentFrag: Fragment? = null
    private lateinit var albumDirFile: File
    private var fragmentPosition = 0
    private lateinit var title: String
    private var previewFragment: PreviewFragment? = null
    private lateinit var albumFragment : ViewAlbumFragment

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_album_layout)
        fragmentPosition = intent.getIntExtra("position", -1)
        title = intent.getStringExtra("name") as String
        albumDirFile = File(intent.getStringExtra("folder")!!)
        albumFragment = ViewAlbumFragment(title, fragmentPosition, albumDirFile, this)
        albumFragment.exitTransition = MaterialFade()
        albumFragment.reenterTransition = MaterialFadeThrough()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, albumFragment)
            .attach(albumFragment)
            .commit()
        addBackPressedListener()
    }

    private fun addBackPressedListener() {
       onBackPressedDispatcher.addCallback(
            this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {

<<<<<<< HEAD
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
           return resources.displayMetrics.heightPixels.toFloat()
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
=======
                    if (supportFragmentManager.backStackEntryCount != 0) {
                        supportFragmentManager.popBackStack()
                        return
>>>>>>> fragment_transition
                    }
                    if (albumFragment.onBackPressed())
                        finish()
                }
            })
    }

    fun startPreview(adapter: MultiSelectRecyclerViewAdapter, itemPosition: Int, view: View) {
        previewFragment = PreviewFragment(adapter, itemPosition, fragmentPosition, view.transitionName)
        previewFragment?.sharedElementEnterTransition = MaterialContainerTransform()

        supportFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .addSharedElement(view, view.transitionName)
            .replace(R.id.fragment_container, previewFragment!!)
            .attach(previewFragment!!)
            .addToBackStack(null)
            .commit()
    }

    fun removePreviewFragment() {
        if (previewFragment == null) return
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, albumFragment)
            .attach(albumFragment)
            .commit()
    }
}