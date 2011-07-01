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
package org.nuclos.client.searchfilter;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.apache.log4j.Logger;
import org.nuclos.client.common.TopicNotificationReceiver;
import org.nuclos.client.main.Main;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.common.JMSConstants;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Pair;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.DateUtils;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;

/**
 * SearchFilterCache containing all searchfilters (entity + global searchfilters)
 * which are assigned to the current user.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:martin.weber@novabit.de">Martin Weber</a>
 * @version 00.01.000
 */

public class SearchFilterCache {

	private static final Logger log = Logger.getLogger(SearchFilterCache.class);

	private static SearchFilterCache singleton;

	private Map<Pair<String, String>, EntitySearchFilter> mpEntitySearchFilter = new HashMap<Pair<String, String>, EntitySearchFilter>();

	private final MessageListener messagelistener = new MessageListener() {
		@Override
        public void onMessage(Message msg) {
			log.debug("Received notification from server: search filter cache changed.");
			SearchFilterCache.this.validate();
			if (msg instanceof ObjectMessage) {
				try {
					String[] asUsers = (String[])((ObjectMessage)msg).getObject();
					for (String sUser : asUsers) {
						if (!(Main.getMainController().getUserName().equals(sUser)))
							UIUtils.runCommandLater(Main.getMainFrame(), new CommonRunnable() {			
								@Override
                                public void run() throws CommonBusinessException {
									Main.getMainController().refreshTaskController();
								}
							});	
					}
				}
				catch (JMSException ex) {
					log.warn("Exception thrown in JMS message listener.", ex);
				}
			}
			else {
				log.warn("Message of type " + msg.getClass().getName() + " received, while a TextMessage was expected.");
			}			
		}
	};
	
	private SearchFilterCache() {
		TopicNotificationReceiver.subscribe(JMSConstants.TOPICNAME_SEARCHFILTERCACHE, messagelistener);
		loadSearchFilters();
	}

	/**
	 * @return the one (and only) instance of SearchFilterCache
	 */
	public static synchronized SearchFilterCache getInstance() {
		if (singleton == null) {
			singleton = new SearchFilterCache();
		}
		return singleton;
	}

	/**
	 * initializes the cache for all entity searchfilters
	 */
	private void loadSearchFilters() {
		for (SearchFilter searchFilter : SearchFilterDelegate.getInstance().getAllSearchFilterByUser(Main.getMainController().getUserName())) {
			if (isFilterValid(searchFilter)) {
				if (searchFilter instanceof EntitySearchFilter) {
					mpEntitySearchFilter.put(new Pair<String, String>(searchFilter.getName(), searchFilter.getOwner()), (EntitySearchFilter)searchFilter);
				}
			}
		}
	}

	/**
	 * gets the searchfilter for the given filter name and owner
	 * @param sFilterName
	 * @param sOwner
	 * @return SearchFilter
	 */
	public SearchFilter getSearchFilter(String sFilterName, String sOwner) {
		new Pair<String, String>(sFilterName, sOwner);
		SearchFilter searchFilter = getEntitySearchFilter(sFilterName, sOwner);

		return searchFilter;
	}

	/**
	 * gets the entity searchfilter for the given filter name and owner
	 * @param sFilterName
	 * @param sOwner
	 * @return GlobalSearchFilter
	 */
	public EntitySearchFilter getEntitySearchFilter(String sFilterName, String sOwner) {
		if (StringUtils.isNullOrEmpty(sOwner)) {
			sOwner = Main.getMainController().getUserName();
		}

		for (Pair<String, String> pKey : mpEntitySearchFilter.keySet()) {
			if (pKey.equals(new Pair<String, String>(sFilterName, sOwner))) {
				return mpEntitySearchFilter.get(pKey);
			}
		}
		return null;
	}

	/**
	 * gets all entity searchfilters
	 * @return Collection<EntitySearchFilter>
	 */
	public Collection<EntitySearchFilter> getAllEntitySearchFilters() {
		Collection<EntitySearchFilter> collSearchFilter = new ArrayList<EntitySearchFilter>();
		for (EntitySearchFilter searchFilter : this.mpEntitySearchFilter.values()) {
			collSearchFilter.add(searchFilter);
		}
		return collSearchFilter;
	}

	/**
	 * checks whether a filter with the given name exists
	 * @param sFilterName
	 * @return boolean
	 */
	public boolean filterExists(String sFilterName) {
		return this.filterExists(sFilterName, null);
	}

