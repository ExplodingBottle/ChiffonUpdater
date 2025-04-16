/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import io.github.explodingbottle.chiffonupdater.project.ProductFilesInfo;

public class FilesInfoEditor extends ComponentEditor implements ActionListener, HashFinishedListener, DocumentListener {

	private static final long serialVersionUID = -976424782892133364L;

	private JLabel descriptionHelp;
	private JTable fileInfosTable;

	private JLabel cDetecText;
	private JLabel cActionsText;

	private JTextArea cDetecArea;
	private JTextArea cActionsArea;

	private JButton deleteSelection;
	private JButton addFromFolder;
	private FilesTableModel tableModel;

	private ProductFilesInfo infos;
	private String relatedFeature;

	private JScrollPane infosPane;

	public FilesInfoEditor(ToolkitWindow handler, ProductFilesInfo infos) {
		super(handler);

		SpringLayout spring = new SpringLayout();
		setLayout(spring);

		this.infos = infos;

		tableModel = new FilesTableModel(infos, returnHandler());
		fileInfosTable = new JTable(tableModel);
		fileInfosTable.setColumnSelectionAllowed(false);

		cDetecArea = new JTextArea();
		cActionsArea = new JTextArea();

		String strend[] = { null };
		infos.getParent().filesForFeatureMap().forEach((f, i) -> {
			if (i == infos) {
				strend[0] = f;
			}
		});
		relatedFeature = strend[0];

		String cdt = infos.getParent().customDetectionCommandsByFeatureMap().get(relatedFeature);
		String cat = infos.getParent().customActionCommandsByFeatureMap().get(relatedFeature);
		if (cdt != null) {
			cDetecArea.setText(cdt);
		}
		if (cat != null) {
			cActionsArea.setText(cat);
		}

		cDetecArea.getDocument().addDocumentListener(this);
		cActionsArea.getDocument().addDocumentListener(this);
		cDetecArea.setToolTipText("Additional detection commands only for this feature.");
		cActionsArea.setToolTipText("Additional action commands only for this feature.");

		infosPane = new JScrollPane(fileInfosTable);
		JScrollPane cdaPane = new JScrollPane(cDetecArea);
		JScrollPane caaPane = new JScrollPane(cActionsArea);

		cDetecText = new JLabel(
				"Detection commands (expected result;command name;arguments) (CUSTOM FUNCTIONS UNAVAILABLE HERE):");
		cActionsText = new JLabel("Action commands (command name;arguments):");

		descriptionHelp = new JLabel("<html>" + "<h3>File information and commands</h3><br>"
				+ "This editor allows you to define file that must be included in a certain version of a certain feature.<br>"
				+ "Entries without a hash are considered as empty folders<br>"
				+ "This editor also allows you to customize list of detection and file commands.<br>"
				+ "<i>Note that the custom commands will only be executed after all the standard commands.</i><br>"
				+ "</html>");

		deleteSelection = new JButton("Delete selection");
		deleteSelection.setToolTipText("Deletes the selected entries.");
		addFromFolder = new JButton("Auto add files from folder");
		addFromFolder.setToolTipText("Automatically adds the different hashes from a folder containing the binaries.");

		deleteSelection.addActionListener(this);
		addFromFolder.addActionListener(this);

		spring.putConstraint(SpringLayout.NORTH, descriptionHelp, 10, SpringLayout.NORTH, this);
		spring.putConstraint(SpringLayout.SOUTH, descriptionHelp, 10 + 120, SpringLayout.NORTH, this);
		spring.putConstraint(SpringLayout.EAST, descriptionHelp, -10, SpringLayout.EAST, this);
		spring.putConstraint(SpringLayout.WEST, descriptionHelp, 10, SpringLayout.WEST, this);

		spring.putConstraint(SpringLayout.NORTH, deleteSelection, 10, SpringLayout.SOUTH, descriptionHelp);
		spring.putConstraint(SpringLayout.SOUTH, deleteSelection, 10 + 20, SpringLayout.SOUTH, descriptionHelp);
		spring.putConstraint(SpringLayout.EAST, deleteSelection, 10 + 200, SpringLayout.WEST, this);
		spring.putConstraint(SpringLayout.WEST, deleteSelection, 10, SpringLayout.WEST, this);

		spring.putConstraint(SpringLayout.NORTH, addFromFolder, 0, SpringLayout.NORTH, deleteSelection);
		spring.putConstraint(SpringLayout.SOUTH, addFromFolder, 0, SpringLayout.SOUTH, deleteSelection);
		spring.putConstraint(SpringLayout.EAST, addFromFolder, 10 + 200, SpringLayout.EAST, deleteSelection);
		spring.putConstraint(SpringLayout.WEST, addFromFolder, 10, SpringLayout.EAST, deleteSelection);

		spring.putConstraint(SpringLayout.NORTH, infosPane, 10, SpringLayout.SOUTH, deleteSelection);
		spring.putConstraint(SpringLayout.SOUTH, infosPane, 10 + 150, SpringLayout.SOUTH, deleteSelection);
		spring.putConstraint(SpringLayout.EAST, infosPane, -10, SpringLayout.EAST, this);
		spring.putConstraint(SpringLayout.WEST, infosPane, 10, SpringLayout.WEST, this);

		spring.putConstraint(SpringLayout.NORTH, cDetecText, 5, SpringLayout.SOUTH, infosPane);
		spring.putConstraint(SpringLayout.SOUTH, cDetecText, 5 + 20, SpringLayout.SOUTH, infosPane);
		spring.putConstraint(SpringLayout.EAST, cDetecText, -10, SpringLayout.EAST, this);
		spring.putConstraint(SpringLayout.WEST, cDetecText, 10, SpringLayout.WEST, this);

		spring.putConstraint(SpringLayout.NORTH, cdaPane, 5, SpringLayout.SOUTH, cDetecText);
		spring.putConstraint(SpringLayout.SOUTH, cdaPane, 5 + 100, SpringLayout.SOUTH, cDetecText);
		spring.putConstraint(SpringLayout.EAST, cdaPane, -10, SpringLayout.EAST, this);
		spring.putConstraint(SpringLayout.WEST, cdaPane, 10, SpringLayout.WEST, this);

		spring.putConstraint(SpringLayout.NORTH, cActionsText, 5, SpringLayout.SOUTH, cdaPane);
		spring.putConstraint(SpringLayout.SOUTH, cActionsText, 5 + 20, SpringLayout.SOUTH, cdaPane);
		spring.putConstraint(SpringLayout.EAST, cActionsText, -10, SpringLayout.EAST, this);
		spring.putConstraint(SpringLayout.WEST, cActionsText, 10, SpringLayout.WEST, this);

		spring.putConstraint(SpringLayout.NORTH, caaPane, 5, SpringLayout.SOUTH, cActionsText);
		spring.putConstraint(SpringLayout.SOUTH, caaPane, 5 + 100, SpringLayout.SOUTH, cActionsText);
		spring.putConstraint(SpringLayout.EAST, caaPane, -10, SpringLayout.EAST, this);
		spring.putConstraint(SpringLayout.WEST, caaPane, 10, SpringLayout.WEST, this);

		setPreferredSize(new Dimension(650, 600));

		if (!"true".equals(
				ToolkitMain.getPersistentStorage().getSettings().getProperty("editor.settings.advanced.show"))) {
			caaPane.setVisible(false);
			cActionsText.setVisible(false);
			cdaPane.setVisible(false);
			cDetecText.setVisible(false);
			setPreferredSize(new Dimension(650, 350));
		}

		add(descriptionHelp);
		add(infosPane);
		add(deleteSelection);
		add(addFromFolder);
		add(cDetecText);
		add(cActionsText);
		add(cdaPane);
		add(caaPane);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource() == deleteSelection) {
			tableModel.deleteSelection(fileInfosTable.getSelectedRows());
			markAsModified();
		}
		if (arg0.getSource() == addFromFolder) {
			File connectedSource = returnHandler().getCurrentConnectedSource();
			if (connectedSource != null) {
				File trgDir = new File(connectedSource, infos.getParent().getParent().getProductName() + "/"
						+ infos.getParent().getVersionName() + "/" + relatedFeature);
				if (trgDir.exists() && trgDir.isDirectory()) {
					HashTask hash = new HashTask(returnHandler(), trgDir, this);
					hash.start();
					return;
				}
			}
			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int returnVal = chooser.showDialog(returnHandler(), "Select version folder");
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				HashTask hash = new HashTask(returnHandler(), chooser.getSelectedFile(), this);
				hash.start();

			}

		}

	}

	@Override
	public void onHashFinished(List<FileInfos> hashes, HashTask task) {
		infos.getFileHashesListReference().addAll(hashes);
		tableModel.fireTableDataChanged();
		fileInfosTable.changeSelection(infos.getFileHashesListReference().size() - 1, 0, false, false);
		markAsModified();
		SwingUtilities.invokeLater(() -> {
			JScrollBar verticalBar = infosPane.getVerticalScrollBar();
			verticalBar.setValue(verticalBar.getMaximum());
		});

	}

	private void onFieldChange(DocumentEvent e) {
		if (e.getDocument() == cDetecArea.getDocument()) {
			if (cDetecArea.getText().isEmpty()) {
				infos.getParent().customDetectionCommandsByFeatureMap().remove(relatedFeature);
			} else {
				infos.getParent().customDetectionCommandsByFeatureMap().put(relatedFeature, cDetecArea.getText());
			}
			markAsModified();
		}
		if (e.getDocument() == cActionsArea.getDocument()) {
			if (cActionsArea.getText().isEmpty()) {
				infos.getParent().customActionCommandsByFeatureMap().remove(relatedFeature);
			} else {
				infos.getParent().customActionCommandsByFeatureMap().put(relatedFeature, cActionsArea.getText());
			}
			markAsModified();
		}
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		onFieldChange(e);

	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		onFieldChange(e);

	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		onFieldChange(e);

	}

}
