package com.jefferson.application.br.adapter;

import android.app.*;
import android.content.*;
import android.view.*;
import android.widget.*;
import com.jefferson.application.br.*;
import com.jefferson.application.br.database.*;
import com.jefferson.application.br.model.*;
import com.jefferson.application.br.widget.*;
import java.util.*;

public class AppsAdapter extends BaseAdapter {

	public ArrayList<String> selection;
	private Activity mActivity;
	private LayoutInflater inflater = null;
    public ArrayList<AppModel> models;
    public AppsDatabase database;
    public static AppLockService service;

	public AppsAdapter(Activity mActivity, ArrayList<AppModel> models) {
        this.mActivity = mActivity;
		this.models = models; 
		this.database = new AppsDatabase(mActivity);
        this.selection = database.getLockedApps();
		inflater = (LayoutInflater) mActivity
			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
		
        if (convertView == null) 
			view = inflater.inflate(R.layout.list_item, null);

	    AppModel info = models.get(position);
		ImageView mImageView = (ImageView) view.findViewById(R.id.iconeApps);
		TextView mTextView = (TextView) view.findViewById(R.id.app_name);
		CheckWidget mCheckView = (CheckWidget) view.findViewById(R.id.check1);

		mImageView.setImageDrawable(info.icon);
		mTextView.setText(info.appname);
		mCheckView.setChecked(selection.contains(info.pname));

		return view;
    }

	public void toogleSelection(int position) {
        //AppLockService appLockService = ((App)mActivity.getApplication()).appLockService;
		String pname = models.get(position).pname;
		if (selection.contains(pname)) {
			selection.remove(pname);
			database.removeLockedApp(pname);
		} else {
			selection.add(pname);
			database.addLockedApp(pname);
		}
		notifyDataSetChanged();
	}

	public final int getCount() {
		return models.size();
	}

	public final Object getItem(int position) {
		return models.get(position);
	}

	public final long getItemId(int position) {
		return position;
	}

}

	
