package com.jefferson.application.br.model;

public class PickerModel {
    
    private String name;
    private String path;
    private int size;
    private String tumb_path;
 
    public void setTumbPath(String str) {
        this.tumb_path = str;
    }
 
    public String getTumbPath() {
        return this.tumb_path;
    }
 
    public void setName(String str) {
        this.name = str;
    }
 
    public String getName() {
        return this.name;
    }
 
    public void setPath(String str) {
        this.path = str;
    }
 
    public String getPath() {
        return this.path;
    }
 
    public void setSize(int i) {
        this.size = i;
    }
 
    public int getSize() {
        return this.size;
    }
}
