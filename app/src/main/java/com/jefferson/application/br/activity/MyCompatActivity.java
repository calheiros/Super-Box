package com.jefferson.application.br.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import com.jefferson.application.br.App;
import com.jefferson.application.br.LocaleManager;
import com.jefferson.application.br.util.MyPreferences;
import com.jefferson.application.br.util.StringUtils;
import com.jefferson.application.br.util.ThemeConfig;

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

    public int getAttrColor(int resId) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getTheme();
        theme.resolveAttribute(resId, typedValue, true);
        return typedValue.data;
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        onApplyCustomTheme();
		super.onCreate(savedInstanceState);
        if (!MyPreferences.getAllowScreenshot())
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        pm = (PowerManager) getSystemService("power");
        app = (App) getApplication();
        KEY = StringUtils.getRandomString(8);
        app.putActivity(this, KEY);
        initialized = true;
    }
    
    public int getNavigationBarHeight(Context context, int orientation) {
        Resources resources = context.getResources(); 
        int id = resources.getIdentifier(orientation == Configuration.ORIENTATION_PORTRAIT ? "navigation_bar_height" : "navigation_bar_height_landscape", "dimen", "android");
        if (id > 0) { 
            return resources.getDimensionPixelSize(id);
        } return 0;
    }
    
    public void applyParentViewPadding(View view) {
        int navigationPadding = getNavigationBarHeight(this, Configuration.ORIENTATION_PORTRAIT);
        if (navigationPadding != 0) {
            int bottom = view.getPaddingBottom() + + navigationPadding;
            int left = view.getPaddingLeft();
            int right = view.getPaddingRight();
            int top = view.getPaddingTop();

            view.setPadding(left, top, right, bottom);
        }
    }
    
    protected void onApplyCustomTheme() {
        setTheme(ThemeConfig.getTheme(this));
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
