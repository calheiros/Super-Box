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

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class IntruderAdapter extends RecyclerView.Adapter<IntruderAdapter.ViewHolder> {

    ArrayList<String> data;
    Context context;

    public IntruderAdapter(ArrayList<String> mData, Context mContext) {
        this.data = mData;
        this.context = mContext;
    }

    @Override
    @NonNull
    public IntruderAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int position) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.intruder_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
    }

    private boolean deletePhotoDialog(int position) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(R.string.excluir)
                .setMessage(R.string.excluir_aviso_imagem)
                .setPositiveButton(R.string.sim, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface p1, int p2) {
                        deletePhoto(position);
                    }
                });
        dialog.setNegativeButton(R.string.cancelar, (p1, p2) -> dialog.create().dismiss());
        dialog.create().show();
        return true;
    }

    private void deletePhoto(int position) {
        if (new File(data.get(position)).delete()) {
            Toast.makeText(context, context.getString(R.string.sucesso), Toast.LENGTH_LONG).show();
            data.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, data.size());
        } else {
            Toast.makeText(context, context.getString(R.string.erro), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        File file = new File(data.get(position));
        Glide.with(context).load(file).centerCrop().into(holder.mImageView);
        holder.mImageView.setOnLongClickListener(v -> deletePhotoDialog(holder.getAdapterPosition()));
        holder.mImageView.setOnClickListener(p1 -> {
                 /*
                 Intent i = new Intent(mContext, Visualizar_Imagem.class);
                 i.putExtra("filepath", mData);
                 i.putExtra("position", position);
                 context.startActivity(i);
                 */
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView mImageView;

        public ViewHolder(View view) {
            super(view);
            mImageView = view.findViewById(R.id.image_intruder);
        }
    }
}
