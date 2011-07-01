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
package org.nuclos.client.ui;

import java.util.Iterator;
import java.util.List;

import javax.swing.DefaultListModel;

/**
 * adds a decent constructor to <code>DefaultListModel</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */

public class CommonDefaultListModel<E> extends DefaultListModel implements MutableListModel<E> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CommonDefaultListModel() {
		super();
	}

	public CommonDefaultListModel(List<E> lst) {
		super();
		for (Iterator<E> iter = lst.iterator(); iter.hasNext();) {
			this.addElement(iter.next());
		}
	}

	@Override
	public void add(Object o) {
		this.addElement(o);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public E remove(int i) {
		return (E) super.remove(i);
	}

}  // class CommonDefaultListModel
