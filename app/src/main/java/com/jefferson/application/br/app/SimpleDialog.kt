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
package com.jefferson.application.br.app

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.TextUtils.TruncateAt
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.jefferson.application.br.R
import com.jefferson.application.br.library.NumberProgressBar
import com.jefferson.application.br.util.BlurUtils
import eightbitlab.com.blurview.BlurView

class SimpleDialog {
    private lateinit var progressBar: NumberProgressBar
    private lateinit var buttonsLayout: View
    private val activity: Activity
    private lateinit var parentView: ViewGroup
    private lateinit var progressBarDialog: SimpleDialog

    private val mHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            progressBar.progress = msg.what
        }
    }
    private var contentText: TextView? = null
    private var contentTitle: TextView? = null
    var maxBytes: Long = 0
    private var currentBytes: Long = 0
    private var progress = 0
    private var isDismissed = false
    private var positiveButton: Button? = null
    private var negativeButton: Button? = null
    private var dialog: JDialog? = null
    private var editTextLayout: View? = null
    private var iconView: ImageView? = null
    private var menuListView: ListView? = null

    constructor(activity: Activity, style: Int) {
        this.activity = activity
        createView(style)
    }

    constructor(activity: Activity) {
        this.activity = activity
        createView(0)
    }

    fun cancel() {
        dialog?.cancel()
    }

    fun setCanceledOnTouchOutside(cancelable: Boolean) {
        dialog?.setCanceledOnTouchOutside(cancelable)
    }

    fun registerBytes(count: Long) {
        currentBytes = count
    }

    val max: Int
        get() = progressBar.max

    fun setMax(value: Int): SimpleDialog {
        progressBar.max = value
        return this
    }

    fun getProgress(): Int {
        return progress
    }

    fun setSingleLineMessage(single: Boolean): SimpleDialog {
        val ellipsize = if (single) TruncateAt.MIDDLE else TruncateAt.END
        val maxLines = if (single) 1 else 256
        contentText?.maxLines = maxLines
        contentText?.ellipsize = ellipsize
        return this
    }

    private fun createView(style: Int) {
        progressBarDialog = this
        parentView =
            LayoutInflater.from(activity).inflate(R.layout.dialog_main_layout, null) as ViewGroup
        progressBar = parentView.findViewById(R.id.number_progress_bar)
        val blurView = parentView.findViewById<BlurView>(R.id.blurView)
        contentTitle = parentView.findViewById(R.id.title_text_view)
        contentText = parentView.findViewById(R.id.message_text_view)
        positiveButton = parentView.findViewById(R.id.dialogPositiveButton)
        negativeButton = parentView.findViewById(R.id.dialogNegativeButton)
        editTextLayout = parentView.findViewById(R.id.dialog_edit_text_layout)
        iconView = parentView.findViewById(R.id.dialog_icon)
        buttonsLayout = parentView.findViewById(R.id.dialog_buttons_layout)
        dialog = JDialog(activity, style == STYLE_INPUT)
        val color = ContextCompat.getColor(activity, R.color.colorAccent)
        progressBar.max = 100
        progressBar.reachedBarColor = color
        progressBar.setProgressTextColor(color)
        configureBlur(activity, blurView)
        configureStyle(style)
    }

    private fun configureBlur(activity: Activity, blurView: BlurView) {
        val radius = 13f
        val decorView = activity.window.decorView
        // ViewGroup you want to start blur from. Choose root as close to BlurView in hierarchy as possible.
        val rootView = decorView.findViewById<ViewGroup>(android.R.id.content)
        val windowBackground = decorView.background
        blurView.outlineProvider = ViewOutlineProvider.BACKGROUND
        blurView.clipToOutline = true
        blurView.setBlurAutoUpdate(true)
        val render = BlurUtils.getRenderAlgorithm(activity)
        blurView.setupWith(rootView, render) // or RenderEffectBlur
            .setFrameClearDrawable(windowBackground) // Optional
            .setBlurRadius(radius)
    }

    fun resetDialog() {
        //reset proprieties
        showNegativeButton(false)
        showPositiveButton(false)
        showTextMessage(false)
        setSingleLineMessage(false)
        showProgressBar(false)
    }

    fun setMenuItems(items: List<MenuItem>, listener: OnItemClickListener?) {
        if (menuListView == null) {
            createMenu()
            Log.w(TAG, "menuListView was not created early!")
        }
        menuListView?.adapter = DialogMenuAdapter(items, context)
        menuListView?.onItemClickListener = listener
    }

    private fun configureStyle(style: Int) {
        when (style) {
            STYLE_ALERT_HIGH -> setIcon(R.drawable.ic_alert_rounded_auth)
            STYLE_ALERT_MEDIUM, STYLE_MENU -> createMenu()
            STYLE_PROGRESS -> showProgressBar(true)
            STYLE_INPUT -> {}
        }
    }

    private fun createMenu() {
        val menuView = (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
            .inflate(R.layout.dialog_list_view_layout, parentView, false)
        menuListView = menuView.findViewById<View>(R.id.dialog_list_view) as ListView
        setContentView(menuView)
    }

    private fun showTextMessage(show: Boolean) {
        contentText?.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showEditText(show: Boolean) {
        val visibility = if (show) View.VISIBLE else View.GONE
        if (editTextLayout != null) editTextLayout?.visibility = visibility
    }

    fun setContentView(view: View?): SimpleDialog {
        (parentView.findViewById<View>(R.id.dialog_layout_container) as ViewGroup).addView(view)
        return this
    }

    fun setProgress(newProgress: Int): SimpleDialog {
        mHandler.sendEmptyMessage(newProgress)
        progress = newProgress
        return this
    }

    fun setCancelable(cancelable: Boolean): SimpleDialog {
        dialog?.setCancelable(false)
        return this
    }

    fun setPositiveButton(stringId: Int, listener: OnDialogClickListener?): SimpleDialog {
        setPositiveButton(activity.getString(stringId), listener)
        return this
    }

    fun setNegativeButton(stringId: Int, listener: OnDialogClickListener?): SimpleDialog {
        setNegativeButton(activity.getString(stringId), listener)
        return this
    }

    fun setOnDismissListener(listener: DialogInterface.OnDismissListener?): SimpleDialog {
        dialog?.setOnDismissListener(listener)
        return this
    }

    fun dismiss() {
        dialog?.dismiss()
    }

    fun showProgressBar(show: Boolean): SimpleDialog {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        return this
    }

    fun showPositiveButton(show: Boolean): SimpleDialog {
        positiveButton?.visibility = if (show) View.VISIBLE else View.GONE
        return this
    }

    fun showNegativeButton(show: Boolean): SimpleDialog {
        negativeButton?.visibility = if (show) View.VISIBLE else View.GONE
        return this
    }

    fun setTitle(titleId: Int): SimpleDialog {
        setTitle(activity.getString(titleId))
        return this
    }

    val context: Context
        get() = activity

    fun show(): SimpleDialog {
        dialog!!.show()
        return this
    }

    fun setTitle(title: String): SimpleDialog {
        if (contentTitle?.visibility != View.VISIBLE) contentTitle?.visibility = View.VISIBLE
        contentTitle?.text = title
        return this
    }

    fun setMessage(text: String): SimpleDialog {
        if (contentText?.visibility != View.VISIBLE) contentText!!.visibility = View.VISIBLE
        contentText?.text = text
        return this
    }

    fun setPositiveButton(buttonText: String?, listener: OnDialogClickListener?): SimpleDialog {
        if (positiveButton!!.visibility != View.VISIBLE) positiveButton!!.visibility = View.VISIBLE
        if (buttonsLayout.visibility != View.VISIBLE) {
            buttonsLayout.visibility = View.VISIBLE
        }
        positiveButton!!.text = buttonText
        positiveButton!!.setOnClickListener(OnClickListener(listener))
        return this
    }

    fun setNegativeButton(buttonText: String?, listener: OnDialogClickListener?): SimpleDialog {
        if (negativeButton!!.visibility != View.VISIBLE) negativeButton!!.visibility = View.VISIBLE
        if (buttonsLayout.visibility != View.VISIBLE) {
            buttonsLayout.visibility = View.VISIBLE
        }
        negativeButton!!.text = buttonText
        negativeButton!!.setOnClickListener(OnClickListener(listener))
        return this
    }

    fun setIcon(resId: Int) {
        iconView?.visibility = View.VISIBLE
        iconView?.setImageResource(resId)
    }

    fun setIconColor(color: Int) {
        iconView?.setColorFilter(color)
    }

    abstract class OnDialogClickListener {
        abstract fun onClick(dialog: SimpleDialog): Boolean
    }

    class MenuItem {
        @JvmField
        var name: String

        @JvmField
        var icon: Int
        var applyIconTint: Boolean

        constructor(name: String, iconRes: Int) {
            this.name = name
            icon = iconRes
            applyIconTint = true
        }

        constructor(name: String, iconRes: Int, applyIconTint: Boolean) {
            this.name = name
            icon = iconRes
            this.applyIconTint = applyIconTint
        }
    }

    class DialogMenuAdapter(private val options: List<MenuItem>, private val context: Context) :
        BaseAdapter() {
        private val defaultTint: Int

        init {
            val theme = context.theme
            val value = TypedValue()
            theme.resolveAttribute(R.attr.commonColorLight, value, true)
            defaultTint = value.data
        }

        override fun getCount(): Int {
            return options.size
        }

        override fun getItem(position: Int): Any {
            return options[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
            var convertView = convertView
            val holder: DialogHolder
            val item = options[position]
            if (convertView == null) {
                convertView =
                    (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
                        .inflate(R.layout.dialog_menu_item_layout, parent, false)
                holder = DialogHolder()
                holder.textView = convertView.findViewById(R.id.dialog_item_text_view)
                holder.imageView = convertView.findViewById(R.id.dialog_item_image_view)
                convertView.tag = holder
            } else {
                holder = convertView.tag as DialogHolder
            }
            if (item.applyIconTint) {
                holder.imageView?.setColorFilter(defaultTint)
            }
            holder.textView?.text = item.name
            holder.imageView?.setImageResource(item.icon)
            return convertView
        }

        internal class DialogHolder {
            var imageView: ImageView? = null
            var textView: TextView? = null
        }
    }

    private inner class OnClickListener(private val listener: OnDialogClickListener?) :
        View.OnClickListener {
        override fun onClick(view: View) {
            if (listener != null && !listener.onClick(progressBarDialog)) return
            dialog?.dismiss()
        }
    }

    private inner class JDialog(context: Context?, requestKeyboard: Boolean) : AlertDialog(
        context!!
    ) {
        private var requestKeyboard = false

        init {
            this.requestKeyboard = requestKeyboard
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(parentView)
            val window = window
            window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window.setBackgroundDrawableResource(R.drawable.dialog_bg_inset)
            window.attributes.windowAnimations = R.style.DialogAnimation
            if (requestKeyboard) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            }
        }

        override fun dismiss() {
            super.dismiss()
            isDismissed = true
        }

        override fun cancel() {
            super.cancel()
            isDismissed = true
        }
    }

    companion object {
        const val TAG = "SimpleDialog"
        const val STYLE_PROGRESS = 123
        const val STYLE_ALERT = 321
        const val STYLE_ALERT_HIGH = 999
        const val STYLE_INPUT = 444
        const val STYLE_MENU = 4
        private const val STYLE_ALERT_MEDIUM = 555

        @JvmStatic
        fun getMenuItems(names: Array<String>, icons: IntArray): List<MenuItem> {
            val menu: MutableList<MenuItem> = ArrayList()
            for (i in names.indices) {
                menu.add(MenuItem(names[i], icons[i]))
            }
            return menu
        }
    }
}