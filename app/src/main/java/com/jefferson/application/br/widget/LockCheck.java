package com.jefferson.application.br.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import com.jefferson.application.br.R;

public class LockCheck extends androidx.appcompat.widget.AppCompatCheckBox {

    public LockCheck(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

    public void setCheckedNoAnimation(boolean p0) {
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
        //animateCheckView(this);
    }
}
