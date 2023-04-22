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
package com.jefferson.application.br.adapter

import android.app.Activity
import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import com.jefferson.application.br.model.ContactsData
import com.jefferson.application.br.R
import java.io.ByteArrayInputStream

class ContactsAdapter(private var contacts: ArrayList<ContactsData>, mActivity: Activity) : BaseAdapter() {
    @JvmField
	var selection: ArrayList<ContactsData> = ArrayList()
    var inflater: LayoutInflater

    init {
        inflater = mActivity
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getCount(): Int {
        return contacts.size
    }

    override fun getItem(position: Int): Any {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var vi = convertView
        if (convertView == null)
            vi = inflater.inflate(R.layout.list_contatos_itens, parent, false)
        vi ?: return null
        val nameLabel = vi.findViewById<View>(R.id.contatos_nome) as TextView
        val phoneLabel = vi.findViewById<View>(R.id.contatos_numero) as TextView
        val contactPhotoView = vi.findViewById<View>(R.id.imagePhoto) as ImageView
        val mCheckBox = vi.findViewById<View>(R.id.check_contacts) as CheckBox
        val contact = contacts[position]
        nameLabel.text = contact.name
        phoneLabel.text = contact.phoneNumber
        if (contact.photo == null) {
            contactPhotoView.setImageResource(R.drawable.ic_photo_contact_null)
        } else {
            contactPhotoView.setImageBitmap(BitmapFactory.decodeStream(ByteArrayInputStream(contact.photo)))
        }
        mCheckBox.isChecked = selection.contains(contact)
        return vi
    }
}