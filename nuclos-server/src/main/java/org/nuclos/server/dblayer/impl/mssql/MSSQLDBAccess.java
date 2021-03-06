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
package org.nuclos.server.dblayer.impl.mssql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nuclos.common.collection.Pair;
import org.nuclos.common2.StringUtils;
import org.nuclos.server.dblayer.DbException;
import org.nuclos.server.dblayer.DbStatementUtils;
import org.nuclos.server.dblayer.IBatch;
import org.nuclos.server.dblayer.impl.BatchImpl;
import org.nuclos.server.dblayer.impl.SqlSequentialUnit;
import org.nuclos.server.dblayer.impl.standard.MetaDataSchemaExtractor;
import org.nuclos.server.dblayer.impl.standard.TransactSqlDbAccess;
import org.nuclos.server.dblayer.impl.util.PreparedString;
import org.nuclos.server.dblayer.statements.AbstractDbStatementVisitor;
import org.nuclos.server.dblayer.statements.DbUpdateStatement;
import org.nuclos.server.dblayer.structure.DbColumn;
import org.nuclos.server.dblayer.structure.DbColumnType;
import org.nuclos.server.dblayer.structure.DbColumnType.DbGenericType;
import org.nuclos.server.dblayer.structure.DbNullable;
import org.nuclos.server.dblayer.structure.DbSequence;
import org.nuclos.server.dblayer.structure.DbSimpleView;

public class MSSQLDBAccess extends TransactSqlDbAccess {

	static final String EXPROP_NUCLOS_SEQUENCE = "Nuclos Sequence";
	static final String EXPROP_NUCLOS_SEQUENCE_TABLE = "Nuclos Sequence Table";

	@Override
	protected String getDataType(DbColumnType columnType) throws DbException {
		if (columnType.getTypeName() != null) {
			return columnType.getTypeName() + columnType.getParametersString();
		} else {
			DbGenericType genericType = columnType.getGenericType();
			switch (genericType) {
			case VARCHAR:
				return String.format("VARCHAR(%d)", columnType.getLength());
			case NUMERIC:
				return String.format("NUMERIC(%d,%d)", columnType.getPrecision(), columnType.getScale());
			case BOOLEAN:
				return "NUMERIC(1,0)";
			case BLOB:
				return "VARBINARY(MAX)";
			case CLOB:
				return "VARCHAR(MAX)";
			case DATE:
			case DATETIME:
				return "DATETIME";
			default:
				throw new DbException("Unsupported column type " + genericType);
			}
		}
	}

	@Override
	protected MetaDataSchemaExtractor getMetaDataExtractor() {
		return new MSSQLMetaDataExtractor();
	}

	@Override
	protected IBatch getSqlForAlterTableColumn(DbColumn column1, DbColumn column2) throws SQLException {
		final PreparedString ps = PreparedString.format("ALTER TABLE %s ALTER COLUMN %s",
			getQualifiedName(column2.getTableName()),
			getColumnSpecForAlterTableColumn(column2, column1));
		final IBatch result;
		if(column2.getDefaultValue() != null && column2.getNullable().equals(DbNullable.NOT_NULL)) {
			result = getSqlForUpdateNotNullColumn(column2);
			result.append(new SqlSequentialUnit(ps));
		}
		else {
			result = BatchImpl.simpleBatch(ps);
		}
		return result;
	}
	
	

	@Override
	protected IBatch getSqlForAlterTableNotNullColumn(DbColumn column) {
		String columnSpec = String.format("%s %s NOT NULL", column.getColumnName(), getDataType(column.getColumnType()));
		
		return BatchImpl.simpleBatch(PreparedString.format("ALTER TABLE %s ALTER COLUMN %s",
			getQualifiedName(column.getTableName()), columnSpec));	
	}

	/**
	 * Always return nulls specification as MS SQL defaults to null
	 */
	@Override
    protected String getColumnSpecForAlterTableColumn(DbColumn column, DbColumn oldColumn) {
	    return super.getColumnSpec(column, true);
    }

	@Override
	protected String getFunctionNameForUseInView(String name) {
		return getQualifiedName(name);
	}

