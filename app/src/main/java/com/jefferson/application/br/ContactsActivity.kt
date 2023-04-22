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
import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.CheckBox
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jefferson.application.br.adapter.ContactsAdapter
import com.jefferson.application.br.database.DatabaseHandler
import com.jefferson.application.br.model.ContactsData
import java.io.ByteArrayOutputStream

class ContactsActivity : AppCompatActivity() {
    var contacts: ArrayList<ContactsData>? = null
    var data: ContactsData? = null
    var db: DatabaseHandler? = null
    var mAdapter: ContactsAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.list_contatos)
        val mlistView = findViewById<ListView>(R.id.listcontatosListView1)
        LoadContacts(this, mlistView).execute()
        db = DatabaseHandler(this)
        mlistView.onItemClickListener =
            OnItemClickListener { _: AdapterView<*>?, vi: View, position: Int, _: Long ->
                val email = contacts!![position].email
                if (email != null) {
                    Toast.makeText(applicationContext, email, Toast.LENGTH_LONG).show()
                }
                val checkBox = vi.findViewById<CheckBox>(R.id.check_contacts)
                if (checkBox.isChecked) {
                    checkBox.isChecked = false
                    mAdapter!!.selection.remove(contacts!![position])
                } else {
                    checkBox.isChecked = true
                    mAdapter!!.selection.add(contacts!![position])
                }
            }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // getMenuInflater().inflate(R.menu.menu_choice, menu);
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            0 -> {
                PrivateContacts.selecionados = mAdapter!!.selection
                setResult(RESULT_OK)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    inner class LoadContacts(var mContext: Activity, var listContatos: ListView) :
        AsyncTask<Void?, Void?, Boolean?>() {
        var progress: ProgressDialog? = null
        var intProg = 0
        override fun onPreExecute() {
            super.onPreExecute()
            progress = ProgressDialog(mContext)
            progress!!.setTitle("Carregando...")
            progress!!.setMessage("Carregando todos os seus contatos, por favor espere...")
            progress!!.setCanceledOnTouchOutside(false)
            progress!!.progress = 0
            progress!!.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            progress!!.show()
            progress!!.setOnCancelListener { mContext.finish() }
        }

        @SuppressLint("Range")
        override fun doInBackground(p1: Array<Void?>): Boolean? {
            val cr = mContext.contentResolver //Activity/Application android.content.Context
            val cursor = cr.query(
                ContactsContract.Contacts.CONTENT_URI,
                null,
                null,
                null,
                ContactsContract.Contacts.DISPLAY_NAME + " ASC "
            )
            progress!!.max = cursor!!.count
            if (cursor.moveToFirst()) {
                contacts = ArrayList()
                do {
                    val id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                    var nome: String? = null
                    var numero: String? = null
                    var photo: ByteArray? = null
                    var email: String? = null
                    val pCur = cr.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(id),
                        null
                    )
                    while (pCur!!.moveToNext()) {
                        numero =
                            pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                        val contactId =
                            pCur.getLong(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID))
                        nome =
                            pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                        photo = openPhoto(contactId, mContext)
                    }
                    pCur.close()
                    val emailCursor = cr.query(
                        ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                        arrayOf(id),
                        null
                    )
                    while (emailCursor!!.moveToNext()) {
                        email =
                            emailCursor.getString(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA))
                        val type =
                            emailCursor.getInt(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE))
                        val s = ContactsContract.CommonDataKinds.Email.getTypeLabel(
                            mContext.resources, type, ""
                        ) as String
                        Log.d("TAG", "$s email: $email")
                    }
                    emailCursor.close()
                    if (nome != null) {
                        val mData = ContactsData()
                        mData.name = nome
                        mData.email = email
                        mData.photo = photo
                        mData.phoneNumber = numero
                        contacts!!.add(mData)
                    }
                    intProg++
                    progress!!.progress = intProg
                } while (cursor.moveToNext())
                cursor.close()
            }
            return null
        }

        override fun onPostExecute(result: Boolean?) {
            super.onPostExecute(result)
            progress!!.dismiss()
            mAdapter = ContactsAdapter(contacts!!, mContext)
            listContatos.adapter = mAdapter
        }

        fun openPhoto(contactId: Long, context: Context): ByteArray? {
            var data: ByteArray? = null
            val contactUri =
                ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId)
            val photoUri =
                Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY)
            val cursor = context.contentResolver.query(
                photoUri,
                arrayOf(ContactsContract.Contacts.Photo.PHOTO),
                null,
                null,
                null
            )
                ?: return null
            try {
                if (cursor.moveToFirst()) {
                    data = cursor.getBlob(0)
                }
            } finally {
                cursor.close()
            }
            return data
        }
    }

    companion object {
        fun getBytes(bitmap: Bitmap): ByteArray {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream)
            return stream.toByteArray()
        }
    }
}