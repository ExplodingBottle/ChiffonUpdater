/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.GZIPInputStream;

public class DetectionFilesManager {

	private URI detectionPoolURI;
	private URI detectionIndexURI;
	private SharedLogger logger;
	private File detArchives;

	private static final String CMPN = "DFMG";

	public DetectionFilesManager(File localDetFolder, URI backendUri) {
		detArchives = new File(localDetFolder, "Archives");
		URI detectionRootURI = backendUri.resolve("detection/");
		detectionPoolURI = detectionRootURI.resolve("pool/");
		detectionIndexURI = detectionRootURI.resolve("detidx.dat");
		logger = AgentMain.getSharedLogger();
	}

	public boolean prepareFolderStructure() {
		if (!detArchives.exists() && !detArchives.mkdir()) {
			logger.log(CMPN, LogLevel.ERROR,
					"prepareFolderStructure() failed because the detection files archives folder couldn't be created.");
			return false;
		}
		return true;
	}

	public List<Properties> loadDetectionPropertiesList() {
		logger.log(CMPN, LogLevel.INFO, "Downloading and reading detection index file.");
		File detIndexFile = new File(detArchives, "index.dat");
		URL detIndexUrl = null;
		try {
			detIndexUrl = detectionIndexURI.toURL();
		} catch (MalformedURLException e) {
			logger.log(CMPN, LogLevel.ERROR, "Failed to cast the detection index URL.");
			return null;
		}
		DownloaderClass detIndexDler = new DownloaderClass(detIndexFile, detIndexUrl);
		if (!detIndexDler.download()) {
			logger.log(CMPN, LogLevel.ERROR, "Failed to download the detection index file.");
			return null;
		}
		Properties detectionIndex = new Properties();
		try (GZIPInputStream input = new GZIPInputStream(new FileInputStream(detIndexFile))) {
			detectionIndex.load(input);
		} catch (IOException e) {
			logger.log(CMPN, LogLevel.WARNING, "Failed to parse the detection index file.");
			return null;
		}
		Map<File, String> neededArchiveFiles = new HashMap<File, String>();
		for (String name : detectionIndex.stringPropertyNames()) {
			String hash = detectionIndex.getProperty(name);
			File requiredFile = new File(detArchives, hash + ".det");
			boolean mustDownload = true;
			if (requiredFile.exists()) {
				HashComputer hashCptr = new HashComputer(requiredFile, logger);
				if (hash.equals(hashCptr.computeHash())) {
					mustDownload = false;
				}
			}
			if (mustDownload) {
				URL detFileURL = null;
				try {
					detFileURL = detectionPoolURI.resolve(hash + ".det").toURL();
				} catch (MalformedURLException e) {
					logger.log(CMPN, LogLevel.ERROR, "Failed to cast the detection file URL.");
					return null;
				}

				logger.log(CMPN, LogLevel.INFO, "Will download detection file " + hash + ".det");
				DownloaderClass detFile = new DownloaderClass(requiredFile, detFileURL);
				if (!detFile.download()) {
					logger.log(CMPN, LogLevel.ERROR, "Failed to download the detection file.");
					return null;
				}
			}
			neededArchiveFiles.put(requiredFile, hash);
		}
		for (File detFile : detArchives.listFiles()) {
			if (!detFile.equals(detIndexFile) && !neededArchiveFiles.containsKey(detFile)) {
				if (detFile.delete()) {
					logger.log(CMPN, LogLevel.INFO, "Deleted unused detection file " + detFile.getName() + ".");
				} else {
					logger.log(CMPN, LogLevel.WARNING,
							"Failed to delete an unused detection file " + detFile.getName() + ".");
				}
			}
		}
		List<Properties> detectionProperties = new ArrayList<Properties>();
		for (File toUnzip : neededArchiveFiles.keySet()) {
			try (GZIPInputStream input = new GZIPInputStream(new FileInputStream(toUnzip))) {
				Properties props = new Properties();
				props.load(input);
				detectionProperties.add(props);
			} catch (IOException e) {
				logger.log(CMPN, LogLevel.ERROR, "Failed to uncompress detection file " + toUnzip.getName() + ".");
				return null;
			}
		}
		return detectionProperties;

	}

}
