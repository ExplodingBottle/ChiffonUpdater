/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

class VersionsRollbackEnumerator {

	private File productDatabaseFolder;
	private SharedLogger enumeratorLogger;

	private static final String COMPONENT_NAME = "VREN";

	public VersionsRollbackEnumerator(SharedLogger enumeratorLogger, File productDatabaseFolder) {
		this.productDatabaseFolder = productDatabaseFolder;
		this.enumeratorLogger = enumeratorLogger;
	}

	public ProductRollbackInformations enumerateRollbackVersions() {
		enumeratorLogger.log(COMPONENT_NAME, LogLevel.INFO,
				"Enumerating versions that can be rolled back with product database "
						+ productDatabaseFolder.getAbsolutePath());
		File updateChain = new File(productDatabaseFolder, HardcodedValues.returnUpdateChainFileName());
		if (!updateChain.exists() || updateChain.isDirectory()) {
			enumeratorLogger.log(COMPONENT_NAME, LogLevel.WARNING, "Missing update chain or it is a directory.");
			return new ProductRollbackInformations();
		}
		UpdateChainManager updChMgr = new UpdateChainManager(updateChain);
		List<String> versions = new ArrayList<String>();
		if (!updChMgr.getVersionsChain(versions)) {
			enumeratorLogger.log(COMPONENT_NAME, LogLevel.WARNING, "Couldn't aquire versions chain data.");
			return new ProductRollbackInformations();
		}

		Map<String, Properties> rollbackInformationsPerFolder = new HashMap<String, Properties>();
		for (int i = versions.size() - 1; i > 0; i--) { // Note: i > 0 is the right way to go as the first version
														// doesn't have a rollback folder.
			String currentVersion = versions.get(i);
			File versionRollbackData = new File(productDatabaseFolder, currentVersion);
			if (!versionRollbackData.exists() || !versionRollbackData.isDirectory()) {
				enumeratorLogger.log(COMPONENT_NAME, LogLevel.WARNING,
						"Version rollback folder for " + currentVersion + " doesn't exist.");
			}
			File rollbackPropertiesFile = new File(versionRollbackData, HardcodedValues.returnRollbackFileName());
			if (!rollbackPropertiesFile.exists() || rollbackPropertiesFile.isDirectory()) {
				enumeratorLogger.log(COMPONENT_NAME, LogLevel.WARNING,
						"Version rollback properties file for " + currentVersion + " doesn't exist.");
			}

			Properties rollbackProperties = new Properties();
			try (FileInputStream fis = new FileInputStream(rollbackPropertiesFile)) {
				rollbackProperties.load(fis);
				rollbackInformationsPerFolder.put(currentVersion, rollbackProperties);
			} catch (IOException e) {
				enumeratorLogger.log(COMPONENT_NAME, LogLevel.WARNING,
						"Failed to read the rollback informations of version " + currentVersion + ".");
			}
		}
		boolean chainBroken = false;
		List<VersionRollbackTargetInformation> vinfos = new ArrayList<VersionRollbackTargetInformation>();
		for (int i = versions.size() - 1; i > 0; i--) {
			String updateToRemove = versions.get(i);
			String targetVersion = versions.get(i - 1);
			Properties currVersionRollbackProps = rollbackInformationsPerFolder.get(updateToRemove);
			Properties targetVersionProps = rollbackInformationsPerFolder.get(targetVersion);
			if (currVersionRollbackProps == null && !chainBroken) {
				enumeratorLogger.log(COMPONENT_NAME, LogLevel.WARNING,
						"Rollback chain broke at version " + currVersionRollbackProps + ".");
				chainBroken = true;
				if (i == versions.size() - 1) {
					return new ProductRollbackInformations(null, null); // No updates can be rolled back
				}
			}
			String updateDescription = null;
			Long installDate = null, updReleaseDate = null;

			if (targetVersionProps != null) {
				updateDescription = targetVersionProps.getProperty("rollback.configuration.description");
				String strRelDate = targetVersionProps.getProperty("rollback.configuration.releasedate");
				String strInsDate = targetVersionProps.getProperty("rollback.configuration.installdate");

				try {
					installDate = Long.parseLong(strInsDate);
				} catch (NumberFormatException e) {
					enumeratorLogger.log(COMPONENT_NAME, LogLevel.WARNING,
							"Couldn't parse install date for target version " + targetVersion + ".");
				}
				try {
					updReleaseDate = Long.parseLong(strRelDate);
				} catch (NumberFormatException e) {
					enumeratorLogger.log(COMPONENT_NAME, LogLevel.WARNING,
							"Couldn't parse release date for target version " + targetVersion + ".");
				}
			}
			vinfos.add(new VersionRollbackTargetInformation(
					chainBroken ? VersionRollbackState.MISSING_ROLLBACK_FILES : VersionRollbackState.CAN_ROLLBACK,
					targetVersion, updateToRemove, updateDescription, installDate, updReleaseDate));

		}
		return new ProductRollbackInformations(vinfos, versions);
	}

}
