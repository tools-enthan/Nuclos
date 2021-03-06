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

package org.nuclos.client.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;

import org.apache.log4j.Logger;
import org.nuclos.client.attribute.AttributeCache;
import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.client.entityobject.CollectableEOEntityClientProvider;
import org.nuclos.client.genericobject.CollectableGenericObjectEntity;
import org.nuclos.client.genericobject.GenericObjectClientUtils;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.main.mainframe.workspace.RestoreUtils;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.common.Actions;
import org.nuclos.common.CollectableEntityFieldWithEntity;
import org.nuclos.common.CollectableEntityFieldWithEntityForExternal;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.WorkspaceDescription;
import org.nuclos.common.WorkspaceDescription.ColumnPreferences;
import org.nuclos.common.WorkspaceDescription.ColumnSorting;
import org.nuclos.common.WorkspaceDescription.Desktop;
import org.nuclos.common.WorkspaceDescription.EntityPreferences;
import org.nuclos.common.WorkspaceDescription.MutableContent;
import org.nuclos.common.WorkspaceDescription.NestedContent;
import org.nuclos.common.WorkspaceDescription.Split;
import org.nuclos.common.WorkspaceDescription.SubFormPreferences;
import org.nuclos.common.WorkspaceDescription.Tabbed;
import org.nuclos.common.WorkspaceDescription.TablePreferences;
import org.nuclos.common.WorkspaceDescription.TasklistPreferences;
import org.nuclos.common.WorkspaceVO;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.PivotInfo;
import org.nuclos.common.entityobject.CollectableEOEntityField;
import org.nuclos.common.entityobject.CollectableEOEntityProvider;
import org.nuclos.common.genericobject.CollectableGenericObjectEntityField;
import org.nuclos.common.masterdata.CollectableMasterDataEntity;
import org.nuclos.common.masterdata.CollectableMasterDataForeignKeyEntityField;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.common.ejb3.PreferencesFacadeRemote;

public class WorkspaceUtils {
	
	private static final Logger LOG = Logger.getLogger(WorkspaceUtils.class);
	
	private static WorkspaceUtils INSTANCE;
	
	// Spring injection
	
	private PreferencesFacadeRemote preferencesFacadeRemote;
	
	private MainFrame mainFrame;
	
	private RestoreUtils restoreUtils;
	
	// end of Spring injection

	WorkspaceUtils() {
		INSTANCE = this;
	}
	
	public static WorkspaceUtils getInstance() {
		return INSTANCE;
	}
	
	public final void setPreferencesFacadeRemote(PreferencesFacadeRemote preferencesFacadeRemote) {
		this.preferencesFacadeRemote = preferencesFacadeRemote;
	}
	
	public final void setMainFrame(MainFrame mainFrame) {
		this.mainFrame = mainFrame;
	}
	
	public final void setRestoreUtils(RestoreUtils restoreUtils) {
		this.restoreUtils = restoreUtils;
	}
	
	public WorkspaceVO getWorkspace() {
		return mainFrame.getWorkspace();
	}
	
	public MainFrame getMainFrame() {
		return mainFrame;
	}
		
