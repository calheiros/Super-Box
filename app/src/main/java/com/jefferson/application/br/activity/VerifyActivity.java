package com.jefferson.application.br.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupMenu;
import com.jefferson.application.br.App;
import com.jefferson.application.br.MaterialLockView;
import com.jefferson.application.br.R;
import com.jefferson.application.br.util.PasswordManager;
import java.util.List;
import android.widget.Toast;
import android.hardware.biometrics.BiometricPrompt;
import android.hardware.biometrics.BiometricPrompt.Builder;

public class VerifyActivity extends MyCompatActivity {  

	private Runnable Runnable;
	private Handler Handler;
	private MaterialLockView materialLockView;
	private String password;

    private static final int REQUEST_WRITE_READ_PERMSSION_CODE = 13;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        password = new PasswordManager().getInternalPassword();
		super.onCreate(savedInstanceState);

        if (password.isEmpty()) {
            startActivity(new Intent(getApplicationContext(), CreatePattern.class).setAction(CreatePattern.ENTER_FIST_CREATE).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
            overridePendingTransition(0, 0);
            return;
        }

        if (Build.VERSION.SDK_INT >= 21) { 
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.pattern);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
//        View parent = findViewById(R.id.patternRelativeLayout);
//        TypedValue typedValue = new TypedValue();
//        Resources.Theme theme = getTheme();
//        theme.resolveAttribute(R.attr.colorPrimary, typedValue, true);
//        int color = typedValue.data;
//        parent.setBackgroundColor(color);

        
//      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
//          
//        BiometricPrompt.Builder promptInfo = new BiometricPrompt.Builder(this);
//        promptInfo.setTitle("Unlock Super Box");
//        promptInfo.build().authenticate(null);
//        }
		materialLockView = (MaterialLockView) findViewById(R.id.pattern);
		materialLockView.setTactileFeedbackEnabled(false);

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
                        if (haveWriteReadPermission()) {
                            startMainActivity();
                        } else {
                            requestWriteReadPermission();
                        }
                    }
					super.onPatternDetected(pattern, SimplePattern);

				}
            }
        );
	}

    @Override
    protected void onApplyCustomTheme() {
        setTheme(R.style.LauncherTheme);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_WRITE_READ_PERMSSION_CODE) {
            if (haveWriteReadPermission()) {
                startMainActivity();
            } else {
                materialLockView.clearPattern();
                Toast.makeText(this, "Required permission not allowed!", Toast.LENGTH_SHORT).show();
            }
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
					startMainActivity();
                    break;
				}
			}
		}
	}

    private void startMainActivity() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}
}
