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
import android.view.View;
import android.view.WindowManager.LayoutParams;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.jefferson.application.br.R;
import com.jefferson.application.br.app.SimpleDialog;
import com.jefferson.application.br.fragment.ImagePreviewFragment;

import java.util.ArrayList;

public class ImagePreviewActivity extends MyCompatActivity implements View.OnClickListener {

    private ViewPager viewPager;
    private ArrayList<String> filepath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_player_layout);
        viewPager = findViewById(R.id.view_pager);
        View deleteButton = findViewById(R.id.delete_imageview);
        View exportButton = findViewById(R.id.export_imageview);
        Intent intent = getIntent();
        int position = intent.getExtras().getInt("position");
        filepath = intent.getStringArrayListExtra("filepath");
        final View optionLayout = findViewById(R.id.options_layout);
        ImagePagerAdapter PagerAdapter = new ImagePagerAdapter(getSupportFragmentManager(), optionLayout);

        viewPager.setAdapter(PagerAdapter);
        viewPager.setOffscreenPageLimit(4);
        viewPager.setPageMargin(20);
        viewPager.setCurrentItem(position);
        viewPager.setOnClickListener(this);
        exportButton.setOnClickListener(this);
        deleteButton.setOnClickListener(this);

        getWindow().addFlags(LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.delete_imageview) {
            dialogDeletionConfirmation();
            return;
        }

        if (id == R.id.export_imageview) {
            exportImage();
        }
    }

    private void dialogDeletionConfirmation() {
        SimpleDialog builder = new SimpleDialog(this, SimpleDialog.STYLE_ALERT_HIGH);
        builder.setTitle(getString(R.string.apagar));
        builder.setMessage(getString(R.string.apagar_image_mensagem));
        builder.setPositiveButton(getString(android.R.string.yes), new SimpleDialog.OnDialogClickListener() {

            @Override
            public boolean onClick(SimpleDialog dialog) {
                return true;
            }
        });
        builder.setNegativeButton(getString(android.R.string.cancel), null);
        builder.show();
    }

    private void exportImage() {

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("index", viewPager.getCurrentItem());
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

    private class ImagePagerAdapter extends FragmentStatePagerAdapter {
        private final View optionsLayout;
        public ImagePagerAdapter(FragmentManager fm, View optionsLayout) {
            super(fm);
            this.optionsLayout = optionsLayout;
        }

        @Override
        public int getCount() {
            return filepath.size();
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return new ImagePreviewFragment(filepath.get(position) , optionsLayout);
        }
    }
}
