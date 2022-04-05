package com.jefferson.application.br;
import android.app.*;
import android.content.*;
import android.database.*;
import android.graphics.*;
import android.net.*;
import android.os.*;
import android.provider.*;
import android.support.v7.app.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.*;
import com.jefferson.application.br.database.*;
import java.io.*;
import java.util.*;

public class ContactsActivity extends AppCompatActivity {

	ArrayList<ContactsData> contacts;
    ContactsData data;
	DatabaseHandler db;
	ContatosAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_contatos);
	    ListView mlistView = (ListView)findViewById(R.id.listcontatosListView1);
		new LoadContacts(this, mlistView).execute();
	    db = new DatabaseHandler(this);

		mlistView.setOnItemClickListener(new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> p1, View vi, int position, long p4) {
					String email = contacts.get(position).email;
					if (email != null) {
						Toast.makeText(getApplicationContext(), email, Toast.LENGTH_LONG).show();
					}

                    CheckBox checkW = (CheckBox)vi.findViewById(R.id.check_contacts);
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
	public static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

	public class LoadContacts extends AsyncTask<Void,Void,Boolean> {

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
			progress.setOnCancelListener(new DialogInterface.OnCancelListener(){

					@Override
					public void onCancel(DialogInterface p1) {
						mContext.finish();
					}
				}
            );
		}

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

                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{ id }, null);
                    while (pCur.moveToNext()) {
                        numero = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        long contactId = pCur.getLong(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                        nome = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        photo = openPhoto(contactId, mContext);

                    }
                    pCur.close();

					Cursor emailCursor = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", new String[] { id }, null);

					while (emailCursor.moveToNext()) {

					    email = emailCursor.getString(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
						int type = emailCursor.getInt(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
						String s = (String) ContactsContract.CommonDataKinds.Email.getTypeLabel(mContext.getResources(), type, "");


						Log.d("TAG", s + " email: " + email);
					}
					emailCursor.close();
                    
					if (nome != null) {
                        ContactsData mData= new ContactsData();
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
															   new String[] {ContactsContract.Contacts.Photo.PHOTO}, null, null, null);
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
