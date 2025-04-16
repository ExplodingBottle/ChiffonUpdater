/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import javax.swing.JLabel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

public class UninstallWelcomePage extends WizardPage {

	private static final long serialVersionUID = 8469378321672800782L;

	public UninstallWelcomePage() {
		SpringLayout layout = getSpringLayout();

		JLabel messageLabel = new JLabel(PackageMain.getTranslator().getTranslation("uninstall.welcome.message"));
		messageLabel.setVerticalAlignment(SwingConstants.TOP);

		layout.putConstraint(SpringLayout.NORTH, messageLabel, 20, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.SOUTH, messageLabel, -20, SpringLayout.SOUTH, this);
		layout.putConstraint(SpringLayout.EAST, messageLabel, -20, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.WEST, messageLabel, 20, SpringLayout.WEST, this);

		add(messageLabel);
	}

}
