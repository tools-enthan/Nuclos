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
package org.nuclos.client.layout.wysiwyg.component;

import java.awt.Component;
import java.util.HashMap;

import javax.swing.JLabel;

import org.apache.log4j.Logger;
import org.nuclos.client.layout.wysiwyg.WYSIWYGMetaInformation;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.COMPONENT_PROCESSOR;
import org.nuclos.client.layout.wysiwyg.component.properties.ComponentProperties;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyUtils;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValueString;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.WYSIWYGLayoutEditorPanel;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.layoutml.LayoutMLConstants;

/**
 * This Class creates new {@link WYSIWYGComponent}.<br>
 * Every class must implement {@link ComponentProcessor}.<br>
 * What kind of Component will be created is checked by:
 * <ul>
 * <li> String Element</li>
 * <li> String ControlType</li>
 * </ul>
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
public class ComponentProcessors implements LayoutMLConstants {

	private static ComponentProcessors singleton;

	private HashMap<String, ComponentProcessor> mapComponentProcessors;
	
	private final HashMap<String, Integer> mapCounter = new HashMap<String, Integer>();

	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(ComponentProcessors.class);

	
	public static ComponentProcessors getInstance() {
		if (singleton == null) {
			singleton = new ComponentProcessors();
		}
		return singleton;
	}

	/**
	 * The Constructor
	 */
	public ComponentProcessors() {
		mapComponentProcessors = new HashMap<String, ComponentProcessor>();
	}

	/**
	 * 
	 * @param sElement
	 * @param sControltype
	 * @param iNumber
	 * @param attributes
	 * @return
	 */
	public Component createComponent(String sElement, String sControltype, Integer iNumber, WYSIWYGMetaInformation metaInf, String name) throws CommonBusinessException {
		if (sControltype == null) {
			sControltype = "";
		}

		if (!mapComponentProcessors.containsKey(sElement + sControltype)) {
			mapComponentProcessors.put(sElement + sControltype, createComponentElementProcessor(sElement, sControltype));
		}

		return mapComponentProcessors.get(sElement + sControltype).createEmptyComponent(iNumber, metaInf, name);
	}
	
	/**
	 * 
	 * @param sElement
	 * @param sControltype
	 * @param metaInf
	 * @param name
	 * @return
	 * @throws CommonBusinessException
	 */
	public Component createComponent(String sElement, String sControltype, WYSIWYGMetaInformation metaInf, String name) throws CommonBusinessException {
		Integer iNumber = getCounterForElement(sElement);
		return createComponent(sElement, sControltype, iNumber, metaInf, name);
	}

	/**
	 * 
	 * @param sElement
	 * @param controltype
	 * @return
	 */
	private ComponentProcessor createComponentElementProcessor(String sElement, String controltype) {
		if (ELEMENT_COLLECTABLECOMPONENT.equals(sElement)) {
			return new CollectableComponentElementProcessor(controltype);
		} else if (ELEMENT_SUBFORM.equals(sElement)) {
			return new SubformElementProcessor();
		} else if (ELEMENT_TABBEDPANE.equals(sElement)) {
			return new TabbedPaneElementProcessor();
		} else if (ELEMENT_SCROLLPANE.equals(sElement)) {
			return new ScrollPaneElementProcessor();
		} else if (ELEMENT_SPLITPANE.equals(sElement)) {
			return new SplitPaneElementProcessor();
		} else if (ELEMENT_LABEL.equals(sElement)) {
			return new StaticLabelElementProcessor();
		} else if (ELEMENT_TEXTFIELD.equals(sElement)) {
			return new StaticTextfieldElementProcessor();
		} else if (ELEMENT_TEXTAREA.equals(sElement)) {
			return new StaticTextAreaElementProcessor();
		} else if (ELEMENT_COMBOBOX.equals(sElement)) {
			return new StaticComboBoxElementProcessor();
		} else if (ELEMENT_BUTTON.equals(sElement)) {
			return new StaticButtonElementProcessor();
		} else if (ELEMENT_SEPARATOR.equals(sElement)) {
			return new StaticSeparatorElementProcessor();
		} else if (ELEMENT_TITLEDSEPARATOR.equals(sElement)) {
			return new StaticTitledSeparatorElementProcessor();
		} else if (ELEMENT_IMAGE.equals(sElement)) {
			return new StaticImageElementProcessor();
		} else if (ELEMENT_PANEL.equals(sElement)) {
			//NUCLEUSINT-650
			return new LayoutPanelElementProcessor();
		}

		return new DefaultElementProcessor();
	}

	/**
	 * This Class creates a {@link WYSIWYGComponent}:
	 * <ul>
	 * <li>{@link #ELEMENT_CHECKBOX} -> {@link WYSIWYGCollectableCheckBox}</li>
	 * <li>{@link #ELEMENT_TEXTFIELD} -> {@link WYSIWYGCollectableTextfield}</li>
	 * <li>{@link #ELEMENT_COMBOBOX} -> {@link WYSIWYGCollectableComboBox}</li>
	 * <li>{@link #CONTROLTYPE_DATECHOOSER} -> {@link WYSIWYGCollectableDateChooser}</li>
	 * <li>{@link #ELEMENT_OPTIONGROUP} -> {@link WYSIWYGCollectableOptionGroup}</li>
	 * <li>{@link #CONTROLTYPE_LISTOFVALUES} -> {@link WYSIWYGCollectableListOfValues}</li>
	 * <li>{@link #CONTROLTYPE_TEXTAREA} -> {@link WYSIWYGCollectableTextArea}</li>
	 * <li>{@link #ELEMENT_LABEL} -> {@link WYSIWYGCollectableLabel}</li>
	 * <li> {@link WYSIWYGUniversalComponent} (universal Component)</li>
	 * </ul>
	 * 
	 * <br>
	 * Created by Novabit Informationssysteme GmbH <br>
	 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
	 * 
	 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
	 * @version 01.00.00
	 */
	private class CollectableComponentElementProcessor implements ComponentProcessor {

		private String controltype;

		/**
		 * @param controltype the Controltype for this CollectableComponent
		 */
		public CollectableComponentElementProcessor(String controltype) {
			this.controltype = controltype;
		}

		/*
		 * (non-Javadoc)
		 * @see org.nuclos.client.layout.wysiwyg.component.ComponentProcessors.ComponentProcessor#createEmptyComponent(java.lang.Integer, org.nuclos.client.layout.wysiwyg.WYSIWYGMetaInformation, java.lang.String)
		 */
		@Override
		public Component createEmptyComponent(Integer iNumber, WYSIWYGMetaInformation metaInf, String name) throws CommonBusinessException {
			Component result;
			ComponentProperties properties = null;
			
			if (ELEMENT_CHECKBOX.equals(controltype)) {
				WYSIWYGCollectableCheckBox check = new WYSIWYGCollectableCheckBox();

				properties = PropertyUtils.getEmptyProperties(check, metaInf);
				properties.setProperty(WYSIWYGCollectableComponent.PROPERTY_NAME, new PropertyValueString(name), String.class);
				check.setProperties(properties);

				check.setMinimumSize(DEFAULTVALUE_CHECKBOX_MINIMUMSIZE);
				result = check;
			} else if (ELEMENT_TEXTFIELD.equals(controltype)) {
				WYSIWYGCollectableTextfield tf = new WYSIWYGCollectableTextfield();
				
				properties = PropertyUtils.getEmptyProperties(tf, metaInf);
				properties.setProperty(WYSIWYGCollectableComponent.PROPERTY_NAME, new PropertyValueString(name), String.class);
				tf.setProperties(properties);

				tf.setMinimumSize(DEFAULTVALUE_TEXTFIELD_MINIMUMSIZE);
				result = tf;
			} else if (ELEMENT_PASSWORD.equals(controltype)) {
				//NUCLEUSINT-1142
				WYSIWYGCollectablePasswordfield tf = new WYSIWYGCollectablePasswordfield();
				
				properties = PropertyUtils.getEmptyProperties(tf, metaInf);
				properties.setProperty(WYSIWYGCollectableComponent.PROPERTY_NAME, new PropertyValueString(name), String.class);
				tf.setProperties(properties);

				tf.setMinimumSize(DEFAULTVALUE_TEXTFIELD_MINIMUMSIZE);
				result = tf;
			} else if (ELEMENT_IMAGE.equals(controltype)) {
				WYSIWYGCollectableImage image = new WYSIWYGCollectableImage();
				
				properties = PropertyUtils.getEmptyProperties(image, metaInf);
				properties.setProperty(WYSIWYGCollectableComponent.PROPERTY_NAME, new PropertyValueString(name), String.class);
				image.setProperties(properties);

				image.setMinimumSize(DEFAULTVALUE_TEXTFIELD_MINIMUMSIZE);
				result = image;
			} else if (ELEMENT_COMBOBOX.equals(controltype)) {
				WYSIWYGCollectableComboBox combo = new WYSIWYGCollectableComboBox();

				properties = PropertyUtils.getEmptyProperties(combo,metaInf);
				properties.setProperty(WYSIWYGCollectableComponent.PROPERTY_NAME, new PropertyValueString(name), String.class);
				combo.setProperties(properties);

				combo.setMinimumSize(DEFAULTVALUE_COMBOBOX_MINIMUMSIZE);
				result = combo;
			} else if (CONTROLTYPE_DATECHOOSER.equals(controltype)) {
				WYSIWYGCollectableDateChooser dc = new WYSIWYGCollectableDateChooser();
				
				properties = PropertyUtils.getEmptyProperties(dc, metaInf);
				properties.setProperty(WYSIWYGCollectableComponent.PROPERTY_NAME, new PropertyValueString(name), String.class);
				dc.setProperties(properties);

				dc.setMinimumSize(DEFAULTVALUE_DATECHOOSER_MINIMUMSIZE);
				result = dc;
			} else if (ELEMENT_OPTIONGROUP.equals(controltype)) {
				WYSIWYGCollectableOptionGroup og = new WYSIWYGCollectableOptionGroup(metaInf);
				
				properties = PropertyUtils.getEmptyProperties(og, metaInf);
				properties.setProperty(WYSIWYGCollectableComponent.PROPERTY_NAME, new PropertyValueString(name), String.class);
				og.setProperties(properties);

				og.setMinimumSize(DEFAULTVALUE_OPTIONGROUP_MINIMUMSIZE);
				result = og;
			} else if (CONTROLTYPE_LISTOFVALUES.equals(controltype)) {
				WYSIWYGCollectableListOfValues lov = new WYSIWYGCollectableListOfValues();
				
				properties = PropertyUtils.getEmptyProperties(lov, metaInf);
				properties.setProperty(WYSIWYGCollectableComponent.PROPERTY_NAME, new PropertyValueString(name), String.class);
				lov.setProperties(properties);

				lov.setMinimumSize(DEFAULTVALUE_LISTOFVALUES_MINIMUMSIZE);
				result = lov;
			} else if (CONTROLTYPE_TEXTAREA.equals(controltype)) {
				WYSIWYGCollectableTextArea cta = new WYSIWYGCollectableTextArea();
				
				properties = PropertyUtils.getEmptyProperties(cta, metaInf);
				properties.setProperty(WYSIWYGCollectableComponent.PROPERTY_NAME, new PropertyValueString(name), String.class);
				cta.setProperties(properties);

				cta.setMinimumSize(DEFAULTVALUE_TEXTAREA_MINIMUMSIZE);
				result = cta;
			} else if (ELEMENT_LABEL.equals(controltype)) {
				WYSIWYGCollectableLabel label = new WYSIWYGCollectableLabel(metaInf);
				
				properties = PropertyUtils.getEmptyProperties(label, metaInf);
				properties.setProperty(WYSIWYGCollectableComponent.PROPERTY_NAME, new PropertyValueString(name), String.class);
				label.setProperties(properties);

				label.setMinimumSize(DEFAULTVALUE_LABEL_MINIMUMSIZE);
				result = label;
			} else if (StringUtils.isNullOrEmpty(controltype)) {
				WYSIWYGUniversalComponent component = new WYSIWYGUniversalComponent(metaInf);
				
				properties = PropertyUtils.getEmptyProperties(component, metaInf);
				properties.setProperty(WYSIWYGCollectableComponent.PROPERTY_NAME, new PropertyValueString(name), String.class);
				component.setProperties(properties);
			
				component.setMinimumSize(DEFAULTVALUE_TEXTFIELD_MINIMUMSIZE);
				result = component;
			} else {
				result = new JLabel(WYSIWYGStringsAndLabels.partedString(COMPONENT_PROCESSOR.ERRORMESSAGE_NOT_SUPPORTED_CONTROLTYPE, controltype));
			}
		
			return result;
		}
	}

	/**
	 * This Class creates a {@link WYSIWYGSplitPane}
	 * 
	 * 
	 * <br>
	 * Created by Novabit Informationssysteme GmbH <br>
	 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
	 * 
	 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
	 * @version 01.00.00
	 */
	private class SplitPaneElementProcessor implements ComponentProcessor {
		/*
		 * (non-Javadoc)
		 * @see org.nuclos.client.layout.wysiwyg.component.ComponentProcessors.ComponentProcessor#createEmptyComponent(java.lang.Integer, org.nuclos.client.layout.wysiwyg.WYSIWYGMetaInformation, java.lang.String)
		 */
		@Override
		public Component createEmptyComponent(Integer iNumber, WYSIWYGMetaInformation metaInf, String name) throws CommonBusinessException {
			WYSIWYGSplitPane splitPane = new WYSIWYGSplitPane();

			ComponentProperties properties = PropertyUtils.getEmptyProperties(splitPane, metaInf);
			splitPane.setProperties(properties);
			properties.setProperty(WYSIWYGSplitPane.PROPERTY_NAME, new PropertyValueString(COMPONENT_PROCESSOR.LABEL_DEFAULTNAME_SPLITPANE+ iNumber), String.class);

			if (iNumber > 0) { // 0 = loading from saved layoutML
				WYSIWYGLayoutEditorPanel firstEditor = new WYSIWYGLayoutEditorPanel(metaInf);
				firstEditor.getTableLayoutUtil().createStandardLayout();

				WYSIWYGLayoutEditorPanel secondEditor = new WYSIWYGLayoutEditorPanel(metaInf);
				secondEditor.getTableLayoutUtil().createStandardLayout();

				splitPane.setEditors(firstEditor, secondEditor);
			}

			splitPane.setMinimumSize(DEFAULTVALUE_SPLITPANE_MINIMUMSIZE);
			return splitPane;
		}
	}

	/**
	 * This Class creates a {@link WYSIWYGScrollPane}
	 * 
	 * 
	 * <br>
	 * Created by Novabit Informationssysteme GmbH <br>
	 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
	 * 
	 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
	 * @version 01.00.00
	 */
	private class ScrollPaneElementProcessor implements ComponentProcessor {
		/*
		 * (non-Javadoc)
		 * @see org.nuclos.client.layout.wysiwyg.component.ComponentProcessors.ComponentProcessor#createEmptyComponent(java.lang.Integer, org.nuclos.client.layout.wysiwyg.WYSIWYGMetaInformation, java.lang.String)
		 */
		@Override
		public Component createEmptyComponent(Integer iNumber, WYSIWYGMetaInformation metaInf, String name) throws CommonBusinessException {
			WYSIWYGScrollPane scrollPane = new WYSIWYGScrollPane();

			if (iNumber > 0) { // 0 = loading from saved layoutML

				WYSIWYGLayoutEditorPanel editor = new WYSIWYGLayoutEditorPanel(metaInf);
				editor.getTableLayoutUtil().createStandardLayout();

				scrollPane.setViewportView(editor);
			}

			ComponentProperties properties = PropertyUtils.getEmptyProperties(scrollPane, metaInf);
			scrollPane.setProperties(properties);
			properties.setProperty(WYSIWYGScrollPane.PROPERTY_NAME, new PropertyValueString(COMPONENT_PROCESSOR.LABEL_DEFAULTNAME_SCROLLPANE + iNumber), String.class);
			
			scrollPane.setMinimumSize(DEFAULTVALUE_SCROLLPANE_MINIMUMSIZE);
			return scrollPane;
		}
	}
	
	/**
	 * This Class creates a {@link WYSIWYGLayoutEditorPanel}
	 * 
	 * <br>
	 * Created by Novabit Informationssysteme GmbH <br>
	 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
	 * 
	 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
	 * @version 01.00.00
	 * NUCLEUSINT-650
	 */
	private class LayoutPanelElementProcessor implements ComponentProcessor {

		@Override
		public Component createEmptyComponent(Integer iNumber, WYSIWYGMetaInformation metaInf, String name) throws CommonBusinessException {
			WYSIWYGLayoutEditorPanel newTableLayoutPanel = new WYSIWYGLayoutEditorPanel(metaInf);
			newTableLayoutPanel.getTableLayoutUtil().createStandardLayout();
			return newTableLayoutPanel;
		}
		
	}

	/**
	 * This Class creates a {@link WYSIWYGTabbedPane}
	 * 
	 * 
	 * <br>
	 * Created by Novabit Informationssysteme GmbH <br>
	 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
	 * 
	 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
	 * @version 01.00.00
	 */
	private class TabbedPaneElementProcessor implements ComponentProcessor {
		/*
		 * (non-Javadoc)
		 * @see org.nuclos.client.layout.wysiwyg.component.ComponentProcessors.ComponentProcessor#createEmptyComponent(java.lang.Integer, org.nuclos.client.layout.wysiwyg.WYSIWYGMetaInformation, java.lang.String)
		 */
		@Override
		public Component createEmptyComponent(Integer iNumber, WYSIWYGMetaInformation metaInf, String name) throws CommonBusinessException {
			WYSIWYGTabbedPane tabbedPane = new WYSIWYGTabbedPane();

			if (iNumber > 0) { // 0 = loading from saved layoutML

				WYSIWYGLayoutEditorPanel oneTab = new WYSIWYGLayoutEditorPanel(metaInf);
				WYSIWYGLayoutEditorPanel secondTab = new WYSIWYGLayoutEditorPanel(metaInf);

				oneTab.getTableLayoutUtil().createStandardLayout();
				secondTab.getTableLayoutUtil().createStandardLayout();

				tabbedPane.addTab(COMPONENT_PROCESSOR.LABEL_DEFAULTNAME_TAB1, oneTab);
				tabbedPane.addTab(COMPONENT_PROCESSOR.LABEL_DEFAULTNAME_TAB2, secondTab);
			}

			ComponentProperties properties = PropertyUtils.getEmptyProperties(tabbedPane, metaInf);
			tabbedPane.setProperties(properties);
			properties.setProperty(WYSIWYGTabbedPane.PROPERTY_NAME, new PropertyValueString(COMPONENT_PROCESSOR.LABEL_DEFAULTNAME_TABBEDPANE + iNumber), String.class);
			
			tabbedPane.setMinimumSize(DEFAULTVALUE_TABBEDPANE_MINIMUMSIZE);
			return tabbedPane;
		}
	}

	/**
	 * This Class creates a {@link WYSIWYGSubForm}
	 * 
	 * 
	 * <br>
	 * Created by Novabit Informationssysteme GmbH <br>
	 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
	 * 
	 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
	 * @version 01.00.00
	 */
	private class SubformElementProcessor implements ComponentProcessor {
		/*
		 * (non-Javadoc)
		 * @see org.nuclos.client.layout.wysiwyg.component.ComponentProcessors.ComponentProcessor#createEmptyComponent(java.lang.Integer, org.nuclos.client.layout.wysiwyg.WYSIWYGMetaInformation, java.lang.String)
		 */
		@Override
		public Component createEmptyComponent(Integer iNumber, WYSIWYGMetaInformation metaInf, String name) throws CommonBusinessException {
			WYSIWYGSubForm subform = new WYSIWYGSubForm(metaInf);
			
			ComponentProperties properties = PropertyUtils.getEmptyProperties(subform, metaInf);
			properties.setProperty(WYSIWYGSubForm.PROPERTY_NAME, new PropertyValueString(COMPONENT_PROCESSOR.LABEL_DEFAULTNAME_SUBFORM + iNumber), String.class);
			//NUCLEUSINT-803
			properties.setProperty(WYSIWYGSubForm.PROPERTY_TOOLBARORIENTATION, new PropertyValueString(ATTRIBUTEVALUE_VERTICAL), String.class);
			subform.setProperties(properties);
			
			subform.setMinimumSize(DEFAULTVALUE_SUBFORM_MINIMUMSIZE);

			return subform;
		}
	}
	
	/**
	 * This Element shows up if something went wrong...<br>
	 * If shown there was not found a fitting {@link ComponentProcessor} for  the incoming Parameters
	 * 
	 * <br>
	 * Created by Novabit Informationssysteme GmbH <br>
	 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
	 * 
	 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
	 * @version 01.00.00
	 */
	private class DefaultElementProcessor implements ComponentProcessor {
		/*
		 * (non-Javadoc)
		 * @see org.nuclos.client.layout.wysiwyg.component.ComponentProcessors.ComponentProcessor#createEmptyComponent(java.lang.Integer, org.nuclos.client.layout.wysiwyg.WYSIWYGMetaInformation, java.lang.String)
		 */
		@Override
		public Component createEmptyComponent(Integer iNumber, WYSIWYGMetaInformation metaInf, String name) throws CommonBusinessException {
			return new JLabel(COMPONENT_PROCESSOR.ERRORMESSAGE_UNSUPPORTED_ELEMENT);
		}
	}

	/**
	 * This Class creates a {@link WYSIWYGStaticLabel}
	 * 
	 * 
	 * <br>
	 * Created by Novabit Informationssysteme GmbH <br>
	 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
	 * 
	 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
	 * @version 01.00.00
	 */
	private class StaticLabelElementProcessor implements ComponentProcessor {
		/*
		 * (non-Javadoc)
		 * @see org.nuclos.client.layout.wysiwyg.component.ComponentProcessors.ComponentProcessor#createEmptyComponent(java.lang.Integer, org.nuclos.client.layout.wysiwyg.WYSIWYGMetaInformation, java.lang.String)
		 */
		@Override
		public Component createEmptyComponent(Integer iNumber, WYSIWYGMetaInformation metaInf, String name) throws CommonBusinessException {
			WYSIWYGComponent element = new WYSIWYGStaticLabel();
			
			ComponentProperties properties = PropertyUtils.getEmptyProperties(element, metaInf);
			properties.setProperty(WYSIWYGStaticLabel.PROPERTY_NAME, new PropertyValueString(COMPONENT_PROCESSOR.LABEL_DEFAULTNAME_STATIC_LABEL + iNumber), String.class);
			properties.setProperty(WYSIWYGStaticLabel.PROPERTY_TEXT, new PropertyValueString(COMPONENT_PROCESSOR.LABEL_DEFAULTNAME_STATIC_LABEL + iNumber), String.class);
			element.setProperties(properties);
			
			return (Component)element;
		}
	}

	/**
	 * This class creates a {@link WYSIWYGStaticTextfield}
	 * 
	 * 
	 * <br>
	 * Created by Novabit Informationssysteme GmbH <br>
	 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
	 * 
	 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
	 * @version 01.00.00
	 */
	private class StaticTextfieldElementProcessor implements ComponentProcessor {
		/*
		 * (non-Javadoc)
		 * @see org.nuclos.client.layout.wysiwyg.component.ComponentProcessors.ComponentProcessor#createEmptyComponent(java.lang.Integer, org.nuclos.client.layout.wysiwyg.WYSIWYGMetaInformation, java.lang.String)
		 */
		@Override
		public Component createEmptyComponent(Integer iNumber, WYSIWYGMetaInformation metaInf, String name) throws CommonBusinessException {
			WYSIWYGComponent element = new WYSIWYGStaticTextfield();
		
			ComponentProperties properties = PropertyUtils.getEmptyProperties(element, metaInf);
			properties.setProperty(WYSIWYGStaticTextfield.PROPERTY_NAME, new PropertyValueString(COMPONENT_PROCESSOR.LABEL_DEFAULTNAME_STATIC_TEXTFIELD + iNumber), String.class);
			element.setProperties(properties);

			((Component)element).setMinimumSize(DEFAULTVALUE_TEXTFIELD_MINIMUMSIZE);
			return (Component)element;
		}
	}
	
	/**
	 * This class creates a {@link WYSIWYGStaticTextfield}
	 * 
	 * 
	 * <br>
	 * Created by Novabit Informationssysteme GmbH <br>
	 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
	 * 
	 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
	 * @version 01.00.00
	 */
	private class StaticImageElementProcessor implements ComponentProcessor {
		/*
		 * (non-Javadoc)
		 * @see org.nuclos.client.layout.wysiwyg.component.ComponentProcessors.ComponentProcessor#createEmptyComponent(java.lang.Integer, org.nuclos.client.layout.wysiwyg.WYSIWYGMetaInformation, java.lang.String)
		 */
		@Override
		public Component createEmptyComponent(Integer iNumber, WYSIWYGMetaInformation metaInf, String name) throws CommonBusinessException {
			WYSIWYGComponent element = new WYSIWYGStaticImage();			
		
			ComponentProperties properties = PropertyUtils.getEmptyProperties(element, metaInf);
			properties.setProperty(WYSIWYGStaticImage.PROPERTY_NAME, new PropertyValueString(COMPONENT_PROCESSOR.LABEL_DEFAULTNAME_STATIC_IMAGE + iNumber), String.class);
			element.setProperties(properties);

			((Component)element).setMinimumSize(DEFAULTVALUE_TEXTFIELD_MINIMUMSIZE);
			return (Component)element;
		}
	}

	/**
	 * This Class creates a {@link WYSIWYGStaticTextarea}
	 * 
	 * 
	 * <br>
	 * Created by Novabit Informationssysteme GmbH <br>
	 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
	 * 
	 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
	 * @version 01.00.00
	 */
	private class StaticTextAreaElementProcessor implements ComponentProcessor {
		/*
		 * (non-Javadoc)
		 * @see org.nuclos.client.layout.wysiwyg.component.ComponentProcessors.ComponentProcessor#createEmptyComponent(java.lang.Integer, org.nuclos.client.layout.wysiwyg.WYSIWYGMetaInformation, java.lang.String)
		 */
		@Override
		public Component createEmptyComponent(Integer iNumber, WYSIWYGMetaInformation metaInf, String name) throws CommonBusinessException {
			WYSIWYGComponent element = new WYSIWYGStaticTextarea();
			
			ComponentProperties properties = PropertyUtils.getEmptyProperties(element, metaInf);
			properties.setProperty(WYSIWYGStaticTextarea.PROPERTY_NAME, new PropertyValueString(COMPONENT_PROCESSOR.LABEL_DEFAULTNAME_STATIC_TEXTAREA + iNumber), String.class);
			element.setProperties(properties);
			
			((Component)element).setMinimumSize(DEFAULTVALUE_TEXTAREA_MINIMUMSIZE);
			return (Component)element;
		}
	}

	/**
	 * This Class creates a {@link WYSIWYGStaticComboBox}
	 * 
	 * 
	 * <br>
	 * Created by Novabit Informationssysteme GmbH <br>
	 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
	 * 
	 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
	 * @version 01.00.00
	 */
	private class StaticComboBoxElementProcessor implements ComponentProcessor {
		/*
		 * (non-Javadoc)
		 * @see org.nuclos.client.layout.wysiwyg.component.ComponentProcessors.ComponentProcessor#createEmptyComponent(java.lang.Integer, org.nuclos.client.layout.wysiwyg.WYSIWYGMetaInformation, java.lang.String)
		 */
		@Override
		public Component createEmptyComponent(Integer iNumber, WYSIWYGMetaInformation metaInf, String name) throws CommonBusinessException {
			WYSIWYGComponent element = new WYSIWYGStaticComboBox();
		
			ComponentProperties properties = PropertyUtils.getEmptyProperties(element, metaInf);
			properties.setProperty(WYSIWYGStaticComboBox.PROPERTY_NAME, new PropertyValueString(COMPONENT_PROCESSOR.LABEL_DEFAULTNAME_STATIC_COMBOBOX + iNumber), String.class);
			element.setProperties(properties);
			
			((Component)element).setMinimumSize(DEFAULTVALUE_COMBOBOX_MINIMUMSIZE);
			return (Component)element;
		}
	}

	/**
	 * This Class creates a {@link WYSIWYGStaticButton}
	 * 
	 * 
	 * <br>
	 * Created by Novabit Informationssysteme GmbH <br>
	 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
	 * 
	 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
	 * @version 01.00.00
	 */
	private class StaticButtonElementProcessor implements ComponentProcessor {
		/*
		 * (non-Javadoc)
		 * @see org.nuclos.client.layout.wysiwyg.component.ComponentProcessors.ComponentProcessor#createEmptyComponent(java.lang.Integer, org.nuclos.client.layout.wysiwyg.WYSIWYGMetaInformation, java.lang.String)
		 */
		@Override
		public Component createEmptyComponent(Integer iNumber, WYSIWYGMetaInformation metaInf, String name) throws CommonBusinessException {	
			WYSIWYGComponent element = new WYSIWYGStaticButton();
			
			ComponentProperties properties = PropertyUtils.getEmptyProperties(element, metaInf);
			properties.setProperty(WYSIWYGStaticButton.PROPERTY_NAME, new PropertyValueString(COMPONENT_PROCESSOR.LABEL_DEFAULTNAME_BUTTON+ iNumber), String.class);
			properties.setProperty(WYSIWYGStaticButton.PROPERTY_LABEL, new PropertyValueString(COMPONENT_PROCESSOR.LABEL_DEFAULTNAME_BUTTON + iNumber), String.class);
			element.setProperties(properties);
			
			((Component)element).setMinimumSize(DEFAULTVALUE_BUTTON_MINIMUMSIZE);
			return (Component)element;
		}
	}

	/**
	 * This Class creates a {@link WYSIWYGStaticSeparator}
	 * 
	 * 
	 * <br>
	 * Created by Novabit Informationssysteme GmbH <br>
	 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
	 * 
	 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
	 * @version 01.00.00
	 */
	private class StaticSeparatorElementProcessor implements ComponentProcessor {
		/*
		 * (non-Javadoc)
		 * @see org.nuclos.client.layout.wysiwyg.component.ComponentProcessors.ComponentProcessor#createEmptyComponent(java.lang.Integer, org.nuclos.client.layout.wysiwyg.WYSIWYGMetaInformation, java.lang.String)
		 */
		@Override
		public Component createEmptyComponent(Integer iNumber, WYSIWYGMetaInformation metaInf, String name) throws CommonBusinessException {
			WYSIWYGComponent element = new WYSIWYGStaticSeparator();
			
			ComponentProperties properties = PropertyUtils.getEmptyProperties(element, metaInf);
			element.setProperties(properties);
			
			return (Component)element;
		}
	}
	
	/**
	 * This Class creates a {@link WYSIWYGStaticTitledSeparator}
	 * 
	 * 
	 * <br>
	 * Created by Novabit Informationssysteme GmbH <br>
	 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
	 * 
	 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
	 * @version 01.00.00
	 */
	private class StaticTitledSeparatorElementProcessor implements ComponentProcessor {
		/*
		 * (non-Javadoc)
		 * @see org.nuclos.client.layout.wysiwyg.component.ComponentProcessors.ComponentProcessor#createEmptyComponent(java.lang.Integer, org.nuclos.client.layout.wysiwyg.WYSIWYGMetaInformation, java.lang.String)
		 */
		@Override
		public Component createEmptyComponent(Integer iNumber, WYSIWYGMetaInformation metaInf, String name) throws CommonBusinessException {
			WYSIWYGStaticTitledSeparator element = new WYSIWYGStaticTitledSeparator(COMPONENT_PROCESSOR.LABEL_DEFAULTNAME_TITLED_SEPERATOR + iNumber);
			
			ComponentProperties properties = PropertyUtils.getEmptyProperties(element, metaInf);
			element.setProperties(properties);
			
			return element;
		}
	}
	
	/**
	 * This Method counts the Elements and creates a "unique" Name by adding a counter behind the Name (e.g. Label_1, Label_2 etc)
	 * @param sElement The Element for counting every Component seperatly
	 * @return the next Number in the Sequence for this Element
	 */
	private int getCounterForElement(String sElement) {
		int iCounter = 1;

		if (!mapCounter.containsKey(sElement)) {
			mapCounter.put(sElement, new Integer(iCounter));
		} else {
			iCounter = mapCounter.get(sElement).intValue();
			iCounter++;
			mapCounter.put(sElement, new Integer(iCounter));
		}

		return iCounter;
	}

	/**
	 * This interface must be implemented to create a new empty Component.<br>
	 * 
	 * <br>
	 * Created by Novabit Informationssysteme GmbH <br>
	 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
	 * 
	 * @author <a href="mailto:maik.stueker@novabit.de">maik.stueker</a>
	 * @version 01.00.00
	 */
	private interface ComponentProcessor {

		/**
		 * This Method is called to create a new Component with the given Parameters.<br>
		 * The Component returned is a "default" Component.
		 * Individual Settings for the Component are set with the {@link ComponentProperties}.<br>
		 * 
		 * Creates a new empty Set of {@link ComponentProperties}.<br>
		 * 
		 * @param iNumber ongoing Number for enumerating the Components (e.g. Label_1)
		 * @param metaInf the {@link WYSIWYGMetaInformation} for getting values etc.
		 * @param name the Name of the Component (the name of the Component)
		 * @return {@link Component} instance of {@link WYSIWYGComponent}
		 * @throws CommonBusinessException
		 */
		Component createEmptyComponent(Integer iNumber, WYSIWYGMetaInformation metaInf, String name) throws CommonBusinessException;
	}
}
