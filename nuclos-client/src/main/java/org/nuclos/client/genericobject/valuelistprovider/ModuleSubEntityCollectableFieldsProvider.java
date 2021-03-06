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
package org.nuclos.client.genericobject.valuelistprovider;

import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldsProvider;
import org.nuclos.common2.exception.CommonBusinessException;

/**
 * Value list provider to get subentities of a module.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:uwe.allner@novabit.de">uwe.allner</a>
 * @version 00.01.000
 * @deprecated legacy VLP
 */
@Deprecated
public class ModuleSubEntityCollectableFieldsProvider implements CollectableFieldsProvider {
	private static final Logger log = Logger.getLogger(ModuleSubEntityCollectableFieldsProvider.class);

	private Integer iModuleId;

	/**
	 * valid parameters:
	 *   "module": module id for restricting available subforms
	 * @param sName parameter name
	 * @param oValue parameter value
	 */
	@Override
	public void setParameter(String sName, Object oValue) {
		log.debug("setParameter - sName = " + sName + " - oValue = " + oValue);
		if (sName.equals("module")) {
			this.iModuleId = (Integer) oValue;
		}
		else {
			// ignore
		}
	}

	@Override
	public List<CollectableField> getCollectableFields() throws CommonBusinessException {
		log.debug("getCollectableFields");

		if (this.iModuleId == null) {
			return Collections.<CollectableField>emptyList();
		} else {
			List<CollectableField> result = MasterDataDelegate.getInstance().getSubEntities(iModuleId);
			Collections.sort(result);
			return result;
		}
	}

}	// class ModuleSubEntityCollectableFieldsProvider
