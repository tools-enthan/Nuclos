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
package org.nuclos.client.wizard.steps;

import java.io.Closeable;

import javax.swing.Icon;

import org.apache.log4j.Logger;
import org.nuclos.client.wizard.NuclosEntityAttributeWizardStaticModel;
import org.nuclos.common2.SpringLocaleDelegate;
import org.pietschy.wizard.InvalidStateException;
import org.pietschy.wizard.PanelWizardStep;
import org.pietschy.wizard.WizardModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
* <br>
* Created by Novabit Informationssysteme GmbH <br>
* Please visit <a href="http://www.novabit.de">www.novabit.de</a>
* 
* @author <a href="mailto:marc.finke@novabit.de">Marc Finke</a>
* @version 01.00.00
*/
@Configurable
public abstract class NuclosEntityAttributeAbstractStep extends PanelWizardStep implements Closeable {
	
	private static final Logger LOG = Logger.getLogger(NuclosEntityAttributeAbstractStep.class);
	
	NuclosEntityAttributeWizardStaticModel model;
	
	SpringLocaleDelegate localeDelegate;

	public NuclosEntityAttributeAbstractStep() {
	}

	public NuclosEntityAttributeAbstractStep(String name, String summary) {
		super(name, summary);
	}

	public NuclosEntityAttributeAbstractStep(String name, String summary, Icon icon) {
		super(name, summary, icon);
	}
	
	@Autowired
	void setSpringLocaleDelegate(SpringLocaleDelegate cld) {
		this.localeDelegate = cld;
	}
	
	protected abstract void initComponents();
	
	@Override
	public void init(WizardModel model) {
		super.init(model);
		this.model = (NuclosEntityAttributeWizardStaticModel)model;
	}
	
	public NuclosEntityAttributeWizardStaticModel getModel() {
		return model;
	}

	@Override
	public void abortBusy() {
		close();
	}
	
	@Override
	public void close() {
		LOG.info("close(): " + this);
		removeAll();
		model = null;
		localeDelegate = null;
	}

}
