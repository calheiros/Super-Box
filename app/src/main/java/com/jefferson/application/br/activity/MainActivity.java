package com.jefferson.application.br.activity;

import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.net.*;
import android.os.*;
import android.preference.*;
import android.support.design.widget.*;
import android.support.v4.app.*;
import android.support.v4.view.*;
import android.support.v4.widget.*;
import android.support.v7.app.*;
import android.support.v7.widget.*;
import android.view.*;
import android.widget.*;
import com.google.android.gms.ads.*;
import com.jefferson.application.br.*;
import com.jefferson.application.br.app.*;
import com.jefferson.application.br.fragment.*;
import com.jefferson.application.br.task.*;
import com.jefferson.application.br.util.*;
import java.io.*;
import java.util.*;

import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import com.jefferson.application.br.R;
import android.support.v4.provider.*;

public class MainActivity extends MyCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

	public static final String admob_key="ca-app-pub-3062666120925607/8250392170";
    public static final String ACTION_INIT_WITH_PREFERENCES = "preferences_init_action";

	private final int GET_URI_CODE = 98;
    private MainFragment mainFragment;
	private LockFragment lockFragment;
	private DrawerLayout drawerLayout;
	private SettingFragment settingFragment;
	private NavigationView navigationView;
	private App app;
	private Fragment oldFrag;
    private SharedPreferences sharedPreferences;
	private int position;
	private static MainActivity instante;
	private ArrayList<FileModel> models;
	private static final int GET_URI_CODE_TASK = 54;
	private AdView adview;
	private InterstitialAd interstitial;

	public static MainActivity getInstance() {
		return instante;
	}

	public void update(MainFragment.ID id) {

		if (mainFragment != null)
			mainFragment.update(id);
	}

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
        this.instante = this;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
        
		MobileAds.initialize(this);

		drawerLayout = (DrawerLayout) findViewById(R.id.mainDrawerLayout);
		navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		if (savedInstanceState != null) {
			startActivity(new Intent(this, VerifyActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
		}
        initialize();
		initGoogleAdView();
		createInterstitial();
        //test only
        //startActivity(new Intent(this, ContatosActvity.class));
	}

	private void initGoogleAdView() {
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
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.ads_item_menu) {

		}
		return super.onOptionsItemSelected(item);
	}

	private void initialize() {
		this.mainFragment = new MainFragment();
	    this.lockFragment = new LockFragment();
        this.settingFragment = new SettingFragment();

		boolean toSetting = ACTION_INIT_WITH_PREFERENCES.equals(getIntent().getAction());
		changeFragment(toSetting ? settingFragment: mainFragment);
		navigationView.getMenu().getItem(toSetting ? 2 : 0).setChecked(true);

	}
	private int index;
	public void showDialogChoose() {

		index = 0;
		String[] options = new String[]{getString(R.string.armaz_interno), getString(R.string.armaz_externo)};
		if (Storage.getExternalStorage() == null)
			options = new String[]{getString(R.string.armaz_interno)};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.armazenamento));
		builder.setSingleChoiceItems(options, getStoragePosition(), new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int position) {
					index = position;
				}
			});
		builder.setPositiveButton(getString(R.string.salvar), new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int p2) {

					Storage.setNewLocalStorage(index);
					if (mainFragment != null)
						mainFragment.update(MainFragment.ID.BOTH);
					settingFragment.updateItem(4);
				}
			});
		builder.setNegativeButton(getString(R.string.cancelar), null);
		builder.create().show();
	}

	public int getStoragePosition() {

        String storageLocation = Storage.getStorageLocation();
        if (Storage.INTERNAL.equals(storageLocation)) {
            return 0;
        }
        if (Storage.EXTERNAL.equals(storageLocation)) {
            return 1;
        }
        return -1;
    }

	private void sorryAlert() {

		View view = getLayoutInflater().inflate(R.layout.dialog_check_box_view, null);

        SimpleDialog dialog = new SimpleDialog(this, SimpleDialog.ALERT_STYLE);
		dialog.setContentTitle("Erro detectado!");
		dialog.setContentText("Lamento pelo erro ocorrido anteriormente. Por favor, relate o erro ocorrido para que ele seja corrigido o mais rápido possível.");
		dialog.setCanceledOnTouchOutside(false);
		dialog.addContentView(view);
		dialog.setPositiveButton("Relatar", new SimpleDialog.OnDialogClickListener(){
				@Override
				public boolean onClick(SimpleDialog dialog) {

					if (Utils.isConnected(MainActivity.this))
						Toast.makeText(getApplicationContext(), "obrigado! relatório de erro enviado.", 1).show();
					else 
						Toast.makeText(getApplicationContext(), "obrigado! relatório de será enviado quando estiver conectado.", 1).show();
					return true;
				}});
		dialog.setNegativeButton(getString(R.string.cancelar), null);
		dialog.show();
		dialog.setOnDismissListener(new DialogInterface.OnDismissListener(){

				@Override
				public void onDismiss(DialogInterface dInterface) {
					sharedPreferences.edit().putBoolean(app.EXCEPTION_FOUND, false).commit();

				}
			});
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
		if (!Utils.isMyServiceRunning(AppLockService.class)) {
			startService(new Intent(this, AppLockService.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

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
				ImportTask mTask = new ImportTask(models, this, ImportTask.SESSION_INSIDE_APP);
				mTask.execute();
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

		new SimpleDialog(this, SimpleDialog.ALERT_STYLE)
			.setContentTitle(getString(R.string.confirmacao))
			.setContentText(getString(R.string.quer_realmente_sair))
			.setPositiveButton(getString(R.string.nao), null)
			.setNegativeButton(getString(R.string.sim), new SimpleDialog.OnDialogClickListener(){

				@Override
				public boolean onClick(SimpleDialog dialog) {
					finish();
					return true;
				}
			})
			.show();
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
        super.onDestroy();
		adview.destroy();
    }

}
