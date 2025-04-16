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

import io.github.explodingbottle.chiffonupdater.project.ProductInfo;
import io.github.explodingbottle.chiffonupdater.project.VersionInfo;
import io.github.explodingbottle.chiffonupdater.tasks.ToolkitTask;

public class BulkLoadDialog extends JDialog implements ActionListener {

	private static final long serialVersionUID = -5316759556815575483L;

	private JButton closeDialog;
	private JButton load;

	private ProductInfo infos;

	private ToolkitWindow owner;
	private VersionInfo target;
	private GlobalLogger logger;

	private File autochosenFile;

	JTextArea area = new JTextArea();

	public static boolean sharedFileMapMaker(File versFolder, VersionInfo target, Map<String, File> filesToInsert,
			GlobalLogger logger) {
		boolean oneFailed[] = { false };
		target.filesForFeatureMap().forEach((feature, filesInfo) -> {
			File featureFolder = new File(versFolder, feature);
			if (!featureFolder.exists()) {
				logger.print("Failed to find feature folder " + target.getVersionName() + "/" + feature + ".");
				oneFailed[0] = true;
			}
			if (!oneFailed[0]) {
				filesInfo.getFileHashesListReference().forEach(f -> {
					File file = new File(featureFolder, f.getFilePath());
					if(file.isFile()) {
						HashComputer computer = new HashComputer(file, new SharedLoggerAdapter(logger));
						if (!f.getFileHash().equalsIgnoreCase(computer.computeHash())) {
							oneFailed[0] = true;
						} else {
							filesToInsert.put(f.getFileHash(), file);
						}
					}
					/*
					if (file.isDirectory() && f.getFileHash().isEmpty()) {
						filesToInsert.put(f.getFileHash(), file);
					} else {
						
					}
					*/
				});
			}

		});
		return !oneFailed[0];
	}

	public static File sharedEulaFileFinder(File versFolder, VersionInfo target, ToolkitWindow owner,
			boolean failure[]) {
		if (target.getCustomEulaPath() != null && !target.getCustomEulaPath().isEmpty()) {
			File cEulaPth = new File(versFolder, target.getCustomEulaPath());
			if (!cEulaPth.exists()) {
				failure[0] = true;

				return null;
			}
			return cEulaPth;
		}
		return null;
	}

	public static File sharedModuleFileFinder(File versFolder, VersionInfo target, ToolkitWindow owner,
			boolean failure[]) {
		if (target.getCustomModulePath() != null && !target.getCustomModulePath().isEmpty()
				&& target.getCustomModulePublisher() != null && !target.getCustomModulePublisher().isEmpty()) {
			File cModPth = new File(versFolder, target.getCustomModulePath());
			if (!cModPth.exists()) {
				failure[0] = true;

				return null;
			}
			return cModPth;
		}
		return null;
	}

	private void extendBulkLoadInformations(VersionInfo vers) {
		vers.filesForFeatureMap().keySet().forEach(feature -> {
			area.append("\t[folder root]" + File.separator + vers.getVersionName() + File.separator + feature + "\t\t"
					+ "(Feature: " + feature + ", Version: " + vers.getVersionName() + ")" + "\r\n");
		});
	}

