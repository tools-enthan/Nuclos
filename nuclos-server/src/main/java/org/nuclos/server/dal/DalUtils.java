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
package org.nuclos.server.dal;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.nuclos.common.CryptUtil;
import org.nuclos.common.NuclosDateTime;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.NuclosImage;
import org.nuclos.common.NuclosPassword;
import org.nuclos.common.NuclosScript;
import org.nuclos.common.dal.vo.AbstractDalVOWithVersion;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.DateTime;
import org.nuclos.common2.IdUtils;
import org.nuclos.common2.InternalTimestamp;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.server.dal.specification.IDalVersionSpecification;
import org.nuclos.server.database.SpringDataBaseHelper;
import org.nuclos.server.dblayer.structure.DbColumn;
import org.nuclos.server.dblayer.structure.DbColumnType;
import org.nuclos.server.dblayer.structure.DbColumnType.DbGenericType;
import org.nuclos.server.genericobject.valueobject.GenericObjectDocumentFile;
import org.nuclos.server.report.ByteArrayCarrier;
import org.nuclos.server.resource.valueobject.ResourceFile;

public class DalUtils {

	public static List<Integer> convertLongIdList(List<Long> listIds) {
		if (listIds == null) {
			return null;
		}
		List<Integer> result = new ArrayList<Integer>(listIds.size());
		for (Long id : listIds) {
			result.add(IdUtils.unsafeToId(id));
		}
		return result;
	}

	public static List<Long> convertIntegerIdList(List<Integer> listIds) {
		if (listIds == null) {
			return null;
		}
		List<Long> result = new ArrayList<Long>(listIds.size());
		for (Integer id : listIds) {
			result.add(IdUtils.toLongId(id));
		}
		return result;
	}
	public static void addNucletEOSystemFields(List<EntityFieldMetaDataVO> entityFields, EntityMetaDataVO eMeta) {
		addNucletEOSystemFields(entityFields, eMeta, false);
	}
	public static void addNucletEOSystemFields(List<EntityFieldMetaDataVO> entityFields, EntityMetaDataVO eMeta, boolean addWithoutCheck) {
		entityFields.add(NuclosEOField.CHANGEDAT.getMetaData());
		entityFields.add(NuclosEOField.CHANGEDBY.getMetaData());
		entityFields.add(NuclosEOField.CREATEDAT.getMetaData());
		entityFields.add(NuclosEOField.CREATEDBY.getMetaData());
		if (eMeta.isStateModel()) {
			entityFields.add(NuclosEOField.SYSTEMIDENTIFIER.getMetaData());
			entityFields.add(NuclosEOField.PROCESS.getMetaData());
			entityFields.add(NuclosEOField.ORIGIN.getMetaData());
			entityFields.add(NuclosEOField.LOGGICALDELETED.getMetaData());
			entityFields.add(NuclosEOField.STATE.getMetaData());
			entityFields.add(NuclosEOField.STATENUMBER.getMetaData());
			entityFields.add(NuclosEOField.STATEICON.getMetaData());
		}
	}

	public static boolean isNucletEOSystemField(EntityFieldMetaDataVO voField) {
		for(NuclosEOField field : NuclosEOField.values()) {
			if(field.getMetaData().getField().equals(voField.getField()))
				return true;
		}
		return false;
	}

	public static Long getNextId() {
		return IdUtils.toLongId(SpringDataBaseHelper.getInstance().getNextIdAsInteger(SpringDataBaseHelper.DEFAULT_SEQUENCE));
	}

	public static void handleVersionUpdate(IDalVersionSpecification processor,
		EntityObjectVO vo, String user) throws CommonStaleVersionException {

		if (vo.getId() != null) {
			final Integer oldVersion = processor.getVersion(vo.getId());
			if (!vo.getVersion().equals(oldVersion)) {
				throw new CommonStaleVersionException("entity object", vo.toString(), Integer.toString(oldVersion));
			}
		}

		updateVersionInformation(vo, user);
		vo.flagUpdate();
	}

	public static <T> void updateVersionInformation(AbstractDalVOWithVersion vo, String user) {
		Date sysdate = new Date();
		if (vo.getCreatedBy() == null) {
			vo.setCreatedBy(user);
		}
		if (vo.getCreatedAt() == null) {
			vo.setCreatedAt(InternalTimestamp.toInternalTimestamp(sysdate));
		}
		if (vo.getVersion() == null) {
			vo.setVersion(1);
		} else {
			vo.setVersion(vo.getVersion()+1);
		}

		vo.setChangedBy(user);
		vo.setChangedAt(InternalTimestamp.toInternalTimestamp(sysdate));
	}

	public static <T> boolean isNuclosProcessor(AbstractDalVOWithVersion dalVO) {
		return dalVO.processor() != null && isNuclosProcessor(dalVO.processor());
	}

	public static boolean isNuclosProcessor(String processor) {
		try {
			Class<?> processorClzz = Class.forName(processor);
			for (Class<?> processorInterface : processorClzz.getInterfaces()){
				if ("org.nuclos.server.dal.processor.nuclos".equals(processorInterface.getPackage().getName())){
					return true;
				}
			}
			return false;
		}
		catch(ClassNotFoundException e) {
			throw new CommonFatalException(e);
		}
	}

