package com.jefferson.application.br.adapter;

import android.content.Intent;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.jefferson.application.br.FolderModel;
import com.jefferson.application.br.R;
import com.jefferson.application.br.activity.GalleryAlbum;
import com.jefferson.application.br.activity.SelectionActivity;
import java.util.ArrayList;
import android.os.Build;
import android.util.Log;

public class PhotosFolderAdapter extends ArrayAdapter<FolderModel> {

    private GalleryAlbum mGalleryAlbum;
    private ViewHolder mViewHolder;
    private ArrayList<FolderModel> al_menu = new ArrayList<>();
    private int option;

    public PhotosFolderAdapter(GalleryAlbum galleryAlbum, ArrayList<FolderModel> al_menu, int option) {
        super(galleryAlbum, R.layout.adapter_photosfolder, al_menu);
        this.al_menu = al_menu;
        this.mGalleryAlbum = galleryAlbum;
		this.option = option;
    }

	public void set(ArrayList<FolderModel> localList) {
		al_menu = localList;
		if (localList.size() > 0)
			notifyDataSetChanged();
		else
			notifyDataSetInvalidated();
	}

    @Override
    public int getCount() {
        return al_menu.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        if (al_menu.size() > 0) {
            return al_menu.size();
        } else {
            return 1;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            mViewHolder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.adapter_photosfolder, parent, false);
            mViewHolder.tv_foldern = (TextView) convertView.findViewById(R.id.tv_folder);
            mViewHolder.tv_foldersize = (TextView) convertView.findViewById(R.id.tv_folder2);
            mViewHolder.iv_image = (ImageView) convertView.findViewById(R.id.iv_image);
			mViewHolder.cd_layout = (CardView) convertView.findViewById(R.id.card_view);
			mViewHolder.play_view = (ImageView) convertView.findViewById(R.id.play_view);
            mViewHolder.cd_layout.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
                        Intent intent = new Intent(mGalleryAlbum, SelectionActivity.class);
                        intent.putExtra("name", al_menu.get(position).getName());
                        intent.putExtra("data", al_menu.get(position).getItems());
                        intent.putExtra("type", mGalleryAlbum.getType());
                        intent.putExtra("position", option);

                        mGalleryAlbum.startActivityForResult(intent, GalleryAlbum.GET_CODE);

                    }
				}
            );
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

		FolderModel f_model = al_menu.get(position);
		mViewHolder.tv_foldern.setText(f_model.getName());
		mViewHolder.tv_foldersize.setText(String.valueOf(f_model.getItems().size()));
        
		if (f_model.getItems().size() != 0) {
			Glide.with(mGalleryAlbum).load("file://" + f_model.getItems().get(0))
				.skipMemoryCache(true)
				.into(mViewHolder.iv_image);
        }

        if  (option == 1) {
            mViewHolder.play_view.setVisibility(View.VISIBLE);
            mViewHolder.play_view.setImageResource(R.drawable.ic_play_circle);
        }
        return convertView;
    }

    private static class ViewHolder {
        TextView tv_foldern, tv_foldersize;
        ImageView iv_image;
		CardView cd_layout;
		ImageView play_view;
    }
}
