/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import io.github.explodingbottle.chiffonupdater.project.MainProjectInfo;

public class MainProjectEditor extends ComponentEditor implements ItemListener, DocumentListener {

	private static final long serialVersionUID = 9180179048792484429L;

	private JLabel descriptionHelp;

	private JTextField globalPath;
	private JCheckBox useGlobalPath;

	private JTextField windowsPath;
	private JTextField macPath;
	private JTextField unixPath;

	private MainProjectInfo infos;

	public MainProjectEditor(ToolkitWindow handler, MainProjectInfo infos) {
		super(handler);
		SpringLayout spring = new SpringLayout();
		setLayout(spring);
		descriptionHelp = new JLabel("<html>" + "<h3>Project settings</h3><br>"
				+ "This editor allows you to configure a required information for this project.<br>"
				+ "These settings tell where will the list of products and other files required<br>"
				+ "for the well functioning of the system will be stored on the end-user machines.<br>"
				+ "Use $VAR$ to replace by a Java system property and %VAR% to replace by an environment variable."
				+ "</html>");
		// descriptionHelp.setBorder(BorderFactory.createLineBorder(Color.BLACK));

		JLabel gPathText = new JLabel("Global Path:");
		JLabel wPathText = new JLabel("Windows Path:");
		JLabel mPathText = new JLabel("MacOS Path:");
		JLabel lPathText = new JLabel("Unix Path:");

		this.infos = infos;

		globalPath = new JTextField();
		globalPath.setToolTipText("The path to the update system root folder on a client machine for any OS.");
		useGlobalPath = new JCheckBox("Use the global path instead of system specific paths.");
		useGlobalPath.setToolTipText("Whether we use the global path or an OS specific path.");

		windowsPath = new JTextField();
		windowsPath.setToolTipText("The path to the update system root folder on a client machine for Windows.");
		macPath = new JTextField();
		macPath.setToolTipText("The path to the update system root folder on a client machine for MacOS.");
		unixPath = new JTextField();
		unixPath.setToolTipText("The path to the update system root folder on a client machine for Unix.");

		globalPath.setText(infos.getGlobalPath());
		windowsPath.setText(infos.getWindowsPath());
		macPath.setText(infos.getMacPath());
		unixPath.setText(infos.getUnixPath());

		useGlobalPath.setSelected(infos.usesGlobalPath());
		updateEnableStates();

		useGlobalPath.addItemListener(this);
		globalPath.getDocument().addDocumentListener(this);
		windowsPath.getDocument().addDocumentListener(this);
		macPath.getDocument().addDocumentListener(this);
		unixPath.getDocument().addDocumentListener(this);

		spring.putConstraint(SpringLayout.NORTH, descriptionHelp, 10, SpringLayout.NORTH, this);
		spring.putConstraint(SpringLayout.SOUTH, descriptionHelp, 10 + 130, SpringLayout.NORTH, this);
		spring.putConstraint(SpringLayout.EAST, descriptionHelp, -10, SpringLayout.EAST, this);
		spring.putConstraint(SpringLayout.WEST, descriptionHelp, 10, SpringLayout.WEST, this);

		spring.putConstraint(SpringLayout.NORTH, gPathText, 10, SpringLayout.SOUTH, descriptionHelp);
		spring.putConstraint(SpringLayout.SOUTH, gPathText, 10 + 20, SpringLayout.SOUTH, descriptionHelp);
		spring.putConstraint(SpringLayout.WEST, gPathText, 10, SpringLayout.WEST, this);
		spring.putConstraint(SpringLayout.EAST, gPathText, 10 + 100, SpringLayout.WEST, this);

		spring.putConstraint(SpringLayout.NORTH, globalPath, 0, SpringLayout.NORTH, gPathText);
		spring.putConstraint(SpringLayout.SOUTH, globalPath, 0, SpringLayout.SOUTH, gPathText);
		spring.putConstraint(SpringLayout.WEST, globalPath, 10, SpringLayout.EAST, gPathText);
		spring.putConstraint(SpringLayout.EAST, globalPath, -20, SpringLayout.EAST, this);

		spring.putConstraint(SpringLayout.NORTH, windowsPath, 0, SpringLayout.NORTH, wPathText);
		spring.putConstraint(SpringLayout.SOUTH, windowsPath, 0, SpringLayout.SOUTH, wPathText);
		spring.putConstraint(SpringLayout.WEST, windowsPath, 10, SpringLayout.EAST, wPathText);
		spring.putConstraint(SpringLayout.EAST, windowsPath, -20, SpringLayout.EAST, this);

		spring.putConstraint(SpringLayout.NORTH, unixPath, 0, SpringLayout.NORTH, lPathText);
		spring.putConstraint(SpringLayout.SOUTH, unixPath, 0, SpringLayout.SOUTH, lPathText);
		spring.putConstraint(SpringLayout.WEST, unixPath, 10, SpringLayout.EAST, lPathText);
		spring.putConstraint(SpringLayout.EAST, unixPath, -20, SpringLayout.EAST, this);

		spring.putConstraint(SpringLayout.NORTH, macPath, 0, SpringLayout.NORTH, mPathText);
		spring.putConstraint(SpringLayout.SOUTH, macPath, 0, SpringLayout.SOUTH, mPathText);
		spring.putConstraint(SpringLayout.WEST, macPath, 10, SpringLayout.EAST, mPathText);
		spring.putConstraint(SpringLayout.EAST, macPath, -20, SpringLayout.EAST, this);

		spring.putConstraint(SpringLayout.NORTH, useGlobalPath, 10, SpringLayout.SOUTH, gPathText);
		spring.putConstraint(SpringLayout.SOUTH, useGlobalPath, 10 + 20, SpringLayout.SOUTH, gPathText);
		spring.putConstraint(SpringLayout.WEST, useGlobalPath, 0, SpringLayout.WEST, gPathText);
		spring.putConstraint(SpringLayout.EAST, useGlobalPath, -20, SpringLayout.EAST, this);

		spring.putConstraint(SpringLayout.NORTH, wPathText, 10, SpringLayout.SOUTH, useGlobalPath);
		spring.putConstraint(SpringLayout.SOUTH, wPathText, 10 + 20, SpringLayout.SOUTH, useGlobalPath);
		spring.putConstraint(SpringLayout.WEST, wPathText, 10, SpringLayout.WEST, this);
		spring.putConstraint(SpringLayout.EAST, wPathText, 10 + 100, SpringLayout.WEST, this);

		spring.putConstraint(SpringLayout.NORTH, mPathText, 10, SpringLayout.SOUTH, wPathText);
		spring.putConstraint(SpringLayout.SOUTH, mPathText, 10 + 20, SpringLayout.SOUTH, wPathText);
		spring.putConstraint(SpringLayout.WEST, mPathText, 10, SpringLayout.WEST, this);
		spring.putConstraint(SpringLayout.EAST, mPathText, 10 + 100, SpringLayout.WEST, this);

		spring.putConstraint(SpringLayout.NORTH, lPathText, 10, SpringLayout.SOUTH, mPathText);
		spring.putConstraint(SpringLayout.SOUTH, lPathText, 10 + 20, SpringLayout.SOUTH, mPathText);
		spring.putConstraint(SpringLayout.WEST, lPathText, 10, SpringLayout.WEST, this);
		spring.putConstraint(SpringLayout.EAST, lPathText, 10 + 100, SpringLayout.WEST, this);

		setPreferredSize(new Dimension(550, 300));

		add(descriptionHelp);
		add(gPathText);
		add(wPathText);
		add(lPathText);
		add(mPathText);
		add(globalPath);
		add(useGlobalPath);
		add(windowsPath);
		add(macPath);
		add(unixPath);

	}

