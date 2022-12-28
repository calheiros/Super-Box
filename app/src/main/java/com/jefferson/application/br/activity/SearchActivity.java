package com.jefferson.application.br.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.jefferson.application.br.R;
import com.jefferson.application.br.adapter.SearchViewAdapter;
import com.jefferson.application.br.model.SimplifiedAlbum;

import java.util.ArrayList;

public class SearchActivity extends MyCompatActivity implements AdapterView.OnItemClickListener {
    public static final String EXTRA_SIMPLE_MODELS = "simple_models_key";
    private SearchViewAdapter adapter;
    private InputMethodManager input;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity_layout);
        ArrayList<SimplifiedAlbum> models = getIntent().getParcelableArrayListExtra(EXTRA_SIMPLE_MODELS);
        configureSearchView(models);

    }
    private void configureSearchView(ArrayList<SimplifiedAlbum> models) {
        ListView listView = findViewById(R.id.items_list_view);
        editText = findViewById(R.id.search_edit_text);
        input = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        editText.requestFocus();
        input.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        adapter = new SearchViewAdapter(models,this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        // atualize a lista de itens quando o texto do EditText for alterado
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // filtre a lista de itens com base no texto digitado pelo usuário
                //adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
                adapter.filter(s.toString());
            }
            // implemente os outros métodos de TextWatcher aqui
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        editText.clearFocus();
        input.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        SimplifiedAlbum choice = adapter.getItem(position);
        Intent intent = new Intent();
        intent.putExtra("result", choice.getName());
        setResult(RESULT_OK, intent);
        finish();
    }
}
