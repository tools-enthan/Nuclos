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
package org.nuclos.client.ui.collect.component;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.apache.log4j.Logger;
import org.nuclos.client.ui.CommonJPasswordField;
import org.nuclos.client.ui.labeled.LabeledPasswordField;
import org.nuclos.common.NuclosPassword;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;

/**
 * NUCLEUSINT-1142
 * @author hartmut.beckschulze
 * @version 01.00.00
 */
public class CollectablePasswordField extends CollectableTextComponent {
	
	private static final Logger LOG = Logger.getLogger(CollectablePasswordField.class);
	
	// @SuppressWarnings("unused")
	private CollectableEntityField clctef;
	
	/**
	 * @param clctef
	 * @postcondition this.isDetailsComponent()
	 */
	public CollectablePasswordField(CollectableEntityField clctef) {
		this(clctef, false);
		assert this.isDetailsComponent();
	}

	public CollectablePasswordField(CollectableEntityField clctef, boolean bSearchable) {
		super(clctef, new LabeledPasswordField(clctef.isNullable(), clctef.getJavaClass(), clctef.getFormatInput(), bSearchable), bSearchable);
		this.clctef = clctef;
	}

	// @todo return JTextField
	public CommonJPasswordField getJTextField() {
		return (CommonJPasswordField) this.getJTextComponent();
	}

	@Override
	public void setColumns(int iColumns) {
		this.getJTextField().setColumns(iColumns);
	}

	@Override
	protected ComparisonOperator[] getSupportedComparisonOperators() {
		return null;
	}


	@Override
	public void setComparisonOperator(ComparisonOperator compop) {
		super.setComparisonOperator(compop);

		if (compop.getOperandCount() < 2) {
			this.runLocked(new Runnable() {
				@Override
				public void run() {
					try {
						getJTextComponent().setText(null);
					}
					catch (Exception e) {
						LOG.error("CollectablePassword.setComparisionOperator: " + e, e);
					}
				}
			});
		}
	}

	@Override
	public TableCellRenderer getTableCellRenderer(boolean subform) {
		return new PasswordCellRenderer();
	}
	
	/**
	 * 
	 * 
	 *
	 */
	protected static class PasswordCellRenderer extends DefaultTableCellRenderer {
		
		public PasswordCellRenderer() {
			setVerticalAlignment(SwingConstants.TOP);
		}
		
		@Override
		public Component getTableCellRendererComponent(JTable tbl, Object oValue, boolean bSelected, boolean bHasFocus, int iRow, int iColumn) {
			super.getTableCellRendererComponent(tbl, oValue, bSelected, bHasFocus, iRow, iColumn);
			
			final Object o;
			if (oValue instanceof CollectableField) {
				CollectableField clctf = (CollectableField) oValue;
				if (clctf.getValue() instanceof NuclosPassword) {
					o = ((NuclosPassword)clctf.getValue()).getValue();
				} else {
					o = clctf.getValue();
				}
			} else {
				o = null;
			}
			
			if (o == null) {
				setText("");
			} else {
				final StringBuffer sbStars = new StringBuffer(o.toString().length());
				for (int i = 0; i < o.toString().length(); i++) {
					sbStars.append('*');
				}
				setText(sbStars.toString());
			}
			
			setBackgroundColor(this, tbl, oValue, bSelected, bHasFocus, iRow, iColumn);
			
			return this;
		}
	};

}	// class CollectableTextField
