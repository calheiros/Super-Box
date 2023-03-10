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
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.jefferson.application.br.R
import com.jefferson.application.br.fragment.VideoPlayerFragment
import com.jefferson.application.br.trigger.SwitchVisibilityTrigger

class VideoPlayerActivity : MyCompatActivity(), View.OnClickListener {
    private lateinit var pagerAdapter: VideoPagerAdapter
    private lateinit var viewPager: ViewPager2
    override fun onCreate(savedInstanceState: Bundle?) {
        configureTransition("shared_element_container")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.preview_pager_layout)
        val exportImageView = findViewById<ImageView>(R.id.export_imageview)
        val deleteImageView = findViewById<ImageView>(R.id.delete_imageview)
        val optionsLayout = findViewById<View>(R.id.options_layout)
        val switchVisibilityTrigger = SwitchVisibilityTrigger(optionsLayout)
        optionsLayout.setOnClickListener(this)
        exportImageView.setOnClickListener(this)
        deleteImageView.setOnClickListener(this)
        val intent = intent
        val position = intent.extras!!.getInt("position")
        val filesPath = intent.getStringArrayListExtra("filepath")
        //fullscreen()
        pagerAdapter = VideoPagerAdapter(this, filesPath!!, switchVisibilityTrigger)
        viewPager = findViewById<View>(R.id.view_pager) as ViewPager2
        viewPager.adapter = pagerAdapter
        viewPager.setCurrentItem(position, false)
        viewPager.offscreenPageLimit = 3
        viewPager.requestFocus()
        viewPager.setOnClickListener(this)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                val data = Intent()
                data.putExtra("index", viewPager.currentItem)
                setResult(RESULT_OK, data)
                finishAfterTransition()
            }
        })
    }

    override fun onClick(v: View) {
        var name = "unknown"
        when (v.id) {
            R.id.export_imageview -> {
                name = "export"
            }
            R.id.delete_imageview -> {
                name = "delete"
            }
            R.id.options_layout -> {
                name = "options"
            }
        }
        Toast.makeText(this, name, Toast.LENGTH_SHORT).show()
    }

    override fun onApplyCustomTheme() {
        //do nothing
    }

    private fun requestOrientation(width: Int, height: Int) {
        val orientation =
            if (width > height) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        requestedOrientation = orientation
    }

    fun hideNavigationBar() {
        val decorView = window.decorView
        val uiOptions = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN)
        decorView.systemUiVisibility = uiOptions
    }

    private fun fullscreen() {
        val win = window
        val winParams = win.attributes
        val bits = WindowManager.LayoutParams.FLAG_FULLSCREEN
        winParams.flags = winParams.flags or bits
        win.attributes = winParams
    }

    private class VideoPagerAdapter(
        fm: FragmentActivity, private val filesPath: ArrayList<String?>,
        private val switchVisibilityTrigger: SwitchVisibilityTrigger
    ) : FragmentStateAdapter(
        fm
    ) {
        private val fragments: Array<VideoPlayerFragment?>?

        init {
            fragments = arrayOfNulls(filesPath.size)
        }

        override fun getItemCount(): Int {
            return filesPath.size
        }

        fun getFragment(position: Int): VideoPlayerFragment? {
            return fragments?.get(position)
        }

        override fun createFragment(position: Int): VideoPlayerFragment {
            var fragment: VideoPlayerFragment? = fragments?.get(position)
            if (fragment == null) {
                fragment = VideoPlayerFragment(filesPath[position]!!, switchVisibilityTrigger)
                fragments?.set(position, fragment)
            }
            return fragment
        }
    }
}