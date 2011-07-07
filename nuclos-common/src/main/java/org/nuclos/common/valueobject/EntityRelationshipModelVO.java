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
package org.nuclos.common.valueobject;

import org.nuclos.server.common.valueobject.NuclosValueObject;

/**
 * Value object representing a state model.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:ramin.goettlich@novabit.de">ramin.goettlich</a>
 * @version 01.00.00
 */
public class EntityRelationshipModelVO extends NuclosValueObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String sName;
	private String sDescription;
	

	public EntityRelationshipModelVO() {
		this(null, null);
	}

	/**
	 * constructor to be called by server only
	 * @param nvo
	 * @param sName model name of underlying database record
	 * @param sDescription model description of underlying database record
	 * @param layout model layout information of underlying database record
	 */
	public EntityRelationshipModelVO(NuclosValueObject nvo, String sName, String sDescription) {
		super(nvo);
		this.sName = sName;
		this.sDescription = sDescription;
	}

	/**
	 * constructor to be called by client only
	 * @param sName model name of underlying database record
	 * @param sDescription model description of underlying database record
	 * @param layout model layout information of underlying database record
	 */
	public EntityRelationshipModelVO(String sName, String sDescription) {
		super();
		this.sName = sName;
		this.sDescription = sDescription;

	}

	/**
	 * get model name of underlying database record
	 * @return model name of underlying database record
	 */
	public String getName() {
		return sName;
	}

	/**
	 * set model name of underlying database record
	 * @param sName model name of underlying database record
	 */
	public void setName(String sName) {
		this.sName = sName;
	}

	/**
	 * get model description of underlying database record
	 * @return model description of underlying database record
	 */
	public String getDescription() {
		return sDescription;
	}

	/**
	 * set model description of underlying database record
	 * @param sDescription model description of underlying database record
	 */
	public void setDescription(String sDescription) {
		this.sDescription = sDescription;
	}

}	// class StateModelVO