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
import android.webkit.MimeTypeMap
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.jefferson.application.br.R
import com.jefferson.application.br.database.PathsDatabase.Companion.getInstance
import com.jefferson.application.br.trigger.SwitchVisibilityTrigger
import com.jefferson.application.br.util.Storage.getDefaultStoragePath
import java.io.File

class ImagePreviewFragment(
    private val path: String,
    private val optionsTrigger: SwitchVisibilityTrigger
) : Fragment(), View.OnClickListener {
    private var parentView: View? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (parentView == null) {
            parentView = inflater.inflate(R.layout.image_preview_layout, container, false)
            val imageView = parentView!!.findViewById<SubsamplingScaleImageView>(R.id.imageView)
            val gifView = parentView!!.findViewById<ImageView>(R.id.gif_view)
            val database = getInstance(
                requireContext(),
                getDefaultStoragePath(requireContext())
            )
            val originalName = database.getMediaPath(File(path).name)
            database.close()
            if (originalName != null) {
                val mimeType = getMimeType(originalName)
                if (mimeType != null && mimeType.endsWith("/gif")) {
                    Glide.with(requireContext()).load("file://$path")
                        .skipMemoryCache(false).into(gifView)
                    return parentView
                }
            }
            imageView.setImage(ImageSource.uri(path))
            imageView.isSoundEffectsEnabled = false
            imageView.setOnClickListener(this)
        }
        return parentView
    }

    override fun onClick(v: View) {
        optionsTrigger.switchVisibility()
    }

    companion object {
        fun getMimeType(url: String?): String? {
            var type: String? = null
            val extension = MimeTypeMap.getFileExtensionFromUrl(url)
            if (extension != null) {
                type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            }
            return type
        }
    }
}