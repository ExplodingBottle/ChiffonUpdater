/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SpringLayout;

public class SourceConnectionDialog extends JDialog implements ActionListener {

	private static final long serialVersionUID = -5316759556815575483L;

	private JButton closeDialog;
	private JButton connect;

	private boolean mustAutoGenerate;

	private ToolkitWindow owner;

	public SourceConnectionDialog(ToolkitWindow owner) {
		super(owner, "Source connection dialog", Dialog.ModalityType.APPLICATION_MODAL);
		Dimension size = owner.getSize();

		Dimension sizeHalf = new Dimension(Math.round((float) (size.getWidth() / 2.0)),
				Math.round((float) (size.getHeight() / 2.0)));
		setSize(sizeHalf);
		setLocationRelativeTo(owner);

		SpringLayout layout = new SpringLayout();
		setLayout(layout);

		this.owner = owner;

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		closeDialog = new JButton("Close");
		connect = new JButton("Connect");
		closeDialog.addActionListener(this);
		connect.addActionListener(this);

		JTextArea area = new JTextArea();

		area.setText("This dialog allows you to connect to a source folder.\r\n"
				+ "The source folder is a folder with a specific structure: \r\n"
				+ "\t[source folder]/Product Name/Version/Feature.\r\n"
				+ "After connecting to the source folder, each time you should have been prompted\r\n"
				+ "for a directory, the source folder will first be checked. If everything has been\r\n"
				+ "set up properly, you should never have again a popup asking you for where to find files.");

		mustAutoGenerate = !"true"
				.equalsIgnoreCase(ToolkitMain.getPersistentStorage().getSettings().getProperty("source.create.manual"));
		if (mustAutoGenerate) {
			area.append("\r\n\r\n"
					+ "The folder structure of the source folder is generated automatically because the setting is enabled.\r\n"
					+ "You can disable it in the settings.");
		}

		area.setEditable(false);

		JScrollPane areaPane = new JScrollPane(area);
		Container pane = getContentPane();

		layout.putConstraint(SpringLayout.NORTH, areaPane, 10, SpringLayout.NORTH, pane);
		layout.putConstraint(SpringLayout.SOUTH, areaPane, -40, SpringLayout.SOUTH, pane);
		layout.putConstraint(SpringLayout.EAST, areaPane, -10, SpringLayout.EAST, pane);
		layout.putConstraint(SpringLayout.WEST, areaPane, 10, SpringLayout.WEST, pane);

		layout.putConstraint(SpringLayout.NORTH, connect, 5, SpringLayout.SOUTH, areaPane);
		layout.putConstraint(SpringLayout.SOUTH, connect, -5, SpringLayout.SOUTH, pane);
		layout.putConstraint(SpringLayout.EAST, connect, -10, SpringLayout.EAST, pane);
		layout.putConstraint(SpringLayout.WEST, connect, -10 - 100, SpringLayout.EAST, pane);

		layout.putConstraint(SpringLayout.NORTH, closeDialog, 0, SpringLayout.NORTH, connect);
		layout.putConstraint(SpringLayout.SOUTH, closeDialog, 0, SpringLayout.SOUTH, connect);
		layout.putConstraint(SpringLayout.EAST, closeDialog, -10, SpringLayout.WEST, connect);
		layout.putConstraint(SpringLayout.WEST, closeDialog, -10 - 100, SpringLayout.WEST, connect);

		add(areaPane);
		add(connect);
		add(closeDialog);

		getRootPane().registerKeyboardAction(e -> {
			setVisible(false);
			dispose();
		}, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == closeDialog) {
			setVisible(false);
			dispose();
		}
		if (e.getSource() == connect) {
			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			String path = ToolkitMain.getPersistentStorage().getSettings().getProperty("source.path");
			if (path != null) {
				chooser.setSelectedFile(new File(path));
			}
			int returnVal = chooser.showDialog(owner, "Select source folder");
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				owner.updateSource(chooser.getSelectedFile());
				if (mustAutoGenerate) {
					owner.generateSourceFolderStructure();
				}
				setVisible(false);
				dispose();
			}
		}
	}

}
