package com.jefferson.application.br.adapter;

import android.app.Activity;
import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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

    public SparseBooleanArray selectionArray = new SparseBooleanArray();
	private Activity activity;
	private LayoutInflater inflater = null;
    public ArrayList<AppModel> models;
    public AppsDatabase database;
    public static AppLockService service;
    private boolean mutable;
    private boolean clicked = false;

	public AppLockAdapter(Activity mActivity, ArrayList<AppModel> models) {
        this.activity = mActivity;
		this.models = models; 
		this.database = new AppsDatabase(mActivity);
        syncSelection();
		inflater = (LayoutInflater) mActivity
			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

    public void clear() {
        this.models.clear();
        this.selectionArray.clear();
        notifyDataSetInvalidated();
    }

    public void putDataSet(ArrayList<AppModel> newModels) {
        this.models = newModels;
        syncSelection();
        notifyDataSetChanged();
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
        //Glide.with(activity).load(info.icon).skipMemoryCache(true).into(imageView);
		imageView.setImageDrawable(info.icon);
		textView.setText(info.appname);
		checkView.setChecked(selectionArray.get(position, false));


        Animation animation = AnimationUtils.loadAnimation(activity, R.anim.zoom_in);
        //animation.setDuration(250);
        view.startAnimation(animation);
        
		return view;
    }

    private void syncSelection() {
        ArrayList<String> lockedPackages = database.getLockedPackages();

        for (int i = 0; i < getCount(); i++) {
            AppModel model = models.get(i);
            if (lockedPackages.contains(model.pname))
                selectionArray.put(i, true);
        }
    }

    /* public void toogleSelection(int index) {
     boolean selected = selectionArray.get(index, false);
     selectionArray.put(index, !selected);
     }
     */
	public void toogleSelection(int position, View view) {
        setMutable(true);
		String pname = models.get(position).pname;
        boolean hasSelected = selectionArray.get(position, false);
        
        if (hasSelected) {
			selectionArray.delete(position);
			database.removeLockedApp(pname);
		} else {
			selectionArray.put(position, true);
			database.addLockedApp(pname);
		}
        
        LockCheck lockView = view.findViewById(R.id.check1);
        lockView.setChecked(!hasSelected);
        
		//notifyDataSetChanged();
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

	
