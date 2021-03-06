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

package org.nuclos.client.customcomp.wizard;

import static info.clearthought.layout.TableLayoutConstants.FILL;
import static info.clearthought.layout.TableLayoutConstants.FULL;
import static info.clearthought.layout.TableLayoutConstants.LEFT;
import static info.clearthought.layout.TableLayoutConstants.PREFERRED;
import static info.clearthought.layout.TableLayoutConstants.TOP;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.ItemSelectable;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.tools.Diagnostic.Kind;

import org.apache.commons.lang.ObjectUtils;
import org.jdesktop.swingx.combobox.ListComboBoxModel;
import org.nuclos.client.common.LocaleDelegate;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.customcomp.CustomComponentCache;
import org.nuclos.client.customcomp.CustomComponentDelegate;
import org.nuclos.client.customcomp.resplan.BackgroundPainter;
import org.nuclos.client.customcomp.resplan.CollectableLabelProvider;
import org.nuclos.client.customcomp.resplan.ResPlanConfigVO;
import org.nuclos.client.customcomp.resplan.ResPlanResourceVO;
import org.nuclos.client.customcomp.resplan.ResPlanTranslationTableModel;
import org.nuclos.client.main.Main;
import org.nuclos.client.rule.admin.RuleEditPanel;
import org.nuclos.client.scripting.GroovySupport;
import org.nuclos.client.ui.Bubble;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.model.AbstractListTableModel;
import org.nuclos.client.ui.table.TableUtils;
import org.nuclos.client.ui.util.TableLayoutBuilder;
import org.nuclos.client.wizard.util.NuclosWizardUtils;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common.TranslationVO;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Pair;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.time.LocalTime;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.LocaleInfo;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.customcomp.valueobject.CustomComponentVO;
import org.nuclos.server.ruleengine.NuclosCompileException.ErrorMessage;
import org.pietschy.wizard.InvalidStateException;
import org.pietschy.wizard.PanelWizardStep;
import org.pietschy.wizard.Wizard;
import org.pietschy.wizard.WizardModel;
import org.pietschy.wizard.models.StaticModel;

public class CustomComponentWizardModel extends StaticModel {

	Wizard wizard;
	boolean completed;
	CustomComponentVO componentVO;
	ResPlanConfigVO configVO;
	List<TranslationVO> translations;
	
	CustomComponentWizardModel() {
		add(new CustomComponentWizardStep1());
		add(new CustomComponentWizardStep2());
		add(new CustomComponentWizardStep3());
		add(new CustomComponentWizardStep4());
		add(new CustomComponentWizardStep5());
		add(new CustomComponentWizardStep6());
		setLastAvailable(false);
		setLastVisible(false);
	}
	
	protected SpringLocaleDelegate getSpringLocaleDelegate() {
		return SpringLocaleDelegate.getInstance();
	}

	void setWizard(Wizard wizard) {
		this.wizard = wizard;
	}

	void setCustomComponentVO(CustomComponentVO vo) {
		this.componentVO = vo;
		if (vo.getData() != null) {
			this.configVO = ResPlanConfigVO.fromBytes(vo.getData());
		} else {
			this.configVO = new ResPlanConfigVO();
		}
	}

	void setTranslations(List<TranslationVO> translations) {
		this.translations = translations;
	}

	@Override
	public void refreshModelState() {
		super.refreshModelState();
		if (((CustomComponentWizardAbstractStep) getActiveStep()).isFinishStep()) {
			setPreviousAvailable(false);
			setCancelAvailable(false);
		}
	}

	public void finish() {
		wizard.getCancelAction().setEnabled(false);

		componentVO.setData(configVO.toBytes());
		UIUtils.runShortCommandForTabbedPane(null, new CommonRunnable() {
			@Override
			public void run() throws CommonBusinessException {
				if (componentVO.getId() != null) {
					CustomComponentDelegate.getInstance().modify(componentVO, translations);
				} else {
					CustomComponentDelegate.getInstance().create(componentVO, translations);
				}
			}
		});
	}

	public void cancelWizard() {
		this.wizard.getCancelAction().actionPerformed(null);
	}

	//
	// Steps
	//

	abstract static class CustomComponentWizardAbstractStep extends PanelWizardStep implements DocumentListener {

		CustomComponentWizardModel model;
		
		// former Spring injection
		
		private SpringLocaleDelegate localeDelegate;
		
		// end of former Spring injection

		CustomComponentWizardAbstractStep(String titleResId) {
			super(SpringLocaleDelegate.getInstance().getText(titleResId, null), null);
		}

		CustomComponentWizardAbstractStep(String titleResId, String summaryResId) {
			super(SpringLocaleDelegate.getInstance().getText(titleResId, null), 
					SpringLocaleDelegate.getInstance().getText(summaryResId, null));
			
			setSpringLocaleDelegate(SpringApplicationContextHolder.getBean(SpringLocaleDelegate.class));
		}
		
		final void setSpringLocaleDelegate(SpringLocaleDelegate cld) {
			this.localeDelegate = cld;
		}

		final SpringLocaleDelegate getSpringLocaleDelegate() {
			return localeDelegate;
		}

		@Override
		public void init(WizardModel model) {
			this.model = (CustomComponentWizardModel) model;
		}

		protected boolean isFinishStep() {
			return false;
		}

		protected abstract void updateState();

		@Override
		public void insertUpdate(DocumentEvent e) {
			updateState();
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			updateState();
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			updateState();
		}

		protected void invalidStateLocalized(JComponent comp, String resourceId, Object...args) throws InvalidStateException {
			invalidState(comp, getSpringLocaleDelegate().getMessage(resourceId, null, args));
		}

		protected void invalidState(JComponent comp, String message) throws InvalidStateException {
			Bubble.Position position = Bubble.Position.SE;
			if (comp == null) {
				comp = this;
				position = Bubble.Position.UPPER;
			}
			new Bubble(comp, message, 8, position).setVisible(true);
			throw new InvalidStateException(message, false);
		}
	}

	static class CustomComponentWizardStep1 extends CustomComponentWizardAbstractStep {

		private static final Pattern INTERNAL_NAME_PATTERN = Pattern.compile("[a-zA-Z][a-zA-Z0-9]*");

		JTextField internalNameTextField;
		JComboBox existingComponents;
		JButton removeButton;

