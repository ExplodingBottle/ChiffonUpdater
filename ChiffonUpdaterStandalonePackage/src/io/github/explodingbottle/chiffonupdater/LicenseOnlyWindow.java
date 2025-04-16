/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

public class LicenseOnlyWindow extends PageWindowTemplate {

	private static final long serialVersionUID = 632875375207104537L;

	public LicenseOnlyWindow(UpdaterThread thread) {
		Translator translator = PackageMain.getTranslator();
		setTitle(translator.getTranslation("wizard.update.title"));

		String eulaText = thread.getEULAText();
		addWizardPage(new LicenseAgreementPage(eulaText, thread.getEulaFileReference()));
		addWizardPage(new HeadlessUpdateStartPage(thread));
		updateCurrentPage();

		UpdaterState state = PackageMain.returnUpdaterState();
		state.setCancelCallback(new UninstallFastCancelCallback());
	}
}
