package com.jefferson.application.br.task

import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.jefferson.application.br.R
import com.jefferson.application.br.adapter.MultiSelectRecyclerViewAdapter
import com.jefferson.application.br.app.ProgressWatcher
import com.jefferson.application.br.app.SimpleDialog
import com.jefferson.application.br.database.AlbumDatabase
import com.jefferson.application.br.fragment.ViewAlbumFragment
import com.jefferson.application.br.util.*
import java.io.*
import java.util.ArrayList

class ExportMediaTask(
    private val selectedItems: List<String?>,
    val simpleDialog: SimpleDialog, val pagerPosition: Int
) : JTask() {

    private val TAG: String = "ImportMediaTask"
    var adapter: MultiSelectRecyclerViewAdapter? = null
    val filesPath = ArrayList<String>()
    private val context = simpleDialog.context
    private val fileTransfer = FileTransfer()
    private val junkList = ArrayList<String?>()

    private val progressUpdater: ProgressWatcher =
        ProgressWatcher(
            fileTransfer,
            simpleDialog
        )

    private val database: AlbumDatabase = AlbumDatabase.getInstance(
        context, Storage.getDefaultStoragePath(context)
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
            for (filePath in selectedItems) {
                try {
                    if (this.isInterrupted) {
                        break
                    }
                    val file = File(filePath!!)
                    val path = database.getMediaPath(file.name) ?: run {
                        Log.d(TAG, "getMediaPath returned null")
                        Storage.getAlternativePath(
                            if (pagerPosition == 0)
                                Storage.IMAGE else Storage.VIDEO
                        )
                    }
                    var fileOutput = File(path)
                    val parent = fileOutput.parentFile
                    if (parent?.mkdirs() != true) {
                        Log.e(
                            "ExportMediaTask", "Error to create parent file: " +
                                    parent?.absolutePath
                        )
                    }
                    if (fileOutput.exists()) {
                        fileOutput = File(Storage.generateFileName(fileOutput))
                    }

                    postUpdate({}, fileOutput.name)
                    if (file.renameTo(fileOutput)) {
                        filesPath.add(fileOutput.absolutePath)
                        database.deleteMediaData(file.name)
                        addJunkItem(path)
                        fileTransfer.increment((fileOutput.length() / 1024f).toDouble())
                    } else {
                        val output = getOutputStream(fileOutput)
                        val input: InputStream = FileInputStream(file)
                        val response = fileTransfer.transferStream(input, output)
                        if (FileTransfer.OK == response) {
                            if (file.delete()) {
                                filesPath.add(fileOutput.absolutePath)
                                database.deleteMediaData(file.name)
                                addJunkItem(path)
                            }
                        } else {
                            Storage.deleteFile(fileOutput, context)
                        }
                    }
                    if (System.currentTimeMillis() - start >= 600 && junkList.size > 0) {
                        postUpdate(ViewAlbumFragment.ACTION_UPDATE_ADAPTER)
                        start = System.currentTimeMillis()
                    }
                } catch (ignored: Exception) {
                }
            }
            if (junkList.isNotEmpty()) {
                postUpdate(ViewAlbumFragment.ACTION_UPDATE_ADAPTER)
            }
        } finally {
            progressUpdater.destroy()
            database.close()
        }
    }

    override fun onStarted() {
        simpleDialog.resetDialog()
        simpleDialog.showProgressBar(true)
        simpleDialog.setTitle(context.getString(R.string.mover))
        simpleDialog.setMessage("")
        simpleDialog.setSingleLineMessage(true)
        simpleDialog.setCancelable(false)
        simpleDialog.setNegativeButton(
            context.getString(R.string.cancelar),
            object : SimpleDialog.OnDialogClickListener() {
                override fun onClick(dialog: SimpleDialog): Boolean {
                    fileTransfer.cancel()
                    interrupt()
                    return true
                }
            })
    }

    override fun onFinished() {
        Log.i("ExportTask", "List size: " + selectedItems.size)
    }

    private fun updateAdapter() {
        adapter ?: return
        allowListModification = false
        if (junkList.isNotEmpty()) {
            for (s in junkList) {
                adapter!!.removeItem(s!!)
            }
            junkList.clear()
        }
        allowListModification = true
    }

    override fun onUpdated(args: Array<out Any>?) {
        if (ViewAlbumFragment.ACTION_UPDATE_ADAPTER == args?.get(0)) {
            updateAdapter()
        } else {
            val name = args?.get(1) as String
            simpleDialog.setMessage(name)
        }
    }

    override fun onException(e: Exception?) {
        Toast.makeText(context, "Finished with error!", Toast.LENGTH_SHORT).show()
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


    @Throws(FileNotFoundException::class)
    fun getOutputStream(file: File): OutputStream {
        var result: OutputStream? = null
        if (Environment.isExternalStorageRemovable(file)) {
            val document = DocumentUtil.getDocumentFile(file, true, context)
            if (document != null) result =
                context.contentResolver.openOutputStream(document.uri)
        } else {
            val parentFile = file.parentFile
            if (parentFile != null && !parentFile.exists()) {
                parentFile.mkdirs()
            }
        }
        return result ?: FileOutputStream(file)
    }
}
