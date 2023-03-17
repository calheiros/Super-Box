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

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.transition.Transition
import androidx.transition.TransitionInflater
import com.jefferson.application.br.R
import com.jefferson.application.br.adapter.MultiSelectRecyclerViewAdapter
import com.jefferson.application.br.fragment.PreviewFragment
import com.jefferson.application.br.fragment.ViewAlbumFragment
import java.io.File


class ViewAlbum : MyCompatActivity() {
    private lateinit var albumDirFile: File
    private var position = 0
    private lateinit var title: String
    private var previewFragment: PreviewFragment? = null
    private lateinit var albumFragment : ViewAlbumFragment
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_album_layout)
        position = intent.getIntExtra("position", -1)
        title = intent.getStringExtra("name") as String
        albumDirFile = File(intent.getStringExtra("folder")!!)
        albumFragment = ViewAlbumFragment(title, position, albumDirFile, this)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, albumFragment)
            .attach(albumFragment)
            .commit()
        addBackPressedListener()
    }

    private fun addBackPressedListener() {
       onBackPressedDispatcher.addCallback(
            this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {

                    if (previewFragment != null) {
                        removePreviewFragment()
                        return
                    }
                    if (albumFragment.onBackPressed())
                        finish()
                }
            })
    }

    fun startPreview(adapter: MultiSelectRecyclerViewAdapter, itemPosition: Int, view: View) {
        previewFragment = PreviewFragment(adapter, itemPosition, position)
        supportFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .addSharedElement(view, view.transitionName)
            .replace(R.id.fragment_container, previewFragment!!)
            .attach(previewFragment!!)
            .commit()
    }

    fun removePreviewFragment() {
        if (previewFragment == null) return
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, albumFragment)
            .attach(albumFragment)
            .commit()
        previewFragment = null
    }
}