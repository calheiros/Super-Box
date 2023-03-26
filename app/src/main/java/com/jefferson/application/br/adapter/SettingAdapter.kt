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
package com.jefferson.application.br.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.material.materialswitch.MaterialSwitch
import com.jefferson.application.br.R
import com.jefferson.application.br.activity.CalculatorActivity
import com.jefferson.application.br.activity.MainActivity
import com.jefferson.application.br.app.SimpleDialog
import com.jefferson.application.br.app.SimpleDialog.OnDialogClickListener
import com.jefferson.application.br.fragment.SettingFragment
import com.jefferson.application.br.model.PreferenceItem
import com.jefferson.application.br.util.MyAnimationUtils
import com.jefferson.application.br.util.MyPreferences

@Suppress("NAME_SHADOWING")
class SettingAdapter(
    private var preferenceItems: ArrayList<PreferenceItem>,
    private var settingFragment: SettingFragment
) : BaseAdapter() {
    var inflater: LayoutInflater = settingFragment.requireActivity()
        .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private var calculatorDescText: TextView? = null
    private var switchCancelled = false
    private var mySwitch: MaterialSwitch? = null

    override fun getCount(): Int {
        return preferenceItems.size
    }

    override fun getItem(id: Int): PreferenceItem {
        return preferenceItems[id]
    }

    override fun getItemId(id: Int): Long {
        return id.toLong()
    }

    @SuppressLint("ViewHolder")
    override fun getView(i: Int, view: View?, viewGroup: ViewGroup): View {
        val prefItem = preferenceItems[i]
        val resId = when (prefItem.type) {
            PreferenceItem.ITEM_TYPE -> R.layout.preference_common_item
            PreferenceItem.ITEM_SWITCH_TYPE -> R.layout.preference_switch_item
            PreferenceItem.SECTION_TYPE -> R.layout.preference_section_item
            else -> throw java.lang.RuntimeException("INVALID PREFERENCE ITEM TYPE: ${prefItem.type}")
        }
        val contentView: View = inflater.inflate(resId, viewGroup, false)
        val titleLabel = contentView.findViewById<TextView>(R.id.pref_title_label)
        titleLabel.text = prefItem.title
        if (prefItem.type == PreferenceItem.SECTION_TYPE) {
            return contentView
        }
        val iconView = contentView.findViewById<ImageView>(R.id.pref_icon_view)
        val descrLabel = contentView.findViewById<TextView>(R.id.pref_description_label)
        iconView.setImageResource(prefItem.iconResId)
        if (prefItem.description != null) {
            descrLabel.visibility = View.VISIBLE
            descrLabel.text = prefItem.description
        }
        if (prefItem.type == PreferenceItem.ITEM_SWITCH_TYPE) {
            val switcher = contentView.findViewById<MaterialSwitch>(R.id.prefs_switch)
            switcher.isChecked = prefItem.checked
        }
    return contentView
}

fun onCalculatorCodeChanged(text: String?) {
    if (switchCancelled && mySwitch != null) {
        switchCancelled = false
        mySwitch?.isChecked = !mySwitch?.isChecked!!
    }
    if (calculatorDescText != null) {
        calculatorDescText?.text = text
    }
}

private fun setExpandableLayoutListener(v: View) {
    val expandableLayout = v.findViewById<View>(R.id.steal_thexpandable_layout)
    val calculator = v.findViewById<View>(R.id.steal_calculator_layout)
    calculatorDescText = v.findViewById(R.id.stealth_expandable_descriptionTextView)
    calculatorDescText?.text = MyPreferences.getCalculatorCode(settingFragment.requireContext())
    mySwitch = v.findViewById(R.id.prefs_switch)
    calculator.setOnClickListener { startCalculatorActivity() }

    v.setOnClickListener(View.OnClickListener {
        val checked = mySwitch?.isChecked
        if (!checked!! && MyPreferences.getCalculatorCode(settingFragment.requireContext()) == "4321") {
            val contentView = settingFragment.requireActivity().layoutInflater.inflate(
                R.layout.calculator_tip_dialog_layout, null
            )
            showNoticeDialog(contentView)
            return@OnClickListener
        }
        mySwitch?.isChecked = !checked
    })
    mySwitch?.setOnCheckedChangeListener { _, checked ->
        settingFragment.setCalculatorEnabled(checked)
        val main = settingFragment.requireActivity() as MainActivity
        if (main.calculatorStateEnabled != checked) {
            Toast.makeText(
                settingFragment.context,
                settingFragment.getString(R.string.reiniciar_para_aplicar),
                Toast.LENGTH_SHORT
            ).show()
        }
        if (checked) {
            MyAnimationUtils.expand(expandableLayout)
        } else {
            MyAnimationUtils.collapse(expandableLayout)
        }
    }
}

private fun showNoticeDialog(contentView: View) {
    val dialog = SimpleDialog(settingFragment.activity as Activity)
    dialog.setContentView(contentView)
    dialog.setTitle(R.string.aviso)
    dialog.setPositiveButton(android.R.string.ok, object : OnDialogClickListener() {
        override fun onClick(dialog: SimpleDialog): Boolean {
            startCalculatorActivity()
            switchCancelled = true
            return true
        }
    })
    dialog.setNegativeButton(android.R.string.cancel, null)
    dialog.setCanceledOnTouchOutside(false)
    dialog.show()
}

private fun startCalculatorActivity() {
    val intent = Intent(settingFragment.activity, CalculatorActivity::class.java)
    intent.action = CalculatorActivity.ACTION_CREATE_CODE
    settingFragment.requireActivity().startActivityForResult(
        intent, SettingFragment.CALCULATOR_CREATE_CODE_RESULT
    )
}

class PrefsHolder {
    var titleLabel: TextView? = null
    var iconView: ImageView? = null
    var descriptionLabel: TextView? = null
    var switcher: Switch? = null
}
}