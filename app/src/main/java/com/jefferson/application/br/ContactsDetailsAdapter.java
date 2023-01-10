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
