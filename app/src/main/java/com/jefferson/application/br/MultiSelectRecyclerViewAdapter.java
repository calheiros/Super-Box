/*
 * Copyright (C) 2023 Jefferson Calheiros


 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jefferson.application.br;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.jefferson.application.br.model.MediaModel;

import java.util.ArrayList;
import java.util.Iterator;

public class MultiSelectRecyclerViewAdapter extends SelectableAdapter<MultiSelectRecyclerViewAdapter.ViewHolder> {

    private final Context context;
    public ArrayList<MediaModel> items;
    private final ViewHolder.ClickListener clickListener;
    private final int mediaType;

    public MultiSelectRecyclerViewAdapter(Context context, ArrayList<MediaModel> arrayList,
                                          ViewHolder.ClickListener clickListener, int mediaType) {
        this.items = arrayList;
        this.context = context;
        this.clickListener = clickListener;
        this.mediaType = mediaType;

    }
    public void toggleItemSelected(int position) {
        toggleSelection(position);
        for(int i: getSelectedItemsHash().keySet()) {
            if (i != position) {
                notifyItemChanged(i);
            }
        }
    }

    private Object getItem(int position) {
        return items.get(position);
    }

    public ArrayList<String> getSelectedItemsPath() {
        ArrayList<String> selectedItemsPath = new ArrayList<>();
        for (int position : getSelectedItems()) {
            selectedItemsPath.add(items.get(position).getPath());
        }
        return selectedItemsPath;
    }

    public ArrayList<String> getListItemsPath() {
        ArrayList<String> arrayListPath = new ArrayList<>();
        for (MediaModel mm : items) {
            arrayListPath.add(mm.getPath());
        }
        return arrayListPath;
    }

    public void updateItemDuration(String path, String time) {
        for (int i = 0; i < items.size(); i++) {
            MediaModel model = items.get(i);
            if (model.getPath().equals(path)) {
                model.setDuration(time);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void removeAll(@NonNull ArrayList<String> paths) {
        ArrayList<MediaModel> deletionList = new ArrayList<>();

        for (MediaModel item : items) {
            if (paths.contains(item.getPath())) {
                deletionList.add(item);
            }
        }
        items.removeAll(deletionList);
        notifyDataSetChanged();
    }

    public void removeItem(@NonNull String path) {
        Iterator<MediaModel> iterator = items.iterator();
        MediaModel item = null;

        while (iterator.hasNext()) {
            item = iterator.next();
            if (item.getPath().equals(path)) {
                break;
            }
        }
        if (item != null) removeItem(item);
    }

    public void removeItem(@NonNull MediaModel item) {
        int index = items.indexOf(item);

        if (index != -1) {
            items.remove(index);
            notifyItemRemoved(index);
        }
    }

    @NonNull @Override
    public MultiSelectRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.generic_gridview_item,
                parent, false);
        return new ViewHolder(itemLayoutView, clickListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        String mediaDuration = items.get(position).getDuration();
        Glide.with(App.getAppContext()).load("file://" + items.get(position).getPath()).
                skipMemoryCache(false).dontAnimate().into(viewHolder.imageView);

        if (mediaDuration != null) {
            viewHolder.durationLabel.setVisibility(View.VISIBLE);
            viewHolder.durationLabel.setText(mediaDuration);
        }

        boolean isSelected = isSelected(position);
        viewHolder.selectionModeOverlay.setVisibility(isSelected ? View.VISIBLE : View.INVISIBLE);

        if (isSelected) {
            viewHolder.selectedCountLabel.setText(String.valueOf(getSelectedItemPosition(position) + 1));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private final View selectionModeOverlay;
        private final TextView selectedCountLabel;
        public ImageView imageView;
        public TextView durationLabel;
        public ImageView smallView;
        private final ClickListener listener;

        public ViewHolder(View rootView, ClickListener listener) {
            super(rootView);
            this.listener = listener;
            imageView = (ImageView) rootView.findViewById(R.id.image);
            smallView = (ImageView) rootView.findViewById(R.id.folder_small_icon_view);
            durationLabel = rootView.findViewById(R.id.gridview_itemTextView);
            selectedCountLabel = rootView.findViewById(R.id.selected_item_count_label);
            selectionModeOverlay = rootView.findViewById(R.id.item_selected_overlay);

            rootView.setOnClickListener(this);
            rootView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onItemClicked(getAdapterPosition(), v);
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
            void onItemClicked(int position, View v);
            boolean onItemLongClicked(int position);
        }
    }
}

