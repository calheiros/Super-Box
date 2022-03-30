package com.jefferson.application.br.fragment;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.jefferson.application.br.App;
import com.jefferson.application.br.CodeManager;
import com.jefferson.application.br.R;
import com.jefferson.application.br.activity.MainActivity;
import com.jefferson.application.br.adapter.AppsAdapter;
import com.jefferson.application.br.model.AppModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.jefferson.application.br.util.DialogUtils;

public class LockFragment extends Fragment implements OnItemClickListener {

    
    private static final int REQUEST_OVERLAY_CODE = 9;

    private View lastClickedCheckView;
    private int lastClickedItemPosition;

	public LockFragment() {
		initTask();
	}

	private ProgressBar mProgressBar;
	private TextView mTextView;
	private ArrayList<AppModel> models = new ArrayList<>();
	private AppsAdapter mAdapter;
	private ListView mListView;
	private Intent intent;

	private View view;
	private Task mTask;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		MainActivity mActivity = ((MainActivity)getActivity());
		if (view == null) {
			view = inflater.inflate(R.layout.list_view_app, container, false);
			mProgressBar = (ProgressBar)view.findViewById(R.id.progressApps);            
			mTextView = (TextView) view.findViewById(R.id.porcent);
			mListView = (ListView) view.findViewById(R.id.appList);
			mListView.setItemsCanFocus(true);

			if (mTask != null) {
				AsyncTask.Status status = mTask.getStatus();
				if (status == AsyncTask.Status.FINISHED) {
					finalizeTask();
				} else {
					mProgressBar.setVisibility(View.VISIBLE);
					mTextView.setVisibility(View.VISIBLE);
				} 
			}

			intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
			mListView.setOnItemClickListener(this);
		}

		Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
		mActivity.setupToolbar(toolbar, getString(R.string.bloquear_apps));
		mActivity.getSupportActionBar().dispatchMenuVisibilityChanged(true);

		return view;
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
                mAdapter.toogleSelection(position);
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
            alert.setNegativeButton("Ignorar", null);
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
	public void initTask() {
		mTask = new Task(App.getAppContext());
		mTask.execute();
	}

    public void finalizeTask() {
		mAdapter = new AppsAdapter(getActivity(), models);
		mListView.setAdapter(mAdapter);
		mProgressBar.setVisibility(View.GONE);
		mTextView.setVisibility(View.GONE);
	}

    private boolean needPermissionForBlocking(Context context) {
		return CodeManager.needPermissionForGetUsages(context);
	}

	@Override
	public void onDestroy() {

		if (mTask.getStatus() != AsyncTask.Status.FINISHED)
			mTask.cancel(true);

		super.onDestroy();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(getContext()) && !CodeManager.needPermissionForGetUsages(getContext())) {
                mAdapter.toogleSelection(lastClickedItemPosition);
                animateCheckView(lastClickedCheckView);
            }

        } else {
            if (!CodeManager.needPermissionForGetUsages(getContext())) {
                mAdapter.toogleSelection(lastClickedItemPosition);
                animateCheckView(lastClickedCheckView);
            }
        }
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu, menu);
		super.onCreateOptionsMenu(menu, inflater);
	} 

	private class Task extends AsyncTask {

        private Context context;
        private double progress;
        public Task(Context context) {
            this.context = context;
        }

		@Override
		protected void onProgressUpdate(Object[] values) {
			super.onProgressUpdate(values);

            if (mTextView != null)
				mTextView.setText(String.valueOf((int)progress) + "%");

            if (mProgressBar != null) {

                if (mProgressBar.isIndeterminate()) {
                    mProgressBar.setIndeterminate(false);
                    mProgressBar.setMax(100);
                }
                mProgressBar.setProgress((int)progress);
            }
		}

        @Override
        protected Void doInBackground(Object... params) {

            try {
				Intent launch = new Intent(Intent.ACTION_MAIN, null);
				launch.addCategory(Intent.CATEGORY_LAUNCHER);

				PackageManager pm = context.getPackageManager();
				List<ResolveInfo> apps = pm.queryIntentActivities(launch, 0);
				Collections.sort(apps, new ResolveInfo.DisplayNameComparator(pm)); 

				for (int i = 0;i < apps.size();i++) {

					if (isCancelled()) {
						break;
                    }
					ResolveInfo p = apps.get(i);

					if (p.activityInfo.packageName.equals(context.getPackageName()))
						continue;

					AppModel newInfo = new AppModel();
					newInfo.appname = p.loadLabel(pm).toString();
					newInfo.pname = p.activityInfo.packageName;
					newInfo.icon = p.activityInfo.loadIcon(pm);
					models.add(newInfo);

					progress = (double)100 / apps.size() * i;
					publishProgress();
				}
			} catch (NullPointerException e) {
				Log.e("Lock Fragment err", e.toString());
			}
			return null;
		}

        @Override
        protected void onPostExecute(Object v) {
			if (view != null)
				finalizeTask();
		}
	}
}
