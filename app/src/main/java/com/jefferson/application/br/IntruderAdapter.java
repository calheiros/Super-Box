package com.jefferson.application.br;

import android.content.*;
import androidx.appcompat.app.*;
import androidx.appcompat.widget.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;

import androidx.recyclerview.widget.RecyclerView;

//import com.squareup.picasso.*;
import java.io.*;
import java.util.*;

public class IntruderAdapter extends RecyclerView.Adapter<IntruderAdapter.ViewHolder> {

	ArrayList<String> mData;
	Context mContext;
	public static class ViewHolder extends RecyclerView.ViewHolder {
		ImageView mImage;

		public ViewHolder(View view) {
			super(view);
			mImage = (ImageView) view.findViewById(R.id.image_intruder);
		}
	}
    
	public IntruderAdapter(ArrayList<String> mData, Context mContext) {

		this.mData = mData;
		this.mContext = mContext;
	}
    
	@Override
	public IntruderAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int position) {
        
		View vi = LayoutInflater.from(parent.getContext())
			.inflate(R.layout.intruder_item, parent, false);

        ViewHolder vh = new ViewHolder(vi);

		return vh;
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, final int position) {

		File tnome = new File(mData.get(position));

		//Picasso.with(mContext).load(tnome).centerCrop().fit().into(holder.mImage);
		holder.mImage.setOnLongClickListener(new OnLongClickListener(){

				@Override
				public boolean onLongClick(View v) {
                    final AlertDialog.Builder mbuild =new AlertDialog.Builder(mContext);

					mbuild.setTitle(R.string.excluir)

                        .setMessage(R.string.excluir_face_intruder)
						.setPositiveButton(R.string.sim, new DialogInterface.OnClickListener(){

							@Override
							public void onClick(DialogInterface p1, int p2) {
								if (new File(mData.get(position)).delete()) {

									Toast.makeText(mContext, "Apagado", Toast.LENGTH_LONG).show();
                                    mData.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position, mData.size());

								} else {
									Toast.makeText(mContext, "Erro desconhecido", Toast.LENGTH_LONG).show();
								}
							}
                        });
					mbuild.setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener(){

							@Override
							public void onClick(DialogInterface p1, int p2) {
								mbuild.create().dismiss();
							}
                        });
                    mbuild.create().show();
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
		return mData.size();
	}

}
