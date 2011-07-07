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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.collect.collectable.searchcondition.PlainSubCondition;
import org.nuclos.common.database.query.SelectQuery;
import org.nuclos.common.database.query.definition.Column;
import org.nuclos.common.database.query.definition.Schema;
import org.nuclos.common.database.query.definition.Table;
import org.nuclos.common.database.query.statement.AndCondition;
import org.nuclos.common.database.query.statement.ComparisonOperator;
import org.nuclos.common.database.query.statement.Condition;
import org.nuclos.common.database.query.statement.InnerJoinCondition;
import org.nuclos.common.database.query.statement.JoinCondition;
import org.nuclos.common.database.query.statement.Operand;
import org.nuclos.common.database.query.statement.OuterJoinCondition;
import org.nuclos.common.querybuilder.DatasourceUtils;
import org.nuclos.common.querybuilder.DatasourceXMLParser;
import org.nuclos.common.querybuilder.NuclosDatasourceException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.report.SchemaCache;
import org.nuclos.server.report.WhereConditionParser;
import org.nuclos.server.report.valueobject.DatasourceParameterVO;
import org.nuclos.server.report.valueobject.DatasourceVO;

public class DatasourceServerUtils {
	private static Logger	log	= Logger.getLogger(DatasourceServerUtils.class);

	public static final SQLCache SQLCACHE = new SQLCache();

	/**
	 * get sql string for datasource definition
	 *
	 * @param iDatasourceId id of datasource
	 * @return string containing sql
	 */
	public static String createSQL(Integer iDatasourceId,
	    Map<String, Object> mpParams) throws NuclosDatasourceException {
		try {
			// final DatasourceLocal datasource =
			// datasourceHome.findByPrimaryKey(iDatasourceId);
			/** @todo consider encoding (UTF-8) */
			// return createSQL(new String(datasource.getObjDatasourceXML()),
			// mpParams);
			DatasourceVO vo = DatasourceCache.getInstance().getDatasourcesById(
			    iDatasourceId, SessionUtils.getCurrentUserName());
			return createSQL(vo.getSource(), mpParams);
		}
		catch(CommonPermissionException e) {
			throw new NuclosDatasourceException(e.getMessage(), e);
		}
	}

	/**
	 * get sql string for report execution.
	 * There is no authorization as the authorization is applied to the report.
	 *
	 * @param iDatasourceId id of datasource
	 * @return string containing sql
	 */
	public static String createSQLForReportExecution(String name, Map<String, Object> mpParams) throws NuclosDatasourceException {
		DatasourceVO vo = DatasourceCache.getInstance().getDatasourceByName(name);
		return createSQL(vo.getSource(), mpParams);
	}

	/**
	 * get sql string for datasource definition without parameter definition
	 *
	 * @param sDatasourceXML xml of datasource
	 * @return string containing sql
	 */
	public static String createSQL(String sDatasourceXML)
	    throws NuclosDatasourceException {
		return createSQL(sDatasourceXML, Collections.<String, Object>emptyMap());
	}

	/**
	 *
	 * @param sDatasourceXML
	 * @param mpParams
	 * @return new PlainSubCondition with '(SELECT intid FROM ( ... ) ds )' condition
	 * @throws NuclosDatasourceException
	 */
	public static PlainSubCondition getConditionWithIdForInClause(String sDatasourceXML, Map<String, Object> mpParams) throws NuclosDatasourceException {
		return new PlainSubCondition(getSqlWithIdForInClause(sDatasourceXML, mpParams));
	}
	
	/**
	 * 
	 * @param sDatasourceXML
	 * @param mpParams
	 * @return
	 * @throws NuclosDatasourceException
	 */
	public static String getSqlWithIdForInClause(String sDatasourceXML, Map<String, Object> mpParams) throws NuclosDatasourceException {
		return "(SELECT intid FROM (" + createSQL(sDatasourceXML, mpParams) + ") ds )";
	}

