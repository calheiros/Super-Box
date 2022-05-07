package com.jefferson.application.br.activity;
import android.os.Bundle;
import com.jefferson.application.br.R;
import android.support.v4.view.ViewPager;

public class ImagePreviewActivity extends MyCompatActivity {
   
    private ViewPager viewPager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_pager_layout);
        viewPager = findViewById(R.id.view_pager);
    }
    
}
