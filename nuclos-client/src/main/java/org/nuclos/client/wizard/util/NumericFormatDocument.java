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
package org.nuclos.client.wizard.util;

import javax.swing.text.PlainDocument;

public class NumericFormatDocument extends PlainDocument {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void insertString(int offset, String str, javax.swing.text.AttributeSet a)  throws javax.swing.text.BadLocationException {
		try {
			Integer.parseInt(str);
		}
		catch(NumberFormatException ex) {
			return;
		}
		
		super.insertString(offset, str, a);
    }
}