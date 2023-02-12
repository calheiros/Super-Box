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

class SimplifiedAlbum : Parcelable {
    var name: String
    var thumbnailPath: String
        private set

    constructor(name: String, thumbnail: String) {
        this.name = name
        thumbnailPath = thumbnail
    }

    private constructor(parcel: Parcel) {
        name = parcel.readString() as String
        thumbnailPath = parcel.readString() as String
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(name)
        dest.writeString(thumbnailPath)
    }

    companion object {
        @JvmField
        val CREATOR: Creator<SimplifiedAlbum?> = object : Creator<SimplifiedAlbum?> {
            override fun createFromParcel(`in`: Parcel): SimplifiedAlbum {
                return SimplifiedAlbum(`in`)
            }

            override fun newArray(size: Int): Array<SimplifiedAlbum?> {
                return arrayOfNulls(size)
            }
        }

        @JvmStatic
        fun createFrom(item: FolderModel): SimplifiedAlbum {
            val itemsPath = item.itemsPath
            val name = item.name
            val thumb = if (itemsPath.size > 0) itemsPath[0] else ""
            return SimplifiedAlbum(name, thumb)
        }
    }
}