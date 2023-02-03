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

package com.jefferson.application.br.util;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import com.jefferson.application.br.R;
//import android.support.v7.app.AlertDialog;

public class DialogUtils {

    public static void configureDialog(androidx.appcompat.app.AlertDialog dialog) {
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
    
    public static int getTheme(Context context) {
       return ThemeConfig.getDialogTheme(context);
    }
    
}
