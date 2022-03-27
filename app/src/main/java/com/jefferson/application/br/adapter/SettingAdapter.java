package com.jefferson.application.br.adapter;

import android.app.*;
import android.view.*;
import android.widget.*;
import com.jefferson.application.br.*;
import com.jefferson.application.br.model.*;
import java.util.*;
import com.jefferson.application.br.fragment.*;
import android.content.pm.*;
import android.graphics.Color;

public class SettingAdapter extends BaseAdapter {

    public LayoutInflater inflater;
    public ArrayList<PreferenceItem> items;
	public SettingFragment mSettingFragmemt;

    public SettingAdapter(ArrayList<PreferenceItem> arrayList, SettingFragment fragment) {

		this.mSettingFragmemt = fragment;
        this.items = arrayList;
        this.inflater = (LayoutInflater) fragment.getActivity().getSystemService("layout_inflater");
    }

    @Override
    public int getCount() {

        return this.items.size();
    }

    @Override
    public PreferenceItem getItem(int id) {

        return this.items.get(id);
    }

    @Override
    public long getItemId(int id) {

        return (long) id;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        PreferenceItem preferenceItem = this.items.get(i);

        if (preferenceItem.type == PreferenceItem.ITEM_SWITCH_TYPE) {
            
            view = inflater.inflate(R.layout.preference_switch_item, (ViewGroup) null);
            TextView titleView = view.findViewById(R.id.title_view);
            ImageView iconView =  view.findViewById(R.id.ic_view);
            ((Switch) view.findViewById(R.id.my_switch)).setChecked(mSettingFragmemt.getComponentEnabledSetting() == PackageManager.COMPONENT_ENABLED_STATE_DISABLED);
            TextView descriptionText = view.findViewById(R.id.description_text_view);
            iconView.setImageResource(preferenceItem.icon_id);

            if (preferenceItem.description == null) {
                descriptionText.setVisibility(View.GONE);
            } else {
                descriptionText.setText(preferenceItem.description);
            }
            titleView.setText(preferenceItem.item_name);
            return view; 
        } 
        
        if (preferenceItem.type == PreferenceItem.SECTION_TYPE) {
            
            view = inflater.inflate(R.layout.preference_section_item, (ViewGroup) null);
            ((TextView) view.findViewById(R.id.title_view)).setText(preferenceItem.item_name);
            return view;
        } 
        
        if (preferenceItem.type == preferenceItem.ITEM_TYPE) {
            
            view = inflater.inflate(R.layout.preference_common_item, (ViewGroup) null);
            TextView descriptionText = (TextView) view.findViewById(R.id.description_text_view);
            ImageView iconView = (ImageView) view.findViewById(R.id.ic_view);
            ((TextView) view.findViewById(R.id.item_title)).setText(preferenceItem.item_name);
            iconView.setImageResource(preferenceItem.icon_id);
            if (preferenceItem.description != null) {
                descriptionText.setVisibility(View.VISIBLE);
                descriptionText.setText(preferenceItem.description);
            }
            return view;
        }
        return null;
    }

}
