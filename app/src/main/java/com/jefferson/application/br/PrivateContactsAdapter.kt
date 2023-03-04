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
package com.jefferson.application.br

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class PrivateContactsAdapter(var contatos: List<Contact>, var activity: Activity) : BaseAdapter() {
    var inflater: LayoutInflater

    init {
        inflater = activity
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getCount(): Int {
        return contatos.size
    }

    override fun getItem(position: Int): Any {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
        var vi = convertView
        val nome = vi.findViewById<View>(R.id.contatos_nome) as TextView

        /*CircleImageView photo = (CircleImageView)vi.findViewById(R.id.imagePhoto);
		
		nome.setText(contatos.get(position)._name);
		
		if(contatos.get(position)._phone_photo == null)
        photo.setImageResource(R.drawable.ic_photo_contact_null);
		else
			photo.setImageBitmap(BitmapFactory.decodeStream(new ByteArrayInputStream(contatos.get(position)._phone_photo)));
		*/return vi
    }
}