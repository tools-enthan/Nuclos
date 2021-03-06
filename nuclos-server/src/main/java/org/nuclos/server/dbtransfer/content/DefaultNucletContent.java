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
package org.nuclos.server.dbtransfer.content;

import java.util.List;

import org.nuclos.common.NuclosEntity;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.LangUtils;
import org.nuclos.server.dbtransfer.TransferUtils;

public class DefaultNucletContent extends AbstractNucletContent {

	public DefaultNucletContent(NuclosEntity entity, NuclosEntity parententity, List<INucletContent> contentTypes) {
		super(entity, parententity, contentTypes);
	}
	
	public DefaultNucletContent(NuclosEntity entity, NuclosEntity parententity,	List<INucletContent> contentTypes, boolean ignoreReferenceToNuclet) {
		super(entity, parententity, contentTypes, ignoreReferenceToNuclet);
	}

	@Override
	public boolean canDelete() {
		return true;
	}

	@Override
	public boolean canUpdate() {
		return true;
	}
	
	@Override
	public String getIdentifier(EntityObjectVO eo) {
		Object ident = eo.getFields().get(getIdentifierField());
		if (ident == null) {
			return "ID="+LangUtils.defaultIfNull(TransferUtils.getOriginId(eo), eo.getId());
		} else {
			return "\""+ident.toString()+"\"";
		}
	}
	
	@Override
	public boolean hasNameIdentifier(EntityObjectVO eo) {
		Object ident = eo.getFields().get(getIdentifierField());
		if (ident == null) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public String getIdentifierField() {
		return "name";
	}

}
