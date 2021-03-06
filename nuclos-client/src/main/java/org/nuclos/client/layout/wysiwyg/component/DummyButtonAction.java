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
package org.nuclos.client.layout.wysiwyg.component;

import java.awt.EventQueue;
import java.awt.KeyboardFocusManager;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.DUMMYBUTTONACTION;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.ui.collect.CollectActionAdapter;
import org.nuclos.client.ui.collect.CollectController;
import org.nuclos.client.ui.layoutml.LayoutMLParser;
import org.nuclos.common.collect.collectable.Collectable;


/**
 * Default ActionCommand for added Buttons to provide a valid ActionCommand
 * NUCLEUSINT-255
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
public class DummyButtonAction<Clct extends Collectable> implements CollectActionAdapter<Clct> {

	/**
	 * Pops up a Optionpane displaying a DummyText
	 */
	@Override
	public void run(final JButton btn, CollectController<Clct> controller, Properties probs) {
		MainFrameTab overlayFrame = new MainFrameTab("Dummy Button Aktion");
		overlayFrame.setLayeredComponent(new JLabel(DUMMYBUTTONACTION.MESSAGE));
		controller.getTab().add(overlayFrame);

		//@todo refactor to LayoutMLButton.
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				// Must be invoked later, else focus is not set with compound components like LOVs
				EventQueue.invokeLater(new Runnable() {
					@Override
		            public void run() {
						if (btn.getClientProperty(LayoutMLParser.ATTRIBUTE_NEXTFOCUSONACTION) != null 
								&& btn.getClientProperty(LayoutMLParser.ATTRIBUTE_NEXTFOCUSONACTION).equals(Boolean.TRUE)) {
							KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent(btn);
						}
					}
				});
			}
		});
	}

	@Override
	public boolean isRunnable(CollectController<Clct> controller, Properties probs) {
		return true;
	}
}
