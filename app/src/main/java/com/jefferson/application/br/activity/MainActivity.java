package com.jefferson.application.br.activity;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.jefferson.application.br.R;
import com.jefferson.application.br.fragment.LockFragment;
import com.jefferson.application.br.fragment.MainFragment;
import com.jefferson.application.br.fragment.SettingFragment;
import com.jefferson.application.br.service.AppLockService;
import com.jefferson.application.br.task.ImportTask;
import com.jefferson.application.br.task.MonoTypePrepareTask;
import com.jefferson.application.br.util.DialogUtils;
import com.jefferson.application.br.util.IntentUtils;
import com.jefferson.application.br.util.MyPreferences;
import com.jefferson.application.br.util.ServiceUtils;
import com.jefferson.application.br.util.Storage;
import com.jefferson.application.br.util.ThemeConfig;
import com.jefferson.application.br.widget.MyAlertDialog;
import java.util.ArrayList;

public class MainActivity extends MyCompatActivity implements NavigationView.OnNavigationItemSelectedListener, ImportTask.Listener {

    private void updateCurrentFragment() {
        if (mainFragment != null) {
            int pagerPosition =  mainFragment.getPagerPosition();
            updateFragment(pagerPosition);
        }
    }

	public static final String admob_key="ca-app-pub-3062666120925607/8250392170";
    public static final String ACTION_START_IN_PREFERENCES = "com.jefferson.application.action.START_IN_PREFERENCES";
    private static final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 12;
    private static final int GET_URI_CODE_TASK = 54;
	private static final int GET_URI_CODE = 98;
    public static final int IMPORT_FROM_GALLERY_CODE = 43;
    
    private BroadcastReceiver receiver;
    public MainFragment mainFragment;
	private LockFragment lockFragment;
	private DrawerLayout drawerLayout;
	private SettingFragment settingFragment;
	private NavigationView navigationView;
	private Fragment oldFrag;
    private SharedPreferences sharedPreferences;
	private int position;
	private static MainActivity instance;
	private AdView adview;
    public boolean calculatorStateEnabled;
    private boolean restarting;
    
	//private InterstitialAd interstitial;

    public static MainActivity getInstance() {
		return instance;
	}

    public static final String ACTION_UPDATE = "com.jefferson.application.action.UPDATE_FRAGMENTS";

	public void setupToolbar(Toolbar toolbar, CharSequence title) {
		setSupportActionBar(toolbar);
		getSupportActionBar().setTitle(title);
		getSupportActionBar().setDisplayShowHomeEnabled(false);

		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.abc_action_bar_home_description);
		toggle.syncState();
		drawerLayout.setDrawerListener(toggle);
	}
    
    public static int CURRENT_THEME;

    public void setRestarting(boolean restarting) {
        this.restarting = restarting;
    }

    public void setupToolbar(Toolbar toolbar, String string, int menuId) {

    }

    @Override
    public void onBeingStarted() {

    }

    @Override
    public void onUserInteration() {

    }

    @Override
    public void onInterrupted() {
        updateCurrentFragment();
    }

    @Override
    public void onFinished() {
        updateCurrentFragment();
    }
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        this.instance = this;
        super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
        this.CURRENT_THEME = ThemeConfig.getTheme(this);
        
        if (Build.VERSION.SDK_INT >= 21) { 
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

		drawerLayout = (DrawerLayout) findViewById(R.id.mainDrawerLayout);
		navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        calculatorStateEnabled = isCalculatorComponentEnabled();

		if (savedInstanceState != null) {
			startActivity(new Intent(this, VerifyActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
		}

        createFragments();
		createAdView();
		//createInterstitial();
        createReceiver();
	}

    public void showSnackBar(String message, int length) {
        if (mainFragment != null) {
            mainFragment.showSnackBar(message, length);
        }
    }

    public void removeFolder(int folderPosition, int pagerPostion) {
        if (mainFragment != null) {
            mainFragment.removeFolder(folderPosition, pagerPostion);
        }
    }

    private void createReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_UPDATE);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateAllFragments();
            }
        };
        registerReceiver(receiver, filter);
    }

	private void createAdView() {
        adview = (AdView)findViewById(R.id.ad_view);
		adview.loadAd(new AdRequest.Builder().build());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        // getMenuInflater().inflate(R.menu.main_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

//	public void createInterstitial() {
//        interstitial = new InterstitialAd(this);
//		interstitial.setAdUnitId("ca-app-pub-3062666120925607/8580168530");
//		interstitial.setAdListener(new AdListener() {
//                @Override
//                public void onAdClosed() {
//                    prepareAd();
//                }
//            });
//		prepareAd();
//	}

//    public void prepareAd() {
//        if (interstitial.isLoading() == false && interstitial.isLoaded() == false) {
//			interstitial.loadAd(new AdRequest.Builder().build());
//		}
//	}
//
//    public void showAd() {
//        if (interstitial.isLoaded()) {
//			interstitial.show();
//		} 
//	}
//
    public void updateFragment(int position) {
        if (mainFragment != null) {
            mainFragment.updateFragment(position);
        }
    }

	public void updateAllFragments() {
        if (mainFragment != null) {
            mainFragment.updateAllFragments();
        }
    }

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.ads_item_menu) {
		}
		return super.onOptionsItemSelected(item);
	}

    public boolean requestPermission() { 
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE); 
                return true; 
            } 
        } return false;
    }

	private void createFragments() {
        this.mainFragment = new MainFragment();
	    this.lockFragment = new LockFragment();
        this.settingFragment = new SettingFragment();
        boolean startInSetting = ACTION_START_IN_PREFERENCES.equals(getIntent().getAction());

        if (startInSetting) {
            settingFragment.setCalculatorEnabled(getIntent().getBooleanExtra("calculator_enabled", false));
        } else {
            settingFragment.setCalculatorEnabled(calculatorStateEnabled);
        }
		changeFragment(startInSetting ? settingFragment: mainFragment);
		navigationView.getMenu().getItem(startInSetting ? 2 : 0).setChecked(true);
	}

    private boolean isCalculatorComponentEnabled() {
        return PackageManager.COMPONENT_ENABLED_STATE_ENABLED == getPackageManager().getComponentEnabledSetting(new ComponentName(this, "com.jefferson.application.br.CalculatorAlias"));
    }

