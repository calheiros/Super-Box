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
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.jefferson.application.br.R
import com.jefferson.application.br.app.SimpleDialog
import com.jefferson.application.br.fragment.ImagePreviewFragment
import com.jefferson.application.br.trigger.SwitchVisibilityTrigger
import com.jefferson.application.br.util.MediaUtils

class ImagePreviewActivity : MyCompatActivity(), View.OnClickListener {
    private lateinit var viewPager: ViewPager2
    private lateinit var filepath: ArrayList<String>
    private lateinit var pagerAdapter: ImagePagerAdapter
    private lateinit var deletedFiles: ArrayList<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.media_view_pager_layout)
        viewPager = findViewById(R.id.view_pager)
        val deleteButton = findViewById<View>(R.id.delete_imageview)
        val exportButton = findViewById<View>(R.id.export_imageview)
        val position = intent.extras!!.getInt("position")
        val optionLayout = findViewById<View>(R.id.options_layout)
        filepath = intent.getStringArrayListExtra("filepath") as ArrayList<String>
        pagerAdapter = ImagePagerAdapter(this, optionLayout)
        //configure view pager
        viewPager.adapter = pagerAdapter
        viewPager.offscreenPageLimit = 4
        viewPager.currentItem = position

        viewPager.setOnClickListener(this)
        exportButton.setOnClickListener(this)
        deleteButton.setOnClickListener(this)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.delete_imageview) {
            val position = viewPager.currentItem
            val path = filepath[position]
            deletionConfirmation(path, position)
            return
        }
        if (id == R.id.export_imageview) {
            exportImage()
        }
    }

    private fun deletionConfirmation(path: String, position: Int) {
        val builder = SimpleDialog(this, SimpleDialog.STYLE_ALERT_HIGH)
        builder.setTitle(getString(R.string.apagar))
        builder.setMessage(getString(R.string.apagar_image_mensagem))
        builder.setPositiveButton(getString(android.R.string.ok), object : SimpleDialog.OnDialogClickListener() {
            override fun onClick(dialog: SimpleDialog): Boolean {
                val success = MediaUtils.deleteMedia(this@ImagePreviewActivity, path)
                if (success) {
                    filepath.removeAt(position)
                    pagerAdapter.notifyDataSetChanged()
                    Toast.makeText(
                        this@ImagePreviewActivity, "deleted!", Toast.LENGTH_SHORT
                    ).show()
                }
                return true
            }
        })
        builder.setNegativeButton(getString(android.R.string.cancel), null)
        builder.show()
    }

    private fun exportImage() {
        //TODO: export single image logic here
    }

    override fun onBackPressed() {
        val intent = Intent()
        intent.putExtra("index", viewPager.currentItem)
        setResult(RESULT_OK, intent)
        super.onBackPressed()
    }

    private inner class ImagePagerAdapter(fa: FragmentActivity, optionsLayout: View?) :
        FragmentStateAdapter(fa) {

        private val optionsTrigger: SwitchVisibilityTrigger

        init {
            optionsTrigger = SwitchVisibilityTrigger(optionsLayout!!)
        }

        override fun getItemCount(): Int {
            return filepath.size
        }

        override fun createFragment(position: Int): Fragment {
            return ImagePreviewFragment(filepath[position], optionsTrigger)
        }
    }
}