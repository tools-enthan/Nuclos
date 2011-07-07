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
package org.nuclos.client.task;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.nuclos.client.attribute.AttributeCache;
import org.nuclos.client.common.ClientParameterProvider;
import org.nuclos.client.common.KeyBinding;
import org.nuclos.client.common.KeyBindingProvider;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.common.NuclosCollectControllerFactory;
import org.nuclos.client.common.Utils;
import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.client.genericobject.CollectableGenericObject;
import org.nuclos.client.genericobject.CollectableGenericObjectWithDependants;
import org.nuclos.client.genericobject.GenericObjectClientUtils;
import org.nuclos.client.genericobject.GenericObjectCollectController;
import org.nuclos.client.genericobject.GenericObjectDelegate;
import org.nuclos.client.genericobject.Modules;
import org.nuclos.client.genericobject.ReportController;
import org.nuclos.client.genericobject.ui.EnterNameDescriptionPanel;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.masterdata.CollectableMasterDataWithDependants;
import org.nuclos.client.masterdata.MasterDataCollectController;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.searchfilter.EntitySearchFilter;
import org.nuclos.client.searchfilter.SaveFilterController;
import org.nuclos.client.searchfilter.SaveFilterController.Command;
import org.nuclos.client.searchfilter.SearchFilterCache;
import org.nuclos.client.searchfilter.SearchFilterDelegate;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.SortableCollectableTableModel;
import org.nuclos.client.ui.collect.component.CollectableComponent;
import org.nuclos.client.ui.collect.component.CollectableComponentFactory;
import org.nuclos.client.ui.popupmenu.DefaultJPopupMenuListener;
import org.nuclos.client.ui.popupmenu.JPopupMenuFactory;
import org.nuclos.client.ui.popupmenu.JPopupMenuListener;
import org.nuclos.client.ui.table.TableUtils;
import org.nuclos.common.Actions;
import org.nuclos.common.CollectableEntityFieldWithEntity;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.ParameterProvider;
import org.nuclos.common.attribute.DynamicAttributeVO;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.DefaultCollectableEntityProvider;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.SearchConditionUtils;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.genericobject.GenericObjectUtils;
import org.nuclos.common.masterdata.CollectableMasterDataEntity;
import org.nuclos.common.security.Permission;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.TruncatableCollection;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.genericobject.ProxyList;
import org.nuclos.server.genericobject.searchcondition.CollectableGenericObjectSearchExpression;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.nuclos.server.genericobject.valueobject.GenericObjectWithDependantsVO;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataWithDependantsVO;
import org.nuclos.server.searchfilter.valueobject.SearchFilterVO;

/**
 * Controller for <code>GenericObjectTaskView</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Corina.Mandoki@novabit.de">Corina Mandoki</a>
 * @version 01.00.00
 */
public class GenericObjectTaskController extends RefreshableTaskController {
	
	//private static final Logger log = Logger.getLogger(GenericObjectTaskController.class);
	
	private Map<Integer, GenericObjectTaskView> mpTaskViews;
	
	GenericObjectTaskController(Component parent) {
		super(parent);

		mpTaskViews= new HashMap<Integer, GenericObjectTaskView>();
	}
	
