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
package org.nuclos.server.common.ejb3;

import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Remote;

import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.WorkspaceDescription;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.server.common.valueobject.PreferencesVO;

@Remote
public interface PreferencesFacadeRemote {

	/**
	 * @return the preferences for the user currently logged
	 */
	@RolesAllowed("Login")
	public abstract PreferencesVO getUserPreferences()
		throws CommonFinderException;
	
	/**
	 * @return the preferences for the user defined as template user, exception if not defined in t_ad_parameter
	 */
	@RolesAllowed("Login")
	public PreferencesVO getTemplateUserPreferences() throws NuclosBusinessException, CommonFinderException;

	/**
	 * stores the preferences for the user currently logged
	 * @param prefsvo the preferences for the user currently logged
	 */
	@RolesAllowed("Login")
	public abstract void modifyUserPreferences(PreferencesVO prefsvo)
		throws CommonFinderException;

	/**
	 * sets the preferences for the user with the given name (administrative use only).
	 * @param sUserName
	 * @param prefsvo the preferences for the user currently logged. If <code>null</code>, the preferences for the given user will be reset.
	 * @throws CommonFinderException
	 */
	@RolesAllowed("UseManagementConsole")
	public abstract void setPreferencesForUser(String sUserName,
		PreferencesVO prefsvo) throws CommonFinderException;

	/**
	 * sets the preferences for the user with the given name (administrative use only).
	 * @param sUserName
	 * @param prefsvo the preferences for the user currently logged. If <code>null</code>, the preferences for the given user will be reset.
	 * @throws CommonFinderException
	 */
	@RolesAllowed("UseManagementConsole")
	public abstract PreferencesVO getPreferencesForUser(String sUserName)
		throws CommonFinderException;

	/**
	 * Merges the given preferences into the preferences for the user with the given name (administrative use only).
	 * @throws CommonFinderException
	 */
	@RolesAllowed("UseManagementConsole")
	public abstract void mergePreferencesForUser(String name, Map<String, Map<String, String>> preferencesToMerge)
		throws CommonFinderException;
	
	/**
	 * 
	 * @return
	 */
	public List<String> getWorkspaceNames();
	
	/**
	 * 
	 * @param name
	 * @return
	 * @throws CommonFinderException
	 */
	public WorkspaceDescription getWorkspace(String name) throws CommonFinderException;
		
	/**
	 * 
	 * @param wd
	 */
	public void storeWorkspace(WorkspaceDescription wd);
	
	/**
	 * 
	 * @param name
	 */
	public void removeWorkspace(String name);
}
