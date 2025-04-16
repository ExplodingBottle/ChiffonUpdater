/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

public class HeadlessUpdateStartPage extends WizardPage {

	private static final long serialVersionUID = 8029435257732078446L;

	private UpdaterThread thread;

	public HeadlessUpdateStartPage(UpdaterThread thread) {
		this.thread = thread;
	}

	@Override
	public void pageShown() {
		thread.startInstallHeadlessCallback();
		getParentingWindow().setVisible(false);
		getParentingWindow().dispose();
	}

}