	/**
	 * get sql string for datasource definition
	 *
	 * @param sDatasourceXML xml of datasource
	 * @return string containing sql
	 */
	public static String createSQL(String sDatasourceXML, Map<String, Object> mpParams) throws NuclosDatasourceException {
		String result = null;
		result = SQLCACHE.getSQL(sDatasourceXML);
		result = replaceParameters(result, mpParams==null?(new HashMap<String, Object>()):mpParams);
		log.debug(result);

		return result;
	}

	/**
	 *
	 * @param sDatasourceXML
	 * @return sql with original parameter placeholders
	 * @throws NuclosDatasourceException
	 */
	public static String createSQLOriginalParameter(String sDatasourceXML) throws NuclosDatasourceException {
		return SQLCACHE.getSQL(sDatasourceXML);
	}

	/**
	 * get sql string for datasource definition
	 *
	 * @param sDatasourceXML xml of datasource
	 * @return string containing sql
	 */
	private static String createSQLForCaching(String sDatasourceXML) throws NuclosDatasourceException {
		String result = null;
		final SelectQuery query = new SelectQuery(
		    SessionUtils.getCurrentUserName());

		final DatasourceXMLParser.Result parseresult = parseDatasourceXML(sDatasourceXML);

		if(parseresult.isModelUsed()) {
			// final Schema schema =
			// SchemaCache.getSchema(ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_NUCLOS_SCHEMA));
			final Schema schema = SchemaCache.getInstance().getCurrentSchema();

			// create from clause
			final Set<Table> setFromTables = new HashSet<Table>();
			for(DatasourceXMLParser.XMLConnector xmlconnector : parseresult.getLstConnectors()) {
				setFromTables.add(createTable(
				    schema,
				    parseresult.getMapXMLTables().get(
				        xmlconnector.getSrcTable())));
				setFromTables.add(createTable(
				    schema,
				    parseresult.getMapXMLTables().get(
				        xmlconnector.getDstTable())));
			}
			for(DatasourceXMLParser.XMLColumn xmlcolumn : parseresult.getLstColumns()) {
				setFromTables.add(createTable(schema,
				    parseresult.getMapXMLTables().get(xmlcolumn.getTable())));
			}
			for(Table table : setFromTables) {
				parseresult.getMapTables().put(table.getAlias(), table);
				query.addToFromClause(table);
			}

			// create where clause (for join)
			boolean bFirst = true;
			Vector<Table> joins = null;

			for(DatasourceXMLParser.XMLConnector xmlconnector : parseresult.getLstConnectors()) {
				final Table srcTable = (Table) (parseresult.getMapTables().get(xmlconnector.getSrcTable())).clone();
				final Column srcColumn = new Column(
				    srcTable.getColumn(xmlconnector.getSrcColumn()));
				final Table dstTable = (Table) (parseresult.getMapTables().get(xmlconnector.getDstTable())).clone();
				final Column dstColumn = new Column(
				    dstTable.getColumn(xmlconnector.getDstColumn()));
				final Operand left = new Operand(srcColumn);
				final Operand right = new Operand(dstColumn);
				final String sJoinType = xmlconnector.getJoinType() == null
				    ? "InnerJoin"
				    : xmlconnector.getJoinType();

				final ComparisonOperator op;

				if(sJoinType.equals("LeftOuterJoin")) {
					op = ComparisonOperator.LEFT_OUTER_JOIN_OPERATOR;
				}
				else if(sJoinType.equals("RightOuterJoin")) {
					op = ComparisonOperator.RIGHT_OUTER_JOIN_OPERATOR;
				}
				else {
					op = ComparisonOperator.EQUALS_OPERATOR;
				}

				// TODO: refactor datasource sql generation

				// At the moment, the MSSQL-/Oracle-specific behaviour is
				// changed as follows:
				// As before, inner joins are written as cartesian product with
				// a where
				// condition.
				// Outer joins now always uses the JOIN syntax (like MSSQL
				// before) because
				// it is standard SQL (and also supported by Oracle since 9i).
				// However, to improve portability, the joins are rewritten
				// using JDBC's
				// escape syntax (i.e. enclosed by the {oj...} delimiters).
				// NOTE that the behavior how the JOIN tables/conditions are
				// generated
				// is unchanged!
				/**
				 * normal WHERE condition. the only case where the statement is
				 * used as cartesian product
				 */
				if(op.equals(ComparisonOperator.EQUALS_OPERATOR)) {
					JoinCondition condition = new InnerJoinCondition(left, op,
					    right);
					if(!bFirst) {
						query.addToWhereClause(new AndCondition());
					}
					else {
						bFirst = false;
					}
					query.addToWhereClause((Condition) condition);
				}
				else {
					/**
					 * this is the LEFT JOIN or RIGHT JOIN case here the
					 * statement is put in a fake table for getting it to the
					 * FROM block of the statement.
					 */
					OuterJoinCondition condition = new OuterJoinCondition(left,
					    op, right);

					int tableAliasNumber = 0;

					if(joins == null) {
						joins = new Vector<Table>();
					}
					String fullTableName = "";

					Table leftTable = ((Column) left.getObject()).getTable();
					Table rightTable = ((Column) right.getObject()).getTable();

					String leftTableName = leftTable.getName();
					String rightTableName = rightTable.getName();

					String fullTableNameLeft = ((Column) left.getObject()).getTable().getAlias()
					    + "." + leftTableName;
					String fullTableNameRight = ((Column) right.getObject()).getTable().getAlias()
					    + "." + rightTableName;
					/**
					 * is the table already used by an join? create a unique
					 * alias for that table, the query is working on the first
					 * alias of that table
					 */
					String tempTableName = "";

					for(Table table : joins) {
						/**
						 * full table names needed because there might be the
						 * reason for multiple instances of a query object
						 */
						tempTableName = table.getName();

						fullTableName = table.getAlias() + "." + tempTableName;

						if(fullTableName.equals(fullTableNameLeft))
							((Column) left.getObject()).getTable().setAlias(
							    getNextNumberForTableAlias(
							        ((Column) left.getObject()).getTable().getAlias(),
							        tableAliasNumber++));

						if(fullTableName.equals(fullTableNameRight))
							((Column) right.getObject()).getTable().setAlias(
							    getNextNumberForTableAlias(
							        ((Column) right.getObject()).getTable().getAlias(),
							        tableAliasNumber++));

					}

					/**
					 * remembering the tables that were used for joining needed
					 * for deleting the tables from the statement afterwards.
					 */
					joins.add(((Column) left.getObject()).getTable());
					joins.add(((Column) right.getObject()).getTable());

					/**
					 * creating a fake table that has the join statment a table
					 * name. otherwise it is not possible to get the join
					 * statement to the FROM block in the statment.
					 */
					query.addToFromClause(condition.getOuterJoinTable());
				}
			}

			// create select, where, group by, order by claues
			final boolean bGroupBy = checkGroupBy(parseresult.getLstColumns());
			final List<String> lstColumnConditions = new ArrayList<String>();

			for(DatasourceXMLParser.XMLColumn xmlcolumn : parseresult.getLstColumns()) {
				final Column column = createColumn(parseresult.getMapTables(),
				    xmlcolumn);
				if(xmlcolumn.isVisible()) {
					query.addToSelectClause(
					    createColumn(parseresult.getMapTables(), xmlcolumn),
					    xmlcolumn.getAlias());
				}
				if(bGroupBy) {
					if(xmlcolumn.getGroup() == null
					    || xmlcolumn.getGroup().length() == 0) {
						xmlcolumn.setGroup(DatasourceVO.GroupBy.MAX.getLabel());
					}
					else if(xmlcolumn.getGroup().equals(
					    DatasourceVO.GroupBy.GROUP.getLabel())) {
						query.addToGroupByClause(
						    createColumn(parseresult.getMapTables(), xmlcolumn),
						    xmlcolumn.getGroup());
					}
					else {
						query.addToGroupByMap(
						    createColumn(parseresult.getMapTables(), xmlcolumn),
						    xmlcolumn.getGroup());
					}
				}
				lstColumnConditions.clear();
				final WhereConditionParser whereConditionParser = new WhereConditionParser();
				for(DatasourceXMLParser.XMLCondition xmlcondition : xmlcolumn.getLstConditions()) {
					if(xmlcondition.getCondition() != null
					    && xmlcondition.getCondition().length() > 0) {
						final String sCondition = whereConditionParser.parseCondition(
						    column, xmlcondition.getCondition());
						lstColumnConditions.add(sCondition);
					}
				}

				if(lstColumnConditions.size() > 0) {
					query.addColumnWhereClauses(lstColumnConditions);
				}
				if(xmlcolumn.getSort() != null
				    && xmlcolumn.getSort().length() > 0) {
					query.addToOrderByClause(
					    column,
					    xmlcolumn.getSort().equals(
					        DatasourceVO.OrderBy.ASCENDING.getLabel()));
				}
			}
			/**
			 * deleting duplicate tables from the query object. they are in
			 * mssql for joins if the vector joins is not null, otherwise it is
			 * not used
			 */
			if(joins != null) {
				for(Table table : joins) {
					query.deleteItemFromLstFrom(table.getName());
				}
				joins = null;
			}

			result = query.getSelectStatement(parseresult.isEntityOptionDynamic());
		}
		else {
			result = parseresult.getQueryStringFromXml();
		}

		log.debug(result);
		return result;
	}

