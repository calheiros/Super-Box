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
import kotlin.Comparator

class SimpleAlbumModel : Parcelable {
    var albumName: String
    var thumbnailPath: String? = ""
    var itemCount: Int = 0
    var albumPath: String = ""
    var isFavorite: Boolean = false
    constructor(name: String, albumPath: String) {
        this.albumName = name
        this.albumPath = albumPath
    }
    constructor(name: String, thumbnail: String, count: Int) {
        this.albumName = name
        thumbnailPath = thumbnail
        this.itemCount = count
    }
    private constructor(parcel: Parcel) {
        albumName = parcel.readString() as String
        thumbnailPath = parcel.readString() as String
        itemCount = parcel.readInt()
        albumPath = parcel.readString() as String
        isFavorite = parcel.readInt() == 1
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(albumName)
        dest.writeString(thumbnailPath)
        dest.writeInt(itemCount)
        dest.writeString(albumName)
        dest.writeInt(if(isFavorite) 1 else 0)
    }

    companion object {
        @JvmField
        val CREATOR: Creator<SimpleAlbumModel?> = object : Creator<SimpleAlbumModel?> {
            override fun createFromParcel(`in`: Parcel): SimpleAlbumModel {
                return SimpleAlbumModel(`in`)
            }

            override fun newArray(size: Int): Array<SimpleAlbumModel?> {
                return arrayOfNulls(size)
            }
        }

        @JvmStatic
        fun createFrom(item: AlbumModel): SimpleAlbumModel {
            val itemsPath = item.itemsPath
            val name = item.name
            val thumb = if (itemsPath.size > 0) itemsPath[0] else ""
            return SimpleAlbumModel(name, thumb, item.itemsPath.size)
        }
        fun sort(models: ArrayList<SimpleAlbumModel>?) {
            if (models == null) return
            models.sortWith(Comparator { f1: SimpleAlbumModel, f2: SimpleAlbumModel ->
                if (f1.isFavorite && !f2.isFavorite) return@Comparator -1
                if (f2.isFavorite && !f1.isFavorite) return@Comparator 1
                f1.albumName.lowercase(Locale.getDefault())
                    .compareTo(f2.albumName.lowercase(Locale.getDefault()))
            })
        }
    }
}