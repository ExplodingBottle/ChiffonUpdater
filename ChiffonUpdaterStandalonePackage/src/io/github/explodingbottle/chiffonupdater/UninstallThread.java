/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.io.File;

import javax.swing.JOptionPane;

public class UninstallThread extends Thread implements UninstallInterface {

	private String uninstVers;
	private SharedLogger logger;

	public UninstallThread(String uninstVers) {
		this.uninstVers = uninstVers;
	}

	public void run() {
		Translator translator = PackageMain.getTranslator();

		UpdaterState state = PackageMain.returnUpdaterState();
		logger = state.getSharedLogger();
		logger.log("UNST", LogLevel.INFO, "Started uninstall thread.");

		File databaseFolder = new File(state.getUpdateRoot(), HardcodedValues.returnProductSpecificUpdateFolderName());
		if (!databaseFolder.exists()) {
			logger.log("UNST", LogLevel.ERROR, "No database folder, cannot continue.");
			if (!state.shouldBeHeadless())
				JOptionPane.showMessageDialog(null, translator.getTranslation("downgrade.nodb"),
						translator.getTranslation("wizard.generic.error"), JOptionPane.ERROR_MESSAGE);
			return;
		}

		VersionsRollbackEnumerator versionsEnumerator = new VersionsRollbackEnumerator(logger, databaseFolder);
		ProductRollbackInformations infos = versionsEnumerator.enumerateRollbackVersions();

		if (infos.getState() == RollbackEnvironmentState.CONFIG_NONPRESENT) {
			logger.log("UNST", LogLevel.ERROR, "Global rollback configuration is not present.");
			return;
		}
		if (infos.getState() == RollbackEnvironmentState.ALL_UPDATES_BROKEN) {
			logger.log("UNST", LogLevel.ERROR, "No updates can be rolled back.");
			if (!state.shouldBeHeadless())
				JOptionPane.showMessageDialog(null, translator.getTranslation("downgrade.misfiles"),
						translator.getTranslation("downgrade.misfiles.title"), JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (infos.getVersions().size() == 0) {
			logger.log("UNST", LogLevel.ERROR, "No update installed (original version).");
			if (!state.shouldBeHeadless())
				JOptionPane.showMessageDialog(null, translator.getTranslation("downgrade.notpossible"),
						translator.getTranslation("downgrade.notpossible.title"), JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		logger.log("UNST", LogLevel.INFO, "Versions chain loaded: ");
		infos.getVersions().forEach(vers -> {

			logger.log("UNST", LogLevel.INFO, vers.getTargetVersion()
					+ (vers.getState() == VersionRollbackState.MISSING_ROLLBACK_FILES ? " (cannot rollback)" : ""));

		});
		if (!state.shouldBeHeadless()) {
			UninstallingWindow versSelWdw = new UninstallingWindow(infos, logger, state.getUpdateRoot());
			versSelWdw.setVisible(true);
		} else {
			if (uninstVers == null) {
				logger.log("UNST", LogLevel.ERROR, "No version chosen for rollback.");
			}
			logger.log("UNST", LogLevel.INFO, "Starting in headless mode uninstall");
			UninstallProcessor proc = new UninstallProcessor(this, uninstVers, logger, infos.getOriginalVersionsChain(),
					state.getUpdateRoot());
			proc.start();
		}

	}

	@Override
	public void onNewUninstallState(UninstallState newState) {
		logger.log("UNST", LogLevel.INFO, "New uninstall state: " + newState);
		if (newState == UninstallState.FINISHED) {
			logger.log("UNST", LogLevel.INFO, "Uninstall finished, now leaving.");
			System.exit(0);
		}

	}

	@Override
	public void onUnrecoverableError(String error) {
		logger.log("UNST", LogLevel.INFO, "Unrecoverable error: " + error);

	}

	@Override
	public boolean onRecoverableError(String error) {
		logger.log("UNST", LogLevel.INFO, "Recoverable error: " + error + " but cannot continue in headless mode.");
		return false;
	}

	@Override
	public void onUninstallInterrupted() {
		logger.log("UNST", LogLevel.INFO, "Uninstall finished/interrupted.");

	}

}
