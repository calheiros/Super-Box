package com.jefferson.application.br;
import android.content.*;
import android.view.*;
import android.widget.*;
import java.util.*;

public class ContactsDetailsAdapter extends BaseAdapter {
	
	List<String> listItens;
	LayoutInflater layoutInflater;
	
	public ContactsDetailsAdapter(Context context, List<String> listItens) {
		this.listItens = listItens;
		layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
	}
	@Override
	public int getCount() {
		return listItens.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		if (convertView == null)
			convertView = layoutInflater.inflate(R.layout.contact_details_items, null);
			
		TextView text=(TextView)convertView.findViewById(R.id.contact_details_name);
		text.setText(listItens.get(position));

		return convertView;
	}

}
