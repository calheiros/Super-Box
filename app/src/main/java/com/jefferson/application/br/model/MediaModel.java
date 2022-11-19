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
