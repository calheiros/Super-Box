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

package com.jefferson.application.br.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.jefferson.application.br.R;
import com.jefferson.application.br.trigger.SwitchVisibilityTrigger;
import com.jefferson.application.br.database.PathsDatabase;
import com.jefferson.application.br.util.Storage;
import java.io.File;

public class ImagePreviewFragment extends Fragment implements View.OnClickListener {

    private final String path;
    private final SwitchVisibilityTrigger optionsTrigger;
    private View parentView;

    public ImagePreviewFragment(String path, SwitchVisibilityTrigger optionsLayout) {
        this.path = path;
        this.optionsTrigger = optionsLayout;
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (parentView == null) {
            parentView = inflater.inflate(R.layout.image_preview_layout, container, false);
            SubsamplingScaleImageView imageView = parentView.findViewById(R.id.imageView);
            ImageView gifView = parentView.findViewById(R.id.gif_view);
            PathsDatabase database = PathsDatabase.getInstance(requireContext(),
                    Storage.getDefaultStoragePath(requireContext()));
            String originPath = database.getMediaPath(new File(path).getName());
            database.close();

            if (originPath != null) {
                String mimeType = getMimeType(originPath);
                if (mimeType != null && mimeType.endsWith("/gif")) {
                    Glide.with(getContext()).load("file://" + path).skipMemoryCache(false).into(gifView);
                    return parentView;
                }
            }
            imageView.setImage(ImageSource.uri(path));
            imageView.setSoundEffectsEnabled(false);
            imageView.setOnClickListener(this);
        }
        return parentView;
    }

    @Override
    public void onClick(View v) {
        optionsTrigger.switchVisibility();
    }
}
