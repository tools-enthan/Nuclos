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
package org.nuclos.client.common;

import java.awt.Component;
import java.awt.IllegalComponentStateException;

import org.nuclos.client.main.Main;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.CollectController;
import org.nuclos.client.ui.collect.CollectState;
import org.nuclos.client.ui.collect.component.CollectableComponent;
import org.nuclos.client.ui.collect.component.CollectableComponentEvent;
import org.nuclos.client.ui.collect.component.CollectableListOfValues;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.exception.CommonBusinessException;

/**
 * Default listener for <code>CollectableListOfValues.Event</code>s.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class NuclosLOVListener implements CollectableListOfValues.LOVListener {
	private static NuclosLOVListener singleton;

	public static synchronized NuclosLOVListener getInstance() {
		if (singleton == null) {
			singleton = new NuclosLOVListener();
		}
		return singleton;
	}

	private NuclosLOVListener() {
	}

	@Override
	public void viewSearchResults(final CollectableListOfValues.Event ev) {
		final CollectableComponent clctcomp = ev.getCollectableComponent();
		UIUtils.runCommandLater(UIUtils.getInternalFrameOrWindowForComponent(clctcomp.getJComponent()), new CommonRunnable() {
			@Override
			public void run() throws CommonBusinessException {
				final String sReferencedEntityName = clctcomp.getEntityField().getReferencedEntityName();
				final CollectController<?> ctl = newCollectController(sReferencedEntityName);
				ctl.runViewResults(ev.getCollectableListOfValues());
				ctl.getCollectPanel().setTabbedPaneEnabledAt(CollectState.OUTERSTATE_SEARCH, false);
			}
		});
	}

	@Override
	public void lookup(final CollectableListOfValues.Event ev) {
		final CollectableComponent clctcomp = ev.getCollectableComponent();
		Component c = UIUtils.getInternalFrameOrWindowForComponent(clctcomp.getControlComponent());
		final MainFrameTab tab;
		if (c instanceof MainFrameTab) {
			tab = (MainFrameTab) c;
		} else {
			MainFrameTab selectedTab = null;
			try {
				selectedTab = MainFrame.getSelectedTab(clctcomp.getControlComponent().getLocationOnScreen());
			} catch (IllegalComponentStateException e) {
				// 
			} finally {
				tab = selectedTab;
			}
		};
		
		if (tab == null) {
			throw new NuclosFatalException("Tab from parent could not be determinded");
		}
		
		UIUtils.runCommandLater(tab, new CommonRunnable() {
			@Override
			public void run() throws CommonBusinessException {
				final String sReferencedEntityName = clctcomp.getEntityField().getReferencedEntityName();
				final CollectController<?> ctl = NuclosCollectControllerFactory.getInstance().newCollectController(tab, sReferencedEntityName, null);
				ctl.runLookupCollectable(ev.getCollectableListOfValues());
			}
		});
	}

	@Override
	public void showDetails(final CollectableComponentEvent ev) {
		final CollectableComponent clctcomp = ev.getCollectableComponent();
		UIUtils.runCommandLater(clctcomp.getJComponent(), new CommonRunnable() {
			@Override
			public void run() throws CommonBusinessException {
				final String sReferencedEntityName = clctcomp.getEntityField().getReferencedEntityName();
				CollectController<?> controller = Main.getMainController().getControllerForInternalFrame((MainFrameTab) Main.getMainFrame().getHomePane().getSelectedComponent());
				Object oId = clctcomp.getField().getValueId();
				if(oId instanceof Long) {
					Long l = (Long)oId;
					oId = new Integer(l.intValue());
				}
				Main.getMainController().showDetails(sReferencedEntityName, oId, controller);
			}
		});
	}

	private static CollectController<? extends Collectable> newCollectController(String sEntityName) throws CommonBusinessException {
		return NuclosCollectControllerFactory.getInstance().newCollectController(MainFrame.getPredefinedEntityOpenLocation(sEntityName), sEntityName, null);
	}

}	// class NuclosLOVListener