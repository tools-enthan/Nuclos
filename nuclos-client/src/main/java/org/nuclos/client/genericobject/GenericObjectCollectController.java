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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.Serializable;
import java.text.Collator;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicReference;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollBar;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.RowSorter.SortKey;
import javax.swing.TransferHandler;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.apache.commons.lang.NullArgumentException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.nuclos.client.attribute.AttributeCache;
import org.nuclos.client.common.ClientParameterProvider;
import org.nuclos.client.common.DependantCollectableMasterDataMap;
import org.nuclos.client.common.DetailsSubFormController;
import org.nuclos.client.common.EntityCollectController;
import org.nuclos.client.common.KeyBindingProvider;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.common.MultiUpdateOfDependants;
import org.nuclos.client.common.NuclosCollectControllerFactory;
import org.nuclos.client.common.NuclosCollectableStateComboBox;
import org.nuclos.client.common.NuclosResultPanel;
import org.nuclos.client.common.SearchConditionSubFormController;
import org.nuclos.client.common.SubFormController;
import org.nuclos.client.common.Utils;
import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.client.entityobject.CollectableEntityObject;
import org.nuclos.client.explorer.ExplorerController;
import org.nuclos.client.explorer.ExplorerDelegate;
import org.nuclos.client.genericobject.actionlisteners.ResetToTemplateUserActionListener;
import org.nuclos.client.genericobject.datatransfer.GenericObjectIdModuleProcess;
import org.nuclos.client.genericobject.datatransfer.TransferableGenericObjects;
import org.nuclos.client.genericobject.logbook.LogbookController;
import org.nuclos.client.genericobject.resulttemplate.SearchResultTemplate;
import org.nuclos.client.genericobject.statehistory.StateHistoryController;
import org.nuclos.client.genericobject.valuelistprovider.GenericObjectCollectableFieldsProviderFactory;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.masterdata.CollectableMasterData;
import org.nuclos.client.masterdata.CollectableMasterDataWithDependants;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.masterdata.MasterDataSubFormController;
import org.nuclos.client.masterdata.valuelistprovider.MasterDataCollectableFieldsProviderFactory;
import org.nuclos.client.resource.NuclosResourceCache;
import org.nuclos.client.resource.ResourceCache;
import org.nuclos.client.searchfilter.EntitySearchFilter;
import org.nuclos.client.searchfilter.SearchFilter;
import org.nuclos.client.searchfilter.SearchFilters;
import org.nuclos.client.statemodel.StateDelegate;
import org.nuclos.client.ui.BlackLabel;
import org.nuclos.client.ui.CommonAbstractAction;
import org.nuclos.client.ui.CommonClientWorkerAdapter;
import org.nuclos.client.ui.CommonMultiThreader;
import org.nuclos.client.ui.DateChooser;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.CollectController;
import org.nuclos.client.ui.collect.CollectPanel;
import org.nuclos.client.ui.collect.CollectState;
import org.nuclos.client.ui.collect.CollectStateAdapter;
import org.nuclos.client.ui.collect.CollectStateConstants;
import org.nuclos.client.ui.collect.CollectStateEvent;
import org.nuclos.client.ui.collect.CollectableComponentsProvider;
import org.nuclos.client.ui.collect.CollectableTableModel;
import org.nuclos.client.ui.collect.DefaultEditView;
import org.nuclos.client.ui.collect.DeleteSelectedCollectablesController;
import org.nuclos.client.ui.collect.DetailsCollectableEventListener;
import org.nuclos.client.ui.collect.DetailsPanel;
import org.nuclos.client.ui.collect.GenericObjectResultController;
import org.nuclos.client.ui.collect.ResultController;
import org.nuclos.client.ui.collect.ResultPanel;
import org.nuclos.client.ui.collect.SearchPanel;
import org.nuclos.client.ui.collect.SortableCollectableTableModel;
import org.nuclos.client.ui.collect.SubForm;
import org.nuclos.client.ui.collect.UpdateSelectedCollectablesController.UpdateAction;
import org.nuclos.client.ui.collect.UserResultController;
import org.nuclos.client.ui.collect.component.CollectableComboBox;
import org.nuclos.client.ui.collect.component.CollectableComponent;
import org.nuclos.client.ui.collect.component.CollectableComponentTableCellEditor;
import org.nuclos.client.ui.collect.component.CollectableComponentWithValueListProvider;
import org.nuclos.client.ui.collect.component.CollectableDateChooser;
import org.nuclos.client.ui.collect.component.EntityListOfValues;
import org.nuclos.client.ui.collect.component.ICollectableListOfValues;
import org.nuclos.client.ui.collect.component.LookupEvent;
import org.nuclos.client.ui.collect.component.LookupListener;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModel;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelAdapter;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelEvent;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelListener;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelProvider;
import org.nuclos.client.ui.collect.component.model.DefaultDetailsEditModel;
import org.nuclos.client.ui.collect.component.model.DefaultSearchEditModel;
import org.nuclos.client.ui.collect.component.model.DetailsComponentModel;
import org.nuclos.client.ui.collect.component.model.DetailsEditModel;
import org.nuclos.client.ui.collect.component.model.EditModel;
import org.nuclos.client.ui.collect.component.model.SearchComponentModel;
import org.nuclos.client.ui.collect.component.model.SearchComponentModelEvent;
import org.nuclos.client.ui.collect.component.model.SearchEditModel;
import org.nuclos.client.ui.labeled.LabeledComponent;
import org.nuclos.client.ui.layoutml.LayoutRoot;
import org.nuclos.client.ui.multiaction.MultiActionProgressPanel;
import org.nuclos.client.ui.multiaction.MultiActionProgressResultHandler;
import org.nuclos.client.ui.multiaction.MultiCollectablesActionController;
import org.nuclos.client.ui.table.TableUtils;
import org.nuclos.client.valuelistprovider.cache.ManagedCollectableFieldsProvider;
import org.nuclos.common.Actions;
import org.nuclos.common.AttributeProvider;
import org.nuclos.common.CollectableEntityFieldWithEntity;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.ParameterProvider;
import org.nuclos.common.PointerCollection;
import org.nuclos.common.PointerException;
import org.nuclos.common.UsageCriteria;
import org.nuclos.common.attribute.DynamicAttributeVO;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldsProvider;
import org.nuclos.common.collect.collectable.CollectableFieldsProviderFactory;
import org.nuclos.common.collect.collectable.CollectableUtils;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common.collect.collectable.DefaultCollectableEntityField;
import org.nuclos.common.collect.collectable.DefaultCollectableEntityProvider;
import org.nuclos.common.collect.collectable.searchcondition.AtomicCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparison;
import org.nuclos.common.collect.collectable.searchcondition.CollectableLikeCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSubCondition;
import org.nuclos.common.collect.collectable.searchcondition.CompositeCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.LogicalOperator;
import org.nuclos.common.collect.collectable.searchcondition.SearchConditionUtils;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common.collection.PredicateUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.genericobject.CollectableGenericObjectEntityField;
import org.nuclos.common.genericobject.GenericObjectUtils;
import org.nuclos.common.security.Permission;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.DateUtils;
import org.nuclos.common2.EntityAndFieldName;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.attribute.valueobject.AttributeCVO;
import org.nuclos.server.genericobject.ProxyList;
import org.nuclos.server.genericobject.searchcondition.CollectableGenericObjectSearchExpression;
import org.nuclos.server.genericobject.valueobject.GeneratorActionVO;
import org.nuclos.server.genericobject.valueobject.GenericObjectDocumentFile;
import org.nuclos.server.genericobject.valueobject.GenericObjectVO;
import org.nuclos.server.genericobject.valueobject.GenericObjectWithDependantsVO;
import org.nuclos.server.genericobject.valueobject.LogbookVO;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.navigation.treenode.EntitySearchResultTreeNode;
import org.nuclos.server.navigation.treenode.GenericObjectTreeNode;
import org.nuclos.server.ruleengine.valueobject.RuleVO;
import org.nuclos.server.statemodel.valueobject.MandatoryFieldVO;
import org.nuclos.server.statemodel.valueobject.StateVO;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * Controller for collecting generic objects.
 * Contains the necessary logic to search for, view and edit generic objects.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class GenericObjectCollectController extends EntityCollectController<CollectableGenericObjectWithDependants> {
	protected static final Logger log = Logger.getLogger(GenericObjectCollectController.class);

	private final Logger logSecurity = Logger.getLogger(log.getName() + ".security");

	private static final String PREFS_KEY_FILTERNAME = "filterName";
	private static final String PREFS_KEY_SEARCHRESULTTEMPLATENAME = "searchResultTemplateName";

	private static final String CALCULATED_ATTRS_WORKER_NAME = "calculatedAttrsWorker";

	private static final String TABSELECTED = "tabselected";


	public static final CollectableEntityField clctefStatus = new DefaultCollectableEntityField(NuclosEOField.STATE.getMetaData().getField(), String.class,
		CommonLocaleDelegate.getLabelFromAttributeCVO(AttributeCache.getInstance().getAttribute(NuclosEOField.STATE.getMetaData().getId().intValue())), null, null, null, true, CollectableField.TYPE_VALUEIDFIELD, null, null);
	public static final CollectableEntityField clctefStatusnumeral = new DefaultCollectableEntityField(NuclosEOField.STATENUMBER.getMetaData().getField(), Integer.class,
		CommonLocaleDelegate.getLabelFromAttributeCVO(AttributeCache.getInstance().getAttribute(NuclosEOField.STATENUMBER.getMetaData().getId().intValue())), null, null, null, true, CollectableField.TYPE_VALUEIDFIELD, null, null);
	private final CollectableEntityField clctefSearchState = new DefaultCollectableEntityField("[status_num_plus_name]", String.class,
		CommonLocaleDelegate.getLabelFromAttributeCVO(AttributeCache.getInstance().getAttribute(NuclosEOField.STATE.getMetaData().getId().intValue())), null, null, null, true, CollectableField.TYPE_VALUEIDFIELD, null, null);
	private final CollectableComboBox clctSearchState = new NuclosCollectableStateComboBox(clctefSearchState, true);

	private static final Collection<String> collUsageCriteriaFieldNames = Collections.unmodifiableCollection(UsageCriteria.getContainedAttributeNames());

	private final CollectableComponentModelListener ccmlistenerUsageCriteriaFieldsForDetails = new CollectableComponentModelAdapter() {
		@Override
		public void collectableFieldChangedInModel(CollectableComponentModelEvent ev) {
			if (ev.collectableFieldHasChanged() || ev.getNewValue().getValue() == null) {
				final String sFieldName = ev.getCollectableComponentModel().getFieldName();
				log.debug("UsageCriteria field " + sFieldName + " changed in Details panel.");
				UIUtils.runCommandLater(getFrame(), new Runnable() {
					@Override
					public void run() {
						try {
							assert getCollectStateModel().getOuterState() == CollectStateConstants.OUTERSTATE_DETAILS;

							GenericObjectCollectController.this.stopEditingInDetails();

							CollectableGenericObjectWithDependants selectedCollectable = GenericObjectCollectController.this.getSelectedCollectable();
							if(selectedCollectable != null && selectedCollectable.getId() != null){
								subFormsLoader.suspendRunningClientWorkers();
								subFormsLoader.addSuspendedClientWorker(CALCULATED_ATTRS_WORKER_NAME); //explicit mark for reloading of calc. attrs. for new layout
							}

							synchronized (this) {
								reloadLayoutForDetailsTab(true);

								if (sFieldName.equals(NuclosEOField.PROCESS.getMetaData().getField()))
									showCustomActions(getCollectStateModel().getDetailsMode());
							}
							if(selectedCollectable != null && selectedCollectable.getId() != null){
								subFormsLoader.startLoading();
								for (final SubFormController subformctl : GenericObjectCollectController.this.getSubFormControllersInDetails()) {
									// TODO try to eliminate this cast
									final MasterDataSubFormController mdsubformctl = (MasterDataSubFormController) subformctl;
									fillSubformMultithreaded(selectedCollectable, mdsubformctl);
								}
								subFormsLoader.resume();
							}

						}
						catch (CommonBusinessException ex) {
							Errors.getInstance().showExceptionDialog(getFrame(), CommonLocaleDelegate.getMessage("GenericObjectCollectController.15","Beim Nachladen eines Layouts ist ein Fehler aufgetreten."), ex);
						}
					}
				});
			}
		}
	};

	private final CollectableComponentModelListener ccmlistenerUsageCriteriaFieldsForSearch = new CollectableComponentModelAdapter() {
		@Override
		public void collectableFieldChangedInModel(CollectableComponentModelEvent ev) {
			if (ev.collectableFieldHasChanged()) {
				final String sFieldName = ev.getCollectableComponentModel().getFieldName();
				log.debug("UsageCriteria field " + sFieldName + " changed in Search panel. New value: " + ev.getNewValue());
				UIUtils.runCommandLater(getFrame(), new Runnable() {
					@Override
					public void run() {
						try {
							reloadLayoutForSearchTab();
						}
						catch (CommonBusinessException ex) {
							Errors.getInstance().showExceptionDialog(getFrame(), CommonLocaleDelegate.getMessage("GenericObjectCollectController.16","Beim Nachladen eines Layouts ist ein Fehler aufgetreten."), ex);
						}
					}
				});
			}
		}
	};

	private final CollectableComponentModelListener ccmlistenerSearchChanged = new CollectableComponentModelAdapter() {
		@Override
		public void searchConditionChangedInModel(SearchComponentModelEvent ev) {
			assert GenericObjectCollectController.this.getCollectStateModel().getOuterState() == CollectStateConstants.OUTERSTATE_SEARCH;

			// Note that we want to call "searchChanged()" on every change, not only valid changes:
			GenericObjectCollectController.this.searchChanged(ev.getSearchComponentModel());
		}
	};

	private final CollectableEventListener collectableEventListener = new CollectableEventListener() {
		@Override
		public void handleCollectableEvent(Collectable collectable, MessageType messageType) {
			switch(messageType) {
			case REFRESH_DONE:
				resetTransferedDetailsData();
				break;
			case SAVE_DONE:
				resetTransferedDetailsData();
				break;
			}
		}
	};

	private static int iFilter;

	private Integer iSearchDeleted = CollectableGenericObjectSearchExpression.SEARCH_UNDELETED;
	protected boolean bUseInvalidMasterData = false;

	private final Integer iModuleId;
	private final CollectPanel<CollectableGenericObjectWithDependants> pnlCollect = newCollectPanel();

	private boolean bGenerated = false;
	private Integer iGenericObjectIdSource;

	private final GenericObjectDelegate lodelegate = GenericObjectDelegate.getInstance();
	private final GeneratorDelegate generatordelegate = GeneratorDelegate.getInstance();
	//protected final JButton btnDeletePhysicallyInDetails = new JButton();
	protected final JMenuItem btnDeletePhysicallyInDetails = new JMenuItem();
	protected final JMenuItem btnDeletePhysicallyInResult = new JMenuItem();
	//protected final JComboBox cmbbxSearchDeleted = new JComboBox();
	protected final ButtonGroup btnGrpSearchDeleted = new ButtonGroup();
	protected final JRadioButtonMenuItem[] miSearchDeleted = new JRadioButtonMenuItem[]{
		new JRadioButtonMenuItem(CommonLocaleDelegate.getMessage("GenericObjectCollectController.59","Nur ungel\u00f6schte suchen")),
		new JRadioButtonMenuItem(CommonLocaleDelegate.getMessage("GenericObjectCollectController.58","Nur gel\u00f6schte suchen")),
		new JRadioButtonMenuItem(CommonLocaleDelegate.getMessage("GenericObjectCollectController.49","Gel\u00f6schte und ungel\u00f6schte suchen"))
	};

	private final JCheckBoxMenuItem chkbxUseInvalidMasterData = new JCheckBoxMenuItem();
	//private final JButton btnMakeTreeRoot = new JButton();
	private final JMenuItem btnMakeTreeRoot = new JMenuItem();
	//private final JButton btnShowStateHistory = new JButton();
	private final JMenuItem btnShowStateHistory = new JMenuItem();
	//private final JButton btnShowLogBook = new JButton();
	private final JMenuItem btnShowLogBook = new JMenuItem();
	private final JButton btnPrintDetails = new JButton();
	//private final JButton btnResetViewToTemplateUser = new JButton();
	private final JMenuItem btnResetViewToTemplateUser = new JMenuItem();
	protected final JButton btnPrintResults = new JButton();
	//private final JButton btnExecuteRule = new JButton();
	private final JMenuItem btnExecuteRule = new JMenuItem();
	//private final JToolBar toolbarFixCustomDetails = UIUtils.createNonFloatableToolBar();
	//protected final JButton btnShowResultInExplorer = new JButton();
	protected final JMenuItem btnShowResultInExplorer = new JMenuItem();
	private final JComboBox cmbbxCurrentState = new JComboBox();
	//private final JToolBar toolbarCustomActionsDetails = UIUtils.createNonFloatableToolBar();
	private final List<Component> toolbarCustomActionsDetails = new ArrayList<Component>();
	private int toolbarCustomActionsDetailsIndex = -1;
	private LayoutRoot layoutrootDetails;

	private static final Color colorHistoricalNotTracked = Utils.translateColorFromParameter(ParameterProvider.KEY_HISTORICAL_STATE_NOT_TRACKED_COLOR);
	private static final Color colorHistoricalChanged = Utils.translateColorFromParameter(ParameterProvider.KEY_HISTORICAL_STATE_CHANGED_COLOR);

	/**
	 * Map<String sEntityName, DetailsSubFormController>
	 */
	private Map<String, DetailsSubFormController<CollectableEntityObject>> mpsubformctlDetails;

	/**
	 * Map<String sEntityName, SearchConditionSubFormController>
	 */

	private Date dateHistorical;
	//private boolean bEnteringHistoricalMode = false;

	private final CollectableEntityField clctefHistoricalDate = new DefaultCollectableEntityField("historicalDate", Date.class,
		null, null, null, null, true, CollectableField.TYPE_VALUEFIELD, null, null);

	private final CollectableDateChooser clctdatechooserHistorical = new CollectableDateChooser(clctefHistoricalDate, false);

	private final CollectableComponentModelListener documentlistenerHistoricalDateChooser = new CollectableComponentModelAdapter() {
		@Override
		public void collectableFieldChangedInModel(CollectableComponentModelEvent ev) {
			cmdHistoricalDateChanged();
		}
	};

	private ProxyList<CollectableGenericObjectWithDependants> proxylstclct;

	/**
	 * shortcut for performance. After update, the layout needn't be reloaded if the quintuple fields didn't change.
	 */
	protected boolean bReloadLayout = true;

	/**
	 * avoids recursively calling reloadLayout
	 */
	private boolean bReloadingLayout = false;

	/**
	 * remember adding/removing quintuple field listeners
	 */
	private boolean[] abUsageCriteriaFieldListenersAdded = new boolean[2];

	/**
	 * remember if current record is readable/writable
	 */
	private Object lockCurrRecReadable = new Object();
	private Boolean blnCurrentRecordReadable = null;
	private Object lockCurrRecWritable = new Object();
	private Boolean blnCurrentRecordWritable = null;

	private Map<String, DetailsComponentModel> transferredDetailsData = new HashMap<String, DetailsComponentModel>();

	/**
	 * action: Delete selected Collectable (in Result panel)
	 */
	final Action actDeleteSelectedCollectablesPhysically = new CommonAbstractAction(CommonLocaleDelegate.getMessage("GenericObjectCollectController.11","Ausgew\u00e4hlte Datens\u00e4tze endg\u00fcltig l\u00f6schen"),
		Icons.getInstance().getIconRealDelete16(), CommonLocaleDelegate.getMessage("GenericObjectCollectController.11","Ausgew\u00e4hlte Datens\u00e4tze endg\u00fcltig l\u00f6schen")) {
		/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent ev) {
			cmdDeleteSelectedCollectablesPhysically();
		}
	};

	final Action actDeleteSelectedCollectables = new CommonAbstractAction(CommonLocaleDelegate.getMessage("GenericObjectCollectController.52","L\u00f6schen..."),
		Icons.getInstance().getIconDelete16(), CommonLocaleDelegate.getMessage("GenericObjectCollectController.12","Ausgew\u00e4hlte Datens\u00e4tze l\u00f6schen")) {
		/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent ev) {
			cmdDeleteSelectedCollectables();
		}
	};

	final Action actDeleteCurrentCollectableInDetails = new CommonAbstractAction(CommonLocaleDelegate.getMessage("GenericObjectCollectController.53","L\u00f6schen..."),
		Icons.getInstance().getIconDelete16(), CommonLocaleDelegate.getMessage("GenericObjectCollectController.37","Diesen Datensatz l\u00f6schen")) {
		/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent ev) {
			cmdDeleteCurrentCollectableInDetails();
		}
	};

	final Action actRestoreSelectedCollectables = new CommonAbstractAction(CommonLocaleDelegate.getMessage("GenericObjectCollectController.98","Wiederherstellen..."),
		Icons.getInstance().getIconDelete16(), CommonLocaleDelegate.getMessage("GenericObjectCollectController.13","Ausgew\u00e4hlte Datens\u00e4tze wiederherstellen")) {
		/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent ev) {
			cmdRestoreSelectedCollectables();
		}
	};

	final Action actRestoreCurrentCollectableInDetails = new CommonAbstractAction(CommonLocaleDelegate.getMessage("GenericObjectCollectController.99","Wiederherstellen"),
		Icons.getInstance().getIconDelete16(), CommonLocaleDelegate.getMessage("GenericObjectCollectController.38","Diesen Datensatz wiederherstellen")) {
		/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent ev) {
			cmdRestoreCurrentCollectableInDetails();
		}
	};

	private MultiUpdateOfDependants multiupdateofdependants;

	/**
	 * controller for search result templates
	 * 
	 * @deprecated Move to GenericObjectResultController.
	 */
	private SearchResultTemplateController searchResultTemplatesController;

	/**
	 * Use the static method <code>newGenericObjectCollectController</code> to create new instances.
	 * 
	 * @param parent
	 * @param iModuleId
	 * @param bAutoInit TODO
	 * 
	 * @deprecated You should normally do sth. like this:<code><pre>
	 * ResultController<~> rc = new ResultController<~>();
	 * *CollectController<~> cc = new *CollectController<~>(.., rc);
	 * </code></pre>
	 */
	public GenericObjectCollectController(JComponent parent, Integer iModuleId, boolean bAutoInit, MainFrameTab tabIfAny) {
		super(parent, CollectableGenericObjectEntity.getByModuleId(iModuleId), new GenericObjectResultController<CollectableGenericObjectWithDependants>());
		this.iModuleId = iModuleId;
		setCompleteCollectablesStrategy(new CompleteGenericObjectsStrategy());
		final MainFrameTab frame = tabIfAny != null ? tabIfAny : newInternalFrame();
		frame.setLayeredComponent(pnlCollect);
		if (bAutoInit)
			init();

		addCollectableEventListener(collectableEventListener);
		setInternalFrame(frame, tabIfAny==null);
	}

	public GenericObjectCollectController(JComponent parent, Integer iModuleId, boolean bAutoInit, 
			MainFrameTab tabIfAny, ResultController<CollectableGenericObjectWithDependants> rc) 
	{
		super(parent, CollectableGenericObjectEntity.getByModuleId(iModuleId), rc);
		this.iModuleId = iModuleId;
		setCompleteCollectablesStrategy(new CompleteGenericObjectsStrategy());
		final MainFrameTab frame = tabIfAny != null ? tabIfAny : newInternalFrame();
		frame.setLayeredComponent(pnlCollect);
		if (bAutoInit)
			init();

		addCollectableEventListener(collectableEventListener);
		setInternalFrame(frame, tabIfAny==null);
	}
	
	/**
	 * @deprecated Move to GenericObjectResultController.
	 */
	public SearchResultTemplateController getSearchResultTemplateController() {
		return searchResultTemplatesController;
	}

	/**
	 * refactoring to avoid dirty use of SwingUtilities.invokeLater()
	 * allows local variable initialization of extenders [FS]
	 *
	 */
	protected void init() {
		initialize(pnlCollect);

		MainFrameTab frame = getFrame();
		setupEditPanels();
		setupKeyActionsForResultPanelVerticalScrollBar();
		setupShortcutsForTabs(frame);
		setupToolbars();

		setupResultTableHeaderRenderer();
		setupAdditionalActions();
		setupDataTransfer();
		getCollectStateModel().addCollectStateListener(new GenericObjectCollectStateListener());

		this.getResultPanel().popupmenuRow.addPopupMenuListener(new PopupMenuListener() {

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {

				try {
					final Integer iProcessId = GenericObjectCollectController.this.getSelectedGenericObjectsCommonFieldIdByFieldName(NuclosEOField.PROCESS.getMetaData().getField());
					final Integer iStateNumeral = GenericObjectCollectController.this.getSelectedGenericObjectsCommonStateNumeral();
					final List<GeneratorActionVO> lstActions = GeneratorActions.getActions(iModuleId, iStateNumeral, iProcessId);

					JMenu mi = GenericObjectCollectController.this.getResultPanel().miGenerations;
					mi.setVisible(lstActions.size() != 0);
					for(final GeneratorActionVO actionVO : lstActions) {
						JMenuItem action = new JMenuItem(new AbstractAction(actionVO.toString()) {

							/**
							 * 
							 */
							private static final long serialVersionUID = 1L;

							@Override
							public void actionPerformed(ActionEvent e) {
								final AtomicReference<Integer> parameterObjectIdRef = new AtomicReference<Integer>();
								if(GenericObjectCollectController.this.getSelectedCollectables().size() == 1){

									try {
										generateGenericObjectsFromSingleSources(parameterObjectIdRef.get(), actionVO);
									}
									catch(CommonBusinessException ex) {
										throw new NuclosFatalException(ex);
									}
								}
								else {
									GenericObjectCollectController.this.generateGenericObjectFromMultipleSources(parameterObjectIdRef.get(), actionVO);
								}
							}

						});
						mi.add(action);
					}
				}
				catch(Exception ex) {
					GenericObjectCollectController.this.getResultPanel().miGenerations.setVisible(false);
				}

			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				clearGeneratorMenu();
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
				clearGeneratorMenu();
			}

			private void clearGeneratorMenu() {
				 GenericObjectCollectController.this.getResultPanel().miGenerations.removeAll();
			}

		});

	}

	private void setupToolbars() {
		if(this.isSearchPanelAvailable())
			setupSearchToolBar();
		setupResultToolBar();
		setupDetailsToolBar();
	}

	@Override
	protected void setupSearchToolBar() {
		// additional functionality in Search panel:
		//final JPanel pnlCustomToolBarAreaSearch = new JPanel();
		//pnlCustomToolBarAreaSearch.setLayout(new GridBagLayout());

		super.setupSearchToolBar();

		//final JToolBar toolbarSearchCustom = newCustomSearchToolBar();
		if (SecurityCache.getInstance().isActionAllowed(Actions.ACTION_READ_DELETED_RECORD)) {
			//toolbarSearchCustom.add(Box.createHorizontalStrut(5));

			//cmbbxSearchDeleted.addItem(CommonLocaleDelegate.getMessage("GenericObjectCollectController.59","Nur ungel\u00f6schte suchen"));
			//cmbbxSearchDeleted.addItem(CommonLocaleDelegate.getMessage("GenericObjectCollectController.58","Nur gel\u00f6schte suchen"));
			//cmbbxSearchDeleted.addItem(CommonLocaleDelegate.getMessage("GenericObjectCollectController.49","Gel\u00f6schte und ungel\u00f6schte suchen"));

			btnGrpSearchDeleted.add(miSearchDeleted[0]);
			btnGrpSearchDeleted.add(miSearchDeleted[1]);
			btnGrpSearchDeleted.add(miSearchDeleted[2]);
			btnGrpSearchDeleted.setSelected(miSearchDeleted[0].getModel(), true);

			//UIUtils.setMaximumSizeToPreferredSize(cmbbxSearchDeleted);
			//toolbarSearchCustom.add(cmbbxSearchDeleted);
			//this.getSearchPanel().addToolBarComponent(cmbbxSearchDeleted);
			this.getSearchPanel().addPopupExtraSeparator();
			this.getSearchPanel().addPopupExtraMenuItem(miSearchDeleted[0]);
			this.getSearchPanel().addPopupExtraMenuItem(miSearchDeleted[1]);
			this.getSearchPanel().addPopupExtraMenuItem(miSearchDeleted[2]);

			/*cmbbxSearchDeleted.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent ev) {
					if (ev.getStateChange() == ItemEvent.SELECTED)
						iSearchDeleted = cmbbxSearchDeleted.getSelectedIndex();
				}
			});*/
			ItemListener il = new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent ev) {
					if (ev.getStateChange() == ItemEvent.SELECTED) {
						for (int i = 0; i < miSearchDeleted.length; i++) {
							if (ev.getSource() == miSearchDeleted[i])
								iSearchDeleted = i;
						}
					}
				}
			};
			miSearchDeleted[0].addItemListener(il);
			miSearchDeleted[1].addItemListener(il);
			miSearchDeleted[2].addItemListener(il);
		}

		refreshFilterView();

		// glue:
		//final JToolBar toolbarGlue = UIUtils.createNonFloatableToolBar();

		// add toolbars to custom toolbar area:
		/*final GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.VERTICAL;
		gbc.weightx = 0.0;
		gbc.weighty = 1.0;
		pnlCustomToolBarAreaSearch.add(toolbarSearchCustom, gbc);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		pnlCustomToolBarAreaSearch.add(toolbarGlue, gbc);
		gbc.fill = GridBagConstraints.VERTICAL;
		gbc.weightx = 0.0;
		gbc.weighty = 1.0;
		JToolBar toolSearchState = UIUtils.createNonFloatableToolBar();
*/
		JComboBox jComboBox = clctSearchState.getJComboBox();
		//UIUtils.setMaximumSizeToPreferredSize(jComboBox);
		jComboBox.setToolTipText(CommonLocaleDelegate.getMessage("GenericObjectCollectController.8","Aktueller Status"));
		//jComboBox.setOpaque(false);
		//CenteringPanel cpSearchState = new CenteringPanel(jComboBox);
		//cpSearchState.setOpaque(false);
		//toolSearchState.add(cpSearchState);
		//toolSearchState.add(Box.createHorizontalStrut(5));
		this.getSearchPanel().addToolBarComponent(new BlackLabel(jComboBox, CommonLocaleDelegate.getMessage("nuclos.entityfield.eo.state.label","Status")), 6);

		//pnlCustomToolBarAreaSearch.add(toolSearchState, gbc);
		setSearchStatesAccordingToUsageCriteria(new UsageCriteria(iModuleId, null));
		//getSearchPanel().setCustomToolBarArea(pnlCustomToolBarAreaSearch);
	}

	/**
	 * sets up a modified renderer for the result table header that uses the HTML formatted toString() value of the
	 * column's entity field rather than its unformatted getLabel() value.
	 */
	private void setupResultTableHeaderRenderer() {
		final JTableHeader header = getResultTable().getTableHeader();
		final TableCellRenderer headerrendererDefault = header.getDefaultRenderer();
		header.setDefaultRenderer(new TableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable tbl, Object oValue, boolean bSelected, boolean bHasFocus, int iRow, int iColumn) {
				final CollectableEntityField clctef = GenericObjectCollectController.this.getResultTableModel().getCollectableEntityField(tbl.convertColumnIndexToModel(iColumn));
				final Object oFormattedValue = clctef.toString();
				return headerrendererDefault.getTableCellRendererComponent(tbl, oFormattedValue, bSelected, bHasFocus, iRow, iColumn);
			}
		});
	}

	@Override
	protected void close() {
		closeSubFormControllersInSearch();
		closeSubFormControllersInDetails();

		super.close();
	}

	private void setupKeyActionsForResultPanelVerticalScrollBar() {
		// maps the default key strokes for JTable to set the vertical scrollbar, so we can intervent:
		final InputMap inputmap = getResultTable().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		inputmap.put(KeyStroke.getKeyStroke(KeyEvent.VK_END, InputEvent.CTRL_MASK), "last");
		inputmap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "nextrow");
		inputmap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0), "nextpage");

		final JScrollBar scrlbarVertical = getResultPanel().getResultTableScrollPane().getVerticalScrollBar();
		final DefaultBoundedRangeModel model = (DefaultBoundedRangeModel) scrlbarVertical.getModel();

		getResultTable().getActionMap().put("last", new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent ev) {
				final int iSupposedValue = model.getMaximum() - model.getExtent();
				model.setValue(iSupposedValue);
				// this causes the necessary rows to be loaded. Loading may be cancelled by the user.
				log.debug("NOW it's time to select the row...");
				if (model.getValue() == iSupposedValue)
					getCollectNavigationModel().selectLastElement();
			}
		});

		getResultTable().getActionMap().put("nextrow", new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent ev) {
				final int iSelectedRow = getResultTable().getSelectedRow();
				final int iLastVisibleRow = TableUtils.getLastVisibleRow(getResultTable());
				if (iSelectedRow + 1 < iLastVisibleRow) {
					// next row is still visible: just select it:
					if (!getCollectNavigationModel().isLastElementSelected())
						getCollectNavigationModel().selectNextElement();
				}
				else {
					// we have to move the viewport before we can select the next row:
					final int iSupposedValue = Math.min(model.getValue() + getResultTable().getRowHeight(),
						model.getMaximum() - model.getExtent());
					model.setValue(iSupposedValue);
					// this causes the necessary rows to be loaded. Loading may be cancelled by the user.
					log.debug("NOW it's time to select the row...");
					if (model.getValue() == iSupposedValue)
						if (!getCollectNavigationModel().isLastElementSelected())
							getCollectNavigationModel().selectNextElement();
				}
			}
		});

		getResultTable().getActionMap().put("nextpage", new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent ev) {
				final int iSupposedValue = Math.min(model.getValue() + model.getExtent(), model.getMaximum() - model.getExtent());
				model.setValue(iSupposedValue);
				// this causes the necessary rows to be loaded. Loading may be cancelled by the user.
				log.debug("NOW it's time to select the row...");
				if (model.getValue() == iSupposedValue) {
					final int iShiftRowCount = (int) Math.ceil((double) model.getExtent() / (double) getResultTable().getRowHeight());
					final int iRow = Math.min(getResultTable().getSelectionModel().getAnchorSelectionIndex() + iShiftRowCount,
						getResultTable().getRowCount() - 1);
					getResultTable().setRowSelectionInterval(iRow, iRow);
				}
			}
		});

		final Action actShowLogBook = new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent ev) {
				cmdShowLogBook();
				getDetailsPanel().grabFocus();
			}
		};
		KeyBindingProvider.bindActionToComponent(KeyBindingProvider.SHOW_LOGBOOK, actShowLogBook, getDetailsPanel());

		final Action actShowStateHistory = new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent ev) {
				cmdShowStateHistory();
				getDetailsPanel().grabFocus();
			}
		};
		KeyBindingProvider.bindActionToComponent(KeyBindingProvider.SHOW_STATE_HISTORIE, actShowStateHistory, getDetailsPanel());

		final Action actPrintCurrentGenericObject = new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent ev) {
				cmdPrintCurrentGenericObject();
				getDetailsPanel().grabFocus();
			}
		};
		KeyBindingProvider.bindActionToComponent(KeyBindingProvider.PRINT_LEASED_OBJECT, actPrintCurrentGenericObject, getDetailsPanel());
	}

	/**
	 * enables drag and copy from rows.
	 */
	private void setupDataTransfer() {
		// enable drag&drop:
		prepareTableForDragAndDrop(getResultPanel().getResultTable());
		prepareTableForDragAndDrop((getResultPanel()).getFixedResultTable());
	}

	private void prepareTableForDragAndDrop(final JTable tbl) {
		tbl.setDragEnabled(true);
		tbl.setTransferHandler(new TransferHandler() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public int getSourceActions(JComponent comp) {
				int result = NONE;
				if (comp == tbl)
					if (GenericObjectCollectController.this.getSelectedCollectable() != null)
						result = COPY;
				return result;
			}

			@Override
			protected Transferable createTransferable(JComponent comp) {
				Transferable result = null;
				if (comp == tbl) {
					final int[] aiSelectedRows = tbl.getSelectedRows();

					final List<GenericObjectIdModuleProcess> lstgoimp = new ArrayList<GenericObjectIdModuleProcess>(aiSelectedRows.length);
					for (int iSelectedRow : aiSelectedRows) {
						final CollectableGenericObjectWithDependants clct = getResultTableModel().getCollectable(iSelectedRow);
						GenericObjectIdModuleProcess transferableObject = getTransferableObject(tbl, iSelectedRow, clct);
						lstgoimp.add(transferableObject);
					}
					if (!lstgoimp.isEmpty())
						result = new TransferableGenericObjects(lstgoimp);
				}
				return result;
			}

			private GenericObjectIdModuleProcess getTransferableObject(
				final JTable tbl, int iSelectedRow,
				final CollectableGenericObjectWithDependants clct) {
				return new GenericObjectIdModuleProcess(clct, GenericObjectCollectController.getTreeViewIdentifier(clct), getContents(tbl, iSelectedRow));
			}

			private String getContents(JTable tbl, int iRow) {
				final StringBuffer sb = new StringBuffer();
				final int iColumnCount = tbl.getColumnCount();

				for (int iColumn = 0; iColumn < iColumnCount; iColumn++) {
					final String sValue = tbl.getValueAt(iRow, iColumn).toString();
					if (sValue.indexOf("\n") >= 0 || sValue.indexOf("\t") >= 0) {
						sb.append("\"");
						sb.append(sValue.replaceAll("\"", "\"\""));
						sb.append("\"");
					}
					else{
						sb.append(sValue);
					}
					if (iColumn < iColumnCount - 1)
						sb.append("\t");
				}
				return sb.toString();
			}
		});
	}

	protected final boolean isHistoricalView() {
		return dateHistorical != null;
	}

	protected void setHistoricalDate(Date dateHistorical) {
		this.dateHistorical = dateHistorical;

		updateHistoricalDateChooser(dateHistorical);
	}

	protected CollectableDateChooser getHistoricalCollectableDateChooser() {
		return clctdatechooserHistorical;
	}

	private void updateHistoricalDateChooser(final Date date) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				synchronized (GenericObjectCollectController.this) {
					unregisterHistoricalDateChooserListener();
					clctdatechooserHistorical.getDateChooser().setDate(date);
					registerHistoricalDateChooserListener();
				}
			}
		});
	}

	private void registerHistoricalDateChooserListener() {
		clctdatechooserHistorical.getModel().addCollectableComponentModelListener(documentlistenerHistoricalDateChooser);
	}

	private void unregisterHistoricalDateChooserListener() {
		clctdatechooserHistorical.getModel().removeCollectableComponentModelListener(documentlistenerHistoricalDateChooser);
	}

	private synchronized void cmdHistoricalDateChanged() {
		boolean bDateValid;
		Date dateHistorical = null;
		try {
			dateHistorical = (Date) clctdatechooserHistorical.getField().getValue();
			if (dateHistorical == null && getHistoricalCollectableDateChooser().getDateChooser().getDate() != null)
				dateHistorical = getHistoricalCollectableDateChooser().getDateChooser().getDate();
			bDateValid = true;
		}
		catch (CommonValidationException ex) {
			bDateValid = false;
		}

		if (bDateValid) {
			// if the historical date is greater than "today", set it to null,
			// in order to show the current object:
			if(dateHistorical != null)
				if(DateUtils.getPureDate(dateHistorical).compareTo(DateUtils.getPureDate(new Date())) >= 0)
					dateHistorical = null;

			if (!LangUtils.equals(this.dateHistorical, dateHistorical)) {
				final Date dateLastValid = this.dateHistorical;
				setHistoricalDate(dateHistorical);

				UIUtils.runCommandLater(getFrame(), new CommonRunnable() {
					@Override
					public void run() throws CommonBusinessException {
						final CollectableGenericObjectWithDependants clct;
						if (GenericObjectCollectController.this.dateHistorical == null)
							clct = readSelectedCollectable();
						else {
							final GenericObjectWithDependantsVO lowdcvoHistorical;
							try {
								assert GenericObjectCollectController.this.dateHistorical != null;
								lowdcvoHistorical = lodelegate.getHistorical(
									getSelectedGenericObjectId(), GenericObjectCollectController.this.dateHistorical);

								// remember if the layout needs to be reloaded afterwards:
								if (lowdcvoHistorical.getUsageCriteria(AttributeCache.getInstance()).equals(getUsageCriteria(getSelectedCollectable())))
									//GenericObjectCollectController.this.bReloadLayout = false;
									removeUsageCriteriaFieldListeners(false);

							}
							catch (CommonFinderException ex) {
								// "rollback":
								setHistoricalDate(dateLastValid);
								throw ex;
							}
							clct = new CollectableGenericObjectWithDependants(lowdcvoHistorical);
						}
						getResultController().replaceSelectedCollectableInTableModel(clct);
						cmdEnterViewMode();
						disableToolbarButtonsForHistoricalView();
					}
				});
			}
			else
				try {
					// check if we need to update the datechooser view:
					if (!LangUtils.equals(clctdatechooserHistorical.getDateChooser().getDate(), dateHistorical))
						updateHistoricalDateChooser(dateHistorical);
				}
			catch (CommonValidationException ex) {
				// Historical date chooser can contain only valid dates, as it is only possible to choose them from the popup; so this may not occur!
				throw new CommonFatalException(ex);
			}
		}
	}

	@Override
	protected boolean isSaveAllowed() {
		synchronized(lockCurrRecWritable) {
			blnCurrentRecordWritable = null;
		}
		return isCurrentRecordWritable() && isNotLoadingSubForms();
	}

	/**
	 * @return true
	 */
	@Override
	protected boolean isMultiEditAllowed() {
		return true;
	}

	private boolean isCurrentRecordReadable() {
		CollectableGenericObjectWithDependants clctgowd = getSelectedCollectable();

		synchronized(lockCurrRecReadable) {
			if (blnCurrentRecordReadable == null)
				blnCurrentRecordReadable = this.isReadAllowed(clctgowd);
			return blnCurrentRecordReadable;
		}
	}

	protected boolean isCurrentRecordWritable() {
		if(isHistoricalView() || isSelectedCollectableMarkedAsDeleted())
			return false;

		if(!MetaDataClientProvider.getInstance().getEntity(getEntityName()).isEditable())
			return false;
		final Integer iModuleIdSelected = getSelectedCollectableModuleId();
		final Integer iGenericObjectId = (Integer) getSelectedCollectableId();

		synchronized(lockCurrRecWritable) {
			if(blnCurrentRecordWritable == null)
				blnCurrentRecordWritable = SecurityCache.getInstance().isWriteAllowedForModule(
				    this.getEntityName(iModuleIdSelected), iGenericObjectId);

			if(!blnCurrentRecordWritable) {
				log.debug("Speichern nicht erlaubt fuer das Modul "
				    + getModuleLabel(iModuleIdSelected));
				return false;
			}
		}
		return true;
	}

	/**
	 * generic objects may deleted if we are not in historical view <code>AND</code>
	 * ((Delete is allowed for this module and the current user and the user has the right to write the object)
	 * <code>OR</code> this object is in its initial state and the current user created this object.)
	 * @param clct
	 * @return Is the "Delete" action for the given Collectable allowed?
	 */
	@Override
	protected boolean isDeleteAllowed(CollectableGenericObjectWithDependants clct) {
		final boolean result;

		if(isHistoricalView()) {
			log.debug("isDeleteAllowed: historical view");
			result = false;
		}
		else if(!MetaDataClientProvider.getInstance().getEntity(getEntityName()).isEditable())
			return false;
		else
			result = hasCurrentUserDeletionRights(clct, false);
		log.debug("isDeleteAllowed == " + result);

		return result;
	}

	/**
	 * @return Is the "Read" action for the given Collectable allowed? May be overridden by subclasses.
	 * @precondition clct != null
	 */
	@Override
	protected boolean isReadAllowed(CollectableGenericObjectWithDependants clct) {
		return SecurityCache.getInstance().isReadAllowedForModule(getEntityName(), (clct == null) ? null : clct.getId());
	}

	/**
	 * @return Is the "Read" action for the given set of Collectables allowed? May be overridden by subclasses.
	 * @precondition clct != null
	 */
	@Override
	protected boolean isReadAllowed(List <CollectableGenericObjectWithDependants> lsClct) {
		for (CollectableGenericObjectWithDependants collgowd: lsClct)
			if (!SecurityCache.getInstance().isReadAllowedForModule(getEntityName(), collgowd.getId()))
				return false;
		return true;
	}

	private boolean hasCurrentUserDeletionRights(CollectableGenericObject clct, boolean physically) {
		final GenericObjectVO govo = clct.getGenericObjectCVO();
		boolean result = false;
		if (getCollectStateModel().getOuterState() == CollectState.OUTERSTATE_RESULT)
			result = SecurityCache.getInstance().isDeleteAllowedForModule(this.getEntityName(), null, physically);
		else
			result = SecurityCache.getInstance().isDeleteAllowedForModule(this.getEntityName(), govo.getId(), physically);
		if (result)
			log.debug("isDeleteAllowed: delete allowed for module and permission is readwrite.");
		else {
			// objects in initial state may always be deleted by their creator:
			final String sCurrentUser = Main.getMainController().getUserName();
			final String sCreator = govo.getCreatedBy();
			log.debug("isDeleteAllowed: current user: " + sCurrentUser + " - creator: " + sCreator);

			if (sCurrentUser.equals(sCreator)) {
				final Integer iInitialStateId = getInitialStateId(getUsageCriteria(clct));
				final Integer iCurrentStateId = getSystemAttributeId(clct, NuclosEOField.STATE.getMetaData().getField());
				assert iInitialStateId != null;
				result = iInitialStateId.equals(iCurrentStateId);
				log.debug("isDeleteAllowed: current state: " + iCurrentStateId + " - initial state: " + iInitialStateId);
			}
		}
		return result;
	}

	protected boolean isPhysicallyDeleteAllowed(CollectableGenericObjectWithDependants clct) {
		boolean result = SecurityCache.getInstance().isActionAllowed(Actions.ACTION_DELETE_RECORD);
		if (clct != null)
			result = result && isDeleteAllowed(clct) && hasCurrentUserDeletionRights(clct, true);
		return result;
	}

	@Override
	public void setSearchDeleted(Integer iSearchDeleted) {
		if (SecurityCache.getInstance().isActionAllowed(Actions.ACTION_READ_DELETED_RECORD)) {
			this.iSearchDeleted = iSearchDeleted;
			//cmbbxSearchDeleted.setSelectedIndex(this.iSearchDeleted);	// Must not be set when user has no right (cmbbx is not filled then)!
			btnGrpSearchDeleted.setSelected(miSearchDeleted[this.iSearchDeleted].getModel(), true);
		}
		else
			this.iSearchDeleted = CollectableGenericObjectSearchExpression.SEARCH_UNDELETED;
	}

	@Override
	protected CollectableFieldsProviderFactory getCollectableFieldsProviderFactoryForSearchEditor() {
		return GenericObjectCollectableFieldsProviderFactory.newFactory(getCollectableEntity().getName(), valueListProviderCache);
	}

	/**
	 * This method is called by <code>cmdClearSearchFields</code>, that is when the user clicks
	 * the "Clear Search Fields" button. This implementation selects the default search filter.
	 */
	@Override
	protected void clearSearchCondition() {
		super.clearSearchCondition();
		// select the default filter (the first entry):
		selectDefaultFilter();
	}

	/**
	 * @return the search filters that can be edited in this collect controller.
	 *         By default, it's the search filters for this collect controller's module.
	 */
	@Override
	protected SearchFilters getSearchFilters() {
		return SearchFilters.forEntity(this.getEntityName());
	}

	@Override
	protected SearchFilter getCurrentSearchFilterFromSearchPanel() throws CommonBusinessException {
		final EntitySearchFilter result = (EntitySearchFilter)super.getCurrentSearchFilterFromSearchPanel();
		result.setSearchDeleted(iSearchDeleted);

		// set selected columns:
		result.setVisibleColumns(getSelectedFields());

		/** @todo set sorting column names */

		return result;
	}

	private void setupResultToolBar() {
		// additional functionality in Result panel:
		//final JToolBar toolbar = UIUtils.createNonFloatableToolBar();

		//toolbar.add(Box.createHorizontalStrut(5));
		getResultPanel().addPopupExtraSeparator();

		getResultPanel().addPopupExtraMenuItem(btnShowResultInExplorer);
		if (isPhysicallyDeleteAllowed(getSelectedCollectable())) {
			//toolbar.add(btnDeletePhysicallyInResult);
			//toolbar.add(Box.createHorizontalStrut(5));
			getResultPanel().addPopupExtraMenuItem(btnDeletePhysicallyInResult);
		}
		//toolbar.add(btnShowResultInExplorer);
		//toolbar.addSeparator();
		//toolbar.add(btnPrintResults);

		getResultPanel().addToolBarComponent(btnPrintResults);

		btnShowResultInExplorer.setIcon(Icons.getInstance().getIconTree16());
		btnShowResultInExplorer.setText(CommonLocaleDelegate.getMessage("GenericObjectCollectController.43","Ergebnis in Explorer anzeigen"));

		btnPrintResults.setIcon(Icons.getInstance().getIconPrintReport16());

		if(SecurityCache.getInstance().isActionAllowed(Actions.ACTION_PRINT_SEARCHRESULT)){
			btnPrintResults.setEnabled(true);
			btnPrintResults.setToolTipText(CommonLocaleDelegate.getMessage("GenericObjectCollectController.44","Ergebnisliste drucken"));
			// action: Print results
			btnPrintResults.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ev) {
					cmdPrint();
				}
			});
		} else {
			btnPrintResults.setEnabled(false);
			btnPrintResults.setToolTipText(CommonLocaleDelegate.getMessage("GenericObjectCollectController.45","Ergebnisliste drucken - Sie verf\u00fcgen nicht \u00fcber ausreichende Rechte."));
		}


		btnDeletePhysicallyInResult.setEnabled(true);
		btnDeletePhysicallyInResult.setAction(actDeleteSelectedCollectablesPhysically);
		//btnDeletePhysicallyInResult.setText(null);

		// action: show results in explorer:
		btnShowResultInExplorer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				cmdShowResultsInExplorer();
			}
		});

		searchResultTemplatesController = new SearchResultTemplateController(getResultPanel(), this);

		getResultTable().getSelectionModel().addListSelectionListener(deleteToggleResultListener);

		//toolbar.add(Box.createGlue());
		// adding reset to template user to toolbar in result view
		String templateUser = ClientParameterProvider.getInstance().getValue(ParameterProvider.KEY_TEMPLATE_USER);
		if (templateUser != null) {
			btnResetViewToTemplateUser.setText("Standardansicht wiederherstellen");
			btnResetViewToTemplateUser.addActionListener(new ResetToTemplateUserActionListener(this));
			getResultPanel().addPopupExtraMenuItem(btnResetViewToTemplateUser);
		}
		//getResultPanel().setCustomToolBarArea(toolbar);
	}

	/**
	 * Command: Delete selected <code>Collectable</code>s in the Result panel.
	 * This is mainly a copy of cmdDeleteSelectedCollectables from the ResultController, but with different messages and different actions.
	 */
	private void cmdDeleteSelectedCollectablesPhysically() {
		assert getCollectStateModel().getOuterState() == CollectState.OUTERSTATE_RESULT;
		assert CollectState.isResultModeSelected(getCollectStateModel().getResultMode());

		if (multipleCollectablesSelected()) {
			final int iCount = getResultTable().getSelectedRowCount();
			final String sMessage = CommonLocaleDelegate.getMessage("GenericObjectCollectController.81","Sollen die ausgew\u00e4hlten {0} Datens\u00e4tze wirklich endg\u00fcltig gel\u00f6scht werden?\nDieser Vorgang kann nicht r\u00fcckg\u00e4ngig gemacht werden!", iCount);
			final int btn = JOptionPane.showConfirmDialog(getFrame(), sMessage, CommonLocaleDelegate.getMessage("GenericObjectCollectController.20","Datens\u00e4tze endg\u00fcltig l\u00f6schen"), JOptionPane.YES_NO_OPTION);
			if (btn == JOptionPane.YES_OPTION)
				new DeleteSelectedCollectablesPhysicallyController(this).run(getMultiActionProgressPanel(iCount));
		}
		else {
			final String sMessage = CommonLocaleDelegate.getMessage("GenericObjectCollectController.76","Soll der ausgew\u00e4hlte Datensatz ({0}) wirklich endg\u00fcltig gel\u00f6scht werden?\nDieser Vorgang kann nicht r\u00fcckg\u00e4ngig gemacht werden!", getSelectedCollectable().getIdentifierLabel());
			final int btn = JOptionPane.showConfirmDialog(getFrame(), sMessage, CommonLocaleDelegate.getMessage("GenericObjectCollectController.23","Datensatz endg\u00fcltig l\u00f6schen"), JOptionPane.YES_NO_OPTION);

			if (btn == JOptionPane.YES_OPTION)
				UIUtils.runCommand(getFrame(), new Runnable() {
					@Override
					public void run() {
						try {
							checkedDeleteCollectablePhysically(getSelectedCollectable());
						}
						catch (CommonPermissionException ex) {
							final String sErrorMsg = "Sie verf\u00fcgen nicht \u00fcber die ausreichenden Rechte, um diesen Datensatz endg\u00fcltig zu l\u00f6schen.";
							Errors.getInstance().showExceptionDialog(getFrame(), sErrorMsg, ex);
						}
						catch (CommonBusinessException ex) {
							Errors.getInstance().showExceptionDialog(getFrame(), CommonLocaleDelegate.getMessage("GenericObjectCollectController.29","Der Datensatz konnte nicht endg\u00fcltig gel\u00f6scht werden."), ex);
						}
					}
				});
		}
	}

	/**
	 * Command: Delete selected <code>Collectable</code>s in the Result panel.
	 */
	private void cmdDeleteSelectedCollectables() {
		assert getCollectStateModel().getOuterState() == CollectState.OUTERSTATE_RESULT;
		assert CollectState.isResultModeSelected(getCollectStateModel().getResultMode());

		if (multipleCollectablesSelected()) {
			final int iCount = getResultTable().getSelectedRowCount();
			final String sMessage = CommonLocaleDelegate.getMessage("GenericObjectCollectController.82","Sollen die ausgew\u00e4hlten {0} Datens\u00e4tze wirklich gel\u00f6scht werden?", iCount);
			final int btn = JOptionPane.showConfirmDialog(getFrame(), sMessage, CommonLocaleDelegate.getMessage("GenericObjectCollectController.21","Datens\u00e4tze l\u00f6schen"), JOptionPane.YES_NO_OPTION);
			if (btn == JOptionPane.YES_OPTION)
				new DeleteSelectedCollectablesController<CollectableGenericObjectWithDependants>(this).run(getMultiActionProgressPanel(iCount));
			else if (btn == JOptionPane.NO_OPTION)
				getResultPanel().btnDelete.setSelected(false);
		}
		else {
			final String sMessage = CommonLocaleDelegate.getMessage("GenericObjectCollectController.77","Soll der ausgew\u00e4hlte Datensatz ({0}) wirklich gel\u00f6scht werden?",
				getSelectedCollectable().getIdentifierLabel());
			final int btn = JOptionPane.showConfirmDialog(getFrame(), sMessage, CommonLocaleDelegate.getMessage("GenericObjectCollectController.25","Datensatz l\u00f6schen"), JOptionPane.YES_NO_OPTION);

			if (btn == JOptionPane.YES_OPTION)
				UIUtils.runCommand(getFrame(), new Runnable() {
					@Override
					public void run() {
						try {
							checkedDeleteSelectedCollectable();
							//refreshResult();
						}
						catch (CommonPermissionException ex) {
							final String sErrorMsg = CommonLocaleDelegate.getMessage("GenericObjectCollectController.68","Sie verf\u00fcgen nicht \u00fcber die ausreichenden Rechte, um diesen Datensatz zu l\u00f6schen.");
							getResultPanel().btnDelete.setSelected(false);
							Errors.getInstance().showExceptionDialog(getFrame(), sErrorMsg, ex);
						}
						catch (CommonBusinessException ex) {
							getResultPanel().btnDelete.setSelected(false);
							//Errors.getInstance().showExceptionDialog(getFrame(), CommonLocaleDelegate.getMessage("GenericObjectCollectController.30","Der Datensatz konnte nicht gel\u00f6scht werden."), ex);
							Errors.getInstance().showExceptionDialog(getFrame(), CommonLocaleDelegate.getMessage("GenericObjectCollectController.30","Der Datensatz konnte nicht gel\u00f6scht werden."), new CommonRemoveException());
						}
					}
				});
			else if (btn == JOptionPane.NO_OPTION)
				getResultPanel().btnDelete.setSelected(false);
		}
	}

	@Override
	public CollectableSearchCondition getCollectableSearchCondition() throws CollectableFieldFormatException {
		return super.getCollectableSearchCondition();
	}

	private void cmdRestoreSelectedCollectables(){
		assert getCollectStateModel().getOuterState() == CollectState.OUTERSTATE_RESULT;
		assert CollectState.isResultModeSelected(getCollectStateModel().getResultMode());

		if (multipleCollectablesSelected()) {
			final int iCount = getResultTable().getSelectedRowCount();
			final String sMessage = CommonLocaleDelegate.getMessage("GenericObjectCollectController.83","Sollen die ausgew\u00e4hlten {0} Datens\u00e4tze wirklich wiederhergestellt werden?", iCount);
			final int btn = JOptionPane.showConfirmDialog(getFrame(), sMessage, CommonLocaleDelegate.getMessage("GenericObjectCollectController.22","Datens\u00e4tze wiederherstellen"), JOptionPane.YES_NO_OPTION);
			if (btn == JOptionPane.YES_OPTION)
				new RestoreSelectedCollectablesController(GenericObjectCollectController.this).run(getMultiActionProgressPanel(iCount));
			else if (btn == JOptionPane.NO_OPTION)
				getResultPanel().btnDelete.setSelected(true);
		}
		else {
			final String sMessage = CommonLocaleDelegate.getMessage("GenericObjectCollectController.78","Soll der ausgew\u00e4hlte Datensatz ({0}) wirklich wiederhergestellt werden?",
				getSelectedCollectable().getIdentifierLabel());
			final int btn = JOptionPane.showConfirmDialog(getFrame(), sMessage, CommonLocaleDelegate.getMessage("GenericObjectCollectController.27","Datensatz wiederherstellen"), JOptionPane.YES_NO_OPTION);

			if (btn == JOptionPane.YES_OPTION)
				UIUtils.runCommand(getFrame(), new Runnable() {
					@Override
					public void run() {
						try {
							checkedRestoreCollectable(getSelectedCollectable());
							refreshResult();
						}
						catch (CommonPermissionException ex) {
							final String sErrorMsg = CommonLocaleDelegate.getMessage("GenericObjectCollectController.66","Sie verf\u00fcgen nicht \u00fcber die ausreichenden Rechte, um diesen Datensatz wiederherzustellen.");
							getResultPanel().btnDelete.setSelected(true);
							Errors.getInstance().showExceptionDialog(getFrame(), sErrorMsg, ex);
						}
						catch (CommonBusinessException ex) {
							getResultPanel().btnDelete.setSelected(true);
							Errors.getInstance().showExceptionDialog(getFrame(), CommonLocaleDelegate.getMessage("GenericObjectCollectController.32","Der Datensatz konnte nicht wiederhergestellt werden."), ex);
						}
					}
				});
			else if (btn == JOptionPane.NO_OPTION)
				getResultPanel().btnDelete.setSelected(true);
		}
	}

	private void cmdRestoreCurrentCollectableInDetails() {
		assert getCollectStateModel().getCollectState().equals(new CollectState(CollectState.OUTERSTATE_DETAILS, CollectState.DETAILSMODE_VIEW));

		if (stopEditingInDetails()) {
			final String sMessage = CommonLocaleDelegate.getMessage("GenericObjectCollectController.75","Soll der angezeigte Datensatz ({0}) wirklich wiederhergestellt werden?", getSelectedCollectable().getIdentifierLabel());
			final int iBtn = JOptionPane.showConfirmDialog(getFrame(), sMessage, CommonLocaleDelegate.getMessage("GenericObjectCollectController.28","Datensatz wiederherstellen"), JOptionPane.YES_NO_OPTION);

			if (iBtn == JOptionPane.OK_OPTION)
				UIUtils.runCommand(getFrame(), new Runnable() {
					@Override
					public void run() {
						try {
							// try to find next or previous object:
							final JTable tblResult = getResultTable();
							final int iSelectedRow = tblResult.getSelectedRow();
							if (iSelectedRow < 0)
								throw new IllegalStateException();

							final int iNewSelectedRow;
							if (iSelectedRow < tblResult.getRowCount() - 1)
								// the selected row is not the last row: select the next row
								iNewSelectedRow = iSelectedRow;
							else if (iSelectedRow > 0)
								// the selected row is not the first row: select the previous row
								iNewSelectedRow = iSelectedRow - 1;
							else {
								// the selected row is the single row: don't select a row
								assert tblResult.getRowCount() == 1;
								assert iSelectedRow == 0;
								iNewSelectedRow = -1;
							}

							checkedRestoreCollectable(getSelectedCollectable());
							getResultTableModel().remove(getSelectedCollectable());
							search(true);

							if (iNewSelectedRow == -1) {
								tblResult.clearSelection();
								// switch to new mode:
								refreshResult();
							}
							else {
								tblResult.setRowSelectionInterval(iNewSelectedRow, iNewSelectedRow);
								// go into view mode again:
								cmdEnterViewMode();
							}
						}
						catch (CommonPermissionException ex) {
							final String sErrorMessage = CommonLocaleDelegate.getMessage("GenericObjectCollectController.67","Sie verf\u00fcgen nicht \u00fcber die ausreichenden Rechte, um diesen Datensatz wiederherzustellen.");
							Errors.getInstance().showExceptionDialog(getFrame(), sErrorMessage, ex);
						}
						catch (CommonBusinessException ex) {
							Errors.getInstance().showExceptionDialog(getFrame(), CommonLocaleDelegate.getMessage("GenericObjectCollectController.33","Der Datensatz konnte nicht wiederhergestellt werden."), ex);
						}
					}
				});
		}
		setDeleteButtonToggleInDetails();
	}

	private void cmdDeleteCurrentCollectableInDetails() {
		assert getCollectStateModel().getCollectState().equals(new CollectState(CollectState.OUTERSTATE_DETAILS, CollectState.DETAILSMODE_VIEW));

		if (stopEditingInDetails()) {
			final String sMessage = CommonLocaleDelegate.getMessage("GenericObjectCollectController.74","Soll der angezeigte Datensatz ({0}) wirklich gel\u00f6scht werden?", getSelectedCollectable().getIdentifierLabel());
			final int iBtn = JOptionPane.showConfirmDialog(getFrame(), sMessage, CommonLocaleDelegate.getMessage("GenericObjectCollectController.26","Datensatz l\u00f6schen"), JOptionPane.YES_NO_OPTION);

			if (iBtn == JOptionPane.OK_OPTION)
				UIUtils.runCommand(getFrame(), new Runnable() {
					@Override
					public void run() {
						try {
							// try to find next or previous object:
							final JTable tblResult = getResultTable();
							final int iSelectedRow = tblResult.getSelectedRow();
							if (iSelectedRow < 0)
								throw new IllegalStateException();

							final int iNewSelectedRow;
							if (iSelectedRow < tblResult.getRowCount() - 1)
								// the selected row is not the last row: select the next row
								iNewSelectedRow = iSelectedRow;
							else if (iSelectedRow > 0)
								// the selected row is not the first row: select the previous row
								iNewSelectedRow = iSelectedRow - 1;
							else {
								// the selected row is the single row: don't select a row
								assert tblResult.getRowCount() == 1;
								assert iSelectedRow == 0;
								iNewSelectedRow = -1;
							}

							checkedDeleteCollectable(getSelectedCollectable());
							search(true);

							if (iNewSelectedRow == -1) {
								tblResult.clearSelection();
								// switch to new mode:
								refreshResult();
							}
							else {
								tblResult.getSelectionModel().setSelectionInterval(iNewSelectedRow, iNewSelectedRow);
								// go into view mode again:
								cmdEnterViewMode();
							}
						}
						catch (CommonPermissionException ex) {
							final String sErrorMessage = CommonLocaleDelegate.getMessage("GenericObjectCollectController.69","Sie verf\u00fcgen nicht \u00fcber die ausreichenden Rechte, um diesen Datensatz zu l\u00f6schen.");
							Errors.getInstance().showExceptionDialog(getFrame(), sErrorMessage, ex);
						}
						catch (CommonBusinessException ex) {
							if (!handlePointerException(ex))
								Errors.getInstance().showExceptionDialog(getFrame(), CommonLocaleDelegate.getMessage("GenericObjectCollectController.31","Der Datensatz konnte nicht gel\u00f6scht werden."), ex);
						}
					}
				});
		}
		setDeleteButtonToggleInDetails();
	}

	/**
	 * @return the toolbar that contains the buttons in the fixed left part of the custom toolbar area).
	 *         Successors may add their own buttons here and should call revalidate() afterwards.
	 */
	/*protected final JToolBar getFixedCustomDetailsToolBar() {
		return toolbarFixCustomDetails;
	}*/

	private void setupDetailsToolBar() {
		final SecurityCache securitycache = SecurityCache.getInstance();

		// additional functionality in Details panel:
		//final JPanel pnlCustomToolBarAreaDetails = new JPanel();
		//pnlCustomToolBarAreaDetails.setLayout(new GridBagLayout());

		// fix custom buttons:
		btnMakeTreeRoot.setName("btnMakeTreeRoot");
		btnMakeTreeRoot.setIcon(Icons.getInstance().getIconMakeTreeRoot16());
		btnMakeTreeRoot.setText(CommonLocaleDelegate.getMessage("GenericObjectCollectController.50","In Explorer anzeigen"));
		btnMakeTreeRoot.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				cmdJumpToTree();
			}
		});
		//getFixedCustomDetailsToolBar().add(btnMakeTreeRoot);
		//this.getDetailsPanel().addToolBarComponent(btnMakeTreeRoot);
		this.getDetailsPanel().addPopupExtraMenuItem(btnMakeTreeRoot);

		if (securitycache.isActionAllowed(Actions.ACTION_DELETE_RECORD)) {
			btnDeletePhysicallyInDetails.setName("btnDeletePhysicallyInDetails");
			btnDeletePhysicallyInDetails.setIcon(Icons.getInstance().getIconRealDelete16());
			btnDeletePhysicallyInDetails.setText(CommonLocaleDelegate.getMessage("GenericObjectCollectController.60","Objekt endg\u00fcltig aus der Datenbank l\u00f6schen"));
			btnDeletePhysicallyInDetails.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ev) {
					cmdDeletePhysically();
				}
			});
			//getFixedCustomDetailsToolBar().add(btnDeletePhysicallyInDetails);
			//this.getDetailsPanel().addToolBarComponent(btnDeletePhysicallyInDetails);
			this.getDetailsPanel().addPopupExtraMenuItem(btnDeletePhysicallyInDetails);
		}

		// Execute rule by user only for authorized personnel
		if (securitycache.isActionAllowed(Actions.ACTION_EXECUTE_RULE_BY_USER)) {
			btnExecuteRule.setName("btnExecuteRule");
			btnExecuteRule.setText(CommonLocaleDelegate.getMessage("GenericObjectCollectController.62","Regeln ausf\u00fchren"));
			btnExecuteRule.setIcon(Icons.getInstance().getIconExecuteRule16());
			btnExecuteRule.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ev) {
					cmdExecuteRuleByUser(GenericObjectCollectController.this.getFrame(), GenericObjectCollectController.this.getEntityName(), GenericObjectCollectController.this.getSelectedCollectable());
				}
			});
			//getFixedCustomDetailsToolBar().add(btnExecuteRule);
			//this.getDetailsPanel().addToolBarComponent(btnExecuteRule);
			this.getDetailsPanel().addPopupExtraSeparator();
			this.getDetailsPanel().addPopupExtraMenuItem(btnExecuteRule);
			btnExecuteRule.setEnabled(securitycache.isActionAllowed(Actions.ACTION_EXECUTE_RULE_BY_USER));
		}



		if (SecurityCache.getInstance().isActionAllowed(Actions.ACTION_USE_INVALID_MASTERDATA)) {
			chkbxUseInvalidMasterData.setText(CommonLocaleDelegate.getMessage("GenericObjectCollectController.96","Ung\u00fcltige Stammdaten anzeigen?"));
			chkbxUseInvalidMasterData.setOpaque(false);
			//chkbxUseInvalidMasterData.setForeground(Color.white);
			chkbxUseInvalidMasterData.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					bUseInvalidMasterData = chkbxUseInvalidMasterData.isSelected();
					//loadSpecializedLayoutForDetails();
					Collection<CollectableComponent> collectableComponents = getDetailsPanel().getEditView().getCollectableComponents();
					for(CollectableComponent clcmp : collectableComponents){
						if(clcmp instanceof CollectableComponentWithValueListProvider){
							CollectableComponentWithValueListProvider clcmpWithVLP = (CollectableComponentWithValueListProvider)clcmp;
							CollectableFieldsProvider valueListProvider = clcmpWithVLP.getValueListProvider();
							if(valueListProvider instanceof ManagedCollectableFieldsProvider){
								((ManagedCollectableFieldsProvider)valueListProvider).setIgnoreValidity(bUseInvalidMasterData);
								clcmpWithVLP.setValueListProvider(valueListProvider);
								clcmpWithVLP.refreshValueList(false);
							}
						}
					}
				}
			});
