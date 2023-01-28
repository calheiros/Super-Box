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

import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.GridView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.jefferson.application.br.FileModel
import com.jefferson.application.br.R
import com.jefferson.application.br.adapter.PhotosFolderAdapter
import com.jefferson.application.br.model.FolderModel
import com.jefferson.application.br.model.MediaModel
import com.jefferson.application.br.task.JTask
import com.jefferson.application.br.util.FileUtils
import com.jefferson.application.br.util.StringUtils

class ImportGalleryActivity : MyCompatActivity(), OnRefreshListener {
    private lateinit var mySwipeRefreshLayout: SwipeRefreshLayout
    private lateinit var myGridView: GridView

    private var objAdapter: PhotosFolderAdapter? = null
    private var position = 0

    private var title: String? = null

    private var retrieveMediaTask: RetrieveMediaTask? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.import_gallery)
        myGridView = findViewById(R.id.gv_folder)
        position = intent.extras!!.getInt("position")
        mySwipeRefreshLayout = findViewById(R.id.swipe_refresh)
        mySwipeRefreshLayout.setOnRefreshListener(this)
        mySwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary)
        val typedValue = TypedValue()
        val theme = theme
        theme.resolveAttribute(R.attr.colorBackgroundLight, typedValue, true)
        val color = typedValue.data
        mySwipeRefreshLayout.setProgressBackgroundColorSchemeColor(color) // .setProgressBackgroundColor(color);
        title =
            if (position == 0) getString(R.string.importar_imagem) else getString(R.string.importar_video)
        retrieveMediaTask = RetrieveMediaTask()
        retrieveMediaTask!!.start()
        setupToolbar()
    }

    override fun onRefresh() {
        if (retrieveMediaTask!!.getStatus() == JTask.Status.FINISHED) {
            objAdapter!!.clear()
            retrieveMediaTask = RetrieveMediaTask()
            retrieveMediaTask!!.start()
        } else {
            mySwipeRefreshLayout.isRefreshing = false
        }
    }

    val type: String
        get() = when (position) {
            0 -> FileModel.IMAGE_TYPE
            1 -> FileModel.VIDEO_TYPE
            else -> throw IllegalArgumentException("can not find type for position: $position")
        }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.setDisplayShowHomeEnabled(true)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.title = title

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_import, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.item_from_gallery) {
            contentFromExternalApp
        } else if (id == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private val contentFromExternalApp: Unit
        get() {
            val intent = Intent()
            intent.type = intentType
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(intent, "Select Picture"),
                PICK_CONTENT_FROM_EXTERNAL_APP
            )
        }
    private val intentType: String
         get() = when (position) {
            0 -> "image/*"
            1 -> "video/*"
            else -> throw RuntimeException("can not find intent type for position $position")
        }

    private fun notImplemented() {
        Toast.makeText(this, "Not implemented!", Toast.LENGTH_SHORT).show()
    }

    val galleryItems: ArrayList<FolderModel>?
        get() {
            val galleryItems = ArrayList<FolderModel>()
            val uri: Uri
            val cursor: Cursor?
            val orderBy: String
            val bucketName: String
            when (position) {
                0 -> {
                    bucketName = MediaStore.Images.Media.BUCKET_DISPLAY_NAME
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    orderBy = MediaStore.Images.Media.DATE_TAKEN
                }
                1 -> {
                    bucketName = MediaStore.Video.Media.BUCKET_DISPLAY_NAME
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    orderBy = MediaStore.Video.Media.DATE_TAKEN
                }
                else -> {
                    return null
                }
            }
            var absolutePathOfImage: String?
            var projection = arrayOf(MediaStore.MediaColumns.DATA, bucketName)
            if (position == 1) {
                projection = arrayOf(
                    MediaStore.Video.VideoColumns.DURATION,
                    MediaStore.MediaColumns.DATA,
                    bucketName
                )
            }
            cursor = applicationContext.contentResolver.query(
                uri,
                projection,
                null,
                null,
                "$orderBy DESC"
            )
            val columnIndexData: Int = cursor!!.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
            val columnIndexFolderName: Int = cursor.getColumnIndexOrThrow(bucketName)
            val columnIndexDuration = cursor.getColumnIndex(MediaStore.Video.Media.DURATION)
            while (cursor.moveToNext()) {
                var duration: String? = null
                if (position == 1) {
                    duration = cursor.getString(columnIndexDuration)
                }
                absolutePathOfImage = cursor.getString(columnIndexData)
                val folderName = cursor.getString(columnIndexFolderName)
                val folderPosition = getFolderIndex(galleryItems, folderName)
                if (folderPosition == -1) {
                    val model = FolderModel()
                    val mm = MediaModel(absolutePathOfImage)
                    if (position == 1) mm.duration = StringUtils.getFormattedVideoDuration(duration)
                    model.name = cursor.getString(columnIndexFolderName)
                    model.addItem(mm)
                    galleryItems.add(model)
                } else {
                    val mm = MediaModel(absolutePathOfImage)
                    if (position == 1) {
                        val formattedTime = StringUtils.getFormattedVideoDuration(duration)
                        mm.duration = formattedTime
                    }
                    galleryItems[folderPosition].addItem(mm)
                }
            }
            cursor.close()
            FolderModel.sort(galleryItems)
            return galleryItems
        }

    private fun getFolderIndex(list: ArrayList<FolderModel>, name: String?): Int {
        @Suppress("NAME_SHADOWING")
        val name: String = name ?: FolderModel.NO_FOLDER_NAME
        for (i in list.indices) {
            val folderName = list[i].name
            if (name == folderName) return i
        }
        return -1
    }

    private fun setAdapter(list: ArrayList<FolderModel>) {
        if (list.isEmpty()) {
            findViewById<View>(R.id.gallery_album_empty_layout).visibility = View.VISIBLE
        }
        objAdapter = PhotosFolderAdapter(this@ImportGalleryActivity, list, position)
        myGridView.adapter = objAdapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            var mediaList: ArrayList<String?>? = ArrayList()
            if (requestCode == PICK_CONTENT_FROM_EXTERNAL_APP) {
                val fileUtils = FileUtils(this)
                if (data!!.clipData != null) {
                    val count = data.clipData!!.itemCount
                    for (i in 0 until count) {
                        val imageUri = data.clipData!!.getItemAt(i).uri //do what do you want to do
                        val path = fileUtils.getPath(imageUri)
                        mediaList!!.add(path)
                    }
                } else if (data.data != null) {
                    val selectedImageUri = data.data //do what do you want to do
                    val path = fileUtils.getPath(selectedImageUri)
                    mediaList!!.add(path)
                } else {
                    return
                }
            } else {
                mediaList = data!!.extras!!.getStringArrayList("selection")
            }
            if (mediaList!!.isEmpty()) {
                return
            }
            val i = Intent()
            i.putExtra("selection", mediaList)
            i.putExtra("type", type)
            i.putExtra("position", position)
            setResult(RESULT_OK, i)
            finish()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS) {
            for (grantResult in grantResults) {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    galleryItems
                } else {
                    Toast.makeText(
                        this@ImportGalleryActivity,
                        "The app was not allowed to read or write to your storage. Hence, it cannot function properly. Please consider granting it this permission",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private inner class RetrieveMediaTask : JTask() {
        private var result: ArrayList<FolderModel>? = null
        private lateinit var myProgress: ProgressBar

        override fun workingThread() {
            result = galleryItems
        }

        override fun onBeingStarted() {
            myProgress = findViewById(R.id.galleryalbumProgressBar)
            myProgress.visibility = View.VISIBLE
        }

        override fun onFinished() {
            myProgress.visibility = View.GONE
            if (result != null) {
                setAdapter(result!!)
            }
            mySwipeRefreshLayout.isRefreshing = false
        }

        override fun onException(e: Exception) {}
    }

    companion object {
        const val GET_CODE = 5658
        private const val REQUEST_PERMISSIONS = 100
        private const val TAG = "ImportGalleryActivity"
        private const val PICK_CONTENT_FROM_EXTERNAL_APP = 1
    }
}