	public BulkLoadDialog(ToolkitWindow owner, ProductInfo infos, VersionInfo target, GlobalLogger logger) {
		super(owner, target == null ? "Bulk load" : "Package creation", Dialog.ModalityType.APPLICATION_MODAL);
		Dimension size = owner.getSize();

		Dimension sizeHalf = new Dimension(Math.round((float) (size.getWidth() / 2.0)),
				Math.round((float) (size.getHeight() / 2.0)));
		setSize(sizeHalf);
		setLocationRelativeTo(owner);

		SpringLayout layout = new SpringLayout();
		setLayout(layout);

		this.logger = logger;
		this.target = target;

		this.infos = infos;
		this.owner = owner;

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		closeDialog = new JButton("Close");
		load = new JButton(target == null ? "Bulk load" : "Build");
		closeDialog.addActionListener(this);
		load.addActionListener(this);

		area.setText(target == null
				? "This will load all the files for the different versions from the same directory.\r\n"
						+ "This task is available for the purpose of being more efficient.\r\n"
						+ "Please note, that previous files data, if any, will be replaced.\r\n\r\n"
						+ "The structure of the directory must be as follows:\r\n"
				: "This will retrieve all the files for the different versions from the same directory.\r\n"
						+ "The selection of the different files is required in order to build the package.\r\n\r\n"
						+ "The structure of the directory must be as follows:\r\n");
		File connectedSource = owner.getCurrentConnectedSource();
		if (connectedSource != null) {
			File trgDir = new File(connectedSource, infos.getProductName());
			if (trgDir.exists() && trgDir.isDirectory()) {
				autochosenFile = trgDir;
				area.append("\tNo action required, files are in the connected folder.");
			}
		}
		if (autochosenFile == null) {
			if (target == null) {
				infos.getVersionsReference().forEach(vers -> {
					extendBulkLoadInformations(vers);
				});
			} else {
				extendBulkLoadInformations(target);
			}
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
			File selectedFile = autochosenFile;
			if (autochosenFile == null) {
				returnVal = chooser.showDialog(owner,
						target == null ? "Select bulk versions folder" : "Select package source folder");
				selectedFile = chooser.getSelectedFile();
			}
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				// HashTask hash = new HashTask(owner, chooser.getSelectedFile(), this);
				// hash.start();
				owner.setIsWindowBusy(true);
				boolean[] oneFailed = new boolean[1];
				if (target == null) {
					List<ToolkitTask> tasks = new ArrayList<ToolkitTask>();

					BulkLoadDialog dialogRef = this;
					File[] fpthrough = { selectedFile };
					infos.getVersionsReference().forEach(vers -> {
						File versFolder = new File(fpthrough[0], vers.getVersionName());
						vers.filesForFeatureMap().forEach((feature, filesInfo) -> {
							File featureFolder = new File(versFolder, feature);
							if (!featureFolder.exists()) {
								oneFailed[0] = true;
							}
							HashTask hash = new HashTask(owner, featureFolder, new HashFinishedListener() {
								@Override
								public void onHashFinished(List<FileInfos> hashes, HashTask task) {
									if (hashes != null) {
										filesInfo.getFileHashesListReference().clear();
										filesInfo.getFileHashesListReference().addAll(hashes);
									} else {
										oneFailed[0] = true;
									}
									synchronized (dialogRef) {
										tasks.remove(task);
									}
									if (tasks.size() == 0) {
										owner.setIsWindowBusy(false);
										if (oneFailed[0]) {
											JOptionPane.showMessageDialog(owner, "At least one hash operation failed.",
													"Failure", JOptionPane.WARNING_MESSAGE);
										} else {
											JOptionPane.showMessageDialog(owner,
													"All the hash operations successfully completed.", "Success",
													JOptionPane.INFORMATION_MESSAGE);
										}
									}
								}
							});
							hash.setShouldImpactMainWindow(false);
							tasks.add(hash);
						});
					});

					if (oneFailed[0]) {
						JOptionPane.showMessageDialog(owner, "Malformed folder.", "Failure", JOptionPane.ERROR_MESSAGE);
						owner.setIsWindowBusy(false);
						return;
					}

					owner.onDataChanged();
					tasks.forEach(task -> {
						task.start();
					});
				} else {
					File versFolder = new File(selectedFile, target.getVersionName());
					if (!versFolder.exists() || !versFolder.isDirectory()) {
						JOptionPane.showMessageDialog(owner, "Version folder not found", "Failure",
								JOptionPane.ERROR_MESSAGE);
						owner.setIsWindowBusy(false);
						return;
					}
					Map<String, File> filesToInsert = new HashMap<String, File>();

					if (!sharedFileMapMaker(versFolder, target, filesToInsert, logger)) {
						JOptionPane.showMessageDialog(owner, "Binary folder doesn't match version data", "Failure",
								JOptionPane.ERROR_MESSAGE);
						owner.setIsWindowBusy(false);
						return;
					}

					boolean failure[] = { false };
					File eulaFile = sharedEulaFileFinder(versFolder, target, owner, failure);
					if (failure[0]) {
						JOptionPane.showMessageDialog(owner, "Custom EULA file missing.", "Failure",
								JOptionPane.ERROR_MESSAGE);
						owner.setIsWindowBusy(false);
						return;
					}
					File moduleFile = sharedModuleFileFinder(versFolder, target, owner, failure);
					if (failure[0]) {
						JOptionPane.showMessageDialog(owner, "Custom module file missing.", "Failure",
								JOptionPane.ERROR_MESSAGE);
						owner.setIsWindowBusy(false);
						return;
					}

					StandalonePackageBuilder stpb = new StandalonePackageBuilder(owner, infos, target, filesToInsert,
							eulaFile, moduleFile, null, null, null, null);
					stpb.start();

				}

				setVisible(false);
				dispose();
			}
		}
	}

}
