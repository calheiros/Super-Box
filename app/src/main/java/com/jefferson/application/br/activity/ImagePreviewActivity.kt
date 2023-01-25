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
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.jefferson.application.br.R
import com.jefferson.application.br.activity.triggers.SwitchVisibilityTrigger
import com.jefferson.application.br.app.SimpleDialog
import com.jefferson.application.br.app.SimpleDialog.OnDialogClickListener
import com.jefferson.application.br.fragment.ImagePreviewFragment

class ImagePreviewActivity : MyCompatActivity(), View.OnClickListener {
    private var viewPager: ViewPager? = null
    private var filepath: ArrayList<String>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.video_player_layout)
        viewPager = findViewById(R.id.view_pager)
        val deleteButton = findViewById<View>(R.id.delete_imageview)
        val exportButton = findViewById<View>(R.id.export_imageview)
        val intent = intent
        val position = intent.extras!!.getInt("position")
        filepath = intent.getStringArrayListExtra("filepath")
        val optionLayout = findViewById<View>(R.id.options_layout)
        val PagerAdapter = ImagePagerAdapter(supportFragmentManager, optionLayout)
        viewPager.setAdapter(PagerAdapter)
        viewPager.setOffscreenPageLimit(4)
        viewPager.setPageMargin(20)
        viewPager.setCurrentItem(position)
        viewPager.setOnClickListener(this)
        exportButton.setOnClickListener(this)
        deleteButton.setOnClickListener(this)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.delete_imageview) {
            dialogDeletionConfirmation()
            return
        }
        if (id == R.id.export_imageview) {
            exportImage()
        }
    }

    private fun dialogDeletionConfirmation() {
        val builder = SimpleDialog(this, SimpleDialog.STYLE_ALERT_HIGH)
        builder.setTitle(getString(R.string.apagar))
        builder.setMessage(getString(R.string.apagar_image_mensagem))
        builder.setPositiveButton(
            getString(android.R.string.yes),
            object : OnDialogClickListener() {
                override fun onClick(dialog: SimpleDialog): Boolean {
                    return true
                }
            })
        builder.setNegativeButton(getString(android.R.string.cancel), null)
        builder.show()
    }

    private fun exportImage() {}
    override fun onBackPressed() {
        val intent = Intent()
        intent.putExtra("index", viewPager!!.currentItem)
        setResult(RESULT_OK, intent)
        super.onBackPressed()
    }

    private inner class ImagePagerAdapter(fm: FragmentManager?, optionsLayout: View?) :
        FragmentStatePagerAdapter(
            fm!!
        ) {
        private val optionsTrigger: SwitchVisibilityTrigger

        init {
            optionsTrigger = SwitchVisibilityTrigger(optionsLayout)
        }

        override fun getCount(): Int {
            return filepath!!.size
        }

        override fun getItem(position: Int): Fragment {
            return ImagePreviewFragment(filepath!![position], optionsTrigger)
        }
    }
}