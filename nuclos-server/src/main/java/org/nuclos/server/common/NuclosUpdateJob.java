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
package org.nuclos.server.common;

import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;
import org.nuclos.common.collection.Pair;
import org.nuclos.server.database.DataBaseHelper;
import org.nuclos.server.dblayer.DbException;
import org.nuclos.server.dblayer.DbStatementUtils;
import org.nuclos.server.dblayer.DbTuple;
import org.nuclos.server.dblayer.expression.DbCurrentDate;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.report.NuclosQuartzJob;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

/**
 * NuclosUpdateJob
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:corina.mandoki@novabit.de">Corina Mandoki</a>
 * @version 01.00.00
 */
public class NuclosUpdateJob extends NuclosQuartzJob{
	
	private static Logger logger = Logger.getLogger("NuclosUpdateJob");		
	
	public NuclosUpdateJob() {
		super(new NuclosUpdateJobImpl());
	}
	
	/**
	 * inner class NuclosUpdateJobImpl: implementation of NuclosUpdateJob
	 */
	private static class NuclosUpdateJobImpl implements Job{
		@Override
		public void execute(JobExecutionContext context) throws JobExecutionException {
			logger.debug("Start executing NuclosUpdateJob");
			
			if(System.getProperty("restricted") == null) {
				//allow only quartz-user to log on server, while the job executes
				System.setProperty("restricted", getUserName());
			}				

			DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
			DbQuery<DbTuple> query = builder.createTupleQuery();
			DbFrom t = query.from("T_AD_UPDATEJOBS").alias("t");
			query.multiselect(
				t.column("INTID", Integer.class),
				t.column("STRJAVACLASSNAME", String.class));
			query.where(t.column("DATEXECUTED", Date.class).isNull());
			query.orderBy(builder.asc(t.column("INTID", Integer.class)), builder.asc(t.column("INTORDER", Integer.class))); 

			ArrayList<Pair<Integer, String>> lstJavaClassNames = new ArrayList<Pair<Integer, String>>();
			try {
				for (DbTuple tuple : DataBaseHelper.getDbAccess().executeQuery(query)) {
					logger.info("Job to execute: " + tuple.get(1, String.class));
					lstJavaClassNames.add(new Pair<Integer, String>(tuple.get(0, Integer.class), tuple.get(1, String.class)));
				}
			} catch (DbException ex) {
				logger.error(ex);
			}
			
			for(Pair<Integer, String> javaClass : lstJavaClassNames) {
				try {
					UpdateJobs job = (UpdateJobs)Class.forName(javaClass.getY()).newInstance();
					boolean isSuccessfulExecuted = job.execute();
					if (isSuccessfulExecuted) {
						DataBaseHelper.execute(DbStatementUtils.updateValues("T_AD_UPDATEJOBS",
							"DATEXECUTED", DbCurrentDate.CURRENT_DATE).where("INTID", javaClass.getX()));
						logger.info("END executing Job: "+javaClass.getY());
					}
				}
				catch (Exception e) {
					logger.error(e);
				}
			}		
			
			deleteJob("NuclosUpdateJob", Scheduler.DEFAULT_GROUP);
			//allow all
			System.clearProperty("restricted");
		}
		
		public void deleteJob(String sJobName, String sJobGroup) {
			final Scheduler scheduler = NuclosScheduler.getInstance().getScheduler();
			try {
				scheduler.deleteJob(sJobName, sJobGroup);
				logger.debug("NuclosUpdateJob deleted");
			}
			catch(SchedulerException ex) {
				logger.error("Failed to delete NuclosUpdateJob ",ex);
			}
			
		}
	}
}