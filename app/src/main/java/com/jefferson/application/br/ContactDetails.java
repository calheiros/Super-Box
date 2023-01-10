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