	/**
	 * checks whether a filter with the given name and owner exists
	 * @param sFilterName
	 * @return boolean
	 */
	public boolean filterExists(String sFilterName, String sOwner) {
		if (sOwner == null) {
			sOwner = Main.getMainController().getUserName();
		}

		return (getEntitySearchFilter(sFilterName, sOwner) != null);
	}

	/**
	 * gets all entity searchfilters for the given entity
	 * @param sEntity
	 * @return Collection<EntitySearchFilter>
	 */
	public Collection<EntitySearchFilter> getEntitySearchFilterByEntity(String sEntity) {
		Collection<EntitySearchFilter> collEntitySearchFilter = new ArrayList<EntitySearchFilter>();

		if (sEntity == null) {
			return collEntitySearchFilter;
		}

		for (EntitySearchFilter entitySearchFilter : getAllEntitySearchFilters()) {
			if (sEntity. equals(entitySearchFilter.getEntityName())) {
				collEntitySearchFilter.add(entitySearchFilter);
			}
		}

		return collEntitySearchFilter;
	}

	/**
	 * gets the entity searchfilter by the given id
	 * @param iId
	 * @return EntitySearchFilter
	 */
	public EntitySearchFilter getEntitySearchFilterById(Integer iId) {
		for (EntitySearchFilter entitySearchFilter : getAllEntitySearchFilters()) {
			if (iId.equals(entitySearchFilter.getId())) {
				return entitySearchFilter;
			}
		}

		return null;
	}

	/**
	 * adds a filter to he cache
	 * @param searchFilter
	 */
	public void addFilter(SearchFilter searchFilter) {
		if (searchFilter instanceof EntitySearchFilter) {
			this.mpEntitySearchFilter.put(new Pair<String, String>(searchFilter.getName(), searchFilter.getOwner()), (EntitySearchFilter)searchFilter);
		}
	}

	/**
	 * removes the given filter from the cache
	 * @param searchFilter
	 */
	public void removeFilter(String sFilterName, String sOwner) {
		removeFilter(getSearchFilter(sFilterName, sOwner));
	}

	public void removeFilter(SearchFilter searchFilter) {
		final Pair<String, String> pIdentifier = new Pair<String, String>(searchFilter.getName(), searchFilter.getOwner());
		if (searchFilter instanceof EntitySearchFilter) {
			for (Pair<String, String> pKey : mpEntitySearchFilter.keySet()) {
				if (pKey.equals(pIdentifier)) {
					mpEntitySearchFilter.remove(pKey);
					break;
				}
			}
		}
	}

	/**
	 * gets all entity searchfilters which are forced to the current user
	 * @return List<EntitySearchFilter>
	 */
	public List<EntitySearchFilter> getForcedFilters() {
		return CollectionUtils.applyFilter(getAllUserFilters(),
			new Predicate<EntitySearchFilter>() {
				@Override
                public boolean evaluate(EntitySearchFilter t) {
	                return t.isForced();
                }});
	}
	
	/**
	 * gets all entity searchfilters of the current user
	 * @return List<EntitySearchFilter>
	 */
	public List<EntitySearchFilter> getAllUserFilters() {
		return CollectionUtils.applyFilter(getAllEntitySearchFilters(),
			new Predicate<EntitySearchFilter>() {
				@Override
                public boolean evaluate(EntitySearchFilter t) {
	                return isFilterValid(t);
                }});
		}

	/**
	 * checks whether a searchfilter is valid
	 * @param searchFilter
	 * @return boolean
	 */
	private boolean isFilterValid(SearchFilter searchFilter) {
		boolean bValid = false;

		Date dValidFrom = searchFilter.getValidFrom();
		Date dValidUntil = searchFilter.getValidUntil();

		Date dToday = DateUtils.today();

		Integer iFrom = DateUtils.compareDateFrom(dToday, dValidFrom);
		Integer iUntil = DateUtils.compareDateUntil(dToday, dValidUntil);

		if (iFrom >= 0 && iUntil <= 0) {
			bValid = true;
		}

		return bValid;
	}

	/**
	 * clears and fills this cache again.
	 * @throws NuclosFatalException
	 * @throws RemoteException
	 */
	public void validate() {
		this.mpEntitySearchFilter.clear();

		loadSearchFilters();
	}
}
