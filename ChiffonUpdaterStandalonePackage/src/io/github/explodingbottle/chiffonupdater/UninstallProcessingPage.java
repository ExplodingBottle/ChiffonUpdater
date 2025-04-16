/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.awt.Image;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;

public class UninstallProcessingPage extends WizardPage implements UninstallInterface {

	private static final long serialVersionUID = 7316639510794830041L;

	private JLabel confCheck;
	// private JLabel preqCheck;
	private JLabel execAction;
	private JLabel postAction;

	private ImageIcon arrowIconImage;
	private ImageIcon checkIconImage;

	private SharedLogger logger;

	private UninstallVersionSelectorPage selecPage;

	private ProductRollbackInformations rollbackInfos;
	private File productRoot;

	private Translator translator;

	public UninstallProcessingPage(UninstallVersionSelectorPage selecPage, SharedLogger logger,
			ProductRollbackInformations rollbackInfos, File productRoot) {

		SpringLayout layout = getSpringLayout();
		translator = PackageMain.getTranslator();

		this.selecPage = selecPage;
		this.logger = logger;
		this.rollbackInfos = rollbackInfos;
		this.productRoot = productRoot;

		confCheck = new JLabel();
		execAction = new JLabel();
		postAction = new JLabel();

		JLabel ccText = new JLabel(translator.getTranslation("downgrade.inspecting"));
		JLabel eaText = new JLabel(translator.getTranslation("downgrade.downgrading"));
		JLabel paText = new JLabel(translator.getTranslation("downgrade.finishing"));

		JLabel messageLabel = new JLabel(translator.getTranslation("downgrade.currently"));

		layout.putConstraint(SpringLayout.NORTH, messageLabel, 20, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.SOUTH, messageLabel, 20 + 20, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.EAST, messageLabel, -20, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.WEST, messageLabel, 20, SpringLayout.WEST, this);

		layout.putConstraint(SpringLayout.NORTH, confCheck, 20, SpringLayout.SOUTH, messageLabel);
		layout.putConstraint(SpringLayout.SOUTH, confCheck, 20 + 20, SpringLayout.SOUTH, messageLabel);
		layout.putConstraint(SpringLayout.EAST, confCheck, 30 + 20, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.WEST, confCheck, 30, SpringLayout.WEST, this);

		layout.putConstraint(SpringLayout.NORTH, execAction, 20, SpringLayout.SOUTH, confCheck);
		layout.putConstraint(SpringLayout.SOUTH, execAction, 20 + 20, SpringLayout.SOUTH, confCheck);
		layout.putConstraint(SpringLayout.EAST, execAction, 30 + 20, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.WEST, execAction, 30, SpringLayout.WEST, this);

		layout.putConstraint(SpringLayout.NORTH, postAction, 20, SpringLayout.SOUTH, execAction);
		layout.putConstraint(SpringLayout.SOUTH, postAction, 20 + 20, SpringLayout.SOUTH, execAction);
		layout.putConstraint(SpringLayout.EAST, postAction, 30 + 20, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.WEST, postAction, 30, SpringLayout.WEST, this);

		/* Linked Texts */
		layout.putConstraint(SpringLayout.NORTH, ccText, 0, SpringLayout.NORTH, confCheck);
		layout.putConstraint(SpringLayout.SOUTH, ccText, 0, SpringLayout.SOUTH, confCheck);
		layout.putConstraint(SpringLayout.EAST, ccText, -20, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.WEST, ccText, 30, SpringLayout.EAST, confCheck);

		// layout.putConstraint(SpringLayout.NORTH, cpText, 0, SpringLayout.NORTH,
		// preqCheck);
		// layout.putConstraint(SpringLayout.SOUTH, cpText, 0, SpringLayout.SOUTH,
		// preqCheck);
		// layout.putConstraint(SpringLayout.EAST, cpText, -20, SpringLayout.EAST,
		// this);
		// layout.putConstraint(SpringLayout.WEST, cpText, 30, SpringLayout.EAST,
		// preqCheck);

		layout.putConstraint(SpringLayout.NORTH, eaText, 0, SpringLayout.NORTH, execAction);
		layout.putConstraint(SpringLayout.SOUTH, eaText, 0, SpringLayout.SOUTH, execAction);
		layout.putConstraint(SpringLayout.EAST, eaText, -20, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.WEST, eaText, 30, SpringLayout.EAST, execAction);

		layout.putConstraint(SpringLayout.NORTH, paText, 0, SpringLayout.NORTH, postAction);
		layout.putConstraint(SpringLayout.SOUTH, paText, 0, SpringLayout.SOUTH, postAction);
		layout.putConstraint(SpringLayout.EAST, paText, -20, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.WEST, paText, 30, SpringLayout.EAST, postAction);

		add(messageLabel);
		add(confCheck);
		// add(preqCheck);
		add(execAction);
		add(postAction);
		add(ccText);
		// add(cpText);
		add(eaText);
		add(paText);

		ImageIcon arrowIconImageUnscaled = PackageMain.getImageResourcesLoader().getLoadedResource("arrow.png");
		ImageIcon checkIconImageUnscaled = PackageMain.getImageResourcesLoader().getLoadedResource("check.png");

		arrowIconImage = new ImageIcon(arrowIconImageUnscaled.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
		checkIconImage = new ImageIcon(checkIconImageUnscaled.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));

	}

	public void pageShown() {
		setAllowPageMovements(false);

		UninstallProcessor proc = new UninstallProcessor(this, selecPage.getSelectedVersion(), logger,
				rollbackInfos.getOriginalVersionsChain(), productRoot);
		proc.start();
	}

	@Override
	public void onNewUninstallState(UninstallState newState) {
		SwingUtilities.invokeLater(() -> {
			if (newState == UninstallState.INSPECTING_CONFIGURATION) {
				setAllowCancel(true);
				confCheck.setIcon(arrowIconImage);
				execAction.setIcon(null);
				postAction.setIcon(null);
			} else if (newState == UninstallState.DOWNGRADING) {
				setAllowCancel(false);
				confCheck.setIcon(checkIconImage);
				execAction.setIcon(arrowIconImage);
				postAction.setIcon(null);
			} else if (newState == UninstallState.POSTDOWNGRADE) {
				setAllowCancel(false);
				confCheck.setIcon(checkIconImage);
				execAction.setIcon(checkIconImage);
				postAction.setIcon(arrowIconImage);
			} else {
				getParentingWindow().showEndWindow(new UninstallDonePage());
			}
		});

	}

	@Override
	public void onUnrecoverableError(String error) {
		JOptionPane.showMessageDialog(getParentingWindow(), translator.getTranslation("downgrade.unrecoverable", error),
				translator.getTranslation("downgrade.unrecoverable.title"), JOptionPane.ERROR_MESSAGE);
		getParentingWindow().setVisible(false);
		getParentingWindow().dispose();
	}

	@Override
	public boolean onRecoverableError(String error) {
		return JOptionPane.showConfirmDialog(getParentingWindow(),
				translator.getTranslation("downgrade.recoverable", error),
				translator.getTranslation("downgrade.recoverable.title"), JOptionPane.YES_NO_OPTION,
				JOptionPane.ERROR_MESSAGE) == 0;
	}

	@Override
	public void onUninstallInterrupted() {
		getParentingWindow().setVisible(false);
		getParentingWindow().dispose();
	}

}
