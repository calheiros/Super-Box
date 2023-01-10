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

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;

import eightbitlab.com.blurview.BlurAlgorithm;
import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderEffectBlur;
import eightbitlab.com.blurview.RenderScriptBlur;

public class BlurUtils {
    public static BlurAlgorithm getRenderAlgorithm(Context context) {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) ?
                new RenderEffectBlur() : new RenderScriptBlur(context);
    }

    public static void blurBitmap(Bitmap bitmap, float radius, Context context) {
        RenderScript rs = RenderScript.create(context);
        //this will blur the bitmapOriginal with a radius and save it in bitmapOriginal
        final Allocation input = Allocation.createFromBitmap(rs, bitmap);
        //use this constructor for best performance, because it uses USAGE_SHARED mode which reuses memory
        final Allocation output = Allocation.createTyped(rs, input.getType());
        final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        script.setRadius(radius);
        script.setInput(input);
        script.forEach(output);
        output.copyTo(bitmap);
    }

    public static void setupWith(BlurView blurView, Activity activity, float radius) {

        View decorView = activity.getWindow().getDecorView();
        // ViewGroup you want to start blur from. Choose root as close to BlurView in hierarchy as possible.
        ViewGroup rootView = decorView.findViewById(android.R.id.content);
        Drawable windowBackground = decorView.getBackground();
        blurView.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
        blurView.setClipToOutline(true);
        blurView.setBlurAutoUpdate(true);
        BlurAlgorithm render = BlurUtils.getRenderAlgorithm(activity);
        blurView.setupWith(rootView, render) // or RenderEffectBlur
                .setFrameClearDrawable(windowBackground) // Optional
                .setBlurRadius(radius);
    }
}
