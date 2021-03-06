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
package org.nuclos.server.masterdata;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.security.RolesAllowed;

import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common2.EntityAndFieldName;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.server.genericobject.AbstractProxyList;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeRemote;
import org.nuclos.server.masterdata.valueobject.MasterDataWithDependantsVO;

/**
 * Proxy list for master data search results.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph Radig</a>
 * @version 00.01.000
 */
public class MasterDataProxyList extends AbstractProxyList<Object, MasterDataWithDependantsVO> {

	private static MasterDataFacadeRemote mdfacade;

	protected final String sEntityName;
	protected final CollectableSearchExpression clctexpr;
	private final List<EntityAndFieldName> lstRequiredSubEntities;

	/**
	 * @param sEntityName
	 * @param clctexpr
	 * @param iRecordCount
	 */
	public MasterDataProxyList(String sEntityName, CollectableSearchExpression clctexpr, List<EntityAndFieldName> lstRequiredSubEntities) {
		super();

		this.sEntityName = sEntityName;
		this.clctexpr = clctexpr;
		this.lstRequiredSubEntities = lstRequiredSubEntities;

		this.initialize();
	}

	@Override
	protected Collection<MasterDataWithDependantsVO> fetchNextChunk(List<Object> lstIntIds) throws RemoteException {
		return this.getMasterDataFacade().getMasterDataMore(this.sEntityName, lstIntIds, lstRequiredSubEntities);
	}

	@Override
	protected Collection<MasterDataWithDependantsVO> fetchChunk(int istart, int iend) throws RemoteException {
		return this.getMasterDataFacade().getMasterDataChunk(this.sEntityName, this.clctexpr, istart, iend);
	}
	
	@Override
	protected Integer countMasterDataRows() {
    	return this.getMasterDataFacade().countMasterDataRows(this.sEntityName, this.clctexpr);
	}

	
	@Override
	protected void fillListOfIds() {
		if (isNewLL()) {
			setListOfIds(new ArrayList<Object>());
			return;
		}
		List<Object> lstIds = getMasterDataFacade().getMasterDataIds(sEntityName, clctexpr);
		setListOfIds(lstIds);
	}
	
	private synchronized MasterDataFacadeRemote getMasterDataFacade() {
		if (mdfacade == null) {
			try {
				// mdfacade = ServiceLocator.getInstance().getFacade(MasterDataFacadeRemote.class);
				mdfacade = (MasterDataFacadeRemote) SpringApplicationContextHolder.getBean("masterDataService");
			}
			catch (RuntimeException ex) {
				throw new NuclosFatalException(ex);
			}
		}
		return mdfacade;
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		final int size = size();
		result.append("GenericObjectProxyList[");
		result.append("size=").append(size);
		result.append(",entity=").append(sEntityName);
		result.append(",reqSubforms=").append(lstRequiredSubEntities);
		result.append(",search=").append(clctexpr);
		mapDescription(result, mpObjects, 5);
		result.append("]");
		return result.toString();
	}
	
}	// class MasterDataProxyList