	/**
	 * New Fields are added
	 * 
	 * @param ep
	 * @return selected columns with fixed
	 */
	public List<String> getSelectedColumns(EntityPreferences ep) {
		return addNewColumns(getSelectedColumns(ep.getResultPreferences()), ep, false);
	}
	/**
	 * New Fields are added
	 * 
	 * @param sfp
	 * @return selected columns with fixed
	 */
	public List<String> getSelectedColumns(SubFormPreferences sfp) {
		return addNewColumns(getSelectedColumns(sfp.getTablePreferences()), sfp, false);
	}
	/**
	 * @param tp
	 * @return selected columns with fixed
	 */
	public List<String> getSelectedColumns(TablePreferences tp) {
		return CollectionUtils.transform(tp.getSelectedColumnPreferences(), 
				new Transformer<ColumnPreferences, String>() {
					@Override
					public String transform(ColumnPreferences i) {
						return i.getColumn();
					}
				});
	}
	
	
	/**
	 * 
	 * @param ep
	 * @return
	 */
	public List<String> getSelectedEntities(EntityPreferences ep) {
		return getSelectedEntities(ep.getResultPreferences());
	}
	/**
	 * 
	 * @param sfp
	 * @return
	 */
	public List<String> getSelectedEntities(SubFormPreferences sfp) {
		return getSelectedEntities(sfp.getTablePreferences());
	}
	/**
	 * 
	 * @param tp
	 * @return
	 */
	private List<String> getSelectedEntities(TablePreferences tp) {
		return CollectionUtils.transform(tp.getSelectedColumnPreferences(), 
				new Transformer<ColumnPreferences, String>() {
					@Override
					public String transform(ColumnPreferences i) {
						return i.getEntity();
					}
				});
	}

	
	/**
	 * 
	 * @param ep
	 * @return
	 */
	public List<Integer> getFixedWidths(EntityPreferences ep) {
		return getFixedWidths(ep.getResultPreferences());
	}
	/**
	 * 
	 * @param sfp
	 * @return
	 */
	public List<Integer> getFixedWidths(SubFormPreferences sfp) {
		return getFixedWidths(sfp.getTablePreferences());
	}
	/**
	 * 
	 * @param tp
	 * @return
	 */
	private List<Integer> getFixedWidths(TablePreferences tp) {
		return CollectionUtils.transform(
				CollectionUtils.select(tp.getSelectedColumnPreferences(),
						new Predicate<ColumnPreferences>() {
							@Override
							public boolean evaluate(ColumnPreferences t) {
								return t.isFixed();
							}
				}), 
				new Transformer<ColumnPreferences, Integer>(){
					@Override
					public Integer transform(ColumnPreferences i) {
						return i.getWidth();
					}
					;
				});
	}
	
	
	/**
	 * New Fields are added
	 * 
	 * @param ep
	 * @return selected columns WITHOUT fixed
	 */
	public List<String> getSelectedWithoutFixedColumns(EntityPreferences ep) {
		return addNewColumns(getSelectedWithoutFixedColumns(ep.getResultPreferences()), ep, true);
	}
	/**
	 * New Fields are added
	 * 
	 * @param sfp
	 * @return selected columns WITHOUT fixed
	 */
	public List<String> getSelectedWithoutFixedColumns(SubFormPreferences sfp) {
		return addNewColumns(getSelectedWithoutFixedColumns(sfp.getTablePreferences()), sfp, true);
	}
	/**
	 * @param tp
	 * @return selected columns WITHOUT fixed
	 */
	private List<String> getSelectedWithoutFixedColumns(TablePreferences tp) {
		return CollectionUtils.transform(
				CollectionUtils.select(tp.getSelectedColumnPreferences(),
						new Predicate<ColumnPreferences>() {
							@Override
							public boolean evaluate(ColumnPreferences t) {
								return !t.isFixed();
							}
				}), 
				new Transformer<ColumnPreferences, String>(){
					@Override
					public String transform(ColumnPreferences i) {
						return i.getColumn();
					}
					;
				});
	}
	
	
	/**
	 * 
	 * @param ep
	 * @return
	 */
	public List<String> getFixedColumns(EntityPreferences ep) {
		return getFixedColumns(ep.getResultPreferences());
	}
	/**
	 * 
	 * @param sfp
	 * @return
	 */
	public List<String> getFixedColumns(SubFormPreferences sfp) {
		return getFixedColumns(sfp.getTablePreferences());
	}
	/**
	 * 
	 * @param tp
	 * @return
	 */
	private List<String> getFixedColumns(TablePreferences tp) {
		return CollectionUtils.transform(
				CollectionUtils.select(tp.getSelectedColumnPreferences(),
						new Predicate<ColumnPreferences>() {
							@Override
							public boolean evaluate(ColumnPreferences t) {
								return t.isFixed();
							}
				}), 
				new Transformer<ColumnPreferences, String>(){
					@Override
					public String transform(ColumnPreferences i) {
						return i.getColumn();
					}
					;
				});
	}
	
	
	/**
	 * 
	 * @param ep
	 * @return selected columns widths (incl. fixed)
	 */
	public List<Integer> getColumnWidths(EntityPreferences ep) {
		return getColumnWidths(ep.getResultPreferences());
	}
	/**
	 * 
	 * @param sfp
	 * @return selected column widths (incl. fixed)
	 */
	public List<Integer> getColumnWidths(SubFormPreferences sfp) {
		return getColumnWidths(sfp.getTablePreferences());
	}
	/**
	 * 
	 * @param tp
	 * @return selected column widths (incl. fixed)
	 */
	public List<Integer> getColumnWidths(TablePreferences tp) {
		return CollectionUtils.transform(tp.getSelectedColumnPreferences(), 
				new Transformer<ColumnPreferences, Integer>() {
					@Override
					public Integer transform(ColumnPreferences i) {
						return i.getWidth();
					}
				});
	}
	
	
	/**
	 * 
	 * @param ep
	 * @return
	 */
	public List<Integer> getColumnWidthsWithoutFixed(EntityPreferences ep) {
		return getColumnWidthsWithoutFixed(ep.getResultPreferences());
	}
	/**
	 * 
	 * @param sfp
	 * @return
	 */
	public List<Integer> getColumnWidthsWithoutFixed(SubFormPreferences sfp) {
		return getColumnWidthsWithoutFixed(sfp.getTablePreferences());
	}
	/**
	 * 
	 * @param tp
	 * @return
	 */
	private List<Integer> getColumnWidthsWithoutFixed(TablePreferences tp) {
		return CollectionUtils.transform(CollectionUtils.select(tp.getSelectedColumnPreferences(), 
				new Predicate<ColumnPreferences>() {
					@Override
					public boolean evaluate(ColumnPreferences t) {
						return !t.isFixed();
					}
				}), 
				new Transformer<ColumnPreferences, Integer>() {
					@Override
					public Integer transform(ColumnPreferences i) {
						return i.getWidth();
					}
				});
	}
	
	
	/**
	 * 
	 * @param ep
	 * @return
	 */
	public Map<String, Integer> getColumnWidthsMap(EntityPreferences ep) {
		return getColumnWidthsMap(ep.getResultPreferences());
	}
	/**
	 * 
	 * @param sfp
	 * @return
	 */
	public Map<String, Integer> getColumnWidthsMap(SubFormPreferences sfp) {
		return getColumnWidthsMap(sfp.getTablePreferences());
	}
	/**
	 * 
	 * @param tp
	 * @return
	 */
	public Map<String, Integer> getColumnWidthsMap(TablePreferences tp) {
		Map<String, Integer> result = new HashMap<String, Integer>();
		for (ColumnPreferences cp : tp.getSelectedColumnPreferences()) {
			result.put(cp.getColumn(), cp.getWidth());
		}
		return result;
	}
	
	
	/**
	 * 
	 * @param ep
	 * @param ciResolver
	 * @return
	 */
	public List<SortKey> getSortKeys(EntityPreferences ep, final IColumnIndexRecolver ciResolver) {
		return getSortKeys(ep.getResultPreferences(), ciResolver);
	}
	/**
	 * 
	 * @param sfp
	 * @param ciResolver
	 * @return
	 */
	public List<SortKey> getSortKeys(SubFormPreferences sfp, final IColumnIndexRecolver ciResolver) {
		return getSortKeys(sfp.getTablePreferences(), ciResolver);
	}
	/**
	 * 
	 * @param tp
	 * @param ciResolver
	 * @return
	 */
	public List<SortKey> getSortKeys(TablePreferences tp, final IColumnIndexRecolver ciResolver) {
		return CollectionUtils.transform(tp.getColumnSortings(), 
				new Transformer<ColumnSorting, SortKey>(){
					@Override
					public SortKey transform(ColumnSorting i) {
						return new SortKey(ciResolver.getColumnIndex(i.getColumn()),
								i.isAsc()?SortOrder.ASCENDING:SortOrder.DESCENDING);
					}
		});
	}
	
	
	/**
	 * 
	 * @param ep
	 * @param fields
	 * @return
	 */
	public List<CollectableEntityField> getSelectedFields(EntityPreferences ep, List<CollectableEntityField> fields) {
		return getSelectedFields(ep.getResultPreferences(), fields);
	}
	/**
	 * 
	 * @param sfp
	 * @param fields
	 * @return
	 */
	public List<CollectableEntityField> getSelectedFields(SubFormPreferences sfp, List<CollectableEntityField> fields) {
		return getSelectedFields(sfp.getTablePreferences(), fields);
	}
	/**
	 * 
	 * @param tp
	 * @param fields
	 * @return
	 */
	private List<CollectableEntityField> getSelectedFields(TablePreferences tp, List<CollectableEntityField> fields) {
		List<CollectableEntityField> result = new ArrayList<CollectableEntityField>();
		for (ColumnPreferences cp : tp.getSelectedColumnPreferences()) {
			for (CollectableEntityField clctef : fields) {
				if (LangUtils.equals(cp.getColumn(), clctef.getName())) {
					if (cp.getEntity() == null || LangUtils.equals(cp.getEntity(), clctef.getEntityName())) {
						result.add(clctef);
					}
				}
			}
		}
		return result;
	}
	
	
	/**
	 * 
	 * @param ep
	 * @param fields
	 * @return
	 */
	public List<CollectableEntityField> getSelectedWithoutFixedFields(EntityPreferences ep, List<CollectableEntityField> fields) {
		return getSelectedWithoutFixedFields(ep.getResultPreferences(), fields);
	}
	/**
	 * 
	 * @param sfp
	 * @param fields
	 * @return
	 */
	public List<CollectableEntityField> getSelectedWithoutFixedFields(SubFormPreferences sfp, List<CollectableEntityField> fields) {
		return getSelectedWithoutFixedFields(sfp.getTablePreferences(), fields);
	}
	/**
	 * 
	 * @param tp
	 * @param fields
	 * @return
	 */
	private List<CollectableEntityField> getSelectedWithoutFixedFields(TablePreferences tp, List<CollectableEntityField> fields) {
		List<CollectableEntityField> result = new ArrayList<CollectableEntityField>();
		for (ColumnPreferences cp : CollectionUtils.select(tp.getSelectedColumnPreferences(),
				new Predicate<ColumnPreferences>() {
					@Override
					public boolean evaluate(ColumnPreferences t) {
						return !t.isFixed();
					}
				})) {
			for (CollectableEntityField clctef : fields) {
				if (LangUtils.equals(cp.getColumn(), clctef.getName())) {
					result.add(clctef);
				}
			}
		}
		return result;
	}
	
	
	/**
	 * 
	 * @param ep
	 * @param selectedFields
	 * @return
	 */
	public Set<CollectableEntityField> getFixedFields(EntityPreferences ep, List<CollectableEntityField> selectedFields) {
		return getFixedFields(ep.getResultPreferences(), selectedFields);
	}
	/**
	 * 
	 * @param sfp
	 * @param selectedFields
	 * @return
	 */
	public Set<CollectableEntityField> getFixedFields(SubFormPreferences sfp, List<CollectableEntityField> selectedFields) {
		return getFixedFields(sfp.getTablePreferences(), selectedFields);
	}
	/**
	 * 
	 * @param tp
	 * @param selectedFields
	 * @return
	 */
	private Set<CollectableEntityField> getFixedFields(TablePreferences tp, List<CollectableEntityField> selectedFields) {
		Set<CollectableEntityField> result = new HashSet<CollectableEntityField>();
		for (ColumnPreferences cp : tp.getSelectedColumnPreferences()) {
			for (CollectableEntityField clctef : selectedFields) {
				if (LangUtils.equals(cp.getColumn(), clctef.getName()) 
						&& cp.isFixed()) {
					result.add(clctef);
				}
			}
		}
		return result;
	}
	
	
	/**
	 * 
	 * @param ep
	 * @return
	 */
	public List<? extends CollectableEntityField> getCollectableEntityFieldsForGenericObject(EntityPreferences ep) {
		final List<CollectableEntityField> result = new ArrayList<CollectableEntityField>();
		
		final MetaDataClientProvider mdProv = MetaDataClientProvider.getInstance();
		final CollectableEOEntityProvider ceeoProv = CollectableEOEntityClientProvider.getInstance();
		
		for (ColumnPreferences cp : ep.getResultPreferences().getSelectedColumnPreferences()) {
			try {
				switch (cp.getType()) {
				
				case ColumnPreferences.TYPE_EOEntityField:

					if (cp.getPivotSubForm() == null) {
						final EntityFieldMetaDataVO efMeta = mdProv.getEntityField(cp.getEntity(), cp.getColumn());
						result.add(new CollectableEOEntityField(efMeta, cp.getEntity()));
					} else {
						result.add(getPivotField(cp));
					}
					
					break;
					
				case ColumnPreferences.TYPE_GenericObjectEntityField:
					result.add(CollectableGenericObjectEntity.getByModuleId(
							mdProv.getEntity(cp.getEntity()).getId().intValue()).getEntityField(cp.getColumn()));
					break;
					
				case ColumnPreferences.TYPE_MasterDataForeignKeyEntityField:
					CollectableMasterDataForeignKeyEntityField clctef = 
						new CollectableMasterDataForeignKeyEntityField(
							MasterDataDelegate.getInstance().getMetaData(cp.getEntity()).getField(cp.getColumn()), cp.getEntity());
					result.add(clctef);
					clctef.setCollectableEntity(new CollectableMasterDataEntity(MasterDataDelegate.getInstance().getMetaData(cp.getEntity())));
					break;
					
				case ColumnPreferences.TYPE_EntityFieldWithEntityForExternal:
					result.add(GenericObjectClientUtils.getCollectableEntityFieldForResult(
							ceeoProv.getCollectableEntity(cp.getEntity()), 
							cp.getColumn(), 
							ceeoProv.getCollectableEntity(ep.getEntity())));
					break;
					
				case ColumnPreferences.TYPE_EntityFieldWithEntity:
					result.add(new CollectableEntityFieldWithEntity(
							ceeoProv.getCollectableEntity(cp.getEntity()), 
							cp.getColumn()));
					break;
					
				default:
					result.add(GenericObjectClientUtils.getCollectableEntityFieldForResult(
							ceeoProv.getCollectableEntity(cp.getEntity()==null?ep.getEntity():cp.getEntity()), 
							cp.getColumn(), 
							ceeoProv.getCollectableEntity(ep.getEntity())));
				}
			} catch (Exception ex) {
				LOG.error("Column could not be restored " + cp, ex); 
			}
		}
		
		if (SecurityCache.getInstance().isActionAllowed(Actions.ACTION_WORKSPACE_CUSTOMIZE_ENTITY_AND_SUBFORM_COLUMNS)
				|| !mainFrame.getWorkspace().isAssigned()) {
		
			// do not add columns first time...
			if (result.isEmpty() && ep.getResultPreferences().getHiddenColumns().isEmpty()) {
				return result;
			}
			// add new columns
			try {
				for (EntityFieldMetaDataVO efMeta : CollectionUtils.sorted( // order by intid
						MetaDataClientProvider.getInstance().getAllEntityFieldsByEntity(ep.getEntity()).values(),
						new Comparator<EntityFieldMetaDataVO>() {
							@Override
							public int compare(EntityFieldMetaDataVO o1, EntityFieldMetaDataVO o2) {
								return o1.getId().compareTo(o2.getId());
							}
						})) {
					if (NuclosEOField.getByField(efMeta.getField()) != null) {
						// do not add system fields
						continue;
					}
					boolean alreadySelected = false;
					for (CollectableEntityField clctef : result) {
						if (LangUtils.equals(clctef.getName(), efMeta.getField())) {
							// field already selected
							alreadySelected = true;
							break;
						}
					}
					if (alreadySelected) {
						continue;
					}
					if (ep.getResultPreferences().getHiddenColumns().contains(efMeta.getField())) {
						// field is hidden
						continue;
					}
					
					// field is new
					result.add(new CollectableGenericObjectEntityField(
							AttributeCache.getInstance().getAttribute(ep.getEntity(), efMeta.getField()),
							efMeta,
							ep.getEntity()));
				}
			} catch (Exception ex) {
				LOG.error("New columns not added", ex);
			} 
		}
		
		return result;
	}
	
	
	/**
	 * 
	 * @param cp
	 * @return
	 * @throws Exception
	 */
	private CollectableEOEntityField getPivotField(ColumnPreferences cp) throws Exception {
		final MetaDataClientProvider mdProv = MetaDataClientProvider.getInstance();
		final PivotInfo pi = new PivotInfo(cp.getPivotSubForm(), cp.getPivotKeyField(), cp.getPivotValueField(), Class.forName(cp.getPivotValueType()));
		EntityFieldMetaDataVO rightField = null;
		for (EntityFieldMetaDataVO f : mdProv.getAllPivotEntityFields(pi, Collections.singletonList(pi.getValueField()))) {
			if (f.getField().equals(cp.getColumn()) && f.getPivotInfo().equals(pi)) {
				rightField = f;
				break;
			}
		}
		if (rightField == null) {
			throw new Exception("No pivot field found for " + pi);
		} 
		return new CollectableEOEntityField(rightField, cp.getEntity());
	}
	
	
	/**
	 * 
	 * @param ep
	 * @param selectedFields
	 * @param fieldWidths 
	 */
	public void setCollectableEntityFieldsForGenericObject(EntityPreferences ep, List<? extends CollectableEntityField> selectedFields, List<Integer> fieldWidths, List<String> fixedFields) {
		if (SecurityCache.getInstance().isActionAllowed(Actions.ACTION_WORKSPACE_CUSTOMIZE_ENTITY_AND_SUBFORM_COLUMNS)
				|| !mainFrame.getWorkspace().isAssigned()) {
		
			ep.getResultPreferences().removeAllSelectedColumnPreferences();
			LOG.debug("setCollectableEntityFieldsForGenericObject for entity "+ep.getEntity());
			
			final List<CollectableEntityField> fixedEfs = new ArrayList<CollectableEntityField>();
			final List<CollectableEntityField> normalEfs = new ArrayList<CollectableEntityField>();
			// fixed before normal columns...
			for (CollectableEntityField clctef : selectedFields) {
				if (fixedFields.contains(clctef.getName())) {
					fixedEfs.add(clctef);
				} else {
					normalEfs.add(clctef);
				}
			}
			
			final List<CollectableEntityField> efs = new ArrayList<CollectableEntityField>();
			efs.addAll(fixedEfs);
			efs.addAll(normalEfs);
			
			for (int i = 0; i < efs.size(); i++) {
				final CollectableEntityField clctef = efs.get(i); 
				final ColumnPreferences cp = new ColumnPreferences();
				cp.setColumn(clctef.getName());
				cp.setEntity(clctef.getEntityName());
				cp.setFixed(i < fixedEfs.size());
				
				if (clctef instanceof CollectableEOEntityField) {
					cp.setType(ColumnPreferences.TYPE_EOEntityField);
					PivotInfo pi = ((CollectableEOEntityField) clctef).getMeta().getPivotInfo();
					if (pi != null) {
						cp.setPivotSubForm(pi.getSubform());
						cp.setPivotKeyField(pi.getKeyField());
						cp.setPivotValueField(pi.getValueField());
						cp.setPivotValueType(pi.getValueType().getName());
					}
				} else if (clctef instanceof CollectableGenericObjectEntityField) {
					cp.setType(ColumnPreferences.TYPE_GenericObjectEntityField);
				} else if (clctef instanceof CollectableMasterDataForeignKeyEntityField) {
					cp.setType(ColumnPreferences.TYPE_MasterDataForeignKeyEntityField);
				} else if (clctef instanceof CollectableEntityFieldWithEntityForExternal) {
					cp.setType(ColumnPreferences.TYPE_EntityFieldWithEntityForExternal);
				} else if (clctef instanceof CollectableEntityFieldWithEntity) {
					cp.setType(ColumnPreferences.TYPE_EntityFieldWithEntity);
				}
				
				if (fieldWidths.size() > i) {
					cp.setWidth(fieldWidths.get(i));
				}
				
				
				ep.getResultPreferences().addSelectedColumnPreferences(cp);
				LOG.debug(StringUtils.logFormat("setCollectableEntityFieldsForGenericObject",cp.getColumn(),cp.getWidth()));
				
				// remove from hidden
				if (ep.getResultPreferences().getHiddenColumns().contains(cp.getColumn())) {
					ep.getResultPreferences().removeHiddenColumn(cp.getColumn());
				}
			}
		}
	}
	
	
	/**
	 * 
	 * @param sfp
	 * @param fields
	 * @param fieldWidths
	 */
	public void addFixedColumns(SubFormPreferences sfp, List<String> fields, List<Integer> fieldWidths) {
		LOG.debug("addFixedColumns for subform " + sfp.getEntity());
		if (SecurityCache.getInstance().isActionAllowed(Actions.ACTION_WORKSPACE_CUSTOMIZE_ENTITY_AND_SUBFORM_COLUMNS)
				|| !mainFrame.getWorkspace().isAssigned()) {
			
				for (int i = 0; i < fields.size(); i++) {
				if (fieldWidths.size() > i) {
					ColumnPreferences cp = new ColumnPreferences();
					cp.setFixed(true);
					cp.setColumn(fields.get(i));
					cp.setWidth(fieldWidths.get(i));
					sfp.getTablePreferences().addSelectedColumnPreferencesInFront(cp);
					LOG.debug(StringUtils.logFormat("addFixedColumns",cp.getColumn(),cp.getWidth()));
				}
			}
		}
	}
	
	
	/**
	 * 
	 * @param ep
	 * @param fields
	 * @param fieldWidths
	 */
	public void setColumnPreferences(EntityPreferences ep, List<String> fields, List<Integer> fieldWidths) {
		LOG.debug("setColumnPreferences for entity"+ep.getEntity());
		setColumnPreferences(ep.getResultPreferences(), fields, fieldWidths);
	}
	/**
	 * 
	 * @param sfp
	 * @param fields
	 * @param fieldWidths
	 */
	public void setColumnPreferences(SubFormPreferences sfp, List<String> fields, List<Integer> fieldWidths) {
		LOG.debug("setColumnPreferences for subform"+sfp.getEntity());
		setColumnPreferences(sfp.getTablePreferences(), fields, fieldWidths);
	}
	/**
	 * 
	 * @param tp
	 * @param fields
	 */
	public void setColumnPreferences(TablePreferences tp, List<String> fields, List<Integer> fieldWidths) {
		if (SecurityCache.getInstance().isActionAllowed(Actions.ACTION_WORKSPACE_CUSTOMIZE_ENTITY_AND_SUBFORM_COLUMNS)
				|| !mainFrame.getWorkspace().isAssigned()) {
			
			tp.removeAllSelectedColumnPreferences();
			for (int i = 0; i < fields.size(); i++) {
				ColumnPreferences cp = new ColumnPreferences();
				cp.setColumn(fields.get(i));
				if (fieldWidths.size()>i) {
					cp.setWidth(fieldWidths.get(i));
				} else {
					cp.setWidth(75);
				}
				
				tp.addSelectedColumnPreferences(cp);
				LOG.debug(StringUtils.logFormat("setColumnPreference",cp.getColumn(),cp.getWidth()));
				
				// remove from hidden
				if (tp.getHiddenColumns().contains(cp.getColumn())) {
					tp.removeHiddenColumn(cp.getColumn());
				}
			}
		}
	}
	
	
	/**
	 * 
	 * @param ep
	 * @param fields
	 */
	public void updateFixedColumns(EntityPreferences ep, List<String> fields) {
		updateFixedColumns(ep.getResultPreferences(), fields);
	}
	/**
	 * 
	 * @param sfp
	 * @param fields
	 */
	public void updateFixedColumns(SubFormPreferences sfp, List<String> fields) {
		updateFixedColumns(sfp.getTablePreferences(), fields);
	}
	/**
	 * 
	 * @param tp
	 * @param fields
	 */
	private void updateFixedColumns(TablePreferences tp, List<String> fields) {
		if (SecurityCache.getInstance().isActionAllowed(Actions.ACTION_WORKSPACE_CUSTOMIZE_ENTITY_AND_SUBFORM_COLUMNS)
				|| !mainFrame.getWorkspace().isAssigned()) {
			
			for (ColumnPreferences cp : tp.getSelectedColumnPreferences()) {
				if (fields.contains(cp.getColumn())) {
					cp.setFixed(true);
				}
			}
		}
	}
	
	
	/**
	 * 
	 * @param ep
	 * @param sortKeys
	 * @param cnResolver
	 */
	public void setSortKeys(EntityPreferences ep, List<? extends SortKey> sortKeys, IColumnNameResolver cnResolver) {
		LOG.debug("setSortKeys for entity " + ep.getEntity());
		setSortKeys(ep.getResultPreferences(), sortKeys, cnResolver);
	}
	/**
	 * 
	 * @param sfp
	 * @param sortKeys
	 * @param cnResolver
	 */
	public void setSortKeys(SubFormPreferences sfp, List<? extends SortKey> sortKeys, IColumnNameResolver cnResolver) {
		LOG.debug("setSortKeys for subform " + sfp.getEntity());
		setSortKeys(sfp.getTablePreferences(), sortKeys, cnResolver);
	}
	/**
	 * 
	 * @param tp
	 * @param sortKeys
	 * @param cnResolver
	 */
	public void setSortKeys(TablePreferences tp, List<? extends SortKey> sortKeys, IColumnNameResolver cnResolver) {
		if (SecurityCache.getInstance().isActionAllowed(Actions.ACTION_WORKSPACE_CUSTOMIZE_ENTITY_AND_SUBFORM_COLUMNS)
				|| !mainFrame.getWorkspace().isAssigned()) {
			
			tp.removeAllColumnSortings();
			for (SortKey sortKey : sortKeys) {
				if (sortKey.getSortOrder() == SortOrder.UNSORTED)
					continue;
				if (sortKey.getColumn() == -1)
					continue;
				ColumnSorting cs = new ColumnSorting();
				cs.setColumn(cnResolver.getColumnName(sortKey.getColumn()));
				cs.setAsc(sortKey.getSortOrder() == SortOrder.ASCENDING);
				tp.addColumnSorting(cs);
				LOG.debug(StringUtils.logFormat("setSortKeys",cs.getColumn(),(cs.isAsc()?"ASC":"DESC")));
			}
		}
	}
	
	
	public void removeColumnSorting(TablePreferences tp, String column) {
		if (SecurityCache.getInstance().isActionAllowed(Actions.ACTION_WORKSPACE_CUSTOMIZE_ENTITY_AND_SUBFORM_COLUMNS)
				|| !mainFrame.getWorkspace().isAssigned()) {
			
			for (ColumnSorting cs : tp.getColumnSortings()) {
				if (LangUtils.equals(cs.getColumn(), column)) {
					tp.removeColumnSorting(cs);
				}
			}
		}
	}
	
	
	/**
	 * 
	 * @param ep
	 * @param entity
	 */
	public void validatePreferences(EntityPreferences ep) {
		validatePreferences(ep.getResultPreferences(), ep.getEntity());
	}
	/**
	 * 
	 * @param sfp
	 * @param entity
	 */
	public void validatePreferences(SubFormPreferences sfp) {
		validatePreferences(sfp.getTablePreferences(), sfp.getEntity());
	}
	/**
	 * 
	 * @param tp
	 * @param entity
	 */
	private void validatePreferences(TablePreferences tp, String entity) {
		// special entities with custom collect controller
		if (NuclosEntity.getByName(entity) != null) {
			switch (NuclosEntity.getByName(entity)) {
			case RULE :
			case TIMELIMITRULE : 
			case CODE :
				return;
			}
		}
		
		for (ColumnPreferences cp : tp.getSelectedColumnPreferences()) {
			try {	
				if (cp.getPivotSubForm() == null) {
					MetaDataClientProvider.getInstance().getEntityField(
						cp.getEntity()!=null?cp.getEntity():entity, 
								cp.getColumn());
				} else {
					MetaDataClientProvider.getInstance().getEntityField(
							cp.getPivotSubForm(), cp.getPivotKeyField());
					MetaDataClientProvider.getInstance().getEntityField(
							cp.getPivotSubForm(), cp.getPivotValueField());
				}
			} catch (Exception e) {
				tp.removeSelectedColumnPreferences(cp);
				removeColumnSorting(tp, cp.getColumn());
			}
		}
		for (String hidden : tp.getHiddenColumns()) {
			try {
				MetaDataClientProvider.getInstance().getEntityField(entity, hidden);
			} catch (Exception e) {
				tp.removeHiddenColumn(hidden);
			}
		}
	}
	
	
	/**
	 * 
	 * @param selectedFields
	 * @param ep
	 * @return
	 */
	private List<String> addNewColumns(final List<String> selectedFields, final EntityPreferences ep, final boolean ignoreFixed) {
		return addNewColumns(selectedFields, ep.getResultPreferences(), ep.getEntity(), ignoreFixed);
	}
	/**
	 * 
	 * @param selectedFields
	 * @param sfp
	 * @return
	 */
	private List<String> addNewColumns(final List<String> selectedFields, final SubFormPreferences sfp, final boolean ignoreFixed) {
		return addNewColumns(selectedFields, sfp.getTablePreferences(), sfp.getEntity(), ignoreFixed);
	}
	/**
	 * 
	 * @param selectedFields
	 * @param tp
	 * @param entity
	 * @return
	 */
	private List<String> addNewColumns(final List<String> selectedFields, final TablePreferences tp, final String entity, final boolean ignoreFixed) {
		if (SecurityCache.getInstance().isActionAllowed(Actions.ACTION_WORKSPACE_CUSTOMIZE_ENTITY_AND_SUBFORM_COLUMNS)
				|| !mainFrame.getWorkspace().isAssigned()) {
		
			// do not add columns first time...
			if (selectedFields.isEmpty() && tp.getHiddenColumns().isEmpty()) {
				return selectedFields;
			}
			
			try {
				for (EntityFieldMetaDataVO efMeta : CollectionUtils.sorted( // order by intid
						MetaDataClientProvider.getInstance().getAllEntityFieldsByEntity(entity).values(),
						new Comparator<EntityFieldMetaDataVO>() {
							@Override
							public int compare(EntityFieldMetaDataVO o1, EntityFieldMetaDataVO o2) {
								return o1.getId().compareTo(o2.getId());
							}
						})) {
					if (NuclosEOField.getByField(efMeta.getField()) != null) {
						// do not add system fields
						continue;
					}
					if (selectedFields.contains(efMeta.getField())) {
						// field already selected
						continue;
					}
					if (tp.getHiddenColumns().contains(efMeta.getField())) {
						// field is hidden
						continue;
					}
					if (ignoreFixed) {
						boolean bContinue = false;
						for (ColumnPreferences cp : tp.getSelectedColumnPreferences()) {
							if (cp.isFixed() && 
									LangUtils.equals(cp.getColumn(), efMeta.getField())) {
								// field is fixed
								bContinue = true;
								break;
							}
						}
						if (bContinue) {
							continue;
						}
					}
					
					// field is new
					selectedFields.add(efMeta.getField());
				}
			} catch (Exception ex) {
				// not a meta data entity
			} 
		}
		return selectedFields;
	}
	
	
	/**
	 * 
	 * @param ep
	 * @param column
	 */
	public void addHiddenColumn(EntityPreferences ep, String column) {
		addHiddenColumn(ep.getResultPreferences(), column);
	}
	/**
	 * 
	 * @param sfp
	 * @param column
	 */
	public void addHiddenColumn(SubFormPreferences sfp, String column) {
		addHiddenColumn(sfp.getTablePreferences(), column);
	}
	/**
	 * 
	 * @param tp
	 * @param column
	 */
	private void addHiddenColumn(TablePreferences tp, String column) {
		if (SecurityCache.getInstance().isActionAllowed(Actions.ACTION_WORKSPACE_CUSTOMIZE_ENTITY_AND_SUBFORM_COLUMNS)
				|| !mainFrame.getWorkspace().isAssigned()) {
			
			tp.addHiddenColumn(column);
		}
	}
	
	
	/**
	 * 
	 * @param ep
	 * @param fields
	 */
	public void addMissingPivotFields(EntityPreferences ep, List<CollectableEntityField> fields) {
		for (ColumnPreferences cp : ep.getResultPreferences().getSelectedColumnPreferences()) {
			try {
				if (cp.getPivotSubForm() != null) {
					CollectableEOEntityField pivotField = getPivotField(cp);
					if (!fields.contains(pivotField))
						fields.add(pivotField);
				}
			} catch (Exception e) {
				LOG.error("Column could not be restored " + cp, e); 
			}
		}
	}
	
	
	public Desktop restoreDesktop(Desktop dsktp) throws CommonBusinessException {
		final WorkspaceVO currentWovo = mainFrame.getWorkspace();
		restoreUtils.storeWorkspace(currentWovo);
		final Long assignedWorkspaceId = mainFrame.getWorkspace().getAssignedWorkspace();
		boolean restoreToSystemDefault = false;
		
		if (assignedWorkspaceId == null) {
			// restore to first time
			restoreToSystemDefault = true;
		} else {
			final WorkspaceDescription assignedWd = preferencesFacadeRemote.getWorkspace(assignedWorkspaceId).getWoDesc();
			if (preferencesFacadeRemote.isWorkspaceStructureChanged(assignedWorkspaceId, mainFrame.getWorkspace().getId())) {
				throw new CommonBusinessException(SpringLocaleDelegate.getInstance().getMessage(
						"Desktop.not.restoreable", "Desktop kann nicht zurückgesetzt werden. Die Struktur der Vorlage entspricht nicht der aktuellen Arbeitsumgebung."));
			} else {
				// find tabbed in current workspace...
				Desktop dsktpAssigned = getDesktopFromTargetWorkspace(
						currentWovo.getWoDesc().getMainFrame().getContent(), 
						dsktp, 
						assignedWd.getMainFrame().getContent());
				if (dsktpAssigned == null) {
					restoreToSystemDefault = true;
				} else {
					return dsktpAssigned;
				}
			}
		}
		
		if (restoreToSystemDefault) {
			return null;
		} 
		
		return null;
	}
	
	
	private Desktop getDesktopFromTargetWorkspace(NestedContent ncSource, Desktop dsktpSource, NestedContent ncTarget) {
		try {
			if (ncSource instanceof MutableContent) {
				return getDesktopFromTargetWorkspace(((MutableContent) ncSource).getContent(), dsktpSource, ((MutableContent) ncTarget).getContent());
			} else if (ncSource instanceof Split) {
				Desktop splitResult = getDesktopFromTargetWorkspace(((Split) ncSource).getContentA(), dsktpSource, ((Split) ncTarget).getContentA());
				if (splitResult == null) 
					splitResult = getDesktopFromTargetWorkspace(((Split) ncSource).getContentB(), dsktpSource, ((Split) ncTarget).getContentB());
				return splitResult;
			} else if (ncSource instanceof Tabbed) {
				if (((Tabbed)ncSource).getDesktop() != null && 
						((Tabbed)ncSource).getDesktop() == dsktpSource) {
					return ((Tabbed)ncTarget).getDesktop();
				}
			}
		} catch (Exception ex) {
			LOG.error(ex);
			// structure change
		}
		
		return null;
	}
	
	
	/**
	 * 
	 * @param ep
	 * @throws CommonBusinessException 
	 */
	public void restoreEntityPreferences(EntityPreferences ep) throws CommonBusinessException {
		final Long assignedWorkspaceId = mainFrame.getWorkspace().getAssignedWorkspace();
		boolean restoreToSystemDefault = false;
		
		if (assignedWorkspaceId == null) {
			// restore to first time
			restoreToSystemDefault = true;
		} else {
			final WorkspaceDescription assignedWd = preferencesFacadeRemote.getWorkspace(assignedWorkspaceId).getWoDesc();
			if (assignedWd.containsEntityPreferences(ep.getEntity())) {
				final TablePreferences assignedTp = assignedWd.getEntityPreferences(ep.getEntity()).getResultPreferences();
				if (assignedTp.getSelectedColumnPreferences().isEmpty()) {
					restoreToSystemDefault = true;
				} else {
					ep.getResultPreferences().clearAndImport(assignedTp);
				}
			} else {
				restoreToSystemDefault = true;
			}
		}
		
		if (restoreToSystemDefault) {
			ep.clearResultPreferences();
		}
	}
	
	
	/**
	 * 
	 * @param sfp
	 * @param mainEntity
	 * @throws CommonBusinessException 
	 */
	public void restoreSubFormPreferences(SubFormPreferences sfp, String mainEntity) throws CommonBusinessException {
		final Long assignedWorkspaceId = mainFrame.getWorkspace().getAssignedWorkspace();
		boolean restoreToSystemDefault = false;
		
		if (assignedWorkspaceId == null) {
			// restore to first time
			restoreToSystemDefault = true;
		} else {
			final WorkspaceDescription assignedWd = preferencesFacadeRemote.getWorkspace(assignedWorkspaceId).getWoDesc();
			if (assignedWd.containsEntityPreferences(mainEntity)) {
				final TablePreferences assignedTp = assignedWd.getEntityPreferences(mainEntity).getSubFormPreferences(sfp.getEntity()).getTablePreferences();
				if (assignedTp.getSelectedColumnPreferences().isEmpty()) {
					restoreToSystemDefault = true;
				} else {
					sfp.getTablePreferences().clearAndImport(assignedTp);
					validatePreferences(sfp);
				}
			} else {
				restoreToSystemDefault = true;
			}
		}
		
		if (restoreToSystemDefault) {
			sfp.clearTablePreferences();
		}
	}
	
