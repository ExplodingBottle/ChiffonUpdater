/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

class UpdaterPathProvider {

	private OperatingSystemDetector osDetector;
	private SharedLogger logger;

	private File chosenPath;

	public UpdaterPathProvider(OperatingSystemDetector detector, SharedLogger logger) {
		osDetector = detector;
		this.logger = logger;
	}

	public void updatePathFromProperties(Properties propFile) {

		if (!osDetector.hasAlreadyDetected()) {
			osDetector.detectOperatingSystem(); // In case we forget to do it before.
		}
		String pathFound = null;
		if (propFile.getProperty("use-global").equalsIgnoreCase("TRUE")) {
			pathFound = propFile.getProperty("global-path");
		} else {
			DetectedOperatingSystem operatingSystem = osDetector.getCurrentOperatingSystem();
			if (operatingSystem == DetectedOperatingSystem.WINDOWS) {
				pathFound = propFile.getProperty("windows-path");
			}
			if (operatingSystem == DetectedOperatingSystem.MAC) {
				pathFound = propFile.getProperty("mac-path");
			}
			if (operatingSystem == DetectedOperatingSystem.UNIX) {
				pathFound = propFile.getProperty("unix-path");
			}
		}
		if (pathFound != null) {
			String newString = "";
			char[] array = pathFound.toCharArray();
			for (int i = 0; i < array.length; i++) {
				if (array[i] == '$' || array[i] == '%') {
					char specialCharacter = array[i];
					if (array.length > i + 1) {
						if (array[i + 1] != specialCharacter) {
							String formedString = "";
							for (int j = i + 1; j < array.length; j++) {
								if (array[j] != specialCharacter) {
									formedString += array[j];
								} else {
									i += j - i;
									break;
								}
							}
							String found;
							if (specialCharacter == '$') {
								found = System.getProperty(formedString);
							} else {
								found = System.getenv(formedString);
							}
							if (found != null) {
								newString += found;
							}
						}
					}
				} else {
					newString += array[i];
				}
			}
			chosenPath = new File(newString);
		}
	}

	public void updatePathFromStream(InputStream input) {
		Properties propFile = new Properties();
		try {
			propFile.load(input);
		} catch (IOException e) {
			logger.log("PPROV", LogLevel.ERROR, "Failed to load the properties.");
			return;
		}
		updatePathFromProperties(propFile);

	}

	public File getAccessibleFolder() {
		if (chosenPath == null) {
			return null;
		}
		if (chosenPath.exists() || chosenPath.mkdirs()) {
			return chosenPath;
		}
		return null;
	}

}
