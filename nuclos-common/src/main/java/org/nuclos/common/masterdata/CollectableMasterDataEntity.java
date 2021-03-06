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
package org.nuclos.common.masterdata;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.NullArgumentException;
import org.apache.log4j.Logger;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldFormat;
import org.nuclos.common.collect.collectable.CollectableUtils;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.collect.collectable.DefaultCollectableEntityField;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaFieldVO;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

/**
 * Provides structural (meta) information about a field in a master data table.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class CollectableMasterDataEntity implements CollectableEntity {

	private static final Logger LOG = Logger.getLogger(CollectableMasterDataEntity.class);

	private final MasterDataMetaVO mdmetavo;
	private final Map<String, CollectableEntityField> mpclctef = CollectionUtils.newHashMap();

	/**
	 * @param mdmetavo
	 * @precondition mdmetavo != null
	 */
	public CollectableMasterDataEntity(MasterDataMetaVO mdmetavo) {
		if (mdmetavo == null) {
			throw new NullArgumentException("mdmetavo");
		}
		this.mdmetavo = mdmetavo;
	}

	@Override
	public String getName() {
		return this.mdmetavo.getEntityName();
	}

	@Override
	public String getLabel() {
		return SpringLocaleDelegate.getInstance().getLabelFromMetaDataVO(mdmetavo);//this.mdmetavo.getLabel();
	}

	public MasterDataMetaVO getMasterDataMetaCVO() {
		return this.mdmetavo;
	}

	/**
	 * @param sFieldName
	 * @return non-null meta field
	 * @throws NuclosFatalException if there is no such field
	 */
	private MasterDataMetaFieldVO getMetaFieldVO(String sFieldName) {
		final MasterDataMetaFieldVO result = this.mdmetavo.getField(sFieldName);
		if (result == null) {
			final String sMessage = StringUtils.getParameterizedExceptionMessage("clctmdef.missing.field.error", sFieldName, this.mdmetavo.getEntityName());
				//"Das Feld " + sFieldName + " ist in der Entit\u00e4t " + this.mdmetavo.getEntityName() + " nicht vorhanden.";
			throw new NuclosFatalException(sMessage);
		}
		return result;
	}

	@Override
	public int getFieldCount() {
		return this.mdmetavo.getFieldNames().size();
	}

	@Override
	public Set<String> getFieldNames() {
		return this.mdmetavo.getFieldNames();
	}

	/**
	 * @return Does this entity have a "name" field?
	 */
	private boolean hasNameField() {
		return this.mdmetavo.getField(MasterDataVO.FIELDNAME_NAME) != null;
	}

	/**
	 * @return <code>"name"</code>, if this field is contained in this entity, <code>null</code> otherwise.
	 */
	@Override
	public String getIdentifierFieldName() {
		return hasNameField() ? MasterDataVO.FIELDNAME_NAME : null;
	}

	@Override
	public CollectableEntityField getEntityField(String sFieldName) {
		CollectableEntityField result = mpclctef.get(sFieldName);
		if (result == null) {
			if (getMetaFieldVO(sFieldName).getForeignEntity() != null)
				result = new CollectableMasterDataForeignKeyEntityField(mdmetavo.getField(sFieldName), getName());
			else if (getMetaFieldVO(sFieldName).getLookupEntity() != null)
				result = new CollectableMasterDataLookupKeyEntityField(mdmetavo.getField(sFieldName), getName());
			else {
				MasterDataMetaFieldVO mdmetafieldvo = getMetaFieldVO(sFieldName);

				int fieldtype = CollectableField.TYPE_VALUEFIELD;

				CollectableField cfDefault = null;

				if (mdmetafieldvo.getDefaultValue() == null) {
					cfDefault = CollectableUtils.getNullField(fieldtype);
				}
				else {
					try {
						cfDefault = new CollectableValueField(CollectableFieldFormat.getInstance(mdmetafieldvo.getJavaClass()).parse(null,mdmetafieldvo.getDefaultValue()));
					}
					catch(CollectableFieldFormatException e) {
						throw new CommonFatalException(e);
					}
				}

				result = new DefaultCollectableEntityField(
							sFieldName,
							mdmetafieldvo.getJavaClass(),
							SpringLocaleDelegate.getInstance().getResource(mdmetafieldvo.getResourceSIdForLabel(), mdmetafieldvo.getLabel()),
							SpringLocaleDelegate.getInstance().getResource(mdmetafieldvo.getResourceSIdForDescription(), mdmetafieldvo.getDescription()),
							mdmetafieldvo.getDataScale(),
							mdmetafieldvo.getDataPrecision(),
							mdmetafieldvo.isNullable(),
							fieldtype,
							null,
							cfDefault,
							mdmetafieldvo.getInputFormat(),
							mdmetafieldvo.getOutputFormat(),
							getName(),
							mdmetafieldvo.getDefaultComponentType());
			}
			mpclctef.put(sFieldName, result);
		}
		result.setCollectableEntity(this);
		return result;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof CollectableMasterDataEntity)) return false;
		final CollectableMasterDataEntity other = (CollectableMasterDataEntity) o;
		return mdmetavo.equals(other.mdmetavo);
	}
	
	@Override
	public int hashCode() {
		return mdmetavo.hashCode() + 1871;
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("entity=").append(mdmetavo);
		result.append(",fields=").append(mpclctef);
		result.append("]");
		return result.toString();
	}

}	// class CollectableMasterDataEntity
