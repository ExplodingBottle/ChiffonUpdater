/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

public class UpdateInstallPage extends WizardHeaderPage implements UpdateCancelledCallback, UpdateInterface {

	private static final long serialVersionUID = -8749007677089001398L;

	private JLabel statusText;
	private JLabel detailsText;
	private JProgressBar progressBar;
	private Translator translator;

	private UpdaterThread updThrd;

	public UpdateInstallPage(UpdaterThread updThrd) {
		translator = PackageMain.getTranslator();

		updateUpperText(translator.getTranslation("wizard.installing.title"));
		SpringLayout layout = getSpringLayout();

		JPanel detailsPanel = new JPanel();
		SpringLayout springLayoutDpanel = new SpringLayout();
		detailsPanel.setLayout(springLayoutDpanel);
		TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY),
				translator.getTranslation("wizard.installing.details"));
		detailsPanel.setBorder(border);

		JLabel installingText = new JLabel(translator.getTranslation("wizard.installing.text2"), SwingConstants.CENTER);
		progressBar = new JProgressBar();
		progressBar.setValue(0);
		// progressBar.setUI(new BasicProgressBarUI()); // ?
		detailsText = new JLabel();
		detailsText.setVerticalAlignment(SwingConstants.TOP);
		statusText = new JLabel();

		this.updThrd = updThrd;

		layout.putConstraint(SpringLayout.NORTH, installingText, 10, SpringLayout.SOUTH, returnHeaderPanel());
		layout.putConstraint(SpringLayout.SOUTH, installingText, 10 + 40, SpringLayout.SOUTH, returnHeaderPanel());
		layout.putConstraint(SpringLayout.EAST, installingText, -100, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.WEST, installingText, 100, SpringLayout.WEST, this);

		layout.putConstraint(SpringLayout.NORTH, statusText, 40, SpringLayout.SOUTH, installingText);
		layout.putConstraint(SpringLayout.SOUTH, statusText, 40 + 20, SpringLayout.SOUTH, installingText);
		layout.putConstraint(SpringLayout.EAST, statusText, -150, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.WEST, statusText, 150, SpringLayout.WEST, this);

		layout.putConstraint(SpringLayout.NORTH, progressBar, 20, SpringLayout.SOUTH, statusText);
		layout.putConstraint(SpringLayout.SOUTH, progressBar, 20 + 20, SpringLayout.SOUTH, statusText);
		layout.putConstraint(SpringLayout.EAST, progressBar, -150, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.WEST, progressBar, 150, SpringLayout.WEST, this);

		layout.putConstraint(SpringLayout.NORTH, detailsPanel, 50, SpringLayout.SOUTH, progressBar);
		layout.putConstraint(SpringLayout.SOUTH, detailsPanel, -50, SpringLayout.SOUTH, this);
		layout.putConstraint(SpringLayout.EAST, detailsPanel, -100, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.WEST, detailsPanel, 100, SpringLayout.WEST, this);

		springLayoutDpanel.putConstraint(SpringLayout.NORTH, detailsText, 10, SpringLayout.NORTH, detailsPanel);
		springLayoutDpanel.putConstraint(SpringLayout.SOUTH, detailsText, -10, SpringLayout.SOUTH, detailsPanel);
		springLayoutDpanel.putConstraint(SpringLayout.EAST, detailsText, -20, SpringLayout.EAST, detailsPanel);
		springLayoutDpanel.putConstraint(SpringLayout.WEST, detailsText, 20, SpringLayout.WEST, detailsPanel);

		add(installingText);
		add(progressBar);
		add(statusText);
		add(detailsPanel);

		detailsPanel.add(detailsText);
	}

	public void pageShown() {
		setAllowPageMovements(false);
		PackageMain.returnUpdaterState().setCancelCallback(this);
		UpdateProcessor proc = new UpdateProcessor(PackageMain.returnUpdaterState(), updThrd.getUpdatePropertiesFile(),
				updThrd.getDetectionFiles(), updThrd.getSelectedInstallation(),
				PackageMain.returnUpdaterState().getSharedLogger(), updThrd.getUpdaterFunctions(), this,
				updThrd.getModulePath(), updThrd.getModuleClassName(), updThrd.getReleaseDate(),
				updThrd.getDescriptionText());
		proc.start();

	}

	@Override
	public boolean canCancel(PageWindowTemplate window) {
		if (JOptionPane.showConfirmDialog(window, translator.getTranslation("wizard.cancelconfirm.text"),
				translator.getTranslation("wizard.cancelconfirm.title"), JOptionPane.YES_NO_OPTION) == 0) {
			return true;
		}
		return false;
	}

	@Override
	public void onCancel(PageWindowTemplate window) {

	}

	@Override
	public void onUpdateDone(boolean succeed) {
		SwingUtilities.invokeLater(() -> {
			setAllowPageMovements(true);
			if (succeed) {
				getParentingWindow().showEndWindow(new UpdateFinishedPage());
			} else {
				getParentingWindow().showEndWindow(new UpdateInterruptedPage());

			}
		});
	}

	@Override
	public void onNewUpdateStatus(String newStatus) {
		SwingUtilities.invokeLater(() -> {
			statusText.setText(newStatus);
		});
	}

	@Override
	public void onNewDetailsAvailable(String newDetails) {
		SwingUtilities.invokeLater(() -> {
			detailsText.setText(newDetails);
		});

	}

	@Override
	public void onNewPercentage(int percentage) {
		SwingUtilities.invokeLater(() -> {
			progressBar.setValue(percentage);
		});

	}

	@Override
	public void onNewProgressBarIndetermination(boolean isIndeterminate) {
		SwingUtilities.invokeLater(() -> {
			progressBar.setIndeterminate(isIndeterminate);
		});
	}

	@Override
	public boolean onError(String details) {
		return JOptionPane.showConfirmDialog(getParentingWindow(),
				translator.getTranslation("wizard.error.recoverable", details),
				translator.getTranslation("wizard.generic.error"), JOptionPane.YES_NO_OPTION,
				JOptionPane.ERROR_MESSAGE) == 0;

	}

	@Override
	public void onUnrecoverableError(String details) {
		JOptionPane.showMessageDialog(getParentingWindow(),
				translator.getTranslation("wizard.error.unrecoverable", details),
				translator.getTranslation("wizard.generic.error"), JOptionPane.ERROR_MESSAGE);

	}

}
