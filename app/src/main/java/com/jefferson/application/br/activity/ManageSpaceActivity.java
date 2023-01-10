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
