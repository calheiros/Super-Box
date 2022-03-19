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

public class MultiSelectRecyclerViewAdapter extends SelectableAdapter<MultiSelectRecyclerViewAdapter.ViewHolder> {

    public ArrayList<String> mListItemsPath;
    public ArrayList<Integer> duration;
    private static Context context;
    private ViewHolder.ClickListener clickListener;
	private int mediaType;
	private HashMap<String, String> map;
    
    public MultiSelectRecyclerViewAdapter(Context context, ArrayList<String> arrayList, ViewHolder.ClickListener clickListener, int mediaType) {
        
        this.mListItemsPath = arrayList;
        this.context = context;
        this.clickListener = clickListener;
		this.mediaType = mediaType;
        this.map = new HashMap<>();
    }

    public void setMediaDuration(HashMap<String, String> map) {
        this.map = map;
        notifyDataSetChanged();
    }
    
	public void removeAll(ArrayList<String> list) {
	
		this.mListItemsPath.removeAll(list);
		notifyDataSetChanged();
	}

	public void removeItem(Object item) {

		int index = mListItemsPath.indexOf(item);
		if (index != -1) {
			mListItemsPath.remove(index);
			notifyItemRemoved(index);
		}
	}

    @Override
    public MultiSelectRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.gridview_item, null);
        ViewHolder viewHolder = new ViewHolder(itemLayoutView, clickListener);
		if (mediaType == 1)
			viewHolder.playView.setVisibility(View.VISIBLE);
		
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {

		viewHolder.selectedOverlay.setVisibility(isSelected(position) ? View.VISIBLE : View.INVISIBLE);
		Glide.with(context).load("file://" + mListItemsPath.get(position)).skipMemoryCache(true).into(viewHolder.imageView);
        String result = map.get(mListItemsPath.get(position));
        
        if (result != null) {
            viewHolder.textView.setVisibility(View.VISIBLE);
            viewHolder.textView.setText(result);
            
            Debug.msg("Result => " + result);
        } else {
            
        }
        
 	}

    @Override
    public int getItemCount() {
        return mListItemsPath.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener,View.OnLongClickListener {

        public ImageView imageView;
        private ClickListener listener;
        private final View selectedOverlay;
        public TextView textView;
		public ImageView playView;
	
        public ViewHolder(View rootView, ClickListener listener) {
            super(rootView);

            this.listener = listener;
		
            imageView = (ImageView) rootView.findViewById(R.id.image);
			playView = (ImageView) rootView.findViewById(R.id.play_view);
            textView = rootView.findViewById(R.id.gridview_itemTextView);
            selectedOverlay = itemView.findViewById(R.id.selected_overlay);
		
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

