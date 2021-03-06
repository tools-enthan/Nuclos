package org.nuclos.client.explorer.node;

import java.awt.Component;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;

import org.nuclos.client.eventsupport.EventSupportManagementController;
import org.nuclos.client.explorer.ExplorerNode;
import org.nuclos.client.explorer.node.eventsupport.EventSupportTargetType;
import org.nuclos.client.explorer.node.eventsupport.EventSupportTreeNode;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.tree.TreeNodeAction;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.server.navigation.treenode.TreeNode;

public class EventSupportTargetExplorerNode extends ExplorerNode<EventSupportTreeNode> {
	
	public EventSupportTargetExplorerNode(TreeNode node)
	{
		super(node);
	}
	
	public boolean importTransferData(Component parent, Transferable transferable, JTree tree) throws IOException, UnsupportedFlavorException {
		return true;
	}
		
	@Override
	public boolean isLeaf() {
		boolean retVal = false;
		if (EventSupportTargetType.EVENTSUPPORT_TYPE.equals(((EventSupportTreeNode)getTreeNode()).getTreeNodeType()) || 
			EventSupportTargetType.STATE_TRANSITION.equals(((EventSupportTreeNode)getTreeNode()).getTreeNodeType()) ||
				((EventSupportTreeNode)getTreeNode()).getSubNodes().isEmpty())
				retVal = true;
			
		return retVal;
	}
	
	@Override
	public int getDataTransferSourceActions() {

		return DnDConstants.ACTION_COPY_OR_MOVE;
	}
	
	@Override
	public Icon getIcon() {

		Icon result = null;
		
		EventSupportTargetType treeNodeType = ((EventSupportTreeNode) getUserObject()).getTreeNodeType();
		
		if (treeNodeType == null)
			 return null;
		
		switch (treeNodeType) 
		{
		case EVENTSUPPORT:
			result = Icons.getInstance().getIconRuleUsage16();
			break;
		case ENTITY:
		case ALL_ENTITIES_ENTITY:
			result = Icons.getInstance().getIconModule();
			break;
		case STATEMODEL:
		case ALL_STATEMODELS_STATEMODEL:
			result = Icons.getInstance().getIconStateModel();
			break;
		case JOB:
		case ALL_JOBS_JOB:
			result = Icons.getInstance().getIconJobRunning();
			break;
		case GENERATION:
		case ALL_GENERATIONS_GENERATION:
			result = Icons.getInstance().getIconGenericObject16();
			break;
		default:
			break;
		}

		return result;
	}
	
	@Override
	public boolean getAllowsChildren() {
		return isLeaf() ? false : super.getAllowsChildren();
	}
	
	
	public List<TreeNodeAction> getTreeNodeActions(JTree tree) {
		
		final List<TreeNodeAction> result = new LinkedList<TreeNodeAction>();

		result.add(new RefreshAction(tree));
		final ShowInOwnTabAction actShowInOwnTab = new ShowInOwnTabAction(tree);
		actShowInOwnTab.setEnabled(!this.getTreeNode().needsParent());
		result.add(actShowInOwnTab);

		result.addAll(getExpandCollapseActions(tree));
		
		return result;
	}

	@Override
	public Action getTreeNodeActionOnMouseClick(JTree tree) {
		return new EventSupportTargetShowPropertyAction(tree);
	}
	
	private class EventSupportTargetShowPropertyAction extends AbstractAction
	{
		JTree tree;
		
		public EventSupportTargetShowPropertyAction(JTree tree) {
			this.tree = tree;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			
			// selected tree element			
			
			final EventSupportTargetExplorerNode node= (EventSupportTargetExplorerNode) tree.getSelectionPath().getLastPathComponent();
			
			// show infos and properties for this node
			EventSupportManagementController controller = node.getTreeNode().getController();
			controller.showTargetSupportProperties(node.getTreeNode());
		}
	}
	
	/**
	 * refreshes the current node (and its children) and notifies the given treemodel
	 * @param dtm the DefaultTreeModel to notify. Must contain this node.
	 * @throws CommonFinderException if the object presented by this node no longer exists.
	 */
	public void refresh(final JTree tree, boolean fullRefreshCurrent) throws CommonFinderException {
	
		DefaultTreeModel dtm = (DefaultTreeModel) tree.getModel();
		unloadChildren();

		this.getTreeNode().refresh();
		loadChildren(true);
		dtm.nodeStructureChanged(this);		
		tree.setSelectionRow(0);
	}
	
	

}
