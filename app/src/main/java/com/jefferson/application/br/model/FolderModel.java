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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.os.Parcelable;
import android.os.Parcel;

public class FolderModel implements Parcelable {

    public static final String NO_FOLDER_NAME = "0";
	private String name = NO_FOLDER_NAME;
	private String path = "";
    private boolean favorite = false;
	private ArrayList<MediaModel> items = new ArrayList<>();

    public FolderModel() {
    }

    public static void sort(ArrayList<FolderModel> models) {
        Collections.sort(models, new Comparator<FolderModel>() {
                    @Override public int compare(FolderModel f1, FolderModel f2) {

                        if(f1.isFavorite() && !f2.isFavorite())
                            return -1;

                        if (f2.isFavorite() && !f1.isFavorite())
                            return 1;

                        return f1.getName().toLowerCase().compareTo(f2.getName().toLowerCase());
                    }
                }
        );
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public String getTag() {
        return null;
    }

	public String getPath() {
		return path;
	}

    private FolderModel(Parcel p) {
        path = p.readString();
        p.readTypedList(items, MediaModel.CREATOR);
    }

    public static final Parcelable.Creator<FolderModel> CREATOR = new Parcelable.Creator<FolderModel>() { 

        public FolderModel createFromParcel(Parcel in) { 
            return new FolderModel(in); 
        } 

        public FolderModel[] newArray(int size) {
            return new FolderModel[size]; 
        }
    };
    
	public void setName(String name) {
        if (name != null) {
            this.name = name; 
         }
	}
     
	public void addItem(MediaModel model) {
		items.add(model);
	}

	public String getName() {
		return name;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public ArrayList<MediaModel> getItems() {
		return items;
	}
    
    public MediaModel getModelByPath(String path) {
        if (path == null) return null;
        
        for (MediaModel model: items){
            if (path.equals(model.getPath()))
                return model;
        }
        
        return null;
    }
    
    public ArrayList<String> getItemsPath() {
        ArrayList<String> list = new ArrayList<>();

        for (MediaModel model:  items) {
            list.add(model.getPath());
        }

        return list;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(path);
        parcel.writeTypedList(items);
    }
}
