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
package org.nuclos.server.report;
	
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.SimpleDoc;
import javax.print.attribute.PrintRequestAttributeSet;

import org.apache.log4j.Logger;

/**
 *
 * @author 
 */
public abstract class NuclosReportPrintJob implements Serializable {

	private final static Logger log = Logger.getLogger(NuclosReportPrintJob.class);
	
	public static synchronized void checkJawin() throws NuclosReportException {
		if (!jawinLoaded) {
			try {
				System.loadLibrary("jawin");
				jawinLoaded = true;
			} catch (SecurityException e) {
				log.error("Error loading jawin", e);
				throw new NuclosReportException("nuclos.jawin.linkerror", e);
			} catch (UnsatisfiedLinkError e) {
				log.error("Error loading jawin", e);
				throw new NuclosReportException("nuclos.jawin.linkerror", e);
			}
		}
	}

	private static volatile boolean jawinLoaded = false;
	
	public void print(PrintService prserv, String sFilename,
			PrintRequestAttributeSet aset) throws PrintException, IOException {
		
		InputStream fis = null;
		try {
			DocPrintJob pj = prserv.createPrintJob();
	        fis = new BufferedInputStream(new FileInputStream(sFilename));
	        pj.print(new SimpleDoc(fis, DocFlavor.INPUT_STREAM.AUTOSENSE, null), (PrintRequestAttributeSet) aset);
		} catch (Exception e) {
			throw new PrintException(e.getMessage());
		}
		finally {
			if (fis != null) {
				fis.close();
			}
		}
	}
}