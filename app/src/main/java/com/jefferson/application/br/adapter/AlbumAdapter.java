package com.jefferson.application.br.adapter;

import android.app.*;
import android.content.*;
import android.support.v7.widget.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import com.bumptech.glide.*;
import com.jefferson.application.br.*;
import com.jefferson.application.br.activity.*;
import com.jefferson.application.br.fragment.*;
import java.util.*;
import com.jefferson.application.br.util.*;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.viewHolder> {

	private AlbumFragment fragment;
	private ArrayList<FolderModel> items;
	private View group;

	public AlbumAdapter(AlbumFragment fragment , ArrayList<FolderModel> items) {
		this.fragment = fragment;
		this.items = items;
	}

	public void setUpdatedData(ArrayList<FolderModel> localList) {
		items = localList;
		notifyDataSetChanged();
	}
	@Override
	public viewHolder onCreateViewHolder(ViewGroup parent, int p2) {
		group = parent;
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_photosfolder, parent, false);
		return new viewHolder(view);
	}

	@Override
	public void onBindViewHolder(final viewHolder holder, final int position) {

        FolderModel f_model = items.get(position);
	    holder.tv_foldern.setText(f_model.getName());
		holder.tv_foldersize.setText(String.valueOf(f_model.getItems().size()));
        boolean isEmpty = f_model.getItems().size() == 0;
        int pagerPosition = fragment.getPagerPosition();
		
        if (pagerPosition == 1) {
			holder.play_view.setVisibility(View.VISIBLE);
            holder.play_view.setImageResource(R.drawable.ic_play_circle);
        }

		if (!isEmpty) {
			Glide.with(fragment).load("file://" + f_model.getItems().get(0).getPath())
				.skipMemoryCache(true)
				.into(holder.iv_image);
        } else {
            holder.iv_image.setImageResource(0);
            holder.play_view.setImageResource(R.drawable.ic_image_broken_variant);
        }
        
        int visibility = ( isEmpty || pagerPosition == 1 ) ? View.VISIBLE : View.GONE;
        holder.play_view.setVisibility(visibility);
		holder.cd_layout.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View view) {
					Intent intent = new Intent(fragment.getContext(), ViewAlbum.class);
					intent.putExtra("position", fragment.getPagerPosition());
					intent.putExtra("name", items.get(position).getName());
					intent.putExtra("data", items.get(position).getItems());
					intent.putExtra("folder", items.get(position).getPath());
					fragment.getActivity().startActivity(intent);
				}
			}
        );

		holder.cd_layout.setOnLongClickListener(new View.OnLongClickListener(){

				@Override
				public boolean onLongClick(final View view) {
                    Context context = view.getContext();
					final String[] options = {context.getString(R.string.apagar), context.getString(R.string.renomear)};
					AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
					builder.setItems(options, new DialogInterface.OnClickListener(){

							@Override
							public void onClick(DialogInterface dInterface, int index) {

                                if (index == 0) {
									fragment.deleteAlbum(items.get(position));
								} else {
                                    fragment.inputFolderDialog(items.get(position), fragment.ACTION_RENAME_FOLDER);
                                }
							}
						}
                    );
                    DialogUtils.configureRoudedDialog(builder.show());

					return false;
				}
			}
        );
	}

	@Override
	public int getItemCount() {
		return items.size();
	}

	public class viewHolder extends RecyclerView.ViewHolder {

        TextView tv_foldern, tv_foldersize;
        ImageView iv_image;
		CardView cd_layout;
		ImageView play_view;

		public viewHolder(View view) {
			super(view);

		    tv_foldern = view.findViewById(R.id.tv_folder);
            tv_foldersize = view.findViewById(R.id.tv_folder2);
            iv_image = view.findViewById(R.id.iv_image);
			cd_layout = view.findViewById(R.id.card_view);
			play_view = view.findViewById(R.id.play_view);

		}
	}
}
