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

package com.jefferson.application.br.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.jefferson.application.br.R;
import com.jefferson.application.br.activity.ImportGalleryActivity;
import com.jefferson.application.br.activity.SelectionActivity;
import com.jefferson.application.br.model.FolderModel;

import java.util.ArrayList;

public class PhotosFolderAdapter extends ArrayAdapter<FolderModel> {

    private final ImportGalleryActivity mGalleryAlbum;
    private final int option;
    private ArrayList<FolderModel> al_menu;

    public PhotosFolderAdapter(ImportGalleryActivity galleryAlbum, ArrayList<FolderModel> al_menu, int option) {
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
        ViewHolder mViewHolder;
        if (convertView == null) {
            mViewHolder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.adapter_photosfolder, parent, false);
            mViewHolder.tv_foldern = (TextView) convertView.findViewById(R.id.tv_folder);
            mViewHolder.tv_foldersize = (TextView) convertView.findViewById(R.id.tv_folder2);
            mViewHolder.iv_image = (ImageView) convertView.findViewById(R.id.iv_image);
            mViewHolder.cd_layout = (RelativeLayout) convertView.findViewById(R.id.adapter_photosfolderParentView);
            //mViewHolder.smallView = (ImageView) convertView.findViewById(R.id.folder_small_icon_view);
            mViewHolder.cd_layout.setOnClickListener(v -> {
                Intent intent = new Intent(mGalleryAlbum, SelectionActivity.class);
                intent.putExtra("name", al_menu.get(position).getName());
                intent.putExtra("data", al_menu.get(position).getItems());
                intent.putExtra("type", mGalleryAlbum.getType());
                intent.putExtra("position", option);
                mGalleryAlbum.startActivityForResult(intent, ImportGalleryActivity.GET_CODE);
            });
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
        return convertView;
    }

    private static class ViewHolder {
        TextView tv_foldern, tv_foldersize;
        ImageView iv_image;
        RelativeLayout cd_layout;
        //ImageView smallView;
    }
}
