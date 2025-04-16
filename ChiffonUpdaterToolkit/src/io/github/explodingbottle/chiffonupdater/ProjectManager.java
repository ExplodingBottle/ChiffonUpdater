/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SpringLayout;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import io.github.explodingbottle.chiffonupdater.project.ProductFilesInfo;
import io.github.explodingbottle.chiffonupdater.project.ProductInfo;
import io.github.explodingbottle.chiffonupdater.project.VersionInfo;

public class ProjectManager extends JPanel implements MouseListener {

	private static final long serialVersionUID = -1689854752230711901L;

	private JTree tree;
	private DefaultTreeModel inUseTreeModel;

	private DefaultMutableTreeNode projectNode;
	private Map<DefaultMutableTreeNode, ProductInfo> productNodes;
	private Map<DefaultMutableTreeNode, VersionInfo> versionNodes;
	private Map<DefaultMutableTreeNode, ProductFilesInfo> fileInfosNodes;
	private DefaultMutableTreeNode websiteNode;
	private DefaultMutableTreeNode customizationNode;
	private Editor editor;

	private boolean shouldUpdateOrder;

	private ChiffonUpdaterProject project;

	private ToolkitWindow window;

	public TreeNode getProjectNode() {
		return projectNode;
	}

	public Map<DefaultMutableTreeNode, ProductInfo> getProductNodes() {
		return productNodes;
	}

	public Map<DefaultMutableTreeNode, VersionInfo> getVersionNodes() {
		return versionNodes;
	}

	public Map<DefaultMutableTreeNode, ProductFilesInfo> getFileInfosNodes() {
		return fileInfosNodes;
	}

	public DefaultMutableTreeNode getWebsiteNode() {
		return websiteNode;
	}

	public DefaultMutableTreeNode getCustomizationNode() {
		return customizationNode;
	}

	public void nextUpdateShouldUpdateTreeOrder() {
		shouldUpdateOrder = true;
	}

	private void cleanupRemovedNodes() {
		TreePath selPath = tree.getSelectionPath();
		Object nodeSel = null;
		if (selPath != null) {
			nodeSel = selPath.getLastPathComponent();
		}
		List<DefaultMutableTreeNode> toRmNodes1 = new ArrayList<DefaultMutableTreeNode>();
		List<DefaultMutableTreeNode> toRmNodes2 = new ArrayList<DefaultMutableTreeNode>();
		List<DefaultMutableTreeNode> toRmNodes3 = new ArrayList<DefaultMutableTreeNode>();

		productNodes.forEach((node, infos) -> {
			if (!project.getProductsList().contains(infos)) {
				inUseTreeModel.removeNodeFromParent(node);
				toRmNodes1.add(node);
			}
		});
		versionNodes.forEach((node, infos) -> {
			if (!infos.getParent().getVersionsReference().contains(infos)) {
				inUseTreeModel.removeNodeFromParent(node);
				toRmNodes2.add(node);
			}
		});
		fileInfosNodes.forEach((node, infos) -> {
			if (!infos.getParent().filesForFeatureMap().values().contains(infos)) {
				inUseTreeModel.removeNodeFromParent(node);
				toRmNodes3.add(node);
			}
		});
		for (DefaultMutableTreeNode toRm : toRmNodes1) {
			if (toRm == nodeSel) {
				editor.clearEditor();
			}
			productNodes.remove(toRm);
		}
		for (DefaultMutableTreeNode toRm : toRmNodes2) {
			if (toRm == nodeSel) {
				editor.clearEditor();
			}
			versionNodes.remove(toRm);
		}
		for (DefaultMutableTreeNode toRm : toRmNodes3) {
			if (toRm == nodeSel) {
				editor.clearEditor();
			}
			fileInfosNodes.remove(toRm);
		}
	}

