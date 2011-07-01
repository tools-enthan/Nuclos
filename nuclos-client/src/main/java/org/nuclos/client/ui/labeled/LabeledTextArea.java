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

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ToolTipManager;
import javax.swing.text.JTextComponent;

import org.nuclos.client.ui.ColorProvider;
import org.nuclos.client.ui.CommonJScrollPane;
import org.nuclos.client.ui.ToolTipTextProvider;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.StringUtils;

/**
 * <code>CollectableComponent</code> that presents a value in a <code>JTextArea</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */

public class LabeledTextArea extends LabeledTextComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JTextArea ta = new JTextArea() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public String getToolTipText(MouseEvent ev) {
			final ToolTipTextProvider provider = LabeledTextArea.this.getToolTipTextProviderForControl();
			return StringUtils.concatHtml(provider != null ? provider.getDynamicToolTipText() : super.getToolTipText(ev), LabeledTextArea.this.getValidationToolTip());
		}

		@Override
		public Color getBackground() {
			final ColorProvider colorproviderBackground = LabeledTextArea.this.getBackgroundColorProvider();
			final Color colorDefault = super.getBackground();
			return (colorproviderBackground != null) ? colorproviderBackground.getColor(colorDefault) : colorDefault;
		}
	};

	private JScrollPane scrlpn = new CommonJScrollPane(this.ta, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER) {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public boolean hasFocus() {
			return ta.hasFocus();
		}
	};

	public LabeledTextArea() {
		/*super();
		this.addControl(this.scrlpn);
		this.getJLabel().setLabelFor(this.ta);

		// always enable wrapping:
		this.ta.setLineWrap(true);
		this.ta.setWrapStyleWord(true);
		*/
		this(true, String.class, null, false);
	}

	public LabeledTextArea(boolean isNullable, Class<?> javaClass, String inputFormat, boolean bSearchable) {
		super(isNullable, javaClass, inputFormat, bSearchable);
		initValidation(isNullable, javaClass, inputFormat);
		if(this.validationLayer != null){
			this.addControl(this.validationLayer);
		} else {
			this.addControl(this.scrlpn);
		}
		this.getJLabel().setLabelFor(this.ta);

		// always enable wrapping:
		this.ta.setLineWrap(true);
		this.ta.setWrapStyleWord(true);
	}

	@Override
	protected JComponent getLayeredComponent(){
		return this.ta;
	}

	@Override
	protected JTextComponent getLayeredTextComponent(){
		return this.ta;
	}
	
	public JScrollPane getJScrollPane() {
		return this.scrlpn;
	}

	public JTextArea getJTextArea() {
		return this.ta;
	}

	/**
	 * @return the text area
	 */
	@Override
	public JTextComponent getJTextComponent() {
		return this.ta;
	}

	@Override
	public JComponent getControlComponent() {
		return this.getJScrollPane();
	}
	
	@Override
	public boolean hasFocus() {
		return this.ta.hasFocus();
	}

	/**
	 * sets the static tooltip text for the label and the control component (not for the panel itself).
	 * The static tooltip is shown in the control component only if no tooltiptextprovider was set for the control.
	 * @param sToolTipText
	 * @postcondition LangUtils.equals(this.getToolTipText(), sToolTipText)
	 */
	@Override
	public void setToolTipText(String sToolTipText) {
		this.getJLabel().setToolTipText(sToolTipText);
		this.ta.setToolTipText(sToolTipText);

		assert LangUtils.equals(this.getToolTipText(), sToolTipText);
	}

	@Override
	public void setToolTipTextProviderForControl(ToolTipTextProvider tooltiptextprovider) {
		super.setToolTipTextProviderForControl(tooltiptextprovider);
		if (tooltiptextprovider != null) {
			// This is necessary to enable dynamic tooltips for the text area:
			ToolTipManager.sharedInstance().registerComponent(this.getJTextArea());
		}
	}

	@Override
	protected GridBagConstraints getGridBagConstraintsForLabel() {
		final GridBagConstraints result = (GridBagConstraints) super.getGridBagConstraintsForLabel().clone();

		result.anchor = GridBagConstraints.NORTHWEST;

		return result;
	}

	@Override
	protected GridBagConstraints getGridBagConstraintsForControl(boolean bFill) {
		final GridBagConstraints result = (GridBagConstraints) super.getGridBagConstraintsForControl(bFill).clone();

		// always fill vertically:
		switch (result.fill) {
			case GridBagConstraints.NONE:
				result.fill = GridBagConstraints.VERTICAL;
				break;
			case GridBagConstraints.HORIZONTAL:
				result.fill = GridBagConstraints.BOTH;
				break;
			case GridBagConstraints.BOTH:
				result.fill = GridBagConstraints.BOTH;
				break;
			default:
				assert false;
		}

		result.weighty = 1.0;

		// no top/bottom insets:
		result.insets.top = 0;
		result.insets.bottom = 0;

		return result;
	}

	/**
	 * sets the number of columns of the textarea
	 * @param iColumns
	 */
	@Override
	public void setColumns(int iColumns) {
		this.getJTextArea().setColumns(iColumns);
	}

	@Override
	public void setRows(int iRows) {
		this.getJTextArea().setRows(iRows);
	}

	@Override
	public void setName(String sName) {
		super.setName(sName);
		UIUtils.setCombinedName(this.ta, sName, "ta");
	}

}  // class LabeledTextArea
