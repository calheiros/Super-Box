package com.jefferson.application.br.util;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import com.jefferson.application.br.App;
import com.jefferson.application.br.R;
//import android.support.v7.app.AlertDialog;

public class DialogUtils {

    public static void configureDialog(android.support.v7.app.AlertDialog dialog) {
        setWindowAttributes(dialog.getWindow());
    }
    
    private static void setWindowAttributes(Window window){
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setBackgroundDrawableResource(R.drawable.dialog_bg_inset);
        window.getAttributes().windowAnimations = R.style.DialogAnimation;
        
    }
    public static void configureDialog(AlertDialog dialog) {
        setWindowAttributes(dialog.getWindow());
    }
    
    public static int getTheme() {
       return ThemeConfig.getDialogTheme(App.getAppContext());
    }
    
}
