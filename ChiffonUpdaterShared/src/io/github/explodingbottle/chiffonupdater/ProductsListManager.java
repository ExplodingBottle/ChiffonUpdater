/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

class ProductsListManager {

	private File productsList;
	private SharedLogger logger;

	public ProductsListManager(File productsList, SharedLogger logger) {
		this.productsList = productsList;
		this.logger = logger;
	}

	public Map<File, String> update(List<File> filesToUpdate, List<File> filesToRemove) {
		Properties productsListProprtyFile = new Properties();
		boolean loadFailed = false;
		if (productsList.exists()) {
			FileInputStream inputStream = null;
			try {
				inputStream = new FileInputStream(productsList);
			} catch (FileNotFoundException e) {
				logger.log("PLMG", LogLevel.WARNING, "Failed to open the product list.");
				return null;
			}
			try {
				productsListProprtyFile.load(inputStream);
			} catch (IOException e) {
				loadFailed = true;
				logger.log("PLMG", LogLevel.WARNING, "Failed to read the product list.");
			}
			try {
				inputStream.close();
			} catch (IOException e) {
				logger.log("PLMG", LogLevel.WARNING, "Failed to close the product list.");
			}
		}
		if (!loadFailed) {
			int removals = 0, additions = 0;
			if (filesToRemove != null) {
				for (File toRemove : filesToRemove) {
					String canonicalPath = null;
					try {
						canonicalPath = toRemove.getCanonicalPath();
					} catch (IOException e) {
						logger.log("PLMG", LogLevel.WARNING,
								"Failed to compute a canonical path for " + toRemove.getAbsolutePath() + ".");
					}

					if (productsListProprtyFile.remove(canonicalPath) != null) {
						removals++;
					}
				}
			}
			List<String> additionalFilesToRemove = new ArrayList<String>();
			productsListProprtyFile.forEach((_filePath, _hash) -> {
				String filePath = (String) _filePath, hash = (String) _hash;
				File toHash = new File(filePath);
				if (!toHash.exists()) {
					String canonicalPath = null;
					try {
						canonicalPath = toHash.getCanonicalPath();
					} catch (IOException e) {
						logger.log("PLMG", LogLevel.WARNING,
								"Failed to compute a canonical path for " + toHash.getAbsolutePath() + ".");
					}
					additionalFilesToRemove.add(canonicalPath);
				} else {
					HashComputer computer = new HashComputer(toHash, logger);
					String computed = computer.computeHash();
					if (computed != null) {
						if (!hash.equalsIgnoreCase(computed)) {
							additionalFilesToRemove.add(filePath);
						}
					}
				}

			});
			for (String toRemove : additionalFilesToRemove) {
				if (productsListProprtyFile.remove(toRemove) != null) {
					removals++;
				}
			}
			;
			if (filesToUpdate != null) {
				for (File toUpdate : filesToUpdate) {
					HashComputer computer = new HashComputer(toUpdate, logger);
					String hash = computer.computeHash();
					if (hash != null) {
						productsListProprtyFile.setProperty(toUpdate.getAbsolutePath(), hash);
						additions++;
					}
				}
			}
			FileOutputStream fos;
			try {
				fos = new FileOutputStream(productsList);
			} catch (FileNotFoundException e) {
				logger.log("PLMG", LogLevel.WARNING, "Failed to open for write the product list.");
				return null;
			}
			try {
				productsListProprtyFile.store(fos, "#############################################################\r\n"
						+ " ChiffonUpdater Managed File" + "\r\n Contains the list of path for each registred products."
						+ "\r\n\r\n DO NOT MODIFY UNLESS YOU KNOW WHAT YOU ARE DOING!\r\n"
						+ "##############################################################");
			} catch (IOException e1) {
				logger.log("PLMG", LogLevel.WARNING, "Failed to write the product list.");
			}
			try {
				fos.close();
			} catch (IOException e) {
				logger.log("PLMG", LogLevel.WARNING, "Failed to close the product list.");
			}
			logger.log("PLMG", LogLevel.INFO,
					"Update ran. Results: " + removals + " removals, " + additions + " additions.");
			Map<File, String> toReturn = new HashMap<File, String>();
			productsListProprtyFile.forEach((filePath, hash) -> {
				toReturn.put(new File((String) filePath), (String) hash);
			});
			return toReturn;
		}
		return null;
	}

}
