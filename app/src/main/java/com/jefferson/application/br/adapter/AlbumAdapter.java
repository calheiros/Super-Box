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
import android.graphics.Path;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.viewHolder> {

	private AlbumFragment fragment;
	private ArrayList<FolderModel> models;
	private View group;
    private int pagerPosition;

	public AlbumAdapter(AlbumFragment fragment , ArrayList<FolderModel> items) {
		this.fragment = fragment;
		this.models = items;
        this.pagerPosition = fragment.getPagerPosition();
	}

    public FolderModel getItem(int itemPosition) {
        if (itemPosition >= 0 && itemPosition < getItemCount()) {
            return models.get(itemPosition);
        }
        return null;
    }
    
    public void sortModels(ArrayList<FolderModel> list) {
        Collections.sort(list, new Comparator<FolderModel>() {
                @Override public int compare(FolderModel o1, FolderModel o2) { 
                    return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase()); 
                } 
            }
        );
    }
    
    public void insertItem(FolderModel item) {
        models.add(item);
        sortModels(models);
        notifyDataSetChanged();
        
        int position = models.indexOf(item);
        if (position != -1) {
            fragment.scrollTo(position);
        }
    }

    private void insertItem(FolderModel model, int position) {
        models.add(position, model);
        notifyItemInserted(position);
        
    }

    public void removeItem(int position) {
        if (position >= 0 && position < getItemCount()) {
            models.remove(position);
            notifyItemRemoved(position);
        } else {
            Toast.makeText(App.getAppContext(), "Can not remove item at: " + position, 1).show();
        }
    }

    public void setUpdatedData(ArrayList<FolderModel> localList) {
		models = localList;
		notifyDataSetChanged();
	}

	@Override
	public viewHolder onCreateViewHolder(ViewGroup parent, int p2) {
		group = parent;
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_photosfolder, parent, false);
		return new viewHolder(view);
	}

    public void removeItem(FolderModel item) {
        int key = models.indexOf(item);
        if (key != -1) {
            models.remove(key);
            notifyItemRemoved(key);
        } else {
            Toast.makeText(App.getAppContext(), "Can not find folder index for item " + item.getName(), 1).show();
        }
    }

    public int getItemPosition(String path) {
        for (int i = 0; i < models.size(); i++) {
            FolderModel model = models.get(i);
            if (model.getPath().equals(path)) {
                return i;
            }
        }
        return -1;
    }
	@Override
	public void onBindViewHolder(final viewHolder holder, int position) {
        final FolderModel f_model = models.get(position);
	    holder.tv_foldern.setText(f_model.getName());
		holder.tv_foldersize.setText(String.valueOf(f_model.getItems().size()));
        boolean isEmpty = f_model.getItems().isEmpty();

        /*if (pagerPosition == 1) {
         holder.smallView.setVisibility(View.VISIBLE);
         holder.smallView.setImageResource(R.drawable.ic_play_box_outline);
         }*/

		if (!isEmpty) {
            holder.smallView.setVisibility(View.GONE);
			Glide.with(fragment).load("file://" + f_model.getItems().get(0).getPath())
				.skipMemoryCache(true)
				.into(holder.iv_image);
        } else {
            holder.iv_image.setImageResource(0);
            holder.smallView.setImageResource(R.drawable.ic_image_broken_variant);
        }

        int visibility = (isEmpty) ? View.VISIBLE : View.GONE;

        if (holder.smallView.getVisibility() != visibility) {
            holder.smallView.setVisibility(visibility);
        }

        holder.cd_layout.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View view) {
					Intent intent = new Intent(fragment.getContext(), ViewAlbum.class);
					intent.putExtra("position", fragment.getPagerPosition());
					intent.putExtra("name", f_model.getName());
					intent.putExtra("data", f_model.getItems());
					intent.putExtra("folder", f_model.getPath());
					fragment.getActivity().startActivity(intent);
				}
			}
        );

		holder.cd_layout.setOnLongClickListener(new View.OnLongClickListener() {

				@Override
				public boolean onLongClick(final View view) {
                    Context context = view.getContext();
					final String[] options = {context.getString(R.string.apagar), context.getString(R.string.renomear)};
					AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
					builder.setItems(options, new DialogInterface.OnClickListener(){

							@Override
							public void onClick(DialogInterface dInterface, int index) {

                                if (index == 0) {
									fragment.deleteFolder(f_model);
								} else {
                                    fragment.inputFolderDialog(f_model, fragment.ACTION_RENAME_FOLDER);
                                }
							}
						}
                    );
                    DialogUtils.configureDialog(builder.show());

					return false;
				}
			}
        );
	}

	@Override
	public int getItemCount() {
		return models.size();
	}

	public class viewHolder extends RecyclerView.ViewHolder {

        TextView tv_foldern, tv_foldersize;
        ImageView iv_image;
		RelativeLayout cd_layout;
		ImageView smallView;

		public viewHolder(View view) {
			super(view);

		    tv_foldern = view.findViewById(R.id.tv_folder);
            tv_foldersize = view.findViewById(R.id.tv_folder2);
            iv_image = view.findViewById(R.id.iv_image);
			cd_layout = view.findViewById(R.id.adapter_photosfolderParentView);
			smallView = view.findViewById(R.id.folder_small_icon_view);
		}
	}
}
