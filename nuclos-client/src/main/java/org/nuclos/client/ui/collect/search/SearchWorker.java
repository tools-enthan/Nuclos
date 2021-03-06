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
package org.nuclos.client.ui.collect.search;

import java.util.List;

import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common2.exception.CommonBusinessException;

/**
 * interface for multithreaded search.
 */
public interface SearchWorker<Clct extends Collectable> {
	/**
	 * performs some initial actions, if necessary, before executing the actual search.
	 * @throws CommonBusinessException
	 * @event-dispatch-thread
	 */
	void startSearch() throws CommonBusinessException;

	/**
	 * performs the actual search.
	 * @return List<Collectable> the search result
	 * @postcondition result != null
	 * @throws CommonBusinessException
	 * @own-thread
	 */
	List<Clct> getResult() throws CommonBusinessException;

	/**
	 * performs some actions, if necessary, after the actual search was executed.
	 * @param lstclctResult
	 * @throws CommonBusinessException
	 * @precondition lstclctResult != null
	 * @event-dispatch-thread
	 */
	void finishSearch(List<Clct> lstclctResult) throws CommonBusinessException;
}