	public void restoreTasklistPreferences(TasklistPreferences tp) throws CommonBusinessException {
		final Long assignedWorkspaceId = mainFrame.getWorkspace().getAssignedWorkspace();
		boolean restoreToSystemDefault = false;
		
		if (assignedWorkspaceId == null) {
			// restore to first time
			restoreToSystemDefault = true;
		} else {
			final WorkspaceDescription assignedWd = preferencesFacadeRemote.getWorkspace(assignedWorkspaceId).getWoDesc();
			if (assignedWd.containsTasklistPreferences(tp.getType(), tp.getName())) {
				final TablePreferences assignedTp = assignedWd.getTasklistPreferences(tp.getType(), tp.getName()).getTablePreferences();
				if (assignedTp.getSelectedColumnPreferences().isEmpty()) {
					restoreToSystemDefault = true;
				} else {
					tp.getTablePreferences().clearAndImport(assignedTp);
				}
			} else {
				restoreToSystemDefault = true;
			}
		}
		
		if (restoreToSystemDefault) {
			tp.clearTablePreferences();
		}
	}
	
	public interface IColumnNameResolver {
		public String getColumnName(int iColumn);
	}
	
	public interface IColumnIndexRecolver {
		public int getColumnIndex(String columnIdentifier);
	}
}
