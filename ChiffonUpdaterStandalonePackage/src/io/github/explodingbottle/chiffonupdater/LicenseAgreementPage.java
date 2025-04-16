/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;

public class LicenseAgreementPage extends WizardHeaderPage implements ItemListener, ActionListener {

	private static final long serialVersionUID = 7371037285934305362L;

	private JRadioButton agreeing;
	private JButton print;
	private Desktop desktop;
	private File eulaFile;
	private SharedLogger logger;
	private Translator translator;

	public LicenseAgreementPage(String eula, File eulaFile) {
		translator = PackageMain.getTranslator();
		logger = PackageMain.returnUpdaterState().getSharedLogger();

		desktop = Desktop.getDesktop();

		this.eulaFile = eulaFile;

		updateUpperText(translator.getTranslation("licagreement.title"));
		SpringLayout layout = getSpringLayout();

		JTextArea area = new JTextArea(eula);
		area.setBackground(getBackground());
		ButtonGroup group = new ButtonGroup();

		JLabel licAgreementText = new JLabel(translator.getTranslation("licagreement.text"));

		JRadioButton declining = new JRadioButton(translator.getTranslation("licagreement.decline"));
		declining.setSelected(true);
		agreeing = new JRadioButton(translator.getTranslation("licagreement.agree"));
		group.add(declining);
		group.add(agreeing);

		print = new JButton(translator.getTranslation("licagreement.print"));
		print.addActionListener(this);
		if (!desktop.isSupported(Action.PRINT)) {
			print.setEnabled(false);
		}

		JScrollPane scroller = new JScrollPane(area);
		area.setEditable(false);
		area.setLineWrap(true);
		area.setWrapStyleWord(true);

		layout.putConstraint(SpringLayout.NORTH, licAgreementText, 10, SpringLayout.SOUTH, returnHeaderPanel());
		layout.putConstraint(SpringLayout.SOUTH, licAgreementText, 10 + 30, SpringLayout.SOUTH, returnHeaderPanel());
		layout.putConstraint(SpringLayout.EAST, licAgreementText, -20, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.WEST, licAgreementText, 20, SpringLayout.WEST, this);

		layout.putConstraint(SpringLayout.NORTH, scroller, 10, SpringLayout.SOUTH, licAgreementText);
		layout.putConstraint(SpringLayout.SOUTH, scroller, -10, SpringLayout.NORTH, agreeing);
		layout.putConstraint(SpringLayout.EAST, scroller, -20, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.WEST, scroller, 20, SpringLayout.WEST, this);

		layout.putConstraint(SpringLayout.SOUTH, declining, -10, SpringLayout.SOUTH, this);
		layout.putConstraint(SpringLayout.NORTH, declining, -10 - 20, SpringLayout.SOUTH, this);
		layout.putConstraint(SpringLayout.EAST, declining, -20, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.WEST, declining, 20, SpringLayout.WEST, this);

		layout.putConstraint(SpringLayout.SOUTH, agreeing, -10, SpringLayout.NORTH, declining);
		layout.putConstraint(SpringLayout.NORTH, agreeing, -10 - 20, SpringLayout.NORTH, declining);
		layout.putConstraint(SpringLayout.EAST, agreeing, 20 + 500, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.WEST, agreeing, 20, SpringLayout.WEST, this);

		layout.putConstraint(SpringLayout.NORTH, print, 0, SpringLayout.NORTH, agreeing);
		layout.putConstraint(SpringLayout.SOUTH, print, 0, SpringLayout.SOUTH, agreeing);
		layout.putConstraint(SpringLayout.WEST, print, 20, SpringLayout.EAST, agreeing);
		layout.putConstraint(SpringLayout.EAST, print, -20, SpringLayout.EAST, this);

		agreeing.addItemListener(this);
		declining.addItemListener(this);

		add(licAgreementText);
		add(agreeing);
		add(declining);
		add(scroller);
		add(print);
	}

	public void pageShown() {
		setAllowNext(agreeing.isSelected());
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() == agreeing) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				setAllowNext(true);
			}
			if (e.getStateChange() == ItemEvent.DESELECTED) {
				setAllowNext(false);
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == print) {
			try {
				desktop.print(eulaFile);
			} catch (IOException ex) {
				logger.log("LICA", LogLevel.WARNING, "Failed to print EULA with error " + ex.getMessage() + ".");
				JOptionPane.showMessageDialog(getParentingWindow(), translator.getTranslation("licagreement.print.err"),
						translator.getTranslation("wizard.error.title"), JOptionPane.WARNING_MESSAGE);
			}
		}
	}

}
