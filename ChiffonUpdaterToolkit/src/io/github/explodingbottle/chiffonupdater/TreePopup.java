/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.tree.TreeNode;

import io.github.explodingbottle.chiffonupdater.project.ProductInfo;
import io.github.explodingbottle.chiffonupdater.project.VersionInfo;
import io.github.explodingbottle.chiffonupdater.tasks.WebsiteFrontendConfigBuilder;

public class TreePopup extends JPopupMenu implements ActionListener {

	private static final long serialVersionUID = 7564634798211361041L;

	private JMenuItem generateExternalLibrary;
	private JMenuItem regNewProduct;
	private JMenuItem buildAll;
	private JMenuItem save;
	private JMenuItem saveAs;
	private JMenuItem close;

	private JMenuItem deleteProduct;
	private JMenuItem addVersion;
	private JMenuItem bulkLoadVersions;

	private JMenuItem connectBackendFolder;
	private JMenuItem disconnectBackendFolder;
	private JMenuItem synchronizeBackendFiles;
	private JMenuItem createFrontendConfigFile;

	private JMenuItem versionRemove;
	// private JMenuItem setLatest;
	private JMenuItem buildForVersion;
	private JMenuItem buildLatest;

	private ToolkitWindow window;

	private ChiffonUpdaterProject project;
	private ProductInfo linkedProduct;
	private VersionInfo linkedVersion;

