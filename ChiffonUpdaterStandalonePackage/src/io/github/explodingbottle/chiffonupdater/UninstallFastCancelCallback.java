/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

public class UninstallFastCancelCallback implements UpdateCancelledCallback {

	@Override
	public boolean canCancel(PageWindowTemplate window) {
		return true;
	}

	@Override
	public void onCancel(PageWindowTemplate window) {
		window.setVisible(false);
		window.dispose();
	}

}
