package com.jefferson.application.br.activity;

import android.app.*;
import android.content.*;
import android.os.*;
import com.jefferson.application.br.*;

public class Launcher extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        
        Intent i = new Intent(Launcher.this, VerifyActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
	}
}
