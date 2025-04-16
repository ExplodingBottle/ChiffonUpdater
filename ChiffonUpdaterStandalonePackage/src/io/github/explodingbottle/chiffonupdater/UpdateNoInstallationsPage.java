/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

public class UpdateNoInstallationsPage extends WindowWelcomePage {

	private static final long serialVersionUID = -8416606800899936414L;

	public UpdateNoInstallationsPage() {
		super(PackageMain.getTranslator().getTranslation("noinstalls.message"));
	}

}
