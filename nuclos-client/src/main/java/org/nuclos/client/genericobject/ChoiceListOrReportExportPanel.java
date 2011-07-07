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
package org.nuclos.client.genericobject;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.Border;

import org.nuclos.client.ui.LineLayout;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.UsageCriteria;
import org.nuclos.server.report.NuclosReportException;

/**
 * Panel for choice, whether a search list or the reports of selection shall be exported; for the choosen
 * selection of reports one can select the output format.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:dirk.funke@novabit.de">Dirk Funke</a>
 * @version 01.00.00
 */
public class ChoiceListOrReportExportPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Border border1;
	GridBagLayout gridBagLayout1 = new GridBagLayout();
	JLabel lblHeadline = new JLabel();

	ButtonGroup bgPrechoice = new ButtonGroup();
	JRadioButton rbList = new JRadioButton();
	JRadioButton rbReport = new JRadioButton();

	JPanel pnlPrechoice = new JPanel();
	JPanel pnlPrechoiceHelp = new JPanel(new FlowLayout(FlowLayout.CENTER));
	JPanel pnlSelection = new JPanel(new CardLayout());
	ReportFormatPanel pnlList;
	ReportSelectionPanel pnlReport = new ReportSelectionPanel();

	public ChoiceListOrReportExportPanel(ReportFormatPanel pnlList) {
		setLayout(new BorderLayout());

		this.pnlList = pnlList;
		pnlPrechoice.setLayout(new LineLayout(LineLayout.VERTICAL));
		lblHeadline.setToolTipText("");
		lblHeadline.setText(CommonLocaleDelegate.getMessage("ChoiceListOrReportExportPanel.1", "Bitte Aktion ausw\u00e4hlen:"));
		rbList.setActionCommand("List");
		rbReport.setActionCommand("Report");
		rbList.setText(CommonLocaleDelegate.getMessage("ChoiceListOrReportExportPanel.2", "Suchergebnisliste drucken"));
		rbReport.setText(CommonLocaleDelegate.getMessage("ChoiceListOrReportExportPanel.3", "Formulare f\u00fcr ausgew\u00e4hlte Objekte drucken"));
		rbList.setSelected(true);
		bgPrechoice.add(rbList);
		bgPrechoice.add(rbReport);
		pnlPrechoice.add(lblHeadline);
		pnlPrechoice.add(rbList);
		pnlPrechoice.add(rbReport);

		pnlPrechoiceHelp.add(pnlPrechoice);

		pnlSelection.add(pnlList, "List");
		pnlSelection.add(pnlReport, "Report");

		add(pnlPrechoiceHelp, BorderLayout.NORTH);
		add(pnlSelection, BorderLayout.CENTER);
	}

	public void prepareSelectionPanel(UsageCriteria usagecriteria, int iObjectCount, String sSelectedFormat)
			throws NuclosBusinessException {
		try {
			pnlSelection.remove(pnlReport);
			pnlReport = ReportController.prepareReportSelectionPanel(usagecriteria, iObjectCount);
			pnlSelection.add(pnlReport, "Report");
		}
		catch (RuntimeException ex) {
			throw new NuclosBusinessException(ex);
		}
		catch (NuclosReportException ex) {
			throw new NuclosBusinessException(ex);
		}
	}
}