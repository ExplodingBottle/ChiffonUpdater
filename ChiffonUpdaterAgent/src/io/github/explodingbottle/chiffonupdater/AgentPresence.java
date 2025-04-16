/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class AgentPresence implements ActionListener {

	private TrayIcon agentIcon;
	private SystemTray sysTray;
	private Translator translator;

	private PopupMenu popupMenu;
	private MenuItem exit;
	private MenuItem about;
	private MenuItem currentConnection;

	private JFrame fallbackJFrame;

	private JButton fallback;

	private AboutWindow aboutWindow;

	public AgentPresence() {
		translator = AgentMain.getTranslator();
		popupMenu = new PopupMenu();
		if (SystemTray.isSupported()) {
			agentIcon = new TrayIcon(AgentMain.getAgentIcon().getImage());
			agentIcon.setImageAutoSize(true);
			sysTray = SystemTray.getSystemTray();
			agentIcon.setPopupMenu(popupMenu);

		}
		exit = new MenuItem(translator.getTranslation("cua.popup.quit"));
		exit.addActionListener(this);
		about = new MenuItem(translator.getTranslation("cua.popup.about"));
		about.addActionListener(this);

		currentConnection = new MenuItem();
		currentConnection.setEnabled(false);
		setCurrentConnection(null);

		popupMenu.add(currentConnection);
		popupMenu.add(about);
		popupMenu.addSeparator();
		popupMenu.add(exit);

		fallback = new JButton(translator.getTranslation("cua.button.fallback"));
		fallback.addActionListener(this);

		fallbackJFrame = new JFrame();
		fallbackJFrame.setSize(300, 100);
		fallbackJFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		fallbackJFrame.add(fallback);

		setTrayIcon(AgentMain.getAgentIcon().getImage());
		updateAgentTitleUsage(translator.getTranslation("cua.title"));
	}

	public void updateAgentTitleUsage(String title) {
		if (fallbackJFrame != null) {
			fallbackJFrame.setTitle(title);
		}
		if (agentIcon != null) {
			agentIcon.setToolTip(title);
		}
	}

	public void permanentlyUnmanifest() {
		if (agentIcon != null) {
			sysTray.remove(agentIcon);
		}
		if (fallbackJFrame != null) {
			fallbackJFrame.setVisible(false);
			fallbackJFrame.dispose();
		}
	}

	public void manifestPresence() {
		if (agentIcon != null) {
			try {
				sysTray.add(agentIcon);
				return;
			} catch (AWTException e) {

			}
		}
		// Falling back to a Window
		fallbackJFrame.setVisible(true);
		fallbackJFrame.add(popupMenu);

	}

	public void setCurrentConnection(String origin) {
		if (origin != null) {
			currentConnection.setLabel(translator.getTranslation("cua.popup.current", origin));
		} else {
			currentConnection.setLabel(translator.getTranslation("cua.popup.current",
					translator.getTranslation("cua.popup.current.none")));
		}
	}

	public void clearAboutWindow() {
		aboutWindow = null;
	}

	public void setTrayIcon(Image img) {
		if (agentIcon != null) {
			if (img != null) {
				agentIcon.setImage(img);
			} else {
				agentIcon.setImage(AgentMain.getAgentIcon().getImage());
			}
		}
		if (fallbackJFrame != null) {
			if (img != null) {
				fallbackJFrame.setIconImage(img);
			} else {
				fallbackJFrame.setIconImage(AgentMain.getAgentIcon().getImage());
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource() == exit) {
			if (AgentMain.getCurrentSession() != null
					&& AgentMain.getCurrentSession().getState() == AgentSessionState.BUSY) {
				JOptionPane.showMessageDialog(null, translator.getTranslation("cua.popup.quit.busy"),
						translator.getTranslation("cua.popup.quit"), JOptionPane.WARNING_MESSAGE);
				return;
			}
			if (JOptionPane.showConfirmDialog(null, translator.getTranslation("cua.popup.quit.conf"),
					translator.getTranslation("cua.popup.quit"), JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE) == 0) {
				AgentMain.getAgentServer().interrupt();
			}
		}
		if (arg0.getSource() == fallback) {
			popupMenu.show(fallback, 0, 0);
		}
		if (arg0.getSource() == about) {
			if (aboutWindow != null) {
				aboutWindow.requestFocus();
			} else {
				aboutWindow = new AboutWindow();
				aboutWindow.setVisible(true);
			}
		}
	}

}