//    private void sorryAlert() {
//		View view = getLayoutInflater().inflate(R.layout.dialog_check_box_view, null);
//        SimpleDialog dialog = new SimpleDialog(this, SimpleDialog.ALERT_STYLE);
//		dialog.setTitle("Erro detectado!");
//		dialog.setMessage("Lamento pelo erro ocorrido anteriormente. Por favor, relate o erro ocorrido para que ele seja corrigido o mais rápido possível.");
//		dialog.setCanceledOnTouchOutside(false);
//		dialog.setContentView(view);
//		dialog.setPositiveButton("Relatar", new SimpleDialog.OnDialogClickListener(){
//				@Override
//				public boolean onClick(SimpleDialog dialog) {
//
//					if (ServiceUtils.isConnected(MainActivity.this))
//						Toast.makeText(getApplicationContext(), "obrigado! relatório de erro enviado.", 1).show();
//					else 
//						Toast.makeText(getApplicationContext(), "obrigado! relatório de será enviado quando estiver conectado.", 1).show();
//					return true;
//				}
//            }
//        );
//
//        dialog.setNegativeButton(getString(R.string.cancelar), null);
//		dialog.show();
//		dialog.setOnDismissListener(new DialogInterface.OnDismissListener(){
//
//				@Override
//				public void onDismiss(DialogInterface dInterface) {
//					//sharedPreferences.edit().putBoolean(app.EXCEPTION_FOUND, false).commit();
//
//				}
//			}
//        );
//	}

	private void changeFragment(Fragment fragment) {
		if (fragment != getSupportFragmentManager().findFragmentById(R.id.fragment_container)) {

			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

			if (oldFrag != null)
				transaction.detach(oldFrag);
			transaction.replace(R.id.fragment_container, fragment);
			transaction.attach(fragment);

			transaction.commit();
			oldFrag = fragment;
		}
	}

	@Override
	public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.main_item1:
                changeFragment(mainFragment);
                break;
            case R.id.main_item2:
                changeFragment(lockFragment);
                break;
            case R.id.item3:
                changeFragment(settingFragment);
                break;
            case R.id.item_4:
                try {
                    IntentUtils.shareApp(this);
                } catch (ActivityNotFoundException e) {
                    activityNotFound();
                }
                break;
            case R.id.item_5:
                try {
                    IntentUtils.reportBug(this);
                } catch (ActivityNotFoundException e) {
                    activityNotFound();
                }
		}

		drawerLayout.closeDrawer(GravityCompat.START);
		return true;
	}

	public void activityNotFound() {
		Toast.makeText(this, "Nenhum app encontrado!", Toast.LENGTH_LONG).show();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

        if (!ServiceUtils.isMyServiceRunning(AppLockService.class)) {
			startService(new Intent(this, AppLockService.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            if (Settings.canDrawOverlays(this)) { 

            }
        }

		if (resultCode == RESULT_OK) {
          
            if (requestCode == 69) {
                updateFragment(position);
                return;
            }
            
            if (requestCode == SettingFragment.CALCULATOR_CREATE_CODE_RESULT) {
                settingFragment.setCodeDescription(MyPreferences.getCalculatorCode());
                return;
            }

            if (requestCode == MainFragment.GET_FILE) {

                Uri uri = null;

                if (data != null) {
					uri = data.getData();
					Toast.makeText(this, uri.getPath(), 1).show();
				}
				return;
			}

			if (requestCode == GET_URI_CODE || requestCode == GET_URI_CODE_TASK) {

                Uri uri = data.getData();

                if (Storage.checkIfSDCardRoot(uri)) {
                    getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    Storage.storeExternalUri(uri.toString());
                } 

			} else if (requestCode == IMPORT_FROM_GALLERY_CODE) {
				position = data.getIntExtra("position", -1);
                String type = data.getStringExtra("type");
				ArrayList<String> paths = data.getStringArrayListExtra("selection");

                Intent intent = new Intent(this, ImportMediaActivity.class);
                intent.putExtra(ImportMediaActivity.TYPE_KEY, type);
                intent.putExtra(ImportMediaActivity.MEDIA_LIST_KEY, paths);
                intent.putExtra(ImportMediaActivity.POSITION_KEY, position);
                this.startActivityForResult(intent, 69);

//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && (Storage.getExternalUri(this) == null || getContentResolver().getPersistedUriPermissions().isEmpty()))
//                    preparationTask.setOnLoopListener(new MonoTypePrepareTask.onLoopListener() {
//
//                            @Override
//                            public void onLoop(String path) {
//
//                                if (Environment.isExternalStorageRemovable(new File(path))) {
//                                    preparationTask.setOnLoopListener(null);
//                                    preparationTask.revokeFinish(true);
//                                    getSdCardUri(GET_URI_CODE_TASK);
//                                }
//                            }
//                        }
//                    );
//                preparationTask.start();
//			}
//
//            if (requestCode == GET_URI_CODE_TASK) {
//                preparationTask.proceed();
//            }
            }
        } else {
            //Toast.makeText(this, "RESUKT_CANCELLED " + requestCode, 1).show();
        }
		super.onActivityResult(requestCode, resultCode, data);
	}

    private Handler getSdCardUriHandler = new Handler() {

        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            Toast.makeText(MainActivity.this, getString(R.string.selecionar_sdcard), 1).show();
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            startActivityForResult(intent, msg.what);

        }

    };

	private void getSdCardUri(int code) {
        getSdCardUriHandler.sendEmptyMessage(code);
	}

    @Override
	public void onBackPressed() {

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            showExitDialog();
        } else {
            drawerLayout.openDrawer(GravityCompat.START);
        }
	}

	private void showExitDialog() {
	    MyAlertDialog.Builder builder = new MyAlertDialog.Builder(this, DialogUtils.getTheme());
        builder.setTitle(getString(R.string.confirmacao));
        builder.setMessage(getString(R.string.quer_realmente_sair));
        builder.setPositiveButton(getString(R.string.sim), new DialogInterface.OnClickListener(){

                @Override
                public void onClick(DialogInterface face, int i) {
                    finish();
                }
            }
        );
        builder.setNegativeButton(getString(R.string.nao), null).show();
	}

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
		adview.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
		adview.pause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        instance = null;
        adview.destroy();

        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }

        if (!restarting) {
            boolean enabled = settingFragment.isCalculatorEnabledInSettings();
            if (enabled != isCalculatorComponentEnabled()) {

                settingFragment.disableLauncherActivity(enabled);
                settingFragment.setCompomentEnabled(!enabled, "com.jefferson.application.br.CalculatorAlias");

                Toast.makeText(this, getString(R.string.aplicando_configuracoes), 1).show();
            }
        }
        super.onDestroy();
    }
}
