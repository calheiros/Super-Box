package com.jefferson.application.br.util;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;

public class IntentUtils {
	public static void shareApp(Context context) throws ActivityNotFoundException {

		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_TEXT, "http://app-security.br.uptodown.com/android");
		context.startActivity(intent);
	}
	public static void reportBug(Context context)throws ActivityNotFoundException {
		Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + "jefferson.calheiros10@gmail.com"));
		intent.putExtra(Intent.EXTRA_SUBJECT, "Super Box - bug report");
		context.startActivity(Intent.createChooser(intent, "Relatar bug"));
	}
    
    public static Intent getDocumentTreeIntent(Context cx) {
        StorageManager sm = (StorageManager)
        cx.getSystemService(Context.STORAGE_SERVICE); 
        StorageVolume sv = sm.getPrimaryStorageVolume(); 
        Intent intent = sv.createOpenDocumentTreeIntent(); 
        intent.addFlags (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
        return intent;
    }
}
