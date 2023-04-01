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
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.database.getStringOrNull
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.jefferson.application.br.R
import com.jefferson.application.br.adapter.PhotosFolderAdapter
import com.jefferson.application.br.model.AlbumModel
import com.jefferson.application.br.model.FileModel
import com.jefferson.application.br.model.MediaModel
import com.jefferson.application.br.task.JTask
import com.jefferson.application.br.util.FileUtils
import com.jefferson.application.br.util.StringUtils

class GalleryImportActivity : MyCompatActivity(), OnRefreshListener {
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var myGridView: GridView
    private lateinit var context: Context
    private var objAdapter: PhotosFolderAdapter? = null
    private var position = 0
    private var title: String? = null
    private var retrieveMediaTask: RetrieveMediaTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this
        setContentView(R.layout.activity_import_gallery)
        myGridView = findViewById(R.id.gv_folder)
        position = intent.extras!!.getInt("position")
        swipeRefreshLayout = findViewById(R.id.swipe_refresh)
        swipeRefreshLayout.setOnRefreshListener(this)
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary)
        val typedValue = TypedValue()
        val theme = theme
        theme.resolveAttribute(R.attr.colorBackgroundLight, typedValue, true)
        val color = typedValue.data
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(color)
        title =
            if (position == 0) getString(R.string.importar_imagem) else getString(R.string.importar_video)
        retrieveMediaTask = RetrieveMediaTask()
        retrieveMediaTask?.start()
        setupToolbar()
    }

    private val externalAppResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val mediaList: ArrayList<String> = ArrayList()
            val data = result.data
            val fileUtils = FileUtils(this)
            if (data?.clipData != null) {
                val count = data.clipData!!.itemCount
                for (i in 0 until count) {
                    val imageUri = data.clipData!!.getItemAt(i).uri
                    val path = fileUtils.getPath(imageUri)
                    mediaList.add(path)
                }
            } else if (data?.data != null) {
                val selectedImageUri = data.data
                val path = fileUtils.getPath(selectedImageUri)
                mediaList.add(path)
            }
            sendResult(mediaList)
        }
    }

    val selectionResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val mediaList = result.data?.extras?.getStringArrayList("selection")
            if (mediaList != null)
                sendResult(mediaList)
        }
    }

    override fun onRefresh() {
        if (retrieveMediaTask!!.status == JTask.Status.FINISHED) {
            objAdapter?.clear()
            retrieveMediaTask = RetrieveMediaTask()
            retrieveMediaTask?.start()
        } else {
            swipeRefreshLayout.isRefreshing = false
        }
    }

    val type: String
        get() = when (position) {
            0 -> FileModel.IMAGE_TYPE
            1 -> FileModel.VIDEO_TYPE
            else -> throw IllegalArgumentException("could not determine media type for position: $position")
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
            externalAppResult.launch(
                Intent.createChooser(intent, "Select Picture")
            )
        }
    private val intentType: String
        get() = when (position) {
            0 -> "image/*"
            1 -> "video/*"
            else -> throw RuntimeException("could not find intent type for position $position")
        }

    val galleryItems: ArrayList<AlbumModel>
        get() {
            val galleryItems = ArrayList<AlbumModel>()
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
                    throw UnsupportedOperationException("unknown media type for position: $position")
                }
            }

            val projection = when (position) {
                0 -> arrayOf(MediaStore.MediaColumns.DATA, bucketName)

                1 -> arrayOf(
                    MediaStore.Video.VideoColumns.DURATION,
                    MediaStore.MediaColumns.DATA,
                    bucketName
                )
                else -> null
            }

            cursor = applicationContext.contentResolver.query(
                uri,
                projection,
                null,
                null,
                "$orderBy DESC"
            )
            var absolutePathOfImage: String?
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
                val folderPosition = getAlbumIndexByName(galleryItems, folderName)
                if (folderPosition == -1) {
                    val model = AlbumModel()
                    val mm = MediaModel(absolutePathOfImage)
                    if (duration != null)
                        mm.duration = StringUtils.getFormattedVideoDuration(duration)
                    model.name = cursor.getStringOrNull(columnIndexFolderName) ?: "0"
                    model.addItem(mm)
                    galleryItems.add(model)
                } else {
                    val mm = MediaModel(absolutePathOfImage)
                    if (position == 1) {
                        val formattedTime =
                            StringUtils.getFormattedVideoDuration(duration.toString())
                        mm.duration = formattedTime
                    }
                    galleryItems[folderPosition].addItem(mm)
                }
            }
            cursor.close()
            AlbumModel.sort(galleryItems)
            return galleryItems
        }

    private fun getAlbumIndexByName(list: ArrayList<AlbumModel>, name: String?): Int {
        @Suppress("NAME_SHADOWING")
        val name: String = name ?: AlbumModel.NO_ALBUM_NAME
        for (i in list.indices) {
            val folderName = list[i].name
            if (name == folderName) return i
        }
        return -1
    }

    private fun setAdapter(list: ArrayList<AlbumModel>) {
        val emptyLayout = findViewById<View>(R.id.gallery_album_empty_layout)
        emptyLayout.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        objAdapter = PhotosFolderAdapter(this@GalleryImportActivity, list, position)
        myGridView.adapter = objAdapter
    }

    private fun sendResult(mediaList: ArrayList<String>) {
        val intent = Intent()
        intent.putExtra("selection", mediaList)
        intent.putExtra("type", type)
        intent.putExtra("position", position)
        setResult(RESULT_OK, intent)
        finish()
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
                        this@GalleryImportActivity,
                        "The app was not allowed to read or write to your storage. Hence, it" +
                                " cannot function properly. Please consider granting it this permission",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private inner class RetrieveMediaTask : JTask() {
        private var result: ArrayList<AlbumModel>? = null
        private lateinit var progressBar: ProgressBar

        override fun workingThread() {
            result = galleryItems
        }

        override fun onStarted() {
            progressBar = findViewById(R.id.galleryalbumProgressBar)
            progressBar.visibility = View.VISIBLE
        }

        override fun onFinished() {
            progressBar.visibility = View.GONE
            if (result != null) {
                setAdapter(result!!)
            } else {
                Toast.makeText(context, "", Toast.LENGTH_LONG).show()
            }
            swipeRefreshLayout.isRefreshing = false
        }

        override fun onException(e: Exception?) {
            Toast.makeText(context, e?.message, Toast.LENGTH_LONG).show()
            progressBar.visibility = View.GONE
        }
    }

    companion object {
        const val GET_CODE = 5658
        private const val REQUEST_PERMISSIONS = 100
    }

}