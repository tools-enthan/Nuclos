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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Action;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;

import org.apache.log4j.Logger;
import org.nuclos.api.context.ScriptContext;
import org.nuclos.client.common.DetailsSubFormController;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.common.Utils;
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
import org.nuclos.client.ui.collect.SubForm;
import org.nuclos.client.ui.collect.SubForm.RefreshValueListAction;
import org.nuclos.client.ui.collect.component.CollectableComboBox;
import org.nuclos.client.ui.collect.component.CollectableComponent;
import org.nuclos.client.ui.collect.component.CollectableComponentTableCellEditor;
import org.nuclos.client.ui.collect.component.CollectableListOfValues;
import org.nuclos.client.ui.collect.component.LabeledCollectableComponentWithVLP;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModel;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelAdapter;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelEvent;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelListener;
import org.nuclos.client.ui.collect.component.model.DetailsComponentModel;
import org.nuclos.client.ui.collect.component.model.DetailsComponentModelEvent;
import org.nuclos.client.ui.gc.ListenerUtil;
import org.nuclos.client.valuelistprovider.DefaultValueProvider;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.NuclosScript;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.expressions.ExpressionParser;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;

/**
 * Controller for the Details panel.
 */
public class DetailsController<Clct extends Collectable> extends CommonController<Clct> {
	
	ScriptObserver so = new ScriptObserver();

	private static final Logger LOG = Logger.getLogger(DetailsController.class);

	//

	/**
	 * Cannot be final because set to null in close(). (tp)
	 */
	private CollectableComponentModelListener ccmlistener = new CollectableComponentModelAdapter() {
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
		getSpringLocaleDelegate().getMessage("CollectController.15","Diesen Datensatz l\u00f6schen")) {

		@Override
        public void actionPerformed(ActionEvent ev) {
			getCollectController().cmdDeleteCurrentCollectableInDetails();
		}
	};

	private final List<DetailsSubFormController<?>> sfcs = new ArrayList<DetailsSubFormController<?>>();

