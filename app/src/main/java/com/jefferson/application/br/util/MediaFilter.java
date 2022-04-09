package com.jefferson.application.br.util;

import android.content.ContentResolver;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.webkit.MimeTypeMap;
import com.jefferson.application.br.App;
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

    public String getMimeTypeFromUri(@NonNull Uri uri) { 
        String mimeType = null; 
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) { 
            ContentResolver cr = App.getAppContext().getContentResolver(); 
            mimeType = cr.getType(uri); 
        } else { 
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri .toString()); 
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase());
        } return mimeType;
    }
}
