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
package org.nuclos.client.ui.collect.detail;

import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.Action;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.nuclos.client.common.DetailsSubFormController;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.entityobject.CollectableEntityObject;
import org.nuclos.client.scripting.ScriptEvaluator;
import org.nuclos.client.scripting.context.CollectControllerScriptContext;
import org.nuclos.client.scripting.context.SubformControllerScriptContext;
import org.nuclos.client.ui.CommonAbstractAction;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.CollectController;
import org.nuclos.client.ui.collect.CollectState;
import org.nuclos.client.ui.collect.CommonController;
import org.nuclos.client.ui.collect.component.CollectableComponent;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModel;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelAdapter;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelEvent;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelListener;
import org.nuclos.client.ui.collect.component.model.DetailsComponentModel;
import org.nuclos.client.ui.collect.component.model.DetailsComponentModelEvent;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.StringUtils;

/**
 * Controller for the Details panel.
 */
public class DetailsController<Clct extends Collectable> extends CommonController<Clct> {

	private final CollectableComponentModelListener ccmlistener = new CollectableComponentModelAdapter() {
		@Override
		public void collectableFieldChangedInModel(CollectableComponentModelEvent ev) {
			final CollectController<Clct> cc = getCollectController();
			assert cc.getCollectStateModel().getOuterState() == CollectState.OUTERSTATE_DETAILS;

			// Note that we don't check ev.collectableFieldHasChanged() here, as we want to set "details changed"
			// on every change, not only valid changes, esp. the case that the user starts typing a date
			// in an empty date field.
			cc.detailsChanged(ev.getCollectableComponentModel());
		}

		@Override
		public void valueToBeChanged(DetailsComponentModelEvent ev) {
			getCollectController().detailsChanged(ev.getCollectableComponentModel());
		}
	};

	private final Action actDeleteCurrentCollectable = new CommonAbstractAction("L\u00f6schen", Icons.getInstance().getIconRealDelete16(),
		CommonLocaleDelegate.getMessage("CollectController.15","Diesen Datensatz l\u00f6schen")) {

		@Override
        public void actionPerformed(ActionEvent ev) {
			getCollectController().cmdDeleteCurrentCollectableInDetails();
		}
	};

	private final List<DetailsSubFormController<?>> sfcs = new ArrayList<DetailsSubFormController<?>>();

	private final CollectableComponentModelListener mdlListener = new CollectableComponentModelAdapter() {
		@Override
		public void collectableFieldChangedInModel(CollectableComponentModelEvent ev) {
			final EntityMetaDataVO meta = MetaDataClientProvider.getInstance().getEntity(getCollectController().getEntityName());
			CollectableComponentModel model = ev.getCollectableComponentModel();
			if (!model.isInitializing()) {
				final String key = MessageFormat.format("#'{'{0}.{1}.{2}\'}'", meta.getNuclet(), meta.getEntity(), model.getFieldName());
				process(key, null, null);
			}
		}
	};

	public DetailsController(CollectController<Clct> cc) {
		super(cc);
	}

	public Action getDeleteCurrentCollectableAction() {
		return actDeleteCurrentCollectable;
	}

	/**
	 * display the number of the current record and the total number of records in the details panel's status bar
	 *
	 * TODO: Make this private again.
	 */
	public void displayCurrentRecordNumberInDetailsPanelStatusBar(){
		final CollectController<Clct> cc = getCollectController();
		getDetailsPanel().setStatusBarText(CommonLocaleDelegate.getMessage("CollectController.8","Datensatz") +
				" " + (cc.getResultTable().getSelectedRow() +1 ) + "/" + cc.getResultTable().getRowCount());
	}

	@Override
	protected boolean isSearchPanel() {
		return false;
	}

	/**
	 * TODO: Make protected again.
	 */
	@Override
	public Collection<DetailsComponentModel> getCollectableComponentModels() {
		return getCollectController().getDetailsPanel().getEditModel().getCollectableComponentModels();
	}

	@Override
	protected CollectableComponentModelListener getCollectableComponentModelListener() {
		return this.ccmlistener;
	}

	@Override
	protected void addAdditionalChangeListeners() {
		getCollectController().addAdditionalChangeListenersForDetails();
		for (CollectableComponentModel m : getCollectableComponentModels()) {
			m.addCollectableComponentModelListener(mdlListener);
		}
	}

	@Override
	protected void removeAdditionalChangeListeners() {
		getCollectController().removeAdditionalChangeListenersForDetails();
	}

	private DetailsPanel getDetailsPanel() {
		return getCollectController().getDetailsPanel();
	}

	/**
	 * TODO: Make this private again.
	 */
	public void setupDetailsPanel() {
		final CollectController<Clct> cc = getCollectController();
		// Details panel:
		// action: Save
		final DetailsPanel pnlDetails = this.getDetailsPanel();
		pnlDetails.btnSave.setAction(cc.getSaveAction());

		// action: Refresh
		pnlDetails.btnRefreshCurrentCollectable.setAction(cc.getRefreshCurrentCollectableAction());

		// action: Delete
		pnlDetails.btnDelete.setAction(this.actDeleteCurrentCollectable);

		// action: New
		pnlDetails.btnNew.setAction(cc.getNewAction());

		// action: Clone
		pnlDetails.btnClone.setAction(cc.getCloneAction());

		// action: Open in new tab
		pnlDetails.btnOpenInNewTab.setAction(cc.getOpenInNewTabAction());

		// action: Bookmark
		pnlDetails.btnBookmark.setAction(cc.getBookmarkAction());

		// navigation actions:
		pnlDetails.btnFirst.setAction(cc.getFirstAction());
		pnlDetails.btnLast.setAction(cc.getLastAction());
		pnlDetails.btnPrevious.setAction(cc.getPreviousAction());
		pnlDetails.btnNext.setAction(cc.getNextAction());

		UIUtils.readSplitPaneStateFromPrefs(cc.getPreferences(), getDetailsPanel());
	}

