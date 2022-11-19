package com.jefferson.application.br.activity;

/*import android.content.*;
import android.content.pm.*;
import android.os.*;
import android.support.v4.app.*;
import android.support.v4.view.*;
import android.view.WindowManager.*;
import android.widget.*;
import com.facebook.drawee.backends.pipeline.*;
import com.jefferson.application.br.*;
import java.util.*;

import com.jefferson.application.br.R;
import com.liuguangqiang.swipeback.*;

public class Visualizar_Imagem extends SwipeBackActivity
{
	final static int mOrientationValues[] = new int[] {ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,};
	int position;
	ProgressBar progres;
	ArrayList<String>  filepath ;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Fresco.initialize(this);
		setContentView(R.layout.image_detail_pager);
        setDragEdge(SwipeBackLayout.DragEdge.TOP);
		Intent i = getIntent();

	    position = i.getExtras().getInt("position");
		filepath = i.getStringArrayListExtra("filepath");

		ViewPager pager = (ViewPager)findViewById(R.id.pager);
		ImagePagerAdapter PagerAdapter = new ImagePagerAdapter(getSupportFragmentManager(), filepath.size());
		pager.setAdapter(PagerAdapter);
		pager.setOffscreenPageLimit(4);
		pager.setPageMargin(20);
		pager.setCurrentItem(position);

		getWindow().addFlags(LayoutParams.FLAG_FULLSCREEN);
	}
	
	@Override
	protected void onStop()
	{
		super.onStop();
		if (!isFinishing())
		{
			CodeManager.finishAllActivity(this);
		}
	}
	
	private class ImagePagerAdapter extends FragmentStatePagerAdapter
	{
        private final int mSize;

        public ImagePagerAdapter(FragmentManager fm, int size)
		{
            super(fm);
            mSize = size;
        }

        @Override
        public int getCount()
		{
            return mSize;
        }

        @Override
        public Fragment getItem(int position)
		{
            return ImageDetailFragment.newInstance(filepath.get(position));
        }
    }
}*/
	
	
