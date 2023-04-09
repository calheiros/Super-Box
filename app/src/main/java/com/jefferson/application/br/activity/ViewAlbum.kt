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

import android.animation.Animator
import android.animation.AnimatorListenerAdapter

import android.os.Bundle
import android.transition.ArcMotion
import android.view.MenuItem
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.animation.AccelerateInterpolator
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.app.SharedElementCallback
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.google.android.material.transition.platform.MaterialFade
import com.google.android.material.transition.platform.MaterialFadeThrough
import com.jefferson.application.br.R
import com.jefferson.application.br.adapter.MultiSelectRecyclerViewAdapter
import com.jefferson.application.br.fragment.MediaPreviewFragment
import com.jefferson.application.br.fragment.ViewAlbumFragment
import com.jefferson.application.br.util.ThemeConfig
import java.io.File
import kotlin.math.max


class ViewAlbum : MyCompatActivity() {
    private lateinit var albumDirFile: File
    private var fragmentPosition = 0
    private lateinit var title: String
    private var previewFragment: MediaPreviewFragment? = null
    private lateinit var albumFragment: ViewAlbumFragment
    private lateinit var rootLayout: View
    private var revealX = 0
    private var revealY = 0
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_album_layout)
        rootLayout = findViewById(R.id.fragment_container)
        fragmentPosition = intent.getIntExtra("position", -1)
        title = intent.getStringExtra("name") as String
        albumDirFile = File(intent.getStringExtra("folder")!!)
        albumFragment = ViewAlbumFragment(title, fragmentPosition, albumDirFile, this)
        albumFragment.exitTransition = MaterialFade()
        albumFragment.reenterTransition = MaterialFadeThrough()
        albumFragment.setExitSharedElementCallback(transitionCallback)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, albumFragment)
            .attach(albumFragment)
            .commit()
        if (savedInstanceState == null &&
            intent.hasExtra(EXTRA_CIRCULAR_REVEAL_X) &&
            intent.hasExtra(EXTRA_CIRCULAR_REVEAL_Y)
        ) {
            rootLayout.visibility = View.INVISIBLE
            revealX = intent.getIntExtra(EXTRA_CIRCULAR_REVEAL_X, 0)
            revealY = intent.getIntExtra(EXTRA_CIRCULAR_REVEAL_Y, 0)
            val viewTreeObserver = rootLayout.viewTreeObserver
            if (viewTreeObserver.isAlive) {
                viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        revealActivity(revealX, revealY)
                        rootLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    }
                })
            }
        } else {
            rootLayout.visibility = View.VISIBLE
        }
        configureBackgroundColor()
        addBackPressedListener()
    }

    private fun configureBackgroundColor() {
        val currentTheme = ThemeConfig.getTheme(this)
        val backgroundColor = getThemeAttributeColor(currentTheme, R.attr.colorBackground)
        rootLayout.setBackgroundColor(backgroundColor)
    }

    override fun onApplyTheme() {}

    fun revealActivity(x: Int, y: Int) {
        val finalRadius =
            (max(rootLayout.width, rootLayout.height) * 1.1f)

        // create the animator for this view (the start radius is zero)
        val circularReveal: Animator =
            ViewAnimationUtils.createCircularReveal(rootLayout, x, y, 0f, finalRadius)
        circularReveal.duration = 360
        circularReveal.interpolator = AccelerateInterpolator()

        // make the view visible and start the animation
        rootLayout.visibility = View.VISIBLE
        circularReveal.start()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
       invokeBackPressed()
        return super.onOptionsItemSelected(item)
    }
    private fun unRevealActivity() {
        if (revealX == 0 && revealY == 0) {
            finish()
        } else {
            val finalRadius = (max(rootLayout.width, rootLayout.height) * 1.1f)
            val circularReveal = ViewAnimationUtils.createCircularReveal(
                rootLayout, revealX, revealY, finalRadius, 0f
            )
            circularReveal.duration = 360
            circularReveal.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    rootLayout.visibility = View.INVISIBLE
                    finish()

                }
            })
            circularReveal.start()
        }
    }

    private fun invokeBackPressed() {
        if (supportFragmentManager.backStackEntryCount != 0) {
            supportFragmentManager.popBackStack()
            return
        }
        if (albumFragment.onBackPressed()) {
            unRevealActivity()
        }
    }

    private fun addBackPressedListener() {
        onBackPressedDispatcher.addCallback(
            this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                   invokeBackPressed()
            }
            })
    }

    private val transitionCallback = object : SharedElementCallback() {
        override fun onMapSharedElements(
            names: MutableList<String>,
            sharedElements: MutableMap<String, View>
        ) {
            if (previewFragment != null) {
                val currentItem = previewFragment?.currentItem ?: return
                val sharedView = (albumFragment.recyclerView.findViewHolderForAdapterPosition(currentItem)
                        as MultiSelectRecyclerViewAdapter.ViewHolder?)?.rootView
                if (sharedView != null) {
                    sharedElements[names[0]] = sharedView
                } else Toast.makeText(this@ViewAlbum, "cannot find item!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun startPreview(itemPosition: Int, view: View) {
        val imageView = view.findViewById<ImageView?>(R.id.album_image_view)
        val thumbnail = imageView.drawable

        previewFragment =
            MediaPreviewFragment(albumFragment.adapter, itemPosition, fragmentPosition, thumbnail)
        previewFragment?.sharedElementEnterTransition = MaterialContainerTransform().apply {
            pathMotion = ArcMotion()
        }
        supportFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .addSharedElement(view, "root_container")
            .replace(R.id.fragment_container, previewFragment!!)
            .attach(previewFragment!!)
            .addToBackStack(null)
            .commit()
    }
    companion object {
        const val EXTRA_CIRCULAR_REVEAL_X = "EXTRA_CIRCULAR_REVEAL_X"
        const val EXTRA_CIRCULAR_REVEAL_Y = "EXTRA_CIRCULAR_REVEAL_Y"
    }

}