	public ProjectManager(Editor editor, ToolkitWindow window) {
		SpringLayout layout = new SpringLayout();
		setLayout(layout);

		inUseTreeModel = new DefaultTreeModel(null);

		productNodes = new HashMap<DefaultMutableTreeNode, ProductInfo>();
		versionNodes = new HashMap<DefaultMutableTreeNode, VersionInfo>();
		fileInfosNodes = new HashMap<DefaultMutableTreeNode, ProductFilesInfo>();

		this.editor = editor;
		this.window = window;

		tree = new JTree(inUseTreeModel);
		tree.setCellRenderer(new ProjectTreeCellRenderer());
		tree.addMouseListener(this);
		JScrollPane scroller = new JScrollPane(tree);

		layout.putConstraint(SpringLayout.EAST, scroller, 0, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.WEST, scroller, 0, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.NORTH, scroller, 0, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.SOUTH, scroller, 0, SpringLayout.SOUTH, this);

		add(scroller);
	}

	public void updateFromProject(ChiffonUpdaterProject project, File projectPath, boolean isModified) {
		this.project = project;

		if (project == null) {
			projectNode = null;
			inUseTreeModel.setRoot(null);
			return;
		}
		boolean projectNodeChanged = false;
		if (projectNode == null) {
			projectNode = new DefaultMutableTreeNode();
			websiteNode = null;
			customizationNode = null;
			productNodes.clear();
			versionNodes.clear();
			fileInfosNodes.clear();
			inUseTreeModel.setRoot(projectNode);
			projectNodeChanged = true;
		}
		String projectName = "Unnamed";
		if (projectPath != null) {
			projectName = projectPath.getName();
		}
		if (isModified) {
			projectName = "*" + projectName;
		}
		projectNode.setUserObject(new ProjectTreeDisplayObject(projectName, ProjectTreeDisplayObjectType.PROJECT));
		inUseTreeModel.nodeChanged(projectNode);
		if (websiteNode == null) {
			websiteNode = new DefaultMutableTreeNode();
			inUseTreeModel.insertNodeInto(websiteNode, projectNode, 0);
		}

		if (customizationNode == null) {
			customizationNode = new DefaultMutableTreeNode();
			inUseTreeModel.insertNodeInto(customizationNode, projectNode, 0);
			customizationNode.setUserObject(new ProjectTreeDisplayObject("Customization information",
					ProjectTreeDisplayObjectType.CUSTOMIZATION));
			inUseTreeModel.nodeChanged(customizationNode);
		}
		File bkendPth = window.getCurrentlyConnectedBackend();
		websiteNode.setUserObject(
				new ProjectTreeDisplayObject("Website " + (bkendPth == null ? "[Disconnected]" : "[Connected]"),
						ProjectTreeDisplayObjectType.WEBSITE));

		inUseTreeModel.nodeChanged(websiteNode);

		cleanupRemovedNodes();

		for (ProductInfo product : project.getProductsList()) {
			DefaultMutableTreeNode linkedProductNode = null;
			for (Entry<DefaultMutableTreeNode, ProductInfo> entry : productNodes.entrySet()) {
				if (entry.getValue() == product) {
					linkedProductNode = entry.getKey();
					break;
				}
			}
			if (linkedProductNode == null) {
				linkedProductNode = new DefaultMutableTreeNode();
				productNodes.put(linkedProductNode, product);
				inUseTreeModel.insertNodeInto(linkedProductNode, projectNode, 0);
			}
			linkedProductNode.setUserObject(
					new ProjectTreeDisplayObject(product.getProductName(), ProjectTreeDisplayObjectType.PRODUCT));

			inUseTreeModel.nodeChanged(linkedProductNode);

			product.getVersionsReference().sort(new Comparator<VersionInfo>() {

				@Override
				public int compare(VersionInfo o1, VersionInfo o2) {
					if (o1.getIterationNumber() > o2.getIterationNumber()) {
						return 1;
					}
					if (o1.getIterationNumber() < o2.getIterationNumber()) {
						return -1;
					}
					return 0;
				}
			});

			for (VersionInfo version : product.getVersionsReference()) {
				DefaultMutableTreeNode linkedVersionNode = null;
				for (Entry<DefaultMutableTreeNode, VersionInfo> entry : versionNodes.entrySet()) {
					if (entry.getValue() == version) {
						linkedVersionNode = entry.getKey();
						break;
					}
				}
				if (linkedVersionNode == null) {
					linkedVersionNode = new DefaultMutableTreeNode();
					inUseTreeModel.insertNodeInto(linkedVersionNode, linkedProductNode, 0);
					versionNodes.put(linkedVersionNode, version);
				}
				if (shouldUpdateOrder) {
					inUseTreeModel.removeNodeFromParent(linkedVersionNode);
					inUseTreeModel.insertNodeInto(linkedVersionNode, linkedProductNode, 0);
				}
				String versName = version.getVersionName();
				if (product.getLatestVersion() == version) {
					versName = "[X] " + versName;

				}
				linkedVersionNode
						.setUserObject(new ProjectTreeDisplayObject(versName, ProjectTreeDisplayObjectType.VERSION));
				inUseTreeModel.nodeChanged(linkedVersionNode);
				for (String featureName : product.getFeaturesReference()) {
					DefaultMutableTreeNode linkedFileInfosNode = null;
					ProductFilesInfo finfo = version.filesForFeatureMap().get(featureName);
					for (Entry<DefaultMutableTreeNode, ProductFilesInfo> entry : fileInfosNodes.entrySet()) {
						if (entry.getValue() == finfo) {
							linkedFileInfosNode = entry.getKey();
							break;
						}
					}
					if (linkedFileInfosNode == null) {
						linkedFileInfosNode = new DefaultMutableTreeNode();
						fileInfosNodes.put(linkedFileInfosNode, finfo);
						inUseTreeModel.insertNodeInto(linkedFileInfosNode, linkedVersionNode, 0);
					}
					linkedFileInfosNode.setUserObject(
							new ProjectTreeDisplayObject(featureName, ProjectTreeDisplayObjectType.FEATURE));
					inUseTreeModel.nodeChanged(linkedFileInfosNode);
				}
			}
			shouldUpdateOrder = false;
		}

		if (projectNodeChanged) {
			inUseTreeModel.nodeStructureChanged(projectNode);
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {

	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

	}

	private void openEditor(Object selNode) {
		if (selNode == projectNode) {
			editor.setEditor(new MainProjectEditor(window, project.getMainInfosRef()));
		}
		if (productNodes.containsKey(selNode)) {
			editor.setEditor(new ProductEditor(window, productNodes.get(selNode)));
		}
		if (versionNodes.containsKey(selNode)) {
			editor.setEditor(new VersionEditor(window, versionNodes.get(selNode)));
		}
		if (fileInfosNodes.containsKey(selNode)) {
			editor.setEditor(new FilesInfoEditor(window, fileInfosNodes.get(selNode)));
		}
		if (selNode == websiteNode) {
			editor.setEditor(new WebsiteConfigEditor(window, project.getMainInfosRef().getWebsiteInfos()));
		}

		if (selNode == customizationNode) {
			editor.setEditor(new CustomizationInfosEditor(window, project.getMainInfosRef().getCustomizationInfos()));
		}
	}

	public void reopenEditor() {
		if (tree.getLastSelectedPathComponent() != null) {
			openEditor(tree.getLastSelectedPathComponent());
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		int selRow = tree.getRowForLocation(e.getX(), e.getY());
		TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
		if (selRow != -1) {
			Object selNode = selPath.getLastPathComponent();
			if (e.getButton() == MouseEvent.BUTTON1) {
				openEditor(selNode);
			}
			if (e.getButton() == MouseEvent.BUTTON3) {
				new TreePopup((TreeNode) selNode, this, window).show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {

	}

}