	public static String getDbIdFieldName(String fieldName) {
		final String uFieldName = fieldName.toUpperCase();
		// return uFieldName.replaceFirst("^\\p{Upper}VALUE_", "INTID_");
		final int index_ = uFieldName.indexOf("_");
		if (index_ < 0) {
			return "INTID_"+uFieldName;
		}
		return "INTID"+uFieldName.substring(index_);
	}

	public static boolean isDbIdField(String fieldName) {
		return fieldName.startsWith("INTID_");
	}

	public static Class<?> getDbType(Class<?> javaType) {
		if (javaType == ByteArrayCarrier.class || javaType == Object.class || javaType == NuclosImage.class) {
			javaType = byte[].class; // Column stores data (blob)
		} else if (javaType == ResourceFile.class || javaType == GenericObjectDocumentFile.class) {
			return String.class; // Column stores filename
		} else if (javaType == DateTime.class) {
			return InternalTimestamp.class;
		} else if (javaType == NuclosPassword.class) {
			return NuclosPassword.class;
		}
		return javaType;
	}

	public static Class<?> getDbType(String javaType) {
		try {
			return getDbType(Class.forName(javaType));
		}
		catch (ClassNotFoundException e) {
			throw new IllegalArgumentException(javaType);
		}
	}

	public static EntityFieldMetaDataVO getFieldMeta(DbColumn column) {
		Class<?> cls = String.class;
		Integer scale = null;
		Integer precision = null;
		String outputformat = null;
		DbColumnType columnType = column.getColumnType();
		if(columnType.getGenericType() != null) {
			switch (columnType.getGenericType()) {
			case DATE:
			case DATETIME:
				cls = Date.class;
				break;
			case NUMERIC:
				scale = columnType.getPrecision();
				precision = columnType.getScale();
				// this seems wrong because it can be floating point number, too...
				if(columnType.getPrecision() != null && columnType.getPrecision() == 1
					&& columnType.getScale() != null && columnType.getScale() == 0) {
					// TODO
					// booleans are mapped as NUMBER(1), but it's possible that there are
					// other columns which are not meant as a boolean.
					cls = Boolean.class;
				}
				else if (columnType.getScale() != null && columnType.getScale() > 0) {
					cls = Double.class;
					outputformat = "#,##0.";
					for (int i = 0; i < columnType.getScale(); i++) {
						outputformat += "0";
					}
				}
				else {
					if (scale == null) {
						// default
						scale = 9;
					}
					cls = Integer.class;
				}
				break;
			case VARCHAR:
				cls = String.class;
				scale = columnType.getLength();
				break;
			}
		}

		EntityFieldMetaDataVO field = new EntityFieldMetaDataVO();
		field.setField(column.getColumnName());
		field.setDbColumn(column.getColumnName());
		field.setDataType(cls.getName());
		field.setScale(scale);
		field.setPrecision(precision);
		field.setFormatOutput(outputformat);
		field.setReadonly(false);
		field.setUnique(false);
		field.setNullable(true);
		field.setIndexed(false);
		field.setSearchable(false);
		field.setModifiable(false);
		field.setInsertable(false);
		field.setLogBookTracking(false);
		field.setShowMnemonic(false);
		field.setPermissiontransfer(false);
		return field;
	}

	public static DbColumnType getDbColumnType(Class<?> javaClass, Integer oldScale, Integer oldPrecision) {
		javaClass = DalUtils.getDbType(javaClass);
		DbGenericType genericType;
		Integer length = null, scale = null, precision = null;
		if (javaClass == String.class && oldScale == null) {
			genericType = DbGenericType.CLOB;
		} else if (javaClass == String.class) {
			genericType = DbGenericType.VARCHAR;
			length = oldScale;
		} else if (javaClass == Integer.class || javaClass == Long.class) {
			genericType = DbGenericType.NUMERIC;
			precision = oldScale;
			scale = 0;
		} else if (javaClass == Double.class) {
			genericType = DbGenericType.NUMERIC;
			precision = oldScale;
			scale = oldPrecision;
		} else if (javaClass == Boolean.class) {
			genericType = DbGenericType.BOOLEAN;
		} else if (javaClass == Date.class) {
			genericType = DbGenericType.DATE;
		} else if (javaClass == InternalTimestamp.class || javaClass == NuclosDateTime.class) {
			genericType = DbGenericType.DATETIME;
		} else if (javaClass == byte[].class) {
			genericType = DbGenericType.BLOB;
		} else if (javaClass == NuclosPassword.class) {
			genericType = DbGenericType.VARCHAR;
			length = CryptUtil.calcSizeForAESHexInputLength(oldScale);
		} else if (javaClass == NuclosScript.class) {
			genericType = DbGenericType.CLOB;
		} else {
			throw new IllegalArgumentException("Unsupported DB column type mapping for " + javaClass);
		}
		return new DbColumnType(genericType, null, length, precision, scale);
	}

	public static DbColumnType getDbColumnType(EntityFieldMetaDataVO meta) {
		try {
			return getDbColumnType(Class.forName(meta.getDataType()), meta.getScale(), meta.getPrecision());
		}
		catch (ClassNotFoundException e) {
			throw new NuclosFatalException(e);
		}
	}
}