	private final CollectableComponentModelListener mdlListener = new CollectableComponentModelAdapter() {
		@Override
		public void collectableFieldChangedInModel(final CollectableComponentModelEvent ev) {
			final EntityMetaDataVO meta = MetaDataClientProvider.getInstance().getEntity(getCollectController().getEntityName());
			CollectableComponentModel model = ev.getCollectableComponentModel();
			if (!model.isInitializing()) {
				//@see NUCLOSINT-1572. we removed using invokeLater here.
				String source = ev.getCollectableComponentModel().getFieldName();
				setSubformDefaultValues(null, source);

				//@see NUCLOSINT-1572. we removed using invokeLater here.
				final String key = MessageFormat.format("{0}.{1}.{2}", meta.getNuclet(), meta.getEntity(), model.getFieldName());
				process(key);
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
		getDetailsPanel().setStatusBarText(getSpringLocaleDelegate().getMessage("CollectController.8","Datensatz") +
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
			// m.addCollectableComponentModelListener(mdlListener);
			ListenerUtil.registerCollectableComponentModelListener(m, null, mdlListener);
		}
	}

	@Override
	protected void removeAdditionalChangeListeners() {
		getCollectController().removeAdditionalChangeListenersForDetails();
		for (CollectableComponentModel m : getCollectableComponentModels()) {
			m.removeCollectableComponentModelListener(mdlListener);
		}
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

	@Override
	public void close() {
		if (!isClosed()) {
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

			ccmlistener = null;

			super.close();
		}
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
		final String sStatus = getSpringLocaleDelegate().getMessage(
				"CollectController.5","\u00c4nderung")+ ": " + (StringUtils.looksEmpty(sChange) ? "<"
				+ getSpringLocaleDelegate().getMessage("CollectController.21","keine") + ">" : sChange);
		this.getDetailsPanel().setStatusBarText(sStatus);
	}

	public void setSubFormControllers(Collection<DetailsSubFormController<CollectableEntityObject>> sfcs) {
		this.sfcs.clear();
		this.sfcs.addAll(sfcs);
		for (final DetailsSubFormController<?> sfc : this.sfcs) {
			sfc.getSubForm().getSubformTable().getModel().addTableModelListener(new TableModelListener() {
				@Override
				public void tableChanged(final TableModelEvent e) {
					switch (e.getType()) {
						case TableModelEvent.INSERT:
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									setSubformDefaultValues(null, null);

									if (sfc != null && !sfc.isClosed()) {
										if (sfc.getSubForm().getParentSubForm() != null) {
											for (DetailsSubFormController<?> psfc : DetailsController.this.sfcs) {
												if (psfc.getEntityAndForeignKeyFieldName().getEntityName().equals(sfc.getSubForm().getParentSubForm())) {
													evaluateNewRow(psfc, sfc, e.getFirstRow());
												}
											}
										}
										else {
											evaluateNewRow(null, sfc, e.getFirstRow());
										}
									}
								}
							});	
							break;
						case TableModelEvent.UPDATE:
							if (e.getColumn() >= 0) {
								final CollectableEntityField column = sfc.getCollectableTableModel().getCollectableEntityField(e.getColumn());
								final EntityMetaDataVO meta = MetaDataClientProvider.getInstance().getEntity(column.getEntityName());
								final String key = MessageFormat.format("{0}.{1}.{2}", meta.getNuclet(), meta.getEntity(), column.getName());

								//@see NUCLOSINT-1572. we removed using invokeLater here.
								setSubformDefaultValues(meta.getEntity(), column.getName());

								if (sfc != null && !sfc.isClosed()) {
									if (sfc.getSubForm().getParentSubForm() != null) {
										for (DetailsSubFormController<?> psfc : DetailsController.this.sfcs) {
											if (psfc.getEntityAndForeignKeyFieldName().getEntityName().equals(sfc.getSubForm().getParentSubForm())) {
												process(key, psfc, sfc, e.getFirstRow());
											}
										}
									}
									else {
										process(key, null, sfc, e.getFirstRow());
									}
								}
							}
							break;
						case TableModelEvent.DELETE:
							for (int i = 0; i < sfc.getCollectableTableModel().getColumnCount(); i++) {
								final CollectableEntityField column = sfc.getCollectableTableModel().getCollectableEntityField(i);
								final EntityMetaDataVO meta = MetaDataClientProvider.getInstance().getEntity(column.getEntityName());
								final String key = MessageFormat.format("{0}.{1}.{2}", meta.getNuclet(), meta.getEntity(), column.getName());

								//@see NUCLOSINT-1572. we removed using invokeLater here.
									setSubformDefaultValues(meta.getEntity(), column.getName());

									if (sfc != null && !sfc.isClosed()) {
										if (sfc.getSubForm().getParentSubForm() != null) {
											for (DetailsSubFormController<?> psfc : DetailsController.this.sfcs) {
												if (psfc.getEntityAndForeignKeyFieldName().getEntityName().equals(sfc.getSubForm().getParentSubForm())) {
													process(key, psfc, sfc, e.getFirstRow());
												}
											}
										}
										else {
											process(key, null, sfc, e.getFirstRow());
										}
									}
							}
							break;
					}
				}
			});
		}
	}

