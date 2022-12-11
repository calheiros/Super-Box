package com.jefferson.application.br.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.jefferson.application.br.App;
import com.jefferson.application.br.model.FolderModel;
import com.jefferson.application.br.R;
import com.jefferson.application.br.activity.ViewAlbum;
import com.jefferson.application.br.fragment.AlbumFragment;
import com.jefferson.application.br.util.DialogUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ViewHolder> {

    private final AlbumFragment fragment;
    private final int pagerPosition;
    private ArrayList<FolderModel> models;
    private View group;

    public AlbumAdapter(AlbumFragment fragment, ArrayList<FolderModel> items) {
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
            @Override
            public int compare(FolderModel o1, FolderModel o2) {
                return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
            }
        });
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
            Toast.makeText(App.getAppContext(), "Can not remove item at: " + position, Toast.LENGTH_LONG).show();
        }
    }

    public void setUpdatedData(ArrayList<FolderModel> localList) {
        models = localList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int p2) {
        group = parent;
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_photosfolder, parent, false);
        return new ViewHolder(view);
    }

    public void notifyItemChanged(FolderModel f_model) {
        for(int i = 0; i < getItemCount(); i++) {
            if (f_model.getName().equals(getItem(i).getName())) {
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void removeItem(FolderModel item) {
        int key = models.indexOf(item);
        if (key != -1) {
            models.remove(key);
            notifyItemRemoved(key);
        } else {
            Toast.makeText(App.getAppContext(), "Can not find folder index for item " + item.getName(), Toast.LENGTH_LONG).show();
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
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final FolderModel f_model = models.get(position);
        holder.tv_foldern.setText(f_model.getName());
        holder.tv_foldersize.setText(String.valueOf(f_model.getItems().size()));
        boolean isEmpty = f_model.getItems().isEmpty();
        holder.favoriteView.setVisibility(f_model.isFavorite() ? View.VISIBLE: View.GONE);

        if (!isEmpty) {
            Glide.with(fragment.requireContext()).load("file://" + f_model.getItems().get(0).getPath()).skipMemoryCache(true).into(holder.iv_image);
        } else {
            holder.iv_image.setImageBitmap(null);
            holder.smallView.setImageResource(R.drawable.ic_image_broken_variant);
        }

        int visibility = (isEmpty) ? View.VISIBLE : View.GONE;

        if (holder.smallView.getVisibility() != visibility) {
            holder.smallView.setVisibility(visibility);
        }

        holder.cd_layout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(fragment.getContext(), ViewAlbum.class);
                intent.putExtra("position", fragment.getPagerPosition());
                intent.putExtra("name", f_model.getName());
                intent.putExtra("data", f_model.getItems());
                intent.putExtra("folder", f_model.getPath());
                fragment.getActivity().startActivity(intent);
            }
        });

        holder.cd_layout.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(final View view) {
                Context context = view.getContext();
                View menuView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.dialog_list_view_layout, null);
                final String[] options = {context.getString(R.string.renomear), context.getString(R.string.apagar), f_model.isFavorite() ? "Remove from favorites": "Add to Favorites"};
                final int[] icons = {R.drawable.ic_rename, R.drawable.ic_delete_all, R.drawable.ic_bookmark};

                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setView(menuView);
                AlertDialog dialog = builder.create();
                DialogUtils.configureDialog(dialog);
                dialog.show();

                DialogAdapter adapter = new DialogAdapter(options, icons, context);
                ListView listView = (ListView) menuView.findViewById(R.id.dialog_list_view);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new DialogMenuListener(f_model, dialog));

                return false;
            }
        });

    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tv_foldern, tv_foldersize;
        ImageView iv_image, favoriteView;
        RelativeLayout cd_layout;
        ImageView smallView;

        public ViewHolder(View view) {
            super(view);
            tv_foldern = view.findViewById(R.id.tv_folder);
            tv_foldersize = view.findViewById(R.id.tv_folder2);
            iv_image = view.findViewById(R.id.iv_image);
            cd_layout = view.findViewById(R.id.adapter_photosfolderParentView);
            smallView = view.findViewById(R.id.folder_small_icon_view);
            favoriteView = view.findViewById(R.id.folder_favorite_icon_view);
        }
    }

    public static class DialogAdapter extends BaseAdapter {
        private final Context context;
        private final CharSequence[] options;
        private final int[] icons;

        public DialogAdapter(CharSequence[] options, int[] icons, Context context) {
            this.options = options;
            this.icons = icons;
            this.context = context;
            if (options.length != icons.length)
                throw new IllegalArgumentException("'Icons' and 'Options' must to have the same length");
        }

        @Override
        public int getCount() {
            return options.length;
        }

        @Override
        public Object getItem(int position) {
            return options[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = null;
            if (convertView == null) {
                view = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                        .inflate(R.layout.dialog_menu_item_layout, parent, false);
                TextView textView = view.findViewById(R.id.dialog_item_text_view);
                ImageView imageView = view.findViewById(R.id.dialog_item_image_view);

                textView.setText(options[position]);
                imageView.setImageResource(icons[position]);
            }

            return view;
        }
    }
    private class DialogMenuListener implements AdapterView.OnItemClickListener {

        FolderModel f_model;
        AlertDialog dialog;

        public DialogMenuListener(FolderModel f_model, AlertDialog dialog) {
            this.dialog = dialog;
            this.f_model = f_model;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            switch (position) {
                case 0:
                    fragment.inputFolderDialog(f_model, AlbumFragment.ACTION_RENAME_FOLDER);
                    break;
                case 1:
                    fragment.deleteFolder(f_model);
                    break;
                case 2:
                    int startPosition = getItemPosition(f_model);

                    if (f_model.isFavorite())
                        fragment.removeFromFavorites(f_model);
                    else
                        fragment.addToFavorites(f_model);

                    FolderModel.sort(models);
                    int endPosition = getItemPosition(f_model);
                    notifyItemMoved(startPosition, endPosition);
                    break;
            }
            dialog.dismiss();
        }
    }

    private int getItemPosition(FolderModel f_model) {
        for (int i = 0; i < getItemCount(); i++) {
            if (getItem(i).equals(f_model)) {
                notifyItemChanged(i);
               return i;
            }
        }
        return -1;
    }
}