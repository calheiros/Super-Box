package com.jefferson.superbox.br.activity;

import android.app.*;
import android.content.*;
import android.media.*;
import android.net.*;
import android.os.*;
import android.preference.*;
import android.support.design.widget.*;
import android.support.v4.app.*;
import android.support.v4.view.*;
import android.support.v4.widget.*;
import android.support.v7.app.*;
import android.support.v7.widget.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import com.jefferson.superbox.br.*;
import com.jefferson.superbox.br.fragment.*;
import java.io.*;
import java.util.*;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;

import android.support.v7.widget.Toolbar;

public class Drawer extends MyCompatActivity {
    private DrawerLayout mDrawerLayout;
    private Toolbar toolbar;
	private NavigationView mNavegation;
	
	private TabLayout tabLayout;
	private Context mContext;
	private Drawer.AppSectionsPagerAdapter mAppSectionsPagerAdapter;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    android.support.v7.app.ActionBarDrawerToggle mDrawerToggle;
	private SharedPreferences sharedPrefrs;
	private SharedPreferences.Editor editor;

	private Boolean allowStopActivity = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = getApplicationContext();
		sharedPrefrs = PreferenceManager.getDefaultSharedPreferences(this);
		editor = sharedPrefrs.edit();

        mTitle = mDrawerTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		tabLayout = (TabLayout) findViewById(R.id.tabLayoutPedido);
		mNavegation = (NavigationView)findViewById(R.id.my_navigation_view);

        setupToolbar();
		setupDrawerToggle();

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(true);

	    mNavegation.setNavigationItemSelectedListener(new OnNavegationListener());

        int branco = getResources().getColor(R.color.white);
		int cinza = getResources().getColor(R.color.cinza);
		//tabLayout.setSelectedTabIndicatorColor(branco);
		tabLayout.setTabTextColors(cinza, branco);
		
	    mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager(), this);

		ViewPager mViewPager = (ViewPager) findViewById(R.id.pager);
        //showNotification();
        mViewPager.setAdapter(mAppSectionsPagerAdapter);
		mViewPager.setPageMargin(20);
		mViewPager.setOffscreenPageLimit(3);
		tabLayout.setupWithViewPager(mViewPager);
		//verifyNumExecute();
	}
	/*private void verifyNumExecute() {
		int exec = sharedPrefrs.getInt("numExecute", 0);

		 if (exec == 2)
		 {
		 editor.putInt("numExecute", 666);
		 editor.commit();
		 showRating_dialog();
		 }
		 else if (exec != 666)
		 {
		 editor.putInt("numExecute", exec + 1);
		 editor.commit();

		 }

	}*/

	/*public void showNotification() {
	    Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		v.vibrate(100);
		Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		NotificationCompat.Builder mBuilder =
			(NotificationCompat.Builder) new NotificationCompat.Builder(this)
			.setSmallIcon(R.drawable.ic_launcher)
			.setSound(notification)
			.setContentTitle("Simple notification")
			.setContentText("This is test of simple notification.");

		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		notificationManager.notify(0, mBuilder.build());
	}*/
	public static class AppSectionsPagerAdapter extends FragmentPagerAdapter {
		Drawer drawer;
		public AppSectionsPagerAdapter(FragmentManager fm, Drawer drawer) {
			super(fm);
			this.drawer = drawer;
		}

        @Override
        public Fragment getItem(int i) {
            switch (i) {
				case 0:
					return null;
				case 1:
					return new LockFragment();

				default: return null;
			}
		}

        @Override
        public int getCount() {
			return 2;
		}

        @Override
        public CharSequence getPageTitle(int position) {
			switch (position) {
				case 0:
					return "Main";
				case 1:
					return "Bloqueio";
				case 2: 

                default: return null;
			}
		}
	}
    private class OnNavegationListener implements  NavigationView.OnNavigationItemSelectedListener {

		@Override
		public boolean onNavigationItemSelected(MenuItem menu) {
			allowStop(true);
			Intent intent = null;
			switch (menu.getItemId()) {
				case R.id.nav_item_0:
					intent = new Intent(mContext, MainActivity.class);

					break;
				case R.id.nav_sub_1:

					Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + "jefferson.calheiros10@gmail.com"));
					startActivity(Intent.createChooser(emailIntent, "Enviar email"));
					break;
				case R.id.nav_sub_2:

					intent = new Intent(Intent.ACTION_SEND);
					intent.setType("text/*");
					intent.addCategory(Intent.CATEGORY_DEFAULT);
					intent.putExtra(Intent.EXTRA_TEXT, "http://app-security.br.uptodown.com/android");
					break;
			}
			if (intent != null) 
				startActivity(intent);
			mDrawerLayout.closeDrawers();

			return false;
		}
	}
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

        return super.onOptionsItemSelected(item);
	}

    @Override
    public void setTitle(CharSequence title) {
		mTitle = title;
        getSupportActionBar().setTitle(mTitle);
	}

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
	}

    void setupToolbar() {
		toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
	}

    void setupDrawerToggle() {
		mDrawerToggle = new android.support.v7.app.ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.app_name, R.string.app_name);
        mDrawerToggle.syncState();
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (!allowStopActivity) {
			if (!isFinishing()) {
				finish();
		    } 
		}
	}

	public void allowStop(Boolean bool) {
		allowStopActivity = bool;
	}
	@Override
	public void onBackPressed() {
		showExitDialog();
	}

	private void showExitDialog() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(getString(R.string.sair))
			.setCancelable(true)
			.setPositiveButton(getString(R.string.sim), new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int i) {
					finish();
				}
			});
		alert.setNegativeButton(getString(R.string.cancelar), new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int i) {

					dialog.cancel();
				}

			});
		alert.create().show();

	}
    
	@Override
	protected void onRestart() {
		super.onRestart();
		allowStop(false);
	}
}
