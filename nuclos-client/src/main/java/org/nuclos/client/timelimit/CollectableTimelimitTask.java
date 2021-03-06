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
package org.nuclos.client.timelimit;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.nuclos.common.NuclosEntity;
import org.nuclos.common.collect.collectable.AbstractCollectableBean;
import org.nuclos.common.collect.collectable.AbstractCollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.DefaultCollectableEntityField;
import org.nuclos.common.collection.Transformer;
import org.nuclos.server.common.valueobject.TimelimitTaskVO;

/**
 * <code>CollectableAdapter</code> for <code>TimelimitTaskVO</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Uwe.Allner@novabit.de">Uwe.Allner</a>
 * @version 01.00.00
 */
public class CollectableTimelimitTask extends AbstractCollectableBean<TimelimitTaskVO> {

	public static final String FIELDNAME_DESCRIPTION = "description";
	public static final String FIELDNAME_CREATEDAT = "createdAt";
	public static final String FIELDNAME_CREATEDBY = "createdBy";
	public static final String FIELDNAME_CHANGEDAT = "changedAt";
	public static final String FIELDNAME_CHANGEDBY = "changedBy";
	public static final String FIELDNAME_COMPLETED = "completed";
	public static final String FIELDNAME_EXPIRED = "expired";
	public static final String FIELDNAME_GENERICOBJECTID = "genericObjectId";
	public static final String FIELDNAME_LEASEDOBJECTIDENTIFIERLABEL = "identifier";
	public static final String FIELDNAME_MODULEID = "moduleId";
	public static final String FIELDNAME_STATUS = "status";
	public static final String FIELDNAME_PROCESS = "process";

	/**
	 * inner class <code>CollectableTask.Entity</code>.
	 * Contains meta information about <code>CollectableTask</code>.
	 */
	public static class Entity extends AbstractCollectableEntity {

