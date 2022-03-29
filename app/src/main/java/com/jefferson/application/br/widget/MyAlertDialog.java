package com.jefferson.application.br.widget;

import android.support.v7.app.AlertDialog;
import android.content.Context;
import com.jefferson.application.br.util.DialogUtils;

public class MyAlertDialog extends AlertDialog {

    public MyAlertDialog(Context context) {
        super(context);
    }

    public static class Builder extends AlertDialog.Builder {
        public Builder(Context context) {
            super(context);
        }
        
        public Builder(Context context, int styleset){
            super(context, styleset);
        }
        
        @Override
        public AlertDialog create() {
            AlertDialog dialog = super.create();
            DialogUtils.configureRoudedDialog(dialog);
            return dialog;
        }
    }
}
