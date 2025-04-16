/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.io.File;

public class UninstallingWindow extends PageWindowTemplate {

	private static final long serialVersionUID = -8928727235981002870L;

	public UninstallingWindow(ProductRollbackInformations rollbackInfos, SharedLogger logger, File productRoot) {
		setTitle(PackageMain.getTranslator().getTranslation("wizard.downgrade.title"));
		UninstallVersionSelectorPage selecPage = new UninstallVersionSelectorPage(rollbackInfos.getVersions());
		addWizardPage(new UninstallWelcomePage());
		addWizardPage(selecPage);
		addWizardPage(new UninstallConfirmPage(selecPage));
		addWizardPage(new UninstallProcessingPage(selecPage, logger, rollbackInfos, productRoot));

		updateCurrentPage();

		UpdaterState state = PackageMain.returnUpdaterState();
		state.setCancelCallback(new UninstallFastCancelCallback());
	}

}
