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
package org.nuclos.server.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.nuclos.common.AbstractProvider;
import org.nuclos.common.JMSConstants;
import org.nuclos.common.MetaDataProvider;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.dal.vo.PivotInfo;
import org.nuclos.common.transport.GzipMap;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.dal.DalUtils;
import org.nuclos.server.dal.processor.jdbc.impl.DynamicMetaDataProcessor;
import org.nuclos.server.dal.provider.NucletDalProvider;
import org.nuclos.server.dal.provider.NuclosDalProvider;
import org.nuclos.server.database.DataBaseHelper;
import org.nuclos.server.dblayer.EntityObjectMetaDbHelper;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbSelection;
import org.nuclos.server.genericobject.GenericObjectMetaDataCache;
import org.nuclos.server.genericobject.Modules;
import org.nuclos.server.jms.NuclosJMSUtils;
import org.nuclos.server.report.SchemaCache;

/**
 * An caching singleton for accessing the meta data information 
 * on the server side.
 */
public class MetaDataServerProvider extends AbstractProvider implements MetaDataProvider{

	//private final ClientNotifier clientnotifier = new ClientNotifier(JMSConstants.TOPICNAME_METADATACACHE);

	private final DataCache dataCache = new DataCache();

	private MetaDataServerProvider(){
		dataCache.buildMaps();
	}

	public static MetaDataServerProvider getInstance() {
		return (MetaDataServerProvider) SpringApplicationContextHolder.getBean("metaDataProvider");
	}

	/**
	 *
	 * @return
	 */
	@Override
    public Collection<EntityMetaDataVO> getAllEntities() {
		return  new ArrayList<EntityMetaDataVO>(dataCache.getMapMetaDataByEntity().values());
	}

	/**
	 *
	 * @return
	 */
	public Collection<EntityMetaDataVO> getNucletEntities() {
		Collection<EntityMetaDataVO> result = new ArrayList<EntityMetaDataVO>();

		for (EntityMetaDataVO metaVO : dataCache.getMapMetaDataByEntity().values()) {
			if (org.nuclos.server.dal.processor.nuclet.JdbcEntityMetaDataProcessor.class.getName().equals(metaVO.processor())) {
				result.add(metaVO);
			}
		}

		return result;
	}

	/**
	 *
	 * @param id
	 * @return
	 */
	@Override
    public EntityMetaDataVO getEntity(Long id) {
		final EntityMetaDataVO result = dataCache.getMapMetaDataById().get(id);
		if (result == null) {
			throw new CommonFatalException("entity with id " + id + " does not exists.");
		}
		return result;
	}

	/**
	 *
	 * @param entity
	 * @return
	 */
	@Override
    public EntityMetaDataVO getEntity(String entity) {
		final EntityMetaDataVO result = dataCache.getMapMetaDataByEntity().get(entity);
		if (result == null) {
			throw new CommonFatalException("entity " + entity + " does not exists.");
		}
		return result;
	}

	/**
	 *
	 * @param entity
	 * @return
	 */
	@Override
    public EntityMetaDataVO getEntity(NuclosEntity entity) {
		final EntityMetaDataVO result = dataCache.getMapMetaDataByEntity().get(entity.getEntityName());
		if (result == null) {
			throw new CommonFatalException("entity " + entity + " does not exists.");
		}
		return result;
	}

	/**
	 *
	 * @param entity
	 * @return
	 */
	@Override
    public Map<String, EntityFieldMetaDataVO> getAllEntityFieldsByEntity(String entity) {
		final Map<String, EntityFieldMetaDataVO> result = dataCache.getMapFieldMetaData().get(entity);
		if (result == null) {
			return Collections.emptyMap();
		}
		return result;
	}

