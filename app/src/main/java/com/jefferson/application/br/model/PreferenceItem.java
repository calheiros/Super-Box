package com.jefferson.application.br.model;
import com.jefferson.application.br.fragment.SettingFragment;
import com.jefferson.application.br.model.PreferenceItem.ID;

public class PreferenceItem {

	public String title;
	public int type;
	public String description;
    public int icon_id;
	public static final int ITEM_SWITCH_TYPE = 3;
	public static final int ITEM_TYPE = 2;
	public static final int SECTION_TYPE = 1;
	public boolean isChecked;
    public ID id;

	public static final enum ID {
        LANGUAGE,
        STORAGE,
        APP_THEME,
        PASSWORD,
        APP_ICON,
        DIALER_CODE,
        ABOUT,
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
