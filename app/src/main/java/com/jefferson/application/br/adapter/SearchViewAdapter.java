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

package com.jefferson.application.br.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.jefferson.application.br.R;
import com.jefferson.application.br.model.FolderModel;
import com.jefferson.application.br.model.SimplifiedAlbum;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

public class SearchViewAdapter extends BaseAdapter {

    private final LayoutInflater inflater;
    private final ArrayList<SimplifiedAlbum> items;
    private final Context context;
    private final ArrayList<SimplifiedAlbum> filteredItems;

    public SearchViewAdapter(ArrayList<SimplifiedAlbum> items, Context context) {
        this.items = items;
        this.context = context;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.filteredItems = new ArrayList<>();
        //when unfiltered, shows all items in alphabetical order.
        sort(items);
        filteredItems.addAll(items);
    }

    public void sort(ArrayList<SimplifiedAlbum> albums) {
        Collections.sort(albums, new Comparator<SimplifiedAlbum>() {

            @Override
            public int compare(SimplifiedAlbum o1, SimplifiedAlbum o2) {
                return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
            }
        });
    }
    @Override
    public int getCount() {
        return filteredItems.size();
    }

    @Override
    public SimplifiedAlbum getItem(int position) {
        return filteredItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder = new Holder();
        SimplifiedAlbum item = getItem(position);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.search_item_layout, parent, false);
            holder.textView = convertView.findViewById(R.id.search_item_text_view);
            holder.thumbView = convertView.findViewById(R.id.thumb_view);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }
        holder.textView.setText(item.getName());
        Glide.with(context).load(item.getThumbPath()).into(holder.thumbView);
        return convertView;
    }

    public void filter(String keyword) {
        filteredItems.clear();
        if (keyword.isEmpty()) {
            filteredItems.addAll(items);
        } else {
            for (SimplifiedAlbum item : items) {
                if (item.getName().toLowerCase().startsWith(keyword.toLowerCase())) {
                    filteredItems.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    private static class Holder {
        ImageView thumbView;
        TextView textView;
    }
}
