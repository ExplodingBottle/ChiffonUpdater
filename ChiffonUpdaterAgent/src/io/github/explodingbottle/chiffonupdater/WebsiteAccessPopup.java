/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.awt.Container;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SpringLayout;

public class WebsiteAccessPopup extends JFrame implements ActionListener, WindowListener {

	private static final long serialVersionUID = 6365063978532946729L;

	private JCheckBox remember;
	private JButton authorize;
	private JButton deny;

	private JLabel message;
	private Translator translator;

	private AgentSession nextSession;

	public void handleTask() {
		String nextCookie = null;
		for (String cookie : AgentMain.getPendingSessionsList().keySet()) {
			nextCookie = cookie;
			break;
		}
		nextSession = null;
		if (nextCookie != null) {
			nextSession = AgentMain.getPendingSessionsList().get(nextCookie);
		}
		if (nextCookie == null || nextSession == null || (AgentMain.getCurrentSession() != null
				&& AgentMain.getCurrentSession().getState() == AgentSessionState.BUSY)) {
			dispose();
			AgentMain.clearCurrentWebsiteAccessPopup();
			return;
		}
		message.setText("<html>" + translator.getTranslation("copen.text", nextSession.getOrigin()) + "</html>");
		remember.setSelected(false);
		setVisible(true);

		deny.grabFocus();

	}

	public WebsiteAccessPopup() {
		translator = AgentMain.getTranslator();
		setTitle(translator.getTranslation("cua.title"));

		ImageIcon agentIcon = AgentMain.getAgentIcon();
		if (AgentMain.getCurrentSession() != null && AgentMain.getCurrentSession().getCustomizationManager() != null) {
			agentIcon = AgentMain.getCurrentSession().getCustomizationManager().getIconToUse();
		}

		setSize(500, 300);
		setLocationRelativeTo(null);
		setResizable(false);

		SpringLayout layout = new SpringLayout();
		setLayout(layout);

		remember = new JCheckBox(translator.getTranslation("copen.remember"));
		message = new JLabel();

		JLabel icon;
		if (agentIcon != null) {
			icon = new JLabel(new ImageIcon(agentIcon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH)));
			setIconImage(agentIcon.getImage());
		} else {
			icon = new JLabel();
		}

		authorize = new JButton(translator.getTranslation("copen.auth"));
		deny = new JButton(translator.getTranslation("copen.deny"));

		authorize.addActionListener(this);
		deny.addActionListener(this);

		addWindowListener(this);

		Container ctPane = getContentPane();

		layout.putConstraint(SpringLayout.SOUTH, remember, -10, SpringLayout.SOUTH, ctPane);
		layout.putConstraint(SpringLayout.WEST, remember, 10, SpringLayout.WEST, ctPane);
		layout.putConstraint(SpringLayout.EAST, remember, -10, SpringLayout.EAST, ctPane);
		layout.putConstraint(SpringLayout.NORTH, remember, -40, SpringLayout.SOUTH, ctPane);

		layout.putConstraint(SpringLayout.SOUTH, authorize, -10, SpringLayout.NORTH, remember);
		layout.putConstraint(SpringLayout.WEST, authorize, 10, SpringLayout.WEST, ctPane);
		layout.putConstraint(SpringLayout.EAST, authorize, -5, SpringLayout.HORIZONTAL_CENTER, ctPane);
		layout.putConstraint(SpringLayout.NORTH, authorize, -40, SpringLayout.NORTH, remember);

		layout.putConstraint(SpringLayout.SOUTH, deny, -10, SpringLayout.NORTH, remember);
		layout.putConstraint(SpringLayout.WEST, deny, 5, SpringLayout.HORIZONTAL_CENTER, ctPane);
		layout.putConstraint(SpringLayout.EAST, deny, -10, SpringLayout.EAST, ctPane);
		layout.putConstraint(SpringLayout.NORTH, deny, -40, SpringLayout.NORTH, remember);

		layout.putConstraint(SpringLayout.NORTH, message, 10, SpringLayout.NORTH, ctPane);
		layout.putConstraint(SpringLayout.WEST, message, 10, SpringLayout.WEST, ctPane);
		layout.putConstraint(SpringLayout.EAST, message, -10, SpringLayout.WEST, icon);
		layout.putConstraint(SpringLayout.SOUTH, message, -10, SpringLayout.NORTH, authorize);

		layout.putConstraint(SpringLayout.NORTH, icon, 10, SpringLayout.NORTH, ctPane);
		layout.putConstraint(SpringLayout.WEST, icon, -10 - 64, SpringLayout.EAST, ctPane);
		layout.putConstraint(SpringLayout.EAST, icon, -10, SpringLayout.EAST, ctPane);
		layout.putConstraint(SpringLayout.SOUTH, icon, -10, SpringLayout.NORTH, authorize);

		setAlwaysOnTop(true);

		add(message);
		add(remember);
		add(authorize);
		add(deny);
		add(icon);

	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource() == deny) {
			AgentMain.getPendingSessionsList().remove(nextSession.getOrigin());
			setVisible(false);
			handleTask();
		}
		if (arg0.getSource() == authorize) {
			if (AgentMain.getCurrentSession() == null
					|| AgentMain.getCurrentSession().getState() != AgentSessionState.BUSY) {
				nextSession.setAllowKeyPersistence(remember.isSelected());
				AgentMain.getAgentPresence().setCurrentConnection(nextSession.getOrigin());
				AgentMain.getSharedLogger().log("WEPO", LogLevel.INFO, "Closing log file for session closure.");
				AgentMain.getSharedLogger().closeFileLogging();
				AgentMain.setCurrentSession(nextSession);
				nextSession.setState(AgentSessionState.UNCONFIGURED);
			}

			AgentMain.getPendingSessionsList().clear();

			setVisible(false);
			handleTask();
		}
	}

	@Override
	public void windowActivated(WindowEvent arg0) {

	}

	@Override
	public void windowClosed(WindowEvent arg0) {

	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		AgentMain.getPendingSessionsList().remove(nextSession.getOrigin());
		handleTask();
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {

	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {

	}

	@Override
	public void windowIconified(WindowEvent arg0) {

	}

	@Override
	public void windowOpened(WindowEvent arg0) {
	}

}
