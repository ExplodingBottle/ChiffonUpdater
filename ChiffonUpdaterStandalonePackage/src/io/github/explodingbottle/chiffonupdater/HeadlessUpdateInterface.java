/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

public class HeadlessUpdateInterface implements UpdateInterface {

	@Override
	public void onUpdateDone(boolean succeed) {
		if (succeed) {
			PackageMain.returnUpdaterState().getSharedLogger().log("HDUI", LogLevel.INFO, "Update finished, now leaving.");
			System.exit(0);
		} else {
			PackageMain.returnUpdaterState().getSharedLogger().log("HDUI", LogLevel.INFO,
					"Update interrupted or cancelled.");
			System.exit(-1);
		}

	}

	@Override
	public void onNewUpdateStatus(String newStatus) {

	}

	@Override
	public void onNewDetailsAvailable(String newDetails) {
		PackageMain.returnUpdaterState().getSharedLogger().log("HDUI", LogLevel.INFO, newDetails);

	}

	@Override
	public void onNewPercentage(int percentage) {

	}

	@Override
	public void onNewProgressBarIndetermination(boolean isIndeterminate) {

	}

	@Override
	public boolean onError(String details) {
		PackageMain.returnUpdaterState().getSharedLogger().log("HDUI", LogLevel.ERROR,
				details + ". This is not an unrecoverable error, please start the setup with interface to try again.");
		return false;
	}

	@Override
	public void onUnrecoverableError(String details) {
		PackageMain.returnUpdaterState().getSharedLogger().log("HDUI", LogLevel.ERROR,
				details + ". This is an unrecoverable error, the update will now stop.");

	}

}
