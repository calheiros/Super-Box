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

package com.jefferson.application.br;

import androidx.appcompat.widget.*;
import android.util.*;

import androidx.recyclerview.widget.RecyclerView;

import java.util.*;

public abstract class SelectableAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
    
	@SuppressWarnings("unused")

	private final LinkedHashMap<Integer,Boolean> selectedItems;
	public SelectableAdapter() {
		selectedItems = new LinkedHashMap<Integer, Boolean>();
	}
	/**
	 * Indicates if the item at position position is selected
	 * @param position Position of the item to check
	 * @return true if the item is selected, false otherwise
	 */
	public boolean isSelected(int position) {
		return getSelectedItems().contains(position);
	}

	/**
	 * Toggle the selection status of the item at a given position
	 * @param position Position of the item to toggle the selection status for
	 */
	protected void toggleSelection(int position) {
		if (Boolean.TRUE.equals(selectedItems.get(position))) {
			selectedItems.remove(position);
		} else {
			selectedItems.put(position, true);
		}
		notifyItemChanged(position);
	}
	public HashMap<Integer, Boolean> getSelectedItemsHash() {
		return selectedItems;
	}
	public int getSelectedItemPosition(int position) {
		Object[] keys = selectedItems.keySet().toArray();
		for (int i = 0; i < keys.length; i++) {
			if ((int)keys[i] == position) return i;
		}
		return -1;
	}

	/**
	 * Clear the selection status for all items
	 */
	public void clearSelection() {
		List<Integer> selection = getSelectedItems();
		selectedItems.clear();
		for (Integer i : selection) {
			notifyItemChanged(i);
		}
	}

	/**
	 * Count the selected items
	 * @return Selected items count
	 */
	public int getSelectedItemCount() {
		return selectedItems.size();
	}

	/**
	 * Indicates the list of selected items
	 * @return List of selected items ids
	 */

	public List<Integer> getSelectedItems() {
		return new ArrayList<>(selectedItems.keySet());
	}
}
