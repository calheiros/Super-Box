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

package com.jefferson.application.br.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import java.util.ArrayList;

public class SimplifiedAlbum implements Parcelable {
    private String name;
    private String thumbnailPath;
    
    public SimplifiedAlbum(String name, String thumb) {
        this.name = name;
        this.thumbnailPath = thumb;
    }

    private SimplifiedAlbum(Parcel parcel) {
        this.name = parcel.readString();
        this.thumbnailPath = parcel.readString();
    }

    public static final Creator<SimplifiedAlbum> CREATOR = new Creator<SimplifiedAlbum>() {
        @Override
        public SimplifiedAlbum createFromParcel(Parcel in) {
            return new SimplifiedAlbum(in);
        }

        @Override
        public SimplifiedAlbum[] newArray(int size) {
            return new SimplifiedAlbum[size];
        }
    };

    public static SimplifiedAlbum createFrom(FolderModel item) {
        ArrayList<String> itemsPath = item.getItemsPath();
        String name = item.getName();
        String thumb = itemsPath.size() > 0 ? itemsPath.get(0) : null;
        return new SimplifiedAlbum(name, thumb);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(thumbnailPath);
    }

    public String getThumbPath() {
        return thumbnailPath;
    }

    public String getName() {
        return name;
    }

    public void setName(String text) {
        this.name = text;
    }
}
