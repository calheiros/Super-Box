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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.jefferson.application.br.R
import com.jefferson.application.br.app.SimpleDialog
import com.jefferson.application.br.trigger.SwitchVisibilityTrigger
import com.jefferson.application.br.util.MediaUtils

class PreviewFragment(var filesPath: ArrayList<String>,
                      var position: Int,
                      var mediaType: Int,
) : Fragment(), View.OnClickListener {
    private lateinit var viewPager: ViewPager2
    private lateinit var pagerAdapter: ImagePagerAdapter
    private val removedItems = ArrayList<String>()
    private var rootView: View? = null
    private fun configureBackPressed() {
      /*
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent()
                intent.putExtra("index", viewPager.currentItem)
                intent.putStringArrayListExtra(EXTRA_REMOVED_ITEMS, removedItems)
                setResult(RESULT_OK, intent)
                finishAfterTransition()
            }
        })*/
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //configureTransition("shared_element_container")
        if(rootView == null) {
            rootView = inflater.inflate(R.layout.media_view_pager_layout, container, false)
            val deleteButton = rootView?.findViewById<View>(R.id.delete_imageview)
            val exportButton = rootView?.findViewById<View>(R.id.export_imageview)
            val optionLayout = rootView?.findViewById<View>(R.id.options_layout)

            pagerAdapter = ImagePagerAdapter(requireActivity(), optionLayout!!)
            viewPager = rootView?.findViewById(R.id.view_pager) as ViewPager2
            viewPager.adapter = pagerAdapter
            viewPager.setCurrentItem(position, false)
            viewPager.setOnClickListener(this)
            exportButton?.setOnClickListener(this)
            deleteButton?.setOnClickListener(this)
            configureBackPressed()
        }
        return rootView
    }


    override fun onClick(v: View) {
        val position = viewPager.currentItem
        val path = filesPath[position]
        when (v.id) {
            R.id.delete_imageview ->
                deletionConfirmation(path, position)
            R.id.export_imageview ->
                exportImage(path, position)
        }
    }

    private fun deletionConfirmation(path: String, position: Int) {
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
            removedItems.add(path)
        }
        Toast.makeText(
            requireContext(),
            if (success) "deleted: $position" else "failed to delete image",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun exportImage(path: String, position: Int) {
        //TODO: logic to export single image here
    }

    private inner class ImagePagerAdapter(fa: FragmentActivity, optionsLayout: View) :
        FragmentStateAdapter(fa) {
        private val optionsTrigger: SwitchVisibilityTrigger

        init {
            optionsTrigger = SwitchVisibilityTrigger(optionsLayout)
        }

        override fun getItemCount(): Int {
            return filesPath.size
        }

        override fun createFragment(position: Int): Fragment {
            return if (mediaType == 1)
                VideoPlayerFragment(filesPath[position], optionsTrigger)
            else
                ImagePreviewFragment(filesPath[position], optionsTrigger)
        }
    }

    companion object {
        val TAG: String = "PreviewFragment"
        const val EXTRA_REMOVED_ITEMS = "key_removed_items"
    }
}