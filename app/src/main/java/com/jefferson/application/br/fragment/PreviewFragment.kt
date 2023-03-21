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
package com.jefferson.application.br.fragment

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.jefferson.application.br.R
import com.jefferson.application.br.adapter.MultiSelectRecyclerViewAdapter
import com.jefferson.application.br.app.SimpleDialog
import com.jefferson.application.br.switcher.ViewVisibilitySwitch
import com.jefferson.application.br.util.MediaUtils
import kotlin.math.abs
import kotlin.math.max

class PreviewFragment(
    private var albumAdapter: MultiSelectRecyclerViewAdapter?,
    private var initialPosition: Int,
    var mediaType: Int,
    private var thumbnail: Drawable?
) : Fragment(), View.OnClickListener {
    private var viewPager: ViewPager2? = null
    private lateinit var pagerAdapter: ImagePagerAdapter
    private lateinit var filesPath: ArrayList<String>
    private var rootView: View? = null

    val currentItem: Int
        get() {
            return viewPager?.currentItem ?: initialPosition
        }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.preview_pager_layout, container, false)
            val deleteButton = rootView?.findViewById<View>(R.id.delete_imageview)
            val exportButton = rootView?.findViewById<View>(R.id.export_imageview)
            val optionLayout = rootView?.findViewById<View>(R.id.options_layout)
            filesPath = albumAdapter?.listItemsPath as ArrayList
            pagerAdapter = ImagePagerAdapter(requireActivity(), optionLayout as View)
            viewPager = rootView?.findViewById(R.id.view_pager) as ViewPager2
            viewPager?.adapter = pagerAdapter
            viewPager?.setPageTransformer(ZoomOutPageTransformer())
            viewPager?.setCurrentItem(initialPosition, false)
            viewPager?.setOnClickListener(this)
            exportButton?.setOnClickListener(this)
            deleteButton?.setOnClickListener(this)
            ViewCompat.setTransitionName(rootView!!, "root_container")
        }
        return rootView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postponeEnterTransition()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()
    }

    override fun onClick(v: View) {
        val position = currentItem
        val path = filesPath[position]
        when (v.id) {
            R.id.delete_imageview -> deleteConfirmation(path, position)
            R.id.export_imageview -> exportImage(path, position)
        }
    }

    private fun deleteConfirmation(path: String, position: Int) {
        val builder = SimpleDialog(requireActivity(), SimpleDialog.STYLE_ALERT_HIGH)
        builder.setTitle(getString(R.string.apagar))
        builder.setMessage(getString(R.string.apagar_image_mensagem))
        builder.setPositiveButton(getString(android.R.string.ok),
            object : SimpleDialog.OnDialogClickListener() {
                override fun onClick(dialog: SimpleDialog): Boolean {
                    deleteMedia(path, position)
                    return true
                }
            })
        builder.setNegativeButton(getString(android.R.string.cancel), null)
        builder.show()
    }

    fun deleteMedia(path: String, position: Int) {
        val success = MediaUtils.deleteMedia(requireContext(), path)
        if (success) {
            pagerAdapter.notifyItemRemoved(position)
            filesPath.removeAt(position)
            albumAdapter?.removeAt(position)
        } else {
            Toast.makeText(
                requireContext(), "failed to delete item at position: $position", Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun exportImage(path: String, position: Int) {
        //TODO: logic to export single image here
    }

    private inner class ImagePagerAdapter(fa: FragmentActivity, optionsLayout: View) :
        FragmentStateAdapter(fa) {
        private val optionsTrigger: ViewVisibilitySwitch

        init {
            optionsTrigger = ViewVisibilitySwitch(optionsLayout)
        }

        override fun getItemCount(): Int {
            return filesPath.size
        }

        override fun createFragment(position: Int): Fragment {
            return if (mediaType == 1)
                VideoPlayerFragment(filesPath[position], optionsTrigger)
            else ImagePreviewFragment(
                filesPath[position], optionsTrigger,
                if (position == initialPosition) thumbnail else null
            )
        }
    }

    class ZoomOutPageTransformer : ViewPager2.PageTransformer {

        override fun transformPage(view: View, position: Float) {
            view.apply {
                val pageWidth = width
                val pageHeight = height
                when {
                    position < -1 -> { // [-Infinity,-1)
                        // This page is way off-screen to the left.
                        alpha = 0f
                    }
                    position <= 1 -> { // [-1,1]
                        // Modify the default slide transition to shrink the page as well.
                        val scaleFactor = max(MIN_SCALE, 1 - abs(position))
                        val vertMargin = pageHeight * (1 - scaleFactor) / 2
                        val horzMargin = pageWidth * (1 - scaleFactor) / 2
                        translationX = if (position < 0) {
                            horzMargin - vertMargin / 2
                        } else {
                            horzMargin + vertMargin / 2
                        }

                        // Scale the page down (between MIN_SCALE and 1).
                        scaleX = scaleFactor
                        scaleY = scaleFactor

                        // Fade the page relative to its size.
                        alpha = (MIN_ALPHA +
                                (((scaleFactor - MIN_SCALE) / (1 - MIN_SCALE)) * (1 - MIN_ALPHA)))
                    }
                    else -> { // (1,+Infinity]
                        // This page is way off-screen to the right.
                        alpha = 0f
                    }
                }
            }
        }
    }

    companion object {
        private const val MIN_SCALE = 0.85f
        private const val MIN_ALPHA = 0.5f
        const val TAG: String = "PreviewFragment"
        const val EXTRA_REMOVED_ITEMS = "key_removed_items"
    }
}