	public TreePopup(TreeNode node, ProjectManager mgr, ToolkitWindow window) {
		this.window = window;
		project = window.getProject();
		if (node == mgr.getProjectNode()) {
			generateExternalLibrary = new JMenuItem("Generate external library");
			save = new JMenuItem("Save", KeyEvent.VK_S);
			saveAs = new JMenuItem("Save as...", KeyEvent.VK_A);
			close = new JMenuItem("Close", KeyEvent.VK_C);
			regNewProduct = new JMenuItem("Register new product", KeyEvent.VK_R);
			buildAll = new JMenuItem("Build all packages", KeyEvent.VK_B);
			generateExternalLibrary.addActionListener(this);
			save.addActionListener(this);
			saveAs.addActionListener(this);
			close.addActionListener(this);
			regNewProduct.addActionListener(this);
			buildAll.addActionListener(this);
			add(regNewProduct);
			add(generateExternalLibrary);
			add(buildAll);
			addSeparator();
			add(save);
			add(saveAs);
			add(close);
		}
		if (node == mgr.getWebsiteNode()) {
			connectBackendFolder = new JMenuItem("Connect backend folder", KeyEvent.VK_B);
			disconnectBackendFolder = new JMenuItem("Disconnect backend folder", KeyEvent.VK_D);
			synchronizeBackendFiles = new JMenuItem("Synchronize backend files", KeyEvent.VK_Y);
			createFrontendConfigFile = new JMenuItem("Create frontend configuration file", KeyEvent.VK_F);

			if (window.getCurrentlyConnectedBackend() != null) {
				connectBackendFolder.setEnabled(false);
			} else {
				disconnectBackendFolder.setEnabled(false);
				synchronizeBackendFiles.setEnabled(false);
			}
			connectBackendFolder.addActionListener(this);
			disconnectBackendFolder.addActionListener(this);
			synchronizeBackendFiles.addActionListener(this);
			createFrontendConfigFile.addActionListener(this);

			add(connectBackendFolder);
			add(disconnectBackendFolder);
			add(synchronizeBackendFiles);
			add(createFrontendConfigFile);
		}
		if (mgr.getProductNodes().containsKey(node)) {
			linkedProduct = mgr.getProductNodes().get(node);

			buildLatest = new JMenuItem("Build update package", KeyEvent.VK_B);
			deleteProduct = new JMenuItem("Delete the product", KeyEvent.VK_D);
			addVersion = new JMenuItem("Add version", KeyEvent.VK_A);
			bulkLoadVersions = new JMenuItem("Bulk load versions", KeyEvent.VK_L);
			deleteProduct.addActionListener(this);
			addVersion.addActionListener(this);
			bulkLoadVersions.addActionListener(this);
			buildLatest.addActionListener(this);
			add(buildLatest);
			add(bulkLoadVersions);
			add(addVersion);
			addSeparator();
			add(deleteProduct);
		}
		if (mgr.getVersionNodes().containsKey(node)) {
			linkedVersion = mgr.getVersionNodes().get(node);
			linkedProduct = linkedVersion.getParent();

			versionRemove = new JMenuItem("Delete", KeyEvent.VK_D);
			// setLatest = new JMenuItem("Set as latest");
			buildForVersion = new JMenuItem("Build update package for this version", KeyEvent.VK_B);

			versionRemove.addActionListener(this);
			// setLatest.addActionListener(this);
			buildForVersion.addActionListener(this);
			add(buildForVersion);
			// add(setLatest);
			addSeparator();
			add(versionRemove);
		}

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == generateExternalLibrary) {
			window.generateExternalLibrary();
		}
		if (e.getSource() == save) {
			window.saveProject();
		}
		if (e.getSource() == saveAs) {
			window.saveProjectAs();
		}
		if (e.getSource() == disconnectBackendFolder) {
			window.disconnectBackend();
		}
		if (e.getSource() == connectBackendFolder) {
			window.connectBackend();
		}
		if (e.getSource() == createFrontendConfigFile) {
			WebsiteFrontendConfigBuilder cfgBuilder = new WebsiteFrontendConfigBuilder(window,
					project.getMainInfosRef().getWebsiteInfos(), ToolkitMain.getGlobalLogger());
			cfgBuilder.start();
		}
		if (e.getSource() == deleteProduct) {
			if (JOptionPane.showConfirmDialog(window, "Are you sure you want to delete this product ?", "Confirmation",
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != 0) {
				return;
			}
			project.getProductsList().remove(linkedProduct);
			window.onDataChanged();
		}
		/*
		 * if (e.getSource() == setLatest) {
		 * linkedVersion.getParent().setLatestVersion(linkedVersion);
		 * window.onDataChanged(); }
		 */
		if (e.getSource() == bulkLoadVersions) {
			BulkLoadDialog dialog = new BulkLoadDialog(window, linkedProduct, null, ToolkitMain.getGlobalLogger());
			dialog.setVisible(true);
		}
		if (e.getSource() == buildAll) {
			BulkProductBinariesDialog dialog = new BulkProductBinariesDialog(window, project, false);
			dialog.setVisible(true);
		}
		if (e.getSource() == synchronizeBackendFiles) {
			BulkProductBinariesDialog dialog = new BulkProductBinariesDialog(window, project, true);
			dialog.setVisible(true);
		}
		if (e.getSource() == buildLatest) {
			// new DetectionFileBuilder(linkedProduct).buildDetectionProperties();
			/*
			 * StandalonePackageBuilder builder = new StandalonePackageBuilder(window,
			 * linkedProduct, linkedProduct.getLatestVersion()); builder.start();
			 */
			BulkLoadDialog dialog = new BulkLoadDialog(window, linkedProduct, linkedProduct.getLatestVersion(),
					ToolkitMain.getGlobalLogger());
			dialog.setVisible(true);
		}
		if (e.getSource() == buildForVersion) {
			BulkLoadDialog dialog = new BulkLoadDialog(window, linkedProduct, linkedVersion,
					ToolkitMain.getGlobalLogger());
			dialog.setVisible(true);
			/*
			 * StandalonePackageBuilder builder = new StandalonePackageBuilder(window,
			 * linkedVersion.getParent(), linkedVersion); builder.start();
			 */
		}
		if (e.getSource() == versionRemove) {
			if (JOptionPane.showConfirmDialog(window, "Are you sure you want to remove this version ?", "Confirmation",
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != 0) {
				return;
			}
			linkedVersion.getParent().getVersionsReference().remove(linkedVersion);
			
			/*
			 * if (linkedVersion.getParent().getLatestVersion() == linkedVersion) { if
			 * (linkedVersion.getParent().getVersionsReference().size() == 0) {
			 * linkedVersion.getParent().setLatestVersion(null); } else {
			 * linkedVersion.getParent().setLatestVersion(linkedVersion.getParent().
			 * getVersionsReference().get(0)); } }
			 */
			window.onDataChanged();
		}
		if (e.getSource() == addVersion) {
			NewVersionDialog dialog = new NewVersionDialog(window, linkedProduct);
			dialog.setVisible(true);
		}
		if (e.getSource() == close) {
			window.closeProject(false);
		}
		if (e.getSource() == regNewProduct) {
			window.regNewProduct();
		}
	}

}
