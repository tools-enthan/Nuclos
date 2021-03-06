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
package org.nuclos.client.report.reportrunner.source;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jawin.DispatchPtr;
import org.jawin.win32.Ole32;
import org.nuclos.client.datasource.DatasourceDelegate;
import org.nuclos.client.report.reportrunner.ReportSource;
import org.nuclos.common.NuclosFile;
import org.nuclos.common2.IOUtils;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.SystemUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.report.NuclosReportException;
import org.nuclos.server.report.valueobject.ReportOutputVO;
import org.nuclos.server.report.valueobject.ResultColumnVO;
import org.nuclos.server.report.valueobject.ResultVO;

public class WordReportSource implements ReportSource {

	private static final Logger log = Logger.getLogger(WordReportSource.class);
	
	private final ReportOutputVO output;
	private final Map<String, Object> params;
	private final Integer maxrows;
	
	public WordReportSource(ReportOutputVO output, Map<String, Object> params, Integer maxrows) {
		super();
		this.output = output;
		this.params = params;
		this.maxrows = maxrows;
	}
	
	@Override
	public NuclosFile getReport() throws NuclosReportException {
		SystemUtils.checkJawin();
		
		try {
			ResultVO resultVO = DatasourceDelegate.getInstance().executeQuery(output.getDatasourceId(), params, maxrows);
			File f = createFile(resultVO);
			return new NuclosFile(f.getName(), IOUtils.readFromBinaryFile(f)); 
		}
		catch (CommonBusinessException e) {
			throw new NuclosReportException(e);
		}
		catch (IOException e) {
			throw new NuclosReportException(e);
		}
	}
	
	private File createFile(ResultVO resultvo) throws NuclosReportException {
		DispatchPtr app = null;
		DispatchPtr documents = null;
		DispatchPtr document = null;
		DispatchPtr fields = null;
		final Map<String, DispatchPtr> mpFields = new HashMap<String, DispatchPtr>();
		new HashMap<String, DispatchPtr>();

		if (StringUtils.isNullOrEmpty(output.getSourceFile())) {
			throw new NuclosReportException(SpringLocaleDelegate.getInstance().getMessage("DOCExport.1", "Word-Datei konnte nicht erstellt werden, da keine Vorlage angegeben wurde."));
		}

		String basefilename = output.getSourceFile().substring(0, output.getSourceFile().lastIndexOf('.'));
		File tempfile;
		try {
			tempfile = File.createTempFile(basefilename, output.getFormat().getExtension());
		}
		catch (IOException e2) {
			throw new NuclosReportException(e2);
		}
		copyTemplateFile(tempfile, output);

		try {
			Ole32.CoInitialize();

			app = new DispatchPtr("Word.Application");

			documents = app.getObject("Documents");
			documents.invoke("Open", tempfile.getAbsolutePath());
			document = app.getObject("ActiveDocument");

			DispatchPtr bookmarks = document.getObject("Bookmarks");

			fields = document.getObject("FormFields");
			Integer iFieldCount = (Integer) fields.get("Count");
			for (int i = 1; i <= iFieldCount; i++) {
				DispatchPtr field = (DispatchPtr) fields.invoke("Item", new Integer(i));
				mpFields.put(field.get("Name").toString().toUpperCase(), field);
			}

			// export data
			for (int i = 0; i < resultvo.getRows().size(); i++) {
			 for (int j = 0; j < resultvo.getColumns().size(); j++) {
					Object value = resultvo.getRows().get(i)[j];
					ResultColumnVO column = resultvo.getColumns().get(j);
					DispatchPtr field = mpFields.get(column.getColumnLabel().toUpperCase());
					if (field == null) {
						field = mpFields.get(column.getColumnLabel().toUpperCase() + "_" + (i + 1));
					}
					if (field != null) {
						DispatchPtr textinput = field.getObject("TextInput");
						if (field != null && value != null) {
							if (value instanceof Date) {
								field.put("Result", SpringLocaleDelegate.getInstance().getDateFormat().format((Date) value));
								textinput.put("Default", SpringLocaleDelegate.getInstance().getDateFormat().format((Date) value));
							} else if (value instanceof Number) {
								field.put("Result", value.toString());
								textinput.put("Default", value.toString());
							} else if (value instanceof Boolean) {
								DispatchPtr checkBox = field.getObject("CheckBox");
								checkBox.put("Value", ((Boolean) value).booleanValue());
								checkBox.close();
							} else if (value instanceof String) {
								DispatchPtr bookmark = null;
								try {
								  bookmark = (DispatchPtr)bookmarks.invoke("Item", field.get("Name").toString());
								} catch(Exception e1) {
									//empty block
									log.warn("createFile: " + e1);
								}
								if(bookmark != null) {
									DispatchPtr bookmarkRange = bookmark.getObject("Range");
									DispatchPtr bookmarkFields = bookmarkRange.getObject("Fields");
									if((Integer)bookmarkFields.get("Count") > 0) {
										DispatchPtr bookmarkField = (DispatchPtr) bookmarkFields.invoke("Item", new Integer(1));
									  DispatchPtr result = bookmarkField.getObject("Result");
										result.put("Text", value);
									}
								} else {
									if(value.toString().length() > 255) {
										value = value.toString().substring(0,254);
									}
									field.put("Result", value);
								}

								textinput.put("Default", value);
							}
						}
						if (textinput != null) {
							textinput.close();
						}
					}
				}
			}

		}
		catch (Throwable e) {
			throw new NuclosReportException(SpringLocaleDelegate.getInstance().getMessage(
				"DOCExport.2", "Die Datei {0} konnte nicht erstellt werden", tempfile.getAbsolutePath()) + ":\n" + e.getMessage(), e);
		}
		finally {
			for (DispatchPtr ptr : mpFields.values()) {
				if (ptr != null) {
					ptr.close();
				}
			}
			if (fields != null) {
				fields.close();
			}

			try {
				document.invoke("Save");
				document.invoke("Close");
				app.invoke("Quit");
				if (document != null) {
					document.close();
				}
				if (documents != null) {
					documents.close();
				}
				if (app != null) {
					app.close();
				}
				Ole32.CoUninitialize();
			}
			catch (Throwable e) {
				throw new NuclosReportException(SpringLocaleDelegate.getInstance().getMessage(
					"DOCExport.2", "Die Datei {0} konnte nicht erstellt werden", tempfile.getAbsolutePath()) + ":\n" + e.getMessage(), e);
			}
		}
		return tempfile;
	}
	
	protected static void copyTemplateFile(File fileDestination, ReportOutputVO outputvo) throws NuclosReportException {
		try {
			if (fileDestination.exists()) {
				fileDestination.delete();
			}
			final String sSourceFile = outputvo.getSourceFile();
			if (sSourceFile != null) {
				final org.nuclos.server.report.ByteArrayCarrier bac = outputvo.getSourceFileContent();
				if (bac == null) {
					throw new NuclosReportException(SpringLocaleDelegate.getInstance().getMessage(
							"AbstractReportExporter.2", "Die Dokumentvorlage \"{0}\" konnte nicht gelesen werden.", sSourceFile));
				}
				IOUtils.writeToBinaryFile(fileDestination, bac.getData());
			}
		}
		catch (IOException ex) {
			throw new NuclosReportException(SpringLocaleDelegate.getInstance().getMessage(
					"AbstractReportExporter.3", "Die Datei \"{0}\" konnte nicht erstellt werden.", fileDestination.getAbsolutePath()) + "\n", ex);
		}
	}
}
