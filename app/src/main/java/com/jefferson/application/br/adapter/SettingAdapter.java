package com.jefferson.application.br.adapter;

import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.jefferson.application.br.R;
import com.jefferson.application.br.fragment.SettingFragment;
import com.jefferson.application.br.model.PreferenceItem;
import java.util.ArrayList;
import com.jefferson.application.br.util.MyAnimationUtils;
import android.content.Intent;
import com.jefferson.application.br.activity.CalculatorActivity;

public class SettingAdapter extends BaseAdapter {

    public LayoutInflater inflater;
    public ArrayList<PreferenceItem> items;
	public SettingFragment settingFragment;

    public SettingAdapter(ArrayList<PreferenceItem> arrayList, SettingFragment fragment) {

		this.settingFragment = fragment;
        this.items = arrayList;
        this.inflater = (LayoutInflater) fragment.getActivity().getSystemService("layout_inflater");
    }

    public ArrayList<PreferenceItem> getPreferenceItems() {
        return items;
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
            Switch mySwitch = view.findViewById(R.id.my_switch);
            TextView descriptionText = view.findViewById(R.id.description_text_view);
            iconView.setImageResource(preferenceItem.icon_id);
            mySwitch.setChecked(settingFragment.getComponentEnabledSetting() == PackageManager.COMPONENT_ENABLED_STATE_DISABLED);

            if (preferenceItem.id == PreferenceItem.ID.APP_ICON) {
                View expandableLayout = inflater.inflate(R.layout.stealth_expandable_layout, null);
                ((ViewGroup) view).addView(expandableLayout);
                if (mySwitch.isChecked()) {
                    expandableLayout.setVisibility(View.VISIBLE);
                }
                setExpandableLayoutListener(view);
            }

            if (preferenceItem.description == null) {
                descriptionText.setVisibility(View.GONE);
            } else {
                descriptionText.setText(preferenceItem.description);
            }
            titleView.setText(preferenceItem.title);
            return view; 
        } 

        if (preferenceItem.type == PreferenceItem.SECTION_TYPE) {
            view = inflater.inflate(R.layout.preference_section_item, (ViewGroup) null);
            ((TextView) view.findViewById(R.id.title_view)).setText(preferenceItem.title);
            return view;
        } 

        if (preferenceItem.type == preferenceItem.ITEM_TYPE) {
            view = inflater.inflate(R.layout.preference_common_item, (ViewGroup) null);

            TextView descriptionText = view.findViewById(R.id.description_text_view);
            ImageView iconView = view.findViewById(R.id.ic_view);
            ((TextView) view.findViewById(R.id.item_title)).setText(preferenceItem.title);
            iconView.setImageResource(preferenceItem.icon_id);

            if (preferenceItem.description != null) {
                descriptionText.setVisibility(View.VISIBLE);
                descriptionText.setText(preferenceItem.description);
            }
            return view;
        }
        return null;

    }

    private void setExpandableLayoutListener(View v) {
        final View expandableLayout = v.findViewById(R.id.steal_thexpandable_layout);
        View calculator = v.findViewById(R.id.steal_calculator_layout);
        calculator.setOnClickListener(new OnClickListener(){

                @Override
                public void onClick(View view) {
                    startCalculatorActivity();
                    //Toast.makeText(settingFragment.getContext(), "clicked!", Toast.LENGTH_SHORT).show();
                }
            }
        );
        
        v.setOnClickListener(new OnClickListener() {
                @Override 
                public void onClick(View v) {
                    Switch mySwitch = v.findViewById(R.id.my_switch);
                    boolean checked = mySwitch.isChecked();

                    if (checked) {
                        MyAnimationUtils.collapse(expandableLayout);
                    } else {
                        MyAnimationUtils.expand(expandableLayout);
                    }
                    mySwitch.setChecked(!checked);
                } 
            }
        );
    }
    
    private void startCalculatorActivity() {
        Intent intent = new Intent(settingFragment.getActivity(), CalculatorActivity.class);
        intent.setAction(CalculatorActivity.ACTION_CREATE_CODE);
        settingFragment.startActivityForResult(intent, SettingFragment.CALCULATOR_CREATE_CODE_RESULT);
    }
}
