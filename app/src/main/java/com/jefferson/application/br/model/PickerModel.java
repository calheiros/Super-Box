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
