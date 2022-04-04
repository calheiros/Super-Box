package com.jefferson.application.br;

import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import com.jefferson.application.br.database.*;
import com.jefferson.application.br.widget.*;
import java.util.*;
import com.jefferson.application.br.util.*;

public class AppLockWindow {

	private Context context;
	private WindowManager windowManager;
	private View view;
	private WindowManager.LayoutParams params;
	private Handler Handler = new Handler();
	private String currentApp;
	private MaterialLockView materialLockView;
    private boolean isLocked;
	private AppsDatabase database;
	private ImageView image_icon;

	public AppLockWindow(final Context context, final AppsDatabase db) {

	    this.context = context;
        this.database = db;

        int layoutParamsType = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? 
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:
            WindowManager.LayoutParams.TYPE_PHONE; 

		params = new WindowManager.LayoutParams(
			WindowManager.LayoutParams.MATCH_PARENT,
			WindowManager.LayoutParams.MATCH_PARENT,
            layoutParamsType,
			(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | 
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON	|
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD) ,
			PixelFormat.RGBX_8888);

		params.windowAnimations = android.R.style.Animation_Dialog;
		windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

		createView();
	}

	private View createParentView() {

        RelativeLayout layout = new RelativeLayout(context) {

            @Override
            public void onAttachedToWindow() {
                super.onAttachedToWindow();
            }

            @Override 
            public boolean onKeyDown(int k, KeyEvent event) { 
                Toast.makeText(context, "KEY CODE: " + event.getKeyCode(), 1).show();
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                    // < your action > 
                    if (startDefaultLauncher()) {
                        unlock();
                    }
                    JDebug.toast("Back pressed!");
                    return true; 
                } 
                return super.dispatchKeyEvent(event); 
            }

            private boolean startDefaultLauncher() {

                try {
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    String name = context.getPackageManager().queryIntentActivities(
                        new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME),
                        PackageManager.MATCH_DEFAULT_ONLY).get(0).activityInfo.packageName; 
                    intent.setPackage(name);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                } catch (Exception e) {
                    JDebug.toast(e.getMessage());
                    return false;
                }

                return true;
            }
        };

        layout.setFocusable(true);
		View view = LayoutInflater.from(context).inflate(R.layout.pattern, null);
        layout.addView(view);
        return layout;
	}

	public void refreshView() {
        View lastView = view;
		createView();

		if (isLocked()) {
			windowManager.removeViewImmediate(lastView);
			addView(view);
		}
	}

	private void createView() {

		view = createParentView();
	    image_icon = (ImageView) view.findViewById(R.id.iconApp);

		if (currentApp != null)
			image_icon.setImageDrawable(getIcon(currentApp));

		materialLockView = (MaterialLockView) view.findViewById(R.id.pattern);
		materialLockView.setTactileFeedbackEnabled(false);
		materialLockView.setOnPatternListener(new PatternListener(context));
	}

    private FrameLayout getLayout() {
		FrameLayout layout = new FrameLayout(context){
			@Override
			public boolean dispatchKeyEvent(KeyEvent event) {
				if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {

					Intent intent = new Intent(Intent.ACTION_MAIN);
					intent.addCategory(Intent.CATEGORY_HOME);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(intent);

					return true;
				}
				return super.dispatchKeyEvent(event);
			}
		};
		layout.setSystemUiVisibility(5122);

		return layout;
	}

	public boolean isLocked() {
		return isLocked;
	}

	public void lockApp(String AppName) {
		isLocked = true;
		currentApp = AppName;
		image_icon.setImageDrawable(getIcon(AppName));
		addView(view);
	}

	public void addView(View view) {
		windowManager.addView(view, params);
	}

	public void unlock() {
		windowManager.removeView(view);
		isLocked = false;
	}

	private Drawable getIcon(String packageName) {
		try {
			return context.getPackageManager().getApplicationIcon(packageName);
		} catch (PackageManager.NameNotFoundException e) {	
		}
		return null;
	}

	public class PatternListener extends MaterialLockView.OnPatternListener {

		private Context context;
        private PasswordManager passManager;

		public PatternListener(Context context) {
			this.context = context;
			this.passManager = new PasswordManager();
        }

		final Runnable Runnable = new Runnable(){

			@Override
			public void run() {
				materialLockView.clearPattern();
			}
		};

		public void onPatternStart() {
			if (Handler != null && Runnable != null) {
				Handler.removeCallbacks(Runnable);
			}
		}

		public void onPatternDetected(List<MaterialLockView.Cell>pattern, String SimplePattern) {

			if (!SimplePattern.equals(correctPass())) {
				materialLockView.setDisplayMode(MaterialLockView.DisplayMode.Wrong);

				Handler.postDelayed(Runnable, 1000);
			} else {
				unlock();
				materialLockView.clearPattern();
				database.addUnlockedApp(currentApp);
                Toast.makeText(context, "A aplicação continuará desbloqueada até que a tela seja desligada!", Toast.LENGTH_LONG).show();

			}
			super.onPatternDetected(pattern, SimplePattern);
		}

		private Object correctPass() {
			return passManager.getInternalPassword();
		}
	}
}
