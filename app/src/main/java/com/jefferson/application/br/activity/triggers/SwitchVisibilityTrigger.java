package com.jefferson.application.br.activity.triggers;

import android.view.View;

public class SwitchVisibilityTrigger {

    private final View view;

    public SwitchVisibilityTrigger (View view) {
       this.view = view;
    }

    public void switchVisibility() {
        int visibility = getVisibility() == View.VISIBLE? View.GONE:View.VISIBLE;
        view.setVisibility(visibility);
    }

    public int getVisibility() {
        return view.getVisibility();
    }
}
