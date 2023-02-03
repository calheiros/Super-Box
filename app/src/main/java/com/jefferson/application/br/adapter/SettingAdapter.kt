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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import com.jefferson.application.br.R
import com.jefferson.application.br.activity.CalculatorActivity
import com.jefferson.application.br.activity.MainActivity
import com.jefferson.application.br.app.SimpleDialog
import com.jefferson.application.br.app.SimpleDialog.OnDialogClickListener
import com.jefferson.application.br.fragment.SettingFragment
import com.jefferson.application.br.model.PreferenceItem
import com.jefferson.application.br.util.MyAnimationUtils
import com.jefferson.application.br.util.MyPreferences

class SettingAdapter(
    var preferenceItems: ArrayList<PreferenceItem>,
    var settingFragment: SettingFragment
) : BaseAdapter() {
    var inflater: LayoutInflater = settingFragment.requireActivity()
        .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private var calculatorDescText: TextView? = null
    private var switchCancelled = false
    private var mySwitch: SwitchCompat? = null

    override fun getCount(): Int {
        return preferenceItems.size
    }

    override fun getItem(id: Int): PreferenceItem {
        return preferenceItems[id]
    }

    override fun getItemId(id: Int): Long {
        return id.toLong()
    }

    override fun getView(i: Int, view: View?, viewGroup: ViewGroup): View? {
        var view = view
        val preferenceItem = preferenceItems[i]
        when (preferenceItem.type) {
            PreferenceItem.ITEM_SWITCH_TYPE -> {
                view = inflater.inflate(R.layout.preference_switch_item, null as ViewGroup?)
                val titleView = view.findViewById<TextView>(R.id.title_view)
                val iconView = view.findViewById<ImageView>(R.id.ic_view)
                val mySwitch = view.findViewById<SwitchCompat>(R.id.my_switch)
                val descriptionText = view.findViewById<TextView>(R.id.description_text_view)
                iconView.setImageResource(preferenceItem.icon_res_id)
                mySwitch.isChecked = preferenceItem.checked

                if (preferenceItem.id == PreferenceItem.ID.APP_ICON) {
                    val expandableLayout =
                        inflater.inflate(R.layout.stealth_expandable_layout, null)
                    (view as ViewGroup).addView(expandableLayout)
                    if (mySwitch.isChecked) {
                        expandableLayout.visibility = View.VISIBLE
                    }
                    setExpandableLayoutListener(view)
                }
                if (preferenceItem.description == null) {
                    descriptionText.visibility = View.GONE
                } else {
                    descriptionText.text = preferenceItem.description
                }
                titleView.text = preferenceItem.title
            }
            PreferenceItem.SECTION_TYPE -> {
                view = inflater.inflate(R.layout.preference_section_item, null as ViewGroup?)
                (view.findViewById<View>(R.id.title_view) as TextView).text = preferenceItem.title
            }
            PreferenceItem.ITEM_TYPE -> {
                view = inflater.inflate(R.layout.preference_common_item, null as ViewGroup?)
                val descriptionText = view.findViewById<TextView>(R.id.description_text_view)
                val iconView = view.findViewById<ImageView>(R.id.ic_view)
                (view.findViewById<View>(R.id.item_title) as TextView).text = preferenceItem.title
                iconView.setImageResource(preferenceItem.icon_res_id)
                if (preferenceItem.description != null) {
                    descriptionText.visibility = View.VISIBLE
                    descriptionText.text = preferenceItem.description
                }
            }
        }
        return view
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
        mySwitch = v.findViewById(R.id.my_switch)
        calculator.setOnClickListener { startCalculatorActivity() }

        v.setOnClickListener(View.OnClickListener {
            val checked = mySwitch?.isChecked
            if (!checked!! && MyPreferences.getCalculatorCode(settingFragment.requireContext()) == "4321") {
                val contentView = settingFragment.requireActivity().layoutInflater.inflate(
                    R.layout.calculator_tip_dialog_layout,
                    null
                )
                val dialog = SimpleDialog(settingFragment.activity as Activity)
                dialog.setContentView(contentView)
                dialog.setTitle(R.string.aviso)
                //dialog.setMessage("The app icon you be changed to a fake one!");
                dialog.setPositiveButton(android.R.string.ok, object : OnDialogClickListener() {
                    override fun onClick(dialog: SimpleDialog): Boolean {
                        startCalculatorActivity()
                        switchCancelled = true
                        return true
                    }
                }
                )
                dialog.setNegativeButton(android.R.string.cancel, null)
                dialog.setCanceledOnTouchOutside(false)
                dialog.show()
                return@OnClickListener
            }
            mySwitch?.isChecked = !checked
        }
        )
        mySwitch?.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { _, checked ->
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
        )
    }

    private fun startCalculatorActivity() {
        val intent = Intent(settingFragment.activity, CalculatorActivity::class.java)
        intent.action = CalculatorActivity.ACTION_CREATE_CODE
        settingFragment.requireActivity().startActivityForResult(
            intent,
            SettingFragment.CALCULATOR_CREATE_CODE_RESULT
        )
    }
}