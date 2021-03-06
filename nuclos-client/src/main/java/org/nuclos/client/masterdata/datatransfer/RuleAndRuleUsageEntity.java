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
package org.nuclos.client.masterdata.datatransfer;

import java.awt.datatransfer.DataFlavor;

import org.nuclos.common2.LangUtils;
import org.nuclos.server.ruleengine.valueobject.RuleVO;

/**
 * Transferdata for an Rule and its Usage in a Entity
 * @author <a href="mailto:rainer.schneider@novabit.de">rainer.schneider</a>
 *
 */
public class RuleAndRuleUsageEntity {

	private final RuleVO ruleVo;
	private final String eventName;
	private final String entity;
	private final Integer statusId;
	private final Integer processId;

	public RuleAndRuleUsageEntity(RuleVO aRuleVo, String aEventName, String aEntity, Integer processId, Integer statusId) {
		super();
		this.ruleVo = aRuleVo;
		this.eventName = aEventName;
		this.entity = aEntity;
		this.statusId = statusId;
		this.processId = processId;
	}

	public String getEventName() {
		return eventName;
	}

	public String getEntity() {
		return entity;
	}

	public Integer getProcessId() {
		return processId;
	}

	public Integer getStatusId() {
		return statusId;
	}

	public RuleVO getRuleVo() {
		return ruleVo;
	}

	public static class RuleUsageDataFlavor extends DataFlavor {

		public RuleUsageDataFlavor() {
			super(RuleAndRuleUsageEntity.class, "ruleWithUsage");
		}

		/**
		 * Note that we have to override equals(DataFlavor) rather than equals(Object) here.
		 * @param that
		 * @return
		 */
		@Override
		public boolean equals(java.awt.datatransfer.DataFlavor that) {
			final boolean result;
			if (!super.equals(that)) {
				result = false;
			}
			else {
				result = LangUtils.equals(this.getHumanPresentableName(), that.getHumanPresentableName());
			}
			return result;
		}

	}

}
