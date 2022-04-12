package com.jefferson.application.br.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.jefferson.application.br.App;
import com.jefferson.application.br.CodeManager;
import com.jefferson.application.br.R;
import com.jefferson.application.br.activity.MainActivity;
import com.jefferson.application.br.adapter.AppLockAdapter;
import com.jefferson.application.br.model.AppModel;
import com.jefferson.application.br.service.AppLockService;
import com.jefferson.application.br.task.JTask;
import com.jefferson.application.br.util.DialogUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LockFragment extends Fragment implements OnItemClickListener {

    private static final int REQUEST_OVERLAY_CODE = 9;
    private View lastClickedCheckView;
    private int lastClickedItemPosition;

	public LockFragment() {
		startLoadPackagesTask();
	}

	private ProgressBar mProgressBar;
	private TextView mTextView;
	private ArrayList<AppModel> appModels;
	private AppLockAdapter appsAdapter;
	private ListView mListView;
	private Intent intent;
	private View view;
	private LoadApplicationsTask mTask;
    private SwipeRefreshLayout mySwipeRefreshLayout;
    private String LOG_TAG = "LockFragment";

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		MainActivity mActivity = ((MainActivity)getActivity());

        if (view == null) {
			view = inflater.inflate(R.layout.list_view_app, container, false);
			mProgressBar = view.findViewById(R.id.progressApps);            
			mTextView = view.findViewById(R.id.porcent);
			mListView = view.findViewById(R.id.appList);
            mySwipeRefreshLayout = view.findViewById(R.id.swiperefresh);
			mListView.setItemsCanFocus(true);

			if (mTask != null) {
				JTask.Status status = mTask.getStatus();
				if (status == JTask.Status.FINISHED) {
					doTaskFinalized();
				} else {
					showProgressView();
				} 
			}

			intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
			mListView.setOnItemClickListener(this);
            mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {

                    @Override
                    public void onRefresh() {
                        if (mTask.getStatus() == JTask.Status.STARTED) {
                            mySwipeRefreshLayout.setRefreshing(false);
                        } else {
                            appsAdapter.clear();
                            showProgressView();
                            startLoadPackagesTask();
                            //Log.i(LOG_TAG, "onRefresh called from SwipeRefreshLayout");
                            // This method performs the actual data-refresh operation.
                            // The method calls setRefreshing(false) when it's finished.
                            //Toast.makeText(getContext(), "refreshing...", 1).show();
                        }
                    }
                }
            );
        } else {
            if (mySwipeRefreshLayout.isRefreshing()) {
                mySwipeRefreshLayout.setRefreshing(false);
            }
        }

		Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
		mActivity.setupToolbar(toolbar, getString(R.string.bloquear_apps));
		mActivity.getSupportActionBar().dispatchMenuVisibilityChanged(true);
        setHasOptionsMenu(true);

		return view;

	}

    private void showProgressView() {
        mProgressBar.setProgress(0);
        mProgressBar.setIndeterminate(true);
        mProgressBar.setVisibility(View.VISIBLE);
        mTextView.setText("");
        mTextView.setVisibility(View.VISIBLE);
        
    }

    @Override
    public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
        lastClickedItemPosition = position;
        lastClickedCheckView = view.findViewById(R.id.check1);
        boolean noNeedOverlayPermission = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(getContext())) { 
            Log.v("App", "Requesting Permission" + Settings.canDrawOverlays(getContext())); // if not construct intent to request permission 
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getContext().getPackageName())); // request permission via start activity for result 
            startActivityForResult(intent, REQUEST_OVERLAY_CODE); //It will call onActivityResult Function After you press Yes/No and go Back after giving permission 
        } else {
            noNeedOverlayPermission = true;
            Log.v("App", "We already have permission for it."); // disablePullNotificationTouch(); // Do your stuff, we got permission captain 
        }

        if (!needPermissionForBlocking(getContext())) {
            if (noNeedOverlayPermission) {
                appsAdapter.toogleSelection(position);
                animateCheckView(lastClickedCheckView);
            }
        } else {
            AlertDialog.Builder alert = new AlertDialog.Builder(getContext(), R.style.CustomAlertDialog);
            alert.setMessage("Você precisa ativar a permisão \"Acessar dados de uso\" para esta função funcionar corretamente.");
            alert.setPositiveButton("conceder", new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface p1, int p2) {
                        startActivityForResult(intent, 0);
                    }
                }
            );

            alert.setNegativeButton(getString(R.string.cancelar), null);
            AlertDialog alertDialog = alert.create();
            DialogUtils.configureRoudedDialog(alertDialog);
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
        }
    }

    public void animateCheckView(View vi) {
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.checked);
        vi.startAnimation(animation);
    }

    @Override
    public void onPause() {
        super.onPause();
        //mySwipeRefreshLayout.setRefreshing(false);
        if (appsAdapter != null && appsAdapter.isMutable()) {
            getContext().startService(new Intent(getContext(), AppLockService.class)
                                      .setAction(App.ACTION_APPLOCK_SERVICE_UPDATE_DATA));
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (appsAdapter != null) {
            appsAdapter.setMutable(false);
        }

    }

	public void startLoadPackagesTask() {
		mTask = new LoadApplicationsTask(App.getAppContext());
		mTask.start();
	}

    public void doTaskFinalized() {
        
        if (appsAdapter == null) {
            appsAdapter = new AppLockAdapter(getActivity(), appModels);
            mListView.setAdapter(appsAdapter);
        } else {
            appsAdapter.putDataSet(appModels);
        }
        
		mProgressBar.setVisibility(View.GONE);
		mTextView.setVisibility(View.GONE);

        if (mySwipeRefreshLayout.isRefreshing()) {
            mySwipeRefreshLayout.setRefreshing(false);
        }
       appModels = null;
	}

    private boolean needPermissionForBlocking(Context context) {
		return CodeManager.needPermissionForGetUsages(context);
	}

	@Override
	public void onDestroy() {
		if (mTask.getStatus() != JTask.Status.FINISHED) {
            mTask.revokeFinish(true);
            mTask.interrupt();
        }    
		super.onDestroy();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(getContext()) && !CodeManager.needPermissionForGetUsages(getContext())) {
                appsAdapter.toogleSelection(lastClickedItemPosition);
                animateCheckView(lastClickedCheckView);
            }

        } else {
            if (!CodeManager.needPermissionForGetUsages(getContext())) {
                appsAdapter.toogleSelection(lastClickedItemPosition);
                animateCheckView(lastClickedCheckView);
            }
        }
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_message_history, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.item_message_history) {
            Toast.makeText(getContext(), "Not implemented!", Toast.LENGTH_LONG).show();
        }

        return false;
    }


	private class LoadApplicationsTask extends JTask {

        private Context context;
        private double progress;

        public LoadApplicationsTask(Context context) {
            this.context = context;
            appModels = new ArrayList<AppModel>();
        }

        @Override
        public void workingThread() {
            Intent launch = new Intent(Intent.ACTION_MAIN, null);
            launch.addCategory(Intent.CATEGORY_LAUNCHER);

            PackageManager pm = context.getPackageManager();
            List<ResolveInfo> apps = pm.queryIntentActivities(launch, 0);
            Collections.sort(apps, new ResolveInfo.DisplayNameComparator(pm)); 

            for (int i = 0;i < apps.size();i++) {

                if (this.isInterrupted()) {
                    break;
                }
                ResolveInfo p = apps.get(i);

                if (p.activityInfo.packageName.equals(context.getPackageName()))
                    continue;

                AppModel newInfo = new AppModel();
                newInfo.appname = p.loadLabel(pm).toString();
                newInfo.pname = p.activityInfo.packageName;
                newInfo.icon = p.activityInfo.loadIcon(pm);
                appModels.add(newInfo);

                progress = (double)100 / apps.size() * i;
                sendUpdate();
            }
        }

        @Override
        public void onBeingStarted() {

        }

        @Override
        public void onFinished() {

            if (view != null) {
				doTaskFinalized();
            }
        }

        @Override
        public void onException(Exception e) {

        }

        @Override
        protected void onUpdated(Object[] get) {

            if (mTextView != null) {
                mTextView.setText(String.valueOf((int)progress) + "%");
            }

            if (mProgressBar != null) {

                if (mProgressBar.isIndeterminate()) {
                    mProgressBar.setIndeterminate(false);
                    mProgressBar.setMax(100);
                }
                mProgressBar.setProgress((int)progress);
            }


        }
    }
}