	private void process(final String sourceExpression) {
		if (getCollectController().isDetailsChangedIgnored()) {
			//return; //@see NUCLOSINT-1572. stopEditing will cause no update of groovy script changes on save.
		}

		ScriptContext ctx = new CollectControllerScriptContext(getCollectController(), sfcs);
		for (CollectableComponent c : getDetailsPanel().getEditView().getCollectableComponents()) {
			EntityFieldMetaDataVO fieldmeta = MetaDataClientProvider.getInstance().getEntityField(getCollectController().getEntityName(), c.getEntityField().getName());
			if (fieldmeta.getCalculationScript() != null) {
				if (ExpressionParser.contains(fieldmeta.getCalculationScript(), sourceExpression)) {
					if(!this.so.containsScript(fieldmeta.getCalculationScript())) {					
						so.putScript(fieldmeta.getCalculationScript());
						CollectableComponentModel m = getDetailsPanel().getEditModel().getCollectableComponentModelFor(fieldmeta.getField());
						Object o = ScriptEvaluator.getInstance().eval(fieldmeta.getCalculationScript(), ctx, m.getField().getValue());
						setCollectableComponentValue(c, o);
					}
				}
			}

			c.setComponentState(ctx, sourceExpression);
		}
	}

	private void process(final String sourceExpression, final DetailsSubFormController<?> psf, final DetailsSubFormController<?> sf, final Integer row) {
		if (getCollectController().isDetailsChangedIgnored()) {
			//return; //@see NUCLOSINT-1572. stopEditing will cause no update of groovy script changes on save.
		}

		process(sourceExpression);
		final SubForm subform = sf.getSubForm();
		if (subform != null && subform.getParentSubForm() != null) {
			for (DetailsSubFormController<?> sfc : sfcs) {
				if (sfc.getEntityAndForeignKeyFieldName().getEntityName().equals(subform.getParentSubForm())) {
					process(sourceExpression, null, sfc, sfc.getSubForm().getJTable().getSelectionModel().getMinSelectionIndex());
				}
			}
		}
		for (int i = 0; i < sf.getCollectableTableModel().getColumnCount(); i++) {
			CollectableEntityField cef = sf.getCollectableTableModel().getCollectableEntityField(i);
			EntityFieldMetaDataVO fieldmeta = MetaDataClientProvider.getInstance().getEntityField(
					sf.getEntityAndForeignKeyFieldName().getEntityName(), cef.getName());
			if (fieldmeta.getCalculationScript() != null) {
				if (ExpressionParser.contains(fieldmeta.getCalculationScript(), sourceExpression)) {
					if (sf.getCollectableTableModel().getRowCount() > row) {
						if(!this.so.containsScript(fieldmeta.getCalculationScript())) {					
							so.putScript(fieldmeta.getCalculationScript());
							Object o = ScriptEvaluator.getInstance().eval(fieldmeta.getCalculationScript(),
									new SubformControllerScriptContext(getCollectController(), psf, sf, sf.getCollectableTableModel().getRow(row)), null);
							setSubFormValue(sf, row, i, cef, o);
						}
					}
				}
			}
		}
	}

	public void evaluateNewCollectable() {
		for (CollectableComponent c : getDetailsPanel().getEditView().getCollectableComponents()) {
			EntityFieldMetaDataVO fieldmeta = MetaDataClientProvider.getInstance().getEntityField(getCollectController().getEntityName(), c.getEntityField().getName());
			if (fieldmeta.getCalculationScript() != null) {
				CollectableComponentModel m = getDetailsPanel().getEditModel().getCollectableComponentModelFor(fieldmeta.getField());
				Object o = ScriptEvaluator.getInstance().eval(fieldmeta.getCalculationScript(), new CollectControllerScriptContext(getCollectController(), sfcs), m.getField().getValue());
				setCollectableComponentValue(c, o);
			}
		}
	}

	public void evaluateNewRow(final DetailsSubFormController<?> psf, final DetailsSubFormController<?> sf, final Integer row) {
		for (int i = 0; i < sf.getCollectableTableModel().getColumnCount(); i++) {
			CollectableEntityField cef = sf.getCollectableTableModel().getCollectableEntityField(i);
			EntityFieldMetaDataVO fieldmeta = MetaDataClientProvider.getInstance().getEntityField(
					sf.getEntityAndForeignKeyFieldName().getEntityName(), cef.getName());
			if (fieldmeta.getCalculationScript() != null) {
				Object o = ScriptEvaluator.getInstance().eval(fieldmeta.getCalculationScript(),
						new SubformControllerScriptContext(getCollectController(), psf, sf, sf.getSelectedCollectable()), null);
				setSubFormValue(sf, row, i, cef, o);
			}
		}
	}

