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

import io.github.explodingbottle.chiffonupdater.project.WebsiteInfo;

public class WebsiteConfigEditor extends ComponentEditor implements DocumentListener, ItemListener {

	private static final long serialVersionUID = 9180179048792484429L;

	private JLabel descriptionHelp;
	private JLabel secondaryHelp;

	private JTextField backendURL;
	private JTextField agentDlURL;
	private JTextField packageDlURL;
	private JTextField packageVersDlURL;

	private JCheckBox allowCatalogUsage;

	private WebsiteInfo infos;

	public WebsiteConfigEditor(ToolkitWindow handler, WebsiteInfo infos) {
		super(handler);
		SpringLayout spring = new SpringLayout();
		setLayout(spring);
		descriptionHelp = new JLabel("<html>" + "<h3>Website</h3><br>"
				+ "This editor allows you to configure the required information for building the website.<br>"
				+ "These settings tell how will the agent, the website and the backend be configured." + "</html>");
		secondaryHelp = new JLabel(
				"<html>" + "*Mandatory.<br>" + "**Default values will be used if left empty." + "</html>");

		JLabel bckendUrlText = new JLabel("<html>Backend URL* <u>A trailing slash is required!</u>:</html>");
		JLabel agentDlText = new JLabel("Agent binaries download URL**:");
		JLabel packageDlText = new JLabel("Package binaries download URL**:");
		JLabel packageVersText = new JLabel("Package version file download URL**:");

		allowCatalogUsage = new JCheckBox("Allow catalog usage and generation");
		allowCatalogUsage.setToolTipText(
				"Whether there should be support for catalog which allows downloading update packages manually.");

		this.infos = infos;

		backendURL = new JTextField();
		backendURL.setToolTipText("The address of the backend that the agent will use.");
		agentDlURL = new JTextField();
		agentDlURL.setToolTipText("The address of the agent for the website to make the end user download it.");
		packageDlURL = new JTextField();
		packageDlURL.setToolTipText("The address of update package that the agent will use for updates.");
		packageVersDlURL = new JTextField();
		packageVersDlURL.setToolTipText("The address of update package versions file that the agent will use.");

		if (!"true".equals(
				ToolkitMain.getPersistentStorage().getSettings().getProperty("editor.settings.advanced.show"))) {
			agentDlText.setVisible(false);
			packageDlText.setVisible(false);
			packageVersText.setVisible(false);
			agentDlURL.setVisible(false);
			packageDlURL.setVisible(false);
			packageVersDlURL.setVisible(false);
		}

		backendURL.setText(infos.getBackendUrl());
		agentDlURL.setText(infos.getAgentDlUrl());
		packageDlURL.setText(infos.getPackageDlUrl());
		packageVersDlURL.setText(infos.getPackageVersDlUrl());
		allowCatalogUsage.setSelected(infos.allowsCatalogUsage());

		backendURL.getDocument().addDocumentListener(this);
		agentDlURL.getDocument().addDocumentListener(this);
		packageDlURL.getDocument().addDocumentListener(this);
		packageVersDlURL.getDocument().addDocumentListener(this);
		allowCatalogUsage.addItemListener(this);

		spring.putConstraint(SpringLayout.NORTH, descriptionHelp, 10, SpringLayout.NORTH, this);
		spring.putConstraint(SpringLayout.SOUTH, descriptionHelp, 10 + 120, SpringLayout.NORTH, this);
		spring.putConstraint(SpringLayout.EAST, descriptionHelp, -10, SpringLayout.EAST, this);
		spring.putConstraint(SpringLayout.WEST, descriptionHelp, 10, SpringLayout.WEST, this);

		spring.putConstraint(SpringLayout.NORTH, bckendUrlText, 10, SpringLayout.SOUTH, descriptionHelp);
		spring.putConstraint(SpringLayout.SOUTH, bckendUrlText, 10 + 20, SpringLayout.SOUTH, descriptionHelp);
		spring.putConstraint(SpringLayout.WEST, bckendUrlText, 10, SpringLayout.WEST, this);
		spring.putConstraint(SpringLayout.EAST, bckendUrlText, -10, SpringLayout.EAST, this);

		spring.putConstraint(SpringLayout.NORTH, backendURL, 10, SpringLayout.SOUTH, bckendUrlText);
		spring.putConstraint(SpringLayout.SOUTH, backendURL, 10 + 20, SpringLayout.SOUTH, bckendUrlText);
		spring.putConstraint(SpringLayout.WEST, backendURL, 10, SpringLayout.WEST, this);
		spring.putConstraint(SpringLayout.EAST, backendURL, -10, SpringLayout.EAST, this);

		spring.putConstraint(SpringLayout.NORTH, allowCatalogUsage, 10, SpringLayout.SOUTH, backendURL);
		spring.putConstraint(SpringLayout.SOUTH, allowCatalogUsage, 10 + 60, SpringLayout.SOUTH, backendURL);
		spring.putConstraint(SpringLayout.WEST, allowCatalogUsage, 10, SpringLayout.WEST, this);
		spring.putConstraint(SpringLayout.EAST, allowCatalogUsage, -10, SpringLayout.EAST, this);

		spring.putConstraint(SpringLayout.NORTH, agentDlText, 10, SpringLayout.SOUTH, allowCatalogUsage);
		spring.putConstraint(SpringLayout.SOUTH, agentDlText, 10 + 20, SpringLayout.SOUTH, allowCatalogUsage);
		spring.putConstraint(SpringLayout.WEST, agentDlText, 10, SpringLayout.WEST, this);
		spring.putConstraint(SpringLayout.EAST, agentDlText, -10, SpringLayout.EAST, this);

		spring.putConstraint(SpringLayout.NORTH, agentDlURL, 10, SpringLayout.SOUTH, agentDlText);
		spring.putConstraint(SpringLayout.SOUTH, agentDlURL, 10 + 20, SpringLayout.SOUTH, agentDlText);
		spring.putConstraint(SpringLayout.WEST, agentDlURL, 10, SpringLayout.WEST, this);
		spring.putConstraint(SpringLayout.EAST, agentDlURL, -10, SpringLayout.EAST, this);

		spring.putConstraint(SpringLayout.NORTH, packageDlText, 10, SpringLayout.SOUTH, agentDlURL);
		spring.putConstraint(SpringLayout.SOUTH, packageDlText, 10 + 20, SpringLayout.SOUTH, agentDlURL);
		spring.putConstraint(SpringLayout.WEST, packageDlText, 10, SpringLayout.WEST, this);
		spring.putConstraint(SpringLayout.EAST, packageDlText, -10, SpringLayout.EAST, this);

		spring.putConstraint(SpringLayout.NORTH, packageDlURL, 10, SpringLayout.SOUTH, packageDlText);
		spring.putConstraint(SpringLayout.SOUTH, packageDlURL, 10 + 20, SpringLayout.SOUTH, packageDlText);
		spring.putConstraint(SpringLayout.WEST, packageDlURL, 10, SpringLayout.WEST, this);
		spring.putConstraint(SpringLayout.EAST, packageDlURL, -10, SpringLayout.EAST, this);

		spring.putConstraint(SpringLayout.NORTH, packageVersText, 10, SpringLayout.SOUTH, packageDlURL);
		spring.putConstraint(SpringLayout.SOUTH, packageVersText, 10 + 20, SpringLayout.SOUTH, packageDlURL);
		spring.putConstraint(SpringLayout.WEST, packageVersText, 10, SpringLayout.WEST, this);
		spring.putConstraint(SpringLayout.EAST, packageVersText, -10, SpringLayout.EAST, this);

		spring.putConstraint(SpringLayout.NORTH, packageVersDlURL, 10, SpringLayout.SOUTH, packageVersText);
		spring.putConstraint(SpringLayout.SOUTH, packageVersDlURL, 10 + 20, SpringLayout.SOUTH, packageVersText);
		spring.putConstraint(SpringLayout.WEST, packageVersDlURL, 10, SpringLayout.WEST, this);
		spring.putConstraint(SpringLayout.EAST, packageVersDlURL, -10, SpringLayout.EAST, this);

		spring.putConstraint(SpringLayout.NORTH, secondaryHelp, 10, SpringLayout.SOUTH, packageVersDlURL);
		spring.putConstraint(SpringLayout.SOUTH, secondaryHelp, 10 + 60, SpringLayout.SOUTH, packageVersDlURL);
		spring.putConstraint(SpringLayout.WEST, secondaryHelp, 10, SpringLayout.WEST, this);
		spring.putConstraint(SpringLayout.EAST, secondaryHelp, -10, SpringLayout.EAST, this);

		setPreferredSize(new Dimension(550, 550));

		add(descriptionHelp);
		add(bckendUrlText);
		add(agentDlText);
		add(packageDlText);
		add(packageVersText);
		add(backendURL);
		add(agentDlURL);
		add(packageDlURL);
		add(packageVersDlURL);
		add(secondaryHelp);
		add(allowCatalogUsage);

	}

	public void updateInfosTextChanged(DocumentEvent e) {
		if (e.getDocument() == backendURL.getDocument()) {
			infos.setBackendUrl(backendURL.getText());
		}
		if (e.getDocument() == agentDlURL.getDocument()) {
			infos.setAgentDlUrl(agentDlURL.getText());
		}
		if (e.getDocument() == packageDlURL.getDocument()) {
			infos.setPackageDlUrl(packageDlURL.getText());
		}
		if (e.getDocument() == packageVersDlURL.getDocument()) {
			infos.setPackageVersDlUrl(packageVersDlURL.getText());
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
		if (e.getSource() == allowCatalogUsage) {
			infos.setAllowCatalogUsage(allowCatalogUsage.isSelected());
		}
		markAsModified();

	}

}
