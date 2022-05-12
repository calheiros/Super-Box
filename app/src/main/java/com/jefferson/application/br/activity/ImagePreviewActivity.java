package com.jefferson.application.br.activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
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
        setContentView(R.layout.view_pager_layout);
        viewPager = findViewById(R.id.view_pager);
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
