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

package com.jefferson.application.br.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.jefferson.application.br.R;

public class LockCheck extends androidx.appcompat.widget.AppCompatCheckBox {

    public LockCheck(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void setChecked(boolean checked) {
		
		if (checked) {
			this.setBackgroundResource(R.drawable.ic_lock);
		} else {
			this.setBackgroundResource(R.drawable.ic_lock_open_variant);
		}
      
		super.setChecked(checked);
	}
    
    public void animateView() {
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.checked);
        startAnimation(animation);
    }
    
    public void setCheckedNoAnim(boolean checked) {
        setChecked(checked);
    }
}
