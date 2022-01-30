package com.jefferson.application.br;

import java.util.ArrayList;

public class FolderModel {
    
    public static final String NO_FOLDER_NAME = "0";
	private String name = NO_FOLDER_NAME;
	private String path = "";
	private ArrayList<String> items = new ArrayList<>();

	public String getPath() {
		return path;
	}
	public void setName(String name) {
        if (name != null) 
            this.name = name;
	}
	public void addItem(String path) {
		items.add(path);
	}
	public String getName() {
		return name;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public ArrayList<String> getItems() {
		return items;
	}

}
