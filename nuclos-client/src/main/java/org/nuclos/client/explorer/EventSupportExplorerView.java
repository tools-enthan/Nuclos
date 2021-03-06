package org.nuclos.client.explorer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.tree.TreeSelectionModel;

import org.nuclos.client.eventsupport.EventSupportActionHandler.ACTIONS;
import org.nuclos.client.explorer.node.eventsupport.EventSupportDragListener;
import org.nuclos.client.explorer.node.eventsupport.EventSupportTreeWillExpandListener;
import org.nuclos.client.explorer.ui.ExplorerNodeRenderer;
import org.nuclos.client.synthetica.NuclosThemeSettings;
import org.nuclos.client.ui.CenteringPanel;
import org.nuclos.client.ui.ColoredLabel;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.server.navigation.treenode.TreeNode;

public class EventSupportExplorerView extends JPanel implements ExplorerView 
{
	public static final int FADE = 2;
	private final JToolBar toolBar = UIUtils.createNonFloatableToolBar();
	private final JScrollPane scrlpn = new JScrollPane();
	private final JTree tree;
	private JTextField searchTextField = new JTextField();
	
	Map<ACTIONS, AbstractAction> actions;
	
	private final JPanel content = new JPanel(new BorderLayout()) {

		@Override
		public void paint(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setPaint(new GradientPaint(0, 0, NuclosThemeSettings.BACKGROUND_PANEL, 0, FADE, Color.WHITE));
			g2.fillRect(0, 0, getWidth(), getHeight());

			super.paint(g);
		}
	};
	
	
	public EventSupportExplorerView(TreeNode tn, Map<ACTIONS, AbstractAction> actions)
	{
		super(new BorderLayout());
		this.setOpaque(false);
		this.setBorder(BorderFactory.createEmptyBorder());

		this.actions = actions;
		
		this.scrlpn.setBorder(BorderFactory.createEmptyBorder());
		this.scrlpn.setOpaque(false);
		this.scrlpn.getViewport().setOpaque(false);

		this.content.setOpaque(true);
		this.content.setBackground(new Color (0, 0, 0, 0));
		this.content.setBorder(BorderFactory.createEmptyBorder(FADE, 0, 0, 0));
		this.content.add(this.scrlpn, BorderLayout.CENTER);

		addToolBarComponents(this.toolBar);
		
		this.add(this.toolBar, BorderLayout.NORTH);
		this.add(this.content, BorderLayout.CENTER);

		final ExplorerNode<?> explorernodeRoot = ExplorerController.newExplorerTree(tn);
		this.tree = new JTree(explorernodeRoot, true);
		this.tree.putClientProperty("JTree.lineStyle", "Angled");
		this.tree.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
		this.tree.setBackground(Color.WHITE);
		this.tree.setRootVisible(true);
		this.tree.setShowsRootHandles(true);
		this.tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		
		addDNDFunctionality(this.tree);
		
		// don't expand on double click:
		this.tree.setToggleClickCount(0);

		this.tree.addKeyListener(new DefaultKeyListener(tree));
		this.tree.addMouseListener(new DefaultMouseListener(tree));

		// enable tool tips:
		ToolTipManager.sharedInstance().registerComponent(this.tree);

		this.tree.setCellRenderer(new ExplorerNodeRenderer());
		this.tree.addTreeWillExpandListener(new EventSupportTreeWillExpandListener(tree));
		
		this.scrlpn.getViewport().add(tree, null);
		
	}
	
	
	protected void addToolBarComponents(JToolBar toolbar) {
		if (getActions().containsKey(ACTIONS.ACTION_REFRESH_SOURCETREE))
			toolBar.add(new JButton(getActions().get(ACTIONS.ACTION_REFRESH_SOURCETREE)));
		
		final AbstractAction abstractAction = getActions().get(ACTIONS.ACTION_RUN_SOURCETREE_SEARCH);
		searchTextField.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {}
			
			@Override
			public void keyReleased(KeyEvent e) {}
			
			@Override
			public void keyPressed(KeyEvent e) {
				JTextField txt = (JTextField) e.getSource();
				if (txt.getText().length() > 3 || txt.getText().length() == 0) {
					abstractAction.actionPerformed(new ActionEvent(e.getSource(), e.getID(), null));
				}
			}
		});
		
		searchTextField.setPreferredSize(new Dimension(150,20));
		
		CenteringPanel cpSearchFilter = new CenteringPanel(searchTextField) {

			@Override
			public Dimension getMinimumSize() {
				return this.getCenteredComponent().getMinimumSize();
			}

			@Override
			public Dimension getMaximumSize() {
				return this.getCenteredComponent().getPreferredSize();
			}

		};
		cpSearchFilter.setOpaque(false);
		ColoredLabel bl = new ColoredLabel(cpSearchFilter, SpringLocaleDelegate.getInstance().getMessage("CollectController.Search.Filter","Filter"));
		bl.setName("blChooseFilter");

		toolbar.add(bl);
	}

	public Map<ACTIONS, AbstractAction> getActions() {
		return actions;
	}

	protected void addDNDFunctionality(JTree pTree) {
		EventSupportDragListener dndListener = new EventSupportDragListener();
		DragSource src = new DragSource();
		DragGestureRecognizer gestRec = src.
                createDefaultDragGestureRecognizer(pTree, 
                DnDConstants.ACTION_MOVE, dndListener);	
	}
	
	@Override
	public JComponent getViewComponent() {
		return this;
	}

	@Override
	public JTree getJTree() {
		return this.tree;
	}

	@Override
	public ExplorerNode<?> getRootNode() {
		return (ExplorerNode<?>) this.getJTree().getModel().getRoot();
	}


	public JTextField getSearchTextField() {
		return searchTextField;
	}
	
	public void setSearchTextField(JTextField newTextField) {
		this.searchTextField = newTextField;
	}
	
	
}
