/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

public class DirectCancelCallback implements UpdateCancelledCallback {

	@Override
	public void onCancel(PageWindowTemplate window) {
		window.updateCancelledDisplay();
	}

	@Override
	public boolean canCancel(PageWindowTemplate window) {
		return true;
	}

}
