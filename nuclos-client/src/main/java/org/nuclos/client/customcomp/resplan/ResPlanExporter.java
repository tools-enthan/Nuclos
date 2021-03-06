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
package org.nuclos.client.customcomp.resplan;

import java.util.Date;

import org.apache.log4j.Logger;
import org.nuclos.client.customcomp.resplan.ResPlanController.GranularityType;
import org.nuclos.client.ui.resplan.Interval;
import org.nuclos.client.ui.resplan.ResPlanModel;
import org.nuclos.client.ui.resplan.TimeModel;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.dblayer.CollectableNameProducer;

/**
 * A SVG (and more) exporter for {@link ResPlanModel}s.
 * 
 * @author Thomas Pasch
 * @deprecated JResPlanComponent is now directly exported as {@link org.apache.batik.svggen.SVGGraphics2D}.
 */
public class ResPlanExporter extends AbstractResPlanExporter<Collectable,Collectable,Collectable>{
	
	private static final Logger LOG = Logger.getLogger(ResPlanExporter.class);
	
	// 
	
	public ResPlanExporter(ResPlanResourceVO vo, GranularityType granularity, 
			Interval<Date> horizon, ResPlanModel<Collectable, Date, Collectable, Collectable> model, TimeModel<Date> time) {
		super(granularity, horizon, model, time);
		setResourceNameProducer(new CollectableNameProducer(vo.getResourceLabel()));
		setEntryNameProducer(new CollectableNameProducer(vo.getBookingLabel()));
	}

	@Override
	protected int getXPixelForTimeCat() {
		return 60;
	}
	
	@Override
	protected String entryRectClass() {
		return "lane-grey";
	}
	
	@Override
	protected String entryTxtClass() {
		return "bigTxt";
	}
	
	@Override
	protected boolean entryTxtCenter() {
		return false;
	}
	
}
