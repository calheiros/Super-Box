package com.jefferson.application.br.adapter;

import android.support.v7.widget.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import com.jefferson.application.br.*;
import java.util.*;
import com.jefferson.application.br.widget.*;

public class ViewAlbumAdapter extends  RecyclerView.Adapter<ViewAlbumAdapter.ListItemViewHolder> {

    private List<String> items;
    private SparseBooleanArray selectedItems;

	public ViewAlbumAdapter(List<String> modelData) {
        if (modelData == null) {
            throw new IllegalArgumentException("modelData must not be null");
        }
        items = modelData;
        selectedItems = new SparseBooleanArray();
    }

	public void removeItem(int position) {
		items.remove(position);
		notifyItemRemoved(position);
	}

    @Override
    public ListItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.
					from(viewGroup.getContext()).
							inflate(R.layout.generic_gridview_item, viewGroup, false);
        return new ListItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ListItemViewHolder viewHolder, int position) {
        String model = items.get(position);
        //viewHolder.itemView.setActivated(selectedItems.get(position, false));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void toggleSelection(int pos) {
        if (selectedItems.get(pos, false)) {
            selectedItems.delete(pos);
        } else {
            selectedItems.put(pos, true);
        }
        notifyItemChanged(pos);
    }

    public void clearSelections() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<Integer>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); i++) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }

    public final static class ListItemViewHolder extends RecyclerView.ViewHolder {

		public ImageView image;
		public TextView fname;
		public TextView fsize;

        public ListItemViewHolder(View view) {
            super(view);
            image = (ImageView) view.findViewById(R.id.image);
			fname = (TextView) view.findViewById(R.id.tv_folder);
			fsize = (TextView) view.findViewById(R.id.tv_folder2);
        }
    }
}
