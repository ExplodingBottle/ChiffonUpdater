/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.io.File;

class OperatingSystemDetector {

	private boolean alreadyDetected;
	private DetectedOperatingSystem currentOperatingSystem;

	public void detectOperatingSystem() {
		if (alreadyDetected)
			throw new IllegalStateException("Detection has been already done.");
		alreadyDetected = true;

		// Not going to rely on System.getProperty("os.name") here.

		// Windows detection
		String strWinDir = System.getenv("WINDIR");
		if (strWinDir != null && System.getenv("OS") != null) {
			File testFile = new File(strWinDir, "System");
			if (testFile.exists()) { // Just to be sure
				currentOperatingSystem = DetectedOperatingSystem.WINDOWS;
				return;
			}
		}

		// Mac OS detection
		if (new File("/Library").exists() && new File("/System").exists() && new File("/Applications").exists()) {
			currentOperatingSystem = DetectedOperatingSystem.MAC;
			return;
		}

		// Let's say it is UNIX by default.
		currentOperatingSystem = DetectedOperatingSystem.UNIX;
	}

	public boolean hasAlreadyDetected() {
		return alreadyDetected;
	}

	public DetectedOperatingSystem getCurrentOperatingSystem() {
		if (!alreadyDetected)
			throw new IllegalStateException("Detection has not been done yet.");
		return currentOperatingSystem;
	}

}
