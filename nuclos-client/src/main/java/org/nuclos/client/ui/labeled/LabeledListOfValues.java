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
package org.nuclos.client.ui.labeled;

import javax.swing.JComponent;

import org.nuclos.client.ui.ColorProvider;
import org.nuclos.client.ui.ListOfValues;
import org.nuclos.client.ui.ToolTipTextProvider;
import org.nuclos.client.ui.UIUtils;

/**
 * <code>LabeldComponent</code> that presents a value in a <code>ListOfValues</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */

public class LabeledListOfValues extends LabeledComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final ListOfValues lov = new ListOfValues();

	public LabeledListOfValues() {
		super();

		this.addControl(this.lov);
		this.getJLabel().setLabelFor(this.lov);
	}

	@Override
	public void setToolTipTextProviderForControl(ToolTipTextProvider tooltiptextprovider) {
		super.setToolTipTextProviderForControl(tooltiptextprovider);
		this.lov.setToolTipTextProvider(tooltiptextprovider);
	}

	@Override
	public void setBackgroundColorProvider(ColorProvider colorproviderBackground) {
		super.setBackgroundColorProvider(colorproviderBackground);
		this.lov.setBackgroundColorProviderForTextField(colorproviderBackground);
	}

	public ListOfValues getListOfValues() {
		return this.lov;
	}

	@Override
	public JComponent getControlComponent() {
		return this.lov;
	}

	@Override
	public void setName(String sName) {
		super.setName(sName);
		UIUtils.setCombinedName(this.lov, sName, "lov");
	}


}  // class LabeledListOfValues
