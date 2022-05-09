package com.jefferson.application.br.util;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.MediaStore.Files;
import android.support.annotation.NonNull;
import android.support.v4.provider.DocumentFile;
import android.util.Log;
import com.jefferson.application.br.App;
import com.jefferson.application.br.R;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

public class Storage extends DocumentUtil {

    public static final String EXTERNAL = "external";
    public static final int IMAGE = 0;
    public static final String INTERNAL = "internal";
    public static final String STORAGE_LOCATION = "storage_loacation";
    public static final int VIDEO = 1;
	public static final String IMAGE_DIR_NAME = "b17rvm0891wgrqwoal5sg6rr";
	public static final String VIDEO_DIR_NAME = "bpe8x1svi9jvhmprmawsy3d8";
    public static final String EXTERNAL_URI_KEY = "external_uri";

    public static void storeExternalUri(String uri) {
        MyPreferences.getSharedPreferencesEditor().putString(Storage.EXTERNAL_URI_KEY, uri).commit();
    }

    public static boolean writeFile(String content, File target) {
        File parent = target.getParentFile();

        if (!parent.exists()) {
            parent.mkdirs();
        }

        try {
            FileWriter writer = new FileWriter(target);
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static String getRecycleBinPath() {
        File file = new File(getDefaultStoragePath(), ".trashed");
        file.mkdirs();
        return file.getAbsolutePath();
    }

    public static boolean writeFile(byte[] content, File target) {
        File parent = target.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }

        ByteBuffer buffer = ByteBuffer.wrap(content);
        try {
            FileOutputStream writer = new FileOutputStream(target);
            FileChannel channel =  writer.getChannel();
            channel.write(buffer);
            channel.close();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static String readFile(File file) {
        StringBuilder builder = new  StringBuilder();
        String result = null;
        int c = 0;
        char[] buff = new char[128];

        try {
            FileReader reader = new FileReader(file);
            while ((c = reader.read(buff)) > 0) {
                builder.append(buff, 0, c);
            }
            result = builder.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static byte[] readFileToByte(File file) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] result = null;
        int c = 0;
        byte[] buff = new byte[128];

        try {
            FileInputStream reader = new FileInputStream(file);
            while ((c = reader.read(buff)) > 0) {
                out.write(buff, 0, c);
            }
            reader.close();
            out.flush();
            out.close();
            result = out.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

	public static void setNewLocalStorage(int selected) {
		if (selected == 0 || selected == 1) {
			PreferenceManager.getDefaultSharedPreferences(App.getAppContext()).
				edit().putString(STORAGE_LOCATION, selected == 0 ? INTERNAL : EXTERNAL).commit();
		}
	}

    public static Uri getExternalUri(Context context) {
        String string = MyPreferences.getSharedPreferences().getString(EXTERNAL_URI_KEY, null);

        if (string == null) {
            return null;
        }

        return Uri.parse(string);
    }

    public static File getFolder(int type) {

        switch (type) {
            case IMAGE:
                return new File(getDefaultStoragePath(), IMAGE_DIR_NAME);
            case VIDEO:
                return new File(getDefaultStoragePath(), VIDEO_DIR_NAME);
            default:
                return null;
        }
    }

    public static String getStorageLocation() {
        return PreferenceManager.getDefaultSharedPreferences(App.getAppContext()).getString(STORAGE_LOCATION, INTERNAL);
    }

    public static int getStoragePosition() {
        String storageLocation = Storage.getStorageLocation();

        if (Storage.INTERNAL.equals(storageLocation)) {
            return 0;
        }

        if (Storage.EXTERNAL.equals(storageLocation)) {
            return 1;
        } 

        return -1;
    }

    public static String getDefaultStoragePath() {
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

			if (externalFilesDirs == null) 
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

	/*public static String getPathFromUri(@NonNull Uri uri, Context context) {
     String filePath = null;

     if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
     try {
     String[] proj = { MediaStore.Video.Media.DATA, MediaStore.Images.Media.DATA};
     Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
     int column_index = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
     boolean c = cursor.moveToFirst();
     if (c) {
     return cursor.getString(column_index);
     }
     } catch (IllegalStateException e) {
     e.printStackTrace();
     }
     } else {
     filePath = uri.getPath();
     }
     Log.d("", "Chosen path = " + filePath);
     return filePath;
     }
     */
    public static String getPath(Uri uri) {
        return new FileUtils(App.getAppContext()).getPath(uri);
    }

    public static void mediaScannerConnection(String[] strArr) {

        MediaScannerConnection.scanFile(App.getAppContext(), strArr, null, new MediaScannerConnection.OnScanCompletedListener(){

				@Override
				public void onScanCompleted(String p1, Uri p2) {

				}
			}
        );
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
