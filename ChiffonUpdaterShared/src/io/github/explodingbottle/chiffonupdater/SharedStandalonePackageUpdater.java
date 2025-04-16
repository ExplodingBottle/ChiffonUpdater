/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

class SharedStandalonePackageUpdater {

	private File accessibleFolderPackageFile;
	private File accessibleFolderPkgVersionsFile;
	private SharedLogger logger;

	private static final String CMPN = "SHPU";

	public SharedStandalonePackageUpdater(File accessibleFolder, SharedLogger logger) {
		this.accessibleFolderPackageFile = new File(accessibleFolder, HardcodedValues.returnLatestPackageFileName());
		this.accessibleFolderPkgVersionsFile = new File(accessibleFolder, HardcodedValues.returnPackageVersionsList());
		this.logger = logger;
	}

	public File updateOrReturnUpdatedFile(File newPackage, File newPackageVersions) {
		Properties loadedPackageVersions = new Properties();
		try (FileInputStream fis = new FileInputStream(newPackageVersions)) {
			loadedPackageVersions.load(fis);
		} catch (IOException e) {
			logger.log(CMPN, LogLevel.WARNING, "Failed to load the new package versions property file.");
			return null;
		}
		return updateOrReturnUpdatedFile(newPackage, loadedPackageVersions);
	}

	public File updateOrReturnUpdatedFile(File newPackage, Properties newPackageVersions) {

		File selectedVersion = null;

		Properties computedPropertiesFile = new Properties();
		boolean mustReplacePkgVersions[] = { false };
		boolean mustReplacePkgBins = false;
		if (accessibleFolderPkgVersionsFile.exists()) {
			try (FileInputStream input = new FileInputStream(accessibleFolderPkgVersionsFile)) {
				computedPropertiesFile.load(input);
			} catch (IOException e) {
				logger.log(CMPN, LogLevel.WARNING, "Failed to read currently installed versions file.");
				return null;
			}
		} else {
			mustReplacePkgVersions[0] = true;
			logger.log(CMPN, LogLevel.INFO, "Package versions file will be installed for the first time.");
		}
		newPackageVersions.forEach((hash, versionId) -> {
			if (!versionId.equals(computedPropertiesFile.get(hash))) {
				mustReplacePkgVersions[0] = true;
				computedPropertiesFile.put(hash, versionId);
			}
		});

		HashComputer computerNew = new HashComputer(newPackage, logger);
		String hashNew = computerNew.computeHash();
		if (hashNew == null) {
			logger.log(CMPN, LogLevel.WARNING, "Failed to compute the hash of the new package.");
			return null;
		}
		Long newVersion = null;
		if (!computedPropertiesFile.containsKey(hashNew)) {
			logger.log(CMPN, LogLevel.WARNING,
					"The computed version file doesn't contain a matching version for the new file.");
			return null;
		} else {
			try {
				newVersion = Long.parseLong(computedPropertiesFile.getProperty(hashNew));
			} catch (NumberFormatException e) {
				logger.log(CMPN, LogLevel.WARNING, "Malformed version number for the new package version.");
				return null;
			}
		}
		if (newVersion != null) {
			if (accessibleFolderPackageFile.exists()) {
				HashComputer computerInstalled = new HashComputer(accessibleFolderPackageFile, logger);
				String hashInstalled = computerInstalled.computeHash();
				Long installedFileVersion = null;
				if (hashInstalled == null) {
					logger.log(CMPN, LogLevel.WARNING, "Failed to compute the hash of the installed package.");
					return null;
				}
				if (!computedPropertiesFile.containsKey(hashInstalled)) {
					logger.log(CMPN, LogLevel.WARNING,
							"The computed version file doesn't contain a matching version for the installed file. Selected as most up-to-date.");
					selectedVersion = accessibleFolderPackageFile;
				} else {
					try {
						installedFileVersion = Long.parseLong(computedPropertiesFile.getProperty(hashInstalled));
					} catch (NumberFormatException e) {
						logger.log(CMPN, LogLevel.WARNING,
								"Malformed version number for the installed package version.");
						return null;
					}
				}
				if (installedFileVersion != null) {
					if (installedFileVersion <= newVersion) {
						logger.log(CMPN, LogLevel.INFO, "New version is the most up-to-date.");
					} else {
						logger.log(CMPN, LogLevel.INFO, "Installed version is the most up-to-date.");
						selectedVersion = accessibleFolderPackageFile;
					}
				}

			} else {
				mustReplacePkgBins = true;
				logger.log(CMPN, LogLevel.INFO, "Standalone package will be installed for the first time.");
			}
		}

		if (mustReplacePkgVersions[0]) {
			try (FileOutputStream outputStream = new FileOutputStream(accessibleFolderPkgVersionsFile)) {
				computedPropertiesFile.store(outputStream,
						"Automatically managed file for package version selection\nMODIFICATION IS DISCOURAGED");
			} catch (IOException e) {
				logger.log(CMPN, LogLevel.WARNING, "Failed installation of the new package file.");
				return null;
			}
		}

		if (mustReplacePkgBins) {
			try {
				Files.copy(newPackage.toPath(), accessibleFolderPackageFile.toPath(),
						StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				logger.log(CMPN, LogLevel.WARNING, "Failed installation of the new package file.");
				return null;
			}
		}

		return selectedVersion;
	}

}
