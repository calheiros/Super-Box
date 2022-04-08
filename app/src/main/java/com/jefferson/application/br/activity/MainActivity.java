package com.jefferson.application.br.activity;

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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
import android.view.View;
import android.widget.Toast;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.jefferson.application.br.App;
import com.jefferson.application.br.AppLockService;
import com.jefferson.application.br.FileModel;
import com.jefferson.application.br.R;
import com.jefferson.application.br.app.SimpleDialog;
import com.jefferson.application.br.fragment.LockFragment;
import com.jefferson.application.br.fragment.MainFragment;
import com.jefferson.application.br.fragment.SettingFragment;
import com.jefferson.application.br.task.ImportTask;
import com.jefferson.application.br.util.IntentUtils;
import com.jefferson.application.br.util.Storage;
import com.jefferson.application.br.util.ServiceUtils;
import java.io.File;
import java.util.ArrayList;
import android.content.pm.ActivityInfo;
import com.jefferson.application.br.util.DialogUtils;
import android.app.AlertDialog;
import com.jefferson.application.br.widget.MyAlertDialog;
import com.jefferson.application.br.widget.MyAlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import com.jefferson.application.br.util.JDebug;

public class MainActivity extends MyCompatActivity implements NavigationView.OnNavigationItemSelectedListener, ImportTask.ImportTaskListener {

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
    
    BroadcastReceiver receiver;

    private void updateCurrentFragment() {
        if (mainFragment != null) {
            int pagerPosition =  mainFragment.getPagerPosition();
            updateFragment(pagerPosition);
        }
    }

	public static final String admob_key="ca-app-pub-3062666120925607/8250392170";
    public static final String ACTION_START_IN_PREFERENCES = "com.jefferson.application.action.START_IN_PREFERENCES";
    private static final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 0;
    private static final int GET_URI_CODE_TASK = 54;
	private static final int GET_URI_CODE = 98;

    public MainFragment mainFragment;
	private LockFragment lockFragment;
	private DrawerLayout drawerLayout;
	private SettingFragment settingFragment;
	private NavigationView navigationView;
	private Fragment oldFrag;
    private SharedPreferences sharedPreferences;
	private int position;
	private static MainActivity instance;
	private ArrayList<FileModel> models;
	private AdView adview;
	private InterstitialAd interstitial;

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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.DefaultTheme);
        this.instance = this;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		drawerLayout = (DrawerLayout) findViewById(R.id.mainDrawerLayout);
		navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        
		if (savedInstanceState != null) {
			startActivity(new Intent(this, VerifyActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
		}
        
        createFragments();
		createAdView();
		createInterstitial();
        createReceiver();
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
        MobileAds.initialize(this);
        adview = (AdView)findViewById(R.id.ad_view);
		adview.loadAd(new AdRequest.Builder().build());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        // getMenuInflater().inflate(R.menu.main_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	public void createInterstitial() {
        interstitial = new InterstitialAd(this);
		interstitial.setAdUnitId("ca-app-pub-3062666120925607/8580168530");
		interstitial.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    prepareAd();
                }
            });
		prepareAd();
	}

    public void prepareAd() {
        if (interstitial.isLoading() == false && interstitial.isLoaded() == false) {
			interstitial.loadAd(new AdRequest.Builder().build());
		}
	}

    public void showAd() {
        if (interstitial.isLoaded()) {
			interstitial.show();
		} 
	}

    public void updateFragment(int position) {
        if (mainFragment != null) {
            mainFragment.update(position);
        }
    }

	public void updateAllFragments() {
        if (mainFragment != null) {
            mainFragment.updateAll();
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

		boolean toSetting = ACTION_START_IN_PREFERENCES.equals(getIntent().getAction());
		changeFragment(toSetting ? settingFragment: mainFragment);
		navigationView.getMenu().getItem(toSetting ? 2 : 0).setChecked(true);
	}

    private void sorryAlert() {
		View view = getLayoutInflater().inflate(R.layout.dialog_check_box_view, null);
        SimpleDialog dialog = new SimpleDialog(this, SimpleDialog.ALERT_STYLE);
		dialog.setTitle("Erro detectado!");
		dialog.setMessage("Lamento pelo erro ocorrido anteriormente. Por favor, relate o erro ocorrido para que ele seja corrigido o mais rápido possível.");
		dialog.setCanceledOnTouchOutside(false);
		dialog.addContentView(view);
		dialog.setPositiveButton("Relatar", new SimpleDialog.OnDialogClickListener(){
				@Override
				public boolean onClick(SimpleDialog dialog) {

					if (ServiceUtils.isConnected(MainActivity.this))
						Toast.makeText(getApplicationContext(), "obrigado! relatório de erro enviado.", 1).show();
					else 
						Toast.makeText(getApplicationContext(), "obrigado! relatório de será enviado quando estiver conectado.", 1).show();
					return true;
				}
            }
        );

        dialog.setNegativeButton(getString(R.string.cancelar), null);
		dialog.show();
		dialog.setOnDismissListener(new DialogInterface.OnDismissListener(){

				@Override
				public void onDismiss(DialogInterface dInterface) {
					//sharedPreferences.edit().putBoolean(app.EXCEPTION_FOUND, false).commit();

				}
			}
        );
	}

	private void changeFragment(Fragment fragment) {
		if (fragment != getSupportFragmentManager().findFragmentById(R.id.fragment_container)) {

			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
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

		if (resultCode == MyCompatActivity.RESULT_OK) {
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
					sharedPreferences.edit().putString(getString(R.string.EXTERNAL_URI), uri.toString()).commit();
				} 

			} else {

                models = new ArrayList<>();
				position = data.getIntExtra("position", -1);
				ArrayList<String> paths = data.getStringArrayListExtra("selection");

                for (String path : paths) {
					FileModel model = new FileModel();
					model.setResource(path);
					model.setDestination(Storage.getFolder(position == 0 ? Storage.IMAGE: Storage.VIDEO).getAbsolutePath());
					model.setType(data.getStringExtra("type"));
					models.add(model);
				}

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)

                    if (hasExternalFile(paths) && (Storage.getExternalUri(this) == null || getContentResolver().getPersistedUriPermissions().isEmpty())) {
						getSdCardUri(GET_URI_CODE_TASK);
						return;
					}
			} 

            if (requestCode != GET_URI_CODE) {
				ImportTask mTask = new ImportTask(this, models, MainActivity.this);
				mTask.start();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void getSdCardUri(int code) {
		Toast.makeText(this, getString(R.string.selecionar_sdcard), 1).show();
		Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
		startActivityForResult(intent, code);
	}

    private boolean hasExternalFile(ArrayList<String> paths) {
        for (String file:paths) {
			if (Environment.isExternalStorageRemovable(new File(file)))
				return true;
		}
		return false;
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
	    MyAlertDialog.Builder builder = new MyAlertDialog.Builder(this, R.style.CustomAlertDialog);
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
        adview.destroy();

        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }

        super.onDestroy();

    }
}
