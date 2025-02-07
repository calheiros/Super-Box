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

class MediaModel : Parcelable {
    var path: String? = null
    var duration: String? = null

    private constructor(p: Parcel) {
        path = p.readString()
        duration = p.readString()
    }

    constructor(path: String?) {
        this.path = path
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(path)
        parcel.writeString(duration)
    }

    override fun toString(): String {
        return path!!
    }

    companion object {
        @JvmField
        val CREATOR: Creator<MediaModel?> = object : Creator<MediaModel?> {
            override fun createFromParcel(`in`: Parcel): MediaModel {
                return MediaModel(`in`)
            }

            override fun newArray(size: Int): Array<MediaModel?> {
                return arrayOfNulls(size)
            }
        }
    }
}