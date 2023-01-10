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

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.view.*;
import android.widget.*;
import com.jefferson.application.br.*;
import java.io.*;
import java.util.*;

import com.jefferson.application.br.R;

public class PrivateContactsAdapter extends BaseAdapter
{
	List<Contact> contatos;
	Activity activity;
	LayoutInflater inflater;
	
	public PrivateContactsAdapter(List<Contact> contatos, Activity activity)
	{
      this.activity = activity;
	  this.contatos = contatos;
	  
		inflater = (LayoutInflater) activity
			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
	} 
	@Override
	public int getCount()
	{
		return contatos.size();
	}

	@Override
	public Object getItem(int position)
	{
		return position;
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	public View getView(final int position, View convertView, ViewGroup parent)
	{

		View vi = convertView;
		if (convertView == null)
			vi = inflater.inflate(R.layout.private_contacts_items, null);
		TextView nome = (TextView)vi.findViewById(R.id.contatos_nome);
		
		/*CircleImageView photo = (CircleImageView)vi.findViewById(R.id.imagePhoto);
		
		nome.setText(contatos.get(position)._name);
		
		if(contatos.get(position)._phone_photo == null)
        photo.setImageResource(R.drawable.ic_photo_contact_null);
		else
			photo.setImageBitmap(BitmapFactory.decodeStream(new ByteArrayInputStream(contatos.get(position)._phone_photo)));
		*/
       
		return vi;
	}

}
