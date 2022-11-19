package com.jefferson.application.br;
import android.os.*;

import androidx.appcompat.app.*;

import com.google.android.material.appbar.CollapsingToolbarLayout;

public class ContactDetails extends AppCompatActivity
{
	ContactsDetailsAdapter adapter;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact_details);
		CollapsingToolbarLayout collapsing = (CollapsingToolbarLayout)findViewById(R.id.collapsingToolbar);
		/*CircleImageView circleImag = (CircleImageView)findViewById(R.id.imagePhoto);
		ListView list = (ListView)findViewById(R.id.contact_info);
        

		Intent i = getIntent();
		String nome = i.getStringExtra("nome");
		byte[] Byte = i.getByteArrayExtra("photo");
		collapsing.setTitle(nome);

		if (Byte != null)
		{
			ByteArrayInputStream input = new ByteArrayInputStream(Byte);
			Bitmap bitmap = BitmapFactory.decodeStream(input);
		    circleImag.setImageBitmap(bitmap);
		}
		else
		{
			circleImag.setImageResource(R.drawable.ic_photo_contact_null);
		}
		List<String> ArrayList=new ArrayList<String>();
		
		for (int in=0;in < 4;in++)
		{
          ArrayList.add("item "+in);
		}
		adapter = new ContactsDetailsAdapter(this,ArrayList);
		list.setAdapter(adapter);
		*/
	}

}
