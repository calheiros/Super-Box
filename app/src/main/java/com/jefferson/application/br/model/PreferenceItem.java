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

public class PreferenceItem {

	public String title;
	public int type;
	public String description;
    public int icon_res_id;
	public static final int ITEM_SWITCH_TYPE = 3;
	public static final int ITEM_TYPE = 2;
	public static final int SECTION_TYPE = 1;
    public ID id;

    public boolean checked;

	public static enum ID {
        LANGUAGE,
        STORAGE,
        APP_THEME,
        PASSWORD,
        APP_ICON,
        DIALER_CODE,
        SCREENSHOT,
        ABOUT, FINGERPRINT,
    }
    
    public PreferenceItem(ID id) {
        this.id = id;
    }

    public PreferenceItem() {
        
    }

	public PreferenceItem(String name, int type, String description, ID id) {
		this.title = name;
		this.type = type;
		this.description = description;
        this.id = id;
	}
}
