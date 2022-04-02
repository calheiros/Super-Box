package com.jefferson.application.br;

import java.util.ArrayList;
import java.io.Serializable;
import com.jefferson.application.br.model.MediaModel;
import android.os.Parcelable;
import android.os.Parcel;

public class FolderModel implements Parcelable {

    public static final String NO_FOLDER_NAME = "0";
	private String name = NO_FOLDER_NAME;
	private String path = "";
	private ArrayList<MediaModel> items = new ArrayList<>();

	public String getPath() {
		return path;
	}
    
    public FolderModel() {

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
        if (name != null) 
            this.name = name; 
	}
     
	public void addItem(MediaModel model) {
		items.add(model);
	}

	public String getName() {
		return name;
	}

	public void setFolderPath(String path) {
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
