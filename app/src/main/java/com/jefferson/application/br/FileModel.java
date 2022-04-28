package com.jefferson.application.br;

public class FileModel {
	
    public static final String IMAGE_TYPE = "imagem";
    public static final String VIDEO_TYPE = "video";
   
    private String source_path;
    private String type;
    private String parentPath = null;
    
    public String getParentPath() {
        return parentPath;
    }

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
