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

public class MediaModel implements Parcelable {

    private String absolutePath;
    private String duration;

    public MediaModel() {
        
    } 

    private MediaModel(Parcel p) {
        absolutePath = p.readString();
        duration = p.readString();
    }

    public static final Parcelable.Creator<MediaModel> CREATOR = new Parcelable.Creator<MediaModel>() { 

        public MediaModel createFromParcel(Parcel in) { 
            return new MediaModel(in); 
        }

        public MediaModel[] newArray(int size) { 
            return new MediaModel[size];
        } 
    };

    public MediaModel(String path) {
        this.absolutePath = path;
    }

    public void setPath(String path) {
        this.absolutePath = path;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getDuration() {
        return duration;
    }

    public String getPath() {
        return absolutePath;
    }
    
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(absolutePath);
        parcel.writeString(duration);
    }

    @Override
    public String toString() {
        return absolutePath;
    }
}