	@Override
	public Map<String, EntityFieldMetaDataVO> getAllPivotEntityFields(PivotInfo info) {
		final EntityMetaDataVO subform = getEntity(info.getSubform());
		final String subformTable = EntityObjectMetaDbHelper.getTableName(subform);
		final EntityFieldMetaDataVO keyField = getEntityField(info.getSubform(), info.getKeyField());
		// final EntityFieldMetaDataVO valueField = getEntityField(info.getSubform(), info.getValueField());
		
		Map<String, EntityFieldMetaDataVO> result = dataCache.getMapPivotMetaData().get(info);
		if (result == null) {
			// select distinct p.<keyfield> from <subform> p 
			DbQuery<String> query = DataBaseHelper.getDbAccess().getQueryBuilder().createQuery(String.class);
			DbFrom from = query.distinct(true).from(subformTable).alias("p");
			query.select(from.column(keyField.getDbColumn(), String.class)).maxResults(40);
			List<String> columns = DataBaseHelper.getDbAccess().executeQuery(query);
			//
			result = new HashMap<String, EntityFieldMetaDataVO>(columns.size());
			for (String c: columns) {
				final EntityObjectVO vo = new EntityObjectVO();
				vo.initFields(columns.size(), 1);
				vo.setEntity(info.getSubform());
				// vo.setDependants(mpDependants);
				
				final EntityFieldMetaDataVO md = new EntityFieldMetaDataVO(vo);
				md.setDynamic(true);
				md.setDbColumn(c);
				md.setField(c);
				md.setNullable(Boolean.TRUE);
				
				result.put(c, md);
			}
			dataCache.getMapPivotMetaData().put(info, result);
		}
		return result;
	}
	
	public Map<String, Map<String, EntityFieldMetaDataVO>> getAllEntityFieldsByEntitiesGz(Collection<String> entities) {
		// We can simply iterate most inefficiently over the single get results,
		// as these depend on caches themselves. All in all, the only thing
		// happening here is an in-memory cache transformation
	    GzipMap<String, Map<String, EntityFieldMetaDataVO>> res = new GzipMap<String, Map<String,EntityFieldMetaDataVO>>();
	    for(String entityName : entities)
	    	res.put(entityName, getAllEntityFieldsByEntity(entityName));
	    return res;
    }


	/**
	 *
	 * @param entity
	 * @param field
	 * @return
	 */
	@Override
    public EntityFieldMetaDataVO getEntityField(String entity, String field) {
		final EntityFieldMetaDataVO result = getAllEntityFieldsByEntity(entity).get(field);
		if (result == null) {
			throw new CommonFatalException("entity field " + field + " in " + entity+ " does not exists.");
		}
		return result;
	}

	/**
	 * 
	 * @param entity
	 * @param field
	 * @return
	 */
	public EntityFieldMetaDataVO getEntityField(NuclosEntity entity, String field) {
		return getEntityField(entity.getEntityName(), field);
	}
	
	@Override
	public EntityFieldMetaDataVO getEntityField(String entity, Long fieldId) {
		for(EntityFieldMetaDataVO fieldMeta : getAllEntityFieldsByEntity(entity).values())
			if(fieldMeta.getId().equals(fieldId))
				return fieldMeta;
		throw new CommonFatalException("entity field with id=" + fieldId + " in " + entity + " does not exists.");
	}

	public synchronized void revalidate(){
		dataCache.buildMaps();
		NucletDalProvider.getInstance().revalidate();

		/** re-/invalidate old caches */
		SchemaCache.getInstance().invalidate();
		MasterDataMetaCache.getInstance().revalidate();
		AttributeCache.getInstance().revalidate();
		Modules.getInstance().invalidate();
		GenericObjectMetaDataCache.getInstance().layoutChanged(null);

		debug("Notified clients that meta data changed.");
		NuclosJMSUtils.sendMessage("Meta data changed.", JMSConstants.TOPICNAME_METADATACACHE);
	}

	@Override
	protected void finalize() throws Throwable {
		//this.clientnotifier.close();
		super.finalize();
	}

	/**
	 *
	 *
	 */
	class DataCache {
		private boolean revalidating = false;

		private long startRevalidating;

		private Map<String, EntityMetaDataVO> mapMetaDataByEntity = null;
		private Map<Long, EntityMetaDataVO> mapMetaDataById = null;
		private Map<String, Map<String, EntityFieldMetaDataVO>> mapFieldMetaData = null;
		private ConcurrentHashMap<PivotInfo, Map<String, EntityFieldMetaDataVO>> mapPivotMetaData = new ConcurrentHashMap<PivotInfo, Map<String,EntityFieldMetaDataVO>>();

		public Map<String, EntityMetaDataVO> getMapMetaDataByEntity() {
			if (isRevalidating()) {
				return getMapMetaDataByEntity();
			} else {
				return mapMetaDataByEntity;
			}
		}

		private Map<String, EntityMetaDataVO> buildMapMetaDataByEntity() {
			Map<String, EntityMetaDataVO> result = new HashMap<String, EntityMetaDataVO>();
			/**
			 * Nuclet Entities
			 */
			for (EntityMetaDataVO eMeta : NucletDalProvider.getInstance().getEntityMetaDataProcessor().getAll()){
				result.put(eMeta.getEntity(), eMeta);
			}

			/**
			 * Nuclos Entities
			 */
			for (EntityMetaDataVO eMeta : NuclosDalProvider.getInstance().getEntityMetaDataProcessor().getAll()){
				result.put(eMeta.getEntity(), eMeta);
			}

			for(EntityMetaDataVO meta : DynamicMetaDataProcessor.getDynamicEntities())
				result.put(meta.getEntity(), meta);

			return result;
		}

