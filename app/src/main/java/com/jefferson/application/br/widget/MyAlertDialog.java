package com.jefferson.application.br.widget;

import androidx.appcompat.app.AlertDialog;
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
            DialogUtils.configureDialog(dialog);
            return dialog;
        }
    }
}
