package com.jefferson.application.br.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.jefferson.application.br.MaterialLockView;
import com.jefferson.application.br.R;
import com.jefferson.application.br.util.PasswordManager;
import android.view.View;
import android.widget.Toast;
import java.util.List;

public class ManageSpaceActivity extends AppCompatActivity {

    public String passwd = "";
    private View patternLayout;
    MaterialLockView pattern;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_space_layout);
        pattern = findViewById(R.id.pattern);
        patternLayout = findViewById(R.id.pattern_view);
        passwd = new PasswordManager().getInternalPassword();
        pattern.setOnPatternListener(new PatternListener());
          Toast.makeText(this, "ACTION: " + getIntent().getAction(), Toast.LENGTH_LONG).show();
    }

    private class PatternListener extends MaterialLockView.OnPatternListener {
        @Override
        public void onPatternStart() {
            super.onPatternStart();

        }
        @Override
        public void onPatternDetected(List<MaterialLockView.Cell> cells, String SimplePattern) {
            super.onPatternDetected(cells, SimplePattern);
            if (SimplePattern.equals(passwd)) {
                patternLayout.setVisibility(View.GONE);
            } else {
                pattern.setDisplayMode(MaterialLockView.DisplayMode.Wrong);
            }
        }
    }
}
