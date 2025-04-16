/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PropertiesLoader {

	private File root;
	private SharedLogger logger;
	private Map<String, Properties> loadedProperties;

	public PropertiesLoader(File root, SharedLogger logger) {
		this.root = root;
		this.logger = logger;
		loadedProperties = new HashMap<String, Properties>();
	}

	public File getPropertiesRootFolder() {
		return root;
	}

	public boolean loadAll() {
		if (!root.isDirectory()) {
			logger.log("SUPL", LogLevel.WARNING, "No properties will be loaded because the path is not a directory.");
			return false;
		}
		for (File currentFile : root.listFiles()) {
			if (currentFile.isFile() && currentFile.getName().endsWith(".properties")) {
				Properties propsFile = new Properties();
				FileInputStream is;
				boolean isValid = true;
				try {
					is = new FileInputStream(currentFile);
				} catch (FileNotFoundException e) {
					logger.log("SUPL", LogLevel.WARNING, "Failed to load properties file " + currentFile.getName());
					return false;
				}
				try {
					propsFile.load(is);
				} catch (IOException e1) {
					logger.log("SUPL", LogLevel.WARNING,
							"Failed to load properties from file " + currentFile.getName());
					isValid = false;
				}
				try {
					is.close();
				} catch (IOException e) {
					logger.log("SUPL", LogLevel.WARNING, "Failed to close properties file " + currentFile.getName());
				}
				if (!isValid) {
					return false;
				} else {
					loadedProperties.put(currentFile.getName().toLowerCase(), propsFile);
				}
			}
		}
		return true;
	}

	public Properties getProperties(String propsFileName) {
		return loadedProperties.get(propsFileName.toLowerCase());
	}

}
