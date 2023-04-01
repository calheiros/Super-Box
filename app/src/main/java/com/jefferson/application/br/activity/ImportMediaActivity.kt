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

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.*
import android.text.Html
import android.text.TextUtils
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdView
import com.google.android.material.snackbar.Snackbar
import com.jefferson.application.br.R
import com.jefferson.application.br.app.SimpleDialog
import com.jefferson.application.br.app.SimpleDialog.OnDialogClickListener
import com.jefferson.application.br.model.FileModel
import com.jefferson.application.br.task.FileModelBuilderTask
import com.jefferson.application.br.task.ImportTask
import com.jefferson.application.br.task.JTask
import com.jefferson.application.br.task.JTask.*
import com.jefferson.application.br.util.Storage
import com.jefferson.application.br.view.CircularProgressView
import java.util.*


class ImportMediaActivity : MyCompatActivity(), OnUpdatedListener, OnBeingStartedListener, OnClickListener {
    private lateinit var parent: FrameLayout
    private lateinit var prepareTextView: TextView
    private lateinit var progressView: CircularProgressView
    private lateinit var titleTextView: TextView
    private lateinit var button: Button
    private lateinit var prepareTitleView: TextView
    private lateinit var messageTextView: TextView
    private var importTask: ImportTask? = null
    private var builderTask: FileModelBuilderTask? = null
    private var animateText: AnimateProgressText? = null
    private var allowCancel = false
    private var typeQuantityRes = 0
    private var adview: AdView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.import_media_layout)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        @Suppress("DEPRECATION")
        setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
        window.statusBarColor = Color.TRANSPARENT
        prepareTitleView =
            findViewById<View>(R.id.import_media_title_preparation_text_view) as TextView
        prepareTextView = findViewById<View>(R.id.import_media_prepare_text_view) as TextView
        messageTextView = findViewById<View>(R.id.import_media_message_text_view) as TextView
        titleTextView = findViewById<View>(R.id.import_media_title_move_text_view) as TextView
        progressView = findViewById<View>(R.id.circle_progress_view) as CircularProgressView
        button = findViewById<View>(R.id.import_media_button) as Button
        val mainActivity = MainActivity.instance
        adview = if (mainActivity == null) MainActivity.createSquareAdview(this)
        else mainActivity.squareAdView
        removeParent(adview)
        parent = findViewById(R.id.ad_view_layout)
        if (adview != null)
            parent.addView(adview)
        initImportTask()
        addBackPressedCallback()
    }

    private fun initImportTask() {
        @Suppress("DEPRECATION")
        val files = intent.getParcelableArrayListExtra<FileModel>(KEY_MODELS)
        if (files != null) {
            typeQuantityRes = R.plurals.quantidade_arquivo_total
            startImportTask(files)
        } else {
            val filesPath =
                intent.getStringArrayListExtra(KEY_MEDIA_LIST) as ArrayList<String>?
            val parent = intent.getStringExtra(KEY_PARENT)
            val type = intent.getStringExtra(KEY_TYPE)
            builderTask = FileModelBuilderTask(this, filesPath!!, type, parent)
            if (type != null) {
                typeQuantityRes =
                    if (type == FileModel.IMAGE_TYPE) R.plurals.quantidade_imagem_total
                    else R.plurals.quantidade_video_total
            }
            if (parent == null) {
                builderTask?.setDestination(
                    Storage.getFolder(
                        if (FileModel.IMAGE_TYPE == type)
                            Storage.IMAGE else Storage.VIDEO, this
                    )!!.absolutePath
                )
            }
            builderTask?.setOnUpdatedListener(this)
            builderTask?.setOnFinishedListener {
                startImportTask(builderTask!!.data)
            }
            builderTask?.start()
            prepareTitleView.text = getString(R.string.checando_arquivos)
        }
    }

    private fun startImportTask(data: ArrayList<FileModel>) {
        importTask = ImportTask(this, data)
        importTask?.setOnUpdatedListener(this)
        importTask?.setOnStartedListener(this)
        importTask?.setOnFinishedListener {
            onFinished()
        }
        importTask?.start()
    }

    private val isTasksRunning: Boolean
        get() = builderTask?.status == Status.STARTED || importTask?.status == Status.STARTED
        
    override fun onBeingStarted() {
        prepareTitleView.text = getString(R.string.transferido)
        animateText = AnimateProgressText(titleTextView, importTask!!)
        animateText?.start()
    }

    fun onFinished() {
        animateText?.cancel()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val res = resources
        val criticalError: Exception? = importTask?.error()
        val failures = importTask!!.failuresCount
        val color = if (failures > 0 || importTask!!.isInterrupted) ContextCompat.getColor(
            this,
            R.color.red
        ) else getAttrColor(R.attr.commonColor)
        val msg =
            if (criticalError != null) getString(R.string.erro_critico)
            else if (failures > 0) res.getQuantityString(
                R.plurals.falha_plural, failures, failures
            ) else if (importTask?.isInterrupted == true)
                "Cancelled!" else getString(R.string.transferencia_sucesso)
        titleTextView.text = getString(R.string.resultado)
        messageTextView.setTextColor(color)
        messageTextView.text = msg
        messageTextView.maxLines = 5
        messageTextView.ellipsize = TextUtils.TruncateAt.END
        button.backgroundTintList = ColorStateList.valueOf(getAttrColor(R.attr.colorAccent))
        button.text = getString(android.R.string.ok)
        button.setOnClickListener(this)

    }

    private fun addBackPressedCallback() {
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!isTasksRunning) {
                    setResult(RESULT_OK)
                    finish()
                    return
                }

                if (allowCancel) {
                    interruptTask()
                    setResult(RESULT_OK)
                    finish()
                    return
                }

                allowCancel = true
                Snackbar.make(
                    messageTextView,
                    "Press back button again to cancel!",
                    Snackbar.LENGTH_SHORT
                ).show()
                Handler(Looper.getMainLooper()).postDelayed({ allowCancel = false }, 2000)

            }
        })
    }

    override fun onClick(v: View?) {
        if (isTasksRunning) {
            interruptTask()
        }
        setResult(RESULT_OK)
        finish()
    }


    override fun onUpdated(values: Array<Any>?) {
        values ?: return
        when (values[0] as Int) {
            1 -> {
                val format = resources.getQuantityString(
                    typeQuantityRes,
                    values[1] as Int,
                    values[1],
                    values[2]
                )

                @Suppress("DEPRECATION")
                val styledText = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) Html.fromHtml(
                    format,
                    Html.FROM_HTML_MODE_LEGACY
                ) else Html.fromHtml(format)
                prepareTextView.text = styledText
            }
            2 -> {
                if (values.size > 4) {
                    Toast.makeText(
                        this,
                        "Invalid arguments: args = " + values.size,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                val msg: String? = values[1] as String?
                if (msg != null) {
                    messageTextView.text = msg
                }
                val progress: Double? = values[2] as Double?
                if (progress != null) {
                    progressView.progress = progress
                }
                val maxProgress: Double? = values[3] as Double?
                if (maxProgress != null) {
                    progressView.max = maxProgress
                }
            }
            -2 -> showNoSpaceAlert(importTask, values[1].toString())
        }
    }

    private fun showNoSpaceAlert(task: ImportTask?, message: String) {
        val dialog = SimpleDialog(this, SimpleDialog.STYLE_ALERT)
        dialog.setTitle(getString(R.string.aviso))
        dialog.setMessage(message)
        dialog.setCancelable(false)
        dialog.setPositiveButton(getString(R.string.continuar), object : OnDialogClickListener() {
            override fun onClick(dialog: SimpleDialog): Boolean {
                task!!.stopWaiting()
                return true
            }
        })
        dialog.setNegativeButton(getString(R.string.cancelar), null)
        dialog.setOnDismissListener {
            if (task?.isWaiting == true) {
                task.interrupt()
                task.stopWaiting()
            }
        }
        dialog.show()
    }

    public override fun onResume() {
        super.onResume()
        adview?.resume()
    }

    public override fun onPause() {
        super.onPause()
        adview?.pause()
    }

    private fun interruptTask() {
        if (importTask != null && importTask!!.status == Status.STARTED) {
            importTask?.interrupt()
        }
        if (builderTask != null && builderTask!!.status == Status.STARTED) {
            builderTask?.cancelTask()
        }
    }

    private class AnimateProgressText(private var textView: TextView, private val task: JTask) :
        Thread() {
        private val text: String = textView.text.toString()
        private var suffix = ""

        private val updateHandler: Handler = object : Handler(Looper.getMainLooper()) {
            override fun dispatchMessage(msg: Message) {
                super.dispatchMessage(msg)
                textView.text = text.plus(suffix)
            }
        }

        fun cancel() {
            interrupt()
        }

        override fun run() {
            super.run()
            while (task.status == Status.STARTED) {
                try {
                    sleep(500)
                } catch (e: InterruptedException) {
                    break
                }
                updateHandler.sendEmptyMessage(0)
                suffix = if (suffix.length > 2) "" else ".".let { suffix += it; suffix }
            }
        }
    }

    companion object {
        const val KEY_TYPE = "type_key"
        const val KEY_MEDIA_LIST = "media_list_key"
        const val KEY_POSITION = "position_key"
        const val KEY_PARENT = "parent_key"
        const val KEY_MODELS = "models_key"

        fun removeParent(v: View?) {
            val parent = v?.parent
            if (parent is ViewGroup) {
                parent.removeView(v) // w  w w .j  a  va  2  s.co m
            }
        }
    }
}