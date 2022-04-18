package com.jefferson.application.br.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.jefferson.application.br.App;
import com.jefferson.application.br.MaterialLockView;
import com.jefferson.application.br.R;
import com.jefferson.application.br.activity.MyCompatActivity;
import com.jefferson.application.br.service.AppLockService;
import com.jefferson.application.br.util.PasswordManager;
import java.util.List;

public class CreatePattern extends MyCompatActivity {

	private String password = null;
	public static final String ENTER_FIST_CREATE = "fist_create";
	public static final String ENTER_RECREATE = "recreate";
	private Handler handler;
	private Runnable runnable;
	private Runnable clearRunnable;
    private Handler clearHandler;
	private String action;
	private MaterialLockView materialLockView;
    private PasswordManager passwordManager;
	private String defaultText;
	private Button button;
	private TextView text;
    private String oldPass;
    
    public void sendCommandService(String key) {
        Intent intent = new Intent(this, AppLockService.class);
        intent.setAction(App.ACTION_APPLOCK_SERVICE_UPDATE_PASSWORD);
        intent.putExtra("key", key);
        startService(intent);
        //stopService(intent);
    }
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_pattern);
        passwordManager = new PasswordManager();
        oldPass = passwordManager.getInternalPassword();
        action = getIntent().getAction();
		defaultText = getString(R.string.desenhe_seu_padrao);

	    text = (TextView) findViewById(R.id.pattern_text);
		button = (Button)findViewById(R.id.bt_pattern);
		text.setText(defaultText);
        button.setEnabled(false);

		materialLockView = (MaterialLockView) findViewById(R.id.pattern);
		materialLockView.setTactileFeedbackEnabled(false);

		materialLockView.setOnPatternListener(new MaterialLockView.OnPatternListener() {
                public void onPatternStart() {
					if (clearRunnable != null && clearHandler != null) {
						clearHandler.removeCallbacks(clearRunnable);
					}
					text.setText(getString(R.string.solte_para_terminar));
                }

				public void onPatternDetected(List<MaterialLockView.Cell>pattern, String SimplePattern) {

					if (SimplePattern.length() >= 4) {
						if (password != null) {
							if (password.equals(SimplePattern)) {
								button.setEnabled(true);
								materialLockView.setEnabled(false);
								text.setText(getString(R.string.senha_definida_como));
							} else {
								text.setText(getString(R.string.tente_de_novo));
								materialLockView.setDisplayMode(MaterialLockView.DisplayMode.Wrong);
								clearPattern();
							}
						} else {
							materialLockView.setEnabled(false);
							password = SimplePattern;
							text.setText(getString(R.string.padrao_salvo));
							materialLockView.setDisplayMode(MaterialLockView.DisplayMode.Correct);

							handler = new Handler();
							runnable = new Runnable(){

								@Override
								public void run() {
									materialLockView.setEnabled(true);
									materialLockView.clearPattern();
									text.setText(getString(R.string.desenhe_novamente));
								}

							};
							handler.postDelayed(runnable, 1500);
						}
					} else {
						materialLockView.setDisplayMode(MaterialLockView.DisplayMode.Wrong);
						text.setText(getString(R.string.connect_mais));
						clearPattern();
					}

					super.onPatternDetected(pattern, SimplePattern);
                }
            }
        );

		button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
                    passwordManager.setPassword(password);
                    sendCommandService(password);

                    switch (action) {
                        case ENTER_FIST_CREATE: 
                            requestPermission();
                            break;
                        case ENTER_RECREATE:
                            finish();
                            break;
                        default:
                            Toast.makeText(CreatePattern.this, "UNKNOWN ACTION!", 1).show();
                    }
				}
            }
        );
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
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.putExtra("package", getPackageName());
                startActivity(intent);

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 12);
                // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            Intent intent = new Intent(CreatePattern.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            overridePendingTransition(0,0);
            
        }
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Toast.makeText(this, "on result " + requestCode,1).show();
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void clearPattern() {
		clearHandler = new Handler();
		clearRunnable = new Runnable(){

			@Override
			public void run() {
				materialLockView.clearPattern();
			}
		};
		clearHandler.postDelayed(clearRunnable, 1500);
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	public void onBackPressed() {
		if (password != null) {
			text.setText(defaultText);
			password = null;
			button.setEnabled(false);
			materialLockView.clearPattern();
			handler.removeCallbacks(runnable);
			if (!materialLockView.isEnabled()) {
				materialLockView.setEnabled(true);
			}
		} else {
			super.onBackPressed();
		}
	}
}

