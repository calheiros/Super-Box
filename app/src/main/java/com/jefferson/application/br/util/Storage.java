package com.jefferson.application.br.util;

import android.annotation.*;
import android.content.*;
import android.database.*;
import android.media.*;
import android.net.*;
import android.os.*;
import android.os.Build.*;
import android.preference.*;
import android.provider.*;
import android.provider.MediaStore.*;
import android.support.v4.provider.*;
import android.util.*;
import com.jefferson.application.br.*;
import java.io.*;
import java.util.*;

public class Storage extends DocumentUtil {

    public static final String EXTERNAL = "external";
    public static final int IMAGE = 1;
    public static final String INTERNAL = "internal";
    public static final String STORAGE_LOCATION = "storage_loacation";
    public static final int VIDEO = 2;
	public static final String IMAGE_DIR = "b17rvm0891wgrqwoal5sg6rr";
	public static final String VIDEO_DIR = "bpe8x1svi9jvhmprmawsy3d8";


	public static void setNewLocalStorage(int selected) {

		if (selected == 0 || selected == 1) {
			PreferenceManager.getDefaultSharedPreferences(App.getAppContext()).
				edit().putString(STORAGE_LOCATION, selected == 0 ? INTERNAL : EXTERNAL).commit();
		}
	}

    public static Uri getExternalUri(Context context) {

        String string = PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.EXTERNAL_URI), null);
        if (string == null) {
            return (Uri) null;
        }
        return Uri.parse(string);
    }


    public static File getFolder(int type) {

        switch (type) {
            case IMAGE:
                return new File(getDefaultStorage(), IMAGE_DIR);
            case VIDEO:
                return new File(getDefaultStorage(), VIDEO_DIR);
            default:
                return null;
        }
    }

    public static String getStorageLocation() {
        return PreferenceManager.getDefaultSharedPreferences(App.getAppContext()).getString(STORAGE_LOCATION, INTERNAL);
    }

    public static String getDefaultStorage() {

		String storageLocation = getStorageLocation();
        String extPath = getExternalStorage();
       	if (INTERNAL.equals(storageLocation) || extPath == null) {
            return getInternalStorage();
      	} else {
			return extPath;
		}
    }

    public static String getExternalStorage() {
        try {
            File[] externalFilesDirs = App.getAppContext().getExternalFilesDirs("");
			if(externalFilesDirs == null) 
				return null;
            for (int i = 0; i < externalFilesDirs.length; i ++) {
                File file = externalFilesDirs[i];
                if (Environment.isExternalStorageRemovable(file)) {
					return file.getAbsolutePath();
                }
				Log.i("SD PATH", file.toString());
            }
        } catch (Exception e) {
			//Toast.makeText(App.getAppContext(), e.toString(), 1).show();
		}
        return (String) null;
    }

    public static String[] toArrayString(ArrayList<String> arrayList, boolean z) {

        String str = z ? "file://" : "";
        String[] strArr = new String[arrayList.size()];
        for (int i = 0; i < arrayList.size(); i ++) {

            StringBuffer stringBuffer = new StringBuffer();
            strArr[i] = stringBuffer.append(str).append(arrayList.get(i)).toString();
        }
        return strArr;
    }

    public static String getInternalStorage() {
        File file = new File(Environment.getExternalStorageDirectory()  + "/." +  App.getAppContext().getPackageName() + "/data");
		file.mkdirs();
		return file.getAbsolutePath();
    }

    public static boolean deleteFile(File file) {

        if (VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            
			DocumentFile documentFile = getDocumentFile(file, false);
			if (documentFile != null)
				return documentFile.delete();
		}
		return file.delete();
    }

    public static void deleteFileFromMediaStore(File file) {
        String canonicalPath;

        ContentResolver contentResolver = App.getAppContext().getContentResolver();
        try {
            canonicalPath = file.getCanonicalPath();
        } catch (IOException e) {
            canonicalPath = file.getAbsolutePath();
        }
        Uri contentUri = Files.getContentUri(EXTERNAL);

        StringBuffer stringBuffer = new StringBuffer();
        String args = stringBuffer.append("_data").append("=?").toString();
        String[] strArr = new String[1];

        strArr[0] = canonicalPath;
        if (contentResolver.delete(contentUri, args, strArr) == 0) {
            String absolutePath = file.getAbsolutePath();
            if (!absolutePath.equals(canonicalPath)) {

                contentResolver.delete(contentUri, absolutePath, new String[]{absolutePath});
            }
        }
    }
    public static void scanMediaFiles(String[] paths) {

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			mediaScannerConnection(paths);
		} else {
			for (String path : paths) {
				Intent intent = new Intent(Intent.ACTION_MEDIA_MOUNTED);
				intent.setData(Uri.parse("file://" + path));
				App.getAppContext().sendBroadcast(intent);
			}
		}
	}
	public static String getPathFromMediaUri(Uri uri, Context context) {
		
		String filePath = null;
		if (uri != null && "content".equals(uri.getScheme())) {
			String[] proj = { MediaStore.Video.Media.DATA };
			Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} else {
			filePath = uri.getPath();
		}
		Log.d("", "Chosen path = " + filePath);
		return filePath;
	}
    public static void mediaScannerConnection(String[] strArr) {

        MediaScannerConnection.scanFile(App.getAppContext(), strArr, null, new MediaScannerConnection.OnScanCompletedListener(){

				@Override
				public void onScanCompleted(String p1, Uri p2) {

				}
			});
    }

    @TargetApi(21)
    public static boolean checkIfSDCardRoot(Uri uri) {

        boolean z = isExternalStorageDocument(uri) && isRootUri(uri) && !isInternalStorage(uri);
        return z;
    }

    @TargetApi(21)
    public static boolean isRootUri(Uri uri) {
        return DocumentsContract.getTreeDocumentId(uri).endsWith(":");
    }

    @TargetApi(21)
    public static boolean isInternalStorage(Uri uri) {

        boolean z = isExternalStorageDocument(uri) && DocumentsContract.getTreeDocumentId(uri).contains("primary");
        return z;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }
}
