/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SpringLayout;

public class AboutWindow extends JFrame implements ActionListener {

	private static final long serialVersionUID = 2790864815134158902L;

	private JButton close;

	public AboutWindow() {
		Translator translator = AgentMain.getTranslator();

		ImageIcon agentIcon = AgentMain.getAgentIcon();
		String vers = null;
		if (AgentMain.getCurrentSession() != null && AgentMain.getCurrentSession().getCustomizationManager() != null) {
			agentIcon = AgentMain.getCurrentSession().getCustomizationManager().getIconToUse();
			String custoTitle = AgentMain.getCurrentSession().getCustomizationManager().getCustomAgentTitle();
			if (custoTitle != null) {
				vers = translator.getTranslation("cua.about.poweredby", custoTitle);
			}
		}
		if (vers == null) {
			vers = translator.getTranslation("cua.title");
		}

		Dimension curDim = new Dimension(400, 300);
		setMinimumSize(curDim);
		setSize(curDim);
		setTitle(translator.getTranslation("cua.popup.about"));

		setLocationRelativeTo(null);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		getRootPane().registerKeyboardAction(e -> {
			setVisible(false);
			dispose();
			AgentMain.getAgentPresence().clearAboutWindow();
		}, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

		SpringLayout layout = new SpringLayout();
		setLayout(layout);

		if (agentIcon != null)
			setIconImage(agentIcon.getImage());

		JLabel programAbout = new JLabel(
				translator.getTranslation("cua.about.text", vers, ChiffonUpdaterVersion.getGlobalVersion()));

		JLabel agentIconLabel = null;
		if (agentIcon != null) {
			ImageIcon rescaled = new ImageIcon(agentIcon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH));
			agentIconLabel = new JLabel(rescaled);
		} else {
			agentIconLabel = new JLabel();
		}

		Container contentPane = getContentPane();

		close = new JButton(translator.getTranslation("cua.about.close"));
		close.addActionListener(this);

		JTextArea licenseArea = new JTextArea(AgentMain.getLicenseText());
		licenseArea.setEditable(false);
		licenseArea.setWrapStyleWord(true);
		licenseArea.setLineWrap(true);

		JScrollPane licenseScroller = new JScrollPane(licenseArea);

		layout.putConstraint(SpringLayout.WEST, agentIconLabel, 10, SpringLayout.WEST, contentPane);
		layout.putConstraint(SpringLayout.EAST, agentIconLabel, 10 + 64, SpringLayout.WEST, contentPane);
		layout.putConstraint(SpringLayout.NORTH, agentIconLabel, 10, SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.SOUTH, agentIconLabel, 10 + 64, SpringLayout.NORTH, contentPane);

		layout.putConstraint(SpringLayout.WEST, programAbout, 10, SpringLayout.EAST, agentIconLabel);
		layout.putConstraint(SpringLayout.EAST, programAbout, -10, SpringLayout.EAST, contentPane);
		layout.putConstraint(SpringLayout.NORTH, programAbout, 0, SpringLayout.NORTH, agentIconLabel);
		layout.putConstraint(SpringLayout.SOUTH, programAbout, 20, SpringLayout.SOUTH, agentIconLabel);

		layout.putConstraint(SpringLayout.WEST, licenseScroller, 10, SpringLayout.WEST, contentPane);
		layout.putConstraint(SpringLayout.EAST, licenseScroller, -10, SpringLayout.EAST, contentPane);
		layout.putConstraint(SpringLayout.NORTH, licenseScroller, 20, SpringLayout.SOUTH, programAbout);
		layout.putConstraint(SpringLayout.SOUTH, licenseScroller, -50, SpringLayout.SOUTH, contentPane);

		layout.putConstraint(SpringLayout.WEST, close, -10 - 100, SpringLayout.EAST, contentPane);
		layout.putConstraint(SpringLayout.EAST, close, -10, SpringLayout.EAST, contentPane);
		layout.putConstraint(SpringLayout.NORTH, close, 10, SpringLayout.SOUTH, licenseScroller);
		layout.putConstraint(SpringLayout.SOUTH, close, -10, SpringLayout.SOUTH, contentPane);

		add(agentIconLabel);
		add(programAbout);
		add(licenseScroller);
		add(close);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == close) {
			setVisible(false);
			dispose();
			AgentMain.getAgentPresence().clearAboutWindow();
		}
	}

}
