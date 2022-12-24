package com.jefferson.application.br.adapter;

import android.content.Context;
import android.content.res.Resources;
import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.jefferson.application.br.R;
import com.jefferson.application.br.model.PickerModel;
import java.util.List;

public class FilePickerAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater mLayoutInflater;
    public List<PickerModel> models;
    private int selectedItem = -1;
    private int itemType;
    
    public FilePickerAdapter(List<PickerModel> list, Context context, int itemType) {
        this.context = context;
        this.models = list;
        this.itemType = itemType;
        this.mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            
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
            view = mLayoutInflater.inflate(R.layout.file_picker_item, viewGroup, false);
        }
        ImageView imageView = view.findViewById(R.id.tumbView);
        TextView textView = view.findViewById(R.id.item_name);
        TextView textView2 = view.findViewById(R.id.item_size);
        View overlay = view.findViewById(R.id.folder_picker_check_overlay);
        PickerModel pickerModel = models.get(position);
        boolean selected = position == getSelectedItem();
        
        int color = selected ? ContextCompat.getColor(context, R.color.colorAccent) : getAttrCommonColor();
        int size = pickerModel.getSize();
        int resId = itemType == 1? R.plurals.video_total_plural: R.plurals.imagem_total_plural;
        String folderSize = context.getResources().getQuantityString(resId, size, size);
        
        overlay.setVisibility(selected ? View.VISIBLE : View.GONE);
        textView.setTextColor(color);
        textView.setText(pickerModel.getName());
        textView2.setText(String.valueOf(folderSize));
        Glide.with(context).load("file://" + pickerModel.getTumbPath()).centerCrop().into(imageView);
        return view;
    }

    private int getAttrCommonColor() {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(R.attr.commonColorLight, typedValue, true);
        return typedValue.data; 
    }
}
