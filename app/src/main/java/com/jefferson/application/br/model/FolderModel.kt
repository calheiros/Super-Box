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

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import java.util.*

class FolderModel : Parcelable {
    var name = NO_FOLDER_NAME
    var path: String? = ""
    var isFavorite = false
    val items = ArrayList<MediaModel>()

    constructor() {}
    private constructor(p: Parcel) {
        path = p.readString()
        p.readTypedList(items as List<MediaModel?>, MediaModel.CREATOR)
    }

    val tag: String?
        get() = null

    fun addItem(model: MediaModel) {
        items.add(model)
    }

    fun getModelByPath(path: String?): MediaModel? {
        if (path == null) return null
        for (model in items) {
            if (path == model.path) return model
        }
        return null
    }

    val itemsPath: ArrayList<String>
        get() {
            val list = ArrayList<String>()
            for (model in items) {
                list.add(model.path as String)
            }
            return list
        }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(path)
        parcel.writeTypedList(items)
    }

    companion object {
        const val NO_FOLDER_NAME = "0"
        @JvmField
        val CREATOR: Creator<FolderModel?> = object : Creator<FolderModel?> {
            override fun createFromParcel(`in`: Parcel): FolderModel {
                return FolderModel(`in`)
            }

            override fun newArray(size: Int): Array<FolderModel?> {
                return arrayOfNulls(size)
            }
        }

        fun sort(models: ArrayList<FolderModel>?) {
            if (models == null) return
            models.sortWith(Comparator { f1: FolderModel, f2: FolderModel ->
                if (f1.isFavorite && !f2.isFavorite) return@Comparator -1
                if (f2.isFavorite && !f1.isFavorite) return@Comparator 1
                f1.name.lowercase(Locale.getDefault())
                    .compareTo(f2.name.lowercase(Locale.getDefault()))
            })
        }
    }
}