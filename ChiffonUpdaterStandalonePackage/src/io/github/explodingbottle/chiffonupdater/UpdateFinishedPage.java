/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

public class UpdateFinishedPage extends WindowWelcomePage {

	private static final long serialVersionUID = -4406522626064408657L;

	public UpdateFinishedPage() {
		super(PackageMain.getTranslator().getTranslation("finished.message"));
	}

}
