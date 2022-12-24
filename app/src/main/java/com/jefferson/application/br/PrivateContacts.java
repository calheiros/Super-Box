package com.jefferson.application.br;
import android.annotation.SuppressLint;
import android.content.*;
import android.database.*;
import android.net.*;
import android.os.*;
import android.provider.*;
import androidx.appcompat.app.*;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.*;
import com.jefferson.application.br.database.*;
import java.util.*;

public class PrivateContacts extends AppCompatActivity {
	public static ArrayList<ContactsData> selecionados;
	ListView lista;
	PrivateContactsAdapter adapter;
	DatabaseHandler database;
	List<Contact> allContact;
	Contact Contact;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.private_contacts_listview);

		lista = (ListView)findViewById(R.id.listcontatosListView1);
		database = new DatabaseHandler(this);
		allContact = database.getAllContacts();
		adapter = new PrivateContactsAdapter(allContact, this);

		lista.setAdapter(adapter);

		lista.setOnItemClickListener(new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> p1, View p2, int position, long p4) {
					Contact = allContact.get(position);

					Intent i = new Intent(getApplicationContext(), ContactDetails.class);
					i.putExtra("nome", Contact._name);
					i.putExtra("photo", Contact._phone_photo);
					startActivity(i);
				}
			});
	}
	public void addContact(String name, String phone, byte[] photo) {

		ArrayList < ContentProviderOperation > ops = new ArrayList < ContentProviderOperation >();

		ops.add(ContentProviderOperation.newInsert(
					ContactsContract.RawContacts.CONTENT_URI)
				.withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
				.withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
				.build());

		//------------------------------------------------------ Names
		if (name != null) {
			ops.add(ContentProviderOperation.newInsert(
						ContactsContract.Data.CONTENT_URI)
					.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
					.withValue(ContactsContract.Data.MIMETYPE,
							   ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
					.withValue(
						ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
						name).build());
		}

		//------------------------------------------------------ Mobile Number                     
		if (phone != null) {
			ops.add(ContentProviderOperation.
					newInsert(ContactsContract.Data.CONTENT_URI)
					.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
					.withValue(ContactsContract.Data.MIMETYPE,
							   ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
					.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
					.withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
							   ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
					.build());
		}
		if (photo != null) {
			ops.add(ContentProviderOperation
					.newInsert(
						ContactsContract.Data.CONTENT_URI)
					.withValue(ContactsContract.Data.MIMETYPE,
							   ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
					.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
					.withValue(ContactsContract.CommonDataKinds.Photo.DATA15, photo).build());
		}
		//------------------------------------------------------ Home Numbers

		// Asking the Contact provider to create a new contact                 
		try {
			getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this, "Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
		}

	}
    
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_confirm, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
				Intent intent = new Intent(this, ContactsActivity.class);
                startActivityForResult(intent, 0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
	@SuppressLint("Range")
	public static boolean deleteContact(Context ctx, String phone, String name) {
		Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone));
		Cursor cur = ctx.getContentResolver().query(contactUri, null, null, null, null);
		try {
			if (cur.moveToFirst()) {
				do {
					if (name.equalsIgnoreCase(cur.getString(cur.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)))) {
						String lookupKey = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
						Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
						ctx.getContentResolver().delete(uri, null, null);
						return true;
					}

				} while (cur.moveToNext());
			}

		} catch (Exception e) {
			System.out.println(e.getStackTrace());
		}
		return false;
	}
    
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			for (ContactsData contData: selecionados) {
				database.addContact(new Contact(contData.nome, contData.numero_phone, contData.photo));
				try {
					deleteContact(this, contData.numero_phone, contData.nome);
				} catch (Exception e) {

				}
			}
			recreate();
		} else {
			Toast.makeText(this, "RESULT_CANCELED", Toast.LENGTH_LONG).show();
		}
        
		super.onActivityResult(requestCode, resultCode, data);
	}

}