	/**
	 * parses the datasource XML
	 *
	 * @param sDatasourceXML
	 * @throws NuclosFatalException
	 * @throws NuclosDatasourceException
	 */
	private static DatasourceXMLParser.Result parseDatasourceXML(
	    String sDatasourceXML) throws NuclosFatalException,
	    NuclosDatasourceException {
		return DatasourceXMLParser.parse(sDatasourceXML);
	}

	/**
	 * return a copy of a Table in Schema because more than one instance is
	 * possible in the Datasource
	 *
	 * @param schema
	 * @param xmltable
	 * @return a copy of the specified table from the schema.
	 */
	private static Table createTable(Schema schema,
	    DatasourceXMLParser.XMLTable xmltable) {
		final Table table = schema.getTable(xmltable.getEntity());
		if(table != null) {
			final Table resultTable = (Table) table.clone();
			resultTable.setAlias(xmltable.getId());
			SchemaCache.getColumns(resultTable);
			return resultTable;
		}
		return new Table(schema, "");
	}

	private static Column createColumn(Map<String, Table> mpTables,
	    DatasourceXMLParser.XMLColumn column) {
		final Table table = mpTables.get(column.getTable());
		final Column result = new Column(table.getColumn(column.getColumn()));
		result.setTable(mpTables.get(column.getTable()));
		result.setAlias(column.getAlias());

		return result;
	}

