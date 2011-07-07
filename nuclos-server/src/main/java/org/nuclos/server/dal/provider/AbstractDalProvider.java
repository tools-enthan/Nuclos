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
package org.nuclos.server.dal.provider;

import java.io.IOException;
import java.util.Properties;

import org.nuclos.common.AbstractProvider;
import org.nuclos.common.NuclosFatalException;

public class AbstractDalProvider extends AbstractProvider {

	public static final String NUCLOS_DAL_PROPERTIES = "nuclos-dal.properties";

	public static Properties getDalProperties() {
		Properties props = new Properties();
		try {
			props.load(AbstractDalProvider.class.getClassLoader().getResourceAsStream(NUCLOS_DAL_PROPERTIES));
		}
		catch (IOException e) {
			throw new NuclosFatalException(e);
		}
		return props;
	}
}