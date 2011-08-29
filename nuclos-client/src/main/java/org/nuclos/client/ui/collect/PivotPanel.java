//Copyright (C) 2011  Novabit Informationssysteme GmbH
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
package org.nuclos.client.ui.collect;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.ItemSelectable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.ComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.ui.model.SimpleCollectionComboBoxModel;
import org.nuclos.client.ui.renderer.EntityFieldMetaDataListCellRenderer;
import org.nuclos.common.CloneUtils;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.PivotInfo;
import org.nuclos.common2.CommonLocaleDelegate;


/**
 * A specialization of SelectFixedColumnsPanel for also choosing 'columns'
 * from a pivot (key -> value) table.
 *
 * @author Thomas Pasch
 * @since Nuclos 3.1.01
 */
public class PivotPanel extends SelectFixedColumnsPanel {

	private static final Logger LOG = Logger.getLogger(PivotPanel.class);

	private static class Header extends JPanel {

		private class Enabler implements ItemListener {

			private final int index;

			public Enabler(int index) {
				this.index = index;
			}

			@Override
			public void itemStateChanged(ItemEvent e) {
				LOG.info("Enabler: item event " + e);
				final JCheckBox src = (JCheckBox) e.getSource();
				final boolean selected = src.isSelected();
				final JLabel key = keyLabels.get(index);
				final JComboBox value = valueCombos.get(index);
				key.setEnabled(selected);
				value.setEnabled(selected);
				final EntityFieldMetaDataVO keyItem = keyMds.get(index);
				final EntityFieldMetaDataVO valueItem = (EntityFieldMetaDataVO) value.getSelectedItem();
				setState(selected, index, keyItem, valueItem);
				fireItemEvent(value, keyItem);
			}

		}

		private class Changer implements ItemListener {

			private final int index;

			public Changer(int index) {
				this.index = index;
			}

			@Override
			public void itemStateChanged(ItemEvent e) {
				LOG.info("Changer: item event " + e);
				final JComboBox src = (JComboBox) e.getSource();
				if (e.getStateChange() == ItemEvent.SELECTED) {
					final JLabel key = keyLabels.get(index);
					final JComboBox value = valueCombos.get(index);
					final EntityFieldMetaDataVO keyItem = keyMds.get(index);
					final EntityFieldMetaDataVO valueItem = (EntityFieldMetaDataVO) value.getSelectedItem();
					setState(true, index, keyItem, valueItem);
					fireItemEvent(value, keyItem);
				}
			}

		}

		private final JCheckBox checkbox;

		private final List<JCheckBox> subformCbs = new ArrayList<JCheckBox>();

		private final List<JLabel> keyLabels = new ArrayList<JLabel>();

		private final List<EntityFieldMetaDataVO> keyMds = new ArrayList<EntityFieldMetaDataVO>();

		private final List<JComboBox> valueCombos = new ArrayList<JComboBox>();

		private final LinkedHashMap<String, PivotInfo> state;

		private final List<String> subformNames = new ArrayList<String>();

		private final List<ItemListener> listener = new LinkedList<ItemListener>();

