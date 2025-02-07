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


import android.app.ActivityOptions;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class MyAnimationUtils {

    public static void expand(final View v) {
        v.measure(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT); 
        final int targetHeight = v.getMeasuredHeight(); // Older versions of android (pre API 21) cancel animations for views with a height of 0. 
        v.getLayoutParams().height = 1; 
        v.setVisibility(View.VISIBLE); 
        Animation a = new Animation() { 
            @Override 
            protected void applyTransformation(float interpolatedTime, Transformation t) { 
                v.getLayoutParams().height = interpolatedTime == 1 ? WindowManager.LayoutParams.WRAP_CONTENT : (int)(targetHeight * interpolatedTime);
                v.requestLayout(); 
            } @Override public boolean willChangeBounds() { 
                return true; }
        }; // 1dp/ms
        a.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a); 
    }
    
    public static void collapse(final View v) { 
        final int initialHeight = v.getMeasuredHeight(); 
        Animation a = new Animation() { 
            @Override protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    v.setVisibility(View.GONE); 
                } else { 
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime); 
                    v.requestLayout(); }
            }
            @Override public boolean willChangeBounds() {
                return true; 
            }
        }; // 1dp/ms 
        a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density)); v.startAnimation(a);
    } 
}
