package com.jefferson.application.br.activity;

import android.*;
import android.content.*;
import android.content.pm.*;
import android.media.*;
import android.os.*;
import android.support.v4.app.*;
import android.support.v4.content.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import com.jefferson.application.br.*;

import java.io.*;
import java.util.*;

import com.jefferson.application.br.R;

public class VerifyActivity extends android.support.v7.app.AppCompatActivity {  

	private SharedPreferences settings;
	private Runnable Runnable;
	private Handler Handler;
	private MaterialLockView materialLockView;
	private String password;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pattern);
	    LocaleManager.configureLocale(this);
	
		settings = getSharedPreferences("config", MODE_PRIVATE);
        password = settings.getString("pattern", "");

		checkPassword();
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

		materialLockView = (MaterialLockView) findViewById(R.id.pattern);
		materialLockView.setTactileFeedbackEnabled(false);
		//Toast.makeText(this, "Tiktok!", 1).show();
	    requestPermission();

		Handler = new Handler();
		Runnable = new Runnable()
		{
			@Override
			public void run() {
				materialLockView.clearPattern();
			}
		};
		materialLockView.setOnPatternListener(new MaterialLockView.OnPatternListener() {
                public void onPatternStart() {
					try {
						Handler.removeCallbacks(Runnable);
					} catch (Exception e) {}
				}

				public void onPatternDetected(List<MaterialLockView.Cell>pattern, String SimplePattern) {
					if (!SimplePattern.equals(password)) {
						materialLockView.setDisplayMode(MaterialLockView.DisplayMode.Wrong);

						Handler.postDelayed(Runnable, 2000);
					} else {
						materialLockView.setDisplayMode(MaterialLockView.DisplayMode.Correct);
	                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
						finish();
					}
					super.onPatternDetected(pattern, SimplePattern);

				}});
	}
	
	private void startPopupMenu(View view) {
		PopupMenu popMenu = new PopupMenu(this, view);
		popMenu.getMenuInflater().inflate(R.menu.menu_recovery_pass, popMenu.getMenu());
		popMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener(){

				@Override
				public boolean onMenuItemClick(MenuItem p1) {
					return false;
				}
			});
		popMenu.show();
	}

	public void requestPermission() {

		if (ContextCompat.checkSelfPermission(this,
											  Manifest.permission.WRITE_EXTERNAL_STORAGE)
			!= PackageManager.PERMISSION_GRANTED) {

			// Should we show an explanation?
			if (ActivityCompat.shouldShowRequestPermissionRationale(this,
																	Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
				// Show an explanation to the user *asynchronously* -- don't block
				// this thread waiting for the user's response! After the user
				// sees the explanation, try again to request the permission.
                 Intent inten = new Intent(Intent.ACTION_APPLICATION_PREFERENCES);
                 startActivity(inten);
                  
			} else {
				// No explanation needed, we can request the permission.
				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 12);
				// MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
				// app-defined int constant. The callback method gets the
				// result of the request.
			}
		} else {
			checkPassword();
		}
	}
	public void checkPassword() {
		if (password.isEmpty()) {
			startActivity(new Intent(getApplicationContext(), CreatePattern.class).setAction(CreatePattern.ENTER_FIST_CREATE).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		for (int i = 0; i < permissions.length; i++) {
			String permission = permissions[i];
			int grantResult = grantResults[i];

			if (permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
				if (grantResult == PackageManager.PERMISSION_GRANTED) {
					checkPassword();
				} else {
					requestPermission();
				}
			}
		}
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}
}
