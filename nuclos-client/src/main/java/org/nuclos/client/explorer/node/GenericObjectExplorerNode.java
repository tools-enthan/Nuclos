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
package org.nuclos.client.explorer.node;

import static org.nuclos.common2.CommonLocaleDelegate.getMessage;

import java.awt.Component;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JTree;

import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.client.explorer.ExplorerNode;
import org.nuclos.client.genericobject.GenericObjectDelegate;
import org.nuclos.client.genericobject.Modules;
import org.nuclos.client.genericobject.RelateGenericObjectsController;
import org.nuclos.client.genericobject.datatransfer.GenericObjectIdModuleProcess;
import org.nuclos.client.genericobject.datatransfer.GenericObjectIdModuleProcess.HasModuleId;
import org.nuclos.client.genericobject.datatransfer.TransferableGenericObjects;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.resource.NuclosResourceCache;
import org.nuclos.client.resource.ResourceCache;
import org.nuclos.client.ui.CommonClientWorkerAdapter;
import org.nuclos.client.ui.CommonMultiThreader;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.tree.TreeNodeAction;
import org.nuclos.common.MutableBoolean;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.navigation.treenode.GenericObjectTreeNode;
import org.nuclos.server.navigation.treenode.GenericObjectTreeNode.RelationDirection;
import org.nuclos.server.navigation.treenode.GenericObjectTreeNode.SystemRelationType;
import org.nuclos.server.navigation.treenode.TreeNode;

/**
 * <code>ExplorerNode</code> for generic objects.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class GenericObjectExplorerNode extends ExplorerNode<GenericObjectTreeNode> implements EntityExplorerNode {

	/**
	 * action: add to group (currently disabled)
	 */
