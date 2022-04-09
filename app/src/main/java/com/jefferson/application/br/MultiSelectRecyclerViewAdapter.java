package com.jefferson.application.br;

import android.content.*;
import android.media.*;
import android.net.*;
import android.support.v7.widget.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import com.bumptech.glide.*;
import java.io.*;
import java.util.*;
import com.jefferson.application.br.util.JDebug;
import com.jefferson.application.br.model.MediaModel;

public class MultiSelectRecyclerViewAdapter extends SelectableAdapter<MultiSelectRecyclerViewAdapter.ViewHolder> {

    public ArrayList<MediaModel> mListItemsModels;
    private static Context context;
    private ViewHolder.ClickListener clickListener;
	private int mediaType;
    
    public MultiSelectRecyclerViewAdapter(Context context, ArrayList<MediaModel> arrayList, ViewHolder.ClickListener clickListener, int mediaType) {

        this.mListItemsModels = arrayList;
        this.context = context;
        this.clickListener = clickListener;
		this.mediaType = mediaType;

    }

    public ArrayList<String> getSelectedItemsPath() {
        ArrayList<String> selectedItemsPath = new ArrayList<>();
    
        for (int position : getSelectedItems()) {
           selectedItemsPath.add(mListItemsModels.get(position).getPath());
        }
        
        return selectedItemsPath; 
    }
   
    public ArrayList getListItemsPath() {
        ArrayList<String> arrayListPath = new ArrayList<>();

        for (MediaModel mm : mListItemsModels) {
            arrayListPath.add(mm.getPath());
        }

        return arrayListPath;
    }

    public void updateItemDuration(String path, String time) {

        for (int i = 0; i < mListItemsModels.size(); i++) {
            MediaModel model = mListItemsModels.get(i);
            if (model.getPath().equals(path)) {
                model.setDuration(time);
                notifyItemChanged(i);
                break;
            }
        }
    }

	public void removeAll(ArrayList<String> paths) {

        if (paths == null) {
            return;
        }
        
        ArrayList <MediaModel > deletetionList = new ArrayList<>();
        Iterator<MediaModel> iterator = mListItemsModels.iterator();
        
        while (iterator.hasNext()) { 
            MediaModel item = iterator.next(); 

            if (paths.contains(item.getPath())) { 
                deletetionList.add(item);
            } 
        }
        
        mListItemsModels.removeAll(deletetionList);
		notifyDataSetChanged();
	}

    public void removeItem(String path) {

        if (path == null) {
            return;
        }
        
        Iterator<MediaModel> iterator = mListItemsModels.iterator();
        MediaModel item = null;
        
        while (iterator.hasNext()) { 
            item = iterator.next(); 
            
            if (item.getPath().equals(path)) { 
                break;
            }
        }
        
        removeItem(item);
    }

	public void removeItem(MediaModel item) {
        
        if (item == null) {
            return;
        }
        
		int index = mListItemsModels.indexOf(item);

        if (index != -1) {
			mListItemsModels.remove(index);
			notifyItemRemoved(index);
		}
	}

    @Override
    public MultiSelectRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.generic_gridview_item, null);
        ViewHolder viewHolder = new ViewHolder(itemLayoutView, clickListener);

        if (mediaType == 1) {
			//viewHolder.playView.setVisibility(View.VISIBLE);
		}
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {

		viewHolder.selectedOverlay.setVisibility(isSelected(position) ? View.VISIBLE : View.INVISIBLE);
		Glide.with(App.getAppContext()).load("file://" + mListItemsModels.get(position).getPath()).skipMemoryCache(true).into(viewHolder.imageView);
        String result = mListItemsModels.get(position).getDuration();

        if (result != null) {
            viewHolder.timeView.setVisibility(View.VISIBLE);
            viewHolder.textView.setText(result);
            //Debug.toast("Result => " + result);
        } else {

        }

 	}

    @Override
    public int getItemCount() {
        return mListItemsModels.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener, View.OnLongClickListener {

        public ImageView imageView;
        private ClickListener listener;
        private final View selectedOverlay;
        public TextView textView;
		public ImageView smallView;
        private View timeView;

        public ViewHolder(View rootView, ClickListener listener) {
            super(rootView);

            this.listener = listener;
		    imageView = (ImageView) rootView.findViewById(R.id.image);
			smallView = (ImageView) rootView.findViewById(R.id.folder_small_icon_view);
            textView = rootView.findViewById(R.id.gridview_itemTextView);
            selectedOverlay = itemView.findViewById(R.id.selected_overlay);
		    timeView = rootView.findViewById(R.id.timeViewLayout);
            rootView.setOnClickListener(this);
            rootView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {

            if (listener != null) {
                listener.onItemClicked(getAdapterPosition());
            }
        }

        @Override
        public boolean onLongClick(View view) {

            if (listener != null) {
                return listener.onItemLongClicked(getAdapterPosition());
            }
            return false;
        }

        public interface ClickListener {

            public void onItemClicked(int position);

            public boolean onItemLongClicked(int position);
        }
	}
}

