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

import android.content.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.*;

import android.view.*;
import android.view.View.*;
import android.widget.*;

import androidx.recyclerview.widget.RecyclerView;

//import com.squareup.picasso.*;
import java.io.*;
import java.util.*;

public class IntruderAdapter extends RecyclerView.Adapter<IntruderAdapter.ViewHolder> {

	ArrayList<String> data;
	Context context;
	public static class ViewHolder extends RecyclerView.ViewHolder {
		ImageView mImage;

		public ViewHolder(View view) {
			super(view);
			mImage = (ImageView) view.findViewById(R.id.image_intruder);
		}
	}
    
	public IntruderAdapter(ArrayList<String> mData, Context mContext) {

		this.data = mData;
		this.context = mContext;
	}
    
	@NonNull
	@Override
	public IntruderAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int position) {
		View vi = LayoutInflater.from(parent.getContext())
			.inflate(R.layout.intruder_item, parent, false);

		return  new ViewHolder(vi);
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
		super.onBindViewHolder(holder, position, payloads);

	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {

		File tnome = new File(data.get(position));

		//Picasso.with(mContext).load(tnome).centerCrop().fit().into(holder.mImage);
		holder.mImage.setOnLongClickListener(new OnLongClickListener(){

				@Override
				public boolean onLongClick(View v) {
					int position = holder.getAdapterPosition();
                    final AlertDialog.Builder builder = new AlertDialog.Builder(context);

					builder.setTitle(R.string.excluir)
                        .setMessage(R.string.excluir_face_intruder)
						.setPositiveButton(R.string.sim, new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface p1, int p2) {
								if (new File(data.get(position)).delete()) {

									Toast.makeText(context, "Apagado", Toast.LENGTH_LONG).show();
                                    data.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position, data.size());

								} else {
									Toast.makeText(context, "Erro desconhecido", Toast.LENGTH_LONG).show();
								}
							}
                        });
					builder.setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener(){

							@Override
							public void onClick(DialogInterface p1, int p2) {
								builder.create().dismiss();
							}
                        });
                    builder.create().show();
					return true;
				}

			});

		holder.mImage.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View p1) {
					/*Intent i = new Intent(mContext, Visualizar_Imagem.class);
                     i.putExtra("filepath", mData);
                     i.putExtra("position", position);
                     mContext.startActivity(i);*/
				}
			});
	}

	@Override
	public int getItemCount() {
		return data.size();
	}

}