	@Override
	protected IBatch getSqlForCreateSequence(DbSequence sequence) {
		List<PreparedString> sql = new ArrayList<PreparedString>();
		// p.x = procedure name / p.y = table name
		Pair<String, String> p = getObjectNamesForSequence(sequence);
		// Create table
		sql.add(PreparedString.format(StringUtils.join("\n",
			"CREATE TABLE %s (",
			"  SEQID INT IDENTITY(%d,1) PRIMARY KEY," +
			"  SEQVAL VARCHAR(1)",
		")"),
		getQualifiedName(p.y),
		sequence.getStartWith()));
		// Create procedure
		sql.add(PreparedString.format(StringUtils.join("\n",
			"CREATE PROCEDURE %1$s AS",
			"BEGIN",
			"  DECLARE @NewSeqValue INT",
			"  SET NOCOUNT ON",
			"  INSERT INTO %2$s (SEQVAL) VALUES ('a')",
			"",
			"  SET @NewSeqValue = SCOPE_IDENTITY()",
			"",
			"  DELETE FROM %2$s WITH (READPAST)",
			"  SELECT @NewSeqValue",
		"END"),
		getQualifiedName(p.x),
		getQualifiedName(p.y)));
		// Add extended property 'Nuclos Sequence' to procedure
		sql.add(PreparedString.format(StringUtils.join("\n",
			"EXEC sys.sp_addextendedproperty ",
			"@name = N'%s',",
			"@value = N'%s',",
			"@level0type = N'SCHEMA', @level0name = %s,",
		"@level1type = N'PROCEDURE',  @level1name = %s"),
		EXPROP_NUCLOS_SEQUENCE,
		sequence.getSequenceName(),
		getSchemaName(),
		p.x));
		// Add extended property 'Nuclos Sequence Table' to procedure
		sql.add(PreparedString.format(StringUtils.join("\n",
			"EXEC sys.sp_addextendedproperty ",
			"@name = N'%s',",
			"@value = N'%s',",
			"@level0type = N'SCHEMA', @level0name = %s,",
		"@level1type = N'PROCEDURE',  @level1name = %s"),
		EXPROP_NUCLOS_SEQUENCE_TABLE,
		p.y,
		getSchemaName(),
		p.x));
		// Add extended property 'Nuclos Sequence' to table
		sql.add(PreparedString.format(StringUtils.join("\n",
			"EXEC sys.sp_addextendedproperty ",
			"@name = N'%s',",
			"@value = N'%s',",
			"@level0type = N'SCHEMA', @level0name = %s,",
		"@level1type = N'TABLE',  @level1name = %s"),
		EXPROP_NUCLOS_SEQUENCE,
		sequence.getSequenceName(),
		getSchemaName(),
		p.y));
		return BatchImpl.simpleBatch(sql);
	}

	@Override
	protected IBatch getSqlForDropSequence(DbSequence sequence) {
		List<PreparedString> sql = new ArrayList<PreparedString>();
		// p.x = procedure name / p.y = table name
		Pair<String, String> p = getObjectNamesForSequence(sequence);
		sql.add(PreparedString.concat("DROP PROCEDURE ", getQualifiedName(p.x)));
		sql.add(PreparedString.concat("DROP TABLE ", getQualifiedName(p.y)));
		return BatchImpl.simpleBatch(sql);
	}
	
	@Override
	protected IBatch getSqlForAlterSimpleView(DbSimpleView oldView, DbSimpleView newView) {
		if (!oldView.getViewName().equals(newView.getViewName())) {
			throw new IllegalArgumentException();
		}
		return BatchImpl.simpleBatch(_getSqlForCreateSimpleView("ALTER VIEW", newView, ""));
	}
	
	@Override
	protected IBatch getSqlForUpdateNotNullColumn(final DbColumn column) throws SQLException {
		final DbUpdateStatement stmt = DbStatementUtils.getDbUpdateStatementWhereFieldIsNull(
				getQualifiedName(column.getTableName()), column.getColumnName(), column.getDefaultValue());

		final PreparedString sPlainUpdate = stmt.build().accept(new AbstractDbStatementVisitor<PreparedString>() {
			@Override
			public PreparedString visitUpdate(DbUpdateStatement update) {				
				String sUpdate = getPreparedStringForUpdate(stmt).toString();
				for(Object obj : update.getColumnValues().values()) {
					if(column.getColumnType().getGenericType().equals(DbGenericType.BOOLEAN)){						
						Boolean bTrue = new Boolean((String)obj);
						sUpdate = org.apache.commons.lang.StringUtils.replace(sUpdate, "?", bTrue ? "1" : "0");
					}
					else {
						sUpdate = org.apache.commons.lang.StringUtils.replace(sUpdate, "?", "'"+obj.toString()+"'");
					}
				}
				return new PreparedString(sUpdate);
			}
		});
		return BatchImpl.simpleBatch(sPlainUpdate);
	}


