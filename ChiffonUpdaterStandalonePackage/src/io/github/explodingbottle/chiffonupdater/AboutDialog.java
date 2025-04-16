/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;

public class AboutDialog extends JDialog implements ActionListener {

	private static final long serialVersionUID = 2790864815134158902L;

	private JButton close;

	public AboutDialog(JFrame parentFrame, Translator translator) {
		super(parentFrame, translator.getTranslation("wizard.about"), Dialog.ModalityType.APPLICATION_MODAL);

		Dimension curDim = new Dimension(400, 300);
		setMinimumSize(curDim);
		setSize(curDim);

		setLocationRelativeTo(parentFrame);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		SpringLayout layout = new SpringLayout();
		setLayout(layout);

		ImageIcon wizardIcon = PackageMain.getCustomizationEngine().getIconToDisplay();

		JLabel programAbout = new JLabel(translator.getTranslation("wizard.about.text",
				PackageMain.getCustomizationEngine().getTitleToDisplay(), ChiffonUpdaterVersion.getGlobalVersion()));

		JLabel wizardIconLabel = null;
		if (wizardIcon != null) {
			ImageIcon rescaled = new ImageIcon(wizardIcon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH));
			wizardIconLabel = new JLabel(rescaled);
		} else {
			wizardIconLabel = new JLabel();
		}

		Container contentPane = getContentPane();

		close = new JButton(translator.getTranslation("wizard.about.close"));
		close.addActionListener(this);

		JTextArea licenseArea = new JTextArea(PackageMain.getLicenseText());
		licenseArea.setEditable(false);
		licenseArea.setWrapStyleWord(true);
		licenseArea.setLineWrap(true);

		JScrollPane licenseScroller = new JScrollPane(licenseArea);

		layout.putConstraint(SpringLayout.WEST, wizardIconLabel, 10, SpringLayout.WEST, contentPane);
		layout.putConstraint(SpringLayout.EAST, wizardIconLabel, 10 + 64, SpringLayout.WEST, contentPane);
		layout.putConstraint(SpringLayout.NORTH, wizardIconLabel, 10, SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.SOUTH, wizardIconLabel, 10 + 64, SpringLayout.NORTH, contentPane);

		layout.putConstraint(SpringLayout.WEST, programAbout, 10, SpringLayout.EAST, wizardIconLabel);
		layout.putConstraint(SpringLayout.EAST, programAbout, -10, SpringLayout.EAST, contentPane);
		layout.putConstraint(SpringLayout.NORTH, programAbout, 0, SpringLayout.NORTH, wizardIconLabel);
		layout.putConstraint(SpringLayout.SOUTH, programAbout, 20, SpringLayout.SOUTH, wizardIconLabel);

		layout.putConstraint(SpringLayout.WEST, licenseScroller, 10, SpringLayout.WEST, contentPane);
		layout.putConstraint(SpringLayout.EAST, licenseScroller, -10, SpringLayout.EAST, contentPane);
		layout.putConstraint(SpringLayout.NORTH, licenseScroller, 20, SpringLayout.SOUTH, programAbout);
		layout.putConstraint(SpringLayout.SOUTH, licenseScroller, -50, SpringLayout.SOUTH, contentPane);

		layout.putConstraint(SpringLayout.WEST, close, -10 - 100, SpringLayout.EAST, contentPane);
		layout.putConstraint(SpringLayout.EAST, close, -10, SpringLayout.EAST, contentPane);
		layout.putConstraint(SpringLayout.NORTH, close, 10, SpringLayout.SOUTH, licenseScroller);
		layout.putConstraint(SpringLayout.SOUTH, close, -10, SpringLayout.SOUTH, contentPane);

		add(wizardIconLabel);
		add(programAbout);
		add(licenseScroller);
		add(close);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == close) {
			setVisible(false);
			dispose();
		}
	}

}
