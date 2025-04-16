/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

public interface UpdateInterface {

	public void onUpdateDone(boolean succeed);

	public void onNewUpdateStatus(String newStatus);

	public void onNewDetailsAvailable(String newDetails);

	public void onNewPercentage(int percentage);

	public void onNewProgressBarIndetermination(boolean isIndeterminate);

	public boolean onError(String details);

	public void onUnrecoverableError(String details);

}
