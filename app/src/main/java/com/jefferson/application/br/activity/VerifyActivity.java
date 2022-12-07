package com.jefferson.application.br.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.PopupMenu;
import com.jefferson.application.br.MaterialLockView;
import com.jefferson.application.br.R;
import com.jefferson.application.br.util.JDebug;
import com.jefferson.application.br.util.PasswordManager;
import java.util.List;
import java.util.concurrent.Executor;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

public class VerifyActivity extends MyCompatActivity {  

	private Runnable Runnable;
	private Handler Handler;
	private MaterialLockView materialLockView;
	private String password;

    private static final int REQUEST_WRITE_READ_PERMISSION_CODE = 13;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        password = new PasswordManager().getInternalPassword();
        super.onCreate(savedInstanceState);
        if (JDebug.isDebugOn())
            Toast.makeText( this, getIntent().getAction(), Toast.LENGTH_SHORT).show();
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
        
//      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
//          
//        BiometricPrompt.Builder promptInfo = new BiometricPrompt.Builder(this);
//        promptInfo.setTitle("Unlock Super Box");
//        promptInfo.build().authenticate(null);
//        }
        //
        checkBiometricSupport();
        materialLockView = (MaterialLockView) findViewById(R.id.pattern);
		materialLockView.setTactileFeedbackEnabled(false);

		Handler = new Handler();
		Runnable = () -> materialLockView.clearPattern();

		materialLockView.setOnPatternListener(new MaterialLockView.OnPatternListener() {
                public void onPatternStart() {
					try {
						Handler.removeCallbacks(Runnable);
					} catch (Exception ignored) {}
				}

				public void onPatternDetected(List<MaterialLockView.Cell>pattern, String SimplePattern) {
					if (!SimplePattern.equals(password)) {
						materialLockView.setDisplayMode(MaterialLockView.DisplayMode.Wrong);
						Handler.postDelayed(Runnable, 2000);
                        wrongPasswdAnimation();
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

    void checkBiometricSupport() {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.biometric_title))
                .setSubtitle(getString(R.string.biometric_subtitle))
                .setDescription(getString(R.string.biometric_desc))
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK | BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .setConfirmationRequired(false)
                .build();

        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(VerifyActivity.this, errString, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                startMainActivity();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(VerifyActivity.this, "authentication failed", Toast.LENGTH_SHORT).show();
            }
        });
        biometricPrompt.authenticate(promptInfo);
    }
    @Override
    protected void onApplyCustomTheme() {
        setTheme(R.style.LauncherTheme);
    }

	private void startPopupMenu(View view) {
		PopupMenu popMenu = new PopupMenu(this, view);
		popMenu.getMenuInflater().inflate(R.menu.menu_recovery_pass, popMenu.getMenu());
		popMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

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
        if (requestCode == REQUEST_WRITE_READ_PERMISSION_CODE) {
            if (haveWriteReadPermission()) {
                startMainActivity();
            } else {
                materialLockView.clearPattern();
                Toast.makeText(this, "Required permission not allowed!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void wrongPasswdAnimation() {
        Animation shakeAnim = AnimationUtils.loadAnimation(this, R.anim.shake_anim);
        View view = findViewById(R.id.icon_super_view);
        if (view != null) {
            view.startAnimation(shakeAnim);
        }
    }

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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
