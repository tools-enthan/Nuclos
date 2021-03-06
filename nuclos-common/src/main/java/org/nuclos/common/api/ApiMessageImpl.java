//Copyright (C) 2012  Novabit Informationssysteme GmbH
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
package org.nuclos.common.api;

import java.io.Serializable;

import org.nuclos.api.Message;

public class ApiMessageImpl implements Serializable {

	private static final long serialVersionUID = -8597812081010245467L;

	private final org.nuclos.api.Message message;
	
	private final Integer receiverId;

	public ApiMessageImpl(Message message, Integer receiverId) {
		super();
		this.message = message;
		this.receiverId = receiverId;
	}

	public org.nuclos.api.Message getMessage() {
		return message;
	}

	public Integer getReceiverId() {
		return receiverId;
	}

	@Override
	public String toString() {
		return "ApiMessageImpl[message=" + getMessage() + ",receiverId=" + getReceiverId() + "]";
	}
	
}
