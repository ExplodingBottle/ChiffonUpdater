/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import io.github.explodingbottle.chiffonupdater.project.VersionInfo;

public class VersionEditor extends ComponentEditor
		implements DocumentListener, FocusListener, ItemListener, ActionListener {

	private static final long serialVersionUID = -7227038511077054040L;

	private JLabel descriptionHelp;
	private JLabel invalidDate;
	private VersionInfo infos;
	private JTextField versionName;
	private JTextField versionReleaseDate;
	private JTextField reportingFile;
	private JTextField iterationNumber;
	private JTextArea versionDescription;
	private JTextArea cPrereqArea;

	private JTextField customModulePath;
	private JTextField customModulePublisher;
	private JTextField eulaPath;

	private JButton increaseVersionRank;
	private JButton decreaseVersionRank;

	private JLabel untoleratedName;

	private static final SimpleDateFormat USED_UPDREL_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	static {
		USED_UPDREL_FORMAT.setLenient(false);
	}

	private JCheckBox shouldShowDesc;
	private JCheckBox shouldHideInCatalog;

	private VersionInfo versionBefore, versionAfter;

	private long lastCastedIterNumb;

	private void updateTextFields() {
		customModulePublisher.setEnabled(customModulePath.getText() != null && !customModulePath.getText().isEmpty());
	}

	private ToolkitWindow handler;

	public VersionEditor(ToolkitWindow handler, VersionInfo infos) {
		super(handler);

		SpringLayout spring = new SpringLayout();
		setLayout(spring);

		this.infos = infos;
		this.handler = handler;

		descriptionHelp = new JLabel(
				"<html>" + "<h3>Version</h3><br>" + "This editor allows you choose a name for your version.<br>"
						+ "For setting up the file hashes, you must navigate to the feature inside of the version.<br>"
						+ "The reporting file corresponds to the file that will register itself to the products<br>"
						+ "list on the end-user system." + "</html>");

		untoleratedName = new JLabel();
		untoleratedName.setForeground(Color.RED);
		untoleratedName.setVisible(false);

		JLabel vName = new JLabel("Version name:");
		JLabel rName = new JLabel("Reporting file:");
		JLabel iNumb = new JLabel("Iteration number:");
		JLabel vDesc = new JLabel("Version description:");
		JLabel vRelDt = new JLabel("Version release date:");
		JLabel cPrereqText = new JLabel("Prerequisite commands (expected result;command name;arguments):");

		JLabel cMod = new JLabel("Custom module:");
		JLabel cModPb = new JLabel("Module publisher class:");
		JLabel eDesc = new JLabel("EULA text:");

		invalidDate = new JLabel("Invalid date format. Make sure it is YYYY-MM-DD.");
		invalidDate.setForeground(Color.RED);
		invalidDate.setVisible(false);

		shouldShowDesc = new JCheckBox("Show the description in the update package");
		shouldShowDesc.setToolTipText(
				"Check if you want to be able to read the description of this update in the update package.");
		shouldShowDesc.setSelected(infos.getShowVersionDescription());
		shouldShowDesc.addItemListener(this);

		shouldHideInCatalog = new JCheckBox("Hide this version from catalog");
		shouldHideInCatalog
				.setToolTipText("Chooses if this specific version be kept out from the catalog for download.");
		shouldHideInCatalog.setSelected(infos.shouldHideInCatalog());
		shouldHideInCatalog.addItemListener(this);

		increaseVersionRank = new JButton("Increase version rank");
		increaseVersionRank.addActionListener(this);
		increaseVersionRank.setToolTipText(
				"Increases the rank of this version by changing its iteration number to make it appear as newer.");
		decreaseVersionRank = new JButton("Decrease version rank");
		decreaseVersionRank.addActionListener(this);
		decreaseVersionRank.setToolTipText(
				"Decreases the rank of this version by changing its iteration number to make it appear as older.");

		cPrereqArea = new JTextArea();
		if (infos.getCustomPrerequisiteCommands() != null) {
			cPrereqArea.setText(infos.getCustomPrerequisiteCommands());
		}
		cPrereqArea.getDocument().addDocumentListener(this);

		JScrollPane cPrereqPane = new JScrollPane(cPrereqArea);
		cPrereqArea.setToolTipText(
				"The commands that will be ran before proceeding to the update to check the prerequisites.");

		JLabel custFilesText = new JLabel(
				"<html>You can also write here the path for custom files, such as the EULA and the custom update module.<br>"
						+ "The path is relative to the version folder (so its parent will be the product folder) and it is possible<br>"
						+ "to use '..'<br>" + "</html>");

		versionReleaseDate = new JTextField(USED_UPDREL_FORMAT.format(new Date(infos.getReleaseDate())));
		versionReleaseDate.setToolTipText("The release date of this version that will be used.");
		versionReleaseDate.getDocument().addDocumentListener(this);

		customModulePath = new JTextField(infos.getCustomModulePath());
		customModulePath.getDocument().addDocumentListener(this);
		customModulePath.setToolTipText(
				"The path relative to the corresponding update version folder of a possible custom module.");
		eulaPath = new JTextField(infos.getCustomEulaPath());
		eulaPath.getDocument().addDocumentListener(this);
		eulaPath.setToolTipText(
				"The path relative to the corresponding update version folder of a possible EULA file.");
		customModulePublisher = new JTextField(infos.getCustomModulePublisher());
		customModulePublisher.getDocument().addDocumentListener(this);
		customModulePublisher.setToolTipText(
				"If a custom module has been chosen, this must be the full name of the publisher class.");

		versionName = new JTextField(infos.getVersionName());
		versionName.setToolTipText("The unique identifier for this version of the product.");
		versionName.getDocument().addDocumentListener(this);
		reportingFile = new JTextField(infos.getReportingFileName());
		reportingFile.setToolTipText(
				"The file that is responsible of reporting the product to the list of installed products.");
		reportingFile.getDocument().addDocumentListener(this);
		iterationNumber = new JTextField("" + infos.getIterationNumber());
		iterationNumber
				.setToolTipText("The number of this version, the biggest number is linked to the latest version.");
		iterationNumber.getDocument().addDocumentListener(this);
		lastCastedIterNumb = infos.getIterationNumber();
		versionDescription = new JTextArea(infos.getVersionDescription());
		versionDescription.setToolTipText("The description of this version that will be shown to the end-user.");
		versionDescription.getDocument().addDocumentListener(this);
		iterationNumber.addFocusListener(this);

		JScrollPane scrollableDesc = new JScrollPane(versionDescription);

		spring.putConstraint(SpringLayout.NORTH, descriptionHelp, 10, SpringLayout.NORTH, this);
		spring.putConstraint(SpringLayout.SOUTH, descriptionHelp, 10 + 120, SpringLayout.NORTH, this);
		spring.putConstraint(SpringLayout.EAST, descriptionHelp, -10, SpringLayout.EAST, this);
		spring.putConstraint(SpringLayout.WEST, descriptionHelp, 10, SpringLayout.WEST, this);

		spring.putConstraint(SpringLayout.NORTH, vName, 10, SpringLayout.SOUTH, descriptionHelp);
		spring.putConstraint(SpringLayout.SOUTH, vName, 10 + 20, SpringLayout.SOUTH, descriptionHelp);
		spring.putConstraint(SpringLayout.WEST, vName, 10, SpringLayout.WEST, this);
		spring.putConstraint(SpringLayout.EAST, vName, 10 + 100, SpringLayout.WEST, this);

		spring.putConstraint(SpringLayout.NORTH, versionName, 0, SpringLayout.NORTH, vName);
		spring.putConstraint(SpringLayout.SOUTH, versionName, 0, SpringLayout.SOUTH, vName);
		spring.putConstraint(SpringLayout.WEST, versionName, 10, SpringLayout.EAST, vName);
		spring.putConstraint(SpringLayout.EAST, versionName, -20, SpringLayout.EAST, this);

		spring.putConstraint(SpringLayout.NORTH, untoleratedName, 10, SpringLayout.SOUTH, vName);
		spring.putConstraint(SpringLayout.SOUTH, untoleratedName, 10 + 20, SpringLayout.SOUTH, vName);
		spring.putConstraint(SpringLayout.WEST, untoleratedName, 10, SpringLayout.WEST, this);
		spring.putConstraint(SpringLayout.EAST, untoleratedName, -20, SpringLayout.EAST, this);

		spring.putConstraint(SpringLayout.NORTH, rName, 10, SpringLayout.SOUTH, untoleratedName);
		spring.putConstraint(SpringLayout.SOUTH, rName, 10 + 20, SpringLayout.SOUTH, untoleratedName);
		spring.putConstraint(SpringLayout.WEST, rName, 10, SpringLayout.WEST, this);
		spring.putConstraint(SpringLayout.EAST, rName, 10 + 100, SpringLayout.WEST, this);

		spring.putConstraint(SpringLayout.NORTH, reportingFile, 0, SpringLayout.NORTH, rName);
		spring.putConstraint(SpringLayout.SOUTH, reportingFile, 0, SpringLayout.SOUTH, rName);
		spring.putConstraint(SpringLayout.WEST, reportingFile, 10, SpringLayout.EAST, rName);
		spring.putConstraint(SpringLayout.EAST, reportingFile, -20, SpringLayout.EAST, this);

		spring.putConstraint(SpringLayout.NORTH, increaseVersionRank, 10, SpringLayout.SOUTH, rName);
		spring.putConstraint(SpringLayout.SOUTH, increaseVersionRank, 10 + 30, SpringLayout.SOUTH, rName);
		spring.putConstraint(SpringLayout.WEST, increaseVersionRank, 10, SpringLayout.WEST, this);
		spring.putConstraint(SpringLayout.EAST, increaseVersionRank, 10 + 200, SpringLayout.WEST, this);

		spring.putConstraint(SpringLayout.NORTH, decreaseVersionRank, 0, SpringLayout.NORTH, increaseVersionRank);
		spring.putConstraint(SpringLayout.SOUTH, decreaseVersionRank, 0, SpringLayout.SOUTH, increaseVersionRank);
		spring.putConstraint(SpringLayout.WEST, decreaseVersionRank, 10, SpringLayout.EAST, increaseVersionRank);
		spring.putConstraint(SpringLayout.EAST, decreaseVersionRank, 10 + 200, SpringLayout.EAST, increaseVersionRank);

		spring.putConstraint(SpringLayout.NORTH, iNumb, 10, SpringLayout.SOUTH, increaseVersionRank);
		spring.putConstraint(SpringLayout.SOUTH, iNumb, 10 + 20, SpringLayout.SOUTH, increaseVersionRank);
		spring.putConstraint(SpringLayout.WEST, iNumb, 10, SpringLayout.WEST, this);
		spring.putConstraint(SpringLayout.EAST, iNumb, 10 + 100, SpringLayout.WEST, this);

		spring.putConstraint(SpringLayout.NORTH, iterationNumber, 0, SpringLayout.NORTH, iNumb);
		spring.putConstraint(SpringLayout.SOUTH, iterationNumber, 0, SpringLayout.SOUTH, iNumb);
		spring.putConstraint(SpringLayout.WEST, iterationNumber, 10, SpringLayout.EAST, iNumb);
		spring.putConstraint(SpringLayout.EAST, iterationNumber, -20, SpringLayout.EAST, this);

		spring.putConstraint(SpringLayout.NORTH, vRelDt, 10, SpringLayout.SOUTH, iNumb);
		spring.putConstraint(SpringLayout.SOUTH, vRelDt, 10 + 20, SpringLayout.SOUTH, iNumb);
		spring.putConstraint(SpringLayout.WEST, vRelDt, 10, SpringLayout.WEST, this);
		spring.putConstraint(SpringLayout.EAST, vRelDt, 10 + 150, SpringLayout.WEST, this);

		spring.putConstraint(SpringLayout.NORTH, invalidDate, 0, SpringLayout.NORTH, vRelDt);
		spring.putConstraint(SpringLayout.SOUTH, invalidDate, 0, SpringLayout.SOUTH, vRelDt);
		spring.putConstraint(SpringLayout.WEST, invalidDate, -20 - 320, SpringLayout.EAST, this);
		spring.putConstraint(SpringLayout.EAST, invalidDate, -20, SpringLayout.EAST, this);

		spring.putConstraint(SpringLayout.NORTH, versionReleaseDate, 0, SpringLayout.NORTH, vRelDt);
		spring.putConstraint(SpringLayout.SOUTH, versionReleaseDate, 0, SpringLayout.SOUTH, vRelDt);
		spring.putConstraint(SpringLayout.WEST, versionReleaseDate, 10, SpringLayout.EAST, vRelDt);
		spring.putConstraint(SpringLayout.EAST, versionReleaseDate, -10, SpringLayout.WEST, invalidDate);

		spring.putConstraint(SpringLayout.NORTH, vDesc, 10, SpringLayout.SOUTH, vRelDt);
		spring.putConstraint(SpringLayout.SOUTH, vDesc, 10 + 20, SpringLayout.SOUTH, vRelDt);
		spring.putConstraint(SpringLayout.WEST, vDesc, 10, SpringLayout.WEST, this);
		spring.putConstraint(SpringLayout.EAST, vDesc, 10 + 200, SpringLayout.WEST, this);

		spring.putConstraint(SpringLayout.NORTH, shouldShowDesc, 0, SpringLayout.NORTH, vDesc);
		spring.putConstraint(SpringLayout.SOUTH, shouldShowDesc, 0, SpringLayout.SOUTH, vDesc);
		spring.putConstraint(SpringLayout.WEST, shouldShowDesc, 5, SpringLayout.EAST, vDesc);
		spring.putConstraint(SpringLayout.EAST, shouldShowDesc, -10, SpringLayout.EAST, this);

		spring.putConstraint(SpringLayout.NORTH, scrollableDesc, 10, SpringLayout.SOUTH, vDesc);
		spring.putConstraint(SpringLayout.SOUTH, scrollableDesc, 10 + 100, SpringLayout.SOUTH, vDesc);
		spring.putConstraint(SpringLayout.WEST, scrollableDesc, 10, SpringLayout.WEST, this);
		spring.putConstraint(SpringLayout.EAST, scrollableDesc, -10, SpringLayout.EAST, this);

		spring.putConstraint(SpringLayout.NORTH, shouldHideInCatalog, 10, SpringLayout.SOUTH, scrollableDesc);
		spring.putConstraint(SpringLayout.SOUTH, shouldHideInCatalog, 10 + 20, SpringLayout.SOUTH, scrollableDesc);
		spring.putConstraint(SpringLayout.WEST, shouldHideInCatalog, 10, SpringLayout.WEST, this);
		spring.putConstraint(SpringLayout.EAST, shouldHideInCatalog, -10, SpringLayout.EAST, this);

		spring.putConstraint(SpringLayout.NORTH, custFilesText, 10, SpringLayout.SOUTH, shouldHideInCatalog);
		spring.putConstraint(SpringLayout.SOUTH, custFilesText, 10 + 50, SpringLayout.SOUTH, shouldHideInCatalog);
		spring.putConstraint(SpringLayout.WEST, custFilesText, 10, SpringLayout.WEST, this);
		spring.putConstraint(SpringLayout.EAST, custFilesText, -10, SpringLayout.EAST, this);

		spring.putConstraint(SpringLayout.NORTH, cMod, 10, SpringLayout.SOUTH, custFilesText);
		spring.putConstraint(SpringLayout.SOUTH, cMod, 10 + 20, SpringLayout.SOUTH, custFilesText);
		spring.putConstraint(SpringLayout.WEST, cMod, 10, SpringLayout.WEST, this);
		spring.putConstraint(SpringLayout.EAST, cMod, 10 + 100, SpringLayout.WEST, this);

		spring.putConstraint(SpringLayout.NORTH, customModulePath, 0, SpringLayout.NORTH, cMod);
		spring.putConstraint(SpringLayout.SOUTH, customModulePath, 0, SpringLayout.SOUTH, cMod);
		spring.putConstraint(SpringLayout.WEST, customModulePath, 10, SpringLayout.EAST, cMod);
		spring.putConstraint(SpringLayout.EAST, customModulePath, 10 + 150, SpringLayout.EAST, cMod);

		spring.putConstraint(SpringLayout.NORTH, cModPb, 0, SpringLayout.NORTH, customModulePath);
		spring.putConstraint(SpringLayout.SOUTH, cModPb, 0, SpringLayout.SOUTH, customModulePath);
		spring.putConstraint(SpringLayout.WEST, cModPb, 10, SpringLayout.EAST, customModulePath);
		spring.putConstraint(SpringLayout.EAST, cModPb, 10 + 150, SpringLayout.EAST, customModulePath);

		spring.putConstraint(SpringLayout.NORTH, customModulePublisher, 0, SpringLayout.NORTH, cModPb);
		spring.putConstraint(SpringLayout.SOUTH, customModulePublisher, 0, SpringLayout.SOUTH, cModPb);
		spring.putConstraint(SpringLayout.WEST, customModulePublisher, 10, SpringLayout.EAST, cModPb);
		spring.putConstraint(SpringLayout.EAST, customModulePublisher, 10 + 150, SpringLayout.EAST, cModPb);

		spring.putConstraint(SpringLayout.NORTH, eDesc, 0, SpringLayout.NORTH, customModulePublisher);
		spring.putConstraint(SpringLayout.SOUTH, eDesc, 0, SpringLayout.SOUTH, customModulePublisher);
		spring.putConstraint(SpringLayout.WEST, eDesc, 10, SpringLayout.EAST, customModulePublisher);
		spring.putConstraint(SpringLayout.EAST, eDesc, 10 + 100, SpringLayout.EAST, customModulePublisher);

		spring.putConstraint(SpringLayout.NORTH, eulaPath, 0, SpringLayout.NORTH, eDesc);
		spring.putConstraint(SpringLayout.SOUTH, eulaPath, 0, SpringLayout.SOUTH, eDesc);
		spring.putConstraint(SpringLayout.WEST, eulaPath, 10, SpringLayout.EAST, eDesc);
		spring.putConstraint(SpringLayout.EAST, eulaPath, 10 + 150, SpringLayout.EAST, eDesc);

		spring.putConstraint(SpringLayout.NORTH, cPrereqText, 10, SpringLayout.SOUTH, customModulePath);
		spring.putConstraint(SpringLayout.SOUTH, cPrereqText, 10 + 40, SpringLayout.SOUTH, customModulePath);
		spring.putConstraint(SpringLayout.WEST, cPrereqText, 10, SpringLayout.WEST, this);
		spring.putConstraint(SpringLayout.EAST, cPrereqText, -10, SpringLayout.EAST, this);

		spring.putConstraint(SpringLayout.NORTH, cPrereqPane, 10, SpringLayout.SOUTH, cPrereqText);
		spring.putConstraint(SpringLayout.SOUTH, cPrereqPane, 10 + 70, SpringLayout.SOUTH, cPrereqText);
		spring.putConstraint(SpringLayout.WEST, cPrereqPane, 10, SpringLayout.WEST, this);
		spring.putConstraint(SpringLayout.EAST, cPrereqPane, -10, SpringLayout.EAST, this);

		setPreferredSize(new Dimension(880, 730));

		if (!"true".equals(
				ToolkitMain.getPersistentStorage().getSettings().getProperty("editor.settings.advanced.show"))) {
			iterationNumber.setVisible(false);
			iNumb.setVisible(false);
			shouldHideInCatalog.setVisible(false);
			cPrereqPane.setVisible(false);
			cPrereqText.setVisible(false);
			eulaPath.setVisible(false);
			eDesc.setVisible(false);
			eulaPath.setVisible(false);
			eDesc.setVisible(false);
			customModulePublisher.setVisible(false);
			cModPb.setVisible(false);
			customModulePath.setVisible(false);
			cMod.setVisible(false);
			custFilesText.setVisible(false);

			setPreferredSize(new Dimension(880, 490));
		}

		add(descriptionHelp);
		add(vName);
		add(versionName);
		add(rName);
		add(reportingFile);
		add(iNumb);
		add(iterationNumber);
		add(vDesc);
		add(scrollableDesc);
		add(custFilesText);
		add(customModulePath);
		add(eulaPath);
		add(customModulePublisher);
		add(cMod);
		add(eDesc);
		add(cModPb);
		add(cPrereqText);
		add(cPrereqPane);
		add(shouldShowDesc);
		add(vRelDt);
		add(invalidDate);
		add(versionReleaseDate);
		add(shouldHideInCatalog);
		add(increaseVersionRank);
		add(decreaseVersionRank);
		add(untoleratedName);

		updateTextFields();
		updateCheckboxState();
		updateRankChangerControls();

	}

	private void updateRankChangerControls() {
		versionBefore = null;
		versionAfter = null;
		for (VersionInfo version : infos.getParent().getVersionsReference()) {
			if ((versionBefore == null || version.getIterationNumber() > versionBefore.getIterationNumber())
					&& !version.equals(infos)) {
				if (version.getIterationNumber() <= infos.getIterationNumber()) {
					versionBefore = version;
				}
			}
			if ((versionAfter == null || version.getIterationNumber() < versionAfter.getIterationNumber())
					&& !version.equals(infos)) {
				if (version.getIterationNumber() >= infos.getIterationNumber()) {
					versionAfter = version;
				}
			}
		}
		increaseVersionRank.setEnabled(versionAfter != null);
		decreaseVersionRank.setEnabled(versionBefore != null);

	}

	private void updateCheckboxState() {
		shouldShowDesc.setEnabled(!versionDescription.getText().isEmpty());
	}

	public void handleDocumentChange(DocumentEvent e) {
		if (e.getDocument() == versionName.getDocument()) {

			List<String> versionNames = new ArrayList<String>();
			for (VersionInfo vers : infos.getParent().getVersionsReference()) {
				if (vers != infos) {
					versionNames.add(vers.getVersionName().toLowerCase());
				}
			}
			boolean visible = false;
			if (versionNames.contains(versionName.getText().toLowerCase())) {
				String name = null;
				for (int i = 1; name == null; i++) {
					String tName = versionName.getText() + "_ " + i;
					if (!versionNames.contains(tName.toLowerCase())) {
						name = tName;
						untoleratedName.setText("Conflict with an existing version. Name will be " + tName);
						visible = true;
					}
				}
				infos.setVersionName(name);
			} else {
				infos.setVersionName(versionName.getText());
			}

			untoleratedName.setVisible(visible);
			markAsModified();
		}
		if (e.getDocument() == versionReleaseDate.getDocument()) {
			try {
				infos.setReleaseDate(USED_UPDREL_FORMAT.parse(versionReleaseDate.getText()).getTime());
				invalidDate.setVisible(false);
				markAsModified();
			} catch (ParseException exc) {
				invalidDate.setVisible(true);
			}
		}
		if (e.getDocument() == reportingFile.getDocument()) {
			infos.setReportingFileName(reportingFile.getText());
			markAsModified();
		}
		if (e.getDocument() == versionDescription.getDocument()) {
			infos.setVersionDescription(versionDescription.getText());
			updateCheckboxState();
			markAsModified();
		}
		if (e.getDocument() == customModulePath.getDocument()) {
			infos.setCustomModulePath(customModulePath.getText());
			updateTextFields();
			markAsModified();
		}
		if (e.getDocument() == eulaPath.getDocument()) {
			infos.setCustomEulaPath(eulaPath.getText());
			markAsModified();
		}
		if (e.getDocument() == customModulePublisher.getDocument()) {
			infos.setCustomModulePublisher(customModulePublisher.getText());
			markAsModified();
		}
		if (e.getDocument() == cPrereqArea.getDocument()) {
			infos.setCustomPrerequisiteCommands(cPrereqArea.getText());
			markAsModified();
		}
		if (e.getDocument() == iterationNumber.getDocument()) {
			try {
				long castedIteration = Long.parseLong(iterationNumber.getText());
				lastCastedIterNumb = castedIteration;
				infos.setIterationNumber(lastCastedIterNumb);
				handler.getProjectManager().nextUpdateShouldUpdateTreeOrder();
			} catch (NumberFormatException exc) {
				return;
			}

			markAsModified();
		}
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		handleDocumentChange(e);

	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		handleDocumentChange(e);

	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		handleDocumentChange(e);

	}

	@Override
	public void focusGained(FocusEvent e) {

	}

	@Override
	public void focusLost(FocusEvent e) {
		try {
			long castedIteration = Long.parseLong(iterationNumber.getText());
			lastCastedIterNumb = castedIteration;
			infos.setIterationNumber(lastCastedIterNumb);
		} catch (NumberFormatException exc) {
			iterationNumber.setText("" + lastCastedIterNumb);
		}

	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() == shouldShowDesc) {
			infos.setShowVersionDescription(shouldShowDesc.isSelected());
			markAsModified();
		}
		if (e.getSource() == shouldHideInCatalog) {
			infos.setShouldHideInCatalog(shouldHideInCatalog.isSelected());
			markAsModified();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == increaseVersionRank) {
			long oldIter = versionAfter.getIterationNumber();
			versionAfter.setIterationNumber(infos.getIterationNumber());
			iterationNumber.setText("" + oldIter);
			updateRankChangerControls();
			markAsModified();
		}
		if (e.getSource() == decreaseVersionRank) {
			long oldIter = versionBefore.getIterationNumber();
			versionBefore.setIterationNumber(infos.getIterationNumber());
			iterationNumber.setText("" + oldIter);
			updateRankChangerControls();
		}
	}

}
