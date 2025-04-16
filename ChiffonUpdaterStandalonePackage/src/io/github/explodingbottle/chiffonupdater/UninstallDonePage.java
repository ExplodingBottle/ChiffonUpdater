/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import javax.swing.JLabel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

public class UninstallDonePage extends WizardPage {

	private static final long serialVersionUID = 6634789919648970293L;

	private JLabel messageLabel;

	public UninstallDonePage() {

		SpringLayout layout = getSpringLayout();

		messageLabel = new JLabel();
		messageLabel.setVerticalAlignment(SwingConstants.TOP);

		layout.putConstraint(SpringLayout.NORTH, messageLabel, 20, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.SOUTH, messageLabel, -20, SpringLayout.SOUTH, this);
		layout.putConstraint(SpringLayout.EAST, messageLabel, -20, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.WEST, messageLabel, 20, SpringLayout.WEST, this);

		add(messageLabel);
	}

	public void pageShown() {
		messageLabel.setText(PackageMain.getTranslator().getTranslation("uninstall.done.message"));
	}

}