	public void close() {
		final DetailsPanel pnlDetails = this.getDetailsPanel();
		pnlDetails.btnSave.setAction(null);
		pnlDetails.btnRefreshCurrentCollectable.setAction(null);
		pnlDetails.btnDelete.setAction(null);
		pnlDetails.btnNew.setAction(null);
		pnlDetails.btnClone.setAction(null);
		pnlDetails.btnOpenInNewTab.setAction(null);
		pnlDetails.btnBookmark.setAction(null);

		pnlDetails.btnFirst.setAction(null);
		pnlDetails.btnLast.setAction(null);
		pnlDetails.btnPrevious.setAction(null);
		pnlDetails.btnNext.setAction(null);

		UIUtils.writeSplitPaneStateToPrefs(getCollectController().getPreferences(), getDetailsPanel());
	}

	/**
	 * TODO: Make this private again.
	 */
	public void updateStatusBarIfNecessary() {
		//log.debug("CollectController.updateStatusBarIfNecessary");
		if (getCollectController().getCollectState().isDetailsModeMultiViewOrEdit()) {
			this.showMultiEditChangeInStatusBar();
		}
	}

	/**
	 * @precondition CollectController.this.getCollectState().isDetailsModeMultiViewOrEdit()
	 */
	private void showMultiEditChangeInStatusBar() {
		final CollectController<Clct> cc = getCollectController();
		if (!cc.getCollectState().isDetailsModeMultiViewOrEdit()) {
			throw new IllegalStateException();
		}

		final String sChange = cc.getMultiEditChangeString();
		final String sStatus = CommonLocaleDelegate.getMessage("CollectController.5","\u00c4nderung")+ ": " + (StringUtils.looksEmpty(sChange) ? "<" + CommonLocaleDelegate.getMessage("CollectController.21","keine") + ">" : sChange);
		this.getDetailsPanel().setStatusBarText(sStatus);
	}

	public void setSubFormControllers(Collection<DetailsSubFormController<CollectableEntityObject>> sfcs) {
		this.sfcs.clear();
		this.sfcs.addAll(sfcs);
		for (final DetailsSubFormController<?> sfc : this.sfcs) {
			sfc.getSubForm().getSubformTable().getModel().addTableModelListener(new TableModelListener() {
				@Override
				public void tableChanged(TableModelEvent e) {
					switch (e.getType()) {
						case TableModelEvent.INSERT:
							break;
						case TableModelEvent.UPDATE:
							if (e.getColumn() >= 0) {
								CollectableEntityField column = sfc.getCollectableTableModel().getCollectableEntityField(e.getColumn());
								EntityMetaDataVO meta = MetaDataClientProvider.getInstance().getEntity(column.getEntityName());
								String key = MessageFormat.format("#'{'{0}.{1}.{2}\'}'", meta.getNuclet(), meta.getEntity(), column.getName());
								process(key, sfc, e.getFirstRow());
							}
							break;
						case TableModelEvent.DELETE:
							break;
					}
				}
			});
		}
	}

	private void process(final String sourceExpression, final DetailsSubFormController<?> sf, final Integer row) {
		if (getCollectController().isDetailsChangedIgnored()) {
			return;
		}

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				for (CollectableComponent c : getDetailsPanel().getEditView().getCollectableComponents()) {
					EntityFieldMetaDataVO fieldmeta = MetaDataClientProvider.getInstance().getEntityField(getCollectController().getEntityName(), c.getEntityField().getName());
					if (fieldmeta.getCalculationScript() != null) {
						if (fieldmeta.getCalculationScript().getSource().contains(sourceExpression)) {
							CollectableComponentModel m = getDetailsPanel().getEditModel().getCollectableComponentModelFor(fieldmeta.getField());
							Object o = ScriptEvaluator.getInstance().eval(fieldmeta.getCalculationScript(), new CollectControllerScriptContext(getCollectController(), sfcs), m.getField().getValue());
							m.setField(new CollectableValueField(o));
						}
					}
				}
				if (sf != null) {
					for (int i = 0; i < sf.getCollectableTableModel().getColumnCount(); i++) {
						CollectableEntityField cef = sf.getCollectableTableModel().getCollectableEntityField(i);
						EntityFieldMetaDataVO fieldmeta = MetaDataClientProvider.getInstance().getEntityField(sf.getEntityAndForeignKeyFieldName().getEntityName(), cef.getName());
						if (fieldmeta.getCalculationScript() != null) {
							if (fieldmeta.getCalculationScript().getSource().contains(sourceExpression)) {
								Object o = ScriptEvaluator.getInstance().eval(fieldmeta.getCalculationScript(), new SubformControllerScriptContext(sf, sf.getSelectedCollectable()), null);
								sf.getCollectableTableModel().setValueAt(new CollectableValueField(o), row, i);
							}
						}
					}
				}
			}
		});
	}
}	// class DetailsController
