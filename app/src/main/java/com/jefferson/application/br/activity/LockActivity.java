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
import androidx.appcompat.widget.Toolbar;
import android.widget.FrameLayout;
import com.jefferson.application.br.R;
import com.jefferson.application.br.fragment.LockFragment;

public class LockActivity extends MyCompatActivity {

    Toolbar toolbar;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		FrameLayout conteiner = (FrameLayout) findViewById(R.id.fragment_container);
		getLayoutInflater().inflate(R.layout.lock_activity, conteiner);
        int frameId = R.id.lock_FrameLayout_conteiner;

		if (conteiner.findViewById(frameId) != null) {
			getSupportFragmentManager().beginTransaction().add(frameId, new LockFragment()).commit();
		} else {
			return;
		}
		toolbar = (Toolbar) conteiner.findViewById(R.id.toolbar);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		if (toolbar != null) {
			setupToolbar();

		}
		super.onPostCreate(savedInstanceState);
	}

	private void setupToolbar() {
		setSupportActionBar(toolbar);
		getSupportActionBar().setTitle("Bloq. de aplicativos");
	}
}
