/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

public class PreInstallPage extends WindowWelcomePage {

	private static final long serialVersionUID = 116068846546131928L;

	public PreInstallPage() {
		super(PackageMain.getTranslator().getTranslation("beforeinstall.message"));
	}

}
