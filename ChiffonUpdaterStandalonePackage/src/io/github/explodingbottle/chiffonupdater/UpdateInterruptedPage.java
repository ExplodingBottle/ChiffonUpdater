/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

public class UpdateInterruptedPage extends WindowWelcomePage {

	private static final long serialVersionUID = -8416606800899936414L;

	public UpdateInterruptedPage() {
		super(PackageMain.getTranslator().getTranslation("interrupted.message"));
	}

}
