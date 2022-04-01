package com.jefferson.application.br.adapter;

import android.content.*;
import android.graphics.*;
import android.view.*;
import android.widget.*;
import com.bumptech.glide.*;
import com.jefferson.application.br.*;
import com.jefferson.application.br.model.*;
import java.util.*;
import android.support.v4.content.ContextCompat;

public class FilePickerAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater mLayoutInflater;
    public List<PickerModel> models;
    private int selectedItem = -1;

    public FilePickerAdapter(List<PickerModel> list, Context context) {
        this.context = context;
        this.models = list;
        this.mLayoutInflater = (LayoutInflater) context.getSystemService("layout_inflater");
    }

    public void update(List<PickerModel> list) {
        this.models = list;
        notifyDataSetChanged();
    }

    public int getSelectedItem() {
        return this.selectedItem;
    }

    public void setSelectedItem(int i) {
        this.selectedItem = i;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return this.models.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return (long) i;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = mLayoutInflater.inflate(R.layout.file_picker_item, (ViewGroup) null);
        }
    
        ImageView imageView = (ImageView) view.findViewById(R.id.tumbView);
        TextView textView = (TextView) view.findViewById(R.id.item_name);
        TextView textView2 = (TextView) view.findViewById(R.id.item_size);
        View overlay = view.findViewById(R.id.layoutOverlay);
        PickerModel pickerModel = models.get(position);
        int color = position == getSelectedItem()? R.color.colorAccent : R.color.item_normal;
        
        textView.setTextColor(ContextCompat.getColor(context, color));
        overlay.setVisibility(position == selectedItem ? View.VISIBLE : View.GONE);
        textView.setText(pickerModel.getName());
        textView2.setText(String.valueOf(pickerModel.getSize()));
        Glide.with(context).load("file://" + pickerModel.getTumbPath()).centerCrop().into(imageView);
        return view;
    }
}
