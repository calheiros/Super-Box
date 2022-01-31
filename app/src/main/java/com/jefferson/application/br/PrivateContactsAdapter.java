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
