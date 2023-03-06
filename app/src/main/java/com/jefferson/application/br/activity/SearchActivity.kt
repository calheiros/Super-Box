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
package com.jefferson.application.br.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.EditText
import android.widget.ListView
import com.jefferson.application.br.R
import com.jefferson.application.br.adapter.SearchViewAdapter
import com.jefferson.application.br.model.SimplifiedAlbum

class SearchActivity : MyCompatActivity(), OnItemClickListener, OnItemLongClickListener {
    private lateinit var adapter: SearchViewAdapter
    private lateinit var input: InputMethodManager
    private lateinit var editText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_activity_layout)
        val models = intent.getParcelableArrayListExtra<SimplifiedAlbum>(EXTRA_SIMPLE_MODELS)
        if (models?.isEmpty() == true) {
            val hint = findViewById<View>(R.id.empty_hint_layout)
            hint.visibility = View.VISIBLE
            return
        }
        configureSearchView(models)
    }

    private fun configureSearchView(models: ArrayList<SimplifiedAlbum>?) {
        val listView = findViewById<ListView>(R.id.items_list_view)
        editText = findViewById(R.id.search_edit_text)
        input = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        editText.requestFocus()
        input.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        adapter = SearchViewAdapter(models!!, this)
        listView.adapter = adapter
        listView.onItemClickListener = this
        listView.onItemLongClickListener = this
        // atualize a lista de itens quando o texto do EditText for alterado
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // filtre a lista de itens com base no texto digitado pelo usuário
                //adapter.getFilter().filter(s);
            }

            override fun afterTextChanged(s: Editable) {
                adapter.filter(s.toString())
            } // implemente os outros métodos de TextWatcher aqui
        })
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
        sendActionAndFinish(ACTION_OPEN_ALBUM, position)
    }

    override fun onItemLongClick(
        parent: AdapterView<*>?, view: View, position: Int, id: Long
    ): Boolean {
        sendActionAndFinish(ACTION_GO_TO_ALBUM, position)
        return true
    }

    private fun sendActionAndFinish(action: String, position: Int) {
        editText.clearFocus()
        editText.getText()
        input.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
        val choice = adapter.getItem(position)
        val intent = Intent()
        intent.putExtra("result", choice.name)
        intent.action = action
        setResult(RESULT_OK, intent)
        finish()
    }

    companion object {
        const val ACTION_OPEN_ALBUM = "action_open_album"
        const val ACTION_GO_TO_ALBUM = "action_go_to_album"
        const val EXTRA_SIMPLE_MODELS = "simple_models_key"
    }
}