//	private static final String ACTIONCOMMAND_ADD_TO_GROUP = "ADD TO GROUP";

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * action: remove from parent group
	 */
	private static final String ACTIONCOMMAND_REMOVE_FROM_PARENT_GROUP = "REMOVE FROM PARENT GROUP";

	/**
	 * action: remove from parent group
	 */
	private static final String ACTIONCOMMAND_REMOVE_RELATION = "REMOVE RELATION";

	public GenericObjectExplorerNode(TreeNode treenode) {
		super(treenode);
	}

	@Override
	public List<TreeNodeAction> getTreeNodeActions(JTree tree) {
		final List<TreeNodeAction> result = new LinkedList<TreeNodeAction>(super.getTreeNodeActions(tree));

		result.add(TreeNodeAction.newSeparatorAction());

		result.add(newShowDetailsAction(tree));

		if (this.isRelated()) {
			result.add(new RemoveRelationAction(tree));
		}

		// add "remove from parent group" action if the parent is a group:
		if (this.getParent() != null && this.getParent() instanceof GroupExplorerNode) {
			result.add(new RemoveFromParentGroupAction(tree));
		}
		return result;
	}

	private TreeNodeAction newShowDetailsAction(JTree tree) {
		final TreeNodeAction result = new ShowDetailsAction(tree);
		final Integer iModuleId = ((GenericObjectExplorerNode) tree.getSelectionPath().getLastPathComponent()).getTreeNode().getModuleId();
		final Integer iGenericObjectId = ((GenericObjectExplorerNode) tree.getSelectionPath().getLastPathComponent()).getTreeNode().getId();
		result.setEnabled(SecurityCache.getInstance().isReadAllowedForModule(Modules.getInstance().getEntityNameByModuleId(iModuleId), iGenericObjectId));
		return result;
	}

	@Override
	public String getDefaultTreeNodeActionCommand(JTree tree) {
		return ACTIONCOMMAND_SHOW_DETAILS;
	}

	/**
	 * For the leased object this method returns just the object icon. For a a possible relation icon call getRelationIcon
	 */
	@Override
	public Icon getIcon(){
		String sResourceName = GenericObjectDelegate.getInstance().getResourceMap().get(getTreeNode().getUsageCriteria().getModuleId());
		String nuclosResource = MetaDataClientProvider.getInstance().getEntity(Modules.getInstance().getEntityNameByModuleId(getTreeNode().getUsageCriteria().getModuleId())).getNuclosResource();
		if (sResourceName != null && ResourceCache.getIconResource(sResourceName) != null) {
			return MainFrame.resizeAndCacheTabIcon(ResourceCache.getIconResource(sResourceName));					
		} else if (nuclosResource != null){
			ImageIcon nuclosIcon = NuclosResourceCache.getNuclosResourceIcon(nuclosResource);
			if (nuclosIcon != null) return MainFrame.resizeAndCacheTabIcon(nuclosIcon);
		}
		return Icons.getInstance().getIconGenericObject16();//GenericObjectMetaDataCache.getInstance().getBestMatchingIcon(getTreeNode().getUsageCriteria());
	}

	public Icon getRelationIcon() {
		final GenericObjectTreeNode treenode = getTreeNode();
		return this.isRelated() ? getRelatedNodeIcon(treenode.getRelationType(), treenode.getRelationDirection()) : null;
	}

	/**
	 * @return Is this node a related node?
	 */
	public boolean isRelated() {
		return this.getTreeNode().isRelated();
	}

	private static Icon getRelatedNodeIcon(SystemRelationType relationtype, RelationDirection direction) {
		final Icon result;
		if (relationtype == null) {
			result = null;
		}
		else {
			switch (relationtype) {
				case PREDECESSOR_OF:
					result = direction.isForward() ? Icons.getInstance().getIconTreeParentToChild() : Icons.getInstance().getIconTreeChildToParent();
					break;
				case PART_OF:
					result = direction.isForward() ? Icons.getInstance().getIconPartOf() : Icons.getInstance().getIconCompositeOf();
					break;
				default:
					result = null;
			}
		}
		return result;
	}

	@Override
	public int getDataTransferSourceActions() {
		return DnDConstants.ACTION_COPY;
	}

	@Override
	public Transferable createTransferable(JTree tree) {
		final GenericObjectTreeNode lotreenode = this.getTreeNode();
		final GenericObjectIdModuleProcess goimp = new GenericObjectIdModuleProcess(lotreenode.getId(),
				lotreenode.getModuleId(), lotreenode.getProcessId(), lotreenode.getLabel());
		return new TransferableGenericObjects(Arrays.asList(new GenericObjectIdModuleProcess[] {goimp}));
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean importTransferData(final Component parent, Transferable transferable, final JTree tree) throws IOException,
			UnsupportedFlavorException {

		final Collection<GenericObjectIdModuleProcess> collgoimp = (Collection<GenericObjectIdModuleProcess>)
				transferable.getTransferData(TransferableGenericObjects.dataFlavor);

		final MutableBoolean mb = new MutableBoolean();

		UIUtils.runCommand(parent, new CommonRunnable() {
			@Override
			public void run() throws CommonBusinessException {
				final GenericObjectTreeNode lotreenodeTarget = getTreeNode();
				final GenericObjectIdModuleProcess goimpTarget = new GenericObjectIdModuleProcess(lotreenodeTarget.getId(),
						lotreenodeTarget.getModuleId(), lotreenodeTarget.getProcessId(), lotreenodeTarget.getLabel());
				mb.setValue(new RelateGenericObjectsController(parent, collgoimp, goimpTarget).run());
			}
		});

		final boolean result = mb.getValue();
		if (result) {
			UIUtils.runCommand(parent, new CommonRunnable() {
				@Override
				public void run() throws CommonBusinessException {
					GenericObjectExplorerNode.this.refresh(tree);
				}
			});
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	protected boolean cmdRelate(Component parent, Transferable transferable, final JTree tree, String sEntityName,
			final String relationType) throws UnsupportedFlavorException, IOException {

		final Collection<GenericObjectIdModuleProcess> collgoimpSource =
				(Collection<GenericObjectIdModuleProcess>) transferable.getTransferData(TransferableGenericObjects.dataFlavor);

		if (CollectionUtils.forall(collgoimpSource, new HasModuleId(Modules.getInstance().getModuleIdByEntityName(sEntityName))))
		{
			final MutableBoolean mb = new MutableBoolean();

			UIUtils.runCommand(parent, new CommonRunnable() {
				@Override
				public void run() throws CommonBusinessException {
					for (GenericObjectIdModuleProcess goimpSource : collgoimpSource) {
						
						if (goimpSource.getGenericObjectId() == getTreeNode().getId()) {
							throw new CommonBusinessException(CommonLocaleDelegate.getMessage("GenericObjectExplorerNode.1", "Der ausgew\u00e4hlte Knoten kann nicht Knoten von sich selber sein!"));
						}
						
						GenericObjectDelegate.getInstance().relate(goimpSource.getGenericObjectId(), relationType,
								getTreeNode().getId(), getTreeNode().getModuleId(), null, null, null);

						refresh(tree);
						mb.setValue(true);
					}
				}
			});
			return mb.getValue();
		}
		else {
			return this.importTransferData(parent, transferable, tree);
		}
	}

	

	@Override
    public Long getId() {
		return getTreeNode().getId().longValue();
    }

	@Override
    public String getEntity() {
	    return MetaDataClientProvider.getInstance().getEntity(getTreeNode().getModuleId().longValue()).getEntity();
    }

	
	/**
	 * inner class ShowDetailsAction. Shows the details for a leased object.
	 */
	private class ShowDetailsAction extends TreeNodeAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		ShowDetailsAction(JTree tree) {
			super(ACTIONCOMMAND_SHOW_DETAILS, CommonLocaleDelegate.getMessage("RuleExplorerNode.1","Details anzeigen"), tree);
		}

		@Override
		public void actionPerformed(ActionEvent ev) {
			this.cmdShowDetails(this.getJTree(), (GenericObjectExplorerNode) this.getJTree().getSelectionPath().getLastPathComponent());
		}

		/**
		 * command: show the details of the leased object represented by the given explorernode
		 * @param tree
		 * @param loexplorernode
		 */
		private void cmdShowDetails(JTree tree, final GenericObjectExplorerNode loexplorernode) {
			UIUtils.runCommand(tree.getParent(), new CommonRunnable() {
				@Override
				public void run() throws CommonBusinessException {
					ExplorerActions.openDetails(GenericObjectExplorerNode.this);
				}
			});
		}
	}	// inner class ShowDetailsAction

	/**
	 * inner class RemoveFromParentGroupAction. Removes this leased object from its parent (group).
	 */
	private class RemoveFromParentGroupAction extends TreeNodeAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * @param tree
		 */
		RemoveFromParentGroupAction(JTree tree) {
			super(ACTIONCOMMAND_REMOVE_FROM_PARENT_GROUP, getMessage("ExplorerController.3","Aus der Gruppe entfernen"), tree);
		}

		@Override
		public void actionPerformed(ActionEvent ev) {
			final JTree tree = this.getJTree();
			final GenericObjectExplorerNode loexplorernode = (GenericObjectExplorerNode) tree.getSelectionPath().getLastPathComponent();
			this.cmdRemoveFromParent(tree, loexplorernode);
		}

		/**
		 * removes this leased object from its parent (group).
		 * @param tree
		 * @param loexplorernode
		 */
		private void cmdRemoveFromParent(final JTree tree, final GenericObjectExplorerNode loexplorernode) {
			/** @todo show confirm dialog */

			UIUtils.runCommand(tree, new Runnable() {
				@Override
				@SuppressWarnings("unchecked")
				public void run() {
					final GenericObjectTreeNode lotreenode = loexplorernode.getTreeNode();
					final int iGenericObjectId = lotreenode.getId();
					final ExplorerNode<? extends TreeNode> explorernodeParent = (ExplorerNode<? extends TreeNode>) loexplorernode.getParent();
					if (explorernodeParent instanceof GroupExplorerNode) {
						final GroupExplorerNode groupexplorernodeParent = (GroupExplorerNode) explorernodeParent;
						final int iGroupId = groupexplorernodeParent.getTreeNode().getId();
						try {
							GenericObjectDelegate.getInstance().removeFromGroup(iGenericObjectId, iGroupId);

							groupexplorernodeParent.refresh(tree);
						}
						catch (CommonBusinessException ex) {
							Errors.getInstance().showExceptionDialog(tree, ex);
						}
					}
				}
			});
		}
	}	// inner class RemoveFromParentGroupAction

	/**
	 * inner class RemoveRelationAction. Removes the relation between this node and its parent.
	 */
	private class RemoveRelationAction extends TreeNodeAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * @param tree
		 */
		RemoveRelationAction(JTree tree) {
			super(ACTIONCOMMAND_REMOVE_RELATION, getMessage("GenericObjectExplorerNode.2", "Beziehung entfernen") + "...", tree);
		}

		@Override
		public void actionPerformed(ActionEvent ev) {
			final JTree tree = this.getJTree();
			
			CommonClientWorkerAdapter<Collectable> searchWorker = new CommonClientWorkerAdapter<Collectable>(null) {
				@Override
				public void init() {
					UIUtils.setWaitCursor(tree);
				}
				
				@Override
				public void work() throws CommonBusinessException {
					final GenericObjectExplorerNode goexplorernode = (GenericObjectExplorerNode) tree.getSelectionPath().getLastPathComponent();
					cmdRemoveRelation(tree, goexplorernode);
				}
				
				@Override
				public void paint() {
					tree.setCursor(null);
				}
				
				@Override
				public void handleError(Exception ex) {
					Errors.getInstance().showExceptionDialog(null, getMessage("GenericObjectExplorerNode.3", "Fehler beim entfernen der Beziehungen"), ex);
				}
			};
			
			CommonMultiThreader.getInstance().execute(searchWorker);
		}

		/**
		 * removes the relation between this node and its parent.
		 * @param tree
		 * @param goexplorernode
		 */
		private void cmdRemoveRelation(final JTree tree, final GenericObjectExplorerNode goexplorernode) {
			final GenericObjectTreeNode gotreenode = goexplorernode.getTreeNode();
			final ExplorerNode<?> explorernodeParent = (ExplorerNode<?>) goexplorernode.getParent();
			if (explorernodeParent instanceof GenericObjectExplorerNode) {
				final GenericObjectExplorerNode goexplorernodeParent = (GenericObjectExplorerNode) explorernodeParent;
				final GenericObjectTreeNode gotreenodeParent = goexplorernodeParent.getTreeNode();

				final boolean bForward = gotreenode.getRelationDirection().isForward();
				final GenericObjectTreeNode gotreenodeSource = bForward ? gotreenodeParent : gotreenode;
				final GenericObjectTreeNode gotreenodeTarget = bForward ? gotreenode : gotreenodeParent;

				final String sMessage = getMessage("GenericObjectExplorerNode.4", "Soll die Beziehung von {0} zu {1} entfernt werden?", gotreenodeSource.getLabel(), gotreenodeTarget.getLabel());

				final int iBtn = JOptionPane.showConfirmDialog(this.getParent(), sMessage, getMessage("GenericObjectExplorerNode.2", "Beziehung entfernen"), JOptionPane.OK_CANCEL_OPTION);
				if (iBtn == JOptionPane.OK_OPTION) {
					UIUtils.runCommand(tree, new CommonRunnable() {
						@Override
						public void run() throws CommonBusinessException {
							final Integer iRelationId = gotreenode.getRelationId();
							if (iRelationId == null) {
								// for backwards compatibility only: this might happen for old deserialized nodes that don't have a relation id yet.
								throw new NuclosBusinessException(getMessage("GenericObjectExplorerNode.5", "Die Beziehung kann nicht entfernt werden, da die Beziehungs-Id fehlt. Bitte aktualisieren Sie die Baumansicht und versuchen Sie es erneut."));
							}
							else {
								GenericObjectDelegate.getInstance().removeRelation(iRelationId, gotreenodeTarget.getId(), gotreenodeTarget.getModuleId());

								goexplorernodeParent.refresh(tree);
							}
						}
					});
				}
			}
		}
	}	// inner class RemoveRelationAction
}	// class GenericObjectExplorerNode