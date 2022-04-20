package com.jefferson.application.br.activity;

import android.*;
import android.content.*;
import android.os.*;
import android.util.*;
import com.jefferson.application.br.*;
import com.jefferson.application.br.util.*;
import android.widget.Toast;
import android.view.View;
import android.graphics.Color;

public class MyCompatActivity extends android.support.v7.app.AppCompatActivity {
   
	public static String KEY;
    private boolean allowQuit;
    private App app;
    private boolean initialized;
    private PowerManager pm;
	private boolean running;

	public boolean isAlive() {
		return running;
	}
    @Override
    protected void onResume() {
        super.onResume();
        
        if (this.app.isCounting()) {
            app.stopCount();
        }
		
        allowQuit = false;
		running = true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
    
    @Override
    public void startActivity(Intent intent) {
        this.allowQuit = true;
        super.startActivity(intent);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        Context context = LocaleManager.updateResources(newBase, LocaleManager.getLanguage(App.getAppContext()));
        super.attachBaseContext(context);
    }
    
    @Override
    public void startActivityForResult(Intent intent, int i) {
        this.allowQuit = true;
        super.startActivityForResult(intent, i);
    }

    @Override
    public void finish() {
        this.allowQuit = true;
        super.finish();
    }
   
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        onApplyCustomTheme();
		super.onCreate(savedInstanceState);

        pm = (PowerManager) getSystemService("power");
        app = (App) getApplication();
        KEY = StringUtils.getRandomString(8);
        app.putActivity(this, KEY);
        initialized = true;
    }
    
    protected void onApplyCustomTheme() {
        setTheme(MyPreferences.getThemeResId());
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        if (this.initialized) {
			app.remove(this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
		running = false;
        
        if (!pm.isScreenOn()) {
            app.startCount(5000);
        } else if (!allowQuit) {
            app.startCount();
        }
    }
}
