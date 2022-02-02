package com.jefferson.application.br.util;

import android.annotation.*;
import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.support.v4.provider.*;
import android.util.*;
import com.jefferson.application.br.*;
import java.io.*;
import java.util.*;
import android.preference.*;

public class DocumentUtil {
	private static String TAG = "document util";

	public static DocumentFile getDocumentFile(final File file, boolean force) {
		String baseFolder = getExtSdCardFolder(file);

		if (baseFolder == null) {
			return null;
		}

		String relativePath = null;
		try {
			String fullPath = file.getCanonicalPath();
			relativePath = fullPath.substring(baseFolder.length() + 1);
		} catch (IOException e) {
			return null;
		}

		Uri treeUri = Storage.getExternalUri(App.getInstance());

		if (treeUri == null) {
			return null;
		}

		// start with root of SD card and then parse through document tree.
		DocumentFile document = DocumentFile.fromTreeUri(App.getInstance(), treeUri);

		String[] parts = relativePath.split("\\/");
		for (int i = 0; i < parts.length; i++) {

			DocumentFile nextDocument = document.findFile(parts[i]);

			if (nextDocument == null && force) {
				if ((i < parts.length - 1) || file.isDirectory()) {
					nextDocument = document.createDirectory(parts[i]);
				} else {
					nextDocument = document.createFile(null, parts[i]);
				}
			}
			document = nextDocument;
			Log.i("next document", nextDocument.getName());

		}

		return document;
	}

	public static String getExtSdCardFolder(final File file) {
		String[] extSdPaths = getExtSdCardPaths();
		try {
			for (int i = 0; i < extSdPaths.length; i++) {
				if (file.getCanonicalPath().startsWith(extSdPaths[i])) {
					return extSdPaths[i];
				}
			}
		} catch (IOException e) {
			return null;
		}
		return null;
	}

	/**
	 * Get a list of external SD card paths. (Kitkat or higher.)
	 *
	 * @return A list of external SD card paths.
	 */
	@TargetApi(Build.VERSION_CODES.KITKAT)
	private static String[]  getExtSdCardPaths() {
		Context context = App.getAppContext();
		List<String> paths = new ArrayList<>();
		for (File file : context.getExternalFilesDirs("external")) {
			if (file != null && !file.equals(context.getExternalFilesDir("external"))) {
				int index = file.getAbsolutePath().lastIndexOf("/Android/data");
				if (index < 0) {
					Log.w(TAG, "Unexpected external file dir: " + file.getAbsolutePath());
				} else {
					String path = file.getAbsolutePath().substring(0, index);
					try {
						path = new File(path).getCanonicalPath();
					} catch (IOException e) {
						// Keep non-canonical path.
					}
					paths.add(path);
				}
			}
		}
		return paths.toArray(new String[paths.size()]);
	}

	/**
	 * Retrieve the application context.
	 *
	 * @return The (statically stored) application context
	 */

}
