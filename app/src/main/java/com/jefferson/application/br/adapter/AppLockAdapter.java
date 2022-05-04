package com.jefferson.application.br.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
import com.jefferson.application.br.util.JDebug;
import com.jefferson.application.br.widget.LockCheck;
import java.util.ArrayList;
import java.util.HashMap;
import android.support.annotation.NonNull;

public class AppLockAdapter extends BaseAdapter {

    public SparseBooleanArray selectionArray = new SparseBooleanArray();
	private Activity activity;
	private LayoutInflater inflater = null;
    public ArrayList<AppModel> models;
    public AppsDatabase database;
    public static AppLockService service;
    private boolean mutable;
    private HashMap<Integer, View> cachedViews;
    private int searchedItemPosition = -1;
    private volatile View view;

	public AppLockAdapter(Activity mActivity, ArrayList<AppModel> models) {
        this.activity = mActivity;
		this.models = models; 
        this.cachedViews = new HashMap<>();
		this.database = new AppsDatabase(mActivity);
        syncSelection();

		inflater = (LayoutInflater) mActivity
			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

    public void animateSearchedItem(int x) {
        View view = cachedViews.get(x);

        if (view != null) {
            view.startAnimation(getBlinkAnimation());
        } else {
            JDebug.toast("View is NULL");
        }
    }

    private Animation getBlinkAnimation() {
        return AnimationUtils.loadAnimation(activity, R.anim.blink);         
    }

    public void setSearchedItem(int x) {
        this.searchedItemPosition = x;
    }

    public void clear() {
        this.models.clear();
        this.selectionArray.clear();
        this.cachedViews.clear();
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

    public int getItemHeight() {
        int height = 1;
        if (view != null) {
            height = view.getHeight();
        }
        return height;
    }
	public View getView(final int position, View convertView, ViewGroup parent) {
        view = convertView;

        if (view == null) {
			view = inflater.inflate(R.layout.list_item, null);
        }

	    AppModel info = models.get(position);
		ImageView imageView = (ImageView) view.findViewById(R.id.iconeApps);
		TextView textView = (TextView) view.findViewById(R.id.app_name);
		LockCheck checkView = (LockCheck) view.findViewById(R.id.check1);

        @NonNull Drawable icon = info.icon;
		imageView.setImageDrawable(icon);
		textView.setText(info.appname);
		checkView.setChecked(selectionArray.get(position, false));
        Animation animation = AnimationUtils.loadAnimation(activity, R.anim.zoom_in);
        animation.setAnimationListener(new Animation.AnimationListener() {

                @Override
                public void onAnimationStart(Animation p1) {

                }

                @Override
                public void onAnimationEnd(Animation anim) {
                    animateIfSearchedItem(position);
                }


                @Override
                public void onAnimationRepeat(Animation p1) {

                }
            }
        );
        view.startAnimation(animation);
        cachedViews.put(position, view);

        return view;
    }

//    private Drawable getResizedDrawable(int position, View view) {
//
//        Drawable drawable = models.get(position).icon;
//        int previewWidth = 0;
//        int previewHeight = 0;
//        int mViewWidth = view.getWidth();
//        int mViewHeight = view.getHeight();
//        int newWidth = 0;
//        int newHeight = 0;
//
//        if (drawable != null && drawable.getIntrinsicWidth() > 0 && drawable.getIntrinsicHeight() > 0) { 
//            // the intrinsic dimensions can be -1 in some cases such as ColorDrawables which aim to fill 
//            // the whole View 
//            previewWidth = drawable.getIntrinsicWidth(); 
//            previewHeight = drawable.getIntrinsicHeight(); 
//        } 
//
//        final float widthScale = mViewWidth / (float) (previewWidth);
//        
//        if (widthScale != 1f) 
//            newWidth = Math.max((int)(widthScale * previewWidth), 1);
//        
//        final float heightScale = mViewHeight / (float) (previewHeight); 
//        
//        if (heightScale != 1f)
//            newHeight = Math.max((int)(heightScale * previewHeight), 1); 
//
//        // Define the Canvas and Bitmap the drawable will be drawn against 
//        final Canvas c = new Canvas(); 
//        Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap(); 
//        c.setBitmap(bitmap); // Draw the scaled drawable into the final bitmap 
//        
//        if (drawable != null) {
//            drawable.setBounds(0, 0, newWidth, newHeight);
//            drawable.draw(c); 
//        }
//        return drawable;
//    }
//    
    private void animateIfSearchedItem(int position) {
        final View view = cachedViews.get(position);

        if (view == null) {
            return; //JDebug.toast("animateIfSearched: View is NULL");
        }

        if (searchedItemPosition == position) {
            view.post(new Runnable(){
                    @Override 
                    public void run() {
                        Animation blinkAnim = getBlinkAnimation();
                        //anim.setDuration(1000);
                        blinkAnim.setStartTime(1000);
                        view.startAnimation(blinkAnim);
                        searchedItemPosition = -1;
                    }
                }
            );
        }
    }

    private void syncSelection() {
        ArrayList<String> lockedPackages = database.getLockedPackages();

        for (int i = 0; i < getCount(); i++) {
            AppModel model = models.get(i);
            if (lockedPackages.contains(model.pname))
                selectionArray.put(i, true);
        }
    }

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
//        LockCheck lockView = view.findViewById(R.id.check1);
//        lockView.setChecked(!hasSelected);
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

	