//			getFixedCustomDetailsToolBar().add(Box.createHorizontalStrut(5));
//			getFixedCustomDetailsToolBar().add(chkbxUseInvalidMasterData);
//			getFixedCustomDetailsToolBar().add(Box.createHorizontalStrut(5));
			this.getDetailsPanel().addPopupExtraMenuItem(chkbxUseInvalidMasterData);
		}


		btnShowStateHistory.setName("btnShowStateHistory");
		btnShowStateHistory.setIcon(Icons.getInstance().getIconStateHistory16());
		btnShowStateHistory.setText(CommonLocaleDelegate.getMessage("GenericObjectCollectController.84","Statushistorie anzeigen"));
		btnShowStateHistory.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				cmdShowStateHistory();
			}
		});
		//getFixedCustomDetailsToolBar().add(btnShowStateHistory);
		//this.getDetailsPanel().addToolBarComponent(btnShowStateHistory);
		this.getDetailsPanel().addPopupExtraSeparator();
		this.getDetailsPanel().addPopupExtraMenuItem(btnShowStateHistory);

		//add the loogbook and historical components only if loogbook tracking is enabled in this module
		if(iModuleId != null && iModuleId > 0 && Modules.getInstance().isLogbookTracking(iModuleId)) {
			btnShowLogBook.setName("btnShowLogBook");
			btnShowLogBook.setIcon(Icons.getInstance().getIconLogBook16());
			btnShowLogBook.setText(CommonLocaleDelegate.getMessage("GenericObjectCollectController.54","Logbuch anzeigen"));
			btnShowLogBook.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ev) {
					cmdShowLogBook();
				}
			});
			//getFixedCustomDetailsToolBar().add(btnShowLogBook);
			//this.getDetailsPanel().addToolBarComponent(btnShowLogBook);
			this.getDetailsPanel().addPopupExtraMenuItem(btnShowLogBook);

			final DateChooser datechooserHistorical = clctdatechooserHistorical.getDateChooser();
			datechooserHistorical.setHistoricalState(true);

			this.getDetailsPanel().addPopupExtraComponent(new BlackLabel(datechooserHistorical, CommonLocaleDelegate.getMessage("GenericObjectCollectController.7","Aktueller/historischer Zustand")));
			registerHistoricalDateChooserListener();
		}

		cmbbxCurrentState.setName("cmbbxCurrentState");
		cmbbxCurrentState.setVisible(false);
		cmbbxCurrentState.setToolTipText(CommonLocaleDelegate.getMessage("GenericObjectCollectController.8","Aktueller Status"));

		toolbarCustomActionsDetailsIndex = this.getDetailsPanel().getToolBarNextIndex();

		this.getDetailsPanel().addToolBarComponent(btnPointer);
	}

	/**
	 * Reload layout when specialized flags (e.g. bUseInvalidMasterData) are set (method only for overriding).
	 */
	protected void loadSpecializedLayoutForDetails() {
		try {
			reloadLayoutForDetailsTab(true);
		}
		catch (CommonBusinessException ex) {
			throw new NuclosFatalException(ex);
		}
	}

	@Override
	public void executeBusinessRules(List<RuleVO> lstRuleVO, boolean bSaveAfterRuleExecution) throws CommonBusinessException{
		GenericObjectDelegate.getInstance().executeBusinessRules(lstRuleVO, getSelectedCollectable().getGenericObjectWithDependantsCVO(), bSaveAfterRuleExecution);
	}

	private final class ObservableSearchWorker extends Observable implements SearchWorker<CollectableGenericObjectWithDependants> {
		@Override
		public void startSearch() throws CommonBusinessException {
			/** @todo maybe this could be done already in the CollectController? */
			makeConsistent(true);

			removePreviousChangeListenersForResultTableVerticalScrollBar();
		}

		@Override
		public ProxyList<CollectableGenericObjectWithDependants> getResult() throws CommonBusinessException {
			return getSearchResult();
		}

		@Override
		public void finishSearch(List<CollectableGenericObjectWithDependants> lstclctResult) {
			setCollectableProxyList((ProxyList<CollectableGenericObjectWithDependants>) lstclctResult);

			fillResultPanel(lstclctResult, lstclctResult.size(), false);

			setupChangeListenerForResultTableVerticalScrollBar();
			super.setChanged();
			super.notifyObservers("search finished");
		}
	}

	public void setupAdditionalActions() {
		// extend popupmenu for rows in result panel:
		btnPrintDetails.setIcon(Icons.getInstance().getIconPrintReport16());
		btnPrintDetails.setToolTipText(CommonLocaleDelegate.getMessage("GenericObjectCollectController.48","Formular erzeugen / drucken"));
		btnPrintDetails.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				cmdPrintCurrentGenericObject();
			}
		});
	}

	/** @todo pull down to CollectController */
	protected CollectPanel<CollectableGenericObjectWithDependants> newCollectPanel() {
		boolean bSearch = MetaDataClientProvider.getInstance().getEntity(this.sEntity).isSearchable();
		return new GenericObjectCollectPanel(bSearch);
	}

	@Override
	public GenericObjectSearchPanel getSearchPanel() {
		return (GenericObjectSearchPanel) super.getSearchPanel();
	}

	@Override
	public GenericObjectDetailsPanel getDetailsPanel() {
		return (GenericObjectDetailsPanel) super.getDetailsPanel();
	}

	@Override
	public GenericObjectResultPanel getResultPanel() {
		return (GenericObjectResultPanel) super.getResultPanel();
	}

	private void setupEditPanels() {
		// get the layout for the Search and Details panels out of the Layout ML definition:
		if(this.isSearchPanelAvailable())
			setupEditPanelForSearchTab();
		setupEditPanelForDetailsTab();
	}

	private void setupEditPanelForSearchTab() {
		final LayoutRoot layoutrootSearch = getInitialLayoutMLDefinitionForSearchPanel();
		layoutrootSearch.getRootComponent().setFocusCycleRoot(true);
		getSearchPanel().setEditView(newSearchEditView(layoutrootSearch));
		// create a controller for each subform:
		closeSubFormControllersInSearch();
		Map<String, SubForm> mpSubForm = layoutrootSearch.getMapOfSubForms();
		mpsubformctlSearch = newSearchConditionSubFormControllers(mpSubForm);
		getSearchPanel().getEditView().setComponentsEnabled(true);
		this.addUsageCriteriaFieldListeners(true);
		setupSubFormController(mpSubForm, mpsubformctlSearch);
	}

	private void setupEditPanelForDetailsTab() {
		// Optimization: loading the details panel isn't necessary here, so we install dummies instead.
		final LayoutRoot layoutrootDetails = getInitialLayoutMLDefinitionForDetailsPanel();
		final JComponent compEdit = getDetailsPanel().newEditComponent(layoutrootDetails.getRootComponent());
		getDetailsPanel().setEditView(DefaultEditView.newDetailsEditView(compEdit, layoutrootDetails, layoutrootDetails.getInitialFocusEntityAndFieldName()));
		this.layoutrootDetails = layoutrootDetails;
		// create a controller for each subform (after closing the old ones):
		closeSubFormControllersInDetails();
		Map<String, SubForm> mpSubForm = layoutrootDetails.getMapOfSubForms();
		Map<String, DetailsSubFormController<CollectableEntityObject>> mpSubFormController = newDetailsSubFormControllers(mpSubForm);
		setMapOfSubFormControllersInDetails(mpSubFormController);
		setupSubFormController(mpSubForm, mpSubFormController);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void setupSubFormController(Map<String, SubForm> mpSubForm, Map<String, ? extends SubFormController> mpSubFormController) {
		Map<SubForm, MasterDataSubFormController> mpSubFormController_tmp = new HashMap<SubForm, MasterDataSubFormController>();

		// create a map of subforms and their controllers
		for (String sSubFormEntityName : mpSubFormController.keySet()) {
			SubFormController subformcontroller = mpSubFormController.get(sSubFormEntityName);
			SubForm subform = subformcontroller.getSubForm();
			if (subformcontroller instanceof DetailsSubFormController<?>) {
				((DetailsSubFormController<CollectableGenericObjectWithDependants>)subformcontroller).setCollectController(this);
				mpSubFormController_tmp.put(subform, (MasterDataSubFormController)subformcontroller);
			}
			// disable child subforms in searchpanel, because it's not possible to search for data in those subforms
			else if (subformcontroller instanceof SearchConditionSubFormController)
				if (subform.getParentSubForm() != null)
					subform.setEnabled(false);
		}

		// assign child subforms to their parents
		for (SubForm subform : mpSubFormController_tmp.keySet()) {
			SubForm parentsubform = mpSubForm.get(subform.getParentSubForm());
			if (parentsubform != null) {
				MasterDataSubFormController subformcontroller = mpSubFormController_tmp.get(parentsubform);
				subformcontroller.addChildSubFormController(mpSubFormController_tmp.get(subform));
			}
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void showFrame() {
		super.showFrame();

		setInitialComponentFocusInSearchTab();
	}

	private void setInitialComponentFocusInSearchTab() {
		Utils.setInitialComponentFocus(getSearchPanel().getEditView(), mpsubformctlSearch);
	}

	private void setInitialComponentFocusInDetailsTab() {
		Utils.setInitialComponentFocus(getDetailsPanel().getEditView(), mpsubformctlDetails);
	}

	/**
	 * @return Map<String sFieldName, DetailsComponentModel> the map of parsed/constructed collectable component models.
	 *         Maps a field name to a <code>DetailsComponentModel</code>.
	 * @todo move to LayoutRoot
	 */
	@SuppressWarnings("deprecation")
	private static Map<String, DetailsComponentModel> getMapOfDetailsComponentModels(LayoutRoot layoutroot) {
		return CollectionUtils.typecheck(layoutroot.getMapOfCollectableComponentModels(), DetailsComponentModel.class);
	}

	/**
	 * parses the LayoutML definition and gets the layout information
	 * @return the LayoutRoot containing the layout information
	 */
	@Override
	protected LayoutRoot getInitialLayoutMLDefinitionForSearchPanel() {
		return getLayoutFromCache(new UsageCriteria(getModuleId(), null),
			new CollectState(CollectState.OUTERSTATE_SEARCH, CollectState.SEARCHMODE_UNSYNCHED));
	}

	protected LayoutRoot getInitialLayoutMLDefinitionForDetailsPanel() {
		return LayoutRoot.newEmptyLayoutRoot(false);
	}

	/**
	 * creates a searchable subform ctl for each subform. If the subform is disabled, the controller will be disabled.
	 * @param mpSubForms
	 */
	@Override
	protected Map<String, SearchConditionSubFormController> newSearchConditionSubFormControllers(Map<String, SubForm> mpSubForms) {
		final String sParentEntityName = this.getEntityName();

		final SearchEditModel editmodelSearch = getSearchPanel().getEditModel();

		return CollectionUtils.transformMap(mpSubForms, new Transformer<SubForm, SearchConditionSubFormController>() {
			@Override
			public SearchConditionSubFormController transform(SubForm subform) {
				return newSearchConditionSubFormController(subform, sParentEntityName, editmodelSearch);
			}
		});
	}

	/**
	 * creates an editable subform ctl for each subform. If the subform is disabled, the controller will be disabled.
	 * @param mpSubForms
	 */
	protected Map<String, DetailsSubFormController<CollectableEntityObject>> newDetailsSubFormControllers(Map<String, SubForm> mpSubForms) {
		final Integer iParentId;
		final String sParentEntityName = this.getEntityName(getSelectedCollectableModuleId());

		final DetailsEditModel editmodelDetails = getDetailsPanel().getEditModel();
		if(getCollectState().isDetailsModeNew()) {
			iParentId = null;
		}
		else {
			iParentId = getSelectedGenericObjectId();
		}

		return CollectionUtils.transformMap(mpSubForms, new Transformer<SubForm, DetailsSubFormController<CollectableEntityObject>>() {
			@Override
			public DetailsSubFormController<CollectableEntityObject> transform(SubForm subform) {
				final DetailsSubFormController<CollectableEntityObject> result = newDetailsSubFormController(subform, sParentEntityName, editmodelDetails);
				result.setParentId(iParentId);
				return result;
			}
		});
	}

	/**
	 * @todo maybe move to CollectController?
	 * @param subform
	 * @param clctcompmodelprovider
	 */
	@Override
	protected SearchConditionSubFormController newSearchConditionSubFormController(SubForm subform, String sParentEntityName,
		CollectableComponentModelProvider clctcompmodelprovider) {

		final String sControllerType = subform.getControllerType();
		if (sControllerType != null && !sControllerType.equals("default"))
			log.warn("Kein spezieller SearchConditionSubFormController f?r Controllertyp " + sControllerType + " vorhanden.");
		return _newSearchConditionSubFormController(clctcompmodelprovider, sParentEntityName, subform);
	}

	@Override
	protected SearchConditionSubFormController _newSearchConditionSubFormController(CollectableComponentModelProvider clctcompmodelprovider,
		String sParentEntityName, SubForm subform) {

		// if parent of subform is another subform, change given parent entity name
		String sParentSubForm = subform.getParentSubForm();
		if (sParentSubForm != null)
			sParentEntityName = sParentSubForm;

		return new SearchConditionSubFormController(getFrame(), parent, clctcompmodelprovider, sParentEntityName, subform,
			getPreferences(), MasterDataCollectableFieldsProviderFactory.newFactory(null, valueListProviderCache));
	}

	/**
	 * @todo maybe pull down to CollectController?
	 * @param subform
	 * @param clctcompmodelprovider
	 * @postcondition result != null
	 */
	private MasterDataSubFormController newDetailsSubFormController(SubForm subform, String sParentEntityName,
		CollectableComponentModelProvider clctcompmodelprovider) {

		// if parent of subform is another subform, change given parent entity name
		String sParentSubForm = subform.getParentSubForm();
		if (sParentSubForm != null)
			sParentEntityName = sParentSubForm;

		MasterDataSubFormController result = newDetailsSubFormController(subform, sParentEntityName, clctcompmodelprovider, getFrame(), parent, getDetailsPanel(), getPreferences());

//		if (bUseInvalidMasterData)
//			result.setCollectableComponentFactory(new NuclosValidityTolerantCollectableComponentFactory());

		return result;
	}

	private void removeAdditionalChangeListeners(boolean bSearchable) {
		for (SubFormController subformctl : getSubFormControllers(bSearchable))
			subformctl.getSubForm().removeChangeListener(getChangeListener(bSearchable));
	}

	@Override
	protected void addAdditionalChangeListenersForSearch() {
		super.addAdditionalChangeListenersForSearch();
		clctSearchState.getSearchModel().addCollectableComponentModelListener(ccmlistenerSearchChanged);
	}

	@Override
	protected void removeAdditionalChangeListenersForSearch() {
		removeAdditionalChangeListeners(true);
		clctSearchState.getSearchModel().removeCollectableComponentModelListener(ccmlistenerSearchChanged);
	}

	@Override
	protected void addAdditionalChangeListenersForDetails() {
		addAdditionalChangeListeners(false);
	}

	@Override
	protected void removeAdditionalChangeListenersForDetails() {
		removeAdditionalChangeListeners(false);
	}

	@Override
	protected Collection<? extends SubFormController> getSubFormControllers(boolean bSearchTab) {
		return bSearchTab ? getSubFormControllersInSearch() : getSubFormControllersInDetails();
	}

	/**
	 * @return Map<String sEntity, DetailsSubFormController>. May be <code>null</code>.
	 */
	@Override
	protected Map<String, DetailsSubFormController<CollectableEntityObject>> getMapOfSubFormControllersInDetails() {
		return mpsubformctlDetails;
	}

	public void setMapOfSubFormControllersInDetails(Map<String, DetailsSubFormController<CollectableEntityObject>> mpSubFormControllersInDetails) {
		mpsubformctlDetails = mpSubFormControllersInDetails;
	}

	@Override
	protected Map<String, SearchConditionSubFormController> getMapOfSubFormControllersInSearch() {
		return this.mpsubformctlSearch;
	}

	/**
	 * @postcondition result != null
	 */
	private Collection<SearchConditionSubFormController> getSubFormControllersInSearch() {
		return CollectionUtils.valuesOrEmptySet(getMapOfSubFormControllersInSearch());
	}

	/**
	 * fills the subform controllers with collectablemasterdata found in the given DependantCollectableMasterDataMap
	 * @param mpDependants
	 * @throws NuclosBusinessException
	 */
	public void fillSubForm(DependantCollectableMasterDataMap mpDependants) throws NuclosBusinessException {
		for (String sEntityName : mpDependants.getEntityNames()) {
			DetailsSubFormController<CollectableEntityObject> sfcontroller = getMapOfSubFormControllersInDetails().get(sEntityName);
			if (sfcontroller instanceof MasterDataSubFormController)
				((MasterDataSubFormController)sfcontroller).fillSubForm(mpDependants.toDependantMasterDataMap().getData(sEntityName));
		}
	}

	/**
	 * @param bMakeConsistent
	 * @return the search condition contained in the search panel's fields (including the subforms' search fields).
	 * @precondition this.isSearchPanelAvailable()
	 * @postcondition result == null || result.isSyntacticallyCorrect()
	 */
	@Override
	protected CollectableSearchCondition getCollectableSearchConditionFromSearchFields(boolean bMakeConsistent) throws CollectableFieldFormatException {
		if (!isSearchPanelAvailable())
			throw new IllegalStateException("!this.isSearchPanelAvailable()");
		final CollectableSearchCondition cond = super.getCollectableSearchConditionFromSearchFields(bMakeConsistent);

		final CompositeCollectableSearchCondition condAnd = new CompositeCollectableSearchCondition(LogicalOperator.AND);
		if (cond != null)
			condAnd.addOperand(cond);

		assert getMapOfSubFormControllersInSearch() != null;
		for (SearchConditionSubFormController subformctl : getMapOfSubFormControllersInSearch().values()) {
			for(CollectableSearchCondition subCond : subformctl.getCollectableSubformSearchConditions()) {
				if(subCond != null)
					condAnd.addOperand(new CollectableSubCondition(subformctl.getCollectableEntity().getName(), subformctl.getForeignKeyFieldName(), subCond));
			}
			/*  Subcondition or old code do not delete please
			final CollectableSearchCondition condSub = subformctl.getCollectableSearchCondition();
			if (condSub != null)
				condAnd.addOperand(new CollectableSubCondition(subformctl.getCollectableEntity().getName(), subformctl.getForeignKeyFieldName(), condSub));
			*/
		}

		final CollectableSearchCondition result = SearchConditionUtils.simplified(condAnd);
		assert result == null || result.isSyntacticallyCorrect();
		return result;
	}

	/**
	 * @return the search condition to display. Includes the currently selected global search filter's search condition (if any).
	 * @throws CollectableFieldFormatException
	 */
	@Override
	protected CollectableSearchCondition getCollectableSearchConditionToDisplay() throws CollectableFieldFormatException {
		final CompositeCollectableSearchCondition compositecond = new CompositeCollectableSearchCondition(LogicalOperator.AND);

		final CollectableSearchCondition clctcond = super.getCollectableSearchConditionToDisplay();
		if (clctcond != null)
			compositecond.addOperand(clctcond);

		return SearchConditionUtils.simplified(compositecond);
	}

	/**
	 * @return the internal version of the collectable search condition, that is used for performing the actual search.
	 *         Includes the selected global search filter's internal search condition (if any).
	 * @throws CollectableFieldFormatException
	 */
	protected final CollectableSearchCondition getInternalSearchCondition() throws CollectableFieldFormatException {
		final CompositeCollectableSearchCondition compositecond = new CompositeCollectableSearchCondition(LogicalOperator.AND);

		final CollectableSearchCondition clctcond = GenericObjectClientUtils.getInternalSearchCondition(getModuleId(), getCollectableSearchCondition());
		if (clctcond != null)
			compositecond.addOperand(clctcond);

		return SearchConditionUtils.simplified(compositecond);
	}

	/**
	 * @return search expression containing the internal version of the collectable search condition,
	 *         that is used for performing the actual search, and the sorting sequence.
	 * @throws CollectableFieldFormatException
	 */
	protected CollectableGenericObjectSearchExpression getInternalSearchExpression() throws CollectableFieldFormatException {
		CollectableGenericObjectSearchExpression clctGOSearchExpression = new CollectableGenericObjectSearchExpression(
				getInternalSearchCondition(), getResultController().getCollectableSortingSequence(), iSearchDeleted);
		return clctGOSearchExpression;
	}

	/**
	 * @return a Comparator that compares the entity labels first, then the field labels.
	 *         Fields of the main entity are sorted lower than all other fields.
	 *         
	 * @deprecated Remove this.
	 */
	private Comparator<CollectableEntityField> getCollectableEntityFieldComparator() {
		return new Comparator<CollectableEntityField>() {
			final Collator collator = LangUtils.getDefaultCollator();

			/**
			 * @param clctefwe1 Expecting a CollectableEntityFieldWithEntity
			 * @param clctefwe2 Expecting a CollectableEntityFieldWithEntity
			 */
			@Override
			public int compare(CollectableEntityField clctefwe1, CollectableEntityField clctefwe2) {
				final int iCompareEntities = LangUtils.compare(getEntityLabel(clctefwe1), getEntityLabel(clctefwe2), collator);
				return (iCompareEntities != 0) ? iCompareEntities : LangUtils.compare(clctefwe1.getLabel(), clctefwe2.getLabel(), collator);
			}

			private String getEntityLabel(CollectableEntityField field) {
				final CollectableEntityFieldWithEntity clctefwe = (CollectableEntityFieldWithEntity) field;
				return clctefwe.getCollectableEntityName().equals(GenericObjectCollectController.this.getCollectableEntity().getName()) ?
					null : clctefwe.getCollectableEntityLabel();
			}
		};
	}

	/**
	 * @return a specific table model with an overridden getValueAt method, providing access to subform entries.
	 */
	@Override
	protected SortableCollectableTableModel<CollectableGenericObjectWithDependants> newResultTableModel() {
		final SortableCollectableTableModel<CollectableGenericObjectWithDependants> result = 
			GenericObjectClientUtils.<CollectableGenericObjectWithDependants>newGenericObjectsResultTableModel(
					getCollectableEntity(), getFields().getSelectedFields());

		// setup sorted fields and sorting order from preferences
		List<SortKey> sortKeys = readColumnOrderFromPreferences();
		if (result.getColumnCount() > 0) {
			try {
				result.setSortKeys(sortKeys, false);
			} catch (IllegalArgumentException e) {
				// sortKeys contains invalid column index, ignore
			}
		}

		class GenericObjectSortingRunnable implements CommonRunnable {
			@Override
			public void run() throws CommonBusinessException {
				List<? extends SortKey> sortKeys = result.getSortKeys();
				List<SortKey> filteredSortKeys = CollectionUtils.applyFilter(sortKeys, new Predicate<SortKey>() {
					@Override
					public boolean evaluate(SortKey k) {
						final CollectableEntityFieldWithEntity clctefSorted = (CollectableEntityFieldWithEntity) result.getCollectableEntityField(k.getColumn());
						return (clctefSorted.getCollectableEntityName().equals(getCollectableEntity().getName()));
					}
				});
				if (filteredSortKeys.size() == sortKeys.size()) {
					//NUCLEUSINT-1039
					cmdSearch();
				} else {
					result.setSortKeys(Collections.<SortKey>emptyList(), false);
					throw new CommonBusinessException(CommonLocaleDelegate.getMessage("GenericObjectCollectController.19","Das Suchergebnis kann nicht nach Unterformularspalten bzw. Vaterspalten sortiert werden."));
				}
			}
		}

		final JTable tbl = getResultTable();
		// clicking header does not mean column selection, but sorting:
		tbl.setColumnSelectionAllowed(false);

		final GenericObjectSortingRunnable runnable = new GenericObjectSortingRunnable();

		// clicking a column header is to cause a new search on the server:
		TableUtils.addMouseListenerForSortingToTableHeader(tbl, result, runnable);

		// @todo: this is not so nice a construct... maybe this should be moved to the result panel, because we use insight into the implementation of fixed tables here.
		TableUtils.addMouseListenerForSortingToTableHeader(getFixedResultTable(), result, runnable);

		return result;
	}

	/**
	 * Get the fixed part of the result table, if any.
	 */
	public final JTable getFixedResultTable() {
		return getResultPanel().getFixedResultTable();
	}

	@Override
	protected SearchWorker<CollectableGenericObjectWithDependants> getSearchWorker() {
		return new ObservableSearchWorker();
	}

	@Override
	protected SearchWorker<CollectableGenericObjectWithDependants> getSearchWorker(List<Observer> lstObservers) {
		ObservableSearchWorker observableSearchWorker = new ObservableSearchWorker();
		for(Observer observer : lstObservers)
			observableSearchWorker.addObserver(observer);
		return observableSearchWorker;
	}

	@Override
	protected void search() throws CommonBusinessException {
		this.search(false);
	}

	@Override
	protected void search(boolean bRefreshOnly) throws CommonBusinessException {
		log.debug("START search");

		/** @todo move to CollectController! */
		this.makeConsistent(true);
		removePreviousChangeListenersForResultTableVerticalScrollBar();
		final ProxyList<CollectableGenericObjectWithDependants> lstclctResult = getSearchResult();
		setCollectableProxyList(lstclctResult);
		this.fillResultPanel(lstclctResult, lstclctResult.size(), false);
		setupChangeListenerForResultTableVerticalScrollBar();
		log.debug("FINISHED search");
	}

	/**
	 * @return List<Collectable>
	 * @throws CollectableFieldFormatException
	 * @postcondition result != null
	 * @own-thread
	 */
	private ProxyList<CollectableGenericObjectWithDependants> getSearchResult() throws CollectableFieldFormatException {
		final CollectableGenericObjectSearchExpression clctexprInternal = getInternalSearchExpression();
		clctexprInternal.setValueListProviderDatasource(getValueListProviderDatasource());
		clctexprInternal.setValueListProviderDatasourceParameter(getValueListProviderDatasourceParameter());
		log.debug("Interne Suchbedingung: " + clctexprInternal.getSearchCondition());

		// OPTIMIZATION: only selected and/or required attributes are loaded here:
		final ProxyList<GenericObjectWithDependantsVO> proxylstlovwdvo =
			lodelegate.getGenericObjectsWithDependants(getModuleId(), clctexprInternal,
				getSelectedAndRequiredAttributeIds(), getSelectedSubEntityNames(), isParentFieldSelected(),
				getIncludeSubModulesForSearch());

		return new CollectableGenericObjectProxyListAdapter(proxylstlovwdvo);
	}

	private boolean getIncludeSubModulesForSearch() {
		// Don't include submodules unless this controller is for a submodule.
		// Don't include submodules for general search:
		final Integer iModuleId = getModuleId();
		return (iModuleId != null) && Modules.getInstance().isSubModule(iModuleId);
	}

	private List<CollectableEntityFieldWithEntity> getSelectedFields() {
		return CollectionUtils.typecheck(getFields().getSelectedFields(), CollectableEntityFieldWithEntity.class);
	}

	private Set<Integer> getSelectedAndRequiredAttributeIds() {
		final Set<? extends CollectableEntityField> stSelectedAttributes = CollectionUtils.selectIntoSet(getSelectedFields(), new CollectableEntityFieldWithEntity.HasEntity(getCollectableEntity()));
		final Set<String> stFieldNamesSelected = CollectionUtils.transformIntoSet(stSelectedAttributes, new CollectableEntityField.GetName());
		final Set<String> stFieldNamesRequired = getRequiredFieldNamesForResult();
		final Set<String> stFieldNamesSelectedOrRequired = CollectionUtils.union(stFieldNamesSelected, stFieldNamesRequired);
		return CollectionUtils.transformIntoSet(stFieldNamesSelectedOrRequired, new AttributeProvider.GetAttributeIdByName(sEntity, AttributeCache.getInstance()));
	}

	/**
	 * @return Set<String>
	 * @postcondition result != null
	 */
	protected Set<String> getSelectedSubEntityNames() {
		return GenericObjectUtils.getSubEntityNames(getSelectedFields(), getCollectableEntity().getName(), Modules.getInstance());
	}

	private boolean isParentFieldSelected() {
		return GenericObjectUtils.containsParentField(getSelectedFields(), this.getParentEntityName());
	}

	// @todo move to ResultController or ResultPanel?
	protected void setCollectableProxyList(ProxyList<CollectableGenericObjectWithDependants> proxylstclct) {
		this.proxylstclct = proxylstclct;
	}

	// @todo move to ResultController or ResultPanel?
	protected ProxyList<CollectableGenericObjectWithDependants> getCollectableProxyList() {
		return proxylstclct;
	}

	/**
	 * sets up the change listener for the vertical scrollbar of the result table,
	 * only if the proxy list has been set (that is not before the first search).
	 */
	protected void setupChangeListenerForResultTableVerticalScrollBar() {
		final ProxyList<? extends Collectable> lstclct = getCollectableProxyList();
		if (lstclct != null)
			getResultPanel().setupChangeListenerForResultTableVerticalScrollBar(lstclct, getFrame());
	}

	protected void removePreviousChangeListenersForResultTableVerticalScrollBar() {
		final JScrollBar scrlbarVertical = getResultPanel().getResultTableScrollPane().getVerticalScrollBar();
		final DefaultBoundedRangeModel model = (DefaultBoundedRangeModel) scrlbarVertical.getModel();
		GenericObjectResultPanel.removePreviousChangeListenersForResultTableVerticalScrollBar(model);
	}

	@Override
	protected void unsafeFillDetailsPanel(CollectableGenericObjectWithDependants clct) throws CommonBusinessException {
		log.debug("GenericObjectCollectController.unsafeFillDetailsPanel start");

		// Don't reload the layout after update - only if quintuple fields changed. */
		if (bReloadLayout)
			// Load the right layout if it is not already there:
			// Note that quintuple field listeners are removed here:
			loadLayoutForDetailsTab(clct, getCollectStateModel().getCollectState());
		else
			// Restore the default setting (Next time, reload the layout):
			bReloadLayout = true;

		/** @todo this doesn't seem right: when the layout changes, the fields are transferred
		 * from the old mask (in loadLayoutForDetailsTab), then they are overwritten with the values of clct? */

		try {
			CollectableGenericObjectWithDependants lowdCurrent = null;
			if (isHistoricalView())
				lowdCurrent = new CollectableGenericObjectWithDependants(GenericObjectDelegate.getInstance().getWithDependants((Integer) getSelectedCollectableId()));

			for (String sFieldName : getOrderedFieldNamesInDetails()) {
				// iterate over the models rather than over the components:
				final CollectableComponentModel clctcompmodel = layoutrootDetails.getCollectableComponentModelFor(sFieldName);
				final CollectableField clctfShown = clct.getField(sFieldName);
				clctcompmodel.setField(clctfShown);

				markFieldInHistoricalView(lowdCurrent, sFieldName, clctfShown);
			}

			// fill subforms:
			log.debug("fill subforms start");
			Collection<LogbookVO> colllogbookvo = null;
			if (isHistoricalView())
				try {
					colllogbookvo = GenericObjectDelegate.getInstance().getLogbook(lowdCurrent.getId());
				}
			catch (CommonFinderException ex) {
				// No logbook, no changes
			}

			if (!getCollectState().isDetailsModeMultiViewOrEdit()) {
				if(clct.getId() != null){
					UIUtils.invokeOnDispatchThread(new Runnable() {
						@Override
						public void run() {
							GenericObjectCollectController.this.getSubFormsLoader().startLoading();
						}
					});
					if (isHistoricalView())
						showLoading(false);
				}

				// loading calculated attributes

				// loading subforms
				for (final SubFormController subformctl : getSubFormControllersInDetails()) {
					// TODO try to eliminate this cast
					final MasterDataSubFormController mdsubformctl = (MasterDataSubFormController) subformctl;
					if (isHistoricalView())
						markSubformInHistoricalView(mdsubformctl, clct, colllogbookvo);
					else {
						//by object generation
						DependantMasterDataMap dependants = clct.getGenericObjectWithDependantsCVO().getDependants();
						if(clct.getId() == null && dependants.getAllData().size() != 0){
							for (String entity: dependants.getEntityNames())
								if (entity.equals(mdsubformctl.getCollectableEntity().getName()))
									mdsubformctl.fillSubForm(dependants.getData(entity));
						}
						else if (clct.getId() == null) {
							mdsubformctl.clear();
							mdsubformctl.getSubForm().getJTable().setBackground(Color.WHITE);
							mdsubformctl.fillSubForm(new ArrayList<EntityObjectVO>());
						}
						else {
							if (mdsubformctl.isChildSubForm())
								continue;

							fillSubformMultithreaded(clct, mdsubformctl);
						}
					}
				}
			}
			log.debug("fill subforms done");

			selectTabPane(clct);


		}
		finally {
			/** @todo this doesn't seem to belong here... */
			this.addUsageCriteriaFieldListeners(false);
		}
		log.debug("GenericObjectCollectController.unsafeFillDetailsPanel done");
	}

	private void selectTabPane(CollectableGenericObjectWithDependants clct) {
		// select tab in state
		//Integer iState = (Integer)clct.getField(NuclosEOField.STATENUMBER.getMetaData().getField()).getValueId();
		DynamicAttributeVO vo = clct.getGenericObjectCVO().getAttribute(NuclosEOField.STATENUMBER.getMetaData().getId().intValue());
		if(vo == null)
			return;
		Integer iState = (Integer)vo.getValue();

		String strTabName = getPreferences().get(TABSELECTED, "");
		for(StateVO voState :  StateDelegate.getInstance().getStatesByModule(getModuleId()))
			if(voState.getNumeral().equals(iState)) {
				if(voState.getTabbedPaneName() != null)
					strTabName = voState.getTabbedPaneName();
				break;
			}

		JComponent jcomp = layoutrootDetails.getRootComponent();
		List<JTabbedPane> lst = new ArrayList<JTabbedPane>();

		searchTabbedPanes(jcomp, lst);

		for(JTabbedPane tabPane : lst)
			for(int i = 0; i < tabPane.getTabCount(); i++)
				if(org.apache.commons.lang.StringUtils.equals(tabPane.getComponentAt(i).getName(), strTabName)
					|| org.apache.commons.lang.StringUtils.equals(tabPane.getTitleAt(i), strTabName)) {
					tabPane.setSelectedIndex(i);
					break;
				}

		for(JTabbedPane tabPane : lst)
			for(int i = 0; i < tabPane.getTabCount(); i++) {
				Component c = tabPane.getComponentAt(i);
				if(c instanceof JComponent) {
					List<JComponent> lstComponents = new ArrayList<JComponent>();
					collectComponents((JComponent)c, lstComponents);
					if(lstComponents.size() == 0) {
						tabPane.setEnabledAt(i, false);
						break;
					}

					boolean blnVisible = false;
					for(JComponent jc : lstComponents){
						if(jc instanceof LabeledComponent) {
							LabeledComponent lc = (LabeledComponent)jc;
							blnVisible = lc.isVisible();
						}
						else if(jc instanceof JPanel)
							blnVisible = false;
						else
							blnVisible = jc.isVisible();
						if(blnVisible)
							break;
					}
					tabPane.setEnabledAt(i, blnVisible);
				}
			}

	}

	private void searchTabbedPanes(JComponent comp, List<JTabbedPane> lst) {
		if(comp instanceof JTabbedPane)
			lst.add((JTabbedPane)comp);
		if(comp.getComponents().length == 0)
			return;
		for(Component c : comp.getComponents())
			if(c instanceof JComponent)
				searchTabbedPanes((JComponent)c, lst);
	}

	private void collectComponents(JComponent comp, List<JComponent> lst) {
		for(Component c : comp.getComponents())
			if(c instanceof JComponent) {
				JComponent jc = (JComponent)c;
				if(jc.getComponentCount() == 0)
					lst.add(jc);
				if(jc instanceof LabeledComponent) {
					lst.add(jc);
					continue;
				}
				collectComponents(jc, lst);
			}
	}

	private void fillSubformMultithreaded(
		final CollectableGenericObjectWithDependants clct,
		final MasterDataSubFormController mdsubformctl)
	throws NuclosBusinessException {
		final SubFormsInterruptableClientWorker sfClientWorker = new SubFormsInterruptableClientWorker() {
			Collection<EntityObjectVO>	collmdcvo;

			@Override
			public void init() throws CommonBusinessException {
				if(!interrupted) {
					mdsubformctl.clear();
					mdsubformctl.getSubForm().setLockedLayer();
					// System.out.println("********** setLockedLayer (worker) *** for "+mdsubformctl.getCollectableEntity().getName()
					// + " - interrupted? " + interrupted);
				}
			}

			@Override
			public void work() throws NuclosBusinessException {
				if(interrupted)
					return;
				else
					collmdcvo = (clct.getId() == null)
					? new ArrayList<EntityObjectVO>()
						: MasterDataDelegate.getInstance().getDependantMasterData(
							mdsubformctl.getCollectableEntity().getName(),
							mdsubformctl.getForeignKeyFieldName(), clct.getId());
			}

			@Override
			public void handleError(Exception ex) {
				if(!interrupted)
					Errors.getInstance().showExceptionDialog(
						getResultsComponent(), ex);
			}

			@Override
			public JComponent getResultsComponent() {
				return mdsubformctl.getSubForm();
			}

			@Override
			public void paint() throws CommonBusinessException {
				// The data schould not be published to sub form! otherwise we
				// will see a sub form data of another object!
				if(!interrupted)
					synchronized(GenericObjectCollectController.this) {
						// GenericObjectCollectController.this.wait();
						final boolean bWasDetailsChangedIgnored = GenericObjectCollectController.this.isDetailsChangedIgnored();
						GenericObjectCollectController.this.setDetailsChangedIgnored(true);
						// System.out.println("********** block this *** for "+mdsubformctl.getCollectableEntity().getName());
						try {
							// System.out.println("********** fillSubForm (worker) *** for "+mdsubformctl.getCollectableEntity().getName()
							// + " - interrupted? " + interrupted);

							final CollectableField clctfield = clct.getField(NuclosEOField.STATE.getMetaData().getField());
							final Integer iStatusId = (clctfield != null)
							? (Integer) clctfield.getValueId()
								: null;

							String entityname = mdsubformctl.getEntityAndForeignKeyFieldName().getEntityName();
							Permission permission = SecurityCache.getInstance().getSubFormPermission(
								entityname, iStatusId);

							if(permission == null)
								mdsubformctl.clear();
							else
								mdsubformctl.fillSubForm(collmdcvo);

							// System.out.println("********** filled (worker) *** for "+mdsubformctl.getCollectableEntity().getName()
							// + " - interrupted? " + interrupted);
						}
						finally {
							if(!bWasDetailsChangedIgnored)
								GenericObjectCollectController.this.setDetailsChangedIgnored(bWasDetailsChangedIgnored);
						}
						GenericObjectCollectController.this.getSubFormsLoader().setSubFormLoaded(
							mdsubformctl.getCollectableEntity().getName(), true);
						mdsubformctl.getSubForm().forceUnlockFrame();
						mdsubformctl.selectFirstRow();
					}
			}
		};
		GenericObjectCollectController.this.getSubFormsLoader().addSubFormClientWorker(
			mdsubformctl.getCollectableEntity().getName(), sfClientWorker);
	}

	private void markSubformInHistoricalView(MasterDataSubFormController subformctl,
		CollectableGenericObjectWithDependants clct, Collection<LogbookVO> logbookEntries) throws NuclosBusinessException {

		String entityName = subformctl.getCollectableEntity().getName();
		Collection<EntityObjectVO> collmdvo = clct.getGenericObjectWithDependantsCVO().getDependants().getData(entityName);
		/** @todo check if this is correct: Shouldn't we initialize the subform even if it is empty? */
		if(!collmdvo.isEmpty())
			subformctl.fillSubForm(collmdvo);

		EntityMetaDataVO entityMeta = MetaDataClientProvider.getInstance().getEntity(entityName);
		boolean hasLoggingFields = false;
		for(EntityFieldMetaDataVO fieldMeta :
				MetaDataClientProvider.getInstance().getAllEntityFieldsByEntity(entityName).values())
			hasLoggingFields |= fieldMeta.isLogBookTracking();

		if(!hasLoggingFields)
			// if no field of the entity is logged, mark subform table as not logged
			subformctl.getSubForm().getJTable().setBackground(colorHistoricalNotTracked);
		else
			for(LogbookVO logbookvo : logbookEntries) {
				Integer mmId = logbookvo.getMasterDataMetaId();
				if(mmId != null && mmId.equals(entityMeta.getId()))
					// if there is any entry in the logbook for this entity done
					// after the date of the historical view, mark the subform
					// table as changed
					if(dateHistorical.compareTo(logbookvo.getCreatedAt()) < 0) {
						subformctl.getSubForm().getJTable().setBackground(colorHistoricalChanged);
						break;
					}
			}
	}

	private void markFieldInHistoricalView(CollectableGenericObjectWithDependants clctlowdCurrent, String sFieldName, CollectableField clctfShown) {
		if (clctlowdCurrent != null) {
			final CollectableField clctf = clctlowdCurrent.getField(sFieldName);
			if (!clctfShown.equals(clctf)) {
				final Collection<CollectableComponent> collclctcomp = layoutrootDetails.getCollectableComponentsFor(sFieldName);
				if (!collclctcomp.isEmpty()) {
					final CollectableComponent clctcomp = collclctcomp.iterator().next();
					final JComponent compFocussable = clctcomp.getFocusableComponent();
					String initialToolTip = (String) compFocussable.getClientProperty("initialToolTip");
					if (initialToolTip == null) {
						initialToolTip = StringUtils.emptyIfNull(compFocussable.getToolTipText());
						compFocussable.putClientProperty("initialToolTip", initialToolTip);
					}
					if (clctcomp instanceof CollectableComboBox) {
						CollectableComboBox clctcmbx = (CollectableComboBox) clctcomp;
						clctcmbx.getJComboBox().setRenderer(clctcmbx.new CollectableFieldRenderer() {
							/**
							 * 
							 */
							private static final long serialVersionUID = 1L;

							@Override
							protected void paintComponent(Graphics g) {
								setBackground(colorHistoricalChanged);
								super.paintComponent(g);
							}
						});
					}
					else
						compFocussable.setBackground(colorHistoricalChanged);
					final String sToolTip = CommonLocaleDelegate.getMessage("GenericObjectCollectController.4","{0} [Ge\u00e4ndert; aktueller Wert: \"{1}\"]", initialToolTip, clctf.toString());
					compFocussable.setToolTipText(sToolTip);
					clctcomp.getJComponent().setToolTipText(sToolTip);
					// todo: mark fields which are not tracked in logbook?
				}
			}
		}
	}

	private List<String> getOrderedFieldNamesInDetails() {
		return layoutrootDetails.getOrderedFieldNames();
	}

	@Override
	protected void unsafeFillMultiEditDetailsPanel(Collection<CollectableGenericObjectWithDependants> collclct) throws CommonBusinessException {
		// set the right layout:
		final UsageCriteria usagecriteria = getGreatestCommonUsageCriteriaFromCollectables(collclct);
		this.reloadLayout(usagecriteria, getCollectStateModel().getCollectState(), true, false);

		// fill the details panel with the common values:
		super.unsafeFillMultiEditDetailsPanel(collclct);

		// load dependent data
		List<EntityAndFieldName> requiredDependants = new ArrayList<EntityAndFieldName>();
		for (DetailsSubFormController<CollectableEntityObject> subformctl : getSubFormControllersInDetails()) {
			if (StringUtils.isNullOrEmpty(subformctl.getSubForm().getParentSubForm())) {
				requiredDependants.add(subformctl.getEntityAndForeignKeyFieldName());
			}
		}

		for (CollectableGenericObjectWithDependants clct : collclct) {
			DependantMasterDataMap dependants = MasterDataDelegate.getInstance().getDependants(clct.getId(), requiredDependants);
			for (String entityname : dependants.getEntityNames()) {
				clct.getGenericObjectWithDependantsCVO().getDependants().setData(entityname, dependants.getData(entityname));
			}
		}

		// begin multi-update of dependants:
		multiupdateofdependants = new MultiUpdateOfDependants(getSubFormControllersInDetails(), collclct);
	}

	/**
	 * Get also changes in subforms
	 * @todo move to DetailsPanel
	 */
	@Override
	protected String getMultiEditChangeString() {
		final String sChangesSoFar = super.getMultiEditChangeString();
		final StringBuilder sbResult = new StringBuilder(sChangesSoFar);

		try {
			for (DetailsSubFormController<CollectableEntityObject> subformctl : getSubFormControllersInDetails())
				if (subformctl.getSubForm().isEnabled()) {
					boolean bChanged = false;
					boolean bNew = false;
					boolean bRemoved = false;
					if (!subformctl.getSubForm().getJTable().isEditing())
						for (CollectableEntityObject clctmd : subformctl.getCollectables(true, true, false)) {
							EntityObjectVO mdvo = clctmd.getEntityObjectVO();
							if (mdvo.isFlagUpdated())
								if (mdvo.getCreatedBy() == null)
									bNew = true;
								else
									bChanged = true;
							if (mdvo.isFlagRemoved())
								bRemoved = true;
						}
					else
						bChanged = true;

					if (bNew || bChanged || bRemoved) {
						if (sbResult.length() > 0)
							sbResult.append(", ");
						sbResult.append("Eintr?ge in ").append(subformctl.getCollectableEntity().getLabel());
						if (bNew)
							sbResult.append(" hinzugef?gt");
						if (bChanged) {
							sbResult.append(bNew ? "/" : " ");
							sbResult.append("ver?ndert");
						}
						if (bRemoved) {
							sbResult.append(bNew || bChanged ? "/" : " ");
							sbResult.append("gel?scht");
						}
					}
				}
		}
		catch (CommonValidationException ex) {
			// This will never occur, as there will not be validated in getCollectables, when bPrepareForSavingAndValidate ist false
			ex.printStackTrace();
		}

		return sbResult.toString();
	}

	private void loadLayoutForDetailsTab(CollectableGenericObject clct, CollectState collectstate) throws CommonBusinessException {
		log.debug("loadLayoutForDetailsTab start");
		boolean transferContents = !collectstate.isDetailsModeNew();
		this.reloadLayout(getUsageCriteria(clct), collectstate, transferContents, false);
		log.debug("loadLayoutForDetailsTab done");
	}

	@Override
	protected CollectableGenericObjectWithDependants readSelectedCollectable() throws CommonBusinessException {
		final CollectableGenericObjectWithDependants result;

		if (isHistoricalView()) {
			/** @todo OPTIMIZE: Is it really necessary to read the current object first? */
			// What about this? UA/020206
			// final int iGenericObjectId = ((Integer) this.getSelectedCollectableId()).intValue();
			// final int iModuleId = this.getSelectedCollectableModuleId().intValue();
			final GenericObjectVO govo = getSelectedGenericObjectCVO();
			final int iGenericObjectId = govo.getId();
			assert dateHistorical != null;
			result = new CollectableGenericObjectWithDependants(lodelegate.getHistorical(iGenericObjectId, dateHistorical));
		}
		else
			result = super.readSelectedCollectable();
		return result;
	}

	@Override
	public CollectableGenericObjectWithDependants readCollectable(CollectableGenericObjectWithDependants clct) throws CommonBusinessException {
		return findCollectableById(clct.getCollectableEntity().getName(), clct.getId());
	}

	@Override
	public CollectableGenericObjectWithDependants findCollectableById(String sEntityName, Object oId) throws CommonBusinessException {
		if (sEntityName == null)
			throw new IllegalArgumentException("sEntityName");
		if (oId == null)
			throw new IllegalArgumentException("oId");

		if(oId instanceof Long)  // Compatability to new EntityObjectVO
			oId = new Integer(((Long) oId).intValue());

		final int iGenericObjectId = (Integer) oId;

		final CollectableGenericObjectWithDependants result = new CollectableGenericObjectWithDependants(lodelegate.getWithDependants(iGenericObjectId));

		assert isCollectableComplete(result);
		return result;
	}

	@Override
	protected CollectableGenericObjectWithDependants findCollectableByIdWithoutDependants(
		String sEntityName, Object oId) throws CommonBusinessException {
		if (sEntityName == null)
			throw new IllegalArgumentException("sEntityName");
		if (oId == null)
			throw new IllegalArgumentException("oId");

		if(oId instanceof Long){
			oId = new Integer(((Long) oId).intValue());
		}
		final int iGenericObjectId = (Integer) oId;

		final CollectableGenericObjectWithDependants result = CollectableGenericObjectWithDependants.newCollectableGenericObject(lodelegate.get(iGenericObjectId));

		assert isCollectableComplete(result);
		return result;
	}

	@Override
	protected boolean isDetailsModeViewLoadingWithoutDependants() {
		return true;
	}

	@Override
	public Integer getVersionOfCollectableById(String sEntityName, Object oId) throws CommonBusinessException {
		if (sEntityName == null)
			throw new IllegalArgumentException("sEntityName");
		if (oId == null)
			throw new IllegalArgumentException("oId");

		final int iGenericObjectId = (Integer) oId;

		return lodelegate.getVersion(iGenericObjectId);
	}

	@Override
	protected CollectableGenericObjectWithDependants updateCurrentCollectable(CollectableGenericObjectWithDependants clctCurrent) throws CommonBusinessException {
		clctCurrent.validate(getCollectableEntityForDetails());

		// todo: validate subform data

		assert !isHistoricalView();

		final CollectableGenericObjectWithDependants result = updateCollectable(clctCurrent, getAllSubFormData(clctCurrent));

		// remember if the layout needs to be reloaded afterwards:
		if (getUsageCriteria(result).equals(getUsageCriteria(clctCurrent)))
			bReloadLayout = false;

		return result;
	}

	@Override
	protected CollectableGenericObjectWithDependants updateCollectable(CollectableGenericObjectWithDependants clct, Object oAdditionalData) throws CommonBusinessException {
		final DependantCollectableMasterDataMap mpclctDependants = (DependantCollectableMasterDataMap) oAdditionalData;

		// @todo validate?

		if (mpclctDependants != null)
			Utils.prepareDependantsForSaving(mpclctDependants);

		final GenericObjectWithDependantsVO lowdcvo = clct.getGenericObjectWithDependantsCVO();
		final GenericObjectWithDependantsVO lowdcvoCurrent = new GenericObjectWithDependantsVO(lowdcvo, mpclctDependants.toDependantMasterDataMap());

		// update the whole thing:
		final GenericObjectWithDependantsVO lowdcvoUpdated = lodelegate.update(lowdcvoCurrent);

		// and return the updated version:
		return new CollectableGenericObjectWithDependants(lowdcvoUpdated);
	}

	/**
	 * @param govo
	 * @return a new <code>CollectableGenericObjectWithDependants</code> wrapping <code>govo</code>.
	 */
	private static CollectableGenericObjectWithDependants newCollectableGenericObject(GenericObjectVO govo) {
		return CollectableGenericObjectWithDependants.newCollectableGenericObject(govo);
	}

	/**
	 * @param clct
	 * @return DependantCollectableMap containing the dependants of the given Collectable relevant for multiple updates
	 *         additional data (if any) needed for multiple updates.
	 * @throws CommonValidationException if some subform data is invalid.
	 */
	@Override
	protected DependantCollectableMasterDataMap getAdditionalDataForMultiUpdate(CollectableGenericObjectWithDependants clct) throws CommonValidationException {
		return multiupdateofdependants.getDependantCollectableMapForUpdate(getSubFormControllersInDetails(), clct);
	}

	/**
	 * @return the CollectableEntity that contains exactly the fields that are contained in the details tab.
	 */
	@Override
	protected final CollectableEntity getCollectableEntityForDetails() {
		final Collection<String> collFieldNames = layoutrootDetails.getFieldNames();
		return new CollectableGenericObjectEntity(this.getEntityName(), getEntityLabel(), collFieldNames);
	}

	/**
	 * @return the parent entity, if any, of this <code>CollectController</code>'s entity.
	 */
	public final CollectableEntity getParentEntity() {
		return getParentEntity(this.getEntityName());
	}

	private static CollectableEntity getParentEntity(String sEntityName) {
		final String sParentEntityName = getParentEntityName(sEntityName);
		return (sParentEntityName == null) ? null : DefaultCollectableEntityProvider.getInstance().getCollectableEntity(sParentEntityName);
	}

	/**
	 * @return the name of the parent entity, if any, of this <code>CollectController</code>'s entity.
	 */
	private String getParentEntityName() {
		return getParentEntityName(this.getEntityName());
	}

	/**
	 * @param sEntityName
	 * @return the name of the given entity's parent entity, if any.
	 */
	private static String getParentEntityName(String sEntityName) {
		return Modules.getInstance().getParentEntityName(sEntityName);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected CollectableGenericObjectWithDependants insertCollectable(CollectableGenericObjectWithDependants clctNew) throws CommonBusinessException {
		if (clctNew.getId() != null)
			throw new IllegalArgumentException("clctNew");

		final DependantCollectableMasterDataMap mpclctDependants = getAllSubFormData(null);

		// We have to clear the ids for cloned objects:
		// @todo eliminate this workaround - this is the wrong place. The right place is the Clone action!
		final DependantMasterDataMap mpDependants = org.nuclos.common.Utils.clearIds(mpclctDependants.toDependantMasterDataMap());

		clctNew.validate(getCollectableEntityForDetails());

		final GenericObjectVO govo = clctNew.getGenericObjectCVO();
		final GenericObjectWithDependantsVO lowdcvoNew = new GenericObjectWithDependantsVO(govo, mpDependants);

		final GenericObjectWithDependantsVO lowdcvoInserted = lodelegate.create(lowdcvoNew, getSelectedSubEntityNames());

		return new CollectableGenericObjectWithDependants(lowdcvoInserted);
	}

	@Override
	protected void cloneSelectedCollectable() throws CommonBusinessException {
		super.cloneSelectedCollectable();

		EditModel modelDetails = getDetailsPanel().getEditModel();

		// Some attributes must be cleared for a new entity object:

		// Remember the origin of the cloned object
		CollectableComponentModel clctcompmodelIdentifier = modelDetails.getCollectableComponentModelFor(NuclosEOField.SYSTEMIDENTIFIER.getMetaData().getField());
		if (clctcompmodelIdentifier != null) {
			CollectableComponentModel clctcompmodelOrigin = modelDetails.getCollectableComponentModelFor(NuclosEOField.ORIGIN.getMetaData().getField());
			if(clctcompmodelOrigin != null)
				clctcompmodelOrigin.setField(clctcompmodelIdentifier.getField());
			clctcompmodelIdentifier.clear();
		}

		CollectableComponentModel clctcompmodelStatus = modelDetails.getCollectableComponentModelFor(NuclosEOField.STATE.getMetaData().getField());
		if (clctcompmodelStatus != null)
			clctcompmodelStatus.clear();
		CollectableComponentModel clctcompmodelStatusNumeral = modelDetails.getCollectableComponentModelFor(NuclosEOField.STATENUMBER.getMetaData().getField());
		if (clctcompmodelStatusNumeral != null)
			clctcompmodelStatusNumeral.clear();

		CollectableField clctfield = getSelectedCollectable().getField(NuclosEOField.STATE.getMetaData().getField());
		Integer statusId = (clctfield != null) ? (Integer) clctfield.getValueId() : null;

		if (statusId != null)
			// check permission of attributes
			for(DynamicAttributeVO attribute :getSelectedCollectable().getGenericObjectCVO().getAttributes()) {
				EntityFieldMetaDataVO fieldMeta = MetaDataClientProvider.getInstance().getEntityField(sEntity, attribute.getAttributeId().longValue());
				CollectableComponentModel clctcompmodel = modelDetails.getCollectableComponentModelFor(fieldMeta.getField());
				if(clctcompmodel != null ) {
					CollectableEntityField clctef = clctcompmodel.getEntityField();
					Permission permission = SecurityCache.getInstance().getAttributePermission(sEntity, clctef.getName(), statusId);

					if(permission == null)
						clctcompmodel.clear();
				}
			}
	}

	/**
	 * gathers the data from all enabled subforms. All rows are gathered, even the removed ones.
	 * @param oParentId set as the parent id for each subform row.
	 * @return the data from all subforms
	 */
	protected DependantCollectableMasterDataMap getAllSubFormData(CollectableGenericObjectWithDependants oParent) throws CommonValidationException {
		final DependantCollectableMasterDataMap result = new DependantCollectableMasterDataMap();

		for (DetailsSubFormController<CollectableEntityObject> subformctl : getSubFormControllersInDetails()) {

			if("nuclos_generalsearchdocument".equals(subformctl.getCollectableEntity().getName())) {
				String sPath = (String)Modules.getInstance().getModuleById(iModuleId).getField("documentPath");

				sPath = getDirectoryPath(StringUtils.emptyIfNull(sPath), oParent);

				if(sPath == null)
					continue;
				for(CollectableMasterData md : subformctl.getCollectables())
					if(md.getField("file").getValue() != null) {
						GenericObjectDocumentFile docFile = (GenericObjectDocumentFile)md.getField("file").getValue();
						docFile.setDirectoryPath(sPath);
						CollectableValueField clctfield = new CollectableValueField(sPath);
						if (md.getField("path") != null && !md.getField("path").equals(clctfield)) {
							md.setField("path", clctfield);
						}
					}
			}

			//NUCLEUSINT-1119
			if (subformctl.getSubForm().getParentSubForm() == null)
				if(oParent != null)
					result.addValues(subformctl.getSubForm().getEntityName(), subformctl.getAllCollectables(oParent.getId(), getSubFormControllersInDetails(), true, null));
				else
					result.addValues(subformctl.getSubForm().getEntityName(), subformctl.getAllCollectables(null, getSubFormControllersInDetails(), true, null));
		}

		return result;
	}

	private String getDirectoryPath(String path, CollectableGenericObjectWithDependants oParent) {

		if (path.contains("${")){
			Pattern referencedEntityPattern = Pattern.compile ("[$][{][\\w\\[\\]]+[}]");
			Matcher referencedEntityMatcher = referencedEntityPattern.matcher (path);
			StringBuffer sb = new StringBuffer();

			while (referencedEntityMatcher.find()) {
				Object value = referencedEntityMatcher.group().substring(2,referencedEntityMatcher.group().length()-1);

				String sName = value.toString();
				Object fieldValue =  getDetailsEditView().getModel().getCollectableComponentModelFor(sName).getField().getValue();
				if(fieldValue != null)
					referencedEntityMatcher.appendReplacement (sb, fieldValue.toString());
				else
					referencedEntityMatcher.appendReplacement (sb, "");
			}

			// complete the transfer to the StringBuffer
			referencedEntityMatcher.appendTail (sb);
			path = sb.toString();

		}

		return path;

	}


	/**
	 * @return
	 * @postcondition result != null
	 * @postcondition result.isComplete()
	 */
	@Override
	public CollectableGenericObjectWithDependants newCollectable() {
		final Integer iModuleId = getModuleId();
		assert iModuleId != null;
		final CollectableGenericObjectWithDependants result = newCollectableGenericObject(new GenericObjectVO(iModuleId, null, null, null));
		assert result != null;
		assert isCollectableComplete(result);
		setSubsequentStatesVisible(false, false);
		return result;
	}

	@Override
	protected void deleteCollectable(CollectableGenericObjectWithDependants clct) throws CommonBusinessException {
		assert !isHistoricalView();

		markCollectableAsDeleted(clct);
	}

	private GenericObjectVO getSelectedGenericObjectCVO() {
		final CollectableGenericObject clctlo = getSelectedCollectable();
		return (clctlo == null) ? null : clctlo.getGenericObjectCVO();
	}

	private void markCollectableAsDeleted(CollectableGenericObjectWithDependants clctlo) throws CommonBusinessException {
		lodelegate.remove(clctlo.getGenericObjectWithDependantsCVO(), false);
	}

	protected void checkedDeleteCollectablePhysically(final CollectableGenericObjectWithDependants clctlo) throws CommonBusinessException {
		if (!isPhysicallyDeleteAllowed(clctlo))
			throw new CommonPermissionException(CommonLocaleDelegate.getMessage("GenericObjectCollectController.41","Endg\u00fcltiges L\u00f6schen ist nicht erlaubt."));

		lodelegate.remove(clctlo.getGenericObjectWithDependantsCVO(), true);
		broadcastCollectableEvent(clctlo, MessageType.DELETE_DONE);

		UIUtils.invokeOnDispatchThread(new Runnable() {
			@Override
			public void run() {
				GenericObjectCollectController.this.getResultTableModel().remove(clctlo);
			}
		});

		setHistoricalDate(null);

		refreshResult();
	}

	protected void checkedRestoreCollectable(CollectableGenericObjectWithDependants clct) throws CommonBusinessException {
		if (!isPhysicallyDeleteAllowed(clct))
			throw new CommonPermissionException(CommonLocaleDelegate.getMessage("GenericObjectCollectController.100","Wiederherstellen ist nicht erlaubt."));
		lodelegate.restore(clct.getGenericObjectWithDependantsCVO());
	}


	/**
	 * @return ?
	 * @deprecated This is a workaround for visibility issues of getSelectedCollectables(), only used for deletion of objects.
	 * @todo Check if we need to call getCompleteSelectedCollectables for deletion
	 */
	@Deprecated
	public final List<CollectableGenericObjectWithDependants> getListOfSelectedCollectables() {
		return super.getSelectedCollectables();
	}

	/**
	 * @return the module id for leased objects collected in this controller. <code>null</code> for "general search".
	 */
	public final Integer getModuleId() {
		return iModuleId;
	}

	/**
	 * @return the module id of the selected Collectable, if any. The module id of this controller, otherwise.
	 */
	protected Integer getSelectedCollectableModuleId() {
		final GenericObjectVO govo = getSelectedGenericObjectCVO();
		return (govo == null) ? getModuleId() : Integer.valueOf(govo.getModuleId());
	}

	@Override
	public final String getEntityName() {
		return this.getEntityName(getModuleId());
	}

	protected final String getEntityName(Integer iModuleId) {
		return Modules.getInstance().getEntityNameByModuleId(iModuleId);
	}

	@Override
	protected String getEntityLabel() {
		return getModuleLabel(getModuleId());
	}

	protected final String getModuleLabel(Integer iModuleId) {
		return Modules.getInstance().getEntityLabelByModuleId(iModuleId);
	}

	/**
	 * @return the id of the current (selected) entity object, if any.
	 */
	private Integer getSelectedGenericObjectId() {
		return (Integer) getSelectedCollectableId();
	}

	/**
	 * @return the state of the selected entity object, if any.
	 */
	private CollectableField getSelectedGenericObjectState() {
		final CollectableField result;
		final Collectable clct = getSelectedCollectable();
		if (clct == null)
			result = null;
		else {
			result = clct.getField(NuclosEOField.STATE.getMetaData().getField());
			log.debug("getCurrentGenericObjectState: (value=" + result.getValue() + ", id=" + result.getValueId() + ")");
		}
		return result;
	}

	/**
	 * @return the state name of the selected entity object, if any.
	 */
	private String getSelectedGenericObjectStateName() {
		final CollectableField clctfState = getSelectedGenericObjectState();
		return (clctfState == null) ? null : CommonLocaleDelegate.getResource(
			StateDelegate.getInstance().getStatemodelClosure(getModuleId()).getResourceSIdForLabel((Integer)clctfState.getValueId()),
			LangUtils.toString(clctfState.getValue()));
	}

	/**
	 * @return the state id of the selected entity object, if any.
	 */
	private Integer getSelectedGenericObjectStateId() {
		final CollectableField clctfState = getSelectedGenericObjectState();
		return (clctfState == null) ? null : (Integer) clctfState.getValueId();
	}

	/**
	 * @return the state numeral of the selected entity object, if any.
	 */
	private Integer getSelectedGenericObjectStateNumeral() {
		final Integer result;
		final Collectable clct = getSelectedCollectable();
		if (clct == null)
			result = null;
		else
			result = (Integer) clct.getValue(NuclosEOField.STATENUMBER.getMetaData().getField());

		return result;
	}

	/**
	 * Delete the selected object physically.
	 * This is mostly a copy from CollectController.cmdDelete; just the message and the called delete method are different.
	 */
	private void cmdDeletePhysically() {
		assert getCollectStateModel().getCollectState().equals(new CollectState(CollectState.OUTERSTATE_DETAILS, CollectState.DETAILSMODE_VIEW));

		if (stopEditingInDetails()) {
			final String sMessage = CommonLocaleDelegate.getMessage("GenericObjectCollectController.73","Soll der angezeigte Datensatz ({0}) wirklich endg\u00fcltig aus der Datenbank gel\u00f6scht werden?\nDieser Vorgang kann nicht r\u00fcckg\u00e4ngig gemacht werden! ", getSelectedCollectable().getIdentifierLabel());
			final int iBtn = JOptionPane.showConfirmDialog(getFrame(), sMessage, CommonLocaleDelegate.getMessage("GenericObjectCollectController.24","Datensatz endg\u00fcltig l\u00f6schen"), JOptionPane.YES_NO_OPTION);

			if (iBtn == JOptionPane.OK_OPTION)
				UIUtils.runCommand(getFrame(), new Runnable() {
					@Override
					public void run() {
						try {
							// try to find next or previous object:
							final JTable tblResult = getResultTable();
							final int iSelectedRow = tblResult.getSelectedRow();
							if (iSelectedRow < 0)
								throw new IllegalStateException();

							final int iNewSelectedRow;
							if (iSelectedRow < tblResult.getRowCount() - 1)
								// the selected row is not the last row: select the next row
								iNewSelectedRow = iSelectedRow;
							else if (iSelectedRow > 0)
								// the selected row is not the first row: select the previous row
								iNewSelectedRow = iSelectedRow - 1;
							else {
								// the selected row is the single row: don't select a row
								assert tblResult.getRowCount() == 1;
								assert iSelectedRow == 0;
								iNewSelectedRow = -1;
							}

							checkedDeleteCollectablePhysically(getSelectedCollectable());

							if (iNewSelectedRow == -1) {
								tblResult.clearSelection();
								// switch to new mode:
								setCollectState(CollectState.OUTERSTATE_DETAILS, CollectState.DETAILSMODE_NEW);
							}
							else {
								tblResult.setRowSelectionInterval(iNewSelectedRow, iNewSelectedRow);
								// go into view mode again:
								setCollectState(CollectState.OUTERSTATE_DETAILS, CollectState.DETAILSMODE_VIEW);
							}
						}
						catch (CommonPermissionException ex) {
							final String sErrorMessage = CommonLocaleDelegate.getMessage("GenericObjectCollectController.70","Sie verf\u00fcgen nicht \u00fcber die ausreichenden Rechte, um dieses Objekt zu l\u00f6schen.");
							Errors.getInstance().showExceptionDialog(getFrame(), sErrorMessage, ex);
						}
						catch (CommonBusinessException ex) {
							if (!handlePointerException(ex))
								Errors.getInstance().showExceptionDialog(getFrame(), CommonLocaleDelegate.getMessage("GenericObjectCollectController.18","Das Objekt konnte nicht gel\u00f6scht werden."), ex);
						}
					}
				});
		}
	}

	private void cmdJumpToTree() {
		UIUtils.runCommand(getFrame(), new CommonRunnable() {
			@Override
			public void run() throws CommonFinderException {
				CollectableGenericObjectWithDependants cgo = getSelectedCollectable();
				if(cgo != null)
					getExplorerController().showInOwnTab(
						ExplorerDelegate.getInstance().getGenericObjectTreeNode(cgo.getId(),
							cgo.getGenericObjectWithDependantsCVO().getModuleId()));
			}
		});
	}

	private void cmdShowStateHistory() {
		UIUtils.runCommand(getFrame(), new CommonRunnable() {
			@Override
			public void run() throws CommonBusinessException {
				final CollectableGenericObject clctSelected = getSelectedCollectable();

				final int iGenericObjectId = clctSelected.getGenericObjectCVO().getId();
				final String sIdentifier = clctSelected.getIdentifierLabel();

				new StateHistoryController(parent).run(getSelectedCollectableModuleId(), iGenericObjectId, sIdentifier);
			}
		});
	}

	private void cmdShowLogBook() {
		UIUtils.runCommand(getFrame(), new Runnable() {
			@Override
			public void run() {
				final CollectableGenericObject clctSelected = getSelectedCollectable();

				assert clctSelected != null;

				final int iGenericObjectId = clctSelected.getGenericObjectCVO().getId();
				final String sIdentifier = clctSelected.getIdentifierLabel();

				try {
					/** @todo frame vs. parent */
					new LogbookController(getFrame(), parent, getSelectedCollectableModuleId(), iGenericObjectId, getPreferences()).run(sIdentifier);
				}
				catch (CommonBusinessException ex) {
					Errors.getInstance().showExceptionDialog(getFrame(), ex);
				}
			}
		});
	}

	/**
	 * command: switch to View mode
	 */
	@Override
	protected void cmdEnterViewMode() {
		if (isSelectedCollectableMarkedAsDeleted())
			if (!SecurityCache.getInstance().isActionAllowed(Actions.ACTION_READ_DELETED_RECORD))
				throw new NuclosFatalException(CommonLocaleDelegate.getMessage("GenericObjectCollectController.65","Sie haben nicht die erforderlichen Rechte, gel\u00f6schte Datens\u00e4tze zu lesen."));

		super.cmdEnterViewMode();
	}

	/**
	 * alternative entry point: view single historical object
	 * @param clct the object to view in details
	 */
	public final void runViewSingleHistoricalCollectable(CollectableGenericObjectWithDependants clct, Date dateHistorical) {
		viewSingleHistoricalCollectable(clct, dateHistorical);
		getFrame().setVisible(true);
	}

	private void viewSingleHistoricalCollectable(CollectableGenericObjectWithDependants clct, Date dateHistorical) {
		// fill result table:
		final List<CollectableGenericObjectWithDependants> lstResult = new ArrayList<CollectableGenericObjectWithDependants>();
		lstResult.add(clct);
		this.fillResultPanel(lstResult);

		pnlCollect.getResultPanel().getResultTable().setRowSelectionInterval(0, 0);
		// select the one result row

		setHistoricalDate(dateHistorical);

		cmdEnterViewMode();
		disableToolbarButtonsForHistoricalView();

	}

	private void disableToolbarButtonsForHistoricalView() {
		pnlCollect.setTabbedPaneEnabledAt(CollectState.OUTERSTATE_SEARCH, false);
		pnlCollect.setTabbedPaneEnabledAt(CollectState.OUTERSTATE_RESULT, false);
		getDetailsPanel().btnDelete.setEnabled(false);
		actDeleteSelectedCollectablesPhysically.setEnabled(false);
		btnExecuteRule.setEnabled(false);
		btnMakeTreeRoot.setEnabled(false);
		getRefreshCurrentCollectableAction().setEnabled(false);
		btnShowLogBook.setEnabled(false);
		btnShowStateHistory.setEnabled(false);
		getLastAction().setEnabled(false);
		getFirstAction().setEnabled(false);
		getNewAction().setEnabled(false);
	}

	/**
	 * alternative entry point: view multiple objects in Details (multi edit) or result panel
	 * @param lstclct the objects to view in Details
	 * @param bShowInDetails Show in Details panel? (false: show in Result panel)
	 */
	public final void runViewMultipleCollectables(List<CollectableGenericObjectWithDependants> lstclct, boolean bShowInDetails) {
		viewMultipleCollectables(lstclct, bShowInDetails);
		getFrame().setVisible(true);
	}

	/**
	 * @param lstclct the objects to view in Result or Details panel
	 * @param bShowInDetails Show in Details panel? (false: show in Result panel)
	 */
	private void viewMultipleCollectables(List<CollectableGenericObjectWithDependants> lstclct, boolean bShowInDetails) {
		// set search condition to match the result (so that refresh will give the same result):
		final Collection<Object> collIds = CollectionUtils.transform(lstclct, new Collectable.GetId());
		final CollectableSearchCondition cond = SearchConditionUtils.getCollectableSearchConditionForIds(collIds);
		setCollectableSearchConditionInSearchPanel(cond);

		// fill result table:
		this.fillResultPanel(lstclct);

		// select all result rows:
		pnlCollect.getResultPanel().getResultTable().setRowSelectionInterval(0, lstclct.size() - 1);

		if (bShowInDetails)
			cmdEnterMultiViewMode();
		else
			UIUtils.runCommand(getFrame(), new CommonRunnable() {
				@Override
				public void run() throws CommonBusinessException {
					GenericObjectCollectController.this.setCollectState(CollectState.OUTERSTATE_RESULT, CollectState.RESULTMODE_NOSELECTION);
				}
			});
	}

	/**
	 * shows/hides the buttons for switching to subsequent states
	 * @param bVisible
	 * @param bEnableButtons
	 */
	private void setSubsequentStatesVisible(boolean bVisible, boolean bEnableButtons) {
		// remove all previous listeners:
		for (ActionListener listener : cmbbxCurrentState.getActionListeners())
			cmbbxCurrentState.removeActionListener(listener);

		// initialize:
		cmbbxCurrentState.removeAllItems();

		if (!bVisible)
			cmbbxCurrentState.setVisible(false);
		else {
			cmbbxCurrentState.setVisible(true);
			cmbbxCurrentState.setEnabled(bEnableButtons);
			final Integer iGenericObjectId = getSelectedGenericObjectId();
			if (iGenericObjectId != null) {
				// Create a temporary list for sorting the entries before entering into combo box
				final List<StateWrapper> lstComboEntries = new ArrayList<StateWrapper>();

				final StateWrapper stateCurrent = new StateWrapper(null, getSelectedGenericObjectStateNumeral(),
					getSelectedGenericObjectStateName());
				lstComboEntries.add(stateCurrent);



				UsageCriteria uc = getUsageCriteria(getSelectedCollectable());
				DynamicAttributeVO av = getSelectedCollectable().getGenericObjectCVO().getAttribute(NuclosEOField.STATE.getMetaData().getId().intValue());
				List<StateVO> lstSubsequentStates = StateDelegate.getInstance().getStatemodel(uc).getSubsequentStates(av.getValueId(), false);

				// Copy all subsequent states to the sorting list:
				for (StateVO statevo : lstSubsequentStates)
					if (statevo == null)
						// we don't want to throw an exception here, so we just log the error:
						log.error("Die Liste der Folgestati enth\u00e4lt ein null-Objekt.");
					else if (!lstComboEntries.contains(new StateWrapper(statevo.getId(), statevo.getNumeral(), statevo.getStatename())))
						lstComboEntries.add(new StateWrapper(statevo.getId(), statevo.getNumeral(),
							CommonLocaleDelegate.getResource(/*StateDelegate.getInstance().getResourceSIdForName(statevo.getId()*/
								StateDelegate.getInstance().getStatemodelClosure(getModuleId()).getResourceSIdForLabel(statevo.getId()
								),
								statevo.getStatename())));

				// Sort and finally enter the items into the combo box:
				Collections.sort(lstComboEntries);
				for (StateWrapper state : lstComboEntries)
					cmbbxCurrentState.addItem(state);
				cmbbxCurrentState.setSelectedItem(stateCurrent);

				final ActionListener al = new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent ev) {
						UIUtils.runCommandLater(parent, new CommonRunnable() {
							@Override
							public void run() {
								final StateWrapper state = (StateWrapper) cmbbxCurrentState.getSelectedItem();
								if (state != stateCurrent && state != null && state.getId() != null) {
									final boolean bUserPressedOk = cmdChangeState(state);
									if (!bUserPressedOk)
										cmbbxCurrentState.setSelectedItem(stateCurrent);
								}
							}
						});
					}
				};
				cmbbxCurrentState.addActionListener(al);
			}
		}

		cmbbxCurrentState.setEnabled(cmbbxCurrentState.getItemCount() != 0);
	}

	private void showCustomActions(int iDetailsMode) {
		final boolean bSingle = CollectState.isDetailsModeViewOrEdit(iDetailsMode);
		final boolean bMulti = CollectState.isDetailsModeMultiViewOrEdit(iDetailsMode);
		final boolean bViewOrEdit = bSingle || bMulti;
		final boolean bView = bViewOrEdit && !CollectState.isDetailsModeChangesPending(iDetailsMode);

		this.getDetailsPanel().removeToolBarComponents(toolbarCustomActionsDetails);
		//toolbarCustomActionsDetails.removeAll();
		toolbarCustomActionsDetails.clear();
		if (toolbarCustomActionsDetailsIndex == -1) {
			return; //
		}

		if (!bViewOrEdit)
			toolbarCustomActionsDetails.add(new BlackLabel(cmbbxCurrentState, CommonLocaleDelegate.getMessage("nuclos.entityfield.eo.state.label","Status")));
		else
			// button: "print details":
			if (isHistoricalView())
				toolbarCustomActionsDetails.add(new BlackLabel(cmbbxCurrentState, CommonLocaleDelegate.getMessage("nuclos.entityfield.eo.state.label","Status")));
			else {
				if (bSingle) {
					/** @todo print historical order */
					toolbarCustomActionsDetails.add(btnPrintDetails);
					btnPrintDetails.setEnabled(bView && hasFormsAssigned(getSelectedCollectable()));
				}

				toolbarCustomActionsDetails.add(new BlackLabel(cmbbxCurrentState, CommonLocaleDelegate.getMessage("nuclos.entityfield.eo.state.label","Status")));

				// buttons/actions for "generate leased object":
				try {
					if (!isSelectedCollectableMarkedAsDeleted())
						if (bSingle) {
							final UsageCriteria usagecriteria = getUsageCriteriaFromView(false);
							final Integer iStateNumeral = getSelectedGenericObjectStateNumeral();
							if (iStateNumeral == null)
								log.info("Keinen aktuellen Zustand gefunden f\u00fcr GenericObject mit Id " + getSelectedGenericObjectId() + ".");
							else
								showGeneratorActions(bView, getModuleId(), iStateNumeral, usagecriteria.getProcessId());
						}
						else
							try {
								final Integer iProcessId = getSelectedGenericObjectsCommonFieldIdByFieldName(NuclosEOField.PROCESS.getMetaData().getField());
								final Integer iStateNumeral = getSelectedGenericObjectsCommonStateNumeral();
								showGeneratorActions(bView, getModuleId(), iStateNumeral, iProcessId);
							}
					catch (NoSuchElementException ex) {
						log.info("Keinen aktuellen Zustand gefunden f\u00fcr GenericObject mit Id " + getSelectedGenericObjectId() + ".");
					}
				}
				catch (CollectableFieldFormatException ex) {
					throw new NuclosFatalException(CommonLocaleDelegate.getMessage("GenericObjectCollectController.61","Prozess-Id ist ung\u00fcltig."), ex);
				}
				UIUtils.ensureMinimumSize(getFrame());
			}

		//if (SecurityCache.getInstance().isActionAllowed(Actions.ACTION_USE_INVALID_MASTERDATA)) {
			//toolbarCustomActionsDetails.add(Box.createHorizontalStrut(5));
			//toolbarCustomActionsDetails.add(chkbxUseInvalidMasterData);
			//toolbarCustomActionsDetails.add(Box.createHorizontalStrut(5));
		//}

		//toolbarCustomActionsDetails.revalidate();
		this.getDetailsPanel().addToolBarComponents(toolbarCustomActionsDetails, toolbarCustomActionsDetailsIndex);
	}

	protected final ExplorerController getExplorerController() {
		return Main.getMainController().getExplorerController();
	}

	/** @todo consider to move this one to [nucleus.]common */

	private static class NoSuchElementException extends NuclosBusinessException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
	}

	/**
	 * @return the common field "value" of the given Collectables. This value is built using the given <code>transformer</code>
	 *         on each <code>Collectable</code>'s field with the given name. The transformer will usually be <code>GetValue</code> or <code>GetValueId</code>.
	 * @throws NoSuchElementException if there is no common field "value" (or if the given <code>Collection</code> is empty)
	 * @postcondition result != null
	 */
	private static Object getCollectablesCommonField(Collection<? extends Collectable> collclct, String sFieldName,
		Transformer<CollectableField, Object> transformer) throws NoSuchElementException {
		if (collclct.isEmpty())
			throw new NoSuchElementException();
		final Iterator<? extends Collectable> iter = collclct.iterator();
		final Object result = transformer.transform(iter.next().getField(sFieldName));
		while (iter.hasNext())
			if (!LangUtils.equals(result, transformer.transform(iter.next().getField(sFieldName))))
				throw new NoSuchElementException();
		return result;
	}

	private Integer getSelectedGenericObjectsCommonFieldIdByFieldName(String sFieldName) throws NoSuchElementException {
		return (Integer) getCollectablesCommonField(getSelectedCollectables(), sFieldName, new CollectableField.GetValueId());
	}

	/**
	 * @return the common state numeral of the selected leased objects.
	 * @throws NoSuchElementException if there is no common state numeral (or if there is no selected object at all)
	 * @postcondition result != null
	 */
	private Integer getSelectedGenericObjectsCommonStateNumeral() throws NoSuchElementException {
		final Integer result = (Integer) getCollectablesCommonField(getSelectedCollectables(), NuclosEOField.STATENUMBER.getMetaData().getField(), new CollectableField.GetValue());
		assert result != null;
		return result;
	}

	/**
	 * @return do the selected leased objects share a common state?
	 */
	private boolean doTheSelectedGenericObjectsShareACommonState() {
		boolean result;
		try {
			getSelectedGenericObjectsCommonStateNumeral();
			result = true;
		}
		catch (NoSuchElementException ex) {
			result = false;
		}
		return result;
	}

	private void showGeneratorActions(boolean bEnableButtons, Integer iModuleId, Integer iStateNumeral,
		Integer iProcessId) {
		final List<GeneratorActionVO> lstActions = GeneratorActions.getActions(iModuleId, iStateNumeral, iProcessId);
		if (lstActions.size() > 0) {
			final JComboBox cmbbxActions = new JComboBox(lstActions.toArray());
			cmbbxActions.setSelectedItem(null);
			cmbbxActions.setEnabled(bEnableButtons);
			//toolbarCustomActionsDetails.addSeparator();
			// workaround for cellrenderer
			int w = cmbbxActions.getPreferredSize().width;
			if(w < 100)
				w = 100;
			cmbbxActions.setPreferredSize(new Dimension(w, cmbbxActions.getPreferredSize().height));
			UIUtils.setMaximumSizeToPreferredSize(cmbbxActions);

			toolbarCustomActionsDetails.add(cmbbxActions);
			if (cmbbxActions.isEnabled())
				cmbbxActions.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent ev) {
						UIUtils.runCommandLater(parent, new CommonRunnable() {
							@Override
							public void run() {
								if (cmbbxActions.getSelectedItem() instanceof GeneratorActionVO)
									try {
										cmdGenerateGenericObject((GeneratorActionVO) cmbbxActions.getSelectedItem());
									}
								finally {
									cmbbxActions.setSelectedItem(null);
								}
							}
						});
					}
				});
			final ListCellRenderer originalRenderer = cmbbxActions.getRenderer();
			cmbbxActions.setRenderer(new DefaultListCellRenderer() {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				@Override
				public Component getListCellRendererComponent(JList jlst, Object oValue, int iIndex, boolean bSelected,
					boolean bHasFocus) {
					final JLabel result = (JLabel) originalRenderer.getListCellRendererComponent(jlst, oValue, iIndex, bSelected,
						bHasFocus);
					result.setOpaque(false);

					if (oValue == null) {
						result.setText(CommonLocaleDelegate.getMessage("GenericObjectCollectController.9","Arbeitsschritt"));
						result.setToolTipText(CommonLocaleDelegate.getMessage("GenericObjectCollectController.17","Bitte w\u00e4hlen Sie einen Arbeitsschritt aus."));
					}
					else {
						assert oValue instanceof GeneratorActionVO;
						final GeneratorActionVO actvo = (GeneratorActionVO) oValue;
						final String sSourceModuleName = getModuleLabel(getSelectedCollectableModuleId());
						final String sTargetModuleName = getModuleLabel(actvo.getTargetModuleId());
						result.setToolTipText(CommonLocaleDelegate.getMessage("GenericObjectCollectController.46","Erzeugt einen {0} aus dem/der aktuellen {1}", sTargetModuleName, sSourceModuleName));
					}
					return result;
				}
			});
		}
	}

	/**
	 * @param stateNew
	 * @return Did the user press ok?
	 * @precondition !this.isHistoricalView()
	 * @precondition this.getCollectState().isDetailsMode()
	 * NUCLEUSINT-1159 needed for accessing the statechange for status button
	 */
	public boolean cmdChangeState(final StateWrapper stateNew) {
		if (isHistoricalView())
			throw new IllegalStateException(CommonLocaleDelegate.getMessage("GenericObjectCollectController.90","Statuswechsel ist in historischer Ansicht nicht m\u00f6glich."));
		if (!getCollectState().isDetailsMode())
			throw new IllegalStateException(CommonLocaleDelegate.getMessage("GenericObjectCollectController.91","Statuswechsel ist nur in Detailmodus m\u00f6glich."));

		final boolean bMultiEdit = getCollectState().isDetailsModeMultiViewOrEdit();

		String sQuestion = bMultiEdit
		? CommonLocaleDelegate.getMessage("GenericObjectCollectController.79","Soll der Wechsel in den Status {0} f\u00fcr die ausgew\u00e4hlten Objekte wirklich durchgef\u00fchrt werden?\nDie vorgenommenen \u00c4nderungen an dem Objekt werden gespeichert.", stateNew.getCombinedStatusText())
			: CommonLocaleDelegate.getMessage("GenericObjectCollectController.80","Soll der Wechsel in den Status {0} wirklich durchgef\u00fchrt werden?", stateNew.getCombinedStatusText());

		final int btn = JOptionPane.showConfirmDialog(getFrame(), sQuestion, CommonLocaleDelegate.getMessage("GenericObjectCollectController.85","Statuswechsel durchf\u00fchren"),
			JOptionPane.OK_CANCEL_OPTION);

		// repaint directly:
		//getFrame().repaint();

		final boolean result = (btn == JOptionPane.OK_OPTION);

		stopEditingInDetails();

		final StateWrapper stateCurrent = new StateWrapper(null, getSelectedGenericObjectStateNumeral(), getSelectedGenericObjectStateName());

		if (result)
			UIUtils.runShortCommand(getFrame(), new CommonRunnable() {
				@Override
				public void run() throws CommonBusinessException {
					if (getCollectStateModel().getDetailsMode() == CollectState.DETAILSMODE_EDIT ||
						getCollectStateModel().getDetailsMode() == CollectState.DETAILSMODE_NEW_CHANGED) {
						if (bMultiEdit) {
							getSubFormsLoader().setAfterLoadingRunnable(new CommonRunnable() {
								@Override
								public void run() throws CommonBusinessException {
									GenericObjectCollectController.this.changeStateForMultipleObjects(stateNew);
								}
							});
							try {
								save();
							}
							catch (CommonBusinessException ex) {
								getSubFormsLoader().setAfterLoadingRunnable(null);
								final String sErrorMsg = CommonLocaleDelegate.getMessage("GenericObjectCollectController.34","Der Statuswechsel konnte nicht vollzogen werden.");
								handleSaveException(ex, sErrorMsg);

								// redisplay the old status
								cmbbxCurrentState.setSelectedItem(stateCurrent);
							}
						} else {
							GenericObjectCollectController.this.changeStateForSingleObjectAndSave(stateNew);
						}

					}
					else if (bMultiEdit)
						GenericObjectCollectController.this.changeStateForMultipleObjects(stateNew);
					else
						GenericObjectCollectController.this.changeStateForSingleObjectAndSave(stateNew);
				}
			});
		return result;
	}

	private void changeStateForSingleObjectAndSave(final StateWrapper stateNew) {
		CommonMultiThreader.getInstance().execute(new CommonClientWorkerAdapter<CollectableGenericObjectWithDependants>(GenericObjectCollectController.this) {
			Integer iGenericObjectId;
			Integer iModuleId;
			StateWrapper stateCurrent;
			CollectableGenericObjectWithDependants clct;

			boolean errorOccurred = false;

			@Override
			public void init() throws CommonBusinessException {
				super.init();
				iGenericObjectId = GenericObjectCollectController.this.getSelectedGenericObjectId();
				iModuleId = getSelectedCollectableModuleId();
				assert iGenericObjectId != null;

				stateCurrent = new StateWrapper(null, getSelectedGenericObjectStateNumeral(), getSelectedGenericObjectStateName());
				clct = GenericObjectCollectController.this.getCompleteSelectedCollectable();
			}

			@Override
			public void work() throws CommonBusinessException {
				if (GenericObjectCollectController.this.changesArePending()) {
					// NUCLOSINT-1114:
					// Value must be 'true' to save the changed SubForm data to DB. (Thomas Pasch)
					CollectableGenericObjectWithDependants updated = GenericObjectCollectController.this.updateCurrentCollectable(true);

					StateDelegate.getInstance().changeStateAndModify(iModuleId, updated.getGenericObjectWithDependantsCVO(), stateNew.getId());
				} else {
					StateDelegate.getInstance().changeState(iModuleId, iGenericObjectId, stateNew.getId());
				}
				broadcastCollectableEvent(clct, MessageType.STATECHANGE_DONE);

				// We have to reload the current leased object, as some fields might have changed:
				// . nuclosState because of the status change
				// . other fields because of business rules
				if (!errorOccurred)
					GenericObjectCollectController.this.refreshCurrentCollectable(false);
			}

			@Override
			public void paint() throws CommonBusinessException {
				super.paint();
			}

			@Override
			public void handleError(Exception ex) {
				errorOccurred = true;
				if (GenericObjectCollectController.this.handlePointerException(ex)) {
					final PointerException pex = PointerException.extractPointerExceptionIfAny(ex);
					if (pex != null) {
						GenericObjectCollectController.this.setCollectableComponentModelsInDetailsMandatoryAdded(pex.getPointerCollection().getFields());
					}
				} else {
					final String sErrorMsg = CommonLocaleDelegate.getMessage("GenericObjectCollectController.34","Der Statuswechsel konnte nicht vollzogen werden.");
					Errors.getInstance().showExceptionDialog(getFrame(), sErrorMsg, ex);
				}

				// redisplay the old status
				cmbbxCurrentState.setSelectedItem(stateCurrent);
			}
		});
	}

	private void changeStateForMultipleObjects(final StateWrapper stateNew) throws CommonBusinessException {
		new ChangeStateForSelectedCollectablesController(this, stateNew).run(getMultiActionProgressPanel(getSelectedCollectables().size()));
	}

	/**
	 * generates one or more leased object(s) from current.
	 * @param generatoractionvo generator action vo to be used for generation
	 */
	private void cmdGenerateGenericObject(final GeneratorActionVO generatoractionvo) {
		assert !isHistoricalView();
		try {
			final String sTargetModuleName = getModuleLabel(generatoractionvo.getTargetModuleId());

			final boolean bMulti = CollectState.isDetailsModeMultiViewOrEdit(getCollectState().getInnerState());
			final String sSourceModuleName = getModuleLabel(getSelectedCollectableModuleId());

			final int iBtn = confirmGenerationType(bMulti, sSourceModuleName, sTargetModuleName, generatoractionvo);

			if (iBtn != JOptionPane.CANCEL_OPTION) {
				final AtomicReference<Integer> parameterObjectIdRef = new AtomicReference<Integer>();
				final CommonRunnable generateRunnable = new CommonRunnable() {
					@Override
					public void run() throws CommonBusinessException {
						Integer parameterObjectId = parameterObjectIdRef.get();
						if (bMulti && iBtn == JOptionPane.YES_OPTION)
							generateGenericObjectFromMultipleSources(parameterObjectId, generatoractionvo);
						else
							generateGenericObjectsFromSingleSources(parameterObjectId, generatoractionvo);
					}
				};
				if (generatoractionvo.getParameterEntityId() != null) {
					UIUtils.runShortCommand(getFrame(), new CommonRunnable() {
						@Override
						public void run() throws CommonBusinessException {
							final EntityMetaDataVO parameterEntity = 
								MetaDataClientProvider.getInstance().getEntity(generatoractionvo.getParameterEntityId().longValue());
							final String pEntityStr = parameterEntity.getEntity();
							/*
							final CollectableEntity pEntitiy = 
								DefaultCollectableEntityProvider.getInstance().getCollectableEntity(pEntityStr);
							 */
							
							final MainFrameTab tab = GenericObjectCollectController.this.getFrame();
							final ICollectableListOfValues lov = new EntityListOfValues(tab);
							final CollectController<?> ctl = NuclosCollectControllerFactory.getInstance().newCollectController(
								tab, pEntityStr, null);
							lov.addLookupListener(new LookupListener() {
								@Override
								public void lookupSuccessful(LookupEvent ev) {
									Collectable clct = ev.getSelectedCollectable();
									if (clct != null) {
										parameterObjectIdRef.set((Integer) clct.getId());
									}
									UIUtils.runShortCommand(getFrame(), generateRunnable);
								}
								@Override
								public int getPriority() {
									return 1;
								}
							});
							ctl.runLookupCollectable(lov);
						}
					});
				} else {
					UIUtils.runShortCommand(getFrame(), generateRunnable);
				}
			}
		}
		catch (Exception ex) {
			Errors.getInstance().showExceptionDialog(getFrame(), ex);
		}
	}

	/**
	 * Execute object generation once for every single selected leased object.
	 * Performed in an own thread.
	 * @param generatoractionvo
	 */
	private void generateGenericObjectsFromSingleSources(final Integer parameterObjectId, final GeneratorActionVO generatoractionvo) throws CommonBusinessException {
		final List<CollectableGenericObjectWithDependants> lstClct = GenericObjectCollectController.this.getCompleteSelectedCollectables();

		if (lstClct.size() == 1) {
			final Integer sourceId = lstClct.get(0).getId();
			CommonMultiThreader.getInstance().execute(
				new CommonClientWorkerAdapter<CollectableGenericObjectWithDependants>(GenericObjectCollectController.this) {
					private volatile GenericObjectVO generatedGo;

					@Override
					public void work() throws CommonBusinessException {
						generatedGo = generatordelegate.generateGenericObject(sourceId, parameterObjectId, generatoractionvo);
					}

					@Override
					public void paint() throws CommonBusinessException {
						if (generatedGo == null)
							return;

						Integer generatedGoId = generatedGo.getId();
						if (SecurityCache.getInstance().isWriteAllowedForModule(Modules.getInstance().getEntityNameByModuleId(generatoractionvo.getTargetModuleId()), generatedGoId))
							if (generatedGoId != null)
								showGenericObject(generatedGoId, generatoractionvo.getTargetModuleId(), false);
							else {
								GenericObjectCollectController goclct = showIncompleteGenericObject(sourceId, generatedGo);

								if (showObjectGenerationWarningIfNewObjectIsNotSaveable()) {
									final String message = CommonLocaleDelegate.getMessage("R00022889", "Der Datensatz konnte nicht erstellt werden.\n\u00dcberpr\u00fcfen Sie alle Pflichtfelder.");
									goclct.setPointerInformation(new PointerCollection(message), null);
									//JOptionPane.showMessageDialog(goclct.parent,
									//	CommonLocaleDelegate.getMessage("R00022889", "Der Datensatz konnte nicht erstellt werden.\n\u00dcberpr\u00fcfen Sie alle Pflichtfelder."),
									//	CommonLocaleDelegate.getMessage("R00022892", "Objektgenerierung"),
									//	JOptionPane.ERROR_MESSAGE);
								}
							}
						super.paint();
					}

					@Override
					public void handleError(Exception ex) {
						if (handlePointerException(ex))
							GenericObjectCollectController.this.forceUnlockFrame();
						else
							super.handleError(ex);
					}


				});
		} else if (lstClct.size() > 1) {
			MultiActionProgressPanel panel = new MultiActionProgressPanel(lstClct.size());
			panel.setResultHandler(new MultiActionProgressResultHandler(this) {
				@Override
				public void handleMultiSelection(Collection<Integer> objectIds) {
					((GenericObjectCollectController) controller).openGenericObjectController(objectIds);
				}
			});
			new MultiCollectablesActionController<CollectableGenericObjectWithDependants, GenericObjectVO>(
				this, CommonLocaleDelegate.getMessage("R00022892", "Objektgenerierung"),
				new MultiGenerateAction(this, parameterObjectId, generatoractionvo), lstClct
			).run(panel);
		}
	}

	protected boolean showObjectGenerationWarningIfNewObjectIsNotSaveable() {
		return true;
	}

	@Override
	public void save() throws CommonBusinessException {
		super.save();
		if (bGenerated) {
			if (getSelectedGenericObjectId() != null) { // could be null if save is not possible (e.g. mandatory fields)
				lodelegate.relate(iGenericObjectIdSource, GenericObjectTreeNode.SystemRelationType.PREDECESSOR_OF.getValue(), getSelectedGenericObjectId(), getModuleId(), null, null, null);
				bGenerated = false;
			}
		}
	}

	/**
	 * resets the temporary stored data which was hold while switching the layout
	 */
	private void resetTransferedDetailsData() {
		transferredDetailsData.clear();
	}

	/**
	 * Execute object generation once for all selected leased objects.
	 * Performed in an own thread.
	 * @param generatoractionvo
	 */
	private void generateGenericObjectFromMultipleSources(final Integer parameterObjectId, final GeneratorActionVO generatoractionvo) {
		CommonMultiThreader.getInstance().execute(new CommonClientWorkerAdapter<CollectableGenericObjectWithDependants>(GenericObjectCollectController.this) {
			private Collection<Integer> collSelectedIds;
			private Collection<Integer> collTargetIds;

			@Override
			public void init() throws CommonBusinessException {
				super.init();
				// Get a collection of the ids of all selected collectables
				collSelectedIds = CollectionUtils.transform(getSelectedCollectables(), new Transformer<Collectable, Integer>() {
					@Override
					public Integer transform(Collectable o) {
						return (Integer) o.getId();
					}
				});
			}

			@Override
			public void work() throws CommonBusinessException {
				if(generatoractionvo.isGroupAttributes())
					collTargetIds = generatordelegate.generateGenericObjectFromMultipleSourcesWithAttributeGrouping(collSelectedIds, parameterObjectId, generatoractionvo);
				else {
					collTargetIds.add(generatordelegate.generateGenericObjectFromMultipleSources(collSelectedIds, parameterObjectId, generatoractionvo));
				}
			}

			@Override
			public void paint() throws CommonBusinessException {
				if(collTargetIds != null) {
					for(Integer iGen : collTargetIds) {
						showGenericObject(iGen, generatoractionvo.getTargetModuleId(), true);
					}
					super.paint();
				}
			}
		});
	}

	/**
	 * Open the generated leased object in an own controller, if possible.
	 * @param iGeneratedObjectId
	 * @param iModuleId
	 * @throws CommonBusinessException
	 */
	@SuppressWarnings("deprecation")
	private void showGenericObject(Integer iGeneratedObjectId, final Integer iModuleId, boolean multiEdit) throws CommonBusinessException {
		if (iGeneratedObjectId != null) {
			// one object was generated - show it:
			final GenericObjectVO govo = lodelegate.get(iModuleId, iGeneratedObjectId);
			final GenericObjectCollectController ctl = GenericObjectCollectController.this;

			String entity = Modules.getInstance().getEntityNameByModuleId(iModuleId);
			JTabbedPane openInTabbed;
			if (MainFrame.isPredefinedEntityOpenLocationSet(entity))
				openInTabbed = MainFrame.getPredefinedEntityOpenLocation(entity);
			else
				openInTabbed = MainFrame.getTabbedPane(GenericObjectCollectController.this.getFrame());

			GenericObjectCollectController newController = NuclosCollectControllerFactory.getInstance().newGenericObjectCollectController(
				openInTabbed, iModuleId, null);
			if(this.getCollectState().getOuterState() == CollectState.OUTERSTATE_DETAILS && !multiEdit)
				newController.addCollectableEventListener(new DetailsCollectableEventListener(ctl, newController));
			newController.runViewSingleCollectable(newCollectableGenericObject(govo));
		}
	}

	/**
	 * Open an incomplete generated object in its own controller.
	 * @throws CommonBusinessException
	 */
	GenericObjectCollectController showIncompleteGenericObject(Integer sourceId, GenericObjectVO generatedGo) throws CommonBusinessException {
		String entity = Modules.getInstance().getEntityNameByModuleId(generatedGo.getModuleId());
		JTabbedPane openInTabbed;
		if (MainFrame.isPredefinedEntityOpenLocationSet(entity))
			openInTabbed = MainFrame.getPredefinedEntityOpenLocation(entity);
		else
			openInTabbed = MainFrame.getTabbedPane(GenericObjectCollectController.this.getFrame());

		GenericObjectCollectController goclct = NuclosCollectControllerFactory.getInstance().
		newGenericObjectCollectController(openInTabbed, generatedGo.getModuleId(), null);
		goclct.setCollectState(CollectState.OUTERSTATE_DETAILS, CollectState.DETAILSMODE_NEW_CHANGED);
		goclct.bGenerated = true;
		goclct.iGenericObjectIdSource = sourceId;
		goclct.unsafeFillDetailsPanel(new CollectableGenericObjectWithDependants((GenericObjectWithDependantsVO) generatedGo));
		goclct.addCollectableEventListener(new DetailsCollectableEventListener(GenericObjectCollectController.this, goclct));
		goclct.showFrame();
		return goclct;
	}

	/**
	 * Ask the user for type of desired generation and confirmation of object generation.
	 * @param bShowDetails
	 * @param bMulti
	 * @param sSourceModuleName
	 * @param sTargetModuleName
	 * @param generatoractionvo
	 * @return selected option
	 */
	private int confirmGenerationType(boolean bMulti, String sSourceModuleName, String sTargetModuleName, GeneratorActionVO generatoractionvo) {
		final int iBtn;
		if (bMulti) {
			if(generatoractionvo.isGroupAttributes()) {
				final String sMessage = CommonLocaleDelegate.getMessage("GenericObjectCollectController.71a","Soll aus den aktuellen {0} mehrere {1} erzeugt werden?", sSourceModuleName, sTargetModuleName);
				iBtn = JOptionPane.showConfirmDialog(getFrame(), sMessage, CommonLocaleDelegate.getMessage("GenericObjectCollectController.5","{0} erzeugen", sTargetModuleName),
					JOptionPane.OK_CANCEL_OPTION);
			}
			else {
				final String sMessage = CommonLocaleDelegate.getMessage("GenericObjectCollectController.72","Soll aus den markierten Objekten vom Typ \"{0}\" eine oder mehrere Objekte vom Typ \"{1}\" erzeugt werden?", sSourceModuleName, sTargetModuleName);
				final Object[] aoOptions = {CommonLocaleDelegate.getMessage("GenericObjectCollectController.39","Eine einzige"), CommonLocaleDelegate.getMessage("GenericObjectCollectController.47","F\u00fcr jeden eine"), CommonLocaleDelegate.getMessage("GenericObjectCollectController.6","Abbrechen")};
				iBtn = JOptionPane.showOptionDialog(parent, sMessage, sTargetModuleName + " erzeugen", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
					null, aoOptions, aoOptions[0]);
			}
		}
		else {
			final String sMessage = CommonLocaleDelegate.getMessage("GenericObjectCollectController.71","Soll aus dem/der aktuellen {0} ein(e) {1} erzeugt werden?", sSourceModuleName, sTargetModuleName);
			iBtn = JOptionPane.showConfirmDialog(getFrame(), sMessage, CommonLocaleDelegate.getMessage("GenericObjectCollectController.5","{0} erzeugen", sTargetModuleName),
				JOptionPane.OK_CANCEL_OPTION);
		}
		return iBtn;
	}

	/**
	 * prints the current leased object
	 * leased object will be refreshed, when printed documents are attached to it
	 */
	private void cmdPrintCurrentGenericObject() {
		try {
			final ReportController reportController = new ReportController(getFrame());
			final List<? extends CollectableGenericObject> lstclctlo = getSelectedCollectables();
			reportController.exportForm(lstclctlo, getGreatestCommonUsageCriteriaFromCollectables(lstclctlo), getDocumentSubformEntityName());
		}
		catch (Exception ex) {
			Errors.getInstance().showExceptionDialog(getFrame(), ex);
		}
	}

	/**
	 * get the subform entity name which is used to attache documents to the generic object
	 * @return entity name
	 */
	protected String getDocumentSubformEntityName() {
		final String sEntity = this.getEntityName() + "document";

		if(MasterDataDelegate.getInstance().hasEntity(sEntity))
			return sEntity;

		return NuclosEntity.GENERALSEARCHDOCUMENT.getEntityName();
	}

	/**
	 * get if the given generic object has assigned forms
	 * @param clct
	 */
	private boolean hasFormsAssigned(CollectableGenericObjectWithDependants clct) {
		final ReportController reportController = new ReportController(getFrame());
		return reportController.hasFormsAssigned(getUsageCriteria(clct));
	}

	/**
	 * @param bSearchPanel Use SearchPanel? (false: use Details panel)
	 * @return the value ids of the quintuple fields from the view (Search or Details panel)
	 * @throws CollectableFieldFormatException
	 */
	private UsageCriteria getUsageCriteriaFromView(boolean bSearchPanel) throws CollectableFieldFormatException {
		final Integer iModuleId = bSearchPanel ? getModuleId() : getSelectedCollectableModuleId();
		return new UsageCriteria(iModuleId,
			getUsageCriteriaFieldIdFromView(bSearchPanel)
		);
	}

	/**
	 * @param bSearchPanel Use SearchPanel? (false: use Details panel)
	 * @param sSystemAttributeKey key of the quintuple field name in system parameters
	 * @return the value id of the given quintuple field
	 * @throws CollectableFieldFormatException
	 */
	private Integer getUsageCriteriaFieldIdFromView(boolean bSearchPanel)
	throws CollectableFieldFormatException {
		// 1. makeConsistent:
		for (CollectableComponent clctcomp : getEditView(bSearchPanel).getCollectableComponentsFor(NuclosEOField.PROCESS.getMetaData().getField()))
			clctcomp.makeConsistent();

		// 2. read model:
		final CollectableComponentModel clctcompmodel = getEditView(bSearchPanel).getModel().getCollectableComponentModelFor(NuclosEOField.PROCESS.getMetaData().getField());
		return (clctcompmodel == null) ? null : (Integer) clctcompmodel.getField().getValueId();
	}

	/**
	 * @param clct
	 * @return the UsageCriteria contained in the given Collectable.
	 */
	protected static UsageCriteria getUsageCriteria(CollectableGenericObject clct) {
		return new UsageCriteria(clct.getGenericObjectCVO().getModuleId(),
			getSystemAttributeId(clct, NuclosEOField.PROCESS.getMetaData().getField())
		);
	}

	private static Integer getSystemAttributeId(CollectableGenericObject clct, String sFieldName) {
		return clct.getCollectableEntity().getFieldNames().contains(sFieldName) ? (Integer) clct.getField(sFieldName).getValueId() : null;
	}

	/**
	 * @param collclct
	 * @return the greatest common quintuple contained in the given Collection
	 * @precondition CollectionUtils.isNonEmpty(collclct)
	 */
	protected static UsageCriteria getGreatestCommonUsageCriteriaFromCollectables(Collection<? extends CollectableGenericObject> collclct) {
		class GetUsageCriteria implements Transformer<CollectableGenericObject, UsageCriteria> {
			@Override
			public UsageCriteria transform(CollectableGenericObject clct) {
				return getUsageCriteria(clct);
			}
		}
		return UsageCriteria.getGreatestCommonUsageCriteria(CollectionUtils.transform(collclct, new GetUsageCriteria()));
	}

	private static boolean isUsageCriteriaField(String sFieldName) {
		boolean result = false;
		for (String sUsageCriteriaFieldName : getUsageCriteriaFieldNames())
			if (sUsageCriteriaFieldName.equals(sFieldName)) {
				result = true;
				break;
			}
		return result;
	}

	private CollectableComponentModelListener getCollectableComponentModelListenerForUsageCriteriaFields(boolean bSearchPanel) {
		return bSearchPanel ? ccmlistenerUsageCriteriaFieldsForSearch : ccmlistenerUsageCriteriaFieldsForDetails;
	}

	private static int getPanelIndex(boolean bSearchPanel) {
		return bSearchPanel ? 0 : 1;
	}

	/**
	 * @return the names of the attributes contained in a quintuple. Note that "module" isn't an attribute, so it's not
	 * part of the result.
	 */
	protected static Collection<String> getUsageCriteriaFieldNames() {
		return collUsageCriteriaFieldNames;
	}

	/**
	 * @param bSearchPanel
	 * @return Have the quintuple field listeners for the given panel been added already?
	 */
	private boolean getUsageCriteriaFieldListenersAdded(boolean bSearchPanel) {
		return abUsageCriteriaFieldListenersAdded[getPanelIndex(bSearchPanel)];
	}

	private void setUsageCriteriaFieldListenersAdded(boolean bSearchPanel, boolean bValue) {
		abUsageCriteriaFieldListenersAdded[getPanelIndex(bSearchPanel)] = bValue;
	}

	/**
	 * adds the quintuple field listeners to the given panel.
	 * @param bSearchPanel
	 * @precondition !this.getUsageCriteriaFieldListenersAdded(bSearchPanel)
	 * @postcondition this.getUsageCriteriaFieldListenersAdded(bSearchPanel)
	 */
	private void addUsageCriteriaFieldListeners(boolean bSearchPanel) {
		if (getUsageCriteriaFieldListenersAdded(bSearchPanel))
			throw new IllegalStateException();
		this.addUsageCriteriaFieldListeners(getEditView(bSearchPanel).getModel(),
			getCollectableComponentModelListenerForUsageCriteriaFields(bSearchPanel));

		setUsageCriteriaFieldListenersAdded(bSearchPanel, true);

		assert getUsageCriteriaFieldListenersAdded(bSearchPanel);
	}

	/**
	 * removes the quintuple field listeners to the given panel, if they have been added before.
	 * @param bSearchPanel
	 * @postcondition !this.getUsageCriteriaFieldListenersAdded(bSearchPanel)
	 */
	private void removeUsageCriteriaFieldListeners(boolean bSearchPanel) {
		if (getUsageCriteriaFieldListenersAdded(bSearchPanel)) {
			this.removeUsageCriteriaFieldListeners(getEditView(bSearchPanel).getModel(),
				getCollectableComponentModelListenerForUsageCriteriaFields(bSearchPanel));

			setUsageCriteriaFieldListenersAdded(bSearchPanel, false);
		}
		assert !getUsageCriteriaFieldListenersAdded(bSearchPanel);
	}

	private void addUsageCriteriaFieldListeners(CollectableComponentModelProvider clctcompmodelprovider,
		CollectableComponentModelListener clctcomplistener) {
		for (String sUsageCriteriaFieldName : getUsageCriteriaFieldNames()) {
			final CollectableComponentModel clctcompmodel = clctcompmodelprovider.getCollectableComponentModelFor(sUsageCriteriaFieldName);
			if (clctcompmodel != null) {
				log.debug("add listener for field " + clctcompmodel.getFieldName());
				clctcompmodel.addCollectableComponentModelListener(clctcomplistener);
			}
		}
	}

	private void removeUsageCriteriaFieldListeners(CollectableComponentModelProvider clctcompmodelprovider,
		CollectableComponentModelListener clctcomplistener) {
		for (String sUsageCriteriaFieldName : getUsageCriteriaFieldNames()) {
			final CollectableComponentModel clctcompmodel = clctcompmodelprovider.getCollectableComponentModelFor(sUsageCriteriaFieldName);
			if (clctcompmodel != null) {
				log.debug("remove listener for field " + clctcompmodel.getFieldName());
				clctcompmodel.removeCollectableComponentModelListener(clctcomplistener);
			}
		}
	}

	/**
	 *
	 * @param usagecriteria
	 */
	protected void setSearchStatesAccordingToUsageCriteria(UsageCriteria usagecriteria) {

		clctSearchState.setProperty("usagecriteria", usagecriteria);
	}

	/**
	 * @param cond
	 * @param bClearSearchFields
	 * @throws CommonBusinessException
	 */
	@Override
	protected void setSearchFieldsAccordingToSearchCondition(CollectableSearchCondition cond, boolean bClearSearchFields) throws CommonBusinessException {
		final boolean bUsageCriteriaFieldListenersWereAdded = getUsageCriteriaFieldListenersAdded(true);
		this.removeUsageCriteriaFieldListeners(true);

		final boolean bUsageCriteriaChanged;
		if (bReloadingLayout)
			bUsageCriteriaChanged = true;
		else {
			// 1. If we are not reloading the layout already, do that first:
			final UsageCriteria usagecriteriaOld = getUsageCriteriaFromView(true);
			final UsageCriteria usagecriteria = getUsageCriteriaFromSearchCondition(cond);
			bUsageCriteriaChanged = !usagecriteria.equals(usagecriteriaOld);

			if (bUsageCriteriaChanged) {
				// Note that the usagecriteria field listeners are removed here:
				this.reloadLayout(usagecriteria, getCollectState(), false, false);
				setSearchStatesAccordingToUsageCriteria(usagecriteria);
			}
		}

		assert !getUsageCriteriaFieldListenersAdded(true);

		try {
			// 2. fill in fields, ignoring changes in the quintuple fields as the right layout is loaded already:
			super.setSearchFieldsAccordingToSearchCondition(cond, bClearSearchFields && !bUsageCriteriaChanged);
		}
		finally {
			if (bUsageCriteriaFieldListenersWereAdded)
				this.addUsageCriteriaFieldListeners(true);
		}
	}

	@Override
	protected void _setSearchFieldsAccordingToSubCondition(CollectableSubCondition cond) throws CommonBusinessException {
		final String sEntityNameSub = cond.getSubEntityName();
		final SearchConditionSubFormController subformctl = mpsubformctlSearch.get(sEntityNameSub);
		if (subformctl == null)
			throw new NuclosFatalException(CommonLocaleDelegate.getMessage("GenericObjectCollectController.40","Ein Unterformular f\u00fcr die Entit\u00e4t {0} ist in der Suchbedingung, aber nicht im aktuellen Layout enthalten.", sEntityNameSub));
		subformctl.setCollectableSearchCondition(cond.getSubCondition());
	}

	@Override
	protected void _clearSearchFields() {
		super._clearSearchFields();

		for (SearchConditionSubFormController subformctl : getSubFormControllersInSearch())
			subformctl.clear();

		clctSearchState.getJComboBox().setSelectedIndex(-1);
	}

	/**
	 * @param cond
	 * @return the quintuple contained in the atomic fields of the given condition.
	 */
	private UsageCriteria getUsageCriteriaFromSearchCondition(CollectableSearchCondition cond) {
		return getUsageCriteriaFromFieldsMap(SearchConditionUtils.getAtomicFieldsMap(cond));
	}

	private UsageCriteria getUsageCriteriaFromFieldsMap(Map<String, CollectableField> mpFields) {
		return new UsageCriteria(getModuleId(),
			getValueIdFromUsageCriteriaField(mpFields)
		);
	}

	private Integer getValueIdFromUsageCriteriaField(Map<String, CollectableField> mpFields) {
		final CollectableField clctfField = mpFields.get(NuclosEOField.PROCESS.getMetaData().getField());
		return (clctfField == null) ? null : (Integer) clctfField.getValueId();
	}

	/**
	 * invokes dialog for export of result table list.
	 * If no lines are selected, just exports the result list.
	 * If one or more lines are selected, asks whether to export result list or to print appropriate reports for selection
	 * @precondition getCollectStateModel().getOuterState() == CollectState.OUTERSTATE_RESULT
	 */
	private void cmdPrint() {
		assert getCollectStateModel().getOuterState() == CollectState.OUTERSTATE_RESULT;

		final List<CollectableGenericObjectWithDependants> lstclctlo = getSelectedCollectables();
		final UsageCriteria usagecriteria = (lstclctlo.isEmpty() ? null : getGreatestCommonUsageCriteriaFromCollectables(lstclctlo));
		final String sDocumentEntityName = this.getEntityName() + "document";

		UIUtils.runCommand(getFrame(), new CommonRunnable() {
			@Override
			public void run() throws CommonBusinessException {
				final boolean bIncludeSubModules = getIncludeSubModulesForSearch();
				new ReportController(getFrame()).export(getCollectableEntity(), getInternalSearchExpression(), getSelectedFields(),
					lstclctlo, usagecriteria, bIncludeSubModules, sDocumentEntityName);
			}
		});
	}

	private void addTabbedPaneListener(LayoutRoot root) {
		List<JTabbedPane> lstTabs = new ArrayList<JTabbedPane>();
		searchTabbedPanes(root.getRootComponent(), lstTabs);
		for(JTabbedPane tabPane : lstTabs)
			tabPane.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent e) {
					JTabbedPane pane = (JTabbedPane)e.getSource();
					String sTitle = pane.getTitleAt(pane.getSelectedIndex());
					GenericObjectCollectController.this.getPreferences().put(GenericObjectCollectController.TABSELECTED, sTitle);
				}
			});
	}

	private void cmdShowResultsInExplorer() {
		UIUtils.runCommand(getFrame(), new CommonRunnable() {
			@Override
			public void run() throws CommonBusinessException {
				String sFilterName = getEntityLabel() + " " + Integer.toString(++iFilter);
				if (getSearchFilterComboBox().getSelectedIndex() != 0)
					sFilterName = sFilterName + ": " + getSearchFilterComboBox().getSelectedItem().toString();
				getExplorerController().showInOwnTab(new EntitySearchResultTreeNode(
					CommonLocaleDelegate.getMessage("GenericObjectCollectController.93","Suchergebnis ({0})", sFilterName), null, getInternalSearchExpression(), sFilterName, Main.getMainController().getUserName(), getEntityName()));
			}
		});
	}

	@Override
	public void makeConsistent(boolean bSearchTab) throws CollectableFieldFormatException {
		super.makeConsistent(bSearchTab);

		if (!stopEditing(bSearchTab))
			/** @todo we need to give a better error message here. */
			throw new CollectableFieldFormatException(CommonLocaleDelegate.getMessage("GenericObjectCollectController.95","Ung\u00fcltige Eingabe in Unterformular."));
	}

	/**
	 * transfers the values/states of the given old collectable component models to the given new collectable component models.
	 */
	private void transferCollectableComponentModelsInDetailsPanel(
		Map<String, DetailsComponentModel> mpclctcompmodelOld,
		Map<String, DetailsComponentModel> mpclctcompmodelNew) {

		transferredDetailsData.putAll(mpclctcompmodelOld);
		for (DetailsComponentModel clctcompmodelNew : mpclctcompmodelNew.values()) {
			final String sFieldName = clctcompmodelNew.getFieldName();

			DetailsComponentModel clctcompmodelOld = transferredDetailsData.get(sFieldName);
			if (clctcompmodelOld != null)
				// 1. Transfer all fields from the old models to the new models
				clctcompmodelNew.assign(clctcompmodelOld);
			else {
				// 2. Set all additional fields (those contained in the new models, but not in the old models) to their default values:
				final CollectableEntityField clctefNew = clctcompmodelNew.getEntityField();
				final CollectableField clctfDefault = clctefNew.getDefault();
				if (!clctfDefault.isNull()) {
					final boolean bFieldExistsInOldPanel = mpclctcompmodelOld.containsKey(clctcompmodelNew.getFieldName());
					if (bFieldExistsInOldPanel)
						log.debug("Skipping field " + clctefNew.getName() + " as it is contained in the old panel.");
					else {
						log.debug("Setting field " + clctefNew.getName() + " to default value " + clctfDefault + ".");
						clctcompmodelNew.setField(clctfDefault);
					}
				}
			}
		}
	}

	private void transferSubFormData(
		Map<String, DetailsSubFormController<CollectableEntityObject>> mpOldSubFormControllers,
		Map<String, DetailsSubFormController<CollectableEntityObject>> mpNewSubFormControllers) {
		for (String sEntityName : mpOldSubFormControllers.keySet())
			if (!mpNewSubFormControllers.containsKey(sEntityName))
				log.warn("Unterformular f\u00c3\u00bcr Entit\u00c3\u00a4t " + sEntityName + " ist in der neuen Maske nicht enthalten.");
			else
				DetailsSubFormController.copyModel(mpOldSubFormControllers.get(sEntityName), mpNewSubFormControllers.get(sEntityName));
	}

	private void reloadLayoutForDetailsTab(final boolean bAddUsageCriteriaFieldListeners) throws CommonBusinessException {
		log.debug("GenericObjectCollectController.reloadLayoutForDetailsTab");
		this.reloadLayout(getCollectStateModel().getCollectState(), bAddUsageCriteriaFieldListeners);
	}

	private void reloadLayoutForSearchTab() throws CommonBusinessException {
		log.debug("BEGIN reloadLayoutForSearchTab");
		this.reloadLayout(getCollectStateModel().getCollectState(), getUsageCriteriaFieldListenersAdded(true));
		setSearchStatesAccordingToUsageCriteria(getUsageCriteriaFromView(true));
		log.debug("FINISHED reloadLayoutForSearchTab");
	}

	private void reloadLayout(CollectState collectstate, boolean bAddUsageCriteriaFieldListeners)
	throws CommonBusinessException {
		final boolean bSearchPanel = collectstate.isSearchMode();
		final UsageCriteria usagecriteria = getUsageCriteriaFromView(bSearchPanel);

		this.reloadLayout(usagecriteria, collectstate, true, bAddUsageCriteriaFieldListeners);
	}

	private void reloadLayout(UsageCriteria usagecriteria, CollectState collectstate, boolean bTransferContents, boolean bAddUsageCriteriaFieldListeners)
	throws CommonBusinessException {
		/** @todo don't do this unless the layout really changed! */

		if (bReloadingLayout)
			throw new IllegalStateException("reloadLayout must not be called recursively!");

		final boolean bSearchPanel = collectstate.isSearchMode();
		try {
			bReloadingLayout = true;

			final LayoutRoot layoutroot = getLayoutFromCache(usagecriteria, collectstate);

			addTabbedPaneListener(layoutroot);

			this.removeUsageCriteriaFieldListeners(bSearchPanel);

			try {
				if (bSearchPanel)
					transferSearchPanel(layoutroot, bTransferContents);
				else {
					final JComponent compEditOld = getDetailsPanel().getEditComponent();
					if (bTransferContents)
						transferDetailsPanel(layoutroot, compEditOld);
					else
						transferDetailsPanel(layoutroot, bTransferContents ? compEditOld : null);
				}

				final Collection<SubForm> collsubform = new HashSet<SubForm>();
				for (SubFormController subformctl : getSubFormControllersInDetails())
					collsubform.add(subformctl.getSubForm());

				respectRights(getDetailsPanel().getEditView().getCollectableComponents(), collsubform, usagecriteria, collectstate);
				respectRights(getDetailsPanel().getEditView().getCollectableLabels(), collsubform, usagecriteria, collectstate);

				// ensure the (possibly new) edit panel is shown completely:
				UIUtils.ensureMinimumSize(getFrame());

				// always revalidate:
				getFrame().revalidate();
			}
			finally {
				if (bAddUsageCriteriaFieldListeners)
					this.addUsageCriteriaFieldListeners(bSearchPanel);
			}
		}
		finally {
			bReloadingLayout = false;
			if (!bSearchPanel) {
				if (getCollectStateModel().getDetailsMode() == CollectState.DETAILSMODE_NEW ||
					getCollectStateModel().getDetailsMode() == CollectState.DETAILSMODE_NEW_CHANGED ||
					getCollectStateModel().getDetailsMode() == CollectState.DETAILSMODE_NEW_SEARCHVALUE) {

					resetCollectableComponentModelsInDetailsMandatory();
					resetCollectableComponentModelsInDetailsMandatoryAdded();
					super.highlightMandatory();
					highlightMandatoryByState(getInitialStateId(usagecriteria));
				}
			}
		}
	}

	/**
	 * transfers the contents of the Details panel after reloading the layout.
	 * @param layoutroot
	 * @param compEditOld the old Edit panel for the Details tab (or null)
	 * @param compEditNew the new Edit panel for the Details tab, as in getEditPanel(..., false)
	 */
	@SuppressWarnings("deprecation")
	private void transferDetailsPanel(final LayoutRoot layoutroot, JComponent compEditOld) {
		// transfer field contents from the old components to the new ones:

		final boolean bChangeListenersWereAdded = changeListenersForDetailsAdded();

		if (bChangeListenersWereAdded)
			removeChangeListenersForDetails();

		try {
			final Map<String, DetailsComponentModel> mpclctcompNew = getMapOfDetailsComponentModels(layoutroot);

			// 0. set multi edit mode for CollectableComponentModels:
			final boolean bMultiEditMode = CollectState.isDetailsModeMultiViewOrEdit(getCollectStateModel().getDetailsMode());
			if (bMultiEditMode)
				for (DetailsComponentModel clctcompmodel : mpclctcompNew.values())
					clctcompmodel.setMultiEditable(true);

			// 1. transfer data in collectable components:
			if (compEditOld != null) {
				final Map<String, DetailsComponentModel> mpclctcompOld =
					CollectionUtils.typecheck(layoutrootDetails.getMapOfCollectableComponentModels(), DetailsComponentModel.class);
				transferCollectableComponentModelsInDetailsPanel(mpclctcompOld, mpclctcompNew);
			}

			final JComponent compEditNew = getDetailsPanel().newEditComponent(layoutroot.getRootComponent());
			getDetailsPanel().setEditView(DefaultEditView.newDetailsEditView(compEditNew, layoutroot, layoutroot.getInitialFocusEntityAndFieldName()));

			// layoutrootDetails is used for the ordered field names:
			layoutrootDetails = layoutroot;

			// Note that the old subform controllers must be closed before creating the new ones, so that the
			// column order and widths are preserved (first stored and then read):
			closeSubFormControllersInDetails();

			// 2. transfer data in subforms:
			Map<String, SubForm> mpSubForm = layoutroot.getMapOfSubForms();
			final Map<String, DetailsSubFormController<CollectableEntityObject>> mpNewSubFormControllers =
				newDetailsSubFormControllers(mpSubForm);

			if (compEditOld != null)
				transferSubFormData(getMapOfSubFormControllersInDetails(), mpNewSubFormControllers);
			setMapOfSubFormControllersInDetails(mpNewSubFormControllers);

			// 3. setup subform controllers
			setupSubFormController(mpSubForm, mpNewSubFormControllers);

			// 4. transfer custom data:
			if (compEditOld != null)
				transferCustomDataInDetailsPanel(layoutroot, compEditOld, compEditNew);
		}
		finally {
			if (bChangeListenersWereAdded)
				addChangeListenersForDetails();
		}
	}

	/**
	 * closes all subform controllers in the Details panel.
	 */
	protected void closeSubFormControllersInDetails() {
		closeSubFormControllers(getSubFormControllersInDetails());
	}

	/**
	 * closes all subform controllers in the Search panel.
	 */
	private void closeSubFormControllersInSearch() {
		closeSubFormControllers(getSubFormControllersInSearch());
	}

	/**
	 * @param collsubformctl
	 * @precondition collsubformctl != null
	 */
	private static void closeSubFormControllers(Collection<? extends SubFormController> collsubformctl) {
		for (SubFormController subformctl : collsubformctl)
			subformctl.close();
	}

	/**
	 * Successors may transfer custom data from the old edit panel to the new edit panel after reloading the layout
	 * of the Details panel.
	 * The default implementation does nothing (no custom data).
	 * @param layoutroot
	 * @param compEditOld the old Edit panel for the Details tab
	 * @param compEditNew the new Edit panel for the Details tab, as in getEditPanel(..., false)
	 * @deprecated seems to be unused.
	 */
	@Deprecated
	protected void transferCustomDataInDetailsPanel(LayoutRoot layoutroot, JComponent compEditOld, JComponent compEditNew) {
		// Default: no custom data
	}

	/**
	 * transfers the (contents of the) Search panel after reloading the layout.
	 * @param layoutroot
	 * @param bTransferContents transfer contents also?
	 * @throws CommonBusinessException
	 * @precondition this.isSearchPanelVisible()
	 */
	private void transferSearchPanel(final LayoutRoot layoutroot, boolean bTransferContents) throws CommonBusinessException {
		if (!isSearchPanelAvailable())
			throw new IllegalStateException("!this.isSearchPanelAvailable()");

		removeChangeListenersForSearch();

		try {
			final CollectableSearchCondition searchcond = bTransferContents ? getCollectableSearchConditionFromSearchPanel(true) : null;

			// Note that the old subform controllers must be closed before creating the new ones, so that the
			// column order and widths are preserved (first stored and then read):
			closeSubFormControllersInSearch();

			getSearchPanel().setEditView(newSearchEditView(layoutroot));

			mpsubformctlSearch = newSearchConditionSubFormControllers(layoutroot.getMapOfSubForms());

			if (bTransferContents)
				// try to transfer search condition:
				try {
					assert isSearchPanelAvailable();
					setCollectableSearchConditionInSearchPanel(searchcond);

					if (log.isEnabledFor(Level.WARN)) {
						final CollectableSearchCondition searchcondNew = getCollectableSearchConditionFromSearchPanel(false);
						if (!LangUtils.equals(searchcondNew, searchcond))
							log.warn("Die Suchbedingung wurde nicht korrekt \u00fcbertragen. Alte Bedingung: " + searchcond + " - Neue Bedingung: " + searchcondNew);
						/** @todo Is warning sufficient here? */
					}
				}
			catch (CommonBusinessException ex) {
				throw new NuclosBusinessException("Die Suchbedingung konnte nicht \u00fcbertragen werden.", ex);
			}
			getSearchPanel().getEditView().setComponentsEnabled(true);
		}
		finally {
			addChangeListenersForSearch();
			cmdDisplayCurrentSearchConditionInSearchPanelStatusBar();
		}
	}

	protected final LayoutRoot getLayoutFromCache(UsageCriteria usagecriteria, CollectState collectstate) {
		final boolean bSearchMode = collectstate.isSearchMode();
		/** @todo maybe factor this out in a protected method and override in GeneralSearchCollectController */
		final org.nuclos.common.collect.collectable.CollectableEntity clcte = bSearchMode ? getCollectableEntity() : CollectableGenericObjectEntity.getByModuleId(usagecriteria.getModuleId());
		final LayoutRoot result = GenericObjectLayoutCache.getInstance().getLayout(clcte, usagecriteria, bSearchMode, getLayoutMLButtonsActionListener(), valueListProviderCache);

		if (bSearchMode) {
			for (CollectableComponent comp : result.getCollectableComponents()) {
				comp.getControlComponent().addFocusListener(collectableComponentSearchFocusListener);
			}
		}

		customizeLayout(result, usagecriteria, collectstate);

		return result;
	}

	/**
	 * customizes the given layout, respecting the user rights.
	 * @param layoutroot
	 */
	protected void customizeLayout(LayoutRoot layoutroot, UsageCriteria usagecriteria, CollectState collectstate) {
		// respect rights in view mode is handled in the GenericObjectCollectStateListener.detailsModeEntered()
		if (collectstate.isSearchMode()) {
			respectRights(layoutroot.getCollectableComponents(), layoutroot.getMapOfSubForms().values(), usagecriteria, collectstate);
			//NUCLEUSINT-442
			respectRights(layoutroot.getCollectableLabels(), layoutroot.getMapOfSubForms().values(), usagecriteria, collectstate);

			fixUsageCriteriaSearchFields(layoutroot);
		}
		// iterate through the component tree and clear the keymaps of all JSplitPanes
		UIUtils.clearJComponentKeymap(layoutroot.getRootComponent(), JSplitPane.class);
	}

	/**
	 * workaround: ComboBoxes for quintuple fields must be pure dropdowns otherwise quintuple field listeners don't work.
	 * @todo This is a bug in CollectableComboBox - it fires two "changed" events instead of one
	 */
	private void fixUsageCriteriaSearchFields(CollectableComponentsProvider clctcompprovider) {
		for (String sUsageCriteriaFieldName : getUsageCriteriaFieldNames())
			for (CollectableComponent clctcomp : clctcompprovider.getCollectableComponentsFor(sUsageCriteriaFieldName))
				if (clctcomp instanceof CollectableComboBox)
					clctcomp.setInsertable(false);
	}

	/**
	 * adjusts the visibility and "enability" ;) of the fields according to the user rights.
	 * @param collclctcomp
	 * @param usagecriteria
	 * @param collectstate
	 */
	protected void respectRights(Collection<CollectableComponent> collclctcomp, Collection<SubForm> collsubform,
		UsageCriteria usagecriteria, CollectState collectstate) {

		synchronized(lockCurrRecReadable) {
			blnCurrentRecordReadable = null;
		}
		synchronized(lockCurrRecWritable) {
			blnCurrentRecordWritable = null;
		}

		final Collection<Integer> collStateIds;
		if (collectstate.isSearchMode() || collectstate.isDetailsModeNew())
			collStateIds = Collections.singletonList(getInitialStateId(usagecriteria));
		else if (collectstate.isDetailsModeViewOrEdit())
			collStateIds = Collections.singletonList(getSelectedGenericObjectStateId());
		else if (collectstate.isDetailsModeMultiViewOrEdit())
			collStateIds = getStateIds(getSelectedCollectables());
		else
			/** @todo this occurs when the user changes a usagecriteria field in the search panel and pushes the Search button
			 * "too quickly". Must be deferred to Elisa 1.1 as changing the reloading behaviour
			 * (runCommand instead of runCommandLater) is too dangerous now. 7.9.2004 */
			throw new IllegalStateException("collectstate: " + collectstate);

		for (CollectableComponent clctcomp : collclctcomp) {
			final Permission permission = getLeastCommonPermission(clctcomp, collStateIds);

			if (!permission.includesReading())
				clctcomp.setVisible(false);
			if (permission.includesWriting())
				clctcomp.setEnabled(clctcomp.isEnabledByInitial());
			else
				clctcomp.setEnabled(false);
		}	// for

		// adjust subforms:
		if (collsubform != null && !collsubform.isEmpty())
			for (SubForm subform : collsubform) {
				Permission permission = getLeastCommonSubformPermission(subform, collStateIds);

				if (!permission.includesReading())
					if(subform != null && subform.getParent() != null)
						subform.getParent().remove(subform);

				if (SecurityCache.getInstance().isWriteAllowedForModule(getEntity(), getSelectedGenericObjectId()) && permission.includesWriting()
					&& MetaDataClientProvider.getInstance().getEntity(subform.getEntityName()).isEditable())
					subform.setEnabled(MetaDataClientProvider.getInstance().getEntity(getEntity()).isEditable());
				else
					subform.setEnabled(collectstate.isSearchMode());
			}
	}

	@Override
	protected boolean isNewAllowed() {
		return super.isNewAllowed() && SecurityCache.getInstance().isNewAllowedForModule(getEntity());
	}

	/**
	 * @param clctcomp
	 * @param collStateIds
	 * @return the least common permission for the
	 * @postcondition result != null
	 */
	private Permission getLeastCommonPermission(CollectableComponent clctcomp, Collection<Integer> collStateIds) {
		if (CollectionUtils.isNullOrEmpty(collStateIds))
			throw new IllegalArgumentException("collStateIds");
		Permission result = Permission.READWRITE;
		for (Iterator<Integer> iter = collStateIds.iterator(); iter.hasNext() && (result != Permission.NONE);) {
			final Integer iStateId = iter.next();
			result = LangUtils.min(result, getPermission(clctcomp, iStateId));
		}
		return result;
	}

	/**
	 * @param clctcomp
	 * @param stateId
	 * @return the permission (read/write) for the given component in the state with the given id.
	 * @postcondition result != null
	 */
	private Permission getPermission(CollectableComponent component, Integer stateId) {
		// @todo This is not as clean. isCurrentRecordReadable() is called for search/new also.
		final Permission permissionRecord = Permission.getPermissionReadingOverrides(isCurrentRecordReadable(), isCurrentRecordWritable());

		logSecurity.debug("getPermission: Erlaubnis f?r den Datensatz: " + permissionRecord);

		final CollectableGenericObjectEntityField clctloef = (CollectableGenericObjectEntityField) component.getEntityField();
		final AttributeCVO attrcvo = clctloef.getAttributeCVO();

		final Permission result = LangUtils.min(permissionRecord, attrcvo.getPermission(stateId));

		logSecurity.debug("getPermission: Erlaubnis f?r das Attribut " + attrcvo.getName() + ": " + result);

		return result;
	}

	/**
	 * @param subform
	 * @param collStateIds
	 * @return the least common permission for the subform
	 * @postcondition result != null
	 */
	protected final Permission getLeastCommonSubformPermission(SubForm subform, Collection<Integer> collStateIds) {
		if(CollectionUtils.isNullOrEmpty(collStateIds))
			throw new IllegalArgumentException("collStateIds");

		Permission result = Permission.READWRITE;

		for(Iterator<Integer> iter = collStateIds.iterator(); iter.hasNext() && (result != Permission.NONE);) {
			final Integer iStateId = iter.next();
			result = LangUtils.min(result, getSubformPermission(subform, iStateId));
		}
		return result;
	}

	/**
	 * @param subform
	 * @param iStateId
	 * @return the permission (read/write) for the given subform in the state
	 * 			with the given id
	 * @postcondition result != null
	 */
	private Permission getSubformPermission(SubForm subform, Integer iStateId) {
		Map<Integer, Permission> mpPermissions = SecurityCache.getInstance().getSubFormPermission(subform.getEntityName());

		if(mpPermissions.containsKey(iStateId)) {
			Permission permission = Permission.READWRITE;
			if (isHistoricalView())
				permission = Permission.READONLY;
			return LangUtils.min(permission, mpPermissions.get(iStateId));
		}
		else
			return Permission.NONE;

	}

	private static Collection<Integer> getStateIds(Collection<? extends CollectableGenericObject> collclct) {
		return CollectionUtils.transform(collclct, new Transformer<CollectableGenericObject, Integer>() {
			@Override
			public Integer transform(CollectableGenericObject clct) {
				return GenericObjectDelegate.getInstance().getStateIdByGenericObject(clct.getId());
				//return getSystemAttributeId(clct, ParameterProvider.KEY_SYSTEMATTRIBUTE_STATUS);
			}
		});
	}

	/**
	 * @param usagecriteria
	 * @return
	 * @precondition usagecriteria.getModuleId() != null
	 */
	private Integer getInitialStateId(UsageCriteria usagecriteria) {
		return StateDelegate.getInstance().getStatemodel(usagecriteria).getInitialStateId();
	}

	/**
	 * @todo move to CollectController after renaming stopEditing() to stopEditingInDetails()
	 * @param bSearchTab
	 * @return Has the editing been stopped?
	 */
	protected boolean stopEditing(boolean bSearchTab) {
		return bSearchTab ? stopEditingInSearch() : stopEditingInDetails();
	}

	@Override
	protected boolean stopEditingInDetails() {
		boolean result = super.stopEditingInDetails();
		if (result)
			for (SubFormController subformctl : getSubFormControllersInDetails())
				result = result && subformctl.stopEditing();
		return result;
	}

	@Override
	protected boolean stopEditingInSearch() {
		boolean result = super.stopEditingInSearch();
		if (result)
			for (SubFormController subformctl : getSubFormControllersInSearch())
				result = result && subformctl.stopEditing();
		return result;
	}

	@Override
	protected String getTitle(int iTab, int iMode) {
		final String[] asTabs = {CommonLocaleDelegate.getMessage("GenericObjectCollectController.92","Suche"), CommonLocaleDelegate.getMessage("GenericObjectCollectController.42","Ergebnis"), CommonLocaleDelegate.getMessage("GenericObjectCollectController.35","Details")};
		final String[] asDetailsMode = {
			CommonLocaleDelegate.getMessage("GenericObjectCollectController.94","Undefiniert"), CommonLocaleDelegate.getMessage("GenericObjectCollectController.36","Details"), CommonLocaleDelegate.getMessage("GenericObjectCollectController.14","Bearbeiten"), CommonLocaleDelegate.getMessage("GenericObjectCollectController.55","Neueingabe"), CommonLocaleDelegate.getMessage("GenericObjectCollectController.57","Neueingabe (Ge\u00e4ndert)"), CommonLocaleDelegate.getMessage("GenericObjectCollectController.63","Sammelbearbeitung"),
			CommonLocaleDelegate.getMessage("GenericObjectCollectController.64","Sammelbearbeitung (Ge\u00e4ndert)"), CommonLocaleDelegate.getMessage("GenericObjectCollectController.56","Neueingabe (\u00dcbernahme Suchwerte)")
		};

		String sPrefix;
		String sSuffix = "";
		final String sMode;

		switch (iTab) {
		case CollectState.OUTERSTATE_DETAILS:
			sPrefix = ""; //getEntityLabel();
			sMode = asDetailsMode[iMode];
			if (CollectState.isDetailsModeViewOrEdit(iMode)) {
				sPrefix += getTreeViewIdentifier(getSelectedCollectable());
			}
			else if (CollectState.isDetailsModeMultiViewOrEdit(iMode))
				sSuffix = CommonLocaleDelegate.getMessage("GenericObjectCollectController.97"," von {0} Objekten", getSelectedCollectables().size());
			break;
		default:
			sPrefix = getEntityLabel();
			sMode = asTabs[iTab];
		}

		final StringBuilder sb = new StringBuilder( sPrefix + (sPrefix.length()>0?" - ":"") + sMode + sSuffix);

		if (isSelectedCollectableMarkedAsDeleted())
			sb.append(CommonLocaleDelegate.getMessage("GenericObjectCollectController.2"," (Objekt ist als gel\u00f6scht markiert)"));
		else if (isHistoricalView())
			sb.append(CommonLocaleDelegate.getMessage("GenericObjectCollectController.1"," (Historischer Zustand vom {0})", dateHistorical));

		return sb.toString();
	}

	@Override
	protected String getLabelForStartTab() {
		String result = null;

		boolean buildTreeView = false;
		switch (getCollectState().getOuterState()) {
			case CollectState.OUTERSTATE_DETAILS:
				buildTreeView = this.getCollectState().isDetailsModeViewOrEdit();
				break;
			case CollectState.OUTERSTATE_RESULT:
				buildTreeView = this.getSelectedCollectables().size() == 1;
				break;
		}

		if (buildTreeView) {
			result = getTreeViewIdentifier(getSelectedCollectable());
		}

		if (result == null) {
			return super.getLabelForStartTab();
		} else {
			return result.trim();
		}
	}

	private static String getTreeViewIdentifier(CollectableGenericObjectWithDependants clct) {
		final String sIdentifier;
		String sDescription = Modules.getInstance().getTreeView(Modules.getInstance().getModuleById(clct.getGenericObjectWithDependantsCVO().getModuleId()));
		if (sDescription != null) {
			sIdentifier = replaceTreeView(sDescription,clct.getGenericObjectWithDependantsCVO(), AttributeCache.getInstance());;
		}
		else
			sIdentifier = clct.getIdentifierLabel();
		return sIdentifier;
	}

	/**
	 * replace the user defined pattern with the attribute values for this object
	 * @param sTreeView
	 * @param gowdvo
	 * @param attrprovider
	 * @return
	 */
	private static String replaceTreeView(String sTreeView, GenericObjectWithDependantsVO gowdvo, AttributeCache attrprovider) {
		int sidx = 0;
		while ((sidx = sTreeView.indexOf("${", sidx)) >= 0) {
			int eidx = sTreeView.indexOf("}", sidx);
			String key = sTreeView.substring(sidx + 2, eidx);
			String flags = null;
			int ci = key.indexOf(':');
			if(ci >= 0) {
				flags = key.substring(ci + 1);
				key = key.substring(0, ci);
			}
			String rep = findReplacement(key, flags, gowdvo, attrprovider);
			sTreeView = sTreeView.substring(0, sidx) + rep + sTreeView.substring(eidx + 1);
			sidx = sidx + rep.length();
		}
		return sTreeView;
	}

	/**
	 * replace a single attribute pattern with the value for this object
	 * @param sKey
	 * @param sFlag
	 * @param gowdvo
	 * @param attrprovider
	 * @return attribute value or "" if attribute has no value
	 */
	private static String findReplacement(String sKey, String sFlag, GenericObjectWithDependantsVO gowdvo, AttributeCache attrprovider) {
		String sResIfNull = "";
		if(sFlag != null)
			for(StringTokenizer st = new StringTokenizer(sFlag, ":"); st.hasMoreElements(); ) {
				String flag = st.nextToken();
				if(flag.startsWith("ifnull="))
					sResIfNull = flag.substring(7);
			}
		DynamicAttributeVO attrVO = gowdvo.getAttribute(attrprovider.getAttribute(gowdvo.getModuleId(), sKey).getId());

		String oValue = null;
		if (attrVO != null && attrVO.getValue() != null) {
			if (attrVO.getValue() instanceof java.util.Date)
				oValue = DateFormat.getDateInstance(DateFormat.DEFAULT, CommonLocaleDelegate.getLocale()).format(attrVO.getValue());
			else
				oValue = attrVO.getValue().toString();
		}
		else
			oValue = sResIfNull;
		return oValue;
	}


	private boolean isSelectedCollectableMarkedAsDeleted() {
		boolean result = false;
		if (getCollectState().getInnerState() == CollectState.DETAILSMODE_VIEW)
			if (!multipleCollectablesSelected()) {
				final CollectableGenericObject clct = getSelectedCollectable();
				if (clct != null && clct.getGenericObjectCVO().isDeleted())
					result = true;
			}
		return result;
	}

	private static class RestorePreferences implements Serializable {
		private static final long serialVersionUID = 6637996725938917463L;

		String searchFilterName;
		String resultTemplateName;
	}

	private static String toXML(RestorePreferences rp) {
		XStream xstream = new XStream(new DomDriver());
		return xstream.toXML(rp);
	}

	private static RestorePreferences fromXML(String xml) {
		XStream xstream = new XStream(new DomDriver());
		return (RestorePreferences) xstream.fromXML(xml);
	}

	@Override
	protected void storeInstanceStateToPreferences(Map<String, String> inheritControllerPreferences) {
		RestorePreferences rp = new RestorePreferences();

		SearchFilter filter = getSelectedSearchFilter();
		rp.searchFilterName = (filter == null || filter.isDefaultFilter()) ? null : filter.getName();

		SearchResultTemplate template = searchResultTemplatesController.getSelectedSearchResultTemplate();
		rp.resultTemplateName = (template == null || template.isDefaultTemplate()) ? null : template.getName();

		inheritControllerPreferences.put(GenericObjectCollectController.class.getName(), toXML(rp));
		super.storeInstanceStateToPreferences(inheritControllerPreferences);
	}

	@Override
	protected void restoreInstanceStateFromPreferences(Map<String, String> inheritControllerPreferences) {
		RestorePreferences rp = fromXML(inheritControllerPreferences.get(GenericObjectCollectController.class.getName()));

		// Restore the settings for the chosen search result template in this module window
		if (rp.resultTemplateName == null)
			searchResultTemplatesController.selectDefaultTemplate();
		else
			// find search result template by name:
			searchResultTemplatesController.setSelectedSearchResultTemplate(rp.resultTemplateName);

		// Restore the settings for the chosen search filter in this module window (may override the global settings)
		if (rp.searchFilterName == null)
			selectDefaultFilter();
		else
			// find filter by name:
			for (int i = 1; i < getSearchFilterComboBox().getItemCount(); ++i)
				if (((SearchFilter) getSearchFilterComboBox().getItemAt(i)).getName().equals(rp.searchFilterName)) {
					getSearchFilterComboBox().setSelectedIndex(i);
					break;
				}

		super.restoreInstanceStateFromPreferences(inheritControllerPreferences);
	}

	@Override
	@Deprecated
	protected int restoreStateFromPreferences(Preferences prefs) throws CommonBusinessException {
		// Restore the settings for the chosen search result template in this module window
		restoreSelectedSearchResultTemplateFromPreferences(prefs);
		return super.restoreStateFromPreferences(prefs);
	}

	/**
	 * @param prefs
	 * @throws CommonBusinessException
	 * @precondition this.isSearchPanelVisible()
	 */
	@Override
	@Deprecated
	protected void restoreSearchCriteriaFromPreferences(Preferences prefs) throws CommonBusinessException {
		if (!isSearchPanelAvailable())
			throw new IllegalStateException("!isSearchPanelVisible()");

		// Restore the settings for the chosen search filter in this module window (may override the global settings)
		restoreSelectedSearchFilterFromPreferences(prefs);

		super.restoreSearchCriteriaFromPreferences(prefs);
	}

	@Deprecated
	private void restoreSelectedSearchFilterFromPreferences(Preferences prefs) {
		// restore search filter:
		final String sFilterName = prefs.get(PREFS_KEY_FILTERNAME, null);
		if (sFilterName == null)
			selectDefaultFilter();
		else
			// find filter by name:
			for (int i = 1; i < getSearchFilterComboBox().getItemCount(); ++i)
				if (((SearchFilter) getSearchFilterComboBox().getItemAt(i)).getName().equals(sFilterName)) {
					getSearchFilterComboBox().setSelectedIndex(i);
					break;
				}
	}

	@Deprecated
	private void restoreSelectedSearchResultTemplateFromPreferences(Preferences prefs) {
		// restore search result template:
		final String sTemplateName = prefs.get(PREFS_KEY_SEARCHRESULTTEMPLATENAME, null);
		if (sTemplateName == null)
			searchResultTemplatesController.selectDefaultTemplate();
		else
			// find search result template by name:
			searchResultTemplatesController.setSelectedSearchResultTemplate(sTemplateName);
	}

	/**
	 * inner class StateWrapper
	 * NUCLEUSINT-1159 needed for changing state with state change button
	 */
	public static class StateWrapper implements Comparable<StateWrapper> {
		private final Integer iId;
		private final Integer iNumeral;
		private final String sName;

		public StateWrapper(Integer iId, Integer iNumeral, String sName) {
			this.iId = iId;
			this.iNumeral = iNumeral;
			this.sName = sName;
		}

		public Integer getId() {
			return iId;
		}

		public Integer getNumeral() {
			return iNumeral;
		}

		public String getName() {
			return sName;
		}

		@Override
		public boolean equals(Object o) {
			if (o == this)
				return true;
			if (o == null)
				return false;
			final StateWrapper that = (StateWrapper) o;
			return LangUtils.equals(iId, that.iId) && LangUtils.equals(iNumeral, that.iNumeral) && LangUtils.equals(sName, that.sName);
		}

		@Override
		public int compareTo(StateWrapper that) {
			return iNumeral.compareTo(that.iNumeral);
		}

		@Override
		public int hashCode() {
			return LangUtils.hashCode(iId) ^ LangUtils.hashCode(iNumeral) ^ LangUtils.hashCode(sName);
		}

		@Override
		public String toString() {
			return getNumeralText() + " " + (sName == null ? "N/A" : sName);
		}

		public String getCombinedStatusText() {
			return getNumeralText() + " (" + (sName == null ? "N/A" : sName) + ")";
		}

		private String getNumeralText() {
			return iNumeral == null ? "N/A" : iNumeral.toString();
		}

	}	// class StateWrapper

	/**
	 * inner class ChangeStateForSelectedCollectablesController
	 */
	private static class ChangeStateForSelectedCollectablesController
	extends MultiCollectablesActionController<CollectableGenericObjectWithDependants, Object> {

		private static class ChangeStateAction extends UpdateAction<CollectableGenericObjectWithDependants> {
			private final GenericObjectCollectController ctl;
			private final StateWrapper stateNew;

			ChangeStateAction(GenericObjectCollectController ctl, StateWrapper stateNew) throws CommonBusinessException {
				super(ctl);
				this.ctl = ctl;
				this.stateNew = stateNew;
			}

			@Override
			public Object perform(CollectableGenericObjectWithDependants clctlo) throws CommonBusinessException {
				super.perform(clctlo);

				final GenericObjectVO govo = clctlo.getGenericObjectCVO();
				StateDelegate.getInstance().changeState(govo.getModuleId(), govo.getId(), stateNew.getId());
				return null;
			}

			@Override
			public String getText(CollectableGenericObjectWithDependants clctlo) {
				return CommonLocaleDelegate.getMessage("GenericObjectCollectController.86","Statuswechsel f\u00fcr Datensatz {0}...", MultiCollectablesActionController.getCollectableLabel(ctl.getEntityName(), clctlo));
			}

			@Override
			public String getSuccessfulMessage(CollectableGenericObjectWithDependants clctlo, Object oResult) {
				return CommonLocaleDelegate.getMessage("GenericObjectCollectController.87","Statuswechsel f\u00fcr Datensatz {0} war erfolgreich.", MultiCollectablesActionController.getCollectableLabel(ctl.getEntityName(), clctlo));
			}

			@Override
			public String getConfirmStopMessage() {
				return CommonLocaleDelegate.getMessage("GenericObjectCollectController.101","Wollen Sie den Statuswechsel f\u00fcr die ausgew\u00e4hlten Datens\u00e4tze an dieser Stelle beenden?\n(Die bisher ge\u00e4nderten Datens\u00e4tze bleiben in jedem Fall ge\u00e4ndert.)");
			}

			@Override
			public String getExceptionMessage(CollectableGenericObjectWithDependants clctlo, Exception ex) {
				return CommonLocaleDelegate.getMessage("GenericObjectCollectController.89","Statuswechsel ist fehlgeschlagen f\u00fcr Datensatz {0}. {1}", MultiCollectablesActionController.getCollectableLabel(ctl.getEntityName(), clctlo), ex.getMessage());
			}

			@Override
			public void executeFinalAction() throws CommonBusinessException {
				// In order to stay in the multi view mode (if possible), one solution would be the following algorithm.
				// But as that seems a bit oversized here (and potentially inefficient for large search results),
				// we just refresh the search and jump back to the result mode.
				// <algorithm>
				// Refresh the current search result. If the previously selected objects are still in the search result,
				// go back to multi view mode (or stay in multi view mode, virtually).
				// Otherwise display the new search result.
				// </algorithm>

				// store field widths before performing search:
				/** @todo this should be done by the search itself! */
				ctl.getResultController().writeSelectedFieldsAndWidthsToPreferences();
				// refresh search result in order to reflect changes made by state transitions:
				ctl.refreshResult();
				ctl.setCollectState(CollectState.OUTERSTATE_RESULT, CollectState.RESULTMODE_NOSELECTION);
			}
		}

		ChangeStateForSelectedCollectablesController(GenericObjectCollectController ctl, StateWrapper stateNew) throws CommonBusinessException {
			super(ctl, CommonLocaleDelegate.getMessage("GenericObjectCollectController.88","Statuswechsel in Status {0}", stateNew.getCombinedStatusText()), new ChangeStateAction(ctl, stateNew), ctl.getCompleteSelectedCollectables());
		}

	}	// class ChangeStateForSelectedCollectablesController

	/**
	 * inner class <code>CompleteGenericObjectsStrategy</code>
	 */
	private static class CompleteGenericObjectsStrategy implements CompleteCollectablesStrategy<CollectableGenericObjectWithDependants> {

		@Override
		public boolean isComplete(CollectableGenericObjectWithDependants clct) {
			/** @todo add "contains all required dependants */
			return clct.isComplete();
		}

		@Override
		public boolean getCollectablesInResultAreAlwaysComplete() {
			return false;
		}

		/**
		 * @return the identifier, status, status numeral and all quintuple fields.
		 */
		@Override
		public Set<String> getRequiredFieldNamesForResult() {
			final Set<String> result = new HashSet<String>();
			result.add(NuclosEOField.SYSTEMIDENTIFIER.getMetaData().getField());
			result.add(NuclosEOField.STATE.getMetaData().getField());
			result.add(NuclosEOField.STATENUMBER.getMetaData().getField());
			result.addAll(getUsageCriteriaFieldNames());
			return result;
		}

		/**
		 * reads a bunch of <code>CollectableGenericObjectWithDependants</code> from the database.
		 * @param collclctlo Collection<Collectable>
		 * @return Collection<Collectable> contains the read <code>CollectableGenericObjectWithDependants</code>.
		 * @throws CommonBusinessException
		 * @precondition collclctlo != null
		 * @postcondition result != null
		 * @postcondition result.size() == collclct.size()
		 */
		@Override
		public Collection<CollectableGenericObjectWithDependants> getCompleteCollectables(Collection<CollectableGenericObjectWithDependants> collclctlo) throws CommonBusinessException {
			if (collclctlo == null)
				throw new NullArgumentException("collclctlo");
			final Collection<CollectableGenericObjectWithDependants> result = new ArrayList<CollectableGenericObjectWithDependants>();

			final Collection<CollectableGenericObject> collclctIncomplete = new ArrayList<CollectableGenericObject>();
			CollectionUtils.split(collclctlo, new Collectable.IsComplete(), result, collclctIncomplete);

			if (!collclctIncomplete.isEmpty()) {
				final Collection<Object> collIds = CollectionUtils.transform(collclctIncomplete, new Collectable.GetId());
				final CollectableSearchCondition cond = SearchConditionUtils.getCollectableSearchConditionForIds(collIds);

				final Integer iCommonModuleId = getCommonModuleId(collclctlo);

				final Set<String> stRequiredSubEntityNames = (iCommonModuleId == null) ?
					Collections.<String>emptySet() :
						GenericObjectMetaDataCache.getInstance().getSubFormEntityNamesByModuleId(iCommonModuleId);

					final List<GenericObjectWithDependantsVO> lstlowdcvo = GenericObjectDelegate.getInstance().getCompleteGenericObjectsWithDependants(iCommonModuleId, cond, stRequiredSubEntityNames);
					result.addAll(CollectionUtils.transform(lstlowdcvo, new CollectableGenericObjectWithDependants.MakeCollectable()));
			}

			assert result != null;
			assert result.size() == collclctlo.size();
			return result;
		}

		/**
		 * @param collclctlo Collection<CollectableGenericObject>
		 * @return the common module id, if any, of the given leased objects.
		 */
		private static Integer getCommonModuleId(Collection<? extends CollectableGenericObject> collclctlo) {
			return Utils.getCommonObject(CollectionUtils.transform(collclctlo, new Transformer<CollectableGenericObject, Integer>() {
				@Override
				public Integer transform(CollectableGenericObject clctlo) {
					return clctlo.getGenericObjectCVO().getModuleId();
				}
			}));
		}

	}	// inner class CompleteGenericObjectsStrategy

	/**
	 * inner class GenericObjectCollectStateListener
	 */
	private class GenericObjectCollectStateListener extends CollectStateAdapter {
		@Override
		public void searchModeEntered(CollectStateEvent ev) throws CommonBusinessException {
			setInitialComponentFocusInSearchTab();
		}

		@Override
		public void resultModeEntered(CollectStateEvent ev) throws NuclosBusinessException {
			if (ev.getOldCollectState().getOuterState() != CollectState.OUTERSTATE_RESULT)
				setupChangeListenerForResultTableVerticalScrollBar();

			final int iResultMode = ev.getNewCollectState().getInnerState();
			final boolean bOneRowSelected = (iResultMode == CollectState.RESULTMODE_SINGLESELECTION);
			final boolean bMoreThanOneRowsSelected = (iResultMode == CollectState.RESULTMODE_MULTISELECTION);
			final boolean bRowsSelected = bOneRowSelected || bMoreThanOneRowsSelected;

			btnDeletePhysicallyInResult.setEnabled(bRowsSelected
				&& isDeleteSelectedCollectableAllowed()
				&& hasCurrentUserDeletionRights(getSelectedCollectable(), true));

			if (log.isDebugEnabled())
				if (CollectState.isResultModeSelected(ev.getNewCollectState().getInnerState())) {
					final UsageCriteria usagecriteria = getGreatestCommonUsageCriteriaFromCollectables(getSelectedCollectables());
					log.debug("Greatest common usagecriteria: " + usagecriteria);
				}
		}

		@Override
		public void resultModeLeft(CollectStateEvent ev) throws NuclosBusinessException {
			if (ev.getNewCollectState().getOuterState() != CollectState.OUTERSTATE_RESULT)
				removePreviousChangeListenersForResultTableVerticalScrollBar();
		}

		@Override
		public void detailsModeEntered(CollectStateEvent ev) throws CommonBusinessException {
			final int iDetailsMode = ev.getNewCollectState().getInnerState();

			final boolean bViewingExistingRecord = (iDetailsMode == CollectState.DETAILSMODE_VIEW);

			final GenericObjectCollectController ctl = GenericObjectCollectController.this;

			ctl.btnDeletePhysicallyInDetails.setEnabled(bViewingExistingRecord
				&& isPhysicallyDeleteAllowed(getSelectedCollectable()));
			ctl.btnMakeTreeRoot.setEnabled(bViewingExistingRecord);
			ctl.btnShowStateHistory.setEnabled(bViewingExistingRecord);
			ctl.btnShowLogBook.setEnabled(bViewingExistingRecord);
			ctl.clctdatechooserHistorical.setEnabled(bViewingExistingRecord);
			ctl.clctdatechooserHistorical.getDateChooser().getJTextField().setEditable(false);
			ctl.chkbxUseInvalidMasterData.setEnabled(bViewingExistingRecord);
			ctl.btnExecuteRule.setEnabled(bViewingExistingRecord);

			// current state, subsequent states and custom actions:
			switch (iDetailsMode) {
			case CollectState.DETAILSMODE_NEW:
				// deselect potentially previously selected entry
				getResultTable().clearSelection();
				break;
			case CollectState.DETAILSMODE_NEW_SEARCHVALUE:
				// deselect potentially previously selected entry
				getResultTable().clearSelection();
				break;
			case CollectState.DETAILSMODE_VIEW:
			case CollectState.DETAILSMODE_EDIT:
				final boolean bWritable = ctl.isCurrentRecordWritable();
				// show subsequent state buttons only in current (non-historical) view and only if the current record is writable:
				setSubsequentStatesVisible(bWritable, bViewingExistingRecord);
				break;

			case CollectState.DETAILSMODE_MULTIVIEW:
			case CollectState.DETAILSMODE_MULTIEDIT:
				// show the buttons for subsequent states only if all objects are in the same state:
				if (doTheSelectedGenericObjectsShareACommonState())
					setSubsequentStatesVisible(true, iDetailsMode == CollectState.DETAILSMODE_MULTIVIEW);
				else
					setSubsequentStatesVisible(false, false);
				break;

			default:
				// hide the buttons for subsequent states:
				setSubsequentStatesVisible(false, false);
			}	// switch

			setDeleteButtonToggleInDetails();
			// show custom actions only in view mode:
			showCustomActions(iDetailsMode);

			final Collection<SubForm> collsubform = new HashSet<SubForm>();
			for (SubFormController subformctl : getSubFormControllersInDetails())
				collsubform.add(subformctl.getSubForm());

			// dynamic reloading of layouts:
			if (CollectState.isDetailsModeChangesPending(iDetailsMode)) {
				// check if a change in a usagecriteria field caused the state change:
				final Object oSource = getSourceOfLastDetailsChange();
				if (oSource != null && oSource instanceof CollectableComponentModel) {
					final CollectableComponentModel clctcompmodelSource = (CollectableComponentModel) oSource;
					if (isUsageCriteriaField(clctcompmodelSource.getFieldName()))
						// don't add usagecriteria field listeners here. They are added later (see below).
						reloadLayoutForDetailsTab(false);
				}

				// add listeners for usagecriteria fields:
				/** @todo don't do this asynchronously - it's not safe!!! */
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						if (!getUsageCriteriaFieldListenersAdded(false))
							// add usagecriteria field listeners here:
							addUsageCriteriaFieldListeners(false);
					}
				});
			}
			// reset the source of last details change in order to prevent a memory leak:
			resetSourceOfLastDetailsChange();

			if (ev.hasOuterStateChanged())
				setInitialComponentFocusInDetailsTab();
		}

		@Override
		public void detailsModeLeft(CollectStateEvent ev) {
			final int iDetailsMode = ev.getOldCollectState().getInnerState();
			final boolean bDetailsChanged = (iDetailsMode == CollectState.DETAILSMODE_EDIT) || (iDetailsMode == CollectState.DETAILSMODE_NEW_CHANGED);

			if (bDetailsChanged) {
				// remove listeners for quintuple fields:
				log.debug("removeUsageCriteriaFieldListeners");
				removeUsageCriteriaFieldListeners(false);
			}
		}
	}	// inner class GenericObjectCollectStateListener

	private final ListSelectionListener deleteToggleResultListener = new ListSelectionListener() {
		@Override
		public void valueChanged(ListSelectionEvent e) {
			if(MetaDataClientProvider.getInstance().getEntity(getEntityName()).isEditable())
				setDeleteButtonToggleInResult();
		}
	};

	/**
	 * set delete button in result mode
	 * @param btn
	 */
	private void setDeleteButtonToggleInResult() {
		List<CollectableGenericObjectWithDependants> collist = getSelectedCollectables();
		int del = 0;
		int ndel = 0;
		Iterator<CollectableGenericObjectWithDependants> iter = collist.iterator();
		while (iter.hasNext()) {
			CollectableGenericObjectWithDependants col = iter.next();
			if (col.getGenericObjectCVO().isDeleted())
				del++;
			else if (!col.getGenericObjectCVO().isDeleted())
				ndel++;
		}

		final int fdel = del;
		final int fndel = ndel;
		UIUtils.invokeOnDispatchThread(new Runnable() {
			@Override
			public void run() {
				if (fdel == 0 && fndel != 0) {
					GenericObjectCollectController.this.getResultPanel().btnDelete.setEnabled(actDeleteSelectedCollectables.isEnabled());
					GenericObjectCollectController.this.getResultPanel().btnDelete.setSelected(false);
					GenericObjectCollectController.this.getResultPanel().btnDelete.setAction(actDeleteSelectedCollectables);
				} else if (fdel != 0 && fndel == 0) {
					GenericObjectCollectController.this.getResultPanel().btnDelete.setEnabled(actRestoreSelectedCollectables.isEnabled());
					GenericObjectCollectController.this.getResultPanel().btnDelete.setSelected(actRestoreSelectedCollectables.isEnabled());
					GenericObjectCollectController.this.getResultPanel().btnDelete.setAction(actRestoreSelectedCollectables);
				} else if ((fdel == 0 && fndel == 0) || (fdel != 0 && fndel != 0)) {
					GenericObjectCollectController.this.getResultPanel().btnDelete.setSelected(actDeleteSelectedCollectables.isEnabled());
					GenericObjectCollectController.this.getResultPanel().btnDelete.setEnabled(false);
					GenericObjectCollectController.this.getResultPanel().btnDelete.setAction(actDeleteSelectedCollectables);
				}
			}
		});
	}

	/**
	 * set toggle delete button in details mode
	 */
	private void setDeleteButtonToggleInDetails() {
		CollectableGenericObjectWithDependants gowd = getSelectedCollectable();
		if (gowd == null) {
			getDetailsPanel().btnDelete.setAction(actDeleteCurrentCollectableInDetails);
			getDetailsPanel().btnDelete.setSelected(false);
		}
		else if (gowd.getGenericObjectCVO().isDeleted()) {
			getDetailsPanel().btnDelete.setAction(actRestoreCurrentCollectableInDetails);
			getDetailsPanel().btnDelete.setSelected(true);
		}
		else {
			getDetailsPanel().btnDelete.setAction(actDeleteCurrentCollectableInDetails);
			getDetailsPanel().btnDelete.setSelected(false);
		}
	}

	@Override
	protected void setDeleteActionEnabled(boolean enabled) {
		actDeleteCurrentCollectableInDetails.setEnabled(enabled);
		actRestoreCurrentCollectableInDetails.setEnabled(enabled);
	}

	/**
	 * @deprecated Factor out, move to GenericObjectResultController or remove completely.
	 */
	public final class GetCollectableEntityFieldForResult implements Transformer<String, CollectableEntityField> {
		private final org.nuclos.common.collect.collectable.CollectableEntity clcte;

		/**
		 * @deprecated Factor out, move to GenericObjectResultController or remove completely.
		 */
		public GetCollectableEntityFieldForResult(org.nuclos.common.collect.collectable.CollectableEntity clcte) {
			this.clcte = clcte;
		}

		@Override
		public CollectableEntityField transform(String sFieldName) {
			return getResultController().getCollectableEntityFieldForResult(clcte, sFieldName);
		}
	}

	protected static class GenericObjectDetailsPanel extends DetailsPanel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * @param compRoot the edit component according to the LayoutML
		 * @return the edit component to be used in the Details panel. Default is <code>compRoot</code> itself.
		 *         Successors may build their own component/panel out of compRoot.
		 * @todo pull down to SearchOrDetailsPanel and/or change signature into EditView newEditView()
		 */
		public JComponent newEditComponent(JComponent compRoot) {
			return compRoot;
		}

		@Override
		protected AbstractButton getDeleteButton() {
			return new JToggleButton();
		}

	}	// inner class GenericObjectDetailsPanel

	protected static class GenericObjectResultPanel extends NuclosResultPanel<CollectableGenericObjectWithDependants> {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		@Override
		protected AbstractButton getDeleteButton() {
			return new JToggleButton();
		}
		@Override
        protected void setupCopyAction() {
	        // no default copy action! GenericObject has it's own
        }
	}

	protected class GenericObjectCollectPanel extends CollectPanel<CollectableGenericObjectWithDependants> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		protected GenericObjectCollectPanel(boolean bSearch) {
			super(bSearch);
		}

		@Override
		public SearchPanel newSearchPanel() {
			Collection<CollectableComponent> additionalSearchComponents = new ArrayList<CollectableComponent>();
			additionalSearchComponents.add(clctSearchState);
			return new GenericObjectSearchPanel(additionalSearchComponents);
		}

		@Override
		public ResultPanel<CollectableGenericObjectWithDependants> newResultPanel() {
			return new GenericObjectResultPanel();
		}

		@Override
		public DetailsPanel newDetailsPanel() {
			return new GenericObjectDetailsPanel();
		}
	}

	/**
	 * @return <code>MultiActionProgressPanel</code> for the MultiObjectsActionController.
	 */
	@Override
	protected MultiActionProgressPanel getMultiActionProgressPanel(int iCount) {
		MultiActionProgressPanel multiActionProgressPanel = new MultiActionProgressPanel(iCount);
		multiActionProgressPanel.setResultHandler(new MultiActionProgressResultHandler(this) {
			@Override
			public void handleMultiSelection(Collection<Integer> collGenericObjectIds) {
				((GenericObjectCollectController) controller).openGenericObjectController(collGenericObjectIds);
			}
		});
		return multiActionProgressPanel;
	}

	/**
	 * open the GenericObjectCollectController for an entity. if collGenericObjectIds only contains one generic object id the controller is opened
	 * in details mode if there are more than one id a search expression is created and the search result is shown
	 *
	 * @param collGenericObjectIds
	 */
	private void openGenericObjectController(final Collection<Integer> collGenericObjectIds) {
		UIUtils.runCommand(parent, new Runnable() {
			@Override
			public void run() {
				try {
					GenericObjectClientUtils.showDetails(parent, collGenericObjectIds);
				}
				catch (CommonBusinessException ex) {
					Errors.getInstance().showExceptionDialog(parent, ex);
				}
			}
		});
	}

	/*
	 * sets search result format accordng to selected SearchResultTemplate
	 * @param templateSelected
	 */
	protected void setSearchResultFormatAccordingToTemplate(SearchResultTemplate templateSelected){
		final List<CollectableEntityField> lstSelectedNew = getFieldsFromFieldNames(getCollectableEntity(), templateSelected.getVisibleColumns());
		final List<CollectableEntityField> fixedColumns = getFieldsFromFieldNames(getCollectableEntity(), templateSelected.getListColumnsFixed());
		Map<String, Integer> listColumnsWidths = templateSelected.getListColumnsWidths();
		Map<CollectableEntityField, Integer> clefListColumnsWidths = new HashMap<CollectableEntityField, Integer>();
		for(CollectableEntityField clFiled : lstSelectedNew)
			if(listColumnsWidths.containsKey(clFiled.getName()))
				clefListColumnsWidths.put(clFiled, listColumnsWidths.get(clFiled.getName()));
		getResultPanel().initializeFields(getFields(), this, lstSelectedNew, fixedColumns, clefListColumnsWidths);
	}

	/*
	 * creates a SearchResultTemplate accordng to selected columns in search result
	 * @throws CommonBusinessException
	 */
	@SuppressWarnings("deprecation")
	protected SearchResultTemplate getCurrentSearchResultFormatFromResultPanel() throws CommonBusinessException {
		final SearchResultTemplate result = new SearchResultTemplate();
		result.setModuleId(getModuleId());
		// set selected columns:
		//List<String> sFieldsNames = CollectableUtils.getFieldNamesFromCollectableEntityFields(this.getSelectedFields());
		//result.setVisibleColumns(sFieldsNames);
		final List<String> lstQualifiedEntityFieldNames = CollectionUtils.transform(getSelectedFields(),
			new CollectableEntityFieldWithEntity.GetQualifiedEntityFieldName());
		result.setVisibleColumns(lstQualifiedEntityFieldNames);
		// TODO set sorting column names
		List<String> lstSortingColumnNames = Collections.emptyList();
		result.setSortingColumnNames(lstSortingColumnNames);
		Map<String, Integer> currentFieldWiths = getResultPanel().getCurrentFieldWithsMap();
		result.setListColumnsWidths(currentFieldWiths);
		//result.setListColumnsFixed(CollectableUtils.getFieldNamesFromCollectableEntityFields(((NuclosResultPanel) this.getResultPanel()).getFixedColumns()));
		final List<String> fixedColumnsNames = CollectableUtils.getFieldNamesFromCollectableEntityFields(getResultPanel().getFixedColumns());
		final List<CollectableEntityFieldWithEntity> filteredFixedColumns = CollectionUtils.select(
			getSelectedFields(),
			PredicateUtils.transformedInputPredicate(new CollectableEntityFieldWithEntity.GetName(),
				PredicateUtils.valuesCollection(fixedColumnsNames)
			)
		);

		final List<String> lstQualifiedEntityFieldNamesFixed = CollectionUtils.transform(filteredFixedColumns,
			new CollectableEntityFieldWithEntity.GetQualifiedEntityFieldName());
		result.setListColumnsFixed(lstQualifiedEntityFieldNamesFixed);
		return result;
	}

	/**
	 * @param currclct
	 * @return the current collectable filled with the values which are set in the search panel,
	 *         but first reload layout if process field is available and filled in search panel
	 * @throws CommonBusinessException
	 */
	@Override
	protected CollectableGenericObjectWithDependants newCollectableWithSearchValues(CollectableGenericObjectWithDependants currclct) throws CommonBusinessException {
		final Collection<SearchComponentModel> collscm = getSearchCollectableComponentModels();

		// iterate over each component in search panel to set process field in details panel
		for (SearchComponentModel scm : collscm)
			if (scm.getFieldName().equals(NuclosEOField.PROCESS.getMetaData().getField())) {
				// set 'process' field in details panel if any found
				for (CollectableComponent clctcomp : getDetailCollectableComponentsFor(scm.getFieldName())) {
					if (scm.getField().getValue() != null) {
						currclct.setField(scm.getFieldName(), scm.getField());
						for (CollectableField clctField : MasterDataDelegate.getInstance().getProcessByUsage(getModuleId(), false))
							if (((String)clctField.getValue()).equals(scm.getField().getValue())) {
								// reload layout according to the 'process' field
								reloadLayout(new UsageCriteria(getModuleId(), (Integer)clctField.getValueId()), getCollectState(), true, true);
								detailsChanged(clctcomp);
								break;
							}
					}
					break;
				}
				break;
			}
		return super.newCollectableWithSearchValues(currclct);
	}

	/**
	 * complete the current collectable with the subform values which are set in the search panel
	 */
	@Override
	protected void newCollectableWithDependantSearchValues() throws NuclosBusinessException {
		Collection<SearchConditionSubFormController> collscsfc = getSubFormControllersInSearch();
		Collection<DetailsSubFormController<CollectableEntityObject>> colldsfc = getSubFormControllersInDetails();

		// iterate over each search subform
		for (SearchConditionSubFormController scsfc : collscsfc)
			// handel only subforms of the first hierarchie
			if (scsfc.getSubForm().getParentSubForm() == null)
				// iterate over each detail subform
				for (DetailsSubFormController<CollectableEntityObject> dsfc : colldsfc)
					if(dsfc.getEntityAndForeignKeyFieldName().getEntityName().equals(scsfc.getEntityAndForeignKeyFieldName().getEntityName()))
						if (dsfc.getSubForm().isEnabled()) {
							SubForm.SubFormTableModel searchTableModel = scsfc.getSearchConditionTableModel();
							CollectableTableModel<CollectableEntityObject> detailsTableModel = dsfc.getCollectableTableModel();
							// iterate over each row found in the search subform
							for (int iSearchRow = 0; iSearchRow < searchTableModel.getRowCount(); iSearchRow++) {
								CollectableMasterData clctmd = dsfc.insertNewRow();
								// iterate over each column found in the search subform
								for (int iSearchColumn = 0; iSearchColumn < searchTableModel.getColumnCount(); iSearchColumn++)
									// iterate over each coresponding column found in the detail subform
									for (int columnDetail = 0; columnDetail < detailsTableModel.getColumnCount(); columnDetail++)
										if (searchTableModel.getColumnName(iSearchColumn).equals(detailsTableModel.getColumnName(columnDetail))) {
											TableCellEditor tce = dsfc.getSubForm().getJTable().getCellEditor(iSearchRow, columnDetail);

											if(tce instanceof CollectableComponentTableCellEditor) {
												boolean bSetAllowed = true;

												if (!isSetAllowedForClctComponent(((CollectableComponentTableCellEditor) tce).getCollectableComponent()))
													bSetAllowed = false;

												if (bSetAllowed) {
													Object oClctSearchCondition = searchTableModel.getValueAt(iSearchRow, iSearchColumn);
													if (oClctSearchCondition != null) {
														String sFieldName = ((AtomicCollectableSearchCondition)oClctSearchCondition).getFieldName();
														Object oSearchValue = null;
														Object oSearchValueId = null;
														if (oClctSearchCondition instanceof CollectableComparison) {
															CollectableField clctField = ((CollectableComparison)oClctSearchCondition).getComparand();
															if (clctField instanceof CollectableValueIdField)
																oSearchValueId = ((CollectableValueIdField)clctField).getValueId();

															oSearchValue = clctField.getValue();
														}
														else if (oClctSearchCondition instanceof CollectableLikeCondition)
															oSearchValue = ((CollectableLikeCondition)oClctSearchCondition).getLikeComparand();

														if (oSearchValue != null) {
															clctmd.getMasterDataCVO().setField(sFieldName, oSearchValue);

															if (oSearchValueId != null) {
																clctmd.setField(sFieldName, new CollectableValueIdField(oSearchValueId, oSearchValue));
																clctmd.getMasterDataCVO().setField(sFieldName+"Id", oSearchValueId);
															}

															detailsChanged(dsfc.getSubForm());
														}
													}
												}
											}
										}
							}
						}
	}

	/**
	 * @return whether the current collectable is generated
	 */
	public boolean isCollectableGenerated() {
		return bGenerated;
	}

	/**
	 * @return the source object's id if current collectable is generated
	 */
	public Integer getSourceObjectId() {
		return iGenericObjectIdSource;
	}

	@Override
	public ImageIcon getIcon() {
		String resourceName = GenericObjectDelegate.getInstance().getResourceMap().get(getModuleId());
		String nuclosResource = MetaDataClientProvider.getInstance().getEntity(Modules.getInstance().getEntityNameByModuleId(getModuleId())).getNuclosResource();
		if(resourceName != null)
			return ResourceCache.getIconResource(resourceName);
		else if (nuclosResource != null){
			ImageIcon nuclosIcon = NuclosResourceCache.getNuclosResourceIcon(nuclosResource);
			if (nuclosIcon != null) return MainFrame.resizeAndCacheTabIcon(nuclosIcon);
		}
		return null;
	}


	@Override
	protected Collection<CollectableEntityField> getAdditionalSearchFields() {
		Collection<CollectableEntityField> additionalFields = new HashSet<CollectableEntityField>();
		if (super.getAdditionalSearchFields() != null)
			additionalFields.addAll(super.getAdditionalSearchFields());
		return additionalFields;

	}


	private class GenericObjectEditView extends DefaultEditView {

		protected GenericObjectEditView(JComponent compRoot, CollectableComponentsProvider clctcompprovider, boolean bForSearch, EntityAndFieldName initialFocusField) {
			super(compRoot, clctcompprovider, newGenericObjectEditModel(clctcompprovider, bForSearch), initialFocusField);
		}

	} // inner class GenericObjectEditView

	@Override
	public GenericObjectEditView newSearchEditView(LayoutRoot layoutroot) {
		final JComponent compEdit = getSearchPanel().newEditComponent(layoutroot.getRootComponent());
		return new GenericObjectEditView(compEdit, new GenericObjectCollectableComponentsProvider(layoutroot), true, layoutroot.getInitialFocusEntityAndFieldName());
	}

	private EditModel newGenericObjectEditModel(CollectableComponentsProvider clctcompprovider, boolean bForSearch) {
		final Collection<CollectableComponent> clctcomp = clctcompprovider.getCollectableComponents();
		return bForSearch ? new GenericObjectSearchEditModel(clctcomp) : new DefaultDetailsEditModel(clctcomp);
	}


	public class GenericObjectSearchEditModel extends DefaultSearchEditModel {

		public GenericObjectSearchEditModel(Collection<CollectableComponent> collclctcomp) {
			super(collclctcomp);
		}

		@Override
		public SearchComponentModel getCollectableComponentModelFor(String sFieldName) {
			if (NuclosEOField.STATE.getMetaData().getField().equals(sFieldName) || NuclosEOField.STATENUMBER.getMetaData().getField().equals(sFieldName))
				return clctSearchState.getSearchModel();
			return super.getCollectableComponentModelFor(sFieldName);
		}

		@Override
		public Collection<SearchComponentModel> getCollectableComponentModels() {
			Collection<SearchComponentModel> result = new HashSet<SearchComponentModel>();
			result.addAll(super.getCollectableComponentModels());
			result.add(clctSearchState.getSearchModel());
			return result;
		}

		@Override
		public Collection<String> getFieldNames() {
			Collection<String> result = new HashSet<String>();
			result.addAll(super.getFieldNames());
			result.add(NuclosEOField.STATE.getMetaData().getField());
			result.add(NuclosEOField.STATENUMBER.getMetaData().getField());
			return result;
		}

	} //inner class GenericObjectSearchEditModel

	private class GenericObjectCollectableComponentsProvider implements CollectableComponentsProvider {

		private CollectableComponentsProvider mainProvider;

		public GenericObjectCollectableComponentsProvider(CollectableComponentsProvider provider) {
			mainProvider = provider;
		}

		@Override
		public Collection<CollectableComponent> getCollectableComponents() {
			Collection<CollectableComponent> mainComponents = mainProvider.getCollectableComponents();
			mainComponents.add(clctSearchState);
			return mainComponents;
		}

		@Override
		public Collection<CollectableComponent> getCollectableComponentsFor(
			String sFieldName) {
			if (NuclosEOField.STATE.getMetaData().getField().equals(sFieldName) || NuclosEOField.STATENUMBER.getMetaData().getField().equals(sFieldName)) {
				Collection<CollectableComponent> result = new ArrayList<CollectableComponent>();
				result.addAll(mainProvider.getCollectableComponentsFor(sFieldName));
				result.add(clctSearchState);
				return result;
			}
			return mainProvider.getCollectableComponentsFor(sFieldName);
		}

		@Override
		public Collection<CollectableComponent> getCollectableLabels() {
			return mainProvider.getCollectableLabels();
		}

	} // inner class GenericObjectCollectableComponentsProvider

	/**
	 *
	 * @param subFormEntity
	 * @param t
	 * @return count imported | count not imported
	 * @throws NuclosBusinessException
	 */
	@SuppressWarnings("rawtypes")
    public int[] dropOnSubForm(String subFormEntity, Transferable t) throws NuclosBusinessException{

		boolean subFormFound = false;
		int[] result = new int[]{0,0};

		try {
			for (DetailsSubFormController subFormCtrl : getSubFormControllersInDetails()) {
				if (subFormCtrl instanceof MasterDataSubFormController
					&& subFormCtrl.getEntityAndForeignKeyFieldName().getEntityName().equals(subFormEntity)) {
					subFormFound = true;

					if (!subFormCtrl.getSubForm().isEnabled()) {
						throw new NuclosBusinessException(CommonLocaleDelegate.getMessage("GenericObjectCollectController.102", "Unterformular ist nicht aktiv."));
					}

					String entityLabel = null;
					boolean noReferenceFound = false;

					final List<?> lstloim = (List<?>) t.getTransferData(TransferableGenericObjects.dataFlavor);
	                for (Object o : lstloim) {
	                	if (o instanceof GenericObjectIdModuleProcess) {
	                		GenericObjectIdModuleProcess goimp = (GenericObjectIdModuleProcess) o;
	                		Integer entityId = goimp.getModuleId();
	                		String entity = MetaDataClientProvider.getInstance().getEntity(LangUtils.convertId(entityId)).getEntity();
	                		entityLabel = CommonLocaleDelegate.getLabelFromMetaDataVO(MetaDataClientProvider.getInstance().getEntity(LangUtils.convertId(entityId)));

	                        try {
	                        	if (!((MasterDataSubFormController)subFormCtrl).insertNewWithReference(
	                        		entity, new CollectableGenericObjectWithDependants(
	                        			GenericObjectDelegate.getInstance().getWithDependants(goimp.getGenericObjectId())),
	                        		true)) {
	                        		result[1] = result[1]+1;
		                		} else {
		                			result[0] = result[0]+1;
		                		}
	                        }
	                        catch(NuclosBusinessException e2) {
	                        	noReferenceFound = true;
	                        }
	                        catch(CommonBusinessException e) {
	                            log.error(e.getMessage(), e);
	                        }
	                	}
	                }

	                if (noReferenceFound) {
	                	throw new NuclosBusinessException(CommonLocaleDelegate.getMessage("GenericObjectCollectController.104", "Dieses Unterformular enth�lt keine Referenzspalte zur Entit�t {0}.", entityLabel));
	                }
				}
			}
		} catch(UnsupportedFlavorException e) {
	        log.error(e.getMessage(), e);
	    } catch(IOException e) {
	        log.error(e.getMessage(), e);
	    }

		if (!subFormFound) {
			throw new NuclosBusinessException(CommonLocaleDelegate.getMessage("GenericObjectCollectController.103", "Unterformular ist nicht im Layout vorhanden."));
		}

		return result;
	}

    public void runViewSingleCollectable(CollectableGenericObjectWithDependants clct, boolean bShow, CommonRunnable pAfterLoadingRunnable) {
		this.subFormsLoader.setAfterLoadingRunnable(pAfterLoadingRunnable);
	    super.runViewSingleCollectable(clct, bShow);
    }

    protected void highlightMandatoryByState(Integer stateId) {
    	if (stateId != null) {
			for (StateVO statevo : StateDelegate.getInstance().getStatemodelClosure(getModuleId()).getAllStates()) {
				if (stateId.equals(statevo.getId())) {
					final Set<String> mandatoryfields = CollectionUtils.transformIntoSet(statevo.getMandatoryFields(), new Transformer<MandatoryFieldVO, String>() {
						@Override
						public String transform(MandatoryFieldVO i) {
							EntityFieldMetaDataVO efMeta = MetaDataClientProvider.getInstance().getEntityField(getEntity(), i.getFieldId().longValue());
							return efMeta != null ? efMeta.getField() : null;
						}});
					setCollectableComponentModelsInDetailsMandatory(mandatoryfields);
				}
			}
		}
    }

	@Override
	protected void highlightMandatory() {
		super.highlightMandatory();
		//getUsageCriteria(getSelectedCollectable());

		if (getCollectStateModel().getDetailsMode() == CollectState.DETAILSMODE_VIEW ||
			getCollectStateModel().getDetailsMode() == CollectState.DETAILSMODE_EDIT) {
			highlightMandatoryByState(getSelectedGenericObjectStateId());

		} else if (getCollectStateModel().getDetailsMode() == CollectState.DETAILSMODE_MULTIEDIT ||
				   getCollectStateModel().getDetailsMode() == CollectState.DETAILSMODE_MULTIVIEW) {
			final Collection<Integer> collStateIds = getStateIds(getSelectedCollectables());
			if (collStateIds.size() == 1) {
				highlightMandatoryByState(collStateIds.iterator().next());
			}
		}
	}

	@Override
	public Map<String, DetailsSubFormController<CollectableEntityObject>> getDetailsSubforms() {
		return this.mpsubformctlDetails;
	}


}	// class GenericObjectCollectController