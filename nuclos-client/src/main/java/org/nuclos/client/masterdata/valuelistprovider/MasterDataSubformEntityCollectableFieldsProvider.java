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
package org.nuclos.client.masterdata.valuelistprovider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.nuclos.client.common.ClientParameterProvider;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.masterdata.MetaDataCache;
import org.nuclos.common.ParameterProvider;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldsProvider;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;

/**
 * Value list provider for all subform entity names of a masterdata entity
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * @author	<a href="mailto:corina.mandoki@novabit.de">Corina Mandoki</a>
 * @version	01.00.00
 */
public class MasterDataSubformEntityCollectableFieldsProvider implements CollectableFieldsProvider{

	private static Logger log = Logger.getLogger(MasterDataSubformEntityCollectableFieldsProvider.class);
	String masterdata = null;
	
	@Override
	public void setParameter(String sName, Object oValue) {
		if (sName.equals("masterdata")) {
			this.masterdata = (String)oValue;
		}
	}
	
	@Override
	public List<CollectableField> getCollectableFields() throws CommonBusinessException {
		log.debug("getCollectableFields");
		
		Collection<MasterDataMetaVO> collmdmetavo = new ArrayList<MasterDataMetaVO>();
		
		for(String sSubform : MasterDataDelegate.getInstance().getSubFormEntityNamesByMasterDataEntity(masterdata, ClientParameterProvider.getInstance().getValue(ParameterProvider.KEY_LAYOUT_CUSTOM_KEY))) {
			collmdmetavo.add(MetaDataCache.getInstance().getMetaData(sSubform));				
		}				

		final List<CollectableField> result = CollectionUtils.transform(collmdmetavo, new Transformer<MasterDataMetaVO, CollectableField>() {
			@Override
			public CollectableField transform(MasterDataMetaVO mdmetavo) {
				return new CollectableValueField(mdmetavo.getEntityName());
			}
		});
		Collections.sort(result);
		
		return result;
	}
 	
}

