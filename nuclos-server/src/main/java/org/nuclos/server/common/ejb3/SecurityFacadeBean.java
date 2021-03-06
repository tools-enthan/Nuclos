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

import java.security.MessageDigest;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.security.RolesAllowed;
import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.nuclos.common.ApplicationProperties;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.ParameterProvider;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.SystemFields;
import org.nuclos.common.security.Permission;
import org.nuclos.common.transport.GzipMap;
import org.nuclos.common2.IdUtils;
import org.nuclos.common2.InternalTimestamp;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.common.AttributeCache;
import org.nuclos.server.common.MasterDataPermissions;
import org.nuclos.server.common.MetaDataServerProvider;
import org.nuclos.server.common.ModulePermissions;
import org.nuclos.server.common.SecurityCache;
import org.nuclos.server.common.ServerParameterProvider;
import org.nuclos.server.database.SpringDataBaseHelper;
import org.nuclos.server.dblayer.DbInvalidResultSizeException;
import org.nuclos.server.dblayer.DbStatementUtils;
import org.nuclos.server.dblayer.DbTuple;
import org.nuclos.server.dblayer.expression.DbId;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.transaction.annotation.Transactional;

/**
 * Facade bean for searchfilter. <br>
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
*/
@Transactional(noRollbackFor= {Exception.class})
public class SecurityFacadeBean implements SecurityFacadeRemote {
	
	private static final Logger LOG = Logger.getLogger(SecurityFacadeBean.class);
	
	private ServerParameterProvider serverParameterProvider;
	
	private SpringDataBaseHelper dataBaseHelper;
	
	public SecurityFacadeBean() {
	}

	@PostConstruct
	public void postConstruct() {
		LOG.info("Authentication successful.");
		getCurrentApplicationInfoOnServer(); // prefill md5 info string.
	}
	
	@Autowired
	void setServerParameterProvider(ServerParameterProvider serverParameterProvider) {
		this.serverParameterProvider = serverParameterProvider;
	}
	
	@Autowired
	void setDataBaseHelper(SpringDataBaseHelper dataBaseHelper) {
		this.dataBaseHelper = dataBaseHelper;
	}

	private final String getCurrentUserName() {
		return SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
	}

	/**
	 * logs the current user in.
	 * @return session id for the current user
	 */
    @RolesAllowed("Login")
	public Integer login() {
		final Integer result = writeLoginProtocol(this.getCurrentUserName());
		LOG.info("User " + this.getCurrentUserName() + " successfully logged in.");
		return result;
	}

	/**
	 * logs the current user out.
	 * @param iSessionId session id for the current user
	 */
    @RolesAllowed("Login")
	public void logout(Integer iSessionId) throws LoginException {
		this.writeLogoutProtocol(iSessionId);
		LOG.info("User " + this.getCurrentUserName() + " logged out.");
	}

	/**
	 * @return information about the current version of the application installed on the server.
	 */
    @RolesAllowed("Login")
	public ApplicationProperties.Version getCurrentApplicationVersionOnServer() {
		return ApplicationProperties.getInstance().getNuclosVersion();
	}

    private String sApplicationInfo = null;
    public String getCurrentApplicationInfoOnServer() {
    	if (sApplicationInfo == null) {
	    	try {
	        	//@todo mixup ip of current server. - not only database.
	        	String info = SpringDataBaseHelper.getInstance().getCurrentConnectionInfo();
	        	sApplicationInfo = new String(Hex.encode(MessageDigest.getInstance("MD5").digest(info.getBytes())));
			} catch (Exception e) {
				throw new NuclosFatalException(e);
			}
    	}
    	return sApplicationInfo;
    }
    
	/**
	 * @return information about the current version of the application installed on the server.
	 */
    @RolesAllowed("Login")
	public String getUserName() {
		return this.getCurrentUserName();
	}

	/**
	 * Get all actions that are allowed for the current user.
	 * @return set that contains the Actions objects (no duplicates).
	 */
    @RolesAllowed("Login")
	public Set<String> getAllowedActions() {
		return SecurityCache.getInstance().getAllowedActions(this.getCurrentUserName());
	}

