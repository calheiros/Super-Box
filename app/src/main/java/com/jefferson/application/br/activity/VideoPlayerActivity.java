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
import com.jefferson.application.br.util.JDebug;
import com.jefferson.application.br.model.MediaModel;
import android.support.v4.app.FragmentStatePagerAdapter;

public class VideoPlayerActivity extends MyCompatActivity {

    public class MyPageListerner implements ViewPager.OnPageChangeListener {

        private VideoPagerAdapter adpter;
        private int lastFragmentPosition;

        public MyPageListerner(VideoPagerAdapter adapter, int position) {
            this.adpter = adapter;
            lastFragmentPosition = position;
        }

        @Override
        public void onPageScrolled(int p1, float p2, int p3) {
            //Debug.toast("p2 = " + p1 + ", p3 = " + p3);
        }

        @Override
        public void onPageSelected(int position) {
            VideoPlayFragment lastFragment = pagerAdapter.getItem(lastFragmentPosition);
           
            if (lastFragment != null) {
                lastFragment.stop();            
            }

            lastFragmentPosition = position;
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

    private VideoPlayerActivity.VideoPagerAdapter pagerAdapter;
    private ViewPager viewPager;
    private int choice;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.video_view_activity);
        Intent intent = getIntent();
        choice = intent.getExtras().getInt("position");
        ArrayList<String> filesPath = intent.getStringArrayListExtra("filepath");

        pagerAdapter = new VideoPagerAdapter(getSupportFragmentManager(), filesPath);
        viewPager = findViewById(R.id.video_view_pager);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOnPageChangeListener(new MyPageListerner(pagerAdapter, choice));
        viewPager.setCurrentItem(choice);
        viewPager.setOffscreenPageLimit(3);
        pagerAdapter.getItem(choice).setPlayOnCreate(true);
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

    private class VideoPagerAdapter extends FragmentStatePagerAdapter {

        private ArrayList<String> filesPath;
        private VideoPlayFragment[] fragments;

        public VideoPagerAdapter(FragmentManager fm, ArrayList<String> paths) {
            super(fm);
            this.filesPath = paths;
            this.fragments = new VideoPlayFragment[paths.size()];
        }

        @Override
        public VideoPlayFragment getItem(int position) {
            VideoPlayFragment fragment = fragments[position];

            if (fragment == null) {
               return fragments[position] = new VideoPlayFragment(filesPath.get(position));
               
             }
             
            return fragment;
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
        setResult(RESULT_OK, intent);

        super.onBackPressed();

    }
}
