/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

public class WindowWelcomePage extends WizardPage {

	private static final long serialVersionUID = -3011886307659205683L;

	public WindowWelcomePage(String message) {
		setBackground(Color.WHITE);
		ImageIcon imgWizardImage = PackageMain.getCustomizationEngine().getBannerToDisplay();

		JLabel messageLabel = new JLabel(message, SwingConstants.CENTER);
		messageLabel.setVerticalAlignment(SwingConstants.TOP);

		SpringLayout layout = getSpringLayout();

		if (imgWizardImage != null) {
			ImageIcon rescaled = new ImageIcon(
					imgWizardImage.getImage().getScaledInstance(250, 460, Image.SCALE_SMOOTH));
			JLabel frontWizardImage = new JLabel(rescaled);

			frontWizardImage
					.setMaximumSize(new Dimension(imgWizardImage.getIconWidth(), imgWizardImage.getIconHeight()));

			layout.putConstraint(SpringLayout.WEST, messageLabel, 10, SpringLayout.EAST, frontWizardImage);

			add(frontWizardImage);
		}

		layout.putConstraint(SpringLayout.EAST, messageLabel, -10, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.NORTH, messageLabel, 60, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.SOUTH, messageLabel, -10, SpringLayout.SOUTH, this);

		add(messageLabel);
	}

}
