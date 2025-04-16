/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

public interface UpdateCancelledCallback {

	public boolean canCancel(PageWindowTemplate window);

	public void onCancel(PageWindowTemplate window);

}