	/**
	 * small helper method for creating a unique table alias for duplicate
	 * tables in mssql joins
	 *
	 * @return nextNumber _<number> T2 T2_1
	 */
	private static String getNextNumberForTableAlias(String alias,
	    int tableAliasNumber) {
		String cut = alias;
		if(cut.indexOf("_") != -1) {
			cut = alias.substring(0, (alias.indexOf("_") + 1));
		}
		else {
			cut = alias + "_";
		}
		return cut + tableAliasNumber;
	}

	private static boolean checkGroupBy(
	    List<DatasourceXMLParser.XMLColumn> lstColumns) {
		for(DatasourceXMLParser.XMLColumn xmlcolumn : lstColumns) {
			if(xmlcolumn.getGroup() != null
			    || (xmlcolumn.getGroup() != null && xmlcolumn.getGroup().length() > 0)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * replace all parameters in sSql with the values from mpParams
	 *
	 * @param sSql
	 * @param mpParams
	 * @return sql string with parameters replaced.
	 */
	private static String replaceParameters(String sSql, Map<String, ?> mpParams) {
		String result = sSql;
		for(String sParameter : DatasourceUtils.getParametersFromString(sSql)) {
			result = replaceAll(result, "$" + sParameter,
			    replaceParam("$" + sParameter, mpParams));
		}

		return result;
	}

	/**
	 * replace get the value for sParameter from mapParams
	 *
	 * @param sParameter
	 * @param mapParams
	 * @return the value for sParameter from the map, or sParameter if no value
	 *         is not found in the map
	 */
	private static String replaceParam(String sParameter,
	    Map<String, ?> mapParams) {
		String sKey = sParameter.replace('$', ' ').trim();
		if(!mapParams.containsKey(sKey)) {
			return sParameter;
		}

		Object param = mapParams.get(sKey);
		if(param instanceof Date) {
			// ISO-format 'yyyy-mm-dd', matches JDBC escape syntax
			param = String.format("%tF", param);
		}
		if(param != null) {
			return param.toString();
		}
		else {
			return "null";
		}
	}

	private static String replaceAll(String s, String sSearchExpr,
	    String sReplacement) {
		final StringBuffer sb = new StringBuffer(s);
		int iFromIndex = 0;
		int iCurrentIndex = 0;
		while((iCurrentIndex = sb.indexOf(sSearchExpr, iFromIndex)) != -1) {
			sb.replace(iCurrentIndex, iCurrentIndex + sSearchExpr.length(),
			    sReplacement);
			iFromIndex = iCurrentIndex + sReplacement.length();
		}
		return sb.toString();
	}

	/**
	    * Retrieve the parameters a datasource accepts.
	    * @param sDatasourceXML
	    * @return
	    * @throws NuclosFatalException
	    * @throws NuclosDatasourceException
	    */
	   public static List<DatasourceParameterVO> getParameters(String sDatasourceXML) throws NuclosFatalException, NuclosDatasourceException {
			// @todo we need a getParameters(Integer iDatasourceId) method for the
			// remote interface to prevent that every user needs the right to
			// execute the get method
			final List<DatasourceParameterVO> result = new ArrayList<DatasourceParameterVO>();
			final DatasourceXMLParser.Result parseresult = parseDatasourceXML(sDatasourceXML);
			result.addAll(parseresult.getLstParameters());
			return result;
	   }

	   /**
	    * Retrieve the parameters a datasource accepts.
	    * @param iDatasourceId
	    * @return
	    * @throws NuclosFatalException
	    * @throws NuclosDatasourceException
	    */
	   public static List<DatasourceParameterVO> getParameters(Integer iDatasourceId) throws NuclosFatalException, NuclosDatasourceException {

	 		final List<DatasourceParameterVO> result = new ArrayList<DatasourceParameterVO>();
	 		DatasourceVO dsvo = DatasourceCache.getInstance().get(iDatasourceId);
	 		final DatasourceXMLParser.Result parseresult = parseDatasourceXML(dsvo.getSource());
	 		result.addAll(parseresult.getLstParameters());
	 		return result;
	   }


	   /**
	    *
	    * SQL Cache
	    */
	   public static class SQLCache {

		   private Map<String, String> mpDatasourcesSQLByXML = null;

		   private synchronized Map<String, String> getMap() {
			   if (mpDatasourcesSQLByXML == null) {
				   mpDatasourcesSQLByXML = Collections.synchronizedMap(new HashMap<String, String>());
			   }
			   return mpDatasourcesSQLByXML;
		   }

		   private String getSQL(String sDatasourceXML) throws NuclosDatasourceException {
			   if (!getMap().containsKey(sDatasourceXML)) {
				   getMap().put(sDatasourceXML, createSQLForCaching(sDatasourceXML));
			   }

			   return getMap().get(sDatasourceXML);
		   }

		   public synchronized void invalidate() {
			   mpDatasourcesSQLByXML = null;
		   }
	   }

}