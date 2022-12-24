package com.jefferson.application.br;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.jefferson.application.br.database.DatabaseHandler;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class ContactsActivity extends AppCompatActivity {

    ArrayList<ContactsData> contacts;
    ContactsData data;
    DatabaseHandler db;
    ContatosAdapter mAdapter;

    public static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_contatos);
        ListView mlistView = (ListView) findViewById(R.id.listcontatosListView1);
        new LoadContacts(this, mlistView).execute();
        db = new DatabaseHandler(this);

        mlistView.setOnItemClickListener(new OnItemClickListener() {

                                             @Override
                                             public void onItemClick(AdapterView<?> p1, View vi, int position, long p4) {
                                                 String email = contacts.get(position).email;
                                                 if (email != null) {
                                                     Toast.makeText(getApplicationContext(), email, Toast.LENGTH_LONG).show();
                                                 }

                                                 CheckBox checkW = (CheckBox) vi.findViewById(R.id.check_contacts);
                                                 if (checkW.isChecked()) {
                                                     checkW.setChecked(false);
                                                     mAdapter.selecionados.remove(contacts.get(position));
                                                 } else {
                                                     checkW.setChecked(true);
                                                     mAdapter.selecionados.add(contacts.get(position));
                                                 }
                                             }
                                         }
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // getMenuInflater().inflate(R.menu.menu_choice, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                PrivateContacts.selecionados = mAdapter.selecionados;
                setResult(RESULT_OK);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public class LoadContacts extends AsyncTask<Void, Void, Boolean> {

        ProgressDialog progress;
        Activity mContext;
        ListView listContatos;
        int intProg;

        public LoadContacts(Activity mContext, ListView listContatos) {
            intProg = 0;
            this.mContext = mContext;
            this.listContatos = listContatos;
        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            progress = new ProgressDialog(mContext);
            progress.setTitle("Carregando...");
            progress.setMessage("Carregando todos os seus contatos, por favor espere...");
            progress.setCanceledOnTouchOutside(false);
            progress.setProgress(0);
            progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progress.show();
            progress.setOnCancelListener(new DialogInterface.OnCancelListener() {

                                             @Override
                                             public void onCancel(DialogInterface p1) {
                                                 mContext.finish();
                                             }
                                         }
            );
        }

        @SuppressLint("Range")
		@Override
        protected Boolean doInBackground(Void[] p1) {
            ContentResolver cr = mContext.getContentResolver(); //Activity/Application android.content.Context
            Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, ContactsContract.Contacts.DISPLAY_NAME + " ASC ");

            progress.setMax(cursor.getCount());

            if (cursor.moveToFirst()) {
                contacts = new ArrayList<ContactsData>();

                do {
                    String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    String nome = null;
                    String numero = null;
                    byte[] photo = null;
                    String email = null;

                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        numero = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        long contactId = pCur.getLong(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                        nome = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        photo = openPhoto(contactId, mContext);

                    }
                    pCur.close();

                    Cursor emailCursor = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", new String[]{id}, null);

                    while (emailCursor.moveToNext()) {

                        email = emailCursor.getString(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                        int type = emailCursor.getInt(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
                        String s = (String) ContactsContract.CommonDataKinds.Email.getTypeLabel(mContext.getResources(), type, "");


                        Log.d("TAG", s + " email: " + email);
                    }
                    emailCursor.close();

                    if (nome != null) {
                        ContactsData mData = new ContactsData();
                        mData.nome = nome;
                        mData.email = email;
                        mData.photo = photo;
                        mData.numero_phone = numero;

                        contacts.add(mData);
                    }

                    intProg++;
                    progress.setProgress(intProg);
                } while (cursor.moveToNext());

                cursor.close();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            progress.dismiss();
            mAdapter = new ContatosAdapter(contacts, mContext);
            listContatos.setAdapter(mAdapter);
        }

        public byte[] openPhoto(long contactId, Context context) {
            byte[] data = null;
            Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
            Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
            Cursor cursor = context.getContentResolver().query(photoUri,
                    new String[]{ContactsContract.Contacts.Photo.PHOTO}, null, null, null);
            if (cursor == null) {
                return null;
            }

            try {
                if (cursor.moveToFirst()) {
                    data = cursor.getBlob(0);

                }
            } finally {
                cursor.close();
            }
            return data;

        }
    }


}
