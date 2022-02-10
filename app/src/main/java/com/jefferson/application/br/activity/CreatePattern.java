package com.jefferson.application.br.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.jefferson.application.br.MaterialLockView;
import com.jefferson.application.br.PasswordManager;
import com.jefferson.application.br.R;
import com.jefferson.application.br.activity.MyCompatActivity;
import java.util.List;

public class CreatePattern extends MyCompatActivity {

	private SharedPreferences settings;
	private SharedPreferences.Editor editor;
	private String password = null;
	public static final String ENTER_FIST_CREATE = "fist_create";
	public static final String ENTER_RECREATE = "recreate";
	private Handler handler;
	private Runnable runnable;
	private Handler handlerC;
	private Runnable runnableC;
	private String action;
	private MaterialLockView materialLockView;
    private PasswordManager passwordManager;
	private String defaultText;
	private Button button;
	private TextView text;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_pattern);
        passwordManager = new PasswordManager(this);
        action = getIntent().getAction();
		defaultText = getString(R.string.desenhe_seu_padrao);

	    text = (TextView) findViewById(R.id.pattern_text);
		button = (Button)findViewById(R.id.bt_pattern);
		text.setText(defaultText);
        button.setEnabled(false);

		materialLockView = (MaterialLockView) findViewById(R.id.pattern);
		materialLockView.setTactileFeedbackEnabled(false);
        settings = getSharedPreferences("config", MODE_PRIVATE);
		editor = settings.edit();

		materialLockView.setOnPatternListener( new MaterialLockView.OnPatternListener() {
                public void onPatternStart() {
					if (runnableC != null && handlerC != null) {
						handlerC.removeCallbacks(runnableC);
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

		button.setOnClickListener( new OnClickListener() {

				@Override
				public void onClick(View v) {

					editor.putString("pattern", password).commit();
					passwordManager.setPassword(password);

					if (action == ENTER_FIST_CREATE) { 
						Intent intent = new Intent(CreatePattern.this, MainActivity.class);
						startActivity(intent);
						finish();
					} else if (action == ENTER_RECREATE)
						finish();
				    else throw new NullPointerException("Action desconhecida");
				}
            }
        );
	}
    
    private void clearPattern() {
		handlerC = new Handler();
		runnableC = new Runnable(){

			@Override
			public void run() {
				materialLockView.clearPattern();
			}
		};
		handlerC.postDelayed(runnableC, 1500);
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

