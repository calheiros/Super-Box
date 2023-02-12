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

class FileModel : Parcelable {
    var resource: String? = null
    var type: String? = null
    var parentPath: String? = null
    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, key: Int) {
        parcel.writeString(resource)
        parcel.writeString(type)
        parcel.writeString(parentPath)
    }

    constructor() {}
    private constructor(parcel: Parcel) {
        resource = parcel.readString()
        type = parcel.readString()
        parentPath = parcel.readString()
    }

    companion object {
        const val IMAGE_TYPE = "imagem"
        const val VIDEO_TYPE = "video"
        @JvmField
        val CREATOR: Creator<FileModel?> = object : Creator<FileModel?> {
            override fun createFromParcel(`in`: Parcel): FileModel {
                return FileModel(`in`)
            }

            override fun newArray(size: Int): Array<FileModel?> {
                return arrayOfNulls(size)
            }
        }
    }
}