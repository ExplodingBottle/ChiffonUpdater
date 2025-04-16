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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SpringLayout;

public class LicensesDialog extends JDialog implements ActionListener {

	private static final long serialVersionUID = 4315295139048368100L;

	private JButton leave;

	public LicensesDialog(JDialog owner, ToolkitWindow tkw) {
		super(owner, "Licenses", Dialog.ModalityType.APPLICATION_MODAL);
		Dimension size = tkw.getSize();

		Dimension sizeHalf = new Dimension(Math.round((float) (size.getWidth() / 2.0)),
				Math.round((float) (size.getHeight() / 2.0)));
		setSize(sizeHalf);
		setMinimumSize(new Dimension(300, 150));
		setLocationRelativeTo(owner);

		SpringLayout layout = new SpringLayout();
		setLayout(layout);

		leave = new JButton("OK");
		leave.addActionListener(this);
		leave.setMnemonic(KeyEvent.VK_O);

		JTextArea textAreaMIT = new JTextArea(ToolkitMain.getMITLicenseString());
		textAreaMIT.setEditable(false);
		textAreaMIT.setLineWrap(true);
		textAreaMIT.setWrapStyleWord(true);

		JScrollPane licenseTextScrollerMIT = new JScrollPane(textAreaMIT);

		JPanel leftButtonsPanel = new JPanel();
		leftButtonsPanel.setLayout(new BorderLayout());

		Container pane = getContentPane();

		layout.putConstraint(SpringLayout.NORTH, leave, -10 - 25, SpringLayout.SOUTH, pane);
		layout.putConstraint(SpringLayout.WEST, leave, -10 - 100, SpringLayout.EAST, pane);
		layout.putConstraint(SpringLayout.EAST, leave, -10, SpringLayout.EAST, pane);
		layout.putConstraint(SpringLayout.SOUTH, leave, -10, SpringLayout.SOUTH, pane);

		layout.putConstraint(SpringLayout.NORTH, licenseTextScrollerMIT, 5, SpringLayout.NORTH, pane);
		layout.putConstraint(SpringLayout.WEST, licenseTextScrollerMIT, 5, SpringLayout.WEST, pane);
		layout.putConstraint(SpringLayout.EAST, licenseTextScrollerMIT, -5, SpringLayout.EAST, pane);
		layout.putConstraint(SpringLayout.SOUTH, licenseTextScrollerMIT, -5, SpringLayout.NORTH, leave);

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		// setResizable(false);

		add(leave);
		add(licenseTextScrollerMIT);

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

	}

}
