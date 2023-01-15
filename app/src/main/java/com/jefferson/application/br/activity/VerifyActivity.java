/*
 * Copyright (C) 2023 Jefferson Calheiros


 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.jefferson.application.br.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import com.jefferson.application.br.MaterialLockView;
import com.jefferson.application.br.R;
import com.jefferson.application.br.util.BlurUtils;
import com.jefferson.application.br.util.MyPreferences;
import com.jefferson.application.br.util.PasswordManager;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Executor;

public class VerifyActivity extends MyCompatActivity {

    private static final int REQUEST_WRITE_READ_PERMISSION_CODE = 13;
    private Runnable Runnable;
    private Handler handler;
    private MaterialLockView materialLockView;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        password = new PasswordManager().getInternalPassword();
        if (password.isEmpty()) {
            startActivity(new Intent(getApplicationContext(), CreatePattern.class).setAction(CreatePattern.ENTER_FIST_CREATE)
                    .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
            overridePendingTransition(0, 0);
            return;
        }
        setNavigationAndStatusBarTransparent();
        setContentView(R.layout.pattern);
        setWallpaper();

        SharedPreferences sharedPrefs = MyPreferences.getSharedPreferences();
        if (sharedPrefs.getBoolean(MyPreferences.KEY_FINGERPRINT, false))
            openBiometricPrompt();

        materialLockView = (MaterialLockView) findViewById(R.id.pattern);
        materialLockView.setTactileFeedbackEnabled(false);
        handler = new Handler();
        Runnable = () -> materialLockView.clearPattern();
        materialLockView.setOnPatternListener(new MyPatternListener());
    }
    private void setNavigationAndStatusBarTransparent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    private void setWallpaper() {
        try {
            ImageView imageView = findViewById(R.id.wallpaper_image_view);
            if (imageView == null) return;
            AssetManager asset = getAssets();

            InputStream rawImage = asset.open("wallpapers/pexels-bruno-thethe.jpg");
            Bitmap wallpaper = BitmapFactory.decodeStream(rawImage);
            BlurUtils.blurBitmap(wallpaper, 25f, this);
            imageView.setImageBitmap(wallpaper);
        } catch (IOException err) {
            Toast.makeText(this, "failed to decode wallpaper", Toast.LENGTH_SHORT).show();
        }
    }

    void openBiometricPrompt() {
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
        popMenu.setOnMenuItemClickListener(p1 -> false);
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

    private class MyPatternListener extends MaterialLockView.OnPatternListener {

        public void onPatternStart() {
            if (handler != null)
                handler.removeCallbacks(Runnable);
        }

        public void onPatternDetected(List<MaterialLockView.Cell> pattern, String SimplePattern) {
            if (!SimplePattern.equals(password)) {
                materialLockView.setDisplayMode(MaterialLockView.DisplayMode.Wrong);
                handler.postDelayed(Runnable, 2000);
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
}
