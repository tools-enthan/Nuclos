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
package org.nuclos.client.explorer;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;
import org.nuclos.client.main.Main;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.tree.ChainedTreeNodeAction;
import org.nuclos.client.ui.tree.TreeNodeAction;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.server.navigation.treenode.TreeNode;

/**
 * A node in the explorer tree.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class ExplorerNode<TN extends TreeNode> extends DefaultMutableTreeNode {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger log = Logger.getLogger(ExplorerNode.class);

	public static final String ACTIONCOMMAND_SHOW_DETAILS = "SHOW DETAILS";
	public static final String ACTIONCOMMAND_COPY = "COPY";
	public static final String ACTIONCOMMAND_PASTE = "PASTE";
	public static final String ACTIONCOMMAND_REMOVE = "REMOVE";
	public static final String ACTIONCOMMAND_SHOW_IN_OWN_TAB = "SHOW_IN_OWN_TAB";
	public static final String ACTIONCOMMAND_REFRESH = "REFRESH";
	public static final String ACTIONCOMMAND_EXPAND = "EXPAND";
	public static final String ACTIONCOMMAND_COLLAPSE = "COLLAPSE";

	/**
	 * Have the children of this node already been loaded?
	 */
	private boolean bChildrenHaveBeenLoaded;

	/**
	 * Use <code>ExplorerNodeFactory.newExplorerNode()</code> to create <code>ExplorerNode</code>s
	 * @param treenode
	 * @postcondition !this.getChildrenHaveBeenLoaded()
	 */
	protected ExplorerNode(TreeNode treenode) {
		super(treenode, true);
	}

	/**
	 * @return the <code>TreeNode</code> object contained (as user object) in this node.
	 */
	@SuppressWarnings("unchecked")
	public TN getTreeNode() {
		return (TN) this.getUserObject();
	}

	/**
	 * @param treenode
	 * @precondition treenode != null
	 */
	private void setTreeNode(TN treenode) {
		this.setUserObject(treenode);
	}

	/**
	 * @return the <code>ExplorerController</code> that controls this node.
	 * @postcondition result != null
	 */
	protected ExplorerController getExplorerController() {
		return Main.getMainController().getExplorerController();
	}

	@Override
	public boolean isLeaf() {
		final Boolean bHasSubNodes = this.getTreeNode().hasSubNodes();
		return (bHasSubNodes != null) && bHasSubNodes.booleanValue();
	}

	/**
	 * Note that this method is (and should be!) private.
	 * @return Have the children of this node already been loaded?
	 */
	private boolean getChildrenHaveBeenLoaded() {
		return this.bChildrenHaveBeenLoaded || (this.getChildCount() > 0);
	}

	/**
	 * loads the children of this node, if they haven't been loaded yet.
	 * @param bLoadAllAvailableSubNodes Load all available subnodes (recursively)?
	 * @postcondition this.getChildrenHaveBeenLoaded()
	 */
	@SuppressWarnings("unchecked")
	public void loadChildren(boolean bLoadAllAvailableSubNodes) {
		if (!this.getChildrenHaveBeenLoaded()) {
			log.debug("START loadChildren");
			for (TreeNode treenodeChild : this.getTreeNode().getSubNodes()) {
				final ExplorerNode<TreeNode> explorernodeChild = (ExplorerNode<TreeNode>) ExplorerNodeFactory.getInstance().newExplorerNode(treenodeChild, false);
				this.add(explorernodeChild);
				if (bLoadAllAvailableSubNodes && (treenodeChild.hasSubNodes() != null)) {
					explorernodeChild.loadChildren(bLoadAllAvailableSubNodes);
				}
			}
			this.bChildrenHaveBeenLoaded = true;
			log.debug("FINISHED loadChildren");
		}
		assert this.getChildrenHaveBeenLoaded();
	}

	/**
	 * unloads the children of this node, if they have been loaded before.
	 * @postcondition !this.getChildrenHaveBeenLoaded()
	 */
	public void unloadChildren() {
		if (this.getChildrenHaveBeenLoaded()) {
			this.removeAllChildren();
			this.bChildrenHaveBeenLoaded = false;
		}
		assert !this.getChildrenHaveBeenLoaded();
	}

	/**
	 * refreshes the current node (and its children) and notifies the given treemodel
	 * @param dtm the DefaultTreeModel to notify. Must contain this node.
	 * @throws CommonFinderException if the object presented by this node no longer exists.
	 */
	@SuppressWarnings({ "unchecked", "deprecation" })
	public void refresh(final JTree tree) throws CommonFinderException {
		refresh(tree, false);
	}

	/**
	 * refreshes the current node (and its children) and notifies the given treemodel
	 * @param dtm the DefaultTreeModel to notify. Must contain this node.
	 * @throws CommonFinderException if the object presented by this node no longer exists.
	 */
	@SuppressWarnings({ "unchecked", "deprecation" })
	public void refresh(final JTree tree, boolean fullRefreshCurrent) throws CommonFinderException {
		List<String> lstExpandedPathsResult = new ArrayList<String>();		
		ExplorerNode.createExpandendPathsForTree(new TreePath(tree.getModel().getRoot()), tree, lstExpandedPathsResult);

		final TreePath selected = tree.getSelectionPath();		
		DefaultTreeModel dtm = (DefaultTreeModel) tree.getModel();
		unloadChildren();

		final TN treenode = getTreeNode();
		if (treenode.implementsNewRefreshMethod()) {
			TN refreshed = null;
			if(fullRefreshCurrent && !this.isRoot()){
				TreeNode parentTreeNode = ((ExplorerNode)this.getParent()).getTreeNode();
				parentTreeNode.removeSubNodes();
				List<? extends TreeNode> parentSubNodes = parentTreeNode.getSubNodes();
				for(TreeNode parentSubNode : parentSubNodes){
					if(ObjectUtils.equals(parentSubNode.getId(), treenode.getId()))
						refreshed = (TN) parentSubNode;
				}
				if(refreshed == null){
					this.removeFromParent();
					dtm.nodeStructureChanged(this);
					return;
				}
			} else {
				refreshed = (TN) treenode.refreshed();
			}
			setTreeNode(refreshed);
		}
		else {
			treenode.refresh();
		}
		treenode.removeSubNodes();
		loadChildren(true);

		assert getChildrenHaveBeenLoaded();

		dtm.nodeStructureChanged(this);
		
		ExplorerNode.expandTreeAsync(lstExpandedPathsResult, tree);
		
		if(selected != null) {
			List<Object> pathEssence = CollectionUtils.asList(selected.getPath());
			Collections.reverse(pathEssence);
			if(pathEssence.size() > 1) {
				pathEssence = pathEssence.subList(0, pathEssence.size() - 1);
				new Thread(new TreeExpander(tree, pathEssence)).start();
			}
		}
	}
	
	private class TreeExpander implements TreeExpansionListener, Runnable {
		private JTree        tree;
		private List<Object> toExpand;
		private TreePath     expanded;
	    private boolean      listener;

	    private TreeExpander(JTree tree, List<Object> toExpand) {
	    	this.tree = tree;
	    	this.toExpand = toExpand;
	    	this.expanded = new TreePath(tree.getModel().getRoot());
	    }
		
	    @Override
        public void run() {
	    	if(toExpand.isEmpty())
	    		return;

	    	if(tree.isExpanded(expanded)) {
	    		SwingUtilities.invokeLater(new Runnable() {
	    			@Override
	    			public void run() {
	    				treeExpanded(new TreeExpansionEvent(tree, expanded));
	    			}});
	    	}
	    	else {
	    		tree.addTreeExpansionListener(TreeExpander.this);
	    		listener = true;
	    		tree.expandPath(expanded);
	    	}
	    }

		@Override
		public void treeExpanded(TreeExpansionEvent event) {
			TreePath eventPath = event.getPath();
			if(eventPath.equals(expanded)) {
				if(listener)
					tree.removeTreeExpansionListener(TreeExpander.this);

				ExplorerNode<?> last   = (ExplorerNode<?>) expanded.getLastPathComponent();
				ExplorerNode<?> expand = (ExplorerNode<?>) toExpand.get(toExpand.size() - 1);
				ExplorerNode<?> child  = null;
				
				for(int i=0; i<last.getChildCount(); i++) {
					ExplorerNode<?> childNode = (ExplorerNode<?>) last.getChildAt(i);
					if(childNode.getTreeNode().equals(expand.getTreeNode())) {
						child = childNode;
						break;
					}
				}
				
				if(child != null) {
					expanded = expanded.pathByAddingChild(child);
					toExpand = toExpand.subList(0, toExpand.size() - 1);
					if(toExpand.isEmpty()) {
						tree.setSelectionPath(expanded);
						tree.scrollPathToVisible(expanded);
						//tree.expandPath(expanded);
					}
					else
						new Thread(TreeExpander.this).start();
				}
			}
		}

		@Override
        public void treeCollapsed(TreeExpansionEvent event) {}
	}
	
	public void handleKeyEvent(JTree tree, KeyEvent ev) {}

	/**
	 * @param tree the tree where the action is about to take place.
	 * @return the list of possible <code>TreeNodeAction</code>s for this node.
	 * These may be shown in the node's context menu.
	 * Separators are shown in the menus for <code>null</code> entries.
	 */
	public List<TreeNodeAction> getTreeNodeActions(JTree tree) {
		final List<TreeNodeAction> result = new LinkedList<TreeNodeAction>();

		result.add(new RefreshAction(tree));
		final ShowInOwnTabAction actShowInOwnTab = new ShowInOwnTabAction(tree);
		actShowInOwnTab.setEnabled(!this.getTreeNode().needsParent());
		result.add(actShowInOwnTab);

		result.addAll(getExpandCollapseActions(tree));

		final TreeNodeAction exploreractCopy = new ChainedTreeNodeAction(ACTIONCOMMAND_COPY, CommonLocaleDelegate.getMessage("ExplorerNode.4","Kopieren"),
				TransferHandler.getCopyAction(), tree);
		result.add(exploreractCopy);

		final TreeNodeAction exploreractPaste = new ChainedTreeNodeAction(ACTIONCOMMAND_PASTE, CommonLocaleDelegate.getMessage("ExplorerNode.2","Einf\u00fcgen"),
				TransferHandler.getPasteAction(), tree);
		result.add(exploreractPaste);

		// enable "copy" action according to the tree's TransferHandler:
		final boolean bCopyEnabled = (tree.getTransferHandler().getSourceActions(tree) & TransferHandler.COPY) != 0;
		exploreractCopy.setEnabled(bCopyEnabled);

		// enable "paste" action according to the tree's TransferHandler:
		// This is hard because TransferHandler.canImport is not called every time the selection changes.
		// Workaround: call canImport anyway to reduce bad paste actions:
		final DataFlavor[] aflavors = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(this).getTransferDataFlavors();
		final boolean bPasteEnabled = tree.getTransferHandler().canImport(tree, aflavors);
		exploreractPaste.setEnabled(bPasteEnabled);

		return result;
	}
	
	/**
	 * 
	 * @param jTree 
	 * @return additional tool bar components
	 */
	public List<JComponent> getToolBarComponents(JTree jTree) {
		return null;
	}

	/**
	 * get actions for expand and collapse the children of this node.
	 * if this node is a leaf return an empty list
	 * @param tree
	 * @return
	 */
	protected List<TreeNodeAction> getExpandCollapseActions(JTree tree) {
		final List<TreeNodeAction> result;
//		if(this.getChildCount() > 0) {
		result = new ArrayList<TreeNodeAction>();
		result.add(TreeNodeAction.newSeparatorAction());
		result.add(new ExpandAction(tree));
		result.add(new CollapseAction(tree));
		result.add(TreeNodeAction.newSeparatorAction());
//		} else {
//			result = Collections.emptyList();
//		}
		return result;
	}

	/**
	 * There is no default action by default. Subclasses may specify the default action here.
	 * @param tree 
	 * @return the action command of the default <code>TreeNodeAction</code> for this node, if any.
	 */
	public String getDefaultTreeNodeActionCommand(JTree tree) {
		return null;
	}

	/**
	 * finds the default action for this node by searching the tree node actions
	 * (as specified in <code>getTreeNodeActions()</code>) for an enabled tree node action with the default tree node action
	 * command (as specified in <code>getDefaultTreeNodeActionCommand()</code>). Note that this method
	 * doesn't have to be overridden in subclasses.
	 * @param tree the tree where the action is about to take place.
	 * @return the default action for this node, if any.
	 * @postcondition result != null --> result.isEnabled()
	 */
	public TreeNodeAction getDefaultTreeNodeAction(JTree tree) {
		TreeNodeAction result = null;

		final String sDefaultTreeActionCommmand = this.getDefaultTreeNodeActionCommand(tree);
		if (sDefaultTreeActionCommmand != null) {
			for (TreeNodeAction action : this.getTreeNodeActions(tree)) {
				if (action.isEnabled() && LangUtils.equals(sDefaultTreeActionCommmand, action.getValue(Action.ACTION_COMMAND_KEY)))
				{
					result = action;
					break;
				}
			}
		}
		assert result == null || result.isEnabled();
		return result;
	}

	/**
	 * @return the label to display for this node
	 */
	public String getLabel() {
		return StringUtils.emptyIfNull(this.getTreeNode().getLabel());
	}

	/**
	 * @return the label to display if this node is a root (if it is displayed in a new tab)
	 */
	public String getLabelForRoot() {
		return this.getLabel();
	}

	/**
	 * @return the text to display for this node
	 */
	@Override
	public String toString() {
		return this.isRoot() ? this.getLabelForRoot() : this.getLabel();
	}

	/**
	 * @return the icon to display for the node, or <code>null</code> for the default icon,
	 * specified by the look&feel.
	 */
	public Icon getIcon() {
		// use default icon by default ;-)
		return null;
	}

	/**
	 * source actions for drag&drop.
	 * @see DnDConstants
	 * @return (default: DnDConstants.ACTION_NONE)
	 */
	public int getDataTransferSourceActions() {
		return DnDConstants.ACTION_NONE;
	}

	/**
	 * @return a <code>Transferable</code> for data transfer (default: null)
	 */
	public Transferable createTransferable(JTree tree) {
		return null;
	}

	/**
	 * imports data from a drop or paste operation. The default implementation throws an
	 * UnsupportedFlavorException to indicate that drag&drop is not supported by default.
	 * @see javax.swing.TransferHandler#importData
	 * @param transferable
	 * @param tree
	 * @return Was the data imported? <code>false</code> may be returned if the drop action requires
	 * additional user input, and the user cancels the operation.
	 * @throws UnsupportedFlavorException
	 */
	public boolean importTransferData(Component parent, Transferable transferable, JTree tree) throws IOException,
			UnsupportedFlavorException {
		throw new UnsupportedFlavorException(null);
	}

	/**
	 * expand all children of this node
	 * @param tree
	 */
	public void expandAllChildren(final JTree tree) {
		for (int i = getChildCount() - 1; i >= 0; i--) {
			final TreePath treePath = new TreePath((((DefaultMutableTreeNode) ExplorerNode.this.getChildAt(i))).getPath());
			if (!tree.isExpanded(treePath)) {
				tree.expandPath(treePath);
			}
		}
	}

	/**
	 * collapse all children of this node
	 * @param tree
	 */
	public void collapseAllChildren(JTree tree) {
		for (int i = getChildCount() - 1; i >= 0; i--) {
			tree.collapsePath(new TreePath(((DefaultMutableTreeNode) this.getChildAt(i)).getPath()));
		}
	}

	/**
	 * Action: Show the current node in its own tab.
	 */
	protected class ShowInOwnTabAction extends TreeNodeAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public ShowInOwnTabAction(JTree tree) {
			super(ACTIONCOMMAND_SHOW_IN_OWN_TAB, CommonLocaleDelegate.getMessage("ExplorerNode.3","In eigenem Reiter anzeigen"), tree);
		}

		@Override
        public void actionPerformed(ActionEvent ev) {
			cmdShowInOwnTabAction();
		}
	}	// inner class ShowInOwnTabAction
	
	protected void cmdShowInOwnTabAction() {
		getExplorerController().cmdShowInOwnTab(ExplorerNode.this.getTreeNode());
	}

	/**
	 * Action: Refresh the descendants of the current node
	 */
	protected class RefreshAction extends TreeNodeAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public RefreshAction(JTree tree) {
			super(ACTIONCOMMAND_REFRESH, CommonLocaleDelegate.getMessage("ExplorerNode.1","Aktualisieren"), tree);
		}

		@Override
        public void actionPerformed(ActionEvent ev) {
			UIUtils.runCommand(getExplorerController().getParent(), new CommonRunnable() {
				@Override
                public void run() throws CommonFinderException {
					ExplorerNode.this.refresh(RefreshAction.this.getJTree(), true);

					// To be sure to correlate the label of the tab also when changed...
					if (ExplorerNode.this.isRoot()) {
						ExplorerNode.this.getExplorerController().refreshNode(ExplorerNode.this.getTreeNode());
					}
				}
			});
		}
	}	// inner class RefreshAction

	/**
	 * Action: Expand all Subnodes
	 */
	protected class ExpandAction extends TreeNodeAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public ExpandAction(JTree tree) {
			super(ACTIONCOMMAND_EXPAND, CommonLocaleDelegate.getMessage("ExplorerNode.5","Unterelemente aufklappen"), tree);
		}

		@Override
        public void actionPerformed(ActionEvent ev) {
			UIUtils.runCommand(getExplorerController().getParent(), new Runnable() {
				@Override
                public void run() {
					final TreePath treePath = new TreePath(ExplorerNode.this);
					if (!ExpandAction.this.getJTree().isExpanded(treePath)) {
						ExpandAction.this.getJTree().expandPath(treePath);
					}

					ExplorerNode.this.expandAllChildren(ExpandAction.this.getJTree());
				}
			});
		}
	}	// inner class ExpandAction

	/**
	 * Action: Collapse all Subnodes
	 */
	protected class CollapseAction extends TreeNodeAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public CollapseAction(JTree tree) {
			super(ACTIONCOMMAND_COLLAPSE, CommonLocaleDelegate.getMessage("ExplorerNode.6","Unterelemente zuklappen"), tree);
		}

		@Override
        public void actionPerformed(ActionEvent ev) {
			UIUtils.runCommand(getExplorerController().getParent(), new Runnable() {
				@Override
                public void run() {
					ExplorerNode.this.collapseAllChildren(CollapseAction.this.getJTree());
				}
			});
		}
	}	// inner class CollapseAction

	public String getIdentifierPath() {
		StringBuilder sb = new StringBuilder();
		for (javax.swing.tree.TreeNode tn : getPath()) {
			sb.append('/');
			sb.append(((ExplorerNode<?>) tn).getTreeNode().getIdentifier());
		}
		return sb.toString().substring(1);
	}
	
	public static TreePath findDescendant(DefaultTreeModel model, String idPath) {
		String[] idComponent = idPath.split("/");
		ExplorerNode<?> root = (ExplorerNode<?>) model.getRoot();
		if (idComponent.length == 0 || !idComponent[0].equals(root.getTreeNode().getIdentifier()))
			return null;
		
		ExplorerNode<?>[] path = new ExplorerNode<?>[idComponent.length];
		path[0] = root;
		for (int i = 1; i < path.length; i++) {
			ExplorerNode<?> child = path[i-1].findChildNodeWithIdentifier(idComponent[i]);
			if (child == null)
				return null;
			path[i] = child;
		}
		
		return new TreePath(path);
	}
	
	public ExplorerNode<?> findChildNodeWithIdentifier(String id) {
		this.loadChildren(false);
		int childCount = this.getChildCount();
		for (int i = 0; i < childCount; i++) {
			ExplorerNode<?> childNode = (ExplorerNode<?>) this.getChildAt(i);
			if (id.equals(childNode.getTreeNode().getIdentifier()))
				return childNode;
		}
		return null;
	}

	
	// --- expanded paths ---> collect paths (e.g. for storing in preferences) ; expand stored paths asynchronous
	
	
	static void createExpandendPathsForTree(TreePath path, JTree tree, List<String> lstExpandedPathsResult) {
		final ExplorerNode<?> explorernode = (ExplorerNode<?>) path.getLastPathComponent();
		boolean isExpanded = tree.isExpanded(path);
		if (isExpanded) {
			lstExpandedPathsResult.add(explorernode.getIdentifierPath());
			
			for (int i = 0; i < explorernode.getChildCount(); i++) {
				final ExplorerNode<?> explorernodeChild = (ExplorerNode<?>) explorernode.getChildAt(i);
				createExpandendPathsForTree(path.pathByAddingChild(explorernodeChild), tree, lstExpandedPathsResult);
			}			
		}		
	}
	
	static void expandTreeAsync(List<String> lstExpandedPaths, final JTree tree) {
		for (final String idPath : lstExpandedPaths) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
					TreePath path = ExplorerNode.findDescendant(model, idPath);
					if (path != null)
						tree.expandPath(path);
				}
			});
		}
	}
	
}	// class ExplorerNode