package com.jefferson.application.br.util;

import android.content.Context;
import com.jefferson.application.br.R;
import android.graphics.drawable.ColorDrawable;
import android.graphics.Color;
import android.app.AlertDialog;
//import android.support.v7.app.AlertDialog;

public class DialogUtils {

    public static void configureRoudedDialog(android.support.v7.app.AlertDialog dialog) {
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.alert_background);
    }

    public static void configureRoudedDialog(AlertDialog dialog) {
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.alert_background);
    }

    public static AlertDialog createRoundedDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomAlertDialog);
        AlertDialog dialog = builder.create();
        configureRoudedDialog(dialog);
        return dialog;
    }
}
