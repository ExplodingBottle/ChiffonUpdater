/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import io.github.explodingbottle.chiffonupdater.project.CustomizationInfos;

public class CustomizationInfosEditor extends ComponentEditor implements DocumentListener {

	private static final long serialVersionUID = 9180179048792484429L;

	private JLabel descriptionHelp;

	private JTextField customAgentTitle;
	private JTextField customPackageTitle;

	private JTextField customAgentIcon;
	private JTextField customPackageIcon;
	private JTextField customPackageBanner;

	private CustomizationInfos infos;

	public CustomizationInfosEditor(ToolkitWindow handler, CustomizationInfos infos) {
		super(handler);
		SpringLayout spring = new SpringLayout();
		setLayout(spring);
		descriptionHelp = new JLabel("<html>" + "<h3>Customization Information</h3><br>"
				+ "This editor allows you to configure graphical customizations for the components.<br>"
				+ "These settings include (but are not limited) to icons or titles.<br>"
				+ "If left blank, the defaults will be used.<br>"
				+ "Paths can be absolute or relative to the source folder.<br>"
				+ "<u>You shouldn't use relative paths if not using a source folder.</u><br>" + "</html>");

		JLabel custAgentTitle = new JLabel("Custom agent title:");
		JLabel custPackageTitle = new JLabel("Custom package title:");
		JLabel custAgentIcon = new JLabel("Custom agent icon:");
		JLabel custPackageIcon = new JLabel("Custom package icon:");
		JLabel custPackageBanner = new JLabel("Custom package banner:");

		this.infos = infos;

		customAgentTitle = new JTextField();
		customAgentTitle.setToolTipText("The custom agent title that will be displayed.");
		customPackageTitle = new JTextField();
		customPackageTitle.setToolTipText("The custom package title that will be displayed.");

		customAgentIcon = new JTextField();
		customAgentIcon.setToolTipText("The path to a custom icon for the agent which can be absolute or relative.");
		customPackageIcon = new JTextField();
		customPackageIcon
				.setToolTipText("The path to a custom icon for the package which can be absolute or relative.");
		customPackageBanner = new JTextField();
		customPackageBanner
				.setToolTipText("The path to a custom banner for the package which can be absolute or relative.");

		customAgentTitle.setText(infos.getCustomAgentTitle());
		customPackageTitle.setText(infos.getCustomPackageTitle());
		customAgentIcon.setText(infos.getCustomAgentIconPath());
		customPackageIcon.setText(infos.getCustomPackageIconPath());
		customPackageBanner.setText(infos.getCustomPackageBannerPath());

		customAgentTitle.getDocument().addDocumentListener(this);
		customPackageTitle.getDocument().addDocumentListener(this);
		customAgentIcon.getDocument().addDocumentListener(this);
		customPackageIcon.getDocument().addDocumentListener(this);
		customPackageBanner.getDocument().addDocumentListener(this);

		spring.putConstraint(SpringLayout.NORTH, descriptionHelp, 10, SpringLayout.NORTH, this);
		spring.putConstraint(SpringLayout.SOUTH, descriptionHelp, 10 + 140, SpringLayout.NORTH, this);
		spring.putConstraint(SpringLayout.EAST, descriptionHelp, -10, SpringLayout.EAST, this);
		spring.putConstraint(SpringLayout.WEST, descriptionHelp, 10, SpringLayout.WEST, this);

		spring.putConstraint(SpringLayout.NORTH, custAgentTitle, 10, SpringLayout.SOUTH, descriptionHelp);
		spring.putConstraint(SpringLayout.SOUTH, custAgentTitle, 10 + 20, SpringLayout.SOUTH, descriptionHelp);
		spring.putConstraint(SpringLayout.WEST, custAgentTitle, 10, SpringLayout.WEST, this);
		spring.putConstraint(SpringLayout.EAST, custAgentTitle, -10, SpringLayout.EAST, this);

		spring.putConstraint(SpringLayout.NORTH, customAgentTitle, 10, SpringLayout.SOUTH, custAgentTitle);
		spring.putConstraint(SpringLayout.SOUTH, customAgentTitle, 10 + 20, SpringLayout.SOUTH, custAgentTitle);
		spring.putConstraint(SpringLayout.WEST, customAgentTitle, 10, SpringLayout.WEST, this);
		spring.putConstraint(SpringLayout.EAST, customAgentTitle, -10, SpringLayout.EAST, this);

		spring.putConstraint(SpringLayout.NORTH, custPackageTitle, 10, SpringLayout.SOUTH, customAgentTitle);
		spring.putConstraint(SpringLayout.SOUTH, custPackageTitle, 10 + 20, SpringLayout.SOUTH, customAgentTitle);
		spring.putConstraint(SpringLayout.WEST, custPackageTitle, 10, SpringLayout.WEST, this);
		spring.putConstraint(SpringLayout.EAST, custPackageTitle, -10, SpringLayout.EAST, this);

		spring.putConstraint(SpringLayout.NORTH, customPackageTitle, 10, SpringLayout.SOUTH, custPackageTitle);
		spring.putConstraint(SpringLayout.SOUTH, customPackageTitle, 10 + 20, SpringLayout.SOUTH, custPackageTitle);
		spring.putConstraint(SpringLayout.WEST, customPackageTitle, 10, SpringLayout.WEST, this);
		spring.putConstraint(SpringLayout.EAST, customPackageTitle, -10, SpringLayout.EAST, this);

		spring.putConstraint(SpringLayout.NORTH, custAgentIcon, 10, SpringLayout.SOUTH, customPackageTitle);
		spring.putConstraint(SpringLayout.SOUTH, custAgentIcon, 10 + 20, SpringLayout.SOUTH, customPackageTitle);
		spring.putConstraint(SpringLayout.WEST, custAgentIcon, 10, SpringLayout.WEST, this);
		spring.putConstraint(SpringLayout.EAST, custAgentIcon, -10, SpringLayout.EAST, this);

		spring.putConstraint(SpringLayout.NORTH, customAgentIcon, 10, SpringLayout.SOUTH, custAgentIcon);
		spring.putConstraint(SpringLayout.SOUTH, customAgentIcon, 10 + 20, SpringLayout.SOUTH, custAgentIcon);
		spring.putConstraint(SpringLayout.WEST, customAgentIcon, 10, SpringLayout.WEST, this);
		spring.putConstraint(SpringLayout.EAST, customAgentIcon, -10, SpringLayout.EAST, this);

		spring.putConstraint(SpringLayout.NORTH, custPackageIcon, 10, SpringLayout.SOUTH, customAgentIcon);
		spring.putConstraint(SpringLayout.SOUTH, custPackageIcon, 10 + 20, SpringLayout.SOUTH, customAgentIcon);
		spring.putConstraint(SpringLayout.WEST, custPackageIcon, 10, SpringLayout.WEST, this);
		spring.putConstraint(SpringLayout.EAST, custPackageIcon, -10, SpringLayout.EAST, this);

		spring.putConstraint(SpringLayout.NORTH, customPackageIcon, 10, SpringLayout.SOUTH, custPackageIcon);
		spring.putConstraint(SpringLayout.SOUTH, customPackageIcon, 10 + 20, SpringLayout.SOUTH, custPackageIcon);
		spring.putConstraint(SpringLayout.WEST, customPackageIcon, 10, SpringLayout.WEST, this);
		spring.putConstraint(SpringLayout.EAST, customPackageIcon, -10, SpringLayout.EAST, this);

		spring.putConstraint(SpringLayout.NORTH, custPackageBanner, 10, SpringLayout.SOUTH, customPackageIcon);
		spring.putConstraint(SpringLayout.SOUTH, custPackageBanner, 10 + 20, SpringLayout.SOUTH, customPackageIcon);
		spring.putConstraint(SpringLayout.WEST, custPackageBanner, 10, SpringLayout.WEST, this);
		spring.putConstraint(SpringLayout.EAST, custPackageBanner, -10, SpringLayout.EAST, this);

		spring.putConstraint(SpringLayout.NORTH, customPackageBanner, 10, SpringLayout.SOUTH, custPackageBanner);
		spring.putConstraint(SpringLayout.SOUTH, customPackageBanner, 10 + 20, SpringLayout.SOUTH, custPackageBanner);
		spring.putConstraint(SpringLayout.WEST, customPackageBanner, 10, SpringLayout.WEST, this);
		spring.putConstraint(SpringLayout.EAST, customPackageBanner, -10, SpringLayout.EAST, this);

		setPreferredSize(new Dimension(550, 550));

		add(descriptionHelp);
		add(custAgentTitle);
		add(custPackageTitle);
		add(custAgentIcon);
		add(custPackageIcon);
		add(custPackageBanner);
		add(customAgentTitle);
		add(customPackageTitle);
		add(customAgentIcon);
		add(customPackageIcon);
		add(customPackageBanner);

	}

	public void updateInfosTextChanged(DocumentEvent e) {
		if (e.getDocument() == customAgentTitle.getDocument()) {
			infos.setCustomAgentTitle(customAgentTitle.getText());
		}
		if (e.getDocument() == customPackageTitle.getDocument()) {
			infos.setCustomPackageTitle(customPackageTitle.getText());
		}
		if (e.getDocument() == customAgentIcon.getDocument()) {
			infos.setCustomAgentIconPath(customAgentIcon.getText());
		}
		if (e.getDocument() == customPackageIcon.getDocument()) {
			infos.setCustomPackageIconPath(customPackageIcon.getText());
		}
		if (e.getDocument() == customPackageBanner.getDocument()) {
			infos.setCustomPackageBannerPath(customPackageBanner.getText());
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

}
