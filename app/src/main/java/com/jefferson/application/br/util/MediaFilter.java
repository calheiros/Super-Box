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

package com.jefferson.application.br.util;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;

import java.io.File;

public class MediaFilter {

    final static String[] imageExt = {".jpg", ".png", ".gif", ".jpeg", ".webp"};
    final static String[] videoExt = {".mp4", ".3gp", ".flv", ".webm"};

    public static boolean isVideo(@NonNull File file) {

        for (String extension : videoExt) {
            Log.i("MediaFilter", "Extension: " + extension);

            if (file.getName().toLowerCase().endsWith(extension)) {
                Log.i("MediaFilter", "Returning TRUE for VIDEO in: " + file);
                return true;
            }
        }
        Log.i("MediaFilter", "Returning FALSE for VIDEO in: " + file);

        return false;
    }

    public static boolean isImage(@NonNull File file) {

        for (String extension : imageExt) {
            Log.i("MediaFilter", "Extension: " + extension);

            if (file.getName().toLowerCase().endsWith(extension)) {
                Log.i("MediaFilter", "Returning TRUE for IMAGE in: " + file);

                return true;
            }
        }
        Log.i("MediaFilter", "Returning FALSE for IMAGE: " + file);

        return false;
    }

    public static String getMimeType(@NonNull String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    public String getMimeTypeFromUri(@NonNull Uri uri, Context context) {
        String mimeType;
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            ContentResolver cr = context.getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase());
        }
        return mimeType;
    }
}
