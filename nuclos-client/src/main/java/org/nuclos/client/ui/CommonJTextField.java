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
package org.nuclos.client.ui;

import java.awt.Dimension;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.ParseException;
import java.util.List;

import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;

/**
 * <code>JTextField</code> which may not be smaller than its preferred size, so all characters
 * are always visible.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */

public class CommonJTextField extends javax.swing.JTextField {
	/**
	 * caches the column width so it needn't be recalculated every time.
	 */
	private int iColumnWidth;

	/**
	 * output format of the text field
	 */
	private Format fOutputFormat;

	/**
	 * the character used to calculate the width needed by one column.
	 */
	private char cColumnWidthChar = 'm';
	
	/**
	 * <code>DocumentFilter</code> to add the autocomplete feature 
	 */
	private AutoCompleterFilter filter;

	public CommonJTextField() {
	}

	public CommonJTextField(int iColumns) {
		super(iColumns);
	}
	
	public void addAutoCompleteItems(List<String> items) {
		if(filter != null)
			filter.setAutoCompleteItems(items);
		else {
			Document doc = getDocument();
			if(doc instanceof AbstractDocument) {
				filter = new AutoCompleterFilter(this);
				filter.setAutoCompleteItems(items);
				((AbstractDocument) doc).setDocumentFilter(filter);
			}
		}
	}
	
	/**
	 * only AbstractDocument has DocumentFilter support
	 */
	@Override
	public void setDocument(Document doc) {
		super.setDocument(doc);
		if(doc instanceof AbstractDocument)
			((AbstractDocument) doc).setDocumentFilter(filter);
	}

	/**
	 * sets the minimum size equal to the preferred size in order to avoid
	 * GridBagLayout flaws.
	 * @return the value of the <code>preferredSize</code> property
	 */
	@Override
	public Dimension getMinimumSize() {
		return this.getPreferredSize();
	}

	/**
	 * sets the character used to calculate the width needed by one column.
	 * @param cColumnWidthChar
	 */
	public void setColumnWidthChar(char cColumnWidthChar) {
		this.cColumnWidthChar = cColumnWidthChar;
	}

	/**
	 * @return the width of one column
	 */
	@Override
	protected int getColumnWidth() {
		if (iColumnWidth == 0) {
			iColumnWidth = getFontMetrics(getFont()).charWidth(cColumnWidthChar);
		}
		return iColumnWidth;
	}

	public void setOutputFormat(Format fOutputFormat) {
		this.fOutputFormat = fOutputFormat;
		addListener();
	}

	/*
	 * add listener to textfield, to format the text of the field,
	 * when ENTER was pressed or the field lost the focus
	 */
	private void addListener() {
		if (isOutputFormatted()) {
			addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					if (!getText().equals(getUnformattedText())) {
						setText(getText());
					}
				}
			});

			addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						if (!getText().equals(getUnformattedText())) {
							setText(getText());
						}
						super.keyPressed(e);
					}
				}
			});
		}
	}

	public Format getOutputFormat() {
		return this.fOutputFormat;
	}

	public boolean isOutputFormatted() {
		if (fOutputFormat != null) {
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public void setText(String sText) {
		super.setText(format(parse(sText)));
	}

	@Override
	public String getText() {
		String sText = super.getText();
		return format(parse(sText));
	}

	public String getUnformattedText() {
		return super.getText();
	}

	/**
	 * formats an object to produce a string
	 * @param obj
	 * @precondition obj != null
	 * @return obj as a String in formatted form
	 */
	public String format(Object obj) {
		if (obj != null) {
			if (isOutputFormatted()) {
				try {
					return fOutputFormat.format(obj);
				}
				catch (IllegalArgumentException e) {
					// throw new CommonFatalException(e);
				}
			}
			return obj.toString();
		}
		else {
			return "";
		}
	}

	/**
	 * parses a text from a string to produce a object (e.g. a Number)
	 * @param sText
	 * @return parsed object
	 */
	public Object parse(String sText) {
		Object obj = sText;
		if (obj != null) {
			if (isOutputFormatted()) {
				try {
					if (fOutputFormat instanceof DecimalFormat) {
						obj = ((DecimalFormat)fOutputFormat).parse(sText);
					}
					/* add additional formats here if necessary */
				}
				catch (ParseException e) {
					//throw new CommonFatalException(e);
				}
			}
		}
		return obj;
	}
	
	/**
	 * NUCLEUSINT-1000
	 * Inserts the clipboard contents into the text.
	 */
	@Override
	public void paste() {
		if (this.isEditable()) {
			Clipboard clipboard = getToolkit().getSystemClipboard();
			try {
				// The MacOS MRJ doesn't convert \r to \n,
				// so do it here
				String selection = ((String) clipboard.getContents(this).getTransferData(DataFlavor.stringFlavor)).replace('\r', '\n');
				if (selection.endsWith("\n")) {
					selection = selection.substring(0, selection.length()-1);
				}	
				//NUCLEUSINT-1139
				replaceSelection(selection);
			}
			catch (Exception e) {
				getToolkit().beep();
				System.err.println("Clipboard does not contain a string");
			}
		}
	}
}  // class CommonJTextField
