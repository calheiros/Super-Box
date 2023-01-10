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

package com.jefferson.application.br.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import com.jefferson.application.br.R;
import com.jefferson.application.br.fragment.VideoPlayerFragment;
import java.util.ArrayList;

public class VideoPlayerActivity extends MyCompatActivity {

    private VideoPlayerActivity.VideoPagerAdapter pagerAdapter;
    private ViewPager viewPager;

    public class MyPageListener implements ViewPager.OnPageChangeListener {

        private int lastFragmentPosition;

        public MyPageListener(int position) {
            lastFragmentPosition = position;
        }

        @Override
        public void onPageScrolled(int p1, float p2, int p3) {
        }

        @Override
        public void onPageSelected(int position) {
            VideoPlayerFragment lastFragment = pagerAdapter.getItem(lastFragmentPosition);
            lastFragment.stop();
            lastFragmentPosition = position;

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

    @Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_player_layout);
        Intent intent = getIntent();
        int choice = intent.getExtras().getInt("position");
        ArrayList<String> filesPath = intent.getStringArrayListExtra("filepath");
        fullscreen();
        pagerAdapter = new VideoPagerAdapter(getSupportFragmentManager(), filesPath);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOnPageChangeListener(new MyPageListener(choice));
        viewPager.setCurrentItem(choice);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setPageMargin(dpToPx(5));
        pagerAdapter.getItem(choice).setPlayOnCreate(true);

        viewPager.requestFocus();
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

    private static class VideoPagerAdapter extends FragmentStatePagerAdapter {

        private final ArrayList<String> filesPath;
        private final VideoPlayerFragment[] fragments;
        
        public VideoPagerAdapter(FragmentManager fm, ArrayList<String> paths) {
            super(fm);
            this.filesPath = paths;
            this.fragments = new VideoPlayerFragment[paths.size()];
        }

        @NonNull
        @Override
        public VideoPlayerFragment getItem(int position) {
            VideoPlayerFragment fragment = fragments[position];

            if (fragment == null) {
                return fragments[position] = new VideoPlayerFragment(filesPath.get(position));
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
