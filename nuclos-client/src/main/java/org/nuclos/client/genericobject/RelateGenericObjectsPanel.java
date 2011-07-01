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

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.client.genericobject.datatransfer.GenericObjectIdModuleProcess;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.LineLayout;
import org.nuclos.client.ui.UIUtils;

/**
 * Panel for creating a relationship between leased objects.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
class RelateGenericObjectsPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static enum RelationType {
		PREDECESSOROF, PARTOF, USERDEFINED
	}

	final JRadioButton rbSuccessorOf = new JRadioButton(CommonLocaleDelegate.getMessage("RelateGenericObjectsPanel.1", "Nachfolge-Beziehung"));
	final JRadioButton rbPartOf = new JRadioButton(CommonLocaleDelegate.getMessage("RelateGenericObjectsPanel.2", "Teil-Beziehung"));
	final JRadioButton rbOtherRelation = new JRadioButton(CommonLocaleDelegate.getMessage("RelateGenericObjectsPanel.3", "Andere Beziehung"));
	final ButtonGroup bg = new ButtonGroup();
	private final SuccessorOfPanel pnlSuccessorOf = new SuccessorOfPanel();
	private final PartOfPanel pnlPartOf = new PartOfPanel();
	private OtherRelationPanel pnlOtherRelation;

	private final CardLayout cardlayout = new CardLayout();
	private final JPanel pnlCenter = new JPanel(cardlayout);

	/**
	 * @param collgoimpSource
	 * @param goimpTarget
	 * @precondition goimpTarget != null
	 */
	RelateGenericObjectsPanel(Collection<GenericObjectIdModuleProcess> collgoimpSource,
			GenericObjectIdModuleProcess goimpTarget) {

		super(new BorderLayout());

		this.getSuccessorOfPanel().setup(collgoimpSource, goimpTarget);
		this.getPartOfPanel().setup(collgoimpSource, goimpTarget);
		this.pnlOtherRelation = new OtherRelationPanel(collgoimpSource, goimpTarget);

		this.init();
	}

	private void init() {
		final JPanel pnlNorth = new JPanel(new LineLayout(LineLayout.VERTICAL));
		this.add(pnlNorth, BorderLayout.NORTH);

		final JPanel pnlOptions = new JPanel(new LineLayout(LineLayout.VERTICAL));
		pnlNorth.add(pnlOptions);

		pnlOptions.setBorder(BorderFactory.createTitledBorder(CommonLocaleDelegate.getMessage("OtherRelationPanel.1", "Beziehungsart")));
		pnlOptions.add(rbSuccessorOf);
		pnlOptions.add(rbPartOf);
		pnlOptions.add(rbOtherRelation);
		bg.add(rbSuccessorOf);
		bg.add(rbPartOf);
		bg.add(rbOtherRelation);
		rbSuccessorOf.setName("rbSuccessorOf");
		rbPartOf.setName("rbPartOf");
		rbOtherRelation.setName("rbOtherRelation");

		pnlCenter.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
		this.add(pnlCenter, BorderLayout.CENTER);
		pnlCenter.add(rbSuccessorOf.getName(), pnlSuccessorOf);
		pnlCenter.add(rbPartOf.getName(), pnlPartOf);
		pnlCenter.add(rbOtherRelation.getName(), pnlOtherRelation);

		final ItemListener itemlistener = new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent ev) {
				if (ev.getStateChange() == ItemEvent.SELECTED) {
					final AbstractButton btn = (AbstractButton) ev.getItemSelectable();
					cardlayout.show(pnlCenter, btn.getName());
				}
			}
		};
		rbSuccessorOf.addItemListener(itemlistener);
		rbPartOf.addItemListener(itemlistener);
		rbOtherRelation.addItemListener(itemlistener);
	}

	/**
	 * @return the relation type selected in this panel
	 */
	public RelationType getRelationType() {
		final RelationType result;

		if (rbSuccessorOf.isSelected()) {
			result = RelationType.PREDECESSOROF;
		}
		else if (rbPartOf.isSelected()) {
			result = RelationType.PARTOF;
		}
		else if (rbOtherRelation.isSelected()) {
			result = RelationType.USERDEFINED;
		}
		else {
			throw new IllegalStateException();
		}
		return result;
	}

	SuccessorOfPanel getSuccessorOfPanel() {
		return this.pnlSuccessorOf;
	}

	PartOfPanel getPartOfPanel() {
		return this.pnlPartOf;
	}

	OtherRelationPanel getCouplingPanel() {
		return this.pnlOtherRelation;
	}

	/**
	 * Displays a relation between objects.
	 */
	static abstract class RelationPanel extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		final JPanel pnlSourceObjects = new JPanel(new LineLayout(LineLayout.VERTICAL));
		final JLabel labTargetObject = new JLabel();

		final JPanel pnlLeft = new JPanel(new BorderLayout());
		final JPanel pnlRight = new JPanel(new BorderLayout());

		protected boolean bReversedDirection = false;

		RelationPanel(String sLeftTitle, String sRightTitle) {
			super(new GridBagLayout());
			this.init(sLeftTitle, sRightTitle);
		}

		/**
		 * @param collgoimpSource
		 * @param goimpTarget
		 * @precondition goimpTarget != null
		 */
		public void setup(Collection<GenericObjectIdModuleProcess> collgoimpSource, GenericObjectIdModuleProcess goimpTarget) {
			for (GenericObjectIdModuleProcess goimpSource : collgoimpSource) {
				final JLabel lab = new JLabel(goimpSource.getGenericObjectIdentifier());
				this.pnlSourceObjects.add(lab);
			}
			this.labTargetObject.setText(goimpTarget.getGenericObjectIdentifier());
		}

		/**
		 * @return if true, source and target objects are swapped, that is: source objects must be interpreted as target objects
		 * and vice versa.
		 */
		public boolean isReversedDirection() {
			return this.bReversedDirection;
		}

		private void init(String sLeftTitle, String sRightTitle) {
			final GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(3, 3, 3, 3), 0, 0);

			this.pnlLeft.add(newScrollPane(this.pnlSourceObjects), BorderLayout.CENTER);

			final JPanel pnlRightContents = new JPanel(new BorderLayout());
			pnlRightContents.add(this.labTargetObject, BorderLayout.NORTH);

			this.pnlRight.add(newScrollPane(pnlRightContents), BorderLayout.CENTER);

			gbc.gridx = 0;
			gbc.gridy = 0;
			this.add(new JLabel(sLeftTitle), gbc);
			gbc.gridx = 2;
			gbc.gridy = 0;
			this.add(new JLabel(sRightTitle), gbc);

			gbc.gridx = 0;
			gbc.gridy = 1;
			gbc.weightx = 1.0;
			gbc.weighty = 1.0;
			this.add(this.pnlLeft, gbc);

			gbc.gridx = 2;
			gbc.gridy = 1;
			this.add(this.pnlRight, gbc);
		}

		private static JScrollPane newScrollPane(JPanel pnl) {
			final JScrollPane result = new JScrollPane(pnl);
			// set the viewport's preferred height to 8 rows:
			UIUtils.setPreferredHeight(result.getViewport(), new JLabel("X").getPreferredSize().height * 8);
			return result;
		}

	}	// inner class RelationPanel

	/**
	 * Displays a relation between objects and lets the user swap the relation's direction.
	 */
	static class SwappableRelationPanel extends RelationPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		SwappableRelationPanel(String sLeftTitle, String sRightTitle) {
			super(sLeftTitle, sRightTitle);

			this.init();
		}

		private void init() {
			final GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(3, 3, 3, 3), 0, 0);
			final JButton btnSwapRelation = new JButton(Icons.getInstance().getIconReverseRelationship16());
			final Dimension dimBtn = new Dimension(60, 25);
			btnSwapRelation.setMinimumSize(dimBtn);
			btnSwapRelation.setPreferredSize(dimBtn);
			btnSwapRelation.setToolTipText(CommonLocaleDelegate.getMessage("RelateGenericObjectsPanel.4", "Beziehung umkehren"));

			btnSwapRelation.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ev) {
					swapLists();
					bReversedDirection = !bReversedDirection;
				}
			});

			gbc.gridx = 1;
			gbc.gridy = 1;
			gbc.fill = GridBagConstraints.NONE;
			gbc.weightx = 0.0;
			this.add(btnSwapRelation, gbc);
		}

		public void swapLists() {
			final GridBagConstraints gbc = new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(3, 3, 3, 3), 0, 0);
			this.remove(this.pnlLeft);
			this.remove(this.pnlRight);

			final JPanel pnlLeft = this.bReversedDirection ? this.pnlRight : this.pnlLeft;
			final JPanel pnlRight = this.bReversedDirection ? this.pnlLeft : this.pnlRight;

			gbc.gridx = 0;
			this.add(pnlRight, gbc);
			gbc.gridx = 2;
			this.add(pnlLeft, gbc);

			this.revalidate();
		}

	}	// inner class SwappableRelationPanel

	static class SuccessorOfPanel extends SwappableRelationPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		SuccessorOfPanel() {
			super(CommonLocaleDelegate.getMessage("RelateGenericObjectsPanel.5", "Vorg\u00e4nger"), CommonLocaleDelegate.getMessage("RelateGenericObjectsPanel.6", "Nachfolger"));
		}
	}

	static class PartOfPanel extends RelationPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		PartOfPanel() {
			super(CommonLocaleDelegate.getMessage("RelateGenericObjectsPanel.7", "Teilobjekt(e)"), CommonLocaleDelegate.getMessage("RelateGenericObjectsPanel.8", "Zusammengesetztes Objekt"));
		}
	}

}	// class RelateGenericObjectsPanel
