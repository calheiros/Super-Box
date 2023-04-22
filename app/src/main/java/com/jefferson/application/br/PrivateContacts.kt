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

import android.annotation.SuppressLint
import android.content.ContentProviderOperation
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jefferson.application.br.ContactsActivity
import com.jefferson.application.br.database.DatabaseHandler
import com.jefferson.application.br.model.ContactsData

class PrivateContacts : AppCompatActivity() {
    var lista: ListView? = null
    var adapter: PrivateContactsAdapter? = null
    var database: DatabaseHandler? = null
    var allContact: List<Contact>? = null
    var Contact: Contact? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.private_contacts_listview)
        lista = findViewById<View>(R.id.listcontatosListView1) as ListView
        database = DatabaseHandler(this)
        allContact = database!!.allContacts
        adapter = PrivateContactsAdapter(allContact!!, this)
        lista!!.adapter = adapter
        lista!!.onItemClickListener = OnItemClickListener { p1, p2, position, p4 ->
            Contact = allContact!![position]
            val i = Intent(applicationContext, ContactDetails::class.java)
            i.putExtra("nome", Contact!!._name)
            i.putExtra("photo", Contact!!._phone_photo)
            startActivity(i)
        }
    }

    fun addContact(name: String?, phone: String?, photo: ByteArray?) {
        val ops = ArrayList<ContentProviderOperation>()
        ops.add(
            ContentProviderOperation.newInsert(
                ContactsContract.RawContacts.CONTENT_URI
            )
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build()
        )

        //------------------------------------------------------ Names
        if (name != null) {
            ops.add(
                ContentProviderOperation.newInsert(
                    ContactsContract.Data.CONTENT_URI
                )
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
                    )
                    .withValue(
                        ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                        name
                    ).build()
            )
        }

        //------------------------------------------------------ Mobile Number                     
        if (phone != null) {
            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                    )
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
                    .withValue(
                        ContactsContract.CommonDataKinds.Phone.TYPE,
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
                    )
                    .build()
            )
        }
        if (photo != null) {
            ops.add(
                ContentProviderOperation
                    .newInsert(
                        ContactsContract.Data.CONTENT_URI
                    )
                    .withValue(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
                    )
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.CommonDataKinds.Photo.DATA15, photo).build()
            )
        }
        //------------------------------------------------------ Home Numbers

        // Asking the Contact provider to create a new contact                 
        try {
            contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Exception: " + e.message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_confirm, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            0 -> {
                val intent = Intent(this, ContactsActivity::class.java)
                startActivityForResult(intent, 0)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            for (contData in selecionados!!) {
                database!!.addContact(Contact(contData.name, contData.phoneNumber, contData.photo))
                try {
                    deleteContact(this, contData.phoneNumber, contData.name)
                } catch (e: Exception) {
                }
            }
            recreate()
        } else {
            Toast.makeText(this, "RESULT_CANCELED", Toast.LENGTH_LONG).show()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        var selecionados: ArrayList<ContactsData>? = null
        @SuppressLint("Range")
        fun deleteContact(ctx: Context, phone: String?, name: String?): Boolean {
            val contactUri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phone)
            )
            val cur = ctx.contentResolver.query(contactUri, null, null, null, null)
            try {
                if (cur!!.moveToFirst()) {
                    do {
                        if (name.equals(
                                cur.getString(cur.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)),
                                ignoreCase = true
                            )
                        ) {
                            val lookupKey =
                                cur.getString(cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY))
                            val uri = Uri.withAppendedPath(
                                ContactsContract.Contacts.CONTENT_LOOKUP_URI,
                                lookupKey
                            )
                            ctx.contentResolver.delete(uri, null, null)
                            return true
                        }
                    } while (cur.moveToNext())
                }
            } catch (e: Exception) {
                println(e.stackTrace)
            }
            return false
        }
    }
}