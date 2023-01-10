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
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.view.WindowManager.LayoutParams;
import com.jefferson.application.br.R;
import com.jefferson.application.br.fragment.ImagePreviewFragment;
import java.util.ArrayList;

public class ImagePreviewActivity extends MyCompatActivity {

    private ViewPager viewPager;
    private ArrayList<String> filepath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_player_layout);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        Intent intent = getIntent();
        int position = intent.getExtras().getInt("position");
        filepath = intent.getStringArrayListExtra("filepath");
        ImagePagerAdapter PagerAdapter = new ImagePagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(PagerAdapter);
        viewPager.setOffscreenPageLimit(4);
        viewPager.setPageMargin(20);
        viewPager.setCurrentItem(position);
        
        getWindow().addFlags(LayoutParams.FLAG_FULLSCREEN);
    }

    private class ImagePagerAdapter extends FragmentStatePagerAdapter {
        
        public ImagePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return filepath.size();
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return new ImagePreviewFragment(filepath.get(position));
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
