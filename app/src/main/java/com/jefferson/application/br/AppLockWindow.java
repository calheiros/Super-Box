package com.jefferson.application.br;

import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import com.jefferson.application.br.database.*;

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
    private boolean locked;
	private AppLockDatabase database;
	private ImageView iconImageView;
    private String passedApp = "";
    private String password;
    private View lastView;
    
	public AppLockWindow(final Context context, final AppLockDatabase db) {

	    this.context = context;
        this.database = db;

        int layoutParamsType = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? 
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:
            WindowManager.LayoutParams.TYPE_PHONE; 

		params = new WindowManager.LayoutParams(
			WindowManager.LayoutParams.MATCH_PARENT,
			WindowManager.LayoutParams.MATCH_PARENT,
            layoutParamsType,
			(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_DIM_BEHIND |
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD) ,
			PixelFormat.TRANSLUCENT);

		params.windowAnimations = android.R.style.Animation_Dialog;
		windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

		createView();
	}

    public void setPassword(String realPassword) {
        this.password = realPassword;
    }

    public void revokePassed() {
        passedApp = "";
    }

    public String getLockePackageName() {
        return currentApp;
    }

    public String getPassedApp() {
        return passedApp;
    }

	private View createParentView() {
        FrameLayout layout = new FrameLayout(context) {

            @Override
            public void onAttachedToWindow() {
                super.onAttachedToWindow();
            }

            @Override 
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                    startDefaultLauncher();
                    JDebug.toast("Back pressed!");
                }
                //Toast.makeText(context, "KEY " + e.getKeyCode(), 1).show();
                return true;
            }
        };

        //layout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        layout.setFocusable(true);

        View root = LayoutInflater.from(context).inflate(R.layout.pattern, layout);
        return root;
    }
    private boolean startDefaultLauncher() {

        try {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            JDebug.toast(e.getMessage());
            return false;
        }

        return true;
    }
	public void refreshView() {
		createView();

		if (isLocked()) {
			windowManager.removeViewImmediate(lastView);
            windowManager.addView(view, params);
		}
	}

	private void createView() {
        lastView = view;
		view = createParentView();
	    iconImageView = (ImageView) view.findViewById(R.id.iconApp);
        materialLockView = (MaterialLockView) view.findViewById(R.id.pattern);

		if (currentApp != null) 
			iconImageView.setImageDrawable(getIconDrawable(currentApp));

        if (materialLockView != null) {
            materialLockView.setTactileFeedbackEnabled(false);
            materialLockView.setOnPatternListener(new PatternListener(context));
        }
	}

	public boolean isLocked() {
		return locked;
	}

	public void lock(String appName) {
		locked = true;
		currentApp = appName;
		iconImageView.setImageDrawable(getIconDrawable(appName));
        windowManager.addView(view, params);
        
	}

//    private void openFakeDialog(String appName) {
//        Intent intent = new Intent(context, FakeDialogActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.putExtra("app_name", appName);
//        context.startActivity(intent);
//    }
//
	public void unlock() {
        try {
            windowManager.removeView(view);
        } catch (IllegalArgumentException e) {
            //JDebug.writeLog(e.getCause());
        }
		locked = false;
	}

	private Drawable getIconDrawable(String packageName) {
		try {
			return context.getPackageManager().getApplicationIcon(packageName);
		} catch (PackageManager.NameNotFoundException e) {}
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
                passedApp = currentApp;
                //Toast.makeText(context, "A aplicação continuará desbloqueada até que a tela seja desligada!", Toast.LENGTH_LONG).show();
			}

			super.onPatternDetected(pattern, SimplePattern);
		}

		private String correctPass() {
            if (password != null) {
                return password;
            } else {
                SharedPreferences prefs = MyPreferences.getSharedPreferences(context);
                return prefs.getString(PasswordManager.PATTERN_KEY, "1234");
            }
		}
	}
}
