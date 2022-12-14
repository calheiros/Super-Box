package com.jefferson.application.br.util;

import android.content.Context;
import android.os.Build;

import eightbitlab.com.blurview.BlurAlgorithm;
import eightbitlab.com.blurview.RenderEffectBlur;
import eightbitlab.com.blurview.RenderScriptBlur;

public class BlurViewUtils
{
        public static BlurAlgorithm getRenderAlgorithm(Context context) {
           return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) ?
                    new RenderEffectBlur() : new RenderScriptBlur(context);
        }
}
