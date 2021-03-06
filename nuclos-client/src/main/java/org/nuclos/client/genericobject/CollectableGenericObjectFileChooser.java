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
package org.nuclos.client.genericobject;

import java.awt.event.MouseEvent;

import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.client.common.CollectableDocumentFileChooserBase;
import org.nuclos.server.genericobject.valueobject.GenericObjectDocumentFile;

/**
 * <code>CollectableComponent</code> to display a <code>GenericObjectDocumentFile</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class CollectableGenericObjectFileChooser extends CollectableDocumentFileChooserBase {

	public CollectableGenericObjectFileChooser(CollectableEntityField clctef, Boolean bSearchable) {
		this(clctef, bSearchable.booleanValue());
	}

	public CollectableGenericObjectFileChooser(CollectableEntityField clctef, boolean bSearchable) {
		super(clctef, bSearchable);
	}

	@Override
	protected org.nuclos.common2.File newFile(String sFileName, byte[] abContents) {
		return new GenericObjectDocumentFile(sFileName, abContents);
	}
	//NUCLEUSINT-512
	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void mousePressed(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {}

}	// class CollectableFileChooser
