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
import java.util.Properties;

public class PersistentStorage {

	private GlobalLogger tkLogger;
	private Properties settings;
	private File settingsFile;

	public PersistentStorage() {
		settings = new Properties();
		tkLogger = ToolkitMain.getGlobalLogger();
		settingsFile = new File(ToolkitMain.getCuToolkitRootPath(), "persistent.properties");
	}

	public void persistentStorageLoad() {
		tkLogger.print("Loading persistent storage (settings)");
		if (!settingsFile.exists()) {
			tkLogger.print("Settings file doesn't exist.");
			return;
		}

		try (FileInputStream fis = new FileInputStream(settingsFile)) {
			settings.load(fis);
		} catch (IOException e) {
			tkLogger.printThrowable(e);
		}
	}

	public Properties getSettings() {
		return settings;
	}

	public void persistentStorageSave() {
		try (FileOutputStream fos = new FileOutputStream(settingsFile)) {
			settings.store(fos, "Persistent data and settings for ChiffonUpdater toolkit.");
		} catch (IOException e) {
			tkLogger.printThrowable(e);
		}
	}

}
