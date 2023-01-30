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
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.ArrayRes
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.jefferson.application.br.R
import com.jefferson.application.br.fragment.VideoPlayerFragment
import kotlin.math.roundToInt

class VideoPlayerActivity : MyCompatActivity(), View.OnClickListener {
    private lateinit var pagerAdapter: VideoPagerAdapter
    private lateinit var viewPager: ViewPager

    inner class MyPageListener(private var lastFragmentPosition: Int) : OnPageChangeListener {
        override fun onPageScrolled(p1: Int, p2: Float, p3: Int) {}
        override fun onPageSelected(position: Int) {
            val lastFragment = pagerAdapter.getItem(lastFragmentPosition)
            lastFragment.stop()
            lastFragmentPosition = position
        }

        override fun onPageScrollStateChanged(state: Int) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.video_player_layout)
        val exportImageView = findViewById<ImageView>(R.id.export_imageview)
        val deleteImageView = findViewById<ImageView>(R.id.delete_imageview)
        val optionsLayout = findViewById<View>(R.id.options_layout)

        optionsLayout.setOnClickListener(this)
        exportImageView.setOnClickListener(this)
        deleteImageView.setOnClickListener(this)

        val intent = intent
        val choice = intent.extras!!.getInt("position")
        val filesPath = intent.getStringArrayListExtra("filepath")
        fullscreen()
        pagerAdapter = VideoPagerAdapter(supportFragmentManager, filesPath!!)
        viewPager = findViewById<View>(R.id.view_pager) as ViewPager
        viewPager.adapter = pagerAdapter
        viewPager.setOnPageChangeListener(MyPageListener(choice))
        viewPager.currentItem = choice
        viewPager.offscreenPageLimit = 3
        viewPager.pageMargin = dpToPx(5)
        pagerAdapter.getItem(choice).setPlayOnCreate(true)
        viewPager.requestFocus()
        viewPager.setOnClickListener(this)
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

    private fun dpToPx(dp: Int): Int {
        val displayMetrics = resources.displayMetrics
        return (dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
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
        fm: FragmentManager?, private val filesPath: ArrayList<String?>
    ) : FragmentStatePagerAdapter(
        fm!!
    ) {
        private val fragments: Array<VideoPlayerFragment?>?

        init {
            fragments = arrayOfNulls(filesPath.size)
        }

        override fun getItem(position: Int): VideoPlayerFragment {
            var fragment: VideoPlayerFragment? = fragments?.get(position)
            if (fragment == null) {
                fragment = VideoPlayerFragment(filesPath[position])
                fragments?.set(position, fragment)
            }
            return fragment
        }

        override fun getCount(): Int {
            return filesPath.size
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val intent = Intent()
        intent.putExtra("index", viewPager.currentItem)
        setResult(RESULT_OK, intent)
        super.onBackPressed()
    }
}