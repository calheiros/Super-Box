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
import com.jefferson.application.br.util.Debug;
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

    public ArrayList getArrayListPath() {
        ArrayList<String> arrayListPath = new ArrayList<>();
        
        for (MediaModel mm : mListItemsModels){
            arrayListPath.add(mm.getPath());
        }
        
        return arrayListPath;
    }

    public void updateItemDuration(String path, String time) {

        for (MediaModel model: mListItemsModels) {
            if (model.getPath().equals(path)) {
                int index = mListItemsModels.indexOf(model);
                model.setDuration(time);
                notifyItemChanged(index);
            }
        }
    }

	public void removeAll(ArrayList<String> paths) {

        if (paths == null) {
            return;
        }
        
        ArrayList<MediaModel> removeList = new ArrayList<>();
        for (MediaModel mm: mListItemsModels) {
            if (paths.contains(mm.getPath())) {
                 removeList.add(mm);
            }
        }
        
        mListItemsModels.removeAll(removeList);
		notifyDataSetChanged();
	}

    public void removeItem(String path) {

        if (path == null) {
            return;
        }
        
        MediaModel model = null;
        int i;
        
        for ( i = (mListItemsModels.size() - 1); i >= 0; i--) {
           
            if (path.equals(mListItemsModels.get(i).getPath())) {
                model = mListItemsModels.get(i);
                Log.i("MultSelectRecyclerView", "REMOVED " + i);
                break;
            }
        }
        
        if (model != null) {
            mListItemsModels.remove(model);
            notifyItemRemoved(i);
        }
    }

	public void removeItem(FileModel item) {
		int index = mListItemsModels.indexOf(item);

        if (index != -1) {
			mListItemsModels.remove(index);
			notifyItemRemoved(index);
		}
	}

    @Override
    public MultiSelectRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.gridview_item, null);
        ViewHolder viewHolder = new ViewHolder(itemLayoutView, clickListener);

        if (mediaType == 1) {
			viewHolder.playView.setVisibility(View.VISIBLE);
		}
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {

		viewHolder.selectedOverlay.setVisibility(isSelected(position) ? View.VISIBLE : View.INVISIBLE);
		Glide.with(App.getAppContext()).load("file://" + mListItemsModels.get(position).getPath()).into(viewHolder.imageView);
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
		public ImageView playView;
        private View timeView;

        public ViewHolder(View rootView, ClickListener listener) {
            super(rootView);

            this.listener = listener;
		    imageView = (ImageView) rootView.findViewById(R.id.image);
			playView = (ImageView) rootView.findViewById(R.id.play_view);
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

