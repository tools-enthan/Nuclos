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
package org.nuclos.server.masterdata.ejb3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.nuclos.common.MetaDataProvider;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common.collect.collectable.MakeCollectableValueIdField;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparison;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.entityobject.CollectableEOEntityField;
import org.nuclos.common2.EntityAndFieldName;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.StringUtils;
import org.nuclos.server.attribute.ejb3.LayoutFacadeLocal;
import org.nuclos.server.common.DatasourceCache;
import org.nuclos.server.common.DatasourceServerUtils;
import org.nuclos.server.common.MetaDataServerProvider;
import org.nuclos.server.common.ServerServiceLocator;
import org.nuclos.server.common.ejb3.NuclosFacadeBean;
import org.nuclos.server.dal.processor.jdbc.TableAliasSingleton;
import org.nuclos.server.dal.processor.jdbc.impl.EOSearchExpressionUnparser;
import org.nuclos.server.dal.processor.nuclet.JdbcEntityObjectProcessor;
import org.nuclos.server.dal.provider.NucletDalProvider;
import org.nuclos.server.dblayer.DbAccess;
import org.nuclos.server.dblayer.DbTuple;
import org.nuclos.server.dblayer.EntityObjectMetaDbHelper;
import org.nuclos.server.dblayer.query.DbCompoundColumnExpression;
import org.nuclos.server.dblayer.query.DbCondition;
import org.nuclos.server.dblayer.query.DbExpression;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbOrder;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.nuclos.server.report.valueobject.DatasourceVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Facade bean for all master data and modul data management functions.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
@Transactional(noRollbackFor= {Exception.class})
public class EntityFacadeBean extends NuclosFacadeBean implements EntityFacadeRemote {

	private static final Logger LOG = Logger.getLogger(EntityFacadeBean.class);

	private static final String FIELDNAME_ACTIVE = "active";
	private static final String FIELDNAME_VALIDFROM = "validFrom";
	private static final String FIELDNAME_VALIDUNTIL = "validUntil";

	/**
	 * @deprecated
	 */
	private MasterDataFacadeHelper helper;
	
	private DatasourceServerUtils datasourceServerUtils;
	
	private DatasourceCache datasourceCache;

	public EntityFacadeBean() {
	}
	
	@Autowired
	void setMasterDataFacadeHelper(MasterDataFacadeHelper masterDataFacadeHelper) {
		this.helper = masterDataFacadeHelper;
	}
	
	@Autowired
	void setDatasourceServerUtils(DatasourceServerUtils datasourceServerUtils) {
		this.datasourceServerUtils = datasourceServerUtils;
	}
	
	@Autowired
	void setDatasourceCache(DatasourceCache datasourceCache) {
		this.datasourceCache = datasourceCache;
	}
	
	/**
	 * @ejb.interface-method view-type="remote"
	 * @ejb.permission role-name="Login"
	 */
	public Map<EntityAndFieldName, String> getSubFormEntityAndParentSubFormEntityNames(Integer iLayoutId) {
		return ServerServiceLocator.getInstance().getFacade(LayoutFacadeLocal.class).getSubFormEntityAndParentSubFormEntityNamesByLayoutId(iLayoutId);
	}

	/**
	 * @param sEntityName
	 * @param sFieldName masterdata field name
	 * @param bCheckValidity Test for active sign and validFrom/validUntil
	 * @return list of collectable fields
	 * @todo this method should be used in CollectableFieldsProviders
	 */
	public List<CollectableField> getCollectableFieldsByName(
		String sEntityName,
		String sFieldName,
		boolean bCheckValidity) {

		MakeCollectableValueIdField clctMaker = new MakeCollectableValueIdField(sFieldName);

		CollectableSearchCondition clctcond = null;
		if (bCheckValidity) {
			Collection<String> collFieldNames = MetaDataServerProvider.getInstance().getAllEntityFieldsByEntity(sEntityName).keySet();
			boolean bContainsActive = collFieldNames.contains(FIELDNAME_ACTIVE);
			boolean bContainsValidFromAndUntil = collFieldNames.contains(FIELDNAME_VALIDFROM) && collFieldNames.contains(FIELDNAME_VALIDUNTIL);
			Date dateNow = new Date(System.currentTimeMillis());

			CollectableSearchCondition clctcondActiv = null;
			CollectableSearchCondition clctcondValids = null;

			if (bContainsActive)
				clctcondActiv = SearchConditionUtils.newEOComparison(sEntityName, FIELDNAME_ACTIVE, ComparisonOperator.EQUAL, Boolean.TRUE, MetaDataServerProvider.getInstance());

			if (bContainsValidFromAndUntil) {
				clctcondValids = SearchConditionUtils.and(
					SearchConditionUtils.or(
						SearchConditionUtils.newEOComparison(sEntityName, FIELDNAME_VALIDFROM,  ComparisonOperator.LESS_OR_EQUAL,   dateNow, MetaDataServerProvider.getInstance()),
						SearchConditionUtils.newEOIsNullComparison(sEntityName, FIELDNAME_VALIDFROM,  ComparisonOperator.IS_NULL, MetaDataServerProvider.getInstance())),
					SearchConditionUtils.or(
						SearchConditionUtils.newEOComparison(sEntityName, FIELDNAME_VALIDUNTIL, ComparisonOperator.GREATER_OR_EQUAL, dateNow, MetaDataServerProvider.getInstance()),
						SearchConditionUtils.newEOIsNullComparison(sEntityName, FIELDNAME_VALIDUNTIL, ComparisonOperator.IS_NULL, MetaDataServerProvider.getInstance()))
					);
			}

			if (clctcondActiv != null && clctcondValids != null) {
				clctcond = SearchConditionUtils.and(clctcondActiv, clctcondValids);
			} else {
				if (clctcondActiv != null)
					clctcond = clctcondActiv;
				else
					clctcond = clctcondValids;
			}
		}

		final EntityFieldMetaDataVO efDeleted = MetaDataServerProvider.getInstance().getAllEntityFieldsByEntity(sEntityName).get(clctEOEFdeleted.getName());
		if (efDeleted != null) {
			CollectableSearchCondition condSearchDeleted = new CollectableComparison(clctEOEFdeleted, ComparisonOperator.EQUAL, new CollectableValueField(false));
			clctcond = clctcond == null ? condSearchDeleted : SearchConditionUtils.and(clctcond, condSearchDeleted);
		}

		JdbcEntityObjectProcessor eoProcessor = NucletDalProvider.getInstance().getEntityObjectProcessor(sEntityName);
		List<EntityObjectVO> eoResult = eoProcessor.getBySearchExpression(clctMaker.getFields(), new CollectableSearchExpression(appendRecordGrants(clctcond, sEntityName)), null, false, false);

		return CollectionUtils.sorted(CollectionUtils.transform(eoResult, clctMaker), new Comparator<CollectableField>() {

			@Override
			public int compare(CollectableField o1, CollectableField o2) {
				return LangUtils.compare(o1.getValue(), o2.getValue());
			}});
	}
	
