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
