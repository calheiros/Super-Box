package com.jefferson.application.br.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;

import com.jefferson.application.br.R;

public class SearchActivity extends MyCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity_layout);
        configureSearchView();

    }
    private void configureSearchView() {
        ListView listView = findViewById(R.id.items_list_view);
        EditText editText = findViewById(R.id.search_edit_text);
        editText.requestFocus();
        // crie um adapter personalizado para o ListView

        String[] items = {"carro", "pasta", "dedo"};
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, items);
        listView.setAdapter(adapter);
        // atualize a lista de itens quando o texto do EditText for alterado
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // filtre a lista de itens com base no texto digitado pelo usuário
                adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {
                    listView.setVisibility(View.GONE);
                } else {
                    listView.setVisibility (View.VISIBLE);
                }
            }
            // implemente os outros métodos de TextWatcher aqui
        });
    }

}
