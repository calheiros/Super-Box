package com.jefferson.application.br.util;


import android.util.*;
import java.io.*;

public class MediaFilter {

	final static String[] imageExt = {".jpg", ".png", ".gif", ".jpeg", ".webp"};
	final static String[] videoExt = {".mp4", ".3gp", ".flv", ".webm"};

	public static boolean isVideo(File file) {

        for (String extension : videoExt) {
			Log.i("MediaFilter", "Extension: " +extension);
			
            if (file.getName().toLowerCase().endsWith(extension)) {
				Log.i("MediaFilter", "Returning TRUE for VIDEO in: " + file);
                return true;
            }
        }
		Log.i("MediaFilter", "Returning FALSE for VIDEO in: " + file);
		
        return false;
    }

    public static boolean isImage(File file) {

        for (String extension : imageExt) {
			Log.i("MediaFilter", "Extension: " +extension);
			
            if (file.getName().toLowerCase().endsWith(extension)) {
				Log.i("MediaFilter", "Returning TRUE for IMAGE in: " + file);
				
                return true;
            }
        }
		Log.i("MediaFilter", "Returning FALSE for IMAGE: " + file);
		
        return false;
    }
}
