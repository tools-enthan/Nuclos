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
package org.nuclos.client.main;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

public class DevelopersPlayground extends JFrame {
	
	private static final Logger LOG = Logger.getLogger(DevelopersPlayground.class);

	private static final long serialVersionUID = 7918814689595516206L;

	public DevelopersPlayground(MainFrame mainFrame) {
		super("Spielplatz");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		Container cp = getContentPane();
		cp.setLayout(new FlowLayout());

		cp.add(new JButton(new AbstractAction("Test locale") {
			@Override
            public void actionPerformed(ActionEvent e) {
				testLocale();
			}}));
		pack();
	}

	private void testLocale() {
		try {
			MasterDataVO mloc = MasterDataDelegate.getInstance().get(NuclosEntity.LOCALE.getEntityName(), 1);
			JOptionPane.showMessageDialog(this, mloc.getFields());
		} catch (CommonFinderException e) {
			LOG.warn("testLocale failed: " + e, e);
		} catch (CommonPermissionException e) {
			LOG.warn("testLocale failed: " + e, e);
		}
	}

}

