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
package org.nuclos.common.collect.collectable;

import java.io.Serializable;
import java.util.Date;
import java.util.prefs.Preferences;

import org.nuclos.common.DefaultComponentTypes;
import org.nuclos.common.NuclosImage;
import org.nuclos.common.collect.collectable.access.CefSecurityAgent;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.RelativeDate;
import org.nuclos.common2.StringUtils;

/**
 * Abstract implementation of a <code>CollectableEntityField</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * <p>
 * TODO: Why there a such strange transient fields? Are they really needed? If this is mainly
 * to write to {@link Preferences}, consider todo below.
 * 
 * </p><p>
 * TODO: Consider {@link org.nuclos.client.common.CollectableEntityFieldPreferencesUtil}
 * to write to {@link Preferences}.
 * </p>
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public abstract class AbstractCollectableEntityField implements CollectableEntityField, Serializable {

	private static final long serialVersionUID = -6972777641358799942L;

	/**
	 * @deprecated Not always present. Why is this transient? How is a value after serialization enforced?
	 */
	private transient CollectableEntity clcte;
	
	/**
	 * @deprecated Why is this transient? How is a value after serialization enforced?
	 */
	private transient CefSecurityAgent securityAgent;
	
	public AbstractCollectableEntityField() {
	}

	@Override
    public boolean isIdField() {
		return (this.getFieldType() == TYPE_VALUEIDFIELD);
	}

	@Override
	public CollectableEntity getCollectableEntity() {
		return clcte;
	}

	@Override
	public void setCollectableEntity(CollectableEntity clent) {
		clcte = clent;
	}

	@Override
    public int getDefaultCollectableComponentType() {
		if (getDefaultComponentType() != null) {
			if (StringUtils.equalsIgnoreCase(getDefaultComponentType(), DefaultComponentTypes.HYPERLINK)) {
				return CollectableComponentTypes.TYPE_HYPERLINK;
			}
			if (StringUtils.equalsIgnoreCase(getDefaultComponentType(), DefaultComponentTypes.EMAIL)) {
				return CollectableComponentTypes.TYPE_EMAIL;
			}	
		}
		
		final int result;
		if (this.isIdField() && this.getJavaClass() != NuclosImage.class) {
			// default is combobox. listofvalues must be specified as controltype explicitly.
			if (getDefaultComponentType() != null) {
				if (StringUtils.equalsIgnoreCase(getDefaultComponentType(), DefaultComponentTypes.COMBOBOX))
					result = CollectableComponentTypes.TYPE_COMBOBOX;
				else if (StringUtils.equalsIgnoreCase(getDefaultComponentType(), DefaultComponentTypes.LISTOFVALUES))
					result = CollectableComponentTypes.TYPE_LISTOFVALUES;
				else 
					result = CollectableComponentTypes.TYPE_COMBOBOX;
			} else
				result = CollectableComponentTypes.TYPE_COMBOBOX;
		}
		else {
			result = CollectableUtils.getCollectableComponentTypeForClass(this.getJavaClass());
		}
		return result;
	}

	@Override
    public final CollectableField getNullField() {
		final CollectableField result = CollectableUtils.getNullField(this.getFieldType());

		assert result != null;
		assert result.isNull();
		assert result.getFieldType() == this.getFieldType();
		return result;
	}

	/**
	 * @return getNullField(). Successors may specify a non-null default value here.
	 */
	@Override
    public CollectableField getDefault() {
		final CollectableField result = this.getNullField();
		assert result != null;
		assert result.getFieldType() == this.getFieldType();
		if (!(this.getJavaClass() == Date.class && result.getValue() != null && result.getValue().toString().equalsIgnoreCase(RelativeDate.today().toString())))
			assert LangUtils.isInstanceOf(result.getValue(), this.getJavaClass());
		return result;
	}

	/**
	 * @precondition this.isIdField()
	 * @return <code>false</code>. This may be overridden by subclasses.
	 */
	@Override
    public boolean isRestrictedToValueList() {
		if (!this.isIdField()) {
			throw new IllegalStateException("isIdField");
		}
		return false;
	}

	/**
	 * @return "name" (as default)
	 */
	@Override
    public String getReferencedEntityFieldName() {
		return "name";
	}

	/**
	 * Two <code>CollectableEntityField</code> instances are equals iff their names
	 * (as defined by <code>getName()</code>) are equal.
	 */
	@Override
	public boolean equals(Object o) {
		return (this == o) || ((o instanceof CollectableEntityField) && this.getName().equals(
				((CollectableEntityField) o).getName()));
	}

	/**
	 * @return hash code based on getName().
	 */
	@Override
	public int hashCode() {
		return this.getName().hashCode();
	}

	/**
	 * @precondition isReferencing()
	 */
	@Override
    public boolean isReferencedEntityDisplayable() {
		if (!isReferencing()) {
			throw new IllegalStateException("referencing");
		}
		return true;
	}

	/**
	 * @return <code>this.getLabel()</code>
	 */
	@Override
	public String toString() {
		return this.getLabel() == null ? this.getName() : this.getLabel();
	}

	/**
	 * set security agent
	 */
	@Override
    public void setSecurityAgent(CefSecurityAgent sa) {
		this.securityAgent = sa;
	}

	/**
	 * get security agent
	 */
	@Override
    public CefSecurityAgent getSecurityAgent() {
		return this.securityAgent;
	}

	/**
	 * is this field readable
	 */
	@Override
    public boolean isReadable() {
		return getSecurityAgent().isReadable();
	}

	/**
	 * is this field writable
	 */
	@Override
    public boolean isWritable() {
		return getSecurityAgent().isWritable();
	}

	/**
	 * is this field removable
	 */
	@Override
    public boolean isRemovable() {
		return getSecurityAgent().isRemovable();
	}

	public String toDescription() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("name=").append(getName());
		result.append(",label=").append(getLabel());
		result.append(",entity=").append(getEntityName());
		result.append(",refField=").append(getReferencedEntityFieldName());
		result.append(",refEntity=").append(getReferencedEntityName());
		result.append("]");
		return result.toString();
	}

}	// class AbstractCollectableEntityField