	public GenericObjectTaskView newGenericObjectTaskView(EntitySearchFilter filter) {
		checkFilter(filter);
		final GenericObjectTaskView gotaskview = new GenericObjectTaskView(filter);
		this.mpTaskViews.put(filter.getId(), gotaskview);
		refresh(gotaskview);
		setupActions(gotaskview);
		
		KeyBinding keybinding = KeyBindingProvider.REFRESH;
		gotaskview.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(keybinding.getKeystroke(), keybinding.getKey());
		gotaskview.getActionMap().put(keybinding.getKey(), new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				refresh(gotaskview);
			}
		});

		return gotaskview;
	}
	
	@SuppressWarnings("deprecation")
    private void checkFilter(EntitySearchFilter filter) {
		List<CollectableEntityFieldWithEntity> lstRemovedFields = new ArrayList<CollectableEntityFieldWithEntity>();
		List<CollectableEntityFieldWithEntity> visibleColumns = filter.getVisibleColumns();
		for (CollectableEntityFieldWithEntity column : visibleColumns) {
			if (Modules.getInstance().isModuleEntity(column.getCollectableEntityName())) {
				try {
					MetaDataClientProvider.getInstance().getEntityField(column.getCollectableEntityName(), column.getName());
				} catch (Exception e) {
					lstRemovedFields.add(column);
					System.err.println(column.getName());
				}
//				if (!AttributeCache.getInstance().getNames().contains((column.getName()))) {
//					lstRemovedFields.add(column);
//					System.err.println(column.getName());
//				}
			}
			else {
				if (MasterDataDelegate.getInstance().getMetaData(column.getCollectableEntityName()).getField(column.getName()) == null)
					lstRemovedFields.add(column);
			}
		}
		visibleColumns.removeAll(lstRemovedFields);
		filter.setVisibleColumns(visibleColumns);
	}

	private void setupActions(final GenericObjectTaskView gotaskview) {
		super.addRefreshIntervalActions(gotaskview);
		
		//add mouse listener for double click in table:
		gotaskview.getJTable().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent ev) {
				if (ev.getClickCount() == 2) {
					cmdShowDetails(gotaskview);
				}
			}
		});
		
		gotaskview.btnRefresh.setAction(new AbstractAction("", Icons.getInstance().getIconRefresh16()) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				cmdRefresh(gotaskview);
			}
		});

		gotaskview.btnPrint.setAction(new AbstractAction(CommonLocaleDelegate.getMessage("PersonalTaskController.4","Aufgabenliste drucken"), 
			Icons.getInstance().getIconPrintReport16()) {
			/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				cmdPrint(gotaskview);
			}
		});
		
		gotaskview.btnPrint.setEnabled(false);
		
		if (SecurityCache.getInstance().isActionAllowed(Actions.ACTION_PRINT_TASKLIST)) {
			gotaskview.btnPrint.setEnabled(true);
		}
		
		gotaskview.btnRename.setAction(new AbstractAction(CommonLocaleDelegate.getMessage("ExplorerController.31", "Umbenennen"), Icons.getInstance().getIconClearSearch16()) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent ev) {
				EntitySearchFilter filter = gotaskview.getFilter();
				String newName = cmdRenameFilter(filter);
				if(newName != null)
					Main.getMainController().getTaskController().getTabFor(gotaskview).setTitle(newName);
			}
		});
		
		this.setupPopupMenuListener(gotaskview);
	}	
	
	private void setupRenderers(GenericObjectTaskView view) {
		// setup a table cell renderer for each column:
		for (Enumeration<TableColumn> enumeration = view.getJTable().getColumnModel().getColumns(); enumeration.hasMoreElements();) {
			final TableColumn column = enumeration.nextElement();
			final int iModelIndex = column.getModelIndex();
			final CollectableEntityField clctef = view.getTableModel().getCollectableEntityField(iModelIndex);
			final CollectableComponent clctcomp = CollectableComponentFactory.getInstance().newCollectableComponent(clctef, null, false);
			column.setCellRenderer(clctcomp.getTableCellRenderer());
		}
	}
	
	private void setupPopupMenuListener(final GenericObjectTaskView gotaskview){
		//context menu:		
		final JMenuItem miDetails = new JMenuItem(CommonLocaleDelegate.getMessage("AbstractCollectableComponent.7","Details anzeigen..."));
		final JMenuItem miDefineAsNewSearchResult = new JMenuItem(CommonLocaleDelegate.getMessage("ExplorerController.22", "In Liste anzeigen"));
		
		final JPopupMenuFactory factory = new JPopupMenuFactory() {
			@Override
            public JPopupMenu newJPopupMenu() {
				final JPopupMenu result = new JPopupMenu();
				miDetails.addActionListener(new ActionListener() {
					@Override
                    public void actionPerformed(ActionEvent ev){
						cmdShowDetails(gotaskview);
					}
				});
				miDefineAsNewSearchResult.addActionListener(new ActionListener() {
					@Override
                    public void actionPerformed(ActionEvent ev) {
						cmdDefineSelectedCollectablesAsNewSearchResult(gotaskview);
					}
				});
				result.add(miDetails);
				result.addSeparator();
				result.add(miDefineAsNewSearchResult);
				return result;
			}
		};
		JPopupMenuListener popupMenuListener = new DefaultJPopupMenuListener(factory, true) {
			@Override
			public void mousePressed(MouseEvent ev) {
				if (ev.getClickCount() == 1) {
					// select current row before opening the menu:
					/** @todo factor out this default selection behavior if possible */
					
						final int iRow = gotaskview.getJTable().rowAtPoint(ev.getPoint());												
						if (iRow >= 0) {							
							//Nur, wenn nicht selektiert, selektieren:
							if (!gotaskview.getJTable().isRowSelected(iRow)) {
								if ((ev.getModifiers() & MouseEvent.CTRL_MASK) != 0) {
									// Control gedr\u00fcckt:
									// Zeile zur Selektion hinzuf\u00fcgen:
									gotaskview.getJTable().addRowSelectionInterval(iRow, iRow);
								}
								else {
									// Sonst nur diese Zeile selektieren:
									gotaskview.getJTable().setRowSelectionInterval(iRow, iRow);
								}
							}  // if	
						}
						final int iSelectedRowCount = gotaskview.getJTable().getSelectedRowCount();
						miDetails.setEnabled(iSelectedRowCount == 1 ? true : false);
						miDefineAsNewSearchResult.setEnabled(iSelectedRowCount > 1 ? true : false);								
					}
				
				super.mousePressed(ev);
			}
		};
		
		gotaskview.getJTable().addMouseListener(popupMenuListener);
	}
	
	private void cmdRefresh(final GenericObjectTaskView gotaskview) {
		UIUtils.runCommand(gotaskview, new CommonRunnable() {
			@Override
            public void run() {
				refresh(gotaskview);
			}
		});
	}

	private void cmdPrint(final GenericObjectTaskView gotaskview) {
		UIUtils.runCommand(gotaskview, new CommonRunnable() {
			@Override
            public void run() {
				print(gotaskview);
			}
		});
	}
	
	void refresh(GenericObjectTaskView gotaskview) {
		EntitySearchFilter filter = gotaskview.getFilter();
		Integer iId = filter.getId();
		String sFilterName = filter.getName();
		// the filter definition might have changed:
		try {
			filter = SearchFilterCache.getInstance().getEntitySearchFilterById(iId);
		}
		catch (NoSuchElementException ex) {
			// when the name of the filter has been changed or when the filter has been deleted we don't do anything
		}
		
		if (filter == null) {
			//the filter has been deleted
			Errors.getInstance().showExceptionDialog(this.getParent(), new CommonBusinessException(
				CommonLocaleDelegate.getMessage("GenericObjectTaskController.1", "Der Filter \"{0}\" existiert nicht mehr.", sFilterName)));
		}		
		else {
			if (Modules.getInstance().isModuleEntity(filter.getEntityName())) {
				this.refreshGenericObjectTaskView(filter);
			}
			else {
				this.refreshMasterDataTaskView(filter);
			}
		}
		
	}
	
	@SuppressWarnings("deprecation")
	private void refreshGenericObjectTaskView(EntitySearchFilter filter) {
		/** @todo eliminate this workaround */
		final CollectableGenericObjectSearchExpression clctexpr = new CollectableGenericObjectSearchExpression(filter.getInternalSearchCondition(), filter.getSortingOrder(), filter.getSearchDeleted());

		//final String sMainEntityName = Modules.getInstance().getEntityNameByModuleId(filter.getModuleId());
		final Set<Integer> stRequiredAttributeIds = new HashSet<Integer>(GenericObjectUtils.getAttributeIds(filter.getVisibleColumns(), filter.getEntityName(), AttributeCache.getInstance()));
		final Set<String> stRequiredSubEntityNames = GenericObjectUtils.getSubEntityNames(filter.getVisibleColumns(), filter.getEntityName(), Modules.getInstance());

		// add the attributes 'nuclosState' and 'nuclosStateNumber' to the set of required attributes to be able
		// to check the permission for the resultset 
		stRequiredAttributeIds.add(NuclosEOField.STATE.getMetaData().getId().intValue());
		stRequiredAttributeIds.add(NuclosEOField.STATENUMBER.getMetaData().getId().intValue());
		
		final int iMaxRowCount = ClientParameterProvider.getInstance().getIntValue(ParameterProvider.KEY_MAX_ROWCOUNT_FOR_SEARCHRESULT_IN_TASKLIST, 500);

		final TruncatableCollection<GenericObjectWithDependantsVO> trunccollgovo = GenericObjectDelegate.getInstance().getRestrictedNumberOfGenericObjects(Modules.getInstance().getModuleIdByEntityName(filter.getEntityName()), clctexpr, stRequiredAttributeIds, stRequiredSubEntityNames, iMaxRowCount);
		String sLabel = CommonLocaleDelegate.getMessage("GenericObjectTaskController.2", "{0} Datens\u00e4tze gefunden.", trunccollgovo.totalSize());
		if (trunccollgovo.isTruncated()) {
			sLabel += " " + CommonLocaleDelegate.getMessage("GenericObjectTaskController.3", "Das Ergebnis wurde nach {0} Zeilen abgeschnitten.", trunccollgovo.size());
		}
		//this.mpTaskViews.get(filter).tfStatusBar.setText(sLabel);
		this.mpTaskViews.get(filter.getId()).tfStatusBar.setText(sLabel);

		final List<CollectableGenericObjectWithDependants> lstclct = CollectionUtils.transform(trunccollgovo, new CollectableGenericObjectWithDependants.MakeCollectable());

		for (CollectableGenericObjectWithDependants clctgowd : lstclct) {
			Integer iStatus = clctgowd.getGenericObjectCVO().getAttribute(NuclosEOField.STATE.getMetaData().getField(), AttributeCache.getInstance()).getValueId();
			
			// check permission for attributes
			for (DynamicAttributeVO dynamicAttributeVO : clctgowd.getGenericObjectCVO().getAttributes()) {
				String sAtributeName = AttributeCache.getInstance().getAttribute(dynamicAttributeVO.getAttributeId()).getName();
				
				if (SecurityCache.getInstance().getAttributePermission(Modules.getInstance().getEntityNameByModuleId(clctgowd.getGenericObjectCVO().getModuleId()), sAtributeName, iStatus) == null) {
					dynamicAttributeVO.setValue(null);
				}
			}
			
			// check permission for subforms
			DependantMasterDataMap mpDependants = clctgowd.getGenericObjectWithDependantsCVO().getDependants();
			if (mpDependants != null) {
				for (String sEntityName : mpDependants.getEntityNames()) {
					Map<Integer, Permission> mpPermission = SecurityCache.getInstance().getSubFormPermission(sEntityName);

					if (mpPermission.get(iStatus) == null) {
						clctgowd.getGenericObjectWithDependantsCVO().getDependants().removeKey(sEntityName);
					}
				}
			}
		}
		
		// remove listener from old model, if any:
		final TableModel modelOld = this.mpTaskViews.get(filter.getId()).getJTable().getModel();
		if (modelOld != null && modelOld instanceof SortableCollectableTableModel<?>) {
			TableUtils.removeMouseListenersForSortingFromTableHeader(this.mpTaskViews.get(filter.getId()).getJTable());
		}

		// create a new table model:
		this.mpTaskViews.get(filter.getId()).getJTable().setModel(this.mpTaskViews.get(filter.getId()).newResultTableModel(filter, lstclct));
		//TableUtils.setOptimalColumnWidths(this.mpTaskViews.get(filter.getId()).getJTable());
		TaskController.setColumnWidths(this.mpTaskViews.get(filter.getId()).readColumnWidthsFromPreferences(), this.mpTaskViews.get(filter.getId()).getJTable());
		
		// setup renderer
		setupRenderers(this.mpTaskViews.get(filter.getId()));
	}
	
	private void refreshMasterDataTaskView(EntitySearchFilter filter) {
		CollectableSearchExpression clctsexpr = new CollectableSearchExpression(filter.getInternalSearchCondition(), filter.getSortingOrder());
		//final int iMaxRowCount = ClientParameterProvider.getInstance().getIntValue(ParameterProvider.KEY_MAX_ROWCOUNT_FOR_SEARCHRESULT_IN_TASKLIST, 500);
		//final TruncatableCollection<MasterDataVO> trunccollmdvo = MasterDataDelegate.getInstance().getMasterData(filter.getEntityName(), filter.getInternalSearchCondition(), false);
		ProxyList<MasterDataWithDependantsVO> lst = MasterDataDelegate.getInstance().getMasterDataProxyList(filter.getEntityName(), clctsexpr);
		
		String sLabel = CommonLocaleDelegate.getMessage("GenericObjectTaskController.2", "{0} Datens\u00e4tze gefunden.", lst.size());
//		if (trunccollmdvo.isTruncated()) {
//			sLabel += " Das Ergebnis wurde nach " + trunccollmdvo.size() + " Zeilen abgeschnitten.";
//		}
		this.mpTaskViews.get(filter.getId()).tfStatusBar.setText(sLabel);
		
		final List<CollectableMasterDataWithDependants> lstclct = CollectionUtils.transform(lst, 
				new CollectableMasterDataWithDependants.MakeCollectable((CollectableMasterDataEntity)DefaultCollectableEntityProvider.getInstance().getCollectableEntity(filter.getEntityName())));
		
		// remove listener from old model, if any:
		final TableModel modelOld = this.mpTaskViews.get(filter.getId()).getJTable().getModel();
		if (modelOld != null && modelOld instanceof SortableCollectableTableModel<?>) {
			TableUtils.removeMouseListenersForSortingFromTableHeader(this.mpTaskViews.get(filter.getId()).getJTable());
		}

		// create a new table model:
		this.mpTaskViews.get(filter.getId()).getJTable().setModel(this.mpTaskViews.get(filter.getId()).newResultTableModel(filter, lstclct));
		//TableUtils.setOptimalColumnWidths(this.mpTaskViews.get(filter.getId()).getJTable());
		TaskController.setColumnWidths(this.mpTaskViews.get(filter.getId()).readColumnWidthsFromPreferences(), this.mpTaskViews.get(filter.getId()).getJTable());
		
		// setup renderer
		setupRenderers(this.mpTaskViews.get(filter.getId()));
	}
	
	void print(GenericObjectTaskView gotaskview) {
		try {
			new ReportController(this.getParent()).export(gotaskview.getJTable(), null);
		}
		catch (CommonBusinessException ex) {
			Errors.getInstance().showExceptionDialog(this.getParent(), ex);
		}
	}

	private void cmdShowDetails(final GenericObjectTaskView gotaskview) {
		UIUtils.runCommand(getParent(), new CommonRunnable() {
			@Override
            public void run() throws CommonBusinessException {
				final Collectable clctSelected = getSelectedCollectable(gotaskview);
				if (clctSelected != null) {
					if (Modules.getInstance().isModuleEntity(gotaskview.getFilter().getEntityName())) {
						final CollectableGenericObject clctloSelected = (CollectableGenericObject) clctSelected;
						// we must reload the partially loaded object:
						final int iModuleId = clctloSelected .getGenericObjectCVO().getModuleId();
						GenericObjectClientUtils.showDetails(MainFrame.getPredefinedEntityOpenLocation(MetaDataClientProvider.getInstance().getEntity(new Long(iModuleId)).getEntity()), iModuleId, clctloSelected.getId());
					}
					else {
						final CollectableMasterDataWithDependants clctmdSelected = (CollectableMasterDataWithDependants) clctSelected;
						Main.getMainController().showDetails(clctmdSelected.getCollectableEntity().getName(), clctmdSelected.getId());
					}					
				}
			}
		});
	}
	
	private void cmdDefineSelectedCollectablesAsNewSearchResult(final GenericObjectTaskView gotaskview) {
		UIUtils.runCommand(getParent(), new CommonRunnable(){
			@Override
            public void run() throws CommonBusinessException {
				final Collection<Collectable> collclct = getSelectedCollectables(gotaskview);

				assert CollectionUtils.isNonEmpty(collclct);
				
				final CollectableSearchCondition cond = getCollectableSearchCondition(collclct);
				if (Modules.getInstance().isModuleEntity(gotaskview.getFilter().getEntityName())) {
					final Integer iModuleId = getCommonModuleId(collclct);
					final GenericObjectCollectController ctlGenericObject = NuclosCollectControllerFactory.getInstance().
							newGenericObjectCollectController(MainFrame.getPredefinedEntityOpenLocation(MetaDataClientProvider.getInstance().getEntity(new Long(iModuleId)).getEntity()), iModuleId, null);
					ctlGenericObject.setSearchDeleted(CollectableGenericObjectSearchExpression.SEARCH_BOTH);
					ctlGenericObject.runViewResults(cond);
				}
				else {
					MasterDataCollectController ctlMasterData = NuclosCollectControllerFactory.getInstance().
						newMasterDataCollectController(MainFrame.getPredefinedEntityOpenLocation(gotaskview.getFilter().getEntityName()), gotaskview.getFilter().getEntityName(), null);
					ctlMasterData.runViewResults(cond);
				}				
			}
		});
		
	}
	
	/**
	 * @return the selected collectable, if any.
	 */
	Collectable getSelectedCollectable(final GenericObjectTaskView gotaskview) {
		final int iSelectedRow = gotaskview.getJTable().getSelectedRow();
		Collectable result = null;
		if (iSelectedRow != -1) {
			result = gotaskview.getTableModel().getCollectable(iSelectedRow);
		}
		return result;
	}

	/**
	 * @return the selected collectables, if any
	 */
	List<Collectable> getSelectedCollectables(final GenericObjectTaskView gotaskview) {
		final List<Integer> lstSelectedRowNumbers = CollectionUtils.asList(gotaskview.getJTable().getSelectedRows());
		final List<Collectable> result = CollectionUtils.transform(lstSelectedRowNumbers, new Transformer<Integer, Collectable>() {
			@Override
            public Collectable transform(Integer iRowNo) {
				return gotaskview.getTableModel().getCollectable(iRowNo);
			}
		});
		assert result != null;
		return result;
	}
	
	
	/**
	 * @param collclct
	 * @return
	 * @precondition !CollectionUtils.isNullOrEmpty(collclct)
	 */
	private static CollectableSearchCondition getCollectableSearchCondition(Collection<Collectable> collclct) {
		final Collection<Object> collIds = CollectionUtils.transform(collclct, new Transformer<Collectable, Object>() {
			@Override
            public Object transform(Collectable clct) {
				return clct.getId();
			}
		});

		return SearchConditionUtils.getCollectableSearchConditionForIds(collIds);
	}
	
	/**
	 * @param collclct
	 * @return the module id shared by all collectables, if any.
	 */
	private static Integer getCommonModuleId(Collection<Collectable> collclct) throws CommonPermissionException{
		return Utils.getCommonObject(CollectionUtils.transform(collclct, new Transformer<Collectable, Integer>() {
			@Override
            public Integer transform(Collectable clct) {
				try {								
					return GenericObjectDelegate.getInstance().get((Integer)clct.getId()).getModuleId();
				}catch(CommonBusinessException ex){
					return null;
				}
								 
			}
		}));
	}
	
	public void removeGenericObjectTaskView(GenericObjectTaskView gotaskview) {
		this.mpTaskViews.remove(gotaskview.getFilter().getId());
	}

	@Override
	public ScheduledRefreshable getSingleScheduledRefreshableView() {
		throw new NuclosFatalException("Undefined View in getScheduledRefreshableView in GenericObjectTaskController"); 
	}

	@Override
	public void refreshScheduled(ScheduledRefreshable isRefreshable) {
		if(isRefreshable instanceof GenericObjectTaskView){
			refresh((GenericObjectTaskView)isRefreshable);
		} else {
			throw new NuclosFatalException("Wrong ScheduledRefreshable in refreshScheduled in GenericObjectTaskController");
		}
	}
	
	private String cmdRenameFilter(EntitySearchFilter viewfilter) {
		EntitySearchFilter oldFilter = SearchFilterCache.getInstance().getEntitySearchFilterById(viewfilter.getId());
		String oldFilterName = oldFilter.getName();

		try {
			if(!oldFilter.isEditable())
				throw new NuclosBusinessException(CommonLocaleDelegate.getMessage("SaveFilterController.4","Der Suchfilter darf von Ihnen nicht ge\u00e4ndert werden."));

			EnterNameDescriptionPanel newNamePanel = new EnterNameDescriptionPanel();
			newNamePanel.getTextFieldName().setText(oldFilter.getName());
			newNamePanel.getTextFieldDescription().setText(oldFilter.getDescription());

			int result = SaveFilterController.showDialog(getParent(), CommonLocaleDelegate.getMessage("SaveFilterController.9", "Bestehenden Filter \"{0}\" \u00e4ndern", oldFilterName), newNamePanel, oldFilterName, Command.Overwrite);
			if(result == JOptionPane.OK_OPTION) {
				EntitySearchFilter newFilter = new EntitySearchFilter();

				newFilter.setSearchFilterVO(new SearchFilterVO(oldFilter.getSearchFilterVO()));
				newFilter.setSearchCondition(oldFilter.getSearchCondition());
				newFilter.setVisibleColumns(oldFilter.getVisibleColumns());

				newFilter.setName(newNamePanel.getTextFieldName().getText());
				newFilter.setDescription(newNamePanel.getTextFieldDescription().getText());

				SearchFilterDelegate.getInstance().updateSearchFilter(newFilter, oldFilterName, oldFilter.getOwner());
				return newFilter.getName();
			}
		}
		catch(NoSuchElementException ex) {}
		catch(NuclosBusinessException e) {
			throw new NuclosFatalException(e);
		}
		return null;
	}

}

