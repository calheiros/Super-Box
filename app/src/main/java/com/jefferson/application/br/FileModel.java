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

import android.os.Parcelable;
import android.os.Parcel;

public class FileModel implements Parcelable {
    public static final String IMAGE_TYPE = "imagem";
    public static final String VIDEO_TYPE = "video";

    private String source_path;
    private String type;
    private String parentPath = null;
    
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int key) {
        parcel.writeString(source_path);
        parcel.writeString(type);
        parcel.writeString(parentPath);
    }
  
    public String getParentPath() {
        return parentPath;
    }
    
    public FileModel() {}
    
    private FileModel(Parcel p) {
        source_path = p.readString();
        type = p.readString();
        parentPath = p.readString();
    }
    
    public static final Parcelable.Creator<FileModel> CREATOR = new Parcelable.Creator<FileModel>() { 

        public FileModel createFromParcel(Parcel in) { 
            return new FileModel(in); 
        }

        public FileModel[] newArray(int size) { 
            return new FileModel[size];
        } 
    };
    
    public void setParentPath(String path){
        this.parentPath = path;
    }

    public String getResource() {
        return this.source_path;
    }

    public void setResource(String str) {
        this.source_path = str;
    }

    public void setType(String str) {
        this.type = str;
    }

    public String getType() {
        return this.type;
    }
}
