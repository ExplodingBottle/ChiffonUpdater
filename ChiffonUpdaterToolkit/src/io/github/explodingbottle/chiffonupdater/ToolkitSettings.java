/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

public class ToolkitSettings extends JDialog implements ActionListener, ItemListener, WindowListener {

	private static final long serialVersionUID = -4591199054878708182L;

	private JButton leave;

	private JButton clearMRU;
	private JButton clearSource;
	private JButton clearBinpack;
	private JButton clearBackend;
	private ToolkitWindow parent;

	private static final Map<String, String> MAP_NAMES_BY_SETTINGS;
	private Map<JCheckBox, String> checkboxes;

	static {
		MAP_NAMES_BY_SETTINGS = new HashMap<String, String>();
		MAP_NAMES_BY_SETTINGS.put("source.create.manual",
				"Prevents the automatic creation of the required folders in the source folder.");
		MAP_NAMES_BY_SETTINGS.put("source.warning.hide",
				"Do not show a warning on startup if the source folder hasn't been connected.");
		MAP_NAMES_BY_SETTINGS.put("editor.settings.advanced.show",
				"Show advanced settings which are rarely used and are mostly for advanced users.");
	}

	public ToolkitSettings(ToolkitWindow parent) {
		super(parent, "Toolkit Settings", ModalityType.APPLICATION_MODAL);

		checkboxes = new HashMap<JCheckBox, String>();

		this.parent = parent;

		addWindowListener(this);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		JPanel settingsPanel = new JPanel();
		BoxLayout bLayout = new BoxLayout(settingsPanel, BoxLayout.PAGE_AXIS);
		settingsPanel.setLayout(bLayout);

		JScrollPane scroller = new JScrollPane(settingsPanel);

		leave = new JButton("OK");
		leave.addActionListener(this);

		clearMRU = new JButton("Clear recent projects list");
		clearMRU.addActionListener(this);
		clearBinpack = new JButton("Clear the last browsed BinPack");
		clearBinpack.addActionListener(this);
		clearSource = new JButton("Clear the last used source folder path");
		clearSource.addActionListener(this);
		clearBackend = new JButton("Clear the last used backend folder path");
		clearBackend.addActionListener(this);

		settingsPanel.add(clearMRU);
		settingsPanel.add(clearBinpack);
		settingsPanel.add(clearSource);
		settingsPanel.add(clearBackend);

		MAP_NAMES_BY_SETTINGS.forEach((setting, name) -> {
			Properties settings = ToolkitMain.getPersistentStorage().getSettings();
			JCheckBox related = new JCheckBox(name);
			related.setSelected(
					settings.getProperty(setting) != null && "true".equalsIgnoreCase(settings.getProperty(setting)));
			related.addItemListener(this);
			checkboxes.put(related, setting);
			settingsPanel.add(related);
		});

		add(BorderLayout.CENTER, scroller);
		add(BorderLayout.SOUTH, leave);

		pack();
		setLocationRelativeTo(parent);
		
		getRootPane().registerKeyboardAction(e -> {
			close();
		}, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
	}

	private void close() {
		ToolkitMain.getPersistentStorage().persistentStorageSave();
		parent.getProjectManager().reopenEditor();
		setVisible(false);
		dispose();
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		String targetSetting = checkboxes.get(e.getSource());
		if (targetSetting != null) {
			JCheckBox checked = (JCheckBox) e.getSource();
			ToolkitMain.getPersistentStorage().getSettings().setProperty(targetSetting, "" + checked.isSelected());
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Properties props = ToolkitMain.getPersistentStorage().getSettings();
		if (e.getSource() == leave) {
			close();
		}
		if (e.getSource() == clearMRU) {
			if (JOptionPane.showConfirmDialog(this,
					"Are you sure you want to clear the list of the most recently used projects? This cannot be undone.",
					"Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == 0) {
				for (int i = 0; i < 5; i++) {
					props.remove("file.mru" + i);
				}
				parent.updateRecentList(null);
			}
		}

		if (e.getSource() == clearBinpack) {
			if (JOptionPane.showConfirmDialog(this,
					"Are you sure you want to clear the path of the last browsed BinPack? This cannot be undone.",
					"Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == 0) {
				props.remove("binpack.lastpath");
			}
		}
		if (e.getSource() == clearBackend) {
			if (JOptionPane.showConfirmDialog(this,
					"Are you sure you want to clear the path of the last used backend folder? This cannot be undone.",
					"Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == 0) {
				props.remove("backend.lastpath");
			}
		}

		if (e.getSource() == clearSource) {
			if (parent.getCurrentConnectedSource() != null) {
				JOptionPane.showMessageDialog(this, "You need to disconnect the source folder first.",
						"Disconnect first", JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (JOptionPane.showConfirmDialog(this,
					"Are you sure you want to clear the path of the last used source folder? This cannot be undone.",
					"Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == 0) {
				props.remove("source.path");
				props.remove("source.connected");
			}
		}
	}

	@Override
	public void windowActivated(WindowEvent e) {

	}

	@Override
	public void windowClosed(WindowEvent e) {

	}

	@Override
	public void windowClosing(WindowEvent e) {
		close();
	}

	@Override
	public void windowDeactivated(WindowEvent e) {

	}

	@Override
	public void windowDeiconified(WindowEvent e) {

	}

	@Override
	public void windowIconified(WindowEvent e) {

	}

	@Override
	public void windowOpened(WindowEvent e) {

	}

}
