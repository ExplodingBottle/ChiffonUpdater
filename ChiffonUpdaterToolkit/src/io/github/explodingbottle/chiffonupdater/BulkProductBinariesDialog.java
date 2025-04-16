/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SpringLayout;

import io.github.explodingbottle.chiffonupdater.binpack.Binpack;
import io.github.explodingbottle.chiffonupdater.binpack.BinpackProvider;
import io.github.explodingbottle.chiffonupdater.project.ProductInfo;
import io.github.explodingbottle.chiffonupdater.project.VersionInfo;
import io.github.explodingbottle.chiffonupdater.tasks.ToolkitTask;

public class BulkProductBinariesDialog extends JDialog implements ActionListener {

	private static final long serialVersionUID = -5316759556815575483L;

	private JButton closeDialog;
	private JButton load;

	private ChiffonUpdaterProject infos;

	private ToolkitWindow owner;
	private List<String> requiredProductNames;
	private Map<ProductInfo, File> productRoots;

	private boolean regenerateCatalog;
	private boolean forBackendSync;

	public BulkProductBinariesDialog(ToolkitWindow owner, ChiffonUpdaterProject infos, boolean forBackendSync) {
		super(owner, "Bulk product binaries chooser", Dialog.ModalityType.APPLICATION_MODAL);

		Dimension size = owner.getSize();

		Dimension sizeHalf = new Dimension(Math.round((float) (size.getWidth() / 2.0)),
				Math.round((float) (size.getHeight() / 2.0)));
		setSize(sizeHalf);
		setLocationRelativeTo(owner);

		SpringLayout layout = new SpringLayout();
		setLayout(layout);

		this.infos = infos;
		this.owner = owner;
		this.forBackendSync = forBackendSync;

		regenerateCatalog = false;

		if (infos.getMainInfosRef().getWebsiteInfos().allowsCatalogUsage() && forBackendSync) {
			regenerateCatalog = JOptionPane.showConfirmDialog(owner,
					"You have allowed the generation of an update catalog.\n"
							+ "Do you want to regenerate it after synchronization?\n"
							+ "This operation might take some time depending on the size and amount of the products and versions.",
					"Catalog regeneration", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == 0;

		}

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		closeDialog = new JButton("Close");
		load = new JButton("Choose");
		closeDialog.addActionListener(this);
		load.addActionListener(this);

		JTextArea area = new JTextArea();
		boolean cdnone[] = { true };

		area.setText("This dialog helps the toolkit to find a folder that contains all the\r\n"
				+ "files for different product versions so you don't have to choose them\r\n"
				+ "one after each other.\r\n\r\n" + "The structure of the directory must be as follows:\r\n");

		productRoots = new HashMap<ProductInfo, File>();

		File connectedSource = owner.getCurrentConnectedSource();
		if (connectedSource != null) {
			infos.getProductsList().forEach(product -> {
				File trgDir = new File(connectedSource, product.getProductName());
				if (trgDir.exists() && trgDir.isDirectory()) {
					productRoots.put(product, trgDir);

				}
			});
		}

		infos.getProductsList().forEach(product -> {
			if (!productRoots.containsKey(product)) {
				cdnone[0] = false;
				if (!regenerateCatalog) {
					VersionInfo vers = product.getLatestVersion();
					vers.filesForFeatureMap().keySet().forEach(feature -> {
						area.append("\t[folder root]" + File.separator + product.getProductName() + File.separator
								+ vers.getVersionName() + File.separator + feature + "\t\t" + "(Feature: " + feature
								+ ", Version: " + vers.getVersionName() + ", Product: " + product.getProductName() + ")"
								+ "\r\n");
					});
				} else {
					for (VersionInfo vers : product.getVersionsReference()) {
						vers.filesForFeatureMap().keySet().forEach(feature -> {
							area.append("\t[folder root]" + File.separator + File.separator + vers.getVersionName()
									+ File.separator + feature + "\t\t" + "(Feature: " + feature + ", Version: "
									+ vers.getVersionName() + ", Product: " + product.getProductName() + ")" + "\r\n");
						});
					}
				}

				if (requiredProductNames == null) {
					requiredProductNames = new ArrayList<String>();
				}
				requiredProductNames.add(product.getProductName());
			}
		});
		if (cdnone[0]) {
			area.append("\tNo action required, files are in the connected folder.");
		}

		area.setEditable(false);

		JScrollPane areaPane = new JScrollPane(area);
		Container pane = getContentPane();

		layout.putConstraint(SpringLayout.NORTH, areaPane, 10, SpringLayout.NORTH, pane);
		layout.putConstraint(SpringLayout.SOUTH, areaPane, -40, SpringLayout.SOUTH, pane);
		layout.putConstraint(SpringLayout.EAST, areaPane, -10, SpringLayout.EAST, pane);
		layout.putConstraint(SpringLayout.WEST, areaPane, 10, SpringLayout.WEST, pane);

		layout.putConstraint(SpringLayout.NORTH, load, 5, SpringLayout.SOUTH, areaPane);
		layout.putConstraint(SpringLayout.SOUTH, load, -5, SpringLayout.SOUTH, pane);
		layout.putConstraint(SpringLayout.EAST, load, -10, SpringLayout.EAST, pane);
		layout.putConstraint(SpringLayout.WEST, load, -10 - 100, SpringLayout.EAST, pane);

		layout.putConstraint(SpringLayout.NORTH, closeDialog, 0, SpringLayout.NORTH, load);
		layout.putConstraint(SpringLayout.SOUTH, closeDialog, 0, SpringLayout.SOUTH, load);
		layout.putConstraint(SpringLayout.EAST, closeDialog, -10, SpringLayout.WEST, load);
		layout.putConstraint(SpringLayout.WEST, closeDialog, -10 - 100, SpringLayout.WEST, load);

		add(areaPane);
		add(load);
		add(closeDialog);

		getRootPane().registerKeyboardAction(e -> {
			setVisible(false);
			dispose();
		}, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == closeDialog) {
			setVisible(false);
			dispose();
		}
		if (e.getSource() == load) {
			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int returnVal = JFileChooser.APPROVE_OPTION;

			if (productRoots.isEmpty() || requiredProductNames != null) {
				returnVal = chooser.showDialog(owner,
						requiredProductNames != null ? "Select additional bulk versions folder"
								: "Select bulk versions folder");
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					int failed[] = { 0 };
					infos.getProductsList().forEach(product -> {
						if (!productRoots.containsKey(product)) {
							File prod = new File(chooser.getSelectedFile(), product.getProductName());
							if (!prod.exists()) {
								failed[0]++;
							} else {
								productRoots.put(product, prod);
							}
						}
					});
					if (failed[0] > 0) {
						JOptionPane.showMessageDialog(owner, "Failed to find " + failed[0] + " product folders.",
								"Failure", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
			}

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				if (forBackendSync) {
					WebsiteBackendSyncTask syncTask = new WebsiteBackendSyncTask(owner, infos,
							ToolkitMain.getGlobalLogger(), productRoots, regenerateCatalog);
					syncTask.start();

					setVisible(false);
					dispose();
				} else {
					returnVal = chooser.showDialog(owner, "Choose the output folder");
					boolean[] oneFailed = new boolean[1];
					List<ToolkitTask> tasks = new ArrayList<ToolkitTask>();
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						owner.setIsWindowBusy(true);
						BinpackProvider bp = new BinpackProvider();
						Binpack selBp = bp.getBinPack(owner);

						productRoots.forEach((prodInfo, file) -> {
							boolean failure[] = { false };
							File versFldr = new File(file, prodInfo.getLatestVersion().getVersionName());
							if (!versFldr.exists() || !versFldr.isDirectory()) {
								ToolkitMain.getGlobalLogger()
										.print("Missing version folder for " + prodInfo.getProductName());
								oneFailed[0] = true;
								return;
							}
							File eulaFile = BulkLoadDialog.sharedEulaFileFinder(versFldr, prodInfo.getLatestVersion(),
									owner, failure);
							if (failure[0]) {
								ToolkitMain.getGlobalLogger().print("Missing EULA for " + prodInfo.getProductName());
								oneFailed[0] = true;
								return;
							}
							File moduleFile = BulkLoadDialog.sharedModuleFileFinder(versFldr,
									prodInfo.getLatestVersion(), owner, failure);
							if (failure[0]) {
								ToolkitMain.getGlobalLogger()
										.print("Missing custom module for " + prodInfo.getProductName());
								oneFailed[0] = true;
								return;
							}

							Map<String, File> filesToInsert = new HashMap<String, File>();
							if (!BulkLoadDialog.sharedFileMapMaker(versFldr, prodInfo.getLatestVersion(), filesToInsert,
									ToolkitMain.getGlobalLogger())) {
								ToolkitMain.getGlobalLogger()
										.print("Failed to build files map for " + prodInfo.getProductName());
								oneFailed[0] = true;
								return;
							}

							StandalonePackageBuilder stp = new StandalonePackageBuilder(owner, prodInfo,
									prodInfo.getLatestVersion(), filesToInsert, eulaFile, moduleFile, selBp,
									chooser.getSelectedFile(), null, null);
							tasks.add(stp);
						});
					}

					if (oneFailed[0]) {
						JOptionPane.showMessageDialog(owner, "At least one error occured.", "Warning",
								JOptionPane.WARNING_MESSAGE);
						owner.setIsWindowBusy(false);
						return;
					}

					tasks.forEach(task -> {
						task.start();
					});

					setVisible(false);
					dispose();
				}
			}
		}
	}

}
