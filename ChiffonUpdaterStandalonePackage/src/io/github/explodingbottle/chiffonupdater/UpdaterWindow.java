/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.text.DateFormat;
import java.util.Date;

public class UpdaterWindow extends PageWindowTemplate {

	private static final long serialVersionUID = 5269712994589111400L;

	public UpdaterWindow(UpdaterThread thread) {
		Translator translator = PackageMain.getTranslator();

		setTitle(translator.getTranslation("wizard.update.title"));

		DateFormat df = DateFormat.getDateInstance();

		String formattedDate = df.format(new Date(thread.getReleaseDate()));

		addWizardPage(new WindowWelcomePage(translator.getTranslation("welcome.message", thread.getTargetProductName(),
				thread.getTargetVersion(), formattedDate)));

		String eulaText = thread.getEULAText();
		if (thread.shouldShowDescription()) {
			addWizardPage(new DescriptionPage(thread.getDescriptionText()));
		}
		if (eulaText != null) {
			addWizardPage(new LicenseAgreementPage(eulaText, thread.getEulaFileReference()));
		}
		addWizardPage(new LocationSelectionPage(thread));
		addWizardPage(new PreInstallPage());
		addWizardPage(new UpdateInstallPage(thread));

		updateCurrentPage();

		UpdaterState state = PackageMain.returnUpdaterState();
		state.setCancelCallback(new DirectCancelCallback());
	}

}
