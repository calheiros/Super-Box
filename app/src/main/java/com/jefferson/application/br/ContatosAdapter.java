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

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

public class ContatosAdapter extends BaseAdapter
{  
    ArrayList<ContactsData> selecionados;
	ArrayList<ContactsData> contacts;
	LayoutInflater inflater;
    
    public ContatosAdapter(ArrayList<ContactsData> data, Activity mActivity)
	{
		this.contacts = data;
		selecionados = new ArrayList<ContactsData>();
		inflater = (LayoutInflater) mActivity
			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	}
	@Override
	public int getCount()
	{

		return contacts.size();
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

	@Override
	public View getView(final int position, View convertView, ViewGroup parent)
	{
		View vi = convertView;
		if (convertView == null)
			vi = inflater.inflate(R.layout.list_contatos_itens, null);
		TextView nome = (TextView)vi.findViewById(R.id.contatos_nome);
		TextView numero = (TextView)vi.findViewById(R.id.contatos_numero);
		ImageView myImageView = (ImageView)vi.findViewById(R.id.imagePhoto);
		CheckBox checkW = (CheckBox)vi.findViewById(R.id.check_contacts);

		ContactsData myContact = contacts.get(position);
		nome.setText(myContact.name);
		numero.setText(myContact.phoneNumber);


       if (myContact.photo == null){
			myImageView.setImageResource(R.drawable.ic_photo_contact_null);
        } else {
			myImageView.setImageBitmap(BitmapFactory.decodeStream(new ByteArrayInputStream(myContact.photo)));

		} if (selecionados.contains(myContact)) {
			checkW.setChecked(true);
		} else {
			checkW.setChecked(false);
	
          }
		return vi;
	}

}
