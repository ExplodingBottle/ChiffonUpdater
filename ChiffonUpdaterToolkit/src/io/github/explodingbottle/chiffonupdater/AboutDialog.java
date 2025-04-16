/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SpringLayout;

public class AboutDialog extends JDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4315295139048368100L;

	private JButton leave;
	private JButton showLicenses;

	private ToolkitWindow owner;

	public AboutDialog(ToolkitWindow owner) {
		super(owner, "About", Dialog.ModalityType.APPLICATION_MODAL);
		Dimension size = new Dimension(450, 250);

		setSize(size);
		setLocationRelativeTo(owner);

		JLabel programLabel = new JLabel(
				new ImageIcon(owner.getWindowIcon().getScaledInstance(64, 64, Image.SCALE_SMOOTH)));

		JLabel programAboutText = new JLabel("<html><u>Chiffon Updater Toolkit</u><br>"
				+ "This is the graphical interface which will allow you to perform many tasks easily.</html>");

		JLabel detailedText = new JLabel("<html>Global ChiffonUpdater version: "
				+ ChiffonUpdaterVersion.getGlobalVersion() + "<br>This program is released under MIT License<html>");

		this.owner = owner;

		SpringLayout layout = new SpringLayout();
		setLayout(layout);

		leave = new JButton("OK");
		leave.addActionListener(this);
		leave.setMnemonic(KeyEvent.VK_O);

		JPanel leftButtonsPanel = new JPanel();
		leftButtonsPanel.setLayout(new BorderLayout());

		showLicenses = new JButton("Show License");
		showLicenses.addActionListener(this);
		showLicenses.setMnemonic(KeyEvent.VK_S);

		Container pane = getContentPane();

		layout.putConstraint(SpringLayout.NORTH, programLabel, 10, SpringLayout.NORTH, pane);
		layout.putConstraint(SpringLayout.WEST, programLabel, 10, SpringLayout.WEST, pane);
		layout.putConstraint(SpringLayout.EAST, programLabel, 10 + 64, SpringLayout.WEST, pane);
		layout.putConstraint(SpringLayout.SOUTH, programLabel, 10 + 64, SpringLayout.NORTH, pane);

		layout.putConstraint(SpringLayout.NORTH, programAboutText, 0, SpringLayout.NORTH, programLabel);
		layout.putConstraint(SpringLayout.WEST, programAboutText, 10, SpringLayout.EAST, programLabel);
		layout.putConstraint(SpringLayout.EAST, programAboutText, -50, SpringLayout.EAST, pane);
		layout.putConstraint(SpringLayout.SOUTH, programAboutText, 0, SpringLayout.SOUTH, programLabel);

		layout.putConstraint(SpringLayout.NORTH, detailedText, 10, SpringLayout.SOUTH, programLabel);
		layout.putConstraint(SpringLayout.WEST, detailedText, 10, SpringLayout.WEST, pane);
		layout.putConstraint(SpringLayout.EAST, detailedText, -50, SpringLayout.EAST, pane);
		layout.putConstraint(SpringLayout.SOUTH, detailedText, 10 + 64, SpringLayout.SOUTH, programLabel);

		layout.putConstraint(SpringLayout.NORTH, leave, -10 - 25, SpringLayout.SOUTH, pane);
		layout.putConstraint(SpringLayout.WEST, leave, -10 - 100, SpringLayout.EAST, pane);
		layout.putConstraint(SpringLayout.EAST, leave, -10, SpringLayout.EAST, pane);
		layout.putConstraint(SpringLayout.SOUTH, leave, -10, SpringLayout.SOUTH, pane);

		layout.putConstraint(SpringLayout.NORTH, showLicenses, -10 - 25, SpringLayout.SOUTH, pane);
		layout.putConstraint(SpringLayout.WEST, showLicenses, 10, SpringLayout.WEST, pane);
		layout.putConstraint(SpringLayout.EAST, showLicenses, 10 + 150, SpringLayout.WEST, pane);
		layout.putConstraint(SpringLayout.SOUTH, showLicenses, -10, SpringLayout.SOUTH, pane);

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setResizable(false);

		add(programLabel);
		add(programAboutText);
		add(leave);
		add(detailedText);
		add(leftButtonsPanel);
		add(showLicenses);

		getRootPane().registerKeyboardAction(e -> {
			setVisible(false);
			dispose();
		}, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == leave) {
			setVisible(false);
			dispose();
		}
		if (e.getSource() == showLicenses) {
			new LicensesDialog(this, owner).setVisible(true);
		}
	}

}
