package com.jefferson.application.br.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import com.jefferson.application.br.App;
import com.jefferson.application.br.MaterialLockView;
import com.jefferson.application.br.R;
import com.jefferson.application.br.util.PasswordManager;
import java.util.List;

public class VerifyActivity extends MyCompatActivity {  

	private Runnable Runnable;
	private Handler Handler;
	private MaterialLockView materialLockView;
	private String password;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        checkPassword();
		setContentView(R.layout.pattern);
        
//        View parent = findViewById(R.id.patternRelativeLayout);
//        TypedValue typedValue = new TypedValue();
//        Resources.Theme theme = getTheme();
//        theme.resolveAttribute(R.attr.colorPrimary, typedValue, true);
//        int color = typedValue.data;
//        parent.setBackgroundColor(color);
//
		materialLockView = (MaterialLockView) findViewById(R.id.pattern);
		materialLockView.setTactileFeedbackEnabled(false);

	    requestPermission();

		Handler = new Handler();
		Runnable = new Runnable() {

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
                        getWindow().setBackgroundDrawableResource(R.drawable.ic_super);
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
	                    startActivity(intent);
                        overridePendingTransition(0, 0);
                    }
					super.onPatternDetected(pattern, SimplePattern);

				}
            }
        );
	}

    @Override
    protected void onApplyCustomTheme() {
        
    }
    
    private boolean canProceed() {
        String action = getIntent().getAction();

        if (action == null) return false;

        switch (action) {
            case Intent.ACTION_MAIN:
                return true;
            case App.ACTION_OPEN_FROM_DIALER:
                return true;
            case App.ACTION_REPORT_CRASH:
                startActivity(new Intent(this, CrashActivity.class).
                              addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).
                              putExtra("message", getIntent().getStringExtra("message")));
                return false;
        }
        return false;
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) { 
                //todo when permission is granted 
            } else { //request for the permission 
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION); 
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
            return;
        }

		if (ContextCompat.checkSelfPermission(this,
											  Manifest.permission.WRITE_EXTERNAL_STORAGE)
			!= PackageManager.PERMISSION_GRANTED) {

			// Should we show an explanation?
			if (ActivityCompat.shouldShowRequestPermissionRationale(this,
																	Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
				// Show an explanation to the user *asynchronously* -- don't block
				// this thread waiting for the user's response! After the user
				// sees the explanation, try again to request the permission.
                Intent intent = new Intent(Intent.ACTION_APPLICATION_PREFERENCES);
                startActivity(intent);

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
        password = new PasswordManager().getInternalPassword();

		if (password.isEmpty()) {
			startActivity(new Intent(getApplicationContext(), CreatePattern.class).setAction(CreatePattern.ENTER_FIST_CREATE).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
            overridePendingTransition(0, 0);
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
					//requestPermission();
				}
			}
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}
}
