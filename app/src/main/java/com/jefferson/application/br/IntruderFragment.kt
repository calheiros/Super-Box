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
package com.jefferson.application.br

import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class IntruderFragment : AppCompatActivity() {
    var adapter: IntruderAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_intruder)
        initToolbar()
        val elements = getElements()
        if (elements.isEmpty()) {
            /*emptyLayout.setVisibility(View.VISIBLE);*/
        }
        val recyclerView = findViewById<RecyclerView>(R.id.mrecycler_view)
        val layoutManager = GridLayoutManager(this, 2)
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.layoutManager = layoutManager
        adapter = IntruderAdapter(elements, this)
        recyclerView.adapter = adapter
    }

    private fun getElements(): ArrayList<String> {
        val path = "/.application/data/Intruder"
        val dirs = File(Environment.getExternalStorageDirectory().absolutePath, path)
        dirs.mkdirs()
        val list = ArrayList<String>()
        val listDirs = dirs.listFiles()
        if (listDirs != null)
            for (listDir in listDirs) {
                list.add(listDir.absolutePath)
            }
        return list
    }

    private fun initToolbar() {
        val tool = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(tool)
        supportActionBar?.title = "intruder"
    }
}