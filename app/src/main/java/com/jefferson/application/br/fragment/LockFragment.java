package com.jefferson.application.br.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
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

public class LockFragment extends Fragment implements OnItemClickListener, android.support.v7.widget.SearchView.OnQueryTextListener {

    private static final int REQUEST_OVERLAY_CODE = 9;
    private View lastClickedParentView;
    private int lastClickedItemPosition;
    private SearchView searchView;

	public LockFragment() {
		startLoadPackagesTask();
	}
    private int firstVisibleItem = -1;
    private int lastVisibleItem = -1;
    private int visibleCount = -1;
    private int totalItemCount = -1;
	private ProgressBar mProgressBar;
	private TextView mTextView;
	private ArrayList<AppModel> appModels;
	private AppLockAdapter appsAdapter;
	private ListView mListView;
	private Intent intent;
	private View parentView;
	private LoadApplicationsTask mTask;
    private SwipeRefreshLayout mySwipeRefreshLayout;
    private String LOG_TAG = "LockFragment";

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		MainActivity mActivity = ((MainActivity)getActivity());

        if (parentView == null) {
			parentView = inflater.inflate(R.layout.list_view_app, container, false);
			mProgressBar = parentView.findViewById(R.id.progressApps);            
			mTextView = parentView.findViewById(R.id.porcent);
			mListView = parentView.findViewById(R.id.appList);
            mySwipeRefreshLayout = parentView.findViewById(R.id.swiperefresh);
			mListView.setItemsCanFocus(true);
            TypedValue typedValue = new TypedValue();
            Resources.Theme theme = getActivity().getTheme();
            theme.resolveAttribute(R.attr.colorBackgroundLight, typedValue, true);
            int color = typedValue.data;
           
            mySwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary);
			mySwipeRefreshLayout.setProgressBackgroundColorSchemeColor(color);// .setProgressBackgroundColor(color);
            mListView.setOnScrollListener(new AbsListView.OnScrollListener() {

                    @Override
                    public void onScrollStateChanged(AbsListView p1, int p2) {

                    }

                    @Override public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) { 
                        LockFragment.this.lastVisibleItem = firstVisibleItem + visibleItemCount;
                        LockFragment.this.firstVisibleItem = firstVisibleItem;
                        LockFragment.this.visibleCount = visibleItemCount;
                        LockFragment.this.totalItemCount = totalItemCount;
                    }
                }
            );

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
                        }
                    }
                }
            );
        } else {
            if (mySwipeRefreshLayout.isRefreshing()) {
                mySwipeRefreshLayout.setRefreshing(false);
            }
        }

		Toolbar toolbar = parentView.findViewById(R.id.toolbar);
		mActivity.setupToolbar(toolbar, getString(R.string.bloquear_apps));
		mActivity.getSupportActionBar().dispatchMenuVisibilityChanged(true);
        setHasOptionsMenu(true);
		return parentView;
	}

    private void applicationFound(int x) {
        mListView.smoothScrollToPositionFromTop(x, (mListView.getHeight() / 2) - (appsAdapter.getItemHeight() / 2));
        //mListView.smoothScrollToPosition(scrollPosition);
        hideInputMethod(getActivity().getWindow().getCurrentFocus());

        if (x >= firstVisibleItem && x < lastVisibleItem) {
            //Toast.makeText(getContext(), "It's a visible item!", 1).show();
            appsAdapter.animateSearchedItem(x);
        } else {
            appsAdapter.setSearchedItem(x);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String input) {

        if (appsAdapter == null || input.isEmpty()) {
            return false;
        }

        AppModel model = null;
        ArrayList<AppModel> models = appsAdapter.models;
        int firstContains = -1;

        for (int x = 0; x < models.size(); x++) {
            model = models.get(x);
            String lowerName = model.appname.toLowerCase();
            String lowerInput = input.toLowerCase();

            if (lowerName.startsWith(lowerInput)) {
                //JDebug.toast("match + " + models.get(x).appname);
                applicationFound(x);
                return true;
            } else if (firstContains == -1) {
                if (lowerName.contains(lowerInput)) {
                    firstContains = x;
                    //JDebug.toast(lowerName + " contains => " + input + " position " + firstContains);
                }
            }
        }

        if (firstContains != -1) {
            applicationFound(firstContains);
            return true;
        }

        Toast toast = Toast.makeText(getContext(), "No match found!", Toast.LENGTH_SHORT);
        //showInputMethod(searchView);
        return false;
    }

//    private Handler scrollHandler = new Handler();
//    private Runnable scrollRunnbale = new Runnable() {
//
//        @Override
//        public void run() {
//
//            if (totalItemCount - scrollPosition < visibleCount) {
//            }
//            //JDebug.toast("scroll ps " + scrollPosition + " total " + totalItemCount + " totalVisible " + visibleCount);
//            //mListView.smoothScrollToPosition(scrollPosition);
//        }
//    };

    @Override
    public boolean onQueryTextChange(String input) {

//        if (appsAdapter == null || input.isEmpty()) {
//            return false;
//        }
//
//        AppModel model = null;
//        ArrayList<AppModel> models = appsAdapter.models;
//        scrollHandler.removeCallbacks(scrollRunnbale);
//
//        for (int x = 0; x < models.size(); x++) {
//            model = models.get(x);
//
//            if (model.appname.toLowerCase().startsWith(input.toLowerCase())) {
//                //JDebug.toast("match + " + models.get(x).appname);
//                scrollPosition = x;
//                scrollHandler.postDelayed(scrollRunnbale, 1000);
//                return true;
//            }
//        }
//
        return false;
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
        lastClickedParentView = view;
        boolean noNeedOverlayPermission = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(getContext())) { 
            Log.v("App", "Requesting Permission" + Settings.canDrawOverlays(getContext())); // if not construct intent to request permission 
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getContext().getPackageName())); // request permission via start activity for result 
            startActivityForResult(intent, REQUEST_OVERLAY_CODE); //It will call onActivityResult Function After you press Yes/No and go Back after giving permission 
        } else {
            noNeedOverlayPermission = true;
            Log.v("App", "We already have permission for it.");
            // disablePullNotificationTouch(); 
            // Do your stuff, we got permission captain 
        }

        if (!needPermissionForBlocking(getContext())) {
            if (noNeedOverlayPermission) {
                appsAdapter.toogleSelection(position, view);
                animateCheckView(lastClickedParentView);
            }
        } else {
            AlertDialog.Builder alert = new AlertDialog.Builder(getContext(), DialogUtils.getTheme());
            alert.setMessage(getString(R.string.usage_data_permission_message));
            alert.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface p1, int p2) {
                        startActivityForResult(intent, 0);
                    }
                }
            );

            alert.setNegativeButton(getString(android.R.string.cancel), null);
            AlertDialog alertDialog = alert.create();
            DialogUtils.configureRoudedDialog(alertDialog);
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
        }
    }

    public void animateCheckView(View vi) {
        View lockView = vi.findViewById(R.id.check1);
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.checked);
        lockView.startAnimation(animation);
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
                appsAdapter.toogleSelection(lastClickedItemPosition, parentView);
                animateCheckView(lastClickedParentView);
            }

        } else {
            if (!CodeManager.needPermissionForGetUsages(getContext())) {
                appsAdapter.toogleSelection(lastClickedItemPosition, parentView);
                animateCheckView(lastClickedParentView);
            }
        }
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_message_history, menu);

//        SearchManager searchManager =(SearchManager) getContext().getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.search).getActionView();
//            searchView.setSearchableInfo(
//        searchManager.getSearchableInfo(getActivity().getComponentName()));
//
        searchView.setOnQueryTextFocusChangeListener(new  OnFocusChangeListener() {
                @Override public void onFocusChange(View view, boolean hasFocus) {
                    if (hasFocus) { 
                        showInputMethod(view.findFocus());
                    }
                }
            }
        );
        searchView.setOnQueryTextListener(this);
		super.onCreateOptionsMenu(menu, inflater);
	}

    private void showInputMethod(View view) {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE); 

        if (imm != null) {
            imm.showSoftInput(view, 0);
        } 
    }

    private void hideInputMethod(@NonNull View view) {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE); 

        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*if (item.getItemId() == R.id.item_message_history) {
         Toast.makeText(getContext(), "Not implemented!", Toast.LENGTH_LONG).show();
         }
         */
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

                if (p.activityInfo.packageName.equals(context.getPackageName())) {
                    continue;
                }
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

            if (parentView != null) {
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
