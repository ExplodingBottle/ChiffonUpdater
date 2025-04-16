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

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SpringLayout;

import io.github.explodingbottle.chiffonupdater.project.ProductFilesInfo;
import io.github.explodingbottle.chiffonupdater.project.ProductInfo;
import io.github.explodingbottle.chiffonupdater.project.VersionInfo;

public class NewVersionDialog extends JDialog implements ActionListener {

	private static final long serialVersionUID = -5316759556815575483L;

	private JButton closeDialog;
	private JButton createProject;

	private VersionEditor editor;
	private VersionInfo mainInfos;

	private ToolkitWindow owner;

	private ProductInfo productParent;

	public NewVersionDialog(ToolkitWindow owner, ProductInfo productParent) {
		super(owner, "New version dialog", Dialog.ModalityType.APPLICATION_MODAL);
		Dimension size = owner.getSize();

		Dimension sizeHalf = new Dimension(Math.round((float) (size.getWidth() / 2.0)),
				Math.round((float) (size.getHeight() / 2.0)));
		setSize(sizeHalf);
		setLocationRelativeTo(owner);

		SpringLayout layout = new SpringLayout();
		setLayout(layout);

		this.owner = owner;
		this.productParent = productParent;

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		closeDialog = new JButton("Close");
		createProject = new JButton("Create");
		closeDialog.addActionListener(this);
		createProject.addActionListener(this);

		mainInfos = new VersionInfo(productParent);
		productParent.getFeaturesReference().forEach(feature -> {
			mainInfos.filesForFeatureMap().put(feature, new ProductFilesInfo(mainInfos));
		});
		mainInfos.setReleaseDate(System.currentTimeMillis());
		if (productParent.getLatestVersion() != null) {
			mainInfos.setIterationNumber(productParent.getLatestVersion().getIterationNumber() + 1);
		} else {
			mainInfos.setIterationNumber(0);
		}

		editor = new VersionEditor(null, mainInfos);
		JScrollPane editorPane = editor.returnPane();
		Container pane = getContentPane();

		layout.putConstraint(SpringLayout.NORTH, pane, 0, SpringLayout.NORTH, editorPane);
		layout.putConstraint(SpringLayout.SOUTH, pane, 40, SpringLayout.SOUTH, editorPane);
		layout.putConstraint(SpringLayout.EAST, pane, 0, SpringLayout.EAST, editorPane);
		layout.putConstraint(SpringLayout.WEST, pane, 0, SpringLayout.WEST, editorPane);

		layout.putConstraint(SpringLayout.NORTH, createProject, 5, SpringLayout.SOUTH, editorPane);
		layout.putConstraint(SpringLayout.SOUTH, createProject, -5, SpringLayout.SOUTH, pane);
		layout.putConstraint(SpringLayout.EAST, createProject, -10, SpringLayout.EAST, pane);
		layout.putConstraint(SpringLayout.WEST, createProject, -10 - 100, SpringLayout.EAST, pane);

		layout.putConstraint(SpringLayout.NORTH, closeDialog, 0, SpringLayout.NORTH, createProject);
		layout.putConstraint(SpringLayout.SOUTH, closeDialog, 0, SpringLayout.SOUTH, createProject);
		layout.putConstraint(SpringLayout.EAST, closeDialog, -10, SpringLayout.WEST, createProject);
		layout.putConstraint(SpringLayout.WEST, closeDialog, -10 - 100, SpringLayout.WEST, createProject);

		add(editorPane);
		add(createProject);
		add(closeDialog);

		pack();
		setLocationRelativeTo(owner);
		
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
		if (e.getSource() == createProject) {
			productParent.getVersionsReference().add(mainInfos);
			/*
			 * if (productParent.getLatestVersion() == null) {
			 * productParent.setLatestVersion(mainInfos); }
			 */
			owner.dialogEndSaveStatusDirtied();
			setVisible(false);
			dispose();
		}
	}

}
