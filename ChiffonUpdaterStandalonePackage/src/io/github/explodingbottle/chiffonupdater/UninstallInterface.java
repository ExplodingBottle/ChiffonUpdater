/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

public interface UninstallInterface {

	public void onNewUninstallState(UninstallState newState);

	public void onUnrecoverableError(String error);

	public boolean onRecoverableError(String error);

	public void onUninstallInterrupted();

}