	/**
	 * @return the module permissions for the current user.
	 */
    @RolesAllowed("Login")
	public ModulePermissions getModulePermissions() {
		return SecurityCache.getInstance().getModulePermissions(this.getCurrentUserName());
	}

	/**
	 * @return the masterdata permissions for the current user.
	 */
    @RolesAllowed("Login")
	public MasterDataPermissions getMasterDataPermissions() {
		return SecurityCache.getInstance().getMasterDataPermissions(this.getCurrentUserName());
	}

	/**
	 * get a String representation of the users session context
	 */
    @RolesAllowed("Login")
	public String getSessionContextAsString() {
		return this.getCurrentUserName();
	}

	/**
	 * write the successful login into the protocol table
	 */
	private Integer writeLoginProtocol(String sUserName) {
		try {
			final String sApplicationName = ApplicationProperties.getInstance().getName();

			DbId nextId = new DbId();
			dataBaseHelper.execute(DbStatementUtils.insertInto("T_AD_SESSION_STATISTIC",
				"SESSION_ID", nextId,
				"USER_ID", sUserName,
				"APPLICATION", sApplicationName,
				"LOGON", new InternalTimestamp(Calendar.getInstance().getTimeInMillis())));

			return nextId.getIdValueInt();
		}
		catch (Exception ex) {
			throw new CommonFatalException("Could not write login protocol", ex);//"Konnte Login nicht protokollieren."
		}
	}

	/**
	 * write the successful logout into the protocol table
	 */
	private void writeLogoutProtocol(Integer iSessionId) {
		dataBaseHelper.execute(DbStatementUtils.updateValues("T_AD_SESSION_STATISTIC",
			"LOGOFF", new InternalTimestamp(Calendar.getInstance().getTimeInMillis())).where("SESSION_ID", iSessionId));
	}

	/**
	 * get the user id from the database
	 * @param sUserName the user name in lower case characters
	 * @return
	 * @throws CommonFatalException
	 */
	public Integer getUserId(final String sUserName) throws CommonFatalException {
		DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<Integer> query = builder.createQuery(Integer.class);
		DbFrom t = query.from("T_MD_USER").alias(SystemFields.BASE_ALIAS);
		query.select(t.baseColumn("INTID", Integer.class));
		query.where(builder.equal(builder.upper(t.baseColumn("STRUSER", String.class)), builder.upper(builder.literal(sUserName))));
		Integer executeQuerySingleResult = null;
		try{
			executeQuerySingleResult = dataBaseHelper.getDbAccess().executeQuerySingleResult(query);
		} catch (DbInvalidResultSizeException e){
			throw new CommonFatalException("Could not find user "+sUserName);
		}
		return executeQuerySingleResult;
	}

	/**
	 * @return the readable subforms
	 */
    public java.util.Map<Integer, org.nuclos.common.security.Permission> getSubFormPermission(String sEntityName) {
		return SecurityCache.getInstance().getSubForm(getCurrentUserName(), sEntityName);
	}

	/**
	 * determine the permission of an attribute regarding the state of a module for the current user
	 * @param sAttributeName
	 * @param iModuleId
	 * @param iStatusNumeral
	 * @return Permission
	 */
    public Permission getAttributePermission(String sEntity, String sAttributeName, Integer iState) {
		// special behaviour for modules which have no statusnumeral (e.g. Rechnungsabschnitte)
		  if (iState == null) {
		   return Permission.READWRITE;
		  }

		  Integer iAttributeGroupId = AttributeCache.getInstance().getAttribute(sEntity, sAttributeName).getAttributegroupId();
		  Map<Integer, Permission> mpAttributePermission = SecurityCache.getInstance().getAttributeGroup(getCurrentUserName(), iAttributeGroupId);

		  return mpAttributePermission.get(iState);
	}