		public Map<Long, EntityMetaDataVO> getMapMetaDataById() {
			if (isRevalidating()) {
				return getMapMetaDataById();
			} else {
				return mapMetaDataById;
			}
		}

		private Map<Long, EntityMetaDataVO> buildMapMetaDataById(Map<String, EntityMetaDataVO> metaDataByEntity) {
			Map<Long, EntityMetaDataVO> result = new HashMap<Long, EntityMetaDataVO>();
			for(EntityMetaDataVO v : metaDataByEntity.values())
				result.put(v.getId(), v);
			return result;
		}

		public Map<String, Map<String, EntityFieldMetaDataVO>> getMapFieldMetaData() {
			if (isRevalidating()) {
				return getMapFieldMetaData();
			} else {
				return mapFieldMetaData;
			}
		}

		public Map<PivotInfo, Map<String, EntityFieldMetaDataVO>> getMapPivotMetaData() {
			if (isRevalidating()) {
				return getMapPivotMetaData();
			} else {
				return mapPivotMetaData;
			}
		}

		private Map<String, Map<String, EntityFieldMetaDataVO>> buildMapFieldMetaData(Map<String, EntityMetaDataVO> mapMetaDataByEntity) {
			Map<String, Map<String, EntityFieldMetaDataVO>> result = new HashMap<String, Map<String,EntityFieldMetaDataVO>>();

			/**
			 * Nuclet Entities
			 */
			for (EntityMetaDataVO eMeta : NucletDalProvider.getInstance().getEntityMetaDataProcessor().getAll()){
				List<EntityFieldMetaDataVO> entityFields = NucletDalProvider.getInstance().getEntityFieldMetaDataProcessor().getByParent(eMeta.getEntity());
				DalUtils.addNucletEOSystemFields(entityFields, eMeta);

				result.put(eMeta.getEntity(), new HashMap<String, EntityFieldMetaDataVO>());
				for (EntityFieldMetaDataVO efMeta : entityFields) {
					result.get(eMeta.getEntity()).put(efMeta.getField(), efMeta);
				}
			}

			/**
			 * Nuclos Entities
			 */
			for (EntityMetaDataVO eMeta : NuclosDalProvider.getInstance().getEntityMetaDataProcessor().getAll()){
				List<EntityFieldMetaDataVO> entityFields = NuclosDalProvider.getInstance().getEntityFieldMetaDataProcessor().getByParent(eMeta.getEntity());

				result.put(eMeta.getEntity(), new ConcurrentHashMap<String, EntityFieldMetaDataVO>());
				for (EntityFieldMetaDataVO efMeta : entityFields) {
					result.get(eMeta.getEntity()).put(efMeta.getField(), efMeta);
				}
			}

			for(String dyna : DynamicMetaDataProcessor.getDynamicEntityViews()) {
				String entity = DynamicMetaDataProcessor.getEntityNameFromDynamicViewName(dyna);
				Long entityId = mapMetaDataByEntity.containsKey(entity)?mapMetaDataByEntity.get(entity).getId():null;
				if (entityId != null) {
					result.put(entity, DynamicMetaDataProcessor.getDynamicFieldsForView(dyna, entityId));
				}
			}


			return result;
		}

		public synchronized void buildMaps() {
			startRevalidating = System.currentTimeMillis();
			revalidating = true;
			mapMetaDataByEntity = Collections.unmodifiableMap(buildMapMetaDataByEntity());
			mapMetaDataById = Collections.unmodifiableMap(buildMapMetaDataById(mapMetaDataByEntity));
			mapFieldMetaData = Collections.unmodifiableMap(buildMapFieldMetaData(mapMetaDataByEntity));
			mapPivotMetaData.clear();
			revalidating = false;
		}

		private boolean isRevalidating() {
			if (revalidating) {
				try {
					if (startRevalidating + 1000l*60 < System.currentTimeMillis())
						throw new NuclosFatalException("nuclos.metadata.revalidation.error.2");
					Thread.sleep(1000);
					return true;
				}
				catch(InterruptedException e) {
					throw new NuclosFatalException(e);
				}
			} else {
				return false;
			}
		}
	} // class DataCache

}
