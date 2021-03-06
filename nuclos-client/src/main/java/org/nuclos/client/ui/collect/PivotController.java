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

import java.awt.Component;
import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

import org.apache.log4j.Logger;
import org.nuclos.client.genericobject.CollectableGenericObjectWithDependants;
import org.nuclos.client.ui.collect.result.GenericObjectResultController;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.PivotInfo;

public class PivotController extends SelectFixedColumnsController {

	private static final Logger LOG = Logger.getLogger(PivotController.class);

	private class ShowPivotListener implements ItemListener {

		@Override
		public void itemStateChanged(ItemEvent e) {
			final ItemSelectable src = e.getItemSelectable();
			final int index = getPivotPanel().indexFromValueComponent(src);
			final String subform = getPivotPanel().getSubformName(src);
			final List<PivotInfo> newState = getPivotPanel().getState(subform);
			final EntityFieldMetaDataVO item = (EntityFieldMetaDataVO) e.getItem();
			LOG.info("index: " + index + " subform: " + subform + " newState: " + newState + " md: " + item);
			if (newState != null && !newState.isEmpty()) {
				resultController.putPivotInfo(subform, newState);
			}
			else {
				resultController.removePivotInfo(subform);
			}
			final Comparator<CollectableEntityField> comp = (Comparator<CollectableEntityField>)
				resultController.getFields().getComparatorForAvaible();
			final SortedSet<CollectableEntityField> available =
				resultController.getFieldsAvailableForResult(comp);
			// TODO: check if the unmodifiable List is necessary here
			final List<CollectableEntityField> selected = new ArrayList<CollectableEntityField>(
					resultController.getFields().getSelectedFields());

			// remove field that are not available any more from selected fields
			for(Iterator<CollectableEntityField> it = selected.iterator(); it.hasNext();) {
				final CollectableEntityField ef = it.next();
				if(!available.remove(ef)) {
					it.remove();
				}
			}

			getModel().set(available, selected, comp);
			// TODO: ???
			setModel(getModel());
		}

	}

	private final GenericObjectResultController<? extends CollectableGenericObjectWithDependants> resultController;

	public PivotController(Component parent, final PivotPanel panel, GenericObjectResultController<? extends CollectableGenericObjectWithDependants> resultController) {
		super(parent, panel);
		this.resultController = resultController;
		if (panel.getHeader() != null) {
			panel.addPivotItemListener(new ShowPivotListener());
			panel.addPivotItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					LOG.info("contentsChanged: " + panel.indexFromValueComponent(e.getItemSelectable()) + ": " + panel.getState());
				}

			});
		}
	}

	private PivotPanel getPivotPanel() {
		return (PivotPanel) getPanel();
	}

}
