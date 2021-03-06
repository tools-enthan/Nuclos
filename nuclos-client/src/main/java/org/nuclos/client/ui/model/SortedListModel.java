//Copyright (C) 2010  Novabit Informationssysteme GmbH
//
//This file is part of Nuclos.
//
//Nuclos is free software: you can redistribute it and/or modify
//it under the terms of the GNU Affero General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//Nuclos is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU Affero General Public License for more details.
//
//You should have received a copy of the GNU Affero General Public License
//along with Nuclos.  If not, see <http://www.gnu.org/licenses/>.
package org.nuclos.client.ui.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

import org.apache.commons.lang.NullArgumentException;

/**
 * <code>ListModel</code> that is always sorted.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */
public class SortedListModel<E> extends AbstractListModel implements MutableListModel<E>, ComboBoxModel {

	private final List<E> lst;

	private final Comparator<? super E> comparator;
	
	private E selected;
	
	private int selectedIndex = -1;

	/**
	 * creates an empty <code>SortedListModel</code>. The elements to be inserted must be <code>Comparable</code>.
	 * @postcondition this.getSize() == 0
	 */
	public SortedListModel() {
		this(Collections.<E>emptyList(), true);

		assert this.getSize() == 0;
	}

	/**
	 * creates a <code>SortedListModel</code> containing the given list. The elements to be inserted must be <code>Comparable</code>.
	 * @param coll the elements to be inserted initially.
	 */
	public SortedListModel(Collection<? extends E> coll, boolean copy) {
		this(coll, null, copy);
	}

	/**
	 * creates an empty <code>SortedListModel</code>.
	 * @postcondition this.getSize() == 0
	 */
	public SortedListModel(Comparator<? super E> comparator) {
		this(Collections.<E>emptyList(), comparator, true);

		assert this.getSize() == 0;
	}

	/**
	 * creates a <code>SortedListModel</code> containing the given list.
	 * @param coll the elements to be inserted initially.
	 * @param comparator used for comparing the elements. If <code>null</code>, the elements must be <code>Comparable</code>.
	 * @precondition lst != null
	 */
	public SortedListModel(Collection<? extends E> coll, Comparator<? super E> comparator, boolean copy) {
		if (coll == null) {
			throw new NullArgumentException("coll");
		}
		if (copy) {
			this.lst = new ArrayList<E>(coll);
		}
		else {
			this.lst = (List<E>) coll;
		}
		this.comparator = comparator;
		this.sort();
	}

	/**
	 * sorts the list by the fields' labels
	 */
	public void sort() {
		Collections.sort(this.lst, this.comparator);
		this.fireContentsChanged(this, 0, this.lst.size() - 1);
	}

	/**
	 * @return the number of elements in this <code>ListModel</code>
	 */
	@Override
	public int getSize() {
		return this.lst.size();
	}

	/**
	 * @param index
	 * @return the object at the given index
	 */
	@Override
	public E getElementAt(int index) {
		return this.lst.get(index);
	}

	/**
	 * appends <code>cfm</code> to this list.
	 * @param o
	 */
	@Override
	public void add(Object o) {
		this.lst.add((E) o);
		final int iIndex = this.lst.size() - 1;
		this.fireIntervalAdded(this, iIndex, iIndex);
		this.sort();
	}

	/**
	 * adds <code>o</code> to this list, at the given index.
	 * @param index
	 * @param o
	 */
	@Override
	public void add(int index, Object o) {
		this.lst.add(index, (E) o);
		if (selectedIndex >= 0 && index <= selectedIndex) {
			selected = null;
			selectedIndex = -1;
		}
		super.fireIntervalAdded(this, index, index);
		this.sort();
	}

	/**
	 * removes the <code>CollectableEntityField</code> at the given index
	 * @param iIndex
	 */
	@Override
	public E remove(int iIndex) {
		final E result = this.lst.remove(iIndex);
		if (iIndex == selectedIndex) {
			selected = null;
			selectedIndex = -1;
		}
		super.fireIntervalRemoved(this, iIndex, iIndex);
		return result;
	}

	@Override
	public void replace(Collection<E> newCollection) {
		final int oldLen = lst.size();
		lst.clear();
		selected = null;
		selectedIndex = -1;
		fireIntervalRemoved(this, 0, oldLen);
		
		lst.addAll(newCollection);
		sort();
	}
	
	// methods from ComboBoxModel

	@Override
	public void setSelectedItem(Object anItem) {
		final E item = (E) anItem;
		selectedIndex = lst.indexOf(item);
		if (selectedIndex < 0) {
			// throw new IllegalStateException();
			return;
		}
		selected = item;
		final int len = lst.size();
		super.fireIntervalRemoved(this, 0, len);
		super.fireIntervalAdded(this, selectedIndex, selectedIndex);
	}

	@Override
	public Object getSelectedItem() {
		return selected;
	}
	
	public E getSelectedTypedItem() {
		return selected;
	}
	
	public int getSelectedIndex() {
		return selectedIndex;
	}

}  // class SortedListModel
