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
package org.nuclos.client.wizard;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import org.pietschy.wizard.ButtonBar;
import org.pietschy.wizard.InvalidStateException;
import org.pietschy.wizard.Wizard;
import org.pietschy.wizard.WizardAction;
import org.pietschy.wizard.WizardModel;

/**
* <br>
* Created by Novabit Informationssysteme GmbH <br>
* Please visit <a href="http://www.novabit.de">www.novabit.de</a>
* 
* @author <a href="mailto:marc.finke@novabit.de">Marc Finke</a>
* @version 01.00.00
*/
public class NuclosEntityWizard extends Wizard {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	boolean blnCancelEnabled = true;

	/**
	 * @param model
	 */
	public NuclosEntityWizard(WizardModel model) {
		super(model);
		this.setDefaultExitMode(EXIT_ON_FINISH);
	}
	
	@Override
	protected ButtonBar createButtonBar() {
		return new NuclosWizardButtonBar(this);
	}	
	
	public void enableCancelAction(boolean bln) {
		blnCancelEnabled = bln;
	}
	
	@Override
	public Action getCancelAction() {
		return new WizardAction("cancel", this){

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void doAction(ActionEvent e) throws InvalidStateException {
				 getWizard().cancel();
			}

			@Override
			protected void updateState() {
				setEnabled(blnCancelEnabled);
			}

		};
	}

}