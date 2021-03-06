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
package org.nuclos.server.dblayer.query;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dblayer.JoinType;

public class DbFrom implements Serializable {
	
	private final DbQuery<?> query;
	private final String tableName;
	private String tableAlias;
	private Set<DbJoin> joins;
	
	// private final MetaDataProvider mdProv = MetaDataServerProvider.getInstance();
	
	DbFrom(DbQuery<?> query, String tableName) {
		if (query == null || tableName == null) throw new NullPointerException();
		this.query = query;
		this.tableName = tableName;
		this.joins = new LinkedHashSet<DbJoin>();
	}
	
	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("table=").append(tableName);
		result.append(", alias=").append(tableAlias);
		result.append(", joins=").append(joins);
		result.append("]");
		return result.toString();
	}

	public String getTableName() {
		return tableName;
	}
	
	public String getAlias() {
		return tableAlias;
	}
	
	public DbFrom alias(String tableAlias) {
		query.registerAlias(this, tableAlias);
		this.tableAlias = tableAlias;
		return this;
	}
	
	public Set<DbJoin> getJoins() {
		return joins;
	}
	
	public DbJoin join(String tableName, JoinType joinType) {
		DbJoin join = new DbJoin(query, this, joinType, tableName);
		joins.add(join);
		return join;
	}
	
	/**
	 * An alternative to join().alias().on() for usability would be very nice...
	public DbJoin join(EntityMetaDataVO joinEntity, JoinType joinType) {
		DbJoin join = new DbJoin(query, this, joinType, joinEntity.getDbEntity());
		joins.add(join);

		final EntityFieldMetaDataVO ref = mdProv.getRefField(entity.getEntity(), joinEntity.getEntity());
		String foreignEntityField = ref.getForeignEntityField();
		// TODO: ???
		if (foreignEntityField == null) {
			foreignEntityField = "INTID";
		}
		
		return join.alias(joinEntity.getEntity()).on(foreignEntityField, ref.getField());
	}
	 */
	
	public DbJoin innerJoin(String tableName) {
		return join(tableName, JoinType.INNER);
	}
	
	public DbJoin leftJoin(String tableName) {
		return join(tableName, JoinType.LEFT);
	}
	
	/**
	 * Return a column.
	 * 
	 * @since Nuclos 3.1.01
	 */
	public <T> DbColumnExpression<T> column(String tAlias, String columnName, Class<T> javaClass) {
		// This would be too early, as the unparser could add a join... (tp)
		// if (!containsAlias(alias)) throw new IllegalArgumentException("FROM clause " + this + " does not contain alias " + alias);
		return new DbColumnExpression<T>(tAlias, this, columnName, javaClass);
	}
	
	/**
	 * Return a column from the <em>base</em> table.
	 * 
	 * @since Nuclos 3.1.01
	 */
	public <T> DbColumnExpression<T> baseColumn(String columnName, Class<T> javaClass) {
		if (!containsAlias(tableAlias)) throw new IllegalArgumentException();
		return new DbColumnExpression<T>(tableAlias, this, columnName, javaClass);
	}
	
	public <T extends Object> DbQuery<T> getQuery() {
		return (DbQuery<T>) query;
	}
	
	/**
	 * An alternative to column() for usability.
	 */
	public <T> DbColumnExpression<T> field(String tableAlias, EntityFieldMetaDataVO field) {
		if (!containsAlias(tableAlias)) throw new IllegalArgumentException();
		if (field.getPivotInfo() != null) {
			throw new IllegalArgumentException(field.toString());
		}
		try {
			return new DbColumnExpression<T>(tableAlias, this, field.getDbColumn(), (Class<T>) Class.forName(field.getDataType()));
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException(e.toString());
		}
	}
	
	@Deprecated
	/** @deprecated Only use case-sensitive columns if needed. */
	public <T> DbColumnExpression<T> columnCaseSensitive(String tableAlias, String columnName, Class<T> javaClass) {
		if (!containsAlias(tableAlias)) throw new IllegalArgumentException(
				"No such table alias in FROM clause " + this + ": " + tableAlias);
		return new DbColumnExpression<T>(tableAlias, this, columnName, javaClass, true);
	}
	
	@Deprecated
	/** @deprecated Only use case-sensitive columns if needed. */
	public <T> DbColumnExpression<T> baseColumnCaseSensitive(String columnName, Class<T> javaClass, boolean isDynmicView) {
		if (!containsAlias(tableAlias)) throw new IllegalArgumentException(
				"No such table alias in FROM clause " + this + ": " + tableAlias);
		if(isDynmicView && "INTID_T_UD_GENERICOBJECT".equalsIgnoreCase(columnName))
			return new DbColumnExpression<T>(tableAlias, this, columnName, javaClass, false);
		return new DbColumnExpression<T>(tableAlias, this, columnName, javaClass, true);
	}
	
	public boolean containsAlias(String tAlias) {
		if (tAlias == null) throw new NullPointerException();
		if (tAlias.equals(tableAlias)) return true;
		for (DbJoin j: joins) {
			if (tAlias.equals(j.getAlias())) return true;
		}
		return false;
	}
	
}
