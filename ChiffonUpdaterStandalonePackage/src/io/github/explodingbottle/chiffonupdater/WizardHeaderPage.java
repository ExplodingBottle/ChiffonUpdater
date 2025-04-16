/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.awt.Color;
import java.awt.Image;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class WizardHeaderPage extends WizardPage {

	private static final long serialVersionUID = -1574954039567651016L;

	private JLabel upperText;
	private JPanel headerPanel;

	public void updateUpperText(String text) {
		upperText.setText("<html><b>" + text + "</b></html>");
	}

	public JPanel returnHeaderPanel() {
		return headerPanel;
	}

	public WizardHeaderPage() {
		SpringLayout layout = getSpringLayout();
		headerPanel = new JPanel();
		SpringLayout hPanelLayout = new SpringLayout();
		headerPanel.setLayout(hPanelLayout);

		headerPanel.setBackground(Color.WHITE);
		headerPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

		layout.putConstraint(SpringLayout.WEST, headerPanel, 0, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.EAST, headerPanel, 0, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.NORTH, headerPanel, 0, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.SOUTH, headerPanel, 100, SpringLayout.NORTH, this);

		ImageIcon imgWizardIconImage = PackageMain.getCustomizationEngine().getIconToDisplay();

		JLabel icon = null;
		if (imgWizardIconImage != null) {
			ImageIcon rescaled = new ImageIcon(
					imgWizardIconImage.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH));
			icon = new JLabel(rescaled);
		} else {
			icon = new JLabel();
		}

		upperText = new JLabel();

		hPanelLayout.putConstraint(SpringLayout.WEST, icon, 30, SpringLayout.WEST, headerPanel);
		hPanelLayout.putConstraint(SpringLayout.EAST, icon, 30 + 80, SpringLayout.WEST, headerPanel);
		hPanelLayout.putConstraint(SpringLayout.NORTH, icon, 10, SpringLayout.NORTH, headerPanel);
		hPanelLayout.putConstraint(SpringLayout.SOUTH, icon, -10, SpringLayout.SOUTH, headerPanel);

		hPanelLayout.putConstraint(SpringLayout.WEST, upperText, 30, SpringLayout.EAST, icon);
		hPanelLayout.putConstraint(SpringLayout.EAST, upperText, -30, SpringLayout.EAST, headerPanel);
		hPanelLayout.putConstraint(SpringLayout.NORTH, upperText, 10, SpringLayout.NORTH, headerPanel);
		hPanelLayout.putConstraint(SpringLayout.SOUTH, upperText, -10, SpringLayout.SOUTH, headerPanel);

		headerPanel.add(upperText);
		headerPanel.add(icon);

		add(headerPanel);

	}

}
