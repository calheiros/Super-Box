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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jefferson.application.br.MaterialLockView
import com.jefferson.application.br.MaterialLockView.OnPatternListener
import com.jefferson.application.br.R
import com.jefferson.application.br.util.PasswordManager

class ManageSpaceActivity : AppCompatActivity() {
    private var passwd = ""
    private var patternLayout: View? = null
    private lateinit var pattern: MaterialLockView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manage_space_layout)
        pattern = findViewById(R.id.pattern)
        patternLayout = findViewById(R.id.pattern_view)
        passwd = PasswordManager().internalPassword
        pattern.setOnPatternListener(PatternListener())
        Toast.makeText(this, "ACTION: " + intent.action, Toast.LENGTH_LONG).show()
    }

    private inner class PatternListener : OnPatternListener() {

        override fun onPatternDetected(cells: List<MaterialLockView.Cell>, SimplePattern: String) {
            super.onPatternDetected(cells, SimplePattern)
            if (SimplePattern == passwd) {
                patternLayout!!.visibility = View.GONE
            } else {
                pattern.displayMode = MaterialLockView.DisplayMode.Wrong
            }
        }
    }
}