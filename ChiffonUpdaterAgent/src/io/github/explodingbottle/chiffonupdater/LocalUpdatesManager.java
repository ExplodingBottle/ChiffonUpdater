/*
 * This file is part of ChiffonUpdater
 *
 * SPDX-License-Identifier: MIT
 */

package io.github.explodingbottle.chiffonupdater;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class LocalUpdatesManager {

	private URI productsRootURI;
	private URI poolURI;
	private URI productsListURI;
	private File localUpdatesRoot;
	private File localUpdatesInfos;
	private File localUpdatesDownload;
	private File localUpdatesExtracted;
	private SharedLogger logger;

	private static final String CMPN = "LUMG";

	public LocalUpdatesManager(File localUpdates, URI backendUri) {
		logger = AgentMain.getSharedLogger();
		localUpdatesRoot = localUpdates;
		localUpdatesInfos = new File(localUpdates, "Infos");
		localUpdatesDownload = new File(localUpdates, "Download");
		localUpdatesExtracted = new File(localUpdates, "Extracted");
		productsRootURI = backendUri.resolve("products/");
		poolURI = backendUri.resolve("pool/");
		productsListURI = productsRootURI.resolve("products.dat");
	}

	private boolean downloadIfNeeded(File targetFile, String hash, URI downloadURI) {
		boolean mustRedownload = true;
		if (targetFile.exists()) {
			HashComputer hashCptr = new HashComputer(targetFile, logger);
			String computedHash = hashCptr.computeHash();
			if (computedHash == null) {
				logger.log(CMPN, LogLevel.WARNING,
						"Failed to compute the hash of the locally stored " + targetFile.getName() + " file.");
			} else if (hash.equals(computedHash)) {
				mustRedownload = false;
			} else {
				logger.log(CMPN, LogLevel.WARNING, "Hash for " + targetFile.getName() + " doesn't match.");
			}
		}
		if (mustRedownload) {
			logger.log(CMPN, LogLevel.INFO, "Must download " + targetFile.getName() + " file.");

			URL toDownloadFromURL;
			try {
				toDownloadFromURL = downloadURI.toURL();
			} catch (MalformedURLException e) {
				logger.log(CMPN, LogLevel.ERROR, "File must be downloaded but failed to cast download URL. Failing.");
				return false;
			}

			DownloaderClass dlProd = new DownloaderClass(targetFile, toDownloadFromURL);
			if (!dlProd.download()) {
				logger.log(CMPN, LogLevel.ERROR, "Failed to download the requested file.");
				return false;
			}
			HashComputer hashCptr = new HashComputer(targetFile, logger);
			String computedHash = hashCptr.computeHash();
			if (!hash.equals(computedHash)) {
				logger.log(CMPN, LogLevel.ERROR, "Hash for " + targetFile.getName() + " doesn't match. Failing.");
				return false;
			}
		}
		return true;
	}

	public boolean prepareFolderStructure() {
		if (!localUpdatesInfos.exists() && !localUpdatesInfos.mkdir()) {
			logger.log(CMPN, LogLevel.ERROR, "Couldn't create infos folder for local updates.");
			return false;
		}
		if (!localUpdatesDownload.exists() && !localUpdatesDownload.mkdir()) {
			logger.log(CMPN, LogLevel.ERROR, "Couldn't create download folder for local updates.");
			return false;
		}
		if (!localUpdatesExtracted.exists() && !localUpdatesExtracted.mkdir()) {
			logger.log(CMPN, LogLevel.ERROR, "Couldn't create extracted folder for local updates.");
			return false;
		}
		return true;
	}

	public Properties fetchProductsInformationsList() {
		logger.log(CMPN, LogLevel.INFO, "Downloading and reading products informations list.");
		File localProductsEnum = new File(localUpdatesRoot, "products.dat");
		URL productsListUrl = null;
		try {
			productsListUrl = productsListURI.toURL();
		} catch (MalformedURLException e) {
			logger.log(CMPN, LogLevel.ERROR, "Failed to cast the products list URL.");
			return null;
		}
		DownloaderClass prodsListDler = new DownloaderClass(localProductsEnum, productsListUrl);
		if (!prodsListDler.download()) {
			logger.log(CMPN, LogLevel.ERROR, "Failed to download the products list.");
			return null;
		}
		Properties productsList = new Properties();
		try (GZIPInputStream input = new GZIPInputStream(new FileInputStream(localProductsEnum))) {
			productsList.load(input);
		} catch (IOException e) {
			logger.log(CMPN, LogLevel.ERROR, "Failed to parse the detection index file.");
			return null;
		}
		return productsList;
	}

	public Properties getProductInformationsByHash(String productInfosHash) {
		logger.log(CMPN, LogLevel.INFO, "Downloading and reading product informations with hash " + productInfosHash);
		File existingInfos = new File(localUpdatesInfos, productInfosHash + ".prd");

		URI toDownloadFromURI = productsRootURI.resolve(productInfosHash + ".prd");
		if (!downloadIfNeeded(existingInfos, productInfosHash, toDownloadFromURI)) {
			logger.log(CMPN, LogLevel.ERROR, "Failed acquire the product informations file.");
			return null;
		}
		Properties productInfos = new Properties();
		try (GZIPInputStream input = new GZIPInputStream(new FileInputStream(existingInfos))) {
			productInfos.load(input);
		} catch (IOException e) {
			logger.log(CMPN, LogLevel.ERROR, "Failed to parse the product informations file.");
			return null;
		}
		return productInfos;
	}

	public String getEulaByHash(String eulaHash) {
		logger.log(CMPN, LogLevel.INFO, "Downloading and reading EULA with hash " + eulaHash);
		File existingEULA = new File(localUpdatesDownload, eulaHash + ".bin");

		URI toDownloadFromURI = poolURI.resolve(eulaHash + ".bin");
		if (!downloadIfNeeded(existingEULA, eulaHash, toDownloadFromURI)) {
			logger.log(CMPN, LogLevel.ERROR, "Failed acquire the EULA file.");
			return null;
		}
		String eulaText;
		char[] buffer = new char[4096];
		StringBuilder eulaBldr = new StringBuilder();
		try (BufferedReader input = new BufferedReader(
				new InputStreamReader(new GZIPInputStream(new FileInputStream(existingEULA))))) {
			int read = input.read(buffer, 0, buffer.length);
			while (read != -1) {
				eulaBldr.append(buffer, 0, read);
				read = input.read(buffer, 0, buffer.length);
			}
		} catch (IOException e) {
			logger.log(CMPN, LogLevel.ERROR, "Failed to parse the product informations file.");
			return null;
		}
		eulaText = eulaBldr.toString();
		return eulaText;
	}

	private boolean deletionFolderWalk(File folder, boolean cleanItemToo) {
		boolean succeed = true;
		for (File file : folder.listFiles()) {
			if (file.isDirectory()) {
				succeed &= deletionFolderWalk(file, true);
			} else {
				succeed &= file.delete();
			}
		}
		if (cleanItemToo) {
			succeed &= folder.delete();
		}
		return succeed;
	}

	public boolean cleanupExtractedFolder() {
		logger.log(CMPN, LogLevel.INFO, "Cleaning up the Extracted folder.");
		return deletionFolderWalk(localUpdatesExtracted, false);
	}

	public boolean runCleanupMaintenanceTask(List<DetectionResult> detectionResults) {
		Map<String, Set<String>> installedProductsNames = new HashMap<String, Set<String>>();
		for (DetectionResult res : detectionResults) {
			Set<String> installedVersions = installedProductsNames.get(res.getProductName());
			if (installedVersions == null) {
				installedVersions = new HashSet<String>();
				installedProductsNames.put(res.getProductName(), installedVersions);
			}
			installedVersions.add(res.getVersion());

		}

		logger.log(CMPN, LogLevel.INFO, "Performing the cleanup maintenance task.");
		File localProductsEnum = new File(localUpdatesRoot, "products.dat");
		Properties productsList = new Properties();
		try (GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(localProductsEnum))) {
			productsList.load(gzis);
		} catch (IOException e) {
			logger.log(CMPN, LogLevel.WARNING,
					"Maintenance task failed because it wasn't possible to read the local products list.");
			return false;
		}
		Set<File> usefulFiles = new HashSet<File>();
		Set<File> usefulBinFiles = new HashSet<File>();
		for (String prodName : productsList.stringPropertyNames()) {
			String linkedHash = productsList.getProperty(prodName);
			if (linkedHash == null) {
				continue;
			}
			File infoFile = new File(localUpdatesInfos, linkedHash + ".prd");
			if (!infoFile.exists()) {
				continue;
			}
			HashComputer cptr = new HashComputer(infoFile, logger);
			String hash = cptr.computeHash();
			if (linkedHash.equalsIgnoreCase(hash) && installedProductsNames.containsKey(prodName)) {
				Set<String> installedVersions = installedProductsNames.get(prodName);
				usefulFiles.add(infoFile);
				Properties loadedProdSpecInfs = new Properties();
				try (GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(infoFile))) {
					loadedProdSpecInfs.load(gzis);
				} catch (IOException e) {
					logger.log(CMPN, LogLevel.WARNING,
							"Couldn't read the product informations, skipping the file " + hash + ".prd.");
				}
				String binHash = loadedProdSpecInfs.getProperty("update.binhash");
				String eulaHash = loadedProdSpecInfs.getProperty("update.eulaHash");
				if (installedVersions != null && !(installedVersions.size() == 1
						&& installedVersions.contains(loadedProdSpecInfs.getProperty("update.versionname")))) {
					if (binHash != null) {
						File fl = new File(localUpdatesDownload, binHash + ".bin");
						if (fl.exists()) {
							usefulBinFiles.add(fl);
						}
					}
					if (eulaHash != null) {
						File fl = new File(localUpdatesDownload, eulaHash + ".bin");
						if (fl.exists()) {
							usefulBinFiles.add(fl);
						}
					}
				}

			}
		}
		for (File infoFile : localUpdatesInfos.listFiles()) {
			if (!usefulFiles.contains(infoFile)) {
				if (!infoFile.delete()) {
					logger.log(CMPN, LogLevel.WARNING, "Couldn't clean useless file " + infoFile.getName() + " up.");
				} else {
					logger.log(CMPN, LogLevel.INFO, "Cleaned useless file " + infoFile.getName() + " up.");
				}
			}
		}
		for (File infoFile : localUpdatesDownload.listFiles()) {
			if (!usefulBinFiles.contains(infoFile)) {
				if (!infoFile.delete()) {
					logger.log(CMPN, LogLevel.WARNING, "Couldn't clean useless file " + infoFile.getName() + " up.");
				} else {
					logger.log(CMPN, LogLevel.INFO, "Cleaned useless file " + infoFile.getName() + " up.");
				}
			}
		}
		return cleanupExtractedFolder();
	}

	public File getPreparedUpdateFolder(String updateHash) {
		byte buff[] = new byte[4096];

		logger.log(CMPN, LogLevel.INFO, "Downloading and extracting update with hash " + updateHash);
		File existingUpdate = new File(localUpdatesDownload, updateHash + ".bin");

		URI toDownloadFromURI = poolURI.resolve(updateHash + ".bin");
		if (!downloadIfNeeded(existingUpdate, updateHash, toDownloadFromURI)) {
			logger.log(CMPN, LogLevel.ERROR, "Failed acquire the update binary file.");
			return null;
		}

		File existingExtractedUpdate = new File(localUpdatesExtracted, updateHash);
		if (existingExtractedUpdate.exists() && existingExtractedUpdate.isDirectory()) {
			deletionFolderWalk(existingExtractedUpdate, true);
		}
		if (existingExtractedUpdate.exists() && existingExtractedUpdate.listFiles().length > 0) {
			logger.log(CMPN, LogLevel.ERROR, "Failed to correctly cleanup the folder for extraction.");
			return null;
		}
		if (!existingExtractedUpdate.mkdir()) {
			logger.log(CMPN, LogLevel.ERROR, "Failed to create the folder for extraction.");
			return null;
		}
		try (ZipInputStream zis = new ZipInputStream(new FileInputStream(existingUpdate))) {
			ZipEntry entry = zis.getNextEntry();
			while (entry != null) {
				File targetFile = new File(existingExtractedUpdate, entry.getName());
				if (entry.isDirectory()) {
					if (!targetFile.exists()) {
						if (!targetFile.mkdirs()) {
							logger.log(CMPN, LogLevel.ERROR, "Couldn't create a folder.");
							return null;
						}
					}
				} else {
					FileOutputStream fos = new FileOutputStream(targetFile);
					int read = zis.read(buff, 0, buff.length);
					while (read != -1) {
						fos.write(buff, 0, read);
						read = zis.read(buff, 0, buff.length);
					}
					fos.close();
				}
				zis.closeEntry();
				entry = zis.getNextEntry();
			}
		} catch (IOException e) {
			logger.log(CMPN, LogLevel.ERROR, "Extraction failed because of " + e.getLocalizedMessage());
			return null;
		}

		return existingExtractedUpdate;
	}

}