	private static final CollectableEOEntityField clctEOEFdeleted = new CollectableEOEntityField(NuclosEOField.LOGGICALDELETED.getMetaData(), "<dummy>");

	public List<CollectableValueIdField> getQuickSearchResult(String entity, String field, String search, Integer vlpId, Map<String, Object> vlpParameter, Integer iMaxRowCount) {
		final MetaDataProvider<EntityMetaDataVO, EntityFieldMetaDataVO> provider = MetaDataServerProvider.getInstance();
		final EntityFieldMetaDataVO efMeta = provider.getEntityField(entity, field);
		return getQuickSearchResult(entity, efMeta, search, vlpId, vlpParameter, iMaxRowCount);
	}

	public List<CollectableValueIdField> getQuickSearchResult(String entity, EntityFieldMetaDataVO efMeta, String search, Integer vlpId, Map<String, Object> vlpParameter, Integer iMaxRowCount) {
		final List<CollectableValueIdField> result = new ArrayList<CollectableValueIdField>();
		try {
			final MetaDataProvider<EntityMetaDataVO, EntityFieldMetaDataVO> provider = MetaDataServerProvider.getInstance();
			final EntityMetaDataVO eForeignMeta = provider.getEntity(efMeta.getForeignEntity() != null ? efMeta.getForeignEntity() : efMeta.getLookupEntity());
			final TableAliasSingleton tas = TableAliasSingleton.getInstance();
			final String alias = tas.getAlias(efMeta);
			final String table = EntityObjectMetaDbHelper.getTableOrViewForSelect(eForeignMeta);
			final DbAccess access = dataBaseHelper.getDbAccess();
			final DbQueryBuilder builder = access.getQueryBuilder();
			final DbQuery<DbTuple> query = builder.createTupleQuery();
			
			final DbFrom from = query.from(table).alias(alias);
			final DbExpression<Long> id = from.baseColumn("INTID", Long.class);
			final DbExpression<String> stringifiedRef = new DbCompoundColumnExpression<String>(from, efMeta, false);
			final DbOrder order = builder.asc(stringifiedRef);
			
			final String wildcard = access.getWildcardLikeSearchChar();
			search = search.replace("*", wildcard);
			search = search.replace("?", "_");
			search = wildcard + StringUtils.toUpperCase(search) + wildcard;
			query.multiselect(id, stringifiedRef);

			final DbCondition condLike = builder.like(builder.upper(stringifiedRef), search);
			final DatasourceVO dsvo = DatasourceCache.getInstance().getValuelistProvider(vlpId);

			final EntityFieldMetaDataVO efDeleted = provider.getAllEntityFieldsByEntity(eForeignMeta.getEntity()).get(clctEOEFdeleted.getName());
			if (efDeleted != null) {
				EOSearchExpressionUnparser unparser = new EOSearchExpressionUnparser(query, eForeignMeta);
				CollectableSearchCondition condSearchDeleted = new CollectableComparison(clctEOEFdeleted, ComparisonOperator.EQUAL, new CollectableValueField(false));

				unparser.unparseSearchCondition(condSearchDeleted);
			}

			if (dsvo != null && dsvo.getValid()) {
				if (efDeleted == null)
					query.where(builder.and(condLike,builder.in(id, 
						datasourceServerUtils.getSqlWithIdForInClause(dsvo.getSource(), vlpParameter))));
				else
					query.addToWhereAsAnd(builder.and(condLike,builder.in(id, 
						datasourceServerUtils.getSqlWithIdForInClause(dsvo.getSource(), vlpParameter))));
			} else {
				if (efDeleted == null)
					query.where(condLike);
				else
					query.addToWhereAsAnd(condLike);
			}
			query.orderBy(order);

			if (iMaxRowCount != null)
				query.maxResults(iMaxRowCount);

			for (DbTuple tuple : access.executeQuery(query)) {
				result.add(new CollectableValueIdField(
					tuple.get(0, Long.class).intValue(),
					tuple.get(1, String.class)));
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}

		return result;
	}

	public String getBaseEntity(String dynamicentityname) {
		return datasourceCache.getBaseEntity(dynamicentityname);
	}

}
