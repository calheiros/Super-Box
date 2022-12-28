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
import com.jefferson.application.br.model.SimplifiedAlbum;

import java.util.ArrayList;

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
        if (!keyword.isEmpty())
            for (SimplifiedAlbum item : items) {
                if (item.getName().toLowerCase().startsWith(keyword.toLowerCase())) {
                    filteredItems.add(item);
                }
            }
        notifyDataSetChanged();
    }

    private static class Holder {
        ImageView thumbView;
        TextView textView;
    }
}
