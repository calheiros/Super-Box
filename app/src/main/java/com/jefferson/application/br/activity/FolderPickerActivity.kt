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

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jefferson.application.br.model.FileModel
import com.jefferson.application.br.R
import com.jefferson.application.br.activity.MainActivity.Companion.instance
import com.jefferson.application.br.adapter.FilePickerAdapter
import com.jefferson.application.br.app.SimpleDialog
import com.jefferson.application.br.app.SimpleDialog.OnDialogClickListener
import com.jefferson.application.br.database.AlbumDatabase
import com.jefferson.application.br.model.PickerModel
import com.jefferson.application.br.task.JTask
import com.jefferson.application.br.util.AlbumUtils
import com.jefferson.application.br.util.Storage
import java.io.File
import java.util.*

class FolderPickerActivity : MyCompatActivity(), OnItemClickListener {
    private var currentPath: String? = null
    private lateinit var pickerAdapter: FilePickerAdapter
    private var myOverlay: View? = null
    private var paths: List<String>? = null
    private var position = 0
    private lateinit var fab: FloatingActionButton

    inner class MoveFilesTask(str: String) : JTask() {
        override fun onException(e: Exception?) {
            Toast.makeText(this@FolderPickerActivity, "Error!", Toast.LENGTH_SHORT).show()
        }

        private var movedArray: ArrayList<String> = ArrayList()
        private var folder: String
        var dialog: SimpleDialog? = null

        init {
            folder = str
        }

        override fun onStarted() {
            dialog = SimpleDialog(this@FolderPickerActivity, SimpleDialog.STYLE_PROGRESS)
            dialog?.setMax(paths!!.size)
            dialog?.setTitle(getString(R.string.movendo))
            dialog?.setProgress(0)
            dialog?.show()
        }

        override fun onUpdated(objArr:  Array<out Any>?) {
            dialog?.setProgress((objArr?.get(0) as Int))
        }

        override fun onFinished() {
            dialog?.dismiss()
            val intent = Intent()
            intent.putExtra("moved_files", movedArray)
            setResult(RESULT_OK, intent)
            finish()
        }

        override fun workingThread() {
            for (str in paths!!) {
                val file = File(str)
                val newFile = File(folder, file.name)
                if (file.renameTo(newFile)) {
                    movedArray.add(str)
                }
                postUpdate(dialog!!.getProgress() + 1)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.file_picker_layout)
        val listView = findViewById<ListView>(R.id.androidList)
        val mToolbar = findViewById<Toolbar>(R.id.toolbar)
        myOverlay = findViewById(R.id.myOverlayLayout)
        position = intent.getIntExtra("position", -1)
        paths = intent.getStringArrayListExtra("selection")
        currentPath = intent.getStringExtra("current_path")
        pickerAdapter = FilePickerAdapter(getModels(position), this, position)
        listView.adapter = pickerAdapter
        listView.onItemClickListener = this
        setSupportActionBar(mToolbar)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.title = getString(R.string.maove_to)

        fab = findViewById(R.id.fab)
        fab.setOnClickListener {
            MoveFilesTask(
                pickerAdapter.models[pickerAdapter.selectedItem].path!!
            ).start()
            fab.hide()
        }
        fab.visibility = View.GONE
    }

    override fun onItemClick(adapterView: AdapterView<*>?, view: View, i: Int, j: Long) {
        val selectedItem = pickerAdapter.selectedItem
        if (selectedItem == -1) {
            fab.visibility = View.VISIBLE
            val slideUpAnimation =
                AnimationUtils.loadAnimation(this@FolderPickerActivity, R.anim.slide_up)
            slideUpAnimation.interpolator = DecelerateInterpolator()
            fab.startAnimation(slideUpAnimation)
        }
        if (selectedItem != i) {
            val overlay = view.findViewById<View>(R.id.folder_picker_check_overlay)
            val anim = AnimationUtils.loadAnimation(this@FolderPickerActivity, R.anim.fade_in)
            anim.duration = 250
            overlay.startAnimation(anim)
            pickerAdapter.selectedItem = i
        }
    }

    fun updateChanges() {
        val mainActivity = instance
        mainActivity?.updateFragment(position)
        pickerAdapter.update(getModels(position))
    }

    @SuppressLint("InflateParams")
    private fun createFolder() {
        val dialog = SimpleDialog(this, SimpleDialog.STYLE_INPUT)
        dialog.setTitle(getString(R.string.criar_pasta))
        dialog.setNegativeButton(getString(android.R.string.cancel), null)
        dialog.setPositiveButton(getString(android.R.string.ok), object : OnDialogClickListener() {
            override fun onClick(dialog: SimpleDialog): Boolean {
                val name = dialog.getInputText()
                val result = AlbumUtils.validateName(name, this@FolderPickerActivity)
                if (!result.ok) {
                    Toast.makeText(this@FolderPickerActivity, result.message, Toast.LENGTH_SHORT)
                        .show()
                    return false
                }
                val success =
                    AlbumUtils.createAlbum(this@FolderPickerActivity, name, position) != null
                if (success) {
                    updateChanges()
                }
                return success
            }
        }).show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_file_picker, menu)
        return true
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            android.R.id.home -> finish()
            R.id.item_create_folder -> createFolder()
        }
        return super.onOptionsItemSelected(menuItem)
    }

    private fun getModels(i: Int): List<PickerModel> {
        val arrayList = ArrayList<PickerModel>()
        val storageAndFolder = Storage.getFolder(if (i == 0) Storage.IMAGE else Storage.VIDEO, this)
        val instance = AlbumDatabase.getInstance(this)
        val listFiles = storageAndFolder?.listFiles()!!
        for (file in listFiles) {
            if (file.isDirectory && file.absolutePath != currentPath) {
                val pickerModel = PickerModel()
                val dirList = file.listFiles()
                var folderName = instance.getAlbumName(
                    file.name,
                    if (position == 0) FileModel.IMAGE_TYPE else FileModel.VIDEO_TYPE
                )
                if (folderName == null) {
                    folderName = file.name
                }
                val length = dirList!!.size
                if (length > 0) {
                    pickerModel.thumbnailPath = dirList[0].absolutePath
                }
                pickerModel.path = file.absolutePath
                pickerModel.name = folderName
                pickerModel.size = length
                arrayList.add(pickerModel)
            }
        }
        val visibility = if (arrayList.isEmpty()) View.VISIBLE else View.GONE
        if (myOverlay!!.visibility != visibility) {
            myOverlay?.visibility = visibility
        }
        //sort files in alphabetically
        arrayList.sortWith { model1: PickerModel, model2: PickerModel ->
            model1.name!!.lowercase(
                Locale.getDefault()
            ).compareTo(
                model2.name!!.lowercase(Locale.getDefault())
            )
        }
        return arrayList
    }
}