	public Map<String, Permission> getAttributePermissionsByEntity(String entity, Integer stateId) {
		HashMap<String, Permission> res = new HashMap<String, Permission>();
		MetaDataServerProvider mdProv = MetaDataServerProvider.getInstance();
		SecurityCache secCache = SecurityCache.getInstance();
		String user = getCurrentUserName();

		Map<String, EntityFieldMetaDataVO> fields = mdProv.getAllEntityFieldsByEntity(entity);
		for(Map.Entry<String, EntityFieldMetaDataVO> e : fields.entrySet()) {
			Permission p;
			if(stateId == null)
				p = Permission.READWRITE;
			else
				p = secCache.getAttributeGroup(
						user,
						IdUtils.unsafeToId(e.getValue().getFieldGroupId()))
					.get(stateId);
			res.put(e.getKey(), p);
		}
		return res;
	}

    @RolesAllowed("Login")
	public Boolean isSuperUser() {
		return SecurityCache.getInstance().isSuperUser(this.getCurrentUserName());
	}

    @RolesAllowed("Login")
	public void invalidateCache(){
		SecurityCache.getInstance().invalidate();
	}

    public Map<String, Object> getInitialSecurityData() {
		GzipMap<String, Object> res = new GzipMap<String, Object>();
		res.put(SecurityFacadeRemote.USERNAME, getCurrentUserName());
		res.put(SecurityFacadeRemote.IS_SUPER_USER, isSuperUser());
		res.put(SecurityFacadeRemote.ALLOWED_ACTIONS, getAllowedActions());
		res.put(SecurityFacadeRemote.MODULE_PERMISSIONS, getModulePermissions());
		res.put(SecurityFacadeRemote.MASTERDATA_PERMISSIONS, getMasterDataPermissions());
		return res;
    }

	public Boolean isLdapAuthenticationActive() {
		DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<Long> query = builder.createQuery(Long.class);
		DbFrom t = query.from("T_AD_LDAPSERVER").alias(SystemFields.BASE_ALIAS);
		query.select(t.baseColumn("INTID", Long.class));
		query.where(builder.and(builder.equal(t.baseColumn("BLNACTIVE", Boolean.class), true), builder.isNotNull(t.baseColumn("STRUSERFILTER", String.class))));

		return dataBaseHelper.getDbAccess().executeQuery(query).size() > 0;
	}

	public Boolean isLdapSynchronizationActive() {
		DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<Long> query = builder.createQuery(Long.class);
		DbFrom t = query.from("T_AD_LDAPSERVER").alias(SystemFields.BASE_ALIAS);
		query.select(t.baseColumn("INTID", Long.class));
		query.where(builder.and(builder.equal(t.baseColumn("BLNACTIVE", Boolean.class), true), builder.isNotNull(t.baseColumn("SEARCHFILTER", String.class))));

		return dataBaseHelper.getDbAccess().executeQuery(query).size() > 0;
	}

	public Date getPasswordExpiration() {
		Integer interval = 0;
		if (serverParameterProvider.getValue(ParameterProvider.KEY_SECURITY_PASSWORD_INTERVAL) != null) {
			interval = serverParameterProvider.getIntValue(ParameterProvider.KEY_SECURITY_PASSWORD_INTERVAL, 0);
		}

		if (interval == 0) {
			return null;
		}

		String username = getCurrentUserName();

		DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<DbTuple> query = builder.createTupleQuery();
		DbFrom t = query.from("T_MD_USER").alias(SystemFields.BASE_ALIAS);
		query.multiselect(t.baseColumn("DATPASSWORDCHANGED", Date.class), t.baseColumn("BLNSUPERUSER", Boolean.class));
		query.where(builder.equal(builder.upper(t.baseColumn("STRUSER", String.class)), builder.upper(builder.literal(username))));
		DbTuple tuple = dataBaseHelper.getDbAccess().executeQuerySingleResult(query);

		Date d = tuple.get(0, Date.class);
		Boolean isSuperUser = Boolean.TRUE.equals(tuple.get(1, Boolean.class));
		if (d != null && (!isLdapAuthenticationActive() || isSuperUser)) {
			Calendar c = Calendar.getInstance();
			c.setTime(d);
			c.add(Calendar.DAY_OF_MONTH, interval);
			return c.getTime();
		}
		return null;
	}
}
