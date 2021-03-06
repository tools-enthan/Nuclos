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

package org.nuclos.server.webservice.ejb3;

import java.util.List;

import javax.jws.WebParam;
import javax.jws.WebService;

@WebService
public interface WebEntryWS {
	/**
	 * List all readable entities in the system.
	 * 
	 * @param user the user
	 * @param pass the password
	 * @return a list of readable entities, including system entities
	 */
	List<String> listEntities_A(@WebParam(name="user") String user, @WebParam(name="pass") String pass);
	
	/**
	 * List the objects of an entity.
	 * 
	 * @param user the user
	 * @param pass the password
	 * @param entityName the entity name to list
	 * @return a list of object ids
	 */
	List<Long> list_A(@WebParam(name="user") String user, @WebParam(name="pass") String pass, @WebParam(name="entityName") String entityName);
	
	/**
	 * Read a given object of a given entity
	 * 
	 * @param user the user
	 * @param pass the password
	 * @param entityName the entity name to read
	 * @param id the object id to read
	 * @return a list of string-represantations in the form "key=value" for all
	 * readable attributes of the given object
	 */
	List<String> read_A(@WebParam(name="user") String user, @WebParam(name="pass") String pass, @WebParam(name="entityName") String entityName, @WebParam(name="id") Long id);
	
	/**
	 * Execute a business rule on a given object
	 * 
	 * @param user the user
	 * @param pass the password
	 * @param entityName the entity name to read
	 * @param id the object id to read
	 * @param rulename the rule to execute
	 */
	void executeBusinessRule_A(@WebParam(name="user") String user, @WebParam(name="pass") String pass, @WebParam(name="entityName") String entityName, @WebParam(name="id") Long id, @WebParam(name="rulename") String rulename);
}
