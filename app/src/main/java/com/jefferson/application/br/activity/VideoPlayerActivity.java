package com.jefferson.application.br.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import com.jefferson.application.br.R;
import com.jefferson.application.br.fragment.VideoPlayerFragment;
import java.util.ArrayList;
import android.view.View.OnSystemUiVisibilityChangeListener;

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
            VideoPlayerFragment lastFragment = pagerAdapter.getItem(lastFragmentPosition);

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
        fullscreen();
        pagerAdapter = new VideoPagerAdapter(getSupportFragmentManager(), filesPath);
        viewPager = (ViewPager) findViewById(R.id.video_view_pager);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOnPageChangeListener(new MyPageListerner(pagerAdapter, choice));
        viewPager.setCurrentItem(choice);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setPageMargin(dpToPx(5));
        pagerAdapter.getItem(choice).setPlayOnCreate(true);
        //hideNavigationBar();
        viewPager.requestFocus();
        // applyParentViewPadding(viewPager);
    }

    @Override
    protected void onApplyCustomTheme() {
        //do nothing
    }
    
    public int dpToPx(int dp) { 
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics(); 
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)); 
    }

    private void requestOrientation(int width, int height) {
        int orientation = width > height ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        setRequestedOrientation(orientation);
    }
    
    void hideNavigationBar(){
        View decorView = getWindow().getDecorView();
        // Hide both the navigation bar and the status bar.
         // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
        // a general rule, you should design your app to hide the status bar whenever you
        // hide the navigation bar.
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
             | View.SYSTEM_UI_FLAG_FULLSCREEN;
         decorView.setSystemUiVisibility(uiOptions);
    }
    
    public void fullscreen() {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        int bits = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        winParams.flags |=  bits;
        win.setAttributes(winParams);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private class VideoPagerAdapter extends FragmentStatePagerAdapter {

        private ArrayList<String> filesPath;
        private VideoPlayerFragment[] fragments;
        
        public VideoPagerAdapter(FragmentManager fm, ArrayList<String> paths) {
            super(fm);
            this.filesPath = paths;
            this.fragments = new VideoPlayerFragment[paths.size()];
        }

        @Override
        public VideoPlayerFragment getItem(int position) {
            VideoPlayerFragment fragment = fragments[position];

            if (fragment == null) {
                return fragments[position] = new VideoPlayerFragment(filesPath.get(position));
            }
            return fragment;
        }

//        public VideoPlayFragment getCachedFragment() {
//            return cachedFragment;
//       }

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
