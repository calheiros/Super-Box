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

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.widget.Toolbar
import com.jefferson.application.br.R
import com.jefferson.application.br.model.AppModel

class LockActivity : MyCompatActivity() {
    var toolbar: Toolbar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = findViewById<View>(R.id.fragment_container) as FrameLayout
        layoutInflater.inflate(R.layout.lock_activity, container)
        val frameId = R.id.lock_FrameLayout_conteiner
        if (container.findViewById<View?>(frameId) != null) {
            //getSupportFragmentManager().beginTransaction().add(frameId, new LockFragment(this)).commit();
        } else {
            return
        }
        toolbar = container.findViewById<View>(R.id.toolbar) as Toolbar
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        if (toolbar != null) {
            setupToolbar(toolbar)
        }
        super.onPostCreate(savedInstanceState)
    }

    private fun setupToolbar(toolbar: Toolbar?) {
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.bloquear_apps)
    }
}