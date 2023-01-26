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

import androidx.recyclerview.widget.RecyclerView

abstract class SelectableAdapter<VH : RecyclerView.ViewHolder?> : RecyclerView.Adapter<VH>() {
    private val selectedItems: LinkedHashMap<Int, Boolean> = LinkedHashMap()

    /**
     * Indicates if the item at position position is selected
     * @param position Position of the item to check
     * @return true if the item is selected, false otherwise
     */
    fun isSelected(position: Int): Boolean {
        return getSelectedItems().contains(position)
    }

    /**
     * Toggle the selection status of the item at a given position
     * @param position Position of the item to toggle the selection status for
     */
    protected fun toggleSelection(position: Int) {
        if (java.lang.Boolean.TRUE == selectedItems[position]) {
            selectedItems.remove(position)
        } else {
            selectedItems[position] = true
        }
        notifyItemChanged(position)
    }

    val selectedItemsHash: HashMap<Int, Boolean>
        get() = selectedItems

    fun getSelectedItemPosition(position: Int): Int {
        val keys: Array<Int> = selectedItems.keys.toTypedArray()
        for (i in keys.indices) {
            if (keys[i] == position) return i
        }
        return -1
    }

    /**
     * Clear the selection status for all items
     */
    fun clearSelection() {
        val selection = getSelectedItems()
        selectedItems.clear()
        for (i in selection) {
            notifyItemChanged(i)
        }
    }

    /**
     * Count the selected items
     * @return Selected items count
     */
    val selectedItemCount: Int
        get() = selectedItems.size

    /**
     * Indicates the list of selected items
     * @return List of selected items ids
     */
    fun getSelectedItems(): List<Int> {
        return ArrayList(selectedItems.keys)
    }
}