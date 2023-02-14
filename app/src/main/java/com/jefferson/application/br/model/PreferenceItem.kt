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
package com.jefferson.application.br.model

class PreferenceItem {
    var title: String? = null
    var type = 0
    var description: String? = null
    var iconResId = 0
    var id: ID? = null
    var checked = false

    enum class ID {
        LANGUAGE, STORAGE, APP_THEME, PASSWORD, APP_ICON, DIALER_CODE, SCREENSHOT, ABOUT, FINGERPRINT
    }

    constructor(id: ID?) {
        this.id = id
    }

    constructor()
    constructor(name: String?, type: Int, description: String?, id: ID?) {
        title = name
        this.type = type
        this.description = description
        this.id = id
    }

    companion object {
        const val ITEM_SWITCH_TYPE = 3
        const val ITEM_TYPE = 2
        const val SECTION_TYPE = 1
    }
}