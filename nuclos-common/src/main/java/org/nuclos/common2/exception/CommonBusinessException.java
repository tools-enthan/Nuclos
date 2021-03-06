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
package org.nuclos.common2.exception;

/**
 * General Novabit business (non-fatal, checked) exception.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph Radig</a>
 * @version	01.00.00
 */

public class CommonBusinessException extends Exception {

	@Deprecated
	static final String PERMISSION = "common.exception.novabitpermissionexception";

	@Deprecated
	static final String CREATE = "common.exception.novabitcreateexception";

	@Deprecated
	static final String FINDER = "common.exception.novabitfinderexception";

	@Deprecated
	static final String REMOVE = "common.exception.novabitremoveexception";

	@Deprecated
	static final String STALE_VERSION = "common.exception.novabitstaleversionexception";

	@Deprecated
	static final String SEARCH_CANCELLED = "common.exception.novabitsearchcancelledexception";

	@Deprecated
	static final String REMOTE = "common.exception.novabitremoteexception";

	@Deprecated
	static final String VALIDATION = "common.exception.novabitvalidationexception";

	public CommonBusinessException() {
		super();
	}

	/**
	 * @param sMessage exception message
	 */
	public CommonBusinessException(String sMessage) {
		super(sMessage);
	}

	/**
	 * @param sMessage exception message
	 * @param tCause wrapped exception
	 */
	public CommonBusinessException(String sMessage, Throwable tCause) {
		super(sMessage != null ? sMessage : getMessage(tCause), tCause);
	}

	/**
	 * @param tCause wrapped exception
	 */
	public CommonBusinessException(Throwable tCause) {
		super(getMessage(tCause));
	}

	protected static String getMessage(Throwable cause) {
		if (cause == null) {
			return null;
		}
		Throwable t = cause;
		if (t.getMessage() != null && (t instanceof CommonBusinessException || t instanceof CommonFatalException)) {
			return t.getMessage();
		}
		return cause.toString();
	}

}
