package com.jefferson.application.br.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import com.jefferson.application.br.App;
import com.jefferson.application.br.R;
import com.jefferson.application.br.fragment.VideoPlayFragment;
import java.util.ArrayList;
import com.jefferson.application.br.util.Debug;

public class VideoPlayerActivity extends MyCompatActivity {

    public class MyPageListerner implements ViewPager.OnPageChangeListener {

        private VideoPagerAdapter adpter;
        private int lastFragmentIndex;

        public MyPageListerner(VideoPagerAdapter adapter, int position) {
            this.adpter = adapter;
            lastFragmentIndex = position;
        }

        @Override
        public void onPageScrolled(int p1, float p2, int p3) {
            Debug.toast("p2 = " + p1 + ", p3 = " + p3);
        }

        @Override
        public void onPageSelected(int position) {

            VideoPlayFragment fragment = pagerAdapter.getItem(position);
            VideoPlayFragment lastFragment = adpter.getItem(lastFragmentIndex);
            lastFragment.stop();
            fragment.start();
            lastFragmentIndex = position;
        }

        @Override
        public void onPageScrollStateChanged(int p1) {

        }

    }
    private VideoPlayerActivity.VideoPagerAdapter pagerAdapter;
    private ViewPager viewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.video_view_activity);
        Intent intent = getIntent();
        int position = intent.getExtras().getInt("position");
        ArrayList<String> filesPath = intent.getStringArrayListExtra("filepath");

        pagerAdapter = new VideoPagerAdapter(getSupportFragmentManager(), filesPath);
        viewPager = findViewById(R.id.video_view_pager);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOnPageChangeListener(new MyPageListerner(pagerAdapter, position));
        viewPager.setCurrentItem(position);
        pagerAdapter.getItem(position).setPlayOnCreate(true);
    }

    private void requestOrientation(int width, int height) {

        int orientation = width > height ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        setRequestedOrientation(orientation);
    }

    public void window() {

        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        int bits = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        winParams.flags |=  bits;
        win.setAttributes(winParams);
    }

    private class VideoPagerAdapter extends FragmentPagerAdapter {

        private ArrayList<String> filesPath;
        private VideoPlayFragment fragments[];

        public VideoPagerAdapter(FragmentManager fm, ArrayList<String> paths) {

            super(fm);
            this.filesPath = paths;
            this.fragments = new VideoPlayFragment[paths.size()];
        }

        @Override
        public VideoPlayFragment getItem(int position) {

            if (fragments[position] == null) {
                fragments[position] = new VideoPlayFragment(filesPath.get(position));
                Log.i("VideoPlayerActivy", "fragment created => " + position);
            }
            return fragments[position];
        }

        @Override
        public int getCount() {
            return filesPath.size();
        }
    }

    @Override
    public void onBackPressed() {
        
        Intent intent = new Intent();
        intent.putExtra("index", viewPager.getCurrentItem());
        setResult(RESULT_OK,intent);
       
        super.onBackPressed();
        
    }
}
