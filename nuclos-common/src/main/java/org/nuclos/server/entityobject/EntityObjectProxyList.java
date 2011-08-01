//Copyright (C) 2011  Novabit Informationssysteme GmbH
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
package org.nuclos.server.entityobject;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.dal.vo.PivotInfo;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.server.common.ejb3.EntityObjectFacadeRemote;
import org.nuclos.server.genericobject.AbstractProxyList;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;

/**
 * Proxy list for entity object search results.
 * <p>
 * Created by Novabit Informationssysteme GmbH
 * </p><p>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * </p>
 * @author Thomas Pasch
 * @since Nuclos 3.1.01
 */
public class EntityObjectProxyList extends AbstractProxyList<Long, EntityObjectVO> {

	private static EntityObjectFacadeRemote facade;

	private final Long id;
	private final CollectableSearchExpression clctexpr;
	private final Set<Long> stRequiredAttributeIds;
	private final Set<String> stRequiredSubEntities;
	private final Set<PivotInfo> pivots;
	private final boolean includeDependents;
	
	public EntityObjectProxyList(Long id, CollectableSearchExpression clctexpr, Set<Long> stRequiredAttributeIds, 
			Set<String> stRequiredSubEntityNames, Set<PivotInfo> pivots, boolean includeDependents) {
		super();
		this.id = id;
		this.clctexpr = clctexpr;
		this.stRequiredAttributeIds = stRequiredAttributeIds;
		this.stRequiredSubEntities = stRequiredSubEntityNames;
		this.pivots = pivots;
		this.includeDependents = includeDependents;

		this.initialize();		
	}
	
	@Override
	protected Collection<EntityObjectVO> fetchNextChunk(List<Long> lstIntIds) throws RuntimeException {
		return getEntityObjectFacade().getEntityObjectsMore(id, lstIntIds, stRequiredAttributeIds, stRequiredSubEntities, pivots, includeDependents);
	}

	@Override
	protected void fillListOfIds() {
		try {
			List<Long> lstIds = getEntityObjectFacade().getEntityObjectIds(id, clctexpr);
			setListOfIds(lstIds);
		}
		catch(RuntimeException e) {
			throw new NuclosFatalException(e);
		}
	}

	private synchronized EntityObjectFacadeRemote getEntityObjectFacade() {
		if (facade == null) {
			try {
				facade = ServiceLocator.getInstance().getFacade(EntityObjectFacadeRemote.class);
			}
			catch (RuntimeException ex) {
				throw new NuclosFatalException(ex);
			}
		}
		return facade;
	}
	
	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		final int size = size();
		result.append("EntityObjectProxyList[");
		result.append("size=").append(size);
		result.append(",reqFields=").append(stRequiredAttributeIds);
		result.append(",reqSubforms=").append(stRequiredSubEntities);
		result.append(",pivots=").append(pivots);
		result.append(",search=").append(clctexpr);
		mapDescription(result, mpObjects, 5);
		result.append("]");
		return result.toString();
	}
	
}