		CustomComponentWizardStep1() {
			super("nuclos.resplan.wizard.step1.title", "nuclos.resplan.wizard.step1.summary");
			final SpringLocaleDelegate localeDelegate = getSpringLocaleDelegate();
					
			internalNameTextField = new JTextField(40);
			internalNameTextField.getDocument().addDocumentListener(this);

			existingComponents = new JComboBox();
			existingComponents.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					updateState();
				}
			});
			removeButton = new JButton(new AbstractAction(localeDelegate.getText("nuclos.resplan.wizard.step1.remove", null)) {
				@Override
				public void actionPerformed(ActionEvent e) {
					final String selectedComponent = (String) existingComponents.getSelectedItem();
					if (selectedComponent == null)
						return;
					int opt = JOptionPane.showConfirmDialog(CustomComponentWizardStep1.this, 
							localeDelegate.getText("nuclos.resplan.wizard.step1.remove.check", null));
					if (opt == JOptionPane.OK_OPTION) {
						UIUtils.runShortCommandForTabbedPane(null, new CommonRunnable() {
							@Override
							public void run() throws CommonBusinessException {
								CustomComponentVO vo = CustomComponentCache.getInstance().getByName(selectedComponent);
								CustomComponentDelegate.getInstance().remove(vo);
							};
						});
						model.cancelWizard();
					}
				}
			});

			new TableLayoutBuilder(this)
				.columns(PREFERRED, PREFERRED, FILL).gaps(5, 5)
				.newRow().addLocalizedLabel("nuclos.resplan.wizard.step1.internalName").add(internalNameTextField)
				.newRow().addLocalizedLabel("nuclos.resplan.wizard.step1.existingConfiguration").add(existingComponents)
				.newRow().add(removeButton);
			;
		}

		@Override
		public void prepare() {
			List<String> list = new ArrayList<String>(CustomComponentCache.getInstance().getAllNames());
			list.add(0, null);
			existingComponents.setModel(new ListComboBoxModel<String>(list));
			updateState();
		}

		@Override
		protected void updateState() {
			boolean reconfigure = existingComponents.getSelectedItem() != null;
			removeButton.setVisible(reconfigure);
			internalNameTextField.setEnabled(!reconfigure);
			boolean complete = !internalNameTextField.getText().isEmpty() || reconfigure;
			setComplete(complete);
		}

		@Override
		public void applyState() throws InvalidStateException {
			String selectedComponent = (String) existingComponents.getSelectedItem();
			CustomComponentVO componentVO;
			if (selectedComponent == null) {
				String internalName = internalNameTextField.getText();
				if (!INTERNAL_NAME_PATTERN.matcher(internalName).matches()) {
					invalidStateLocalized(internalNameTextField, "nuclos.resplan.wizard.step1.check.invalidInternalName");
				}
				componentVO = new CustomComponentVO();
				componentVO.setInternalName(internalNameTextField.getText());
				componentVO.setComponentType("org.nuclos.resplan");
				componentVO.setComponentVersion("0.9");
			} else {
				componentVO = CustomComponentCache.getInstance().getByName(selectedComponent).clone();
				if (!"org.nuclos.resplan".equals(componentVO.getComponentType())) {
					invalidState(null, "Only resource components can be reconfigured");
				}
				// Load translations
				try {
					model.setTranslations(CustomComponentDelegate.getInstance().getTranslations(componentVO.getId()));
				} catch (CommonBusinessException e) {
					throw new InvalidStateException(e.getMessage());
				}
			}
			model.setCustomComponentVO(componentVO);
		}
	}

	static class CustomComponentWizardStep2 extends CustomComponentWizardAbstractStep {

		JScrollPane scrollPane;
		JTable translationTable;

		Collection<LocaleInfo> locales;
		CustomComponentTranslationTableModel tablemodel;

		public static String[] labels = TranslationVO.labelsCustomComponent;

		CustomComponentWizardStep2() {
			super("nuclos.resplan.wizard.step2.title", "nuclos.resplan.wizard.step2.summary");

			tablemodel = new CustomComponentTranslationTableModel();
			List<TranslationVO> lstTranslation = new ArrayList<TranslationVO>();

			locales = LocaleDelegate.getInstance().getAllLocales(false);
			for(LocaleInfo voLocale : locales) {
				String sLocaleLabel = voLocale.language;
				Integer iLocaleID = voLocale.localeId;
				String sCountry = voLocale.title;
				Map<String, String> map = new HashMap<String, String>();

				TranslationVO translation = new TranslationVO(iLocaleID, sCountry, sLocaleLabel, map);
				for(String sLabel : labels) {
					translation.getLabels().put(sLabel, "");
				}
				lstTranslation.add(translation);
			}
			tablemodel.setRows(lstTranslation);

			translationTable = new JTable(tablemodel);
			JTextField txtField = new JTextField();
			txtField.addFocusListener(NuclosWizardUtils.createWizardFocusAdapter());
			DefaultCellEditor editor = new DefaultCellEditor(txtField);
			editor.setClickCountToStart(1);

			for(TableColumn col : CollectionUtils.iterableEnum(translationTable.getColumnModel().getColumns())) {
				col.setCellEditor(editor);
			}

			translationTable.getTableHeader().addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					stopCellEditing();
				}
			});

			scrollPane = new JScrollPane(translationTable);

			new TableLayoutBuilder(this).columns(FILL).gaps(5, 5).newRow(FILL).add(scrollPane);
		}

		private void stopCellEditing() {
	        for(TableColumn col : CollectionUtils.iterableEnum(translationTable.getColumnModel().getColumns())) {
	        	TableCellEditor cellEditor = col.getCellEditor();
				if(cellEditor != null)
	        		cellEditor.stopCellEditing();
	        }
		}

		@Override
		public void prepare() {
			if(model.translations != null && model.translations.size() > 0) {
				tablemodel.setRows(model.translations);
			}
			setComplete(true);
		}

		@Override
		protected void updateState() {

		}

		@Override
		public void applyState() throws InvalidStateException {
			stopCellEditing();
			model.translations = tablemodel.getRows();
		}
	}

	static class CustomComponentWizardStep3 extends CustomComponentWizardAbstractStep implements ItemListener, ChangeListener {

		JComboBox resEntityComboBox;
		JComboBox resSortFieldComboBox;
		JComboBox entryEntityComboBox;
		JComboBox referenceFieldComboBox;
		JComboBox milestoneFieldComboBox;

		JComboBox dateFromFieldComboBox;
		JComboBox dateUntilFieldComboBox;

		JCheckBox withTimeCheckBox;
		JComboBox timeFromFieldComboBox;
		JComboBox timeUntilFieldComboBox;
		LocalTimeSpanPane timeSpanPane;
		
		JCheckBox withRelationCheckBox;
		JComboBox relationEntityComboBox;
		JComboBox relationFromFieldComboBox;
		JComboBox relationToFieldComboBox;
		JCheckBox newRelationFromControllerCheckBox;

		CustomComponentWizardStep3() {
			super("nuclos.resplan.wizard.step3.title", "nuclos.resplan.wizard.step3.summary");
			final SpringLocaleDelegate localeDelegate = getSpringLocaleDelegate();

			resEntityComboBox = createJComboBox(30);
			resSortFieldComboBox = createJComboBox(30);
			milestoneFieldComboBox = createJComboBox(30);
			entryEntityComboBox = createJComboBox(30);
			referenceFieldComboBox = createJComboBox(30);

			dateFromFieldComboBox = createJComboBox(20);
			dateUntilFieldComboBox = createJComboBox(20);
			timeFromFieldComboBox = createJComboBox(20);
			timeUntilFieldComboBox = createJComboBox(20);

			withTimeCheckBox = new JCheckBox(localeDelegate.getText("nuclos.resplan.wizard.step3.withTimeSpans", null));
			withTimeCheckBox.addItemListener(this);
			timeSpanPane = new LocalTimeSpanPane();
			timeSpanPane.addChangeListener(this);
			
			withRelationCheckBox = new JCheckBox(localeDelegate.getText("nuclos.resplan.wizard.step3.withRelation", null));
			withRelationCheckBox.addItemListener(this);
			relationEntityComboBox = createJComboBox(30);
			relationFromFieldComboBox = createJComboBox(30);
			relationToFieldComboBox = createJComboBox(30);
			newRelationFromControllerCheckBox = new JCheckBox(localeDelegate.getText("nuclos.resplan.wizard.step3.newRelationFromController", null));

			new TableLayoutBuilder(this)
				.columns(PREFERRED, PREFERRED, 5, PREFERRED, PREFERRED, FILL).gaps(5, 5)
				.newRow().addLocalizedLabel("nuclos.resplan.wizard.step3.resourceEntity").add(resEntityComboBox, 3)
				.newRow().addLocalizedLabel("nuclos.resplan.wizard.step3.resourceSortField").add(resSortFieldComboBox, 3)
				.newRow().addLocalizedLabel("nuclos.resplan.wizard.step3.entryEntity").add(entryEntityComboBox, 3)
				.newRow().addLocalizedLabel("nuclos.resplan.wizard.step3.referenceField").add(referenceFieldComboBox, 3)
				.newRow().addLocalizedLabel("nuclos.resplan.wizard.step3.milestoneField").add(milestoneFieldComboBox, 3)
				.newRow(5)
				.newRow().addLocalizedLabel("nuclos.resplan.wizard.step3.dateFromField").add(dateFromFieldComboBox)
					.skip().addLocalizedLabel("nuclos.resplan.wizard.step3.dateUntilField").add(dateUntilFieldComboBox)
				.newRow(5)
				.newRow().add(withTimeCheckBox)
				.newRow().addLocalizedLabel("nuclos.resplan.wizard.step3.timeFromField").add(timeFromFieldComboBox)
					.skip().addLocalizedLabel("nuclos.resplan.wizard.step3.timeUntilField").add(timeUntilFieldComboBox)
				.newRow(5)
				.newRow().addLocalizedLabel("nuclos.resplan.wizard.step3.timeSpans", "nuclos.resplan.wizard.step3.description", TOP)
					.add(timeSpanPane, 3, LEFT, FULL)
				.newRow(5)
				.newRow().add(withRelationCheckBox)
				.newRow().addLocalizedLabel("nuclos.resplan.wizard.step3.relationEntity").add(relationEntityComboBox, 3)
				.newRow().addLocalizedLabel("nuclos.resplan.wizard.step3.relationFromField").add(relationFromFieldComboBox, 3)
				.newRow().addLocalizedLabel("nuclos.resplan.wizard.step3.relationToField").add(relationToFieldComboBox, 3)
				.newRow().add(newRelationFromControllerCheckBox);
			;
		}

		private JComboBox createJComboBox(int width) {
			JComboBox comboBox = new JComboBox();
			if (width > 0) {
				char ch [] = new char[width]; Arrays.fill(ch, 'x');
				comboBox.setPrototypeDisplayValue(new String(ch));
			}
			comboBox.addItemListener(this);
			return comboBox;
		}

		@Override
		public void prepare() {
			resEntityComboBox.setModel(new ListComboBoxModel<String>(getEntityNames()));
			resEntityComboBox.setSelectedItem(model.configVO.getResourceEntity());

			entryEntityComboBox.setModel(new ListComboBoxModel<String>(getEntityNames()));
			entryEntityComboBox.setSelectedItem(model.configVO.getEntryEntity());

			configureResourceSortFieldComboBox();
			resSortFieldComboBox.setSelectedItem(model.configVO.getResourceSortField());
			
			configureMilestoneFieldComboBox();
			milestoneFieldComboBox.setSelectedItem(model.configVO.getMilestoneField());

			configureReferenceFieldComboBox();
			referenceFieldComboBox.setSelectedItem(model.configVO.getReferenceField());

			withTimeCheckBox.setSelected(!StringUtils.looksEmpty(model.configVO.getTimePeriodsString()));
			configureDateTimeComboBoxes(false);
			dateFromFieldComboBox.setSelectedItem(model.configVO.getDateFromField());
			dateUntilFieldComboBox.setSelectedItem(model.configVO.getDateUntilField());
			timeFromFieldComboBox.setSelectedItem(model.configVO.getTimeFromField());
			timeUntilFieldComboBox.setSelectedItem(model.configVO.getTimeUntilField());
			timeSpanPane.setText(model.configVO.getTimePeriodsString());
			
			withRelationCheckBox.setSelected(!StringUtils.looksEmpty(model.configVO.getRelationEntity()));
			configureRelationEntityComboBox();
			configureRelationFieldComboBoxes();
			relationEntityComboBox.setSelectedItem(model.configVO.getRelationEntity());
			relationFromFieldComboBox.setSelectedItem(model.configVO.getRelationFromField());
			relationToFieldComboBox.setSelectedItem(model.configVO.getRelationToField());
			newRelationFromControllerCheckBox.setSelected(model.configVO.isNewRelationFromController());

			updateState();
		}

		private void configureResourceSortFieldComboBox() {
			final String resEntity = (String) resEntityComboBox.getSelectedItem();
			List<String> fieldNames = new ArrayList<String>();
			if (resEntity != null) {
				for (EntityFieldMetaDataVO field : MetaDataClientProvider.getInstance().getAllEntityFieldsByEntity(resEntity).values()) {
					fieldNames.add(field.getField());
				}
			}
			Collections.sort(fieldNames);
			fieldNames.add(0, null);
			resSortFieldComboBox.setModel(new ListComboBoxModel<String>(fieldNames));
		}
		
		private void configureMilestoneFieldComboBox() {
			final String entryEntity = (String) entryEntityComboBox.getSelectedItem();
			List<String> fieldNames = new ArrayList<String>();
			if (entryEntity != null) {
				for (EntityFieldMetaDataVO field : MetaDataClientProvider.getInstance().getAllEntityFieldsByEntity(entryEntity).values()) {
					if (Boolean.class.getName().equals(field.getDataType())) {
						fieldNames.add(field.getField());
					}
				}
			}
			Collections.sort(fieldNames);
			fieldNames.add(0, null);
			milestoneFieldComboBox.setModel(new ListComboBoxModel<String>(fieldNames));
		}

		private void configureReferenceFieldComboBox() {
			final String resEntity = (String) resEntityComboBox.getSelectedItem();
			final String entryEntity = (String) entryEntityComboBox.getSelectedItem();
			List<String> refFieldNames = new ArrayList<String>();
			if (resEntity != null && entryEntity != null) {
				for (EntityFieldMetaDataVO field : MetaDataClientProvider.getInstance().getAllEntityFieldsByEntity(entryEntity).values()) {
					if (resEntity.equals(field.getForeignEntity()))
						refFieldNames.add(field.getField());
				}
			}
			Collections.sort(refFieldNames);
			referenceFieldComboBox.setModel(new ListComboBoxModel<String>(refFieldNames));
		}

		private void configureDateTimeComboBoxes(boolean timeOnly) {
			boolean withTime = withTimeCheckBox.isSelected();
			final String entryEntity = (String) entryEntityComboBox.getSelectedItem();
			List<String> dateFields = new ArrayList<String>();
			List<String> timeFields = new ArrayList<String>();
			if (entryEntity != null || entryEntity != null) {
				for (EntityFieldMetaDataVO field : MetaDataClientProvider.getInstance().getAllEntityFieldsByEntity(entryEntity).values()) {
					if (field.getForeignEntity() != null)
						continue;
					if ("java.util.Date".equals(field.getDataType())) {
						dateFields.add(field.getField());
					}
					if (withTime && "java.lang.String".equals(field.getDataType()) && field.getId() > 0) {
						timeFields.add(field.getField());
					}
				}
				Collections.sort(dateFields);
				Collections.sort(timeFields);
			}
			dateFields.add(0, null);
			timeFields.add(0, null);
			if (!timeOnly) {
				dateFromFieldComboBox.setModel(new ListComboBoxModel<String>(dateFields));
				dateUntilFieldComboBox.setModel(new ListComboBoxModel<String>(dateFields));
			}
			timeFromFieldComboBox.setModel(new ListComboBoxModel<String>(timeFields));
			timeUntilFieldComboBox.setModel(new ListComboBoxModel<String>(timeFields));

			timeFromFieldComboBox.setEnabled(withTime);
			timeUntilFieldComboBox.setEnabled(withTime);
			timeSpanPane.setEnabled(withTime);
			if (!withTime) {
				timeSpanPane.setText("");
			}
		}
		
		private void configureRelationEntityComboBox() {
			final boolean withRelation = withRelationCheckBox.isSelected();
			final String entryEntity = (String) entryEntityComboBox.getSelectedItem();
			List<String> relationEntities = new ArrayList<String>();
			relationEntities.add(null);
			if (withRelation) {
				relationEntities.addAll(getRelationEntityNames(entryEntity));
			}
			relationEntityComboBox.setModel(new ListComboBoxModel<String>(relationEntities));
			relationEntityComboBox.setEnabled(withRelation);
			newRelationFromControllerCheckBox.setEnabled(withRelation);
		}
		
		private void configureRelationFieldComboBoxes() {
			final boolean withRelation = withRelationCheckBox.isSelected();
			final String entryEntity = (String) entryEntityComboBox.getSelectedItem();
			final String relEntity = (String) relationEntityComboBox.getSelectedItem();
			List<String> fieldNames = new ArrayList<String>();
			if (withRelation) {
				if (relEntity != null) {
					for (EntityFieldMetaDataVO field : MetaDataClientProvider.getInstance().getAllEntityFieldsByEntity(relEntity).values()) {
						if (LangUtils.equals(entryEntity, field.getForeignEntity())) {
							fieldNames.add(field.getField());
						}
					}
				}
				Collections.sort(fieldNames);
			}
			fieldNames.add(0, null);
			relationFromFieldComboBox.setModel(new ListComboBoxModel<String>(fieldNames));
			relationToFieldComboBox.setModel(new ListComboBoxModel<String>(fieldNames));
			relationFromFieldComboBox.setEnabled(withRelation);
			relationToFieldComboBox.setEnabled(withRelation);
		}
		
		private void updateRelationFromFieldComboBox() {
			final String relToField = (String) relationToFieldComboBox.getSelectedItem();
			final String relFromField = (String) relationFromFieldComboBox.getSelectedItem();
			if (relToField == null && relFromField == null) {
				if (relationFromFieldComboBox.getItemCount() > 1) {
					relationFromFieldComboBox.setSelectedIndex(1);
				}
			} else if (LangUtils.equals(relToField, relFromField)) {
				relationFromFieldComboBox.setSelectedIndex(0);
			} else if (relFromField == null) {
				for (int i = 1; i < relationFromFieldComboBox.getModel().getSize(); i++) {
					if (!LangUtils.equals(relToField, relationFromFieldComboBox.getModel().getElementAt(i))) {
						relationFromFieldComboBox.setSelectedIndex(i);
					}
				}
			}
		}
		
		private void updateRelationToFieldComboBox() {
			final String relToField = (String) relationToFieldComboBox.getSelectedItem();
			final String relFromField = (String) relationFromFieldComboBox.getSelectedItem();
			if (LangUtils.equals(relToField, relFromField)) {
				relationToFieldComboBox.setSelectedIndex(0);
			} else if (relToField == null) {
				for (int i = 1; i < relationToFieldComboBox.getModel().getSize(); i++) {
					if (!LangUtils.equals(relFromField, relationToFieldComboBox.getModel().getElementAt(i))) {
						relationToFieldComboBox.setSelectedIndex(i);
					}
				}
			}
		}

		List<String> getEntityNames() {
			List<String> entityNames = new ArrayList<String>();
			for (EntityMetaDataVO entity : MetaDataClientProvider.getInstance().getAllEntities()) {
				if (entity.getEntity().startsWith("nuclos_") || entity.isDynamic())
					continue;
				entityNames.add(entity.getEntity());
			}
			Collections.sort(entityNames);
			return entityNames;
		}
		
		List<String> getRelationEntityNames(String entryEntity) {
			List<String> entityNames = new ArrayList<String>();
				if (entryEntity != null) {
				for (EntityMetaDataVO entity : MetaDataClientProvider.getInstance().getAllEntities()) {
					if (entity.getEntity().startsWith("nuclos_"))
						continue;
					
					int countFieldsToResource = 0;
					for (EntityFieldMetaDataVO efMeta : MetaDataClientProvider.getInstance().getAllEntityFieldsByEntity(entity.getEntity()).values()) {
						if (LangUtils.equals(entryEntity, efMeta.getForeignEntity())) {
							countFieldsToResource++;
						}
					}
					
					if (countFieldsToResource >= 2) {
						entityNames.add(entity.getEntity());
					}
				}
				Collections.sort(entityNames);
			}
			return entityNames;
		}

		@Override
		public void itemStateChanged(ItemEvent e) {
			ItemSelectable source = e.getItemSelectable();
			if (source == resEntityComboBox) {
				configureResourceSortFieldComboBox();
				configureRelationEntityComboBox();
				configureRelationFieldComboBoxes();
			}
			if (source == resEntityComboBox || source == entryEntityComboBox) {
				configureReferenceFieldComboBox();
				configureMilestoneFieldComboBox();
			}
			if (source == entryEntityComboBox || source == withTimeCheckBox) {
				configureDateTimeComboBoxes(source == withTimeCheckBox);
			}
			if (source == withRelationCheckBox) {
				configureRelationEntityComboBox();
				configureRelationFieldComboBoxes();
			}
			if (source == relationEntityComboBox) {
				configureRelationFieldComboBoxes();
				updateRelationFromFieldComboBox();
				updateRelationToFieldComboBox();
			}
			if (source == relationFromFieldComboBox) {
				updateRelationToFieldComboBox();
			}
			if (source == relationToFieldComboBox) {
				updateRelationFromFieldComboBox();
			}
			updateState();
		}

		@Override
		public void stateChanged(ChangeEvent e) {
			updateState();
		}

		@Override
		protected void updateState() {
			boolean complete = resEntityComboBox.getSelectedItem() != null
					&& entryEntityComboBox.getSelectedItem() != null
					&& referenceFieldComboBox.getSelectedItem() != null
					&& dateFromFieldComboBox.getSelectedItem() != null
					&& dateUntilFieldComboBox.getSelectedItem() != null;
			if (withTimeCheckBox.isSelected()) {
				complete &= !timeSpanPane.getText().isEmpty()
					&& timeFromFieldComboBox.getSelectedItem() != null
					&& timeUntilFieldComboBox.getSelectedItem() != null;
			if (withRelationCheckBox.isSelected()) {
				complete &= relationEntityComboBox.getSelectedItem() != null
					&& relationFromFieldComboBox.getSelectedItem() != null
					&& relationToFieldComboBox.getSelectedItem() != null;
			}
			}
			setComplete(complete);
		}

		@Override
		public void applyState() throws InvalidStateException {
			if (LangUtils.equals(dateFromFieldComboBox.getSelectedItem(), dateUntilFieldComboBox.getSelectedItem()))
				invalidStateLocalized(dateUntilFieldComboBox, "nuclos.resplan.wizard.step3.check.differentDateFiels");

			if (withTimeCheckBox.isSelected()) {
				if (LangUtils.equals(timeFromFieldComboBox.getSelectedItem(), timeUntilFieldComboBox.getSelectedItem())) {
					invalidStateLocalized(dateUntilFieldComboBox, "nuclos.resplan.wizard.step3.check.differentTimeFiels");
				}
				LocalTime startTime = null;
				LocalTime time = null;
				boolean dayNightTransition = false;
				for (Pair<LocalTime, LocalTime> p : timeSpanPane.getLocalTimeSpans()) {
					if (startTime == null) {
						startTime = p.x;
					}
					int xToY = p.x.compareTo(p.y);
					if (xToY == 0) {
						invalidStateLocalized(timeSpanPane, "nuclos.resplan.wizard.step3.check.emptyTimespan", p.x, p.y);
					}
					boolean valid = (xToY <= 0);
					if (!valid && !dayNightTransition && p.y.compareTo(startTime) <= 0) {
						dayNightTransition = true;
						valid = true;
					}
					if (time != null && time.compareTo(p.x) > 0) {
						valid = false;
					}
					if (!valid) {
						invalidStateLocalized(timeSpanPane, "nuclos.resplan.wizard.step3.check.invalidTimespan", p.x, p.y);
					}
					time = p.y;
				}
			}

			model.configVO.setResourceEntity((String) resEntityComboBox.getSelectedItem());
			model.configVO.setResourceSortField((String) resSortFieldComboBox.getSelectedItem());
			model.configVO.setEntryEntity((String) entryEntityComboBox.getSelectedItem());
			model.configVO.setReferenceField((String) referenceFieldComboBox.getSelectedItem());
			model.configVO.setMilestoneField((String) milestoneFieldComboBox.getSelectedItem());

			model.configVO.setDateFromField((String) dateFromFieldComboBox.getSelectedItem());
			model.configVO.setDateUntilField((String) dateUntilFieldComboBox.getSelectedItem());
			if (withTimeCheckBox.isSelected()) {
				model.configVO.setTimeFromField((String) timeFromFieldComboBox.getSelectedItem());
				model.configVO.setTimeUntilField((String) timeUntilFieldComboBox.getSelectedItem());
				model.configVO.setTimePeriodsString(timeSpanPane.getText());
			} else {
				model.configVO.setTimePeriodsString(null);
				model.configVO.setTimeFromField(null);
				model.configVO.setTimeUntilField(null);
			}
			
			if (withRelationCheckBox.isSelected()) {
				model.configVO.setRelationEntity((String) relationEntityComboBox.getSelectedItem());
				model.configVO.setRelationFromField((String) relationFromFieldComboBox.getSelectedItem());
				model.configVO.setRelationToField((String) relationToFieldComboBox.getSelectedItem());
				model.configVO.setNewRelationFromController(newRelationFromControllerCheckBox.isSelected());
			} else {
				model.configVO.setRelationEntity(null);
				model.configVO.setRelationFromField(null);
				model.configVO.setRelationToField(null);
				model.configVO.setNewRelationFromController(false);
			}
		}
	}

	static class CustomComponentWizardStep4 extends CustomComponentWizardAbstractStep {

		JScrollPane scrollPane;
		JTable translationTable;
		JTextField tfDefaultViewFrom;
		JTextField tfDefaultViewUntil;

		Collection<LocaleInfo> locales;
		ResPlanTranslationTableModel tablemodel;
		
		JRadioButton relationPresentationOrthogonal;
		JRadioButton relationPresentationStraight;
		
		JRadioButton relationFromPlain;
		JRadioButton relationFromDot;
		JRadioButton relationFromArrow;
		
		JRadioButton relationToPlain;
		JRadioButton relationToDot;
		JRadioButton relationToArrow;

		CustomComponentWizardStep4() {
			super("nuclos.resplan.wizard.step4.title", "nuclos.resplan.wizard.step4.summary");
			final SpringLocaleDelegate localeDelegate = getSpringLocaleDelegate();

			locales = LocaleDelegate.getInstance().getAllLocales(false);
			tablemodel = new ResPlanTranslationTableModel(locales);

			translationTable = new JTable(tablemodel);
			TableCellEditor editor = new ResourceCellEditor();

			for(TableColumn col : CollectionUtils.iterableEnum(translationTable.getColumnModel().getColumns())) {
				col.setCellEditor(editor);
			}

			translationTable.getTableHeader().addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					stopCellEditing();
				}
			});

			translationTable.setRowHeight(50);

			scrollPane = new JScrollPane(translationTable);
			
			tfDefaultViewFrom = new JTextField(10);
			tfDefaultViewUntil = new JTextField(10);
			
			relationPresentationOrthogonal = new JRadioButton(localeDelegate.getText("nuclos.resplan.wizard.step4.relationPresentationOrthogonal"));
			relationPresentationStraight = new JRadioButton(localeDelegate.getText("nuclos.resplan.wizard.step4.relationPresentationStraight"));
			ButtonGroup bgRelationPresentation = new ButtonGroup();
			bgRelationPresentation.add(relationPresentationOrthogonal);
			bgRelationPresentation.add(relationPresentationStraight);
			
			relationFromPlain = new JRadioButton(localeDelegate.getText("nuclos.resplan.wizard.step4.relationEndpointPlain"));
			relationFromDot = new JRadioButton(localeDelegate.getText("nuclos.resplan.wizard.step4.relationEndpointDot"));
			relationFromArrow = new JRadioButton(localeDelegate.getText("nuclos.resplan.wizard.step4.relationEndpointArrow"));
			ButtonGroup bgRelationFrom = new ButtonGroup();
			bgRelationFrom.add(relationFromPlain);
			bgRelationFrom.add(relationFromDot);
			bgRelationFrom.add(relationFromArrow);
			
			relationToPlain = new JRadioButton(localeDelegate.getText("nuclos.resplan.wizard.step4.relationEndpointPlain"));
			relationToDot = new JRadioButton(localeDelegate.getText("nuclos.resplan.wizard.step4.relationEndpointDot"));
			relationToArrow = new JRadioButton(localeDelegate.getText("nuclos.resplan.wizard.step4.relationEndpointArrow"));
			ButtonGroup bgRelationTo = new ButtonGroup();
			bgRelationTo.add(relationToPlain);
			bgRelationTo.add(relationToDot);
			bgRelationTo.add(relationToArrow);

			TableLayoutBuilder tlb = new TableLayoutBuilder(this).columns(PREFERRED, PREFERRED, PREFERRED, FILL).gaps(5, 5);
			tlb.newRow(PREFERRED).
				addLabel(localeDelegate.getMessage("nuclos.resplan.wizard.step4.defaultViewFrom", "Standard Zeitraum von (HEUTE")).
				add(tfDefaultViewFrom).
				add(new JLabel(localeDelegate.getMessage("nuclos.resplan.wizard.step4.defaultViewFrom2", ")"), 2));
			tlb.newRow(PREFERRED).
				addLabel(localeDelegate.getMessage("nuclos.resplan.wizard.step4.defaultViewUntil", "Standard Zeitraum bis (HEUTE")).
				add(tfDefaultViewUntil).
				add(new JLabel(localeDelegate.getMessage("nuclos.resplan.wizard.step4.defaultViewUntil2", ")"), 2));
			tlb.newRow(PREFERRED).add(new JLabel("          " + localeDelegate.getMessage("nuclos.resplan.wizard.step4.defaultView", "t=Tag,w=Woche,m=Monat,j=Jahr")), 4);
			tlb.newRow(10).add(new JSeparator(), 4);
			tlb.newRow(FILL).add(scrollPane, 4);
			
			JPanel jpnRelation = new JPanel();
			TableLayoutBuilder tbllayRelation = new TableLayoutBuilder(jpnRelation).columns(PREFERRED, PREFERRED, PREFERRED, FILL).gaps(5, 5);
			tbllayRelation.newRow();
			
			JPanel jpnRelationFrom = new JPanel();
			jpnRelationFrom.setBorder(BorderFactory.createTitledBorder(localeDelegate.getText("nuclos.resplan.wizard.step4.relationFrom")));
			TableLayoutBuilder tbllayRelationFrom = new TableLayoutBuilder(jpnRelationFrom).columns(PREFERRED, PREFERRED);
			tbllayRelationFrom.newRow().add(relationFromPlain);
			tbllayRelationFrom.newRow().add(relationFromDot).add(new JLabel(new ImageIcon(getClass().getResource("/org/nuclos/client/relation/images/oval_start.gif"))));
			tbllayRelationFrom.newRow().add(relationFromArrow).add(new JLabel(new ImageIcon(getClass().getResource("/org/nuclos/client/relation/images/classic_start.gif"))));
			tbllayRelation.add(jpnRelationFrom);
			
			JPanel jpnRelationPresentation = new JPanel();
			jpnRelationPresentation.setBorder(BorderFactory.createTitledBorder(localeDelegate.getText("nuclos.resplan.wizard.step4.relationPresentation")));
			TableLayoutBuilder tbllayRelationPresentation = new TableLayoutBuilder(jpnRelationPresentation).columns(PREFERRED, PREFERRED);
			tbllayRelationPresentation.newRow().add(relationPresentationStraight).add(new JLabel(new ImageIcon(getClass().getResource("/org/nuclos/client/relation/images/straight.png"))));
			tbllayRelationPresentation.newRow().add(relationPresentationOrthogonal).add(new JLabel(new ImageIcon(getClass().getResource("/org/nuclos/client/relation/images/vertical.png"))));
			tbllayRelation.add(jpnRelationPresentation);
			
			JPanel jpnRelationTo = new JPanel();
			jpnRelationTo.setBorder(BorderFactory.createTitledBorder(localeDelegate.getText("nuclos.resplan.wizard.step4.relationTo")));
			TableLayoutBuilder tbllayRelationTo = new TableLayoutBuilder(jpnRelationTo).columns(PREFERRED, PREFERRED);
			tbllayRelationTo.newRow().add(relationToPlain);
			tbllayRelationTo.newRow().add(relationToDot).add(new JLabel(new ImageIcon(getClass().getResource("/org/nuclos/client/relation/images/oval_end.gif"))));
			tbllayRelationTo.newRow().add(relationToArrow).add(new JLabel(new ImageIcon(getClass().getResource("/org/nuclos/client/relation/images/classic_end.gif"))));
			tbllayRelation.add(jpnRelationTo);
			
			tlb.newRow().addLocalizedLabel("nuclos.resplan.wizard.step4.relation", TOP).addFullSpan(jpnRelation);
		}

		private void stopCellEditing() {
	        for(TableColumn col : CollectionUtils.iterableEnum(translationTable.getColumnModel().getColumns())) {
	        	TableCellEditor cellEditor = col.getCellEditor();
				if(cellEditor != null) {
	        		cellEditor.stopCellEditing();
				}
	        }
		}

		@Override
		public void prepare() {
			List<ResPlanResourceVO> resources = new ArrayList<ResPlanResourceVO>();
			for (LocaleInfo locale : locales) {
				boolean found = false;
				if (model.configVO != null && model.configVO.getResources() != null) {
					for (ResPlanResourceVO vo : model.configVO.getResources()) {
						if (locale.localeId.equals(vo.getLocaleId())) {
							found = true;
							resources.add(vo);
						}
					}
				}
				if (!found) {
					ResPlanResourceVO vo = new ResPlanResourceVO();
					vo.setLocaleId(locale.localeId);
					resources.add(vo);
				}
			}
			tablemodel.setRows(resources);
			
			tfDefaultViewFrom.setText(model.configVO.getDefaultViewFrom());
			tfDefaultViewUntil.setText(model.configVO.getDefaultViewUntil());
			
			switch (model.configVO.getRelationPresentation()) {
				case ResPlanConfigVO.RELATION_PRESENTATION_STRAIGHT: relationPresentationStraight.setSelected(true); break;
				default: relationPresentationOrthogonal.setSelected(true);
			}
			
			switch (model.configVO.getRelationFromPresentation()) {
				case ResPlanConfigVO.RELATION_ENDPOINT_PRESENTATION_PLAIN: relationFromPlain.setSelected(true); break;
				case ResPlanConfigVO.RELATION_ENDPOINT_PRESENTATION_ARROW: relationFromArrow.setSelected(true); break;
				default: relationFromDot.setSelected(true);
			}
			
			switch (model.configVO.getRelationToPresentation()) {
				case ResPlanConfigVO.RELATION_ENDPOINT_PRESENTATION_PLAIN: relationToPlain.setSelected(true); break;
				case ResPlanConfigVO.RELATION_ENDPOINT_PRESENTATION_DOT: relationToDot.setSelected(true); break;
				default: relationToArrow.setSelected(true);
			}

			updateState();
		}

		@Override
		protected void updateState() {
			final boolean withRelation = !StringUtils.looksEmpty(model.configVO.getRelationEntity());
			relationPresentationOrthogonal.setEnabled(withRelation);
			relationPresentationStraight.setEnabled(withRelation);
			relationFromPlain.setEnabled(withRelation);
			relationFromDot.setEnabled(withRelation);
			relationFromArrow.setEnabled(withRelation);
			relationToPlain.setEnabled(withRelation);
			relationToDot.setEnabled(withRelation);
			relationToArrow.setEnabled(withRelation);
			setComplete(true);
		}

		@Override
		public void applyState() throws InvalidStateException {
			stopCellEditing();
			model.configVO.setResources(tablemodel.getRows());
			model.configVO.setDefaultViewFrom(tfDefaultViewFrom.getText());
			model.configVO.setDefaultViewUntil(tfDefaultViewUntil.getText());
			
			if (relationPresentationStraight.isSelected()) {
				model.configVO.setRelationPresentation(ResPlanConfigVO.RELATION_PRESENTATION_STRAIGHT);
			} else if (relationPresentationOrthogonal.isSelected()) {
				model.configVO.setRelationPresentation(ResPlanConfigVO.RELATION_PRESENTATION_ORTHOGONAL);
			} 
			
			if (relationFromPlain.isSelected()) {
				model.configVO.setRelationFromPresentation(ResPlanConfigVO.RELATION_ENDPOINT_PRESENTATION_PLAIN);
			} else if (relationFromDot.isSelected()) {
				model.configVO.setRelationFromPresentation(ResPlanConfigVO.RELATION_ENDPOINT_PRESENTATION_DOT);
			} else if (relationFromArrow.isSelected()) {
				model.configVO.setRelationFromPresentation(ResPlanConfigVO.RELATION_ENDPOINT_PRESENTATION_ARROW);
			}
			
			if (relationToPlain.isSelected()) {
				model.configVO.setRelationToPresentation(ResPlanConfigVO.RELATION_ENDPOINT_PRESENTATION_PLAIN);
			} else if (relationToDot.isSelected()) {
				model.configVO.setRelationToPresentation(ResPlanConfigVO.RELATION_ENDPOINT_PRESENTATION_DOT);
			} else if (relationToArrow.isSelected()) {
				model.configVO.setRelationToPresentation(ResPlanConfigVO.RELATION_ENDPOINT_PRESENTATION_ARROW);
			}
		}
	}

	static class CustomComponentWizardStep5 extends CustomComponentWizardAbstractStep implements ItemListener {

		JCheckBox scriptActiveCheckBox;
		JButton editCodeButton;
		JLabel codeStateLabel;

		CustomComponentCodeEditor codeEditor;
		JComboBox backgroundPaintMethod;
		JComboBox resourceCellMethod;
		JComboBox entryCellMethod;

		CustomComponentWizardStep5() {
			super("nuclos.resplan.wizard.step5.title", "nuclos.resplan.wizard.step5.summary");
			final SpringLocaleDelegate localeDelegate = getSpringLocaleDelegate();

			codeEditor = new CustomComponentCodeEditor();

			scriptActiveCheckBox = new JCheckBox(localeDelegate.getText("nuclos.resplan.wizard.step5.scriptingActivated", null));
			scriptActiveCheckBox.addItemListener(this);
			codeStateLabel = new JLabel("");
			editCodeButton = new JButton("Skriptcode editieren");
			editCodeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					codeEditor.run();
					codeEditor.compile();
					updateState();
				}
			});

			backgroundPaintMethod = new JComboBox();
			backgroundPaintMethod.setEditable(true);
			backgroundPaintMethod.addItemListener(this);

			resourceCellMethod = new JComboBox();
			resourceCellMethod.setEditable(true);
			resourceCellMethod.addItemListener(this);

			entryCellMethod = new JComboBox();
			entryCellMethod.setEditable(true);
			entryCellMethod.addItemListener(this);

			new TableLayoutBuilder(this)
				.columns(PREFERRED, PREFERRED, FILL).gaps(5, 5)
				.newRow().add(scriptActiveCheckBox, 2)
				.newRow(5)
				.newRow().add(editCodeButton)
				.newRow().addFullSpan(codeStateLabel)
				.newRow(5)
				.newRow().addLocalizedLabel("nuclos.resplan.wizard.step5.ruleFormatBackground", "nuclos.resplan.wizard.step5.ruleFormatBackground.toolTip")
					.add(backgroundPaintMethod, 2)
				.newRow().addLocalizedLabel("nuclos.resplan.wizard.step5.ruleFormatResource", "nuclos.resplan.wizard.step5.ruleFormatResource.toolTip")
					.add(resourceCellMethod, 2)
				.newRow().addLocalizedLabel("nuclos.resplan.wizard.step5.ruleFormatEntry", "nuclos.resplan.wizard.step5.ruleFormatEntryd.toolTip")
					.add(entryCellMethod, 2);
		}

		@Override
		public void prepare() {
			codeEditor.setCode(StringUtils.emptyIfNull(model.configVO.getScriptingCode()));
			scriptActiveCheckBox.setSelected(model.configVO.isScriptingActivated());
			backgroundPaintMethod.setSelectedItem(model.configVO.getScriptingBackgroundPaintMethod());
			resourceCellMethod.setSelectedItem(model.configVO.getScriptingResourceCellMethod());
			entryCellMethod.setSelectedItem(model.configVO.getScriptingEntryCellMethod());
			updateState();
		}

		@Override
		public void applyState() throws InvalidStateException {
			boolean withScripting = scriptActiveCheckBox.isSelected();
			model.configVO.setScriptingActivated(withScripting);
			if (withScripting && !codeEditor.getSupport().isCompiled()) {
				invalidStateLocalized(scriptActiveCheckBox, "nuclos.resplan.wizard.step5.scriptError");
			}
			model.configVO.setScriptingCode(StringUtils.nullIfEmpty(codeEditor.getCode()));
			model.configVO.setScriptingBackgroundPaintMethod(getAndCheckMethod(backgroundPaintMethod, withScripting, BackgroundPainter.SCRIPTING_SIGNATURE));
			model.configVO.setScriptingResourceCellMethod(getAndCheckMethod(resourceCellMethod, withScripting, CollectableLabelProvider.SCRIPTING_SIGNATURE));
			model.configVO.setScriptingEntryCellMethod(getAndCheckMethod(entryCellMethod, withScripting, CollectableLabelProvider.SCRIPTING_SIGNATURE));
		}

		private String getAndCheckMethod(JComboBox comboBox, boolean check, Class<?>... argumentTypes) throws InvalidStateException {
			String methodName = StringUtils.nullIfEmpty((String) comboBox.getSelectedItem());
//			if (methodName != null && check) {
//				GroovySupport support = codeEditor.getSupport();
//				List<String> supportedMethods = support.findMethodNames(argumentTypes);
//				if (!supportedMethods.contains(methodName)) {
//					if (support.methodExists(methodName)) {
//						invalidStateLocalized(scriptActiveCheckBox, "nuclos.resplan.wizard.step5.scriptMethodIncompatible", methodName);
//					} else {
//						invalidStateLocalized(scriptActiveCheckBox, "nuclos.resplan.wizard.step5.scriptMethodNotFound", methodName);
//					}
//				}
//			}
			return methodName;
		}

		@Override
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.SELECTED)
				updateState();
		}

		@Override
		protected void updateState() {
			boolean withCode = scriptActiveCheckBox.isSelected();
			boolean compiled = false;
			final SpringLocaleDelegate localeDelegate = getSpringLocaleDelegate();
			if (withCode) {
				GroovySupport support = codeEditor.getSupport();
				compiled = support.isCompiled();
				codeStateLabel.setText(compiled
						? localeDelegate.getText("nuclos.resplan.wizard.step5.scriptOk", null)
						: localeDelegate.getText("nuclos.resplan.wizard.step5.scriptError", null));
				setComplete(compiled);
//				replaceModel(backgroundPaintMethod, support.findMethodNames(BackgroundPainter.SCRIPTING_SIGNATURE));
//				replaceModel(resourceCellMethod, support.findMethodNames(CollectableLabelProvider.SCRIPTING_SIGNATURE));
//				replaceModel(entryCellMethod, support.findMethodNames(CollectableLabelProvider.SCRIPTING_SIGNATURE));
			} else {
				codeStateLabel.setText(localeDelegate.getText("nuclos.resplan.wizard.step5.scriptDisabled", null));
				setComplete(true);
			}
			backgroundPaintMethod.setEnabled(compiled);
			resourceCellMethod.setEnabled(compiled);
			entryCellMethod.setEnabled(compiled);
		}

		private void replaceModel(JComboBox comboBox, List<String> values) {
			ListComboBoxModel<String> model = new ListComboBoxModel<String>(values);
			model.setSelectedItem(comboBox.getSelectedItem());
			comboBox.setModel(model);
		}
	}

	static class CustomComponentCodeEditor extends JPanel {

		RuleEditPanel editPanel;
		GroovySupport support;

		public CustomComponentCodeEditor() {
			super(new BorderLayout());
			
			final SpringLocaleDelegate localeDelegate = SpringLocaleDelegate.getInstance();
			this.support = new GroovySupport();

			editPanel = new RuleEditPanel(null);
			editPanel.getJavaEditorPanel().setContentType("text/groovy");

			JToolBar toolBar = new JToolBar();
			toolBar.add(new AbstractAction(localeDelegate.getText("nuclos.resplan.wizard.step5.scriptEditor.compile", null)) {
				@Override
				public void actionPerformed(ActionEvent e) {
					compile();
				}
			});
			toolBar.add(new AbstractAction(localeDelegate.getText("nuclos.resplan.wizard.step5.scriptEditor.close", null)) {
				@Override
				public void actionPerformed(ActionEvent e) {
					compile();
					Window window = SwingUtilities.getWindowAncestor(CustomComponentCodeEditor.this);
					if (window != null)
						window.dispose();
				}
			});

			add(toolBar, BorderLayout.NORTH);
			add(new JScrollPane(editPanel));
		}

		public void run() {
			JDialog dialog = new JDialog(Main.getInstance().getMainFrame(), 
					SpringLocaleDelegate.getInstance().getText("nuclos.resplan.wizard.step5.scriptEditor.title", null));
			dialog.setModal(true);
			dialog.getContentPane().add(this);
			dialog.pack();
			dialog.setLocationByPlatform(true);
			dialog.setVisible(true);
		}

		public String getCode() {
			return editPanel.getJavaEditorPanel().getText();
		}

		public void setCode(String code) {
			editPanel.getJavaEditorPanel().setText(code);
			compile();
		}

		public void compile() {
			editPanel.clearMessages();
			try {
				support.compile(getCode());
			} catch (Exception ex) {
				editPanel.setMessages(Arrays.asList(new ErrorMessage(Kind.ERROR, "Skript", ex.getMessage())));
			}
		}

		public GroovySupport getSupport() {
			return support;
		}
	}

	static class CustomComponentWizardStep6 extends CustomComponentWizardAbstractStep {

		CustomComponentWizardStep6() {
			super("nuclos.resplan.wizard.step6.title", "nuclos.resplan.wizard.step6.summary");
			final SpringLocaleDelegate localeDelegate = getSpringLocaleDelegate();

			add(new JLabel(localeDelegate.getText("nuclos.resplan.wizard.step6.summary", null)));
		}

		@Override
		protected void updateState() {
		}

		@Override
		protected boolean isFinishStep() {
			return true;
		}

		@Override
		public void prepare() {
			setComplete(true);
			model.finish();
		}

		@Override
		public void applyState() throws InvalidStateException {
			model.wizard.close();
			Main.getInstance().getMainController().refreshMenus();
		}
	}

	static class LocalTimeSpanPane extends JPanel {

		private LocalTimeSpanTableModel tableModel;
		private JTable table;
		private JToolBar toolBar;
		private List<ChangeListener> changeListeners = new ArrayList<ChangeListener>();

		public LocalTimeSpanPane() {
			super(new BorderLayout());

			setBorder(BorderFactory.createEtchedBorder());
			tableModel = new LocalTimeSpanTableModel();
			tableModel.addTableModelListener(new TableModelListener() {
				@Override
				public void tableChanged(TableModelEvent e) {
					for (ChangeListener c : changeListeners)
						c.stateChanged(new ChangeEvent(LocalTimeSpanPane.class));
				}
			});
			table = new JTable(tableModel);
			table.setDefaultEditor(Object.class, new LocalTimeSpinnerCellEditor());
//			table.setDefaultRenderer(Object.class, new LocalTimeSpinnerCellEditor());
			table.setBorder(null);
			TableUtils.setOptimalColumnWidths(table);

			toolBar = new JToolBar(JToolBar.VERTICAL);
			toolBar.setFloatable(false);
			toolBar.setBackground(getBackground());
			toolBar.setOpaque(true);
			toolBar.add(new AbstractAction("Add", Icons.getInstance().getIconNew16()) {
				@Override
				public void actionPerformed(ActionEvent e) {
					int row = table.getSelectedRow();
					if (row != -1) {
						row = table.convertRowIndexToModel(row) + 1;
					} else {
						row = table.getRowCount();
					}
					LocalTime time = new LocalTime(0);
					//if (row > 0) {
					//	time = tableModel.getValueAt(row - 1, 1);
					//}
					tableModel.add(row, Pair.makePair(time, time));
					int viewRow = table.convertRowIndexToView(row);
					if (viewRow != -1) {
						table.editCellAt(viewRow, 0);
					}
				}
			});
			toolBar.add(new AbstractAction("Remove", Icons.getInstance().getIconDelete16()) {
				@Override
				public void actionPerformed(ActionEvent e) {
					int row = table.getSelectedRow();
					if (row != -1) {
						boolean canRemove = false;
						TableCellEditor cellEditor = table.getCellEditor();
						if (cellEditor != null)
							canRemove = cellEditor.stopCellEditing();
						if (canRemove)
							tableModel.remove(table.convertRowIndexToModel(row));
					}
				}
			});

			JScrollPane sp = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			sp.setBorder(null);

			add(toolBar, BorderLayout.WEST);
			add(sp);
			setPreferredSize(new Dimension(220, 100));
		}

		public void addChangeListener(ChangeListener listener) {
			changeListeners.add(listener);
		}

		public void removeChangeListener(ChangeListener listener) {
			changeListeners.remove(listener);
		}

		public List<Pair<LocalTime, LocalTime>> getLocalTimeSpans() {
			TableCellEditor cellEditor = table.getCellEditor();
			if (cellEditor != null)
				cellEditor.stopCellEditing();
			return tableModel.getRows();
		}

		public String getText() {
			StringBuilder sb = new StringBuilder();
			for (Pair<LocalTime, LocalTime> p : getLocalTimeSpans()) {
				if (sb.length() > 0)
					sb.append(';');
				sb.append(String.format("%s-%s", p.x, p.y));
			}
			return sb.toString().trim();
		}

		public void setText(String text) {
			tableModel.setRows(ResPlanConfigVO.parseTimePeriodsString(text));
		}
	}

	static class LocalTimeSpanTableModel extends AbstractListTableModel<Pair<LocalTime, LocalTime>> {

		public LocalTimeSpanTableModel() {
		}

		@Override
		public List<Pair<LocalTime, LocalTime>> getRows() {
			return super.getRows();
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public String getColumnName(int column) {
			return getSpringLocaleDelegate().getText("nuclos.resplan.wizard.step3.timeSpans." + (column == 0 ? "from" : "until"), null);
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return true;
		}

		@Override
		public LocalTime getValueAt(int rowIndex, int columnIndex) {
			Pair<LocalTime, LocalTime> p = getRow(rowIndex);
			return columnIndex == 0 ? p.x : p.y;
		}

		@Override
		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			LocalTime oldValue = getValueAt(rowIndex, columnIndex);
			if (ObjectUtils.equals(value, oldValue))
				return;
			Pair<LocalTime, LocalTime> p = getRow(rowIndex);
			switch (columnIndex) {
			case 0: p.x = (LocalTime) value; break;
			case 1: p.y = (LocalTime) value; break;
			}
			fireTableCellUpdated(rowIndex, columnIndex);
		}
	}

	static class LocalTimeSpinnerCellEditor extends AbstractCellEditor implements TableCellEditor, TableCellRenderer, ActionListener, ChangeListener {

		private final JSpinner spinner;
		private final JSpinner.DateEditor dateEditor;

		public LocalTimeSpinnerCellEditor() {
			SpinnerDateModel sm = new SpinnerDateModel(new Date(0), null, null, Calendar.HOUR_OF_DAY);
			spinner = new JSpinner(sm);
			spinner.setBorder(null);
			dateEditor = new JSpinner.DateEditor(spinner, "HH:mm");
			dateEditor.getTextField().setHorizontalAlignment(JTextField.LEFT);
			spinner.setEditor(dateEditor);
			spinner.addFocusListener(new FocusListener() {
				@Override
				public void focusGained(FocusEvent e) {
					dateEditor.getTextField().requestFocusInWindow();
				}
				@Override
				public void focusLost(FocusEvent e) {
				}
			});
			dateEditor.getTextField().addActionListener(this);
			spinner.addChangeListener(this);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			stopCellEditing();
		}

		@Override
		public void stateChanged(ChangeEvent e) {
			// nothing
		}

		@Override
		public boolean stopCellEditing() {
			try {
				spinner.commitEdit();
				return super.stopCellEditing();
			} catch (ParseException e) {
				return false;
			}
		}

		@Override
		public Object getCellEditorValue() {
			return LocalTime.parse(String.format("%tT", spinner.getValue()));
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			configure(table, value, false);
			if (dateEditor.getTextField().getText().length() >= 2) {
				dateEditor.getTextField().select(0, 2);
			}
			return spinner;
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			configure(table, value, true);
			return spinner;
		}

		private void configure(JTable table, Object value, boolean focused) {
			spinner.setValue(((LocalTime) value).toDate(new Date(0)));
		}
	}

	static class ResourceCellEditor extends AbstractCellEditor implements TableCellEditor, KeyListener {

		private final JScrollPane scroll;
		private final JTextArea ta;
		private JTable table;

		public ResourceCellEditor() {
			ta = new JTextArea();
			ta.setLineWrap(true);
			ta.setWrapStyleWord(true);
			ta.addFocusListener(NuclosWizardUtils.createWizardFocusAdapter());
			ta.addKeyListener(this);
			scroll = new JScrollPane(ta);
			scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		}

		@Override
		public Object getCellEditorValue() {
			return ta.getText();
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			if (this.table == null) {
				this.table = table;
			}
			String text = LangUtils.defaultIfNull((String)value, "");
	        ta.setText(text);
	        return scroll;
		}

		@Override
		public void keyTyped(KeyEvent e) { }

		@Override
		public void keyPressed(KeyEvent e) {
	        if (e.getKeyCode() == KeyEvent.VK_TAB && !e.isShiftDown() && table != null) {
	            e.consume();

	            int column = table.getEditingColumn();
	            int row = table.getEditingRow();
	            stopCellEditing();
	            if ((column + 1)  >= table.getColumnCount()) {
	                if ((row + 1) >= table.getRowCount()) {
	                	row = -1;
	                }
	                else {
	                    row++;
	                    column = 0;
	                }

	            }
	            else {
	            	column++;
	            }

	            if (row > -1 && column > -1) {
	            	table.changeSelection(row, column, false, false);
	            }
	        }
		}

		@Override
		public void keyReleased(KeyEvent e) { }
	}
}

