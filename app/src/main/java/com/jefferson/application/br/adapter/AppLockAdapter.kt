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
package com.jefferson.application.br.adapter

import android.app.Activity
import android.content.Context
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.jefferson.application.br.R
import com.jefferson.application.br.database.AppLockDatabase
import com.jefferson.application.br.model.AppModel
import com.jefferson.application.br.util.JDebug
import com.jefferson.application.br.widget.LockCheck

@Suppress("NAME_SHADOWING")
class AppLockAdapter(private val activity: Activity, var models: ArrayList<AppModel>) :
    BaseAdapter() {
    private var selectionArray = SparseBooleanArray()
    var inflater: LayoutInflater
    var database: AppLockDatabase = AppLockDatabase(activity)
    var isMutable = false
    private val cachedViews: HashMap<Int, View?> = HashMap()
    private var searchedItemPosition = -1

    init {
        syncSelection()
        inflater = activity
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    fun animateSearchedItem(x: Int) {
        val view = cachedViews[x]
        if (view != null) {
            view.startAnimation(blinkAnimation)
        } else {
            JDebug.toast("View is NULL")
        }
    }

    private val blinkAnimation: Animation
        get() = AnimationUtils.loadAnimation(activity, R.anim.blink)

    fun setSearchedItem(x: Int) {
        searchedItemPosition = x
    }

    fun clear() {
        models.clear()
        selectionArray.clear()
        cachedViews.clear()
        notifyDataSetInvalidated()
    }

    fun putDataSet(newModels: ArrayList<AppModel>) {
        models = newModels
        syncSelection()
        notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var convertView = convertView
        val holder: Holder?

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item, parent, false)
            holder = Holder()
            holder.imageView = convertView.findViewById<View>(R.id.iconeApps) as ImageView
            holder.textView = convertView.findViewById<View>(R.id.app_name) as TextView
            holder.lockCheck = convertView.findViewById<View>(R.id.check1) as LockCheck
            convertView.tag = holder
        } else {
            holder = convertView.tag as Holder
        }
        val info = models[position]
        val icon = info.icon
        holder.imageView?.setImageDrawable(icon)
        holder.textView?.text = info.packageName
        holder.lockCheck?.isChecked = selectionArray[position, false]

        val animation = AnimationUtils.loadAnimation(activity, R.anim.zoom_in)
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p1: Animation) {}
            override fun onAnimationEnd(anim: Animation) {
                animateIfSearchedItem(position)
            }

            override fun onAnimationRepeat(p1: Animation) {}
        }
        )
        convertView?.startAnimation(animation)
        cachedViews[position] = convertView
        return convertView
    }

    val itemHeight: Int
        get() {
            val item = cachedViews[0]
            return item?.height ?: 0
        }

    internal class Holder {
        var imageView: ImageView? = null
        var textView: TextView? = null
        var lockCheck: LockCheck? = null
    }

    private fun animateIfSearchedItem(position: Int) {
        val view = cachedViews[position]
            ?: return  //JDebug.toast("animateIfSearched: View is NULL");
        if (searchedItemPosition == position) {
            view.post {
                val blinkAnim: Animation = blinkAnimation
                //anim.setDuration(1000);
                blinkAnim.startTime = 1000
                view.startAnimation(blinkAnim)
                searchedItemPosition = -1
            }
        }
    }

    private fun syncSelection() {
        val lockedPackages = database.lockedPackages
        for (i in 0 until count) {
            val model = models[i]
            if (lockedPackages.contains(model.name)) selectionArray.put(i, true)
        }
    }

    fun toggleSelection(position: Int) {
        isMutable = true
        val name = models[position].name
        val hasSelected = selectionArray[position, false]
        if (hasSelected) {
            selectionArray.delete(position)
            database.removeLockedApp(name)
        } else {
            selectionArray.put(position, true)
            database.addLockedApp(name)
        }
        //        LockCheck lockView = view.findViewById(R.id.check1);
//        lockView.setChecked(!hasSelected);
    }

    override fun getCount(): Int {
        return models.size
    }

    override fun getItem(position: Int): Any {
        return models[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
}