package com.jefferson.application.br.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.jefferson.application.br.R;
import com.jefferson.application.br.database.AppsDatabase;
import com.jefferson.application.br.model.AppModel;
import com.jefferson.application.br.service.AppLockService;
import com.jefferson.application.br.widget.LockCheck;
import java.util.ArrayList;

public class AppLockAdapter extends BaseAdapter {

	public ArrayList<String> selection;
	private Activity mActivity;
	private LayoutInflater inflater = null;
    public ArrayList<AppModel> models;
    public AppsDatabase database;
    public static AppLockService service;

    private boolean mutable;

	public AppLockAdapter(Activity mActivity, ArrayList<AppModel> models) {
        this.mActivity = mActivity;
		this.models = models; 
		this.database = new AppsDatabase(mActivity);
        this.selection = database.getLockedPackages();
		inflater = (LayoutInflater) mActivity
			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

    public void setMutable(boolean mutable) {
        this.mutable = mutable;
    }

    public boolean isMutable() {
        return mutable;
    }

	public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (convertView == null) {
			view = inflater.inflate(R.layout.list_item, null);
        }
        
	    AppModel info = models.get(position);
		ImageView imageView = (ImageView) view.findViewById(R.id.iconeApps);
		TextView textView = (TextView) view.findViewById(R.id.app_name);
		LockCheck checkView = (LockCheck) view.findViewById(R.id.check1);

		imageView.setImageDrawable(info.icon);
		textView.setText(info.appname);
		checkView.setChecked(selection.contains(info.pname));

		return view;
    }

	public void toogleSelection(int position) {
        setMutable(true);
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

	