	private void setCollectableComponentValue(CollectableComponent c, Object value) {
		if (value == ScriptContext.CANCEL) {
			return;
		}

		if (c instanceof CollectableListOfValues) {
			setValueByListOfValues((CollectableListOfValues)c, value);
		}
		else if (c instanceof CollectableComboBox) {
			setValueByComboBox((CollectableComboBox)c, value);
		}
		else {
			c.getModel().setField(new CollectableValueField(value));
		}
	}

	private void setSubFormValue(DetailsSubFormController<?> ctl, int row, int column, CollectableEntityField cef, Object value) {
		if (value == ScriptContext.CANCEL) {
			return;
		}

		TableCellEditor editor = ctl.getTableCellEditor(ctl.getSubForm().getJTable(), row, cef);
		if (editor instanceof CollectableComponentTableCellEditor) {
			CollectableComponent c = ((CollectableComponentTableCellEditor) editor).getCollectableComponent();
			if (c instanceof CollectableListOfValues) {
				setValueByListOfValues((CollectableListOfValues)c, value);
			}
			else if (c instanceof CollectableComboBox) {
				setValueByComboBox((CollectableComboBox)c, value);
			}
			else {
				c.setField(new CollectableValueField(value));
			}
			editor.stopCellEditing();
			try {
				ctl.getCollectableTableModel().setValueAt(c.getField(), row, column);
			}
			catch (CollectableFieldFormatException e) {
				LOG.warn("Failed to set script calculated value.", e);
			}
		}
		else {
			ctl.getCollectableTableModel().setValueAt(new CollectableValueField(value), row, column);
		}
	}

	private void setValueByListOfValues(CollectableListOfValues c, Object value) {
		if (value != null) {
			Collectable clct;
			try {
				clct = Utils.getReferencedCollectable(c.getEntityField().getEntityName(), c.getEntityField().getName(), value);
				c.acceptLookedUpCollectable(clct);
			}
			catch (CommonBusinessException e) {
				LOG.warn("Failed to set script calculated value.", e);
			}
		}
		else {
			c.setField(CollectableValueIdField.NULL);
		}
	}

	private void setValueByComboBox(CollectableComboBox c, Object value) {
		if (value != null) {
			Collectable clct;
			try {
				clct = Utils.getReferencedCollectable(c.getEntityField().getEntityName(), c.getEntityField().getName(), value);
				Object oForeignValue = Utils.getRepresentation(c.getEntityField().getReferencedEntityName(), c.getEntityField().getReferencedEntityFieldName(), clct);
				c.setField(new CollectableValueIdField(value, oForeignValue));
			}
			catch (CommonBusinessException e) {
				LOG.warn("Failed to set script calculated value.", e);
			}
		}
		else {
			c.setField(CollectableValueIdField.NULL);
		}
	}

	public List<DetailsSubFormController<?>> getSubFormControllers() {
		return new ArrayList<DetailsSubFormController<?>>(this.sfcs);
	}