		private Header(String baseEntity, Map<String, Map<String, EntityFieldMetaDataVO>> subFormFields, Map<String,PivotInfo> state) {
			super(new GridBagLayout());
			// copy state: see below
			this.state = new LinkedHashMap<String, PivotInfo>();

			// setPreferredSize(new Dimension(400, 200));
			final GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL;

			checkbox = new JCheckBox(
					CommonLocaleDelegate.getMessageFromResource("pivot.panel.enable.pivot"));
			checkbox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					final JCheckBox src = (JCheckBox) e.getSource();
					final boolean selected = src.isSelected();
					for (int i = 0; i < subformCbs.size(); ++i) {
						final JCheckBox s = subformCbs.get(i);
						s.setEnabled(selected);
						final boolean senabled = selected && s.isSelected();
						final JLabel key = keyLabels.get(i);
						final JComboBox value = valueCombos.get(i);
						key.setEnabled(senabled);
						value.setEnabled(senabled);
						final EntityFieldMetaDataVO keyItem = keyMds.get(i);
						final EntityFieldMetaDataVO valueItem = (EntityFieldMetaDataVO) value.getSelectedItem();
						setState(senabled, i, keyItem, valueItem);
						fireItemEvent(value, keyItem);
					}
				}
			});

			c.gridx = 0;
			c.gridy = 0;
			c.gridwidth = 3;
			c.anchor = GridBagConstraints.CENTER;
			c.fill = GridBagConstraints.HORIZONTAL;
			add(checkbox, c);
			setVisible(true);
			checkbox.setVisible(true);
			checkbox.setEnabled(true);
			checkbox.setSelected(!state.isEmpty());

			// label
			JLabel label = new JLabel(
					CommonLocaleDelegate.getMessageFromResource("pivot.panel.pivot.entity"));
			c.gridy = 1;
			c.gridx = 0;
			c.weightx = 0.2;
			add(label, c);
			label = new JLabel(
					CommonLocaleDelegate.getMessageFromResource("pivot.panel.key.field"));
			c.gridx = 1;
			c.weightx = 0.2;
			add(label, c);
			label = new JLabel(
					CommonLocaleDelegate.getMessageFromResource("pivot.panel.value.field"));
			c.gridx = 2;
			c.weightx = 0.2;
			add(label, c);

			//
			int index = 0;
			c.gridwidth = 1;
			c.ipadx = 3;
			c.ipady = 1;
			// c.weightx = 0.0;

			final MetaDataClientProvider mdProv = MetaDataClientProvider.getInstance();
			final Collator collator = Collator.getInstance(CommonLocaleDelegate.getLocale());
			for (String subform: subFormFields.keySet()) {
				final Map<String, EntityFieldMetaDataVO> fields = subFormFields.get(subform);

				// copy state
				PivotInfo pinfo = state.get(subform);
				if (pinfo != null) {
					this.state.put(subform, pinfo);
				}
				final boolean enabled = this.state.containsKey(subform);

				final EntityMetaDataVO mdSubform = mdProv.getEntity(subform);
				final JCheckBox cb = new JCheckBox(CommonLocaleDelegate.getLabelFromMetaDataVO(mdSubform));
				cb.setSelected(enabled);
				cb.addItemListener(new Enabler(index));
				subformCbs.add(cb);
				c.gridy = index + 2;
				c.gridx = 0;
				add(cb, c);
				cb.setVisible(true);
				cb.setEnabled(checkbox.isSelected());

				final Changer changer = new Changer(index);
				final EntityFieldMetaDataVO keyField = mdProv.getPivotKeyField(baseEntity, subform);
				final JLabel l = new JLabel(CommonLocaleDelegate.getLabelFromMetaFieldDataVO(keyField));
				l.setEnabled(enabled);
				keyLabels.add(l);
				keyMds.add(keyField);
				c.gridx = 1;
				add(l, c);

				final JComboBox combo = mkCombo(collator, fields);
				combo.setEnabled(enabled);
				if (pinfo != null) {
					combo.setSelectedItem(fields.get(pinfo.getValueField()));
				}
				combo.addItemListener(changer);
				valueCombos.add(combo);
				c.gridx = 2;
				add(combo, c);

				subformNames.add(subform);
				++index;
			}
		}

		private static JComboBox mkCombo(final Collator col, Map<String, EntityFieldMetaDataVO> fields) {
			final TreeSet<EntityFieldMetaDataVO> sorted = new TreeSet<EntityFieldMetaDataVO>(
					new Comparator<EntityFieldMetaDataVO>() {
						@Override
						public int compare(EntityFieldMetaDataVO o1, EntityFieldMetaDataVO o2) {
							return col.compare(CommonLocaleDelegate.getLabelFromMetaFieldDataVO(o1),
									CommonLocaleDelegate.getLabelFromMetaFieldDataVO(o2));
						}
					});
			sorted.addAll(fields.values());
			final ComboBoxModel model = new SimpleCollectionComboBoxModel<EntityFieldMetaDataVO>(sorted);
			final JComboBox result = new JComboBox(model);
			result.setRenderer(EntityFieldMetaDataListCellRenderer.getInstance());
			result.setVisible(true);
			result.setSelectedIndex(0);
			return result;
		}

		private void setState(boolean selected, int index, EntityFieldMetaDataVO keyItem, EntityFieldMetaDataVO valueItem) {
			// set state
			final String subform = subformNames.get(index);
			if (selected) {
				final Class<?> type;
				try {
					type = Class.forName(valueItem.getDataType());
				} catch (ClassNotFoundException e) {
					throw new IllegalStateException(e);
				}
				state.put(subform, new PivotInfo(subform, keyItem.getField(), valueItem.getField(), type));
			}
			else {
				state.put(subform, null);
			}
		}

		private void fireItemEvent(ItemSelectable source, EntityFieldMetaDataVO item) {
			final List<ItemListener> l;
			synchronized (listener) {
				if (listener.isEmpty()) return;
				l = (List<ItemListener>) CloneUtils.cloneCollection(listener);
			}
			final ItemEvent event = new ItemEvent(source, ItemEvent.SELECTED, item, ItemEvent.SELECTED);
			for (ItemListener i: l) {
				i.itemStateChanged(event);
			}
		}

	}

	public PivotPanel(String baseEntity, Map<String, Map<String, EntityFieldMetaDataVO>> subFormFields, Map<String,PivotInfo> state) {
		super(subFormFields.isEmpty() ? null : new Header(baseEntity, subFormFields, state));
	}

	public void addPivotItemListener(ItemListener l) {
		List<ItemListener> listener = getHeader().listener;
		synchronized (listener) {
			listener.add(l);
		}
	}

	public void removePivotItemListener(ItemListener l) {
		List<ItemListener> listener = getHeader().listener;
		synchronized (listener) {
			listener.remove(l);
		}
	}

	public PivotInfo getState(String subformName) {
		return getHeader().state.get(subformName);
	}

	public String getSubformName(int index) {
		return getHeader().subformNames.get(index);
	}

	public int indexFromValueComponent(ItemSelectable key) {
		return getHeader().valueCombos.indexOf(key);
	}

	public Map<String,PivotInfo> getState() {
		return getHeader().state;
	}

	public Header getHeader() {
		return (Header) getHeaderComponent();
	}

}