		private Entity() {
			super(NuclosEntity.TIMELIMITTASK.getEntityName(), "Frist");
			final String entity = NuclosEntity.TIMELIMITTASK.getEntityName();
			
			this.addCollectableEntityField(new DefaultCollectableEntityField(FIELDNAME_DESCRIPTION, String.class, 
					getSpringLocaleDelegate().getMessage("CollectableTimelimitTask.13","Frist"),
					getSpringLocaleDelegate().getMessage("CollectableTimelimitTask.7","Beschreibung der Frist"), 255, null, false, CollectableField.TYPE_VALUEFIELD, null, null, entity, null));
			this.addCollectableEntityField(new DefaultCollectableEntityField(FIELDNAME_COMPLETED, Date.class, 
					getSpringLocaleDelegate().getMessage("CollectableTimelimitTask.9","Erledigt am"),
					getSpringLocaleDelegate().getMessage("CollectableTimelimitTask.2","Abgeschlossen am"), null, null, true, CollectableField.TYPE_VALUEFIELD, null, null, entity, null));
			this.addCollectableEntityField(new DefaultCollectableEntityField(FIELDNAME_EXPIRED, Date.class, 
					getSpringLocaleDelegate().getMessage("CollectableTimelimitTask.1","Abgelaufen am"),
					getSpringLocaleDelegate().getMessage("CollectableTimelimitTask.14","Frist abgelaufen am"), null, null, true, CollectableField.TYPE_VALUEFIELD, null, null, entity, null));
			this.addCollectableEntityField(new DefaultCollectableEntityField(FIELDNAME_CREATEDAT, Date.class, 
					getSpringLocaleDelegate().getMessage("CollectableTimelimitTask.10","Erstellt am"),
					getSpringLocaleDelegate().getMessage("CollectableTimelimitTask.12","Erstellungsdatum"), null, null, false, CollectableField.TYPE_VALUEFIELD, null, null, entity, null));
			this.addCollectableEntityField(new DefaultCollectableEntityField(FIELDNAME_CREATEDBY, String.class, 
					getSpringLocaleDelegate().getMessage("CollectableTimelimitTask.11","Erstellt von"),
					getSpringLocaleDelegate().getMessage("CollectableTimelimitTask.5","Autor der Erstellung"), 255, null, false, CollectableField.TYPE_VALUEFIELD, null, null, entity, null));
			this.addCollectableEntityField(new DefaultCollectableEntityField(FIELDNAME_CHANGEDAT, Date.class, 
					getSpringLocaleDelegate().getMessage("CollectableTimelimitTask.15","Ge\u00e4ndert am"),
					getSpringLocaleDelegate().getMessage("CollectableTimelimitTask.8","Datum der letzten \u00c4nderung"), null, null, false, CollectableField.TYPE_VALUEFIELD, null, null, entity, null));
			this.addCollectableEntityField(new DefaultCollectableEntityField(FIELDNAME_CHANGEDBY, String.class, 
					getSpringLocaleDelegate().getMessage("CollectableTimelimitTask.16","Ge\u00e4ndert von"),
					getSpringLocaleDelegate().getMessage("CollectableTimelimitTask.6","Autor der letzten \u00c4nderung"), 255, null, false, CollectableField.TYPE_VALUEFIELD, null, null, entity, null));
			this.addCollectableEntityField(new DefaultCollectableEntityField(FIELDNAME_GENERICOBJECTID, Integer.class,
					getSpringLocaleDelegate().getMessage("CollectableTimelimitTask.20","Objekt-ID"), 
					getSpringLocaleDelegate().getMessage("CollectableTimelimitTask.17","ID des zugeordneten Objekts"), null, null, 
				true, CollectableField.TYPE_VALUEFIELD, null, null, entity, null));
			this.addCollectableEntityField(new DefaultCollectableEntityField(FIELDNAME_LEASEDOBJECTIDENTIFIERLABEL,
					String.class, 
					getSpringLocaleDelegate().getMessage("CollectableTimelimitTask.24","Zugeordnetes Objekt"), 
					getSpringLocaleDelegate().getMessage("CollectableTimelimitTask.23","System-ID des zugeordneten Objekts"), 255, null, true,
					CollectableField.TYPE_VALUEFIELD, null, null, entity, null));
			this.addCollectableEntityField(new DefaultCollectableEntityField(FIELDNAME_MODULEID, Integer.class, 
					getSpringLocaleDelegate().getMessage("CollectableTimelimitTask.18","Modul-ID"),
					getSpringLocaleDelegate().getMessage("CollectableTimelimitTask.19","Modul-ID des zugeordneten Objekts"), null, null, true, CollectableField.TYPE_VALUEFIELD, null, null, entity, null));

			this.addCollectableEntityField(new DefaultCollectableEntityField(FIELDNAME_STATUS, String.class, 
					getSpringLocaleDelegate().getMessage("CollectableTimelimitTask.21","Status"),
					getSpringLocaleDelegate().getMessage("CollectableTimelimitTask.22","Status des zugeordneten Objekts"), 255, null, false, CollectableField.TYPE_VALUEFIELD, null, null, entity, null));
			this.addCollectableEntityField(new DefaultCollectableEntityField(FIELDNAME_PROCESS, String.class, 
					getSpringLocaleDelegate().getMessage("CollectableTimelimitTask.3","Aktion"),
					getSpringLocaleDelegate().getMessage("CollectableTimelimitTask.4","Aktion des zugeordneten Objekts"), 255, null, false, CollectableField.TYPE_VALUEFIELD, null, null, entity, null));
		}

		/**
		 * @return the names of the fields to display (in the personal task list)
		 */
		public List<String> getNamesOfFieldsToDisplay() {
			return Arrays.asList(FIELDNAME_DESCRIPTION, FIELDNAME_EXPIRED, FIELDNAME_COMPLETED, FIELDNAME_LEASEDOBJECTIDENTIFIERLABEL, FIELDNAME_STATUS, FIELDNAME_PROCESS);
		}
	}

	public static final CollectableTimelimitTask.Entity clcte = new Entity();

	public CollectableTimelimitTask(TimelimitTaskVO taskvo) {
		super(taskvo);
	}

	public TimelimitTaskVO getTimelimitTaskVO() {
		return this.getBean();
	}

	@Override
	protected CollectableEntity getCollectableEntity() {
		return clcte;
	}

	@Override
	public Object getId() {
		return this.getTimelimitTaskVO().getId();
	}

	@Override
	public String getIdentifierLabel() {
		return this.getTimelimitTaskVO().getDescription();
	}

	@Override
	public int getVersion() {
		return this.getTimelimitTaskVO().getVersion();
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("entity=").append(getCollectableEntity());
		result.append(",vo=").append(getBean());
		result.append(",timeLimitTaskVo=").append(getTimelimitTaskVO());
		result.append(",id=").append(getId());
		result.append(",label=").append(getIdentifierLabel());
		result.append(",complete=").append(isComplete());
		result.append("]");
		return result.toString();
	}
	
	public static class MakeCollectable implements Transformer<TimelimitTaskVO, CollectableTimelimitTask> {
		@Override
		public CollectableTimelimitTask transform(TimelimitTaskVO o) {
			return new CollectableTimelimitTask(o);
		}
	}

}	// class CollectableTimelimitTask