	private void setSubformDefaultValues(String sourceEntity, String sourceField) {
		for (DetailsSubFormController<?> sfc : sfcs) {
			if (sfc.isClosed()) {
				continue;
			}

			for (CollectableEntityField cef : getFieldsWithDefaultValueProvider(sfc, sourceEntity, sourceField)) {
				for (int j = 0; j < sfc.getCollectableTableModel().getRowCount(); j++) {
					CollectableField cfOld = sfc.getCollectableTableModel().getValueAt(j, sfc.getCollectableTableModel().getColumn(cef));
					if (cfOld != null && !cfOld.isNull()) {
						continue;
					}

					TableCellEditor editor = sfc.getTableCellEditor(sfc.getSubForm().getJTable(), j, cef);
					if (editor instanceof CollectableComponentTableCellEditor) {
						CollectableComponent c = ((CollectableComponentTableCellEditor) editor).getCollectableComponent();
						if (c instanceof LabeledCollectableComponentWithVLP) {
							LabeledCollectableComponentWithVLP vlp = (LabeledCollectableComponentWithVLP) c;

							if (vlp.getValueListProvider() != null && vlp.getValueListProvider() instanceof DefaultValueProvider) {
								DefaultValueProvider dvp = (DefaultValueProvider) vlp.getValueListProvider();

								if (dvp.isSupported()) {
									CollectableField cf;
									try {
										cf = dvp.getDefaultValue();
									}
									catch (CommonBusinessException e1) {
										throw new NuclosFatalException(e1);
									}

									if (c instanceof CollectableListOfValues && cf.getValueId() != null) {
										setValueByListOfValues((CollectableListOfValues) c, cf.getValueId());
									}
									else if (c instanceof CollectableComboBox && cf.getValueId() != null) {
										setValueByComboBox((CollectableComboBox)c, cf.getValueId());
									}
									else {
										c.setField(cf);
									}
									editor.stopCellEditing();
									try {
										sfc.getCollectableTableModel().setValueAt(c.getField(), j, sfc.getCollectableTableModel().getColumn(cef));
									}
									catch (CollectableFieldFormatException e) {
										LOG.warn("Failed to set default value.", e);
									}
								}
							}
						}
					}
				}
			}
		}
	}

	private List<CollectableEntityField> getFieldsWithDefaultValueProvider(DetailsSubFormController<?> sfc, String sourceEntity, String sourceField) {
		final List<CollectableEntityField> result = new ArrayList<CollectableEntityField>();
		for (SubForm.Column col : sfc.getSubForm().getColumns()) {
			if (sourceField != null) {
				if (col.getRefreshValueListActions() != null && col.getValueListProvider() != null) {
					if (col.getValueListProvider() instanceof DefaultValueProvider && ((DefaultValueProvider)col.getValueListProvider()).isSupported()) {
						for (RefreshValueListAction act : col.getRefreshValueListActions()) {
							String target = act.getTargetComponentName();
							if (LangUtils.equals(sourceEntity, act.getParentComponentEntityName()) && LangUtils.equals(sourceField, act.getParentComponentName())) {
								for (int i = 0; i < sfc.getCollectableTableModel().getColumnCount(); i++) {
									CollectableEntityField cef = sfc.getCollectableTableModel().getCollectableEntityField(i);
									if (LangUtils.equals(target, cef.getName())) {
										result.add(cef);
									}
								}
							}
						}
					}
				}
			}
			else {
				if (col.getValueListProvider() != null) {
					if (col.getValueListProvider() instanceof DefaultValueProvider && ((DefaultValueProvider)col.getValueListProvider()).isSupported()) {
						for (int i = 0; i < sfc.getCollectableTableModel().getColumnCount(); i++) {
							CollectableEntityField cef = sfc.getCollectableTableModel().getCollectableEntityField(i);
							if (LangUtils.equals(col.getName(), cef.getName())) {
								result.add(cef);
							}
						}
					}
				}
			}
		}
		return result;
	}
	
	class ScriptObserver {
		
		Set<NuclosScript> setScript;
		
		public ScriptObserver() {
			setScript = new HashSet<NuclosScript>();
		}
		
		public void putScript(NuclosScript script) {
			setScript.add(script);
		}
		
		public boolean containsScript(NuclosScript script) {
			boolean contains = false;
			
			if(setScript.contains(script)) {
				setScript.clear();
				contains = true;
			}
			
			return contains;
		}
		
	}
	
}	// class DetailsController
