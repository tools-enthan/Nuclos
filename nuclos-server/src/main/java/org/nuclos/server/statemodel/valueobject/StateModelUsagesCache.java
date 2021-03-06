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
package org.nuclos.server.statemodel.valueobject;

import javax.annotation.PostConstruct;

import org.nuclos.common.UsageCriteria;
import org.nuclos.common.dal.vo.SystemFields;
import org.nuclos.common.dblayer.JoinType;
import org.nuclos.server.database.SpringDataBaseHelper;
import org.nuclos.server.dblayer.DbTuple;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbJoin;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Singleton class for getting initial states and state models by usage criteria.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:ramin.goettlich@novabit.de">ramin.goettlich</a>
 * @version 00.01.000
 */
@Component
public class StateModelUsagesCache {

	private static StateModelUsagesCache INSTANCE;
	
	// 

	private StateModelUsages stateModelUsages;
	
	private SpringDataBaseHelper dataBaseHelper;

	public static StateModelUsagesCache getInstance() {
		if (INSTANCE.stateModelUsages == null) {
			throw new IllegalStateException("too early");
		}
		return INSTANCE;
	}

	StateModelUsagesCache() {
		INSTANCE = this;
	}
	
	@Autowired
	void setDataBaseHelper(SpringDataBaseHelper dataBaseHelper) {
		this.dataBaseHelper = dataBaseHelper;
	}
	
	@PostConstruct
	final void init() {
		validate();
	}

	/**
	 * @return
	 * @postcondition result != null
	 */
	public synchronized StateModelUsages getStateUsages() {
		if (this.stateModelUsages == null) {
			this.validate();
		}
		final StateModelUsages result = this.stateModelUsages;
		assert result != null;
		return result;
	}

	public synchronized void revalidate() {
		this.invalidate();
		this.validate();
	}

	private synchronized void invalidate() {
		this.stateModelUsages = null;
	}

	private synchronized void validate() {
		this.stateModelUsages = buildCache();
	}

	/**
	 * @return
	 * @postcondition result != null
	 */
	private StateModelUsages buildCache() {
		final StateModelUsages result = new StateModelUsages();

		DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<DbTuple> query = builder.createTupleQuery();
		DbFrom s = query.from("T_MD_STATE").alias("s");
		DbJoin t = s.join("T_MD_STATE_TRANSITION", JoinType.INNER).alias(SystemFields.BASE_ALIAS).on("INTID", "INTID_T_MD_STATE_2", Integer.class);
		DbJoin u = s.join("T_MD_STATEMODELUSAGE",	JoinType.INNER).alias("u").on("INTID_T_MD_STATEMODEL", "INTID_T_MD_STATEMODEL", Integer.class);
		query.multiselect(
		   s.baseColumn("INTID_T_MD_STATEMODEL", Integer.class),
		   s.baseColumn("INTID", Integer.class),
		   u.baseColumn("INTID_T_MD_MODULE", Integer.class),
		   u.baseColumn("INTID_T_MD_PROCESS", Integer.class));
		query.where(t.baseColumn("INTID_T_MD_STATE_1", Integer.class).isNull());
		query.orderBy(builder.asc(u.baseColumn("INTID_T_MD_MODULE", Integer.class)),
		   builder.asc(u.baseColumn("INTID_T_MD_PROCESS", Integer.class)));
		
		for (DbTuple tuple : dataBaseHelper.getDbAccess().executeQuery(query)) {
			final UsageCriteria usagecriteria = new UsageCriteria(
				tuple.get(2, Integer.class), tuple.get(3, Integer.class), null, null);
			result.add(new StateModelUsages.StateModelUsage(
				tuple.get(0, Integer.class), tuple.get(1, Integer.class), usagecriteria));
		}

		assert result != null;
		return result;
	}

}	// class StateModelUsagesCache
