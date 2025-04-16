/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.awt.Container;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.KeyStroke;
import javax.swing.SpringLayout;

public class SourceConnectionWarning extends JDialog implements ActionListener {

	private static final long serialVersionUID = 9107706471264696999L;

	private JCheckBox rememberChoice;
	private JButton connectFolder;
	private JButton ignore;

	private ToolkitWindow owner;

	public SourceConnectionWarning(ToolkitWindow owner) {
		super(owner, "Connect a source folder", Dialog.ModalityType.APPLICATION_MODAL);

		setSize(500, 340);
		setMinimumSize(getSize());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setLocationRelativeTo(owner);

		this.owner = owner;

		JLabel firstWarning = new JLabel(
				"<html>A source folder is a very useful folder that you should connect now in order to centralize<br>"
						+ "all the binaries and other files needed to make the update chain work.<br>"
						+ "This is why it is extremely recommended that you choose one now.</html>");
		JLabel secondWarning = new JLabel("<html>The automatic source folder generation setting is enabled.<br>"
				+ "Without a source folder you cannot take advantage of it.<br>"
				+ "If you disconnected your source folder you should reconnect it now to prevent a desynchronization.</html>");

		secondWarning.setVisible(!"true".equalsIgnoreCase(
				ToolkitMain.getPersistentStorage().getSettings().getProperty("source.create.manual")));

		connectFolder = new JButton("Connect the folder now");
		connectFolder.setMnemonic(KeyEvent.VK_C);
		connectFolder.addActionListener(this);
		ignore = new JButton("Ignore");
		ignore.setMnemonic(KeyEvent.VK_I);
		ignore.addActionListener(this);
		rememberChoice = new JCheckBox("Remember my choice");
		rememberChoice.setToolTipText("You can always cancel this in the settings.");

		SpringLayout layout = new SpringLayout();
		setLayout(layout);

		Container pane = getContentPane();

		layout.putConstraint(SpringLayout.NORTH, firstWarning, 10, SpringLayout.NORTH, pane);
		layout.putConstraint(SpringLayout.EAST, firstWarning, -10, SpringLayout.EAST, pane);
		layout.putConstraint(SpringLayout.WEST, firstWarning, 10, SpringLayout.WEST, pane);
		layout.putConstraint(SpringLayout.SOUTH, firstWarning, 70, SpringLayout.NORTH, pane);

		layout.putConstraint(SpringLayout.NORTH, secondWarning, 10, SpringLayout.SOUTH, firstWarning);
		layout.putConstraint(SpringLayout.EAST, secondWarning, 0, SpringLayout.EAST, firstWarning);
		layout.putConstraint(SpringLayout.WEST, secondWarning, 0, SpringLayout.WEST, firstWarning);
		layout.putConstraint(SpringLayout.SOUTH, secondWarning, 70 + 10, SpringLayout.SOUTH, firstWarning);

		layout.putConstraint(SpringLayout.NORTH, rememberChoice, -40, SpringLayout.SOUTH, pane);
		layout.putConstraint(SpringLayout.EAST, rememberChoice, -10, SpringLayout.EAST, pane);
		layout.putConstraint(SpringLayout.WEST, rememberChoice, 10, SpringLayout.WEST, pane);
		layout.putConstraint(SpringLayout.SOUTH, rememberChoice, -10, SpringLayout.SOUTH, pane);

		layout.putConstraint(SpringLayout.NORTH, ignore, -40, SpringLayout.NORTH, rememberChoice);
		layout.putConstraint(SpringLayout.EAST, ignore, -10, SpringLayout.HORIZONTAL_CENTER, pane);
		layout.putConstraint(SpringLayout.WEST, ignore, 10, SpringLayout.WEST, pane);
		layout.putConstraint(SpringLayout.SOUTH, ignore, -10, SpringLayout.NORTH, rememberChoice);

		layout.putConstraint(SpringLayout.NORTH, connectFolder, -40, SpringLayout.NORTH, rememberChoice);
		layout.putConstraint(SpringLayout.EAST, connectFolder, -10, SpringLayout.EAST, pane);
		layout.putConstraint(SpringLayout.WEST, connectFolder, 10, SpringLayout.HORIZONTAL_CENTER, pane);
		layout.putConstraint(SpringLayout.SOUTH, connectFolder, -10, SpringLayout.NORTH, rememberChoice);

		add(firstWarning);
		add(secondWarning);
		add(connectFolder);
		add(ignore);
		add(rememberChoice);

		getRootPane().registerKeyboardAction(e -> {
			closeAndSave();
		}, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

	}

	public void closeAndSave() {
		if (rememberChoice.isSelected()) {
			PersistentStorage storage = ToolkitMain.getPersistentStorage();
			storage.getSettings().setProperty("source.warning.hide", "true");
			storage.persistentStorageSave();
		}
		setVisible(false);
		dispose();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == ignore) {
			closeAndSave();
		}
		if (e.getSource() == connectFolder) {
			SourceConnectionDialog dialog = new SourceConnectionDialog(owner);
			dialog.setVisible(true);
			closeAndSave();
		}
	}

}