	public void updateEnableStates() {
		if (useGlobalPath.isSelected()) {
			globalPath.setEnabled(true);
			windowsPath.setEnabled(false);
			unixPath.setEnabled(false);
			macPath.setEnabled(false);
		} else {
			globalPath.setEnabled(false);
			windowsPath.setEnabled(true);
			unixPath.setEnabled(true);
			macPath.setEnabled(true);
		}
	}

	public void updateInfosTextChanged(DocumentEvent e) {
		if (e.getDocument() == globalPath.getDocument()) {
			infos.setGlobalPath(globalPath.getText());
		}
		if (e.getDocument() == windowsPath.getDocument()) {
			infos.setWindowsPath(windowsPath.getText());
		}
		if (e.getDocument() == unixPath.getDocument()) {
			infos.setUnixPath(unixPath.getText());
		}
		if (e.getDocument() == macPath.getDocument()) {
			infos.setMacPath(macPath.getText());
		}
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		updateInfosTextChanged(e);
		markAsModified();

	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		updateInfosTextChanged(e);
		markAsModified();

	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		updateInfosTextChanged(e);
		markAsModified();

	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() == useGlobalPath) {
			infos.setUseGlobalPath(useGlobalPath.isSelected());
			updateEnableStates();
			markAsModified();
		}
	}

}
