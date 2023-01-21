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

    private static Context context;
    public ArrayList<MediaModel> mListItemsModels;
    private final ViewHolder.ClickListener clickListener;
    private final int mediaType;

    public MultiSelectRecyclerViewAdapter(Context context, ArrayList<MediaModel> arrayList, ViewHolder.ClickListener clickListener, int mediaType) {

        this.mListItemsModels = arrayList;
        MultiSelectRecyclerViewAdapter.context = context;
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

    public ArrayList<String> getListItemsPath() {
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

    public void removeAll(@NonNull ArrayList<String> paths) {
        ArrayList<MediaModel> deletionList = new ArrayList<>();

        for (MediaModel item : mListItemsModels) {
            if (paths.contains(item.getPath())) {
                deletionList.add(item);
            }
        }

        mListItemsModels.removeAll(deletionList);
        notifyDataSetChanged();
    }

    public void removeItem(@NonNull String path) {
        Iterator<MediaModel> iterator = mListItemsModels.iterator();
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
        int index = mListItemsModels.indexOf(item);

        if (index != -1) {
            mListItemsModels.remove(index);
            notifyItemRemoved(index);
        }
    }

    @NonNull
    @Override
    public MultiSelectRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.generic_gridview_item,  parent, false);
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
        } else {

        }

    }

    @Override
    public int getItemCount() {
        return mListItemsModels.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private final View selectedOverlay;
        public ImageView imageView;
        public TextView textView;
        public ImageView smallView;
        private final ClickListener listener;
        private final View timeView;

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