	class MSSQLMetaDataExtractor extends TransactSqlMetaDataExtractor {

		@Override
		protected Collection<DbSequence> getSequences() throws SQLException {
			List<DbSequence> sequences = new ArrayList<DbSequence>();
			Collection<String> sequenceProcedures = getObjectsWithExtendedProperty(
				getSchemaName(), "PROCEDURE", EXPROP_NUCLOS_SEQUENCE).keySet();
			for (String procedureName : sequenceProcedures) {
				sequences.add(getSequence(procedureName));
			}
			return sequences;
		}

		protected DbSequence getSequence(String objectName) throws SQLException {
			Map<String, String> props = getObjectProperties(getSchemaName(), "PROCEDURE", objectName);
			String sequenceName = props.get(EXPROP_NUCLOS_SEQUENCE);
			long startWith = 0L;
			try {
				startWith = executor.getNextId(sequenceName);
			} catch (DbException e) {
				log.warn("Could not determine next id for sequence " + sequenceName, e);
			}
			DbSequence sequence = new DbSequence(sequenceName, startWith);
			sequence.setHint(HINT_SEQUENCE_TABLE, props.get(EXPROP_NUCLOS_SEQUENCE_TABLE));
			return sequence;
		}

		protected Map<String, String> getExtendedProperties(String propertyName, String schemaName, String level1Type, String level1Name) throws SQLException {
			Map<String, String> properties = new HashMap<String, String>();
			PreparedStatement stmt = connection.prepareStatement(
				"SELECT objtype, objname, name, CAST(value AS VARCHAR(200)) value FROM fn_listextendedproperty(?, 'SCHEMA', ?, ?, ?, NULL, NULL)");
			try {
				stmt.setString(1, propertyName);
				stmt.setString(2, schemaName);
				stmt.setString(3, level1Type);
				stmt.setString(4, level1Name);
				ResultSet rs = stmt.executeQuery();
				try {
					while (rs.next()) {
						properties.put(rs.getString("objname"), rs.getString("value"));
					}
				} finally {
					rs.close();
				}
			} finally {
				stmt.close();
			}
			return properties;
		}

		/**
		 * Returns a map object name -> property value.
		 */
		protected Map<String, String> getObjectsWithExtendedProperty(String schemaName, String objectType, String propertyName) throws SQLException {
			Map<String, String> properties = new HashMap<String, String>();
			PreparedStatement stmt = connection.prepareStatement(
				"SELECT objname, CAST(value AS VARCHAR(200)) value FROM fn_listextendedproperty(?, 'SCHEMA', ?, ?, NULL, NULL, NULL)");
			try {
				stmt.setString(1, propertyName);
				stmt.setString(2, schemaName);
				stmt.setString(3, objectType);
				ResultSet rs = stmt.executeQuery();
				try {
					while (rs.next()) {
						properties.put(rs.getString("objname"), rs.getString("value"));
					}
				} finally {
					rs.close();
				}
			} finally {
				stmt.close();
			}
			return properties;
		}

		/**
		 * Returns a map property name -> property value.
		 */
		protected Map<String, String> getObjectProperties(String schemaName, String objectType, String objectName) throws SQLException {
			Map<String, String> properties = new HashMap<String, String>();
			PreparedStatement stmt = connection.prepareStatement(
				"SELECT name, CAST(value AS VARCHAR(200)) value FROM fn_listextendedproperty(NULL, 'SCHEMA', ?, ?, ?, NULL, NULL)");
			try {
				stmt.setString(1, schemaName);
				stmt.setString(2, objectType);
				stmt.setString(3, objectName);
				ResultSet rs = stmt.executeQuery();
				try {
					while (rs.next()) {
						properties.put(rs.getString("name"), rs.getString("value"));
					}
				} finally {
					rs.close();
				}
			} finally {
				stmt.close();
			}
			return properties;
		}

		@Override
		protected String normalizeCallableName(String name) {
			// MSSQL's JDBC metadata API suffixes the regular procedure/function name (xxx_NAME)
			// with their arity (for what the SPECIFIC_NAME is actually intended)...
			int semi = name.indexOf(';');
			return (semi >= 0) ? name.substring(0, semi) : name;
		}
